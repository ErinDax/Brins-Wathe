package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.BombComponent;
import cn.erindax.brinswathe.component.MorticianComponent;
import cn.erindax.brinswathe.component.SniperComponent;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.entity.ZhuimuBoat;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.jetbrains.annotations.Nullable;

public final class Grappler {
    public static final double RANGE = 16.0D;
    private static final double AIM_TOLERANCE = 2.5D;
    private static final double STOP_DISTANCE = 1.25D;
    private static final double STOP_HEIGHT = 1.25D;
    private static final double CLIMB_HEIGHT = 1.25D;
    private static final double STEP = 0.25D;
    private static final int MAX_STEPS = 128;
    private static final double GROUND_PROBE = 0.1D;
    private static final double ROPE_SPACING = 0.5D;

    private Grappler() {
    }

    public static boolean canTarget(Player hooker, Player target) {
        if (target == hooker || !GameFunctions.isPlayerAliveAndSurvival(target)) return false;
        MorticianComponent mortician = MorticianComponent.KEY.get(target);
        return mortician == null || !mortician.isDisguised();
    }

    public static boolean canHook(ServerPlayer hooker, @Nullable ServerPlayer target) {
        Vec3 eye = hooker.getEyePosition();
        Vec3 reach = eye.add(hooker.getViewVector(1.0F).scale(RANGE));
        if (target == null || target.level() != hooker.level() || !canTarget(hooker, target)
            || !isAimedAt(eye, reach, target.getBoundingBox())) {
            return refuse(hooker, Component.translatable("message.brinswathe.grappler.no_target", (int) RANGE));
        }
        if (!hasClearLine(hooker, eye, reach, target.getBoundingBox())) {
            return refuse(hooker, Component.translatable("message.brinswathe.grappler.blocked"));
        }
        if (isAnchored(target)) {
            return refuse(hooker, Component.translatable("message.brinswathe.grappler.immune"));
        }
        return true;
    }

    public static void hook(ServerPlayer hooker, ServerPlayer target) {
        ServerLevel level = hooker.serverLevel();
        Vec3 hand = hooker.getEyePosition().subtract(0.0D, 0.3D, 0.0D);
        Vec3 caught = target.getBoundingBox().getCenter();
        target.stopUsingItem();
        if (target.isSleeping()) target.stopSleepInBed(true, true);
        target.stopRiding();
        Vec3 destination = pull(target, hooker.position());
        target.connection.teleport(
            destination.x, destination.y, destination.z,
            target.getYRot(), target.getXRot(), RelativeMovement.ROTATION
        );
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        target.resetFallDistance();
        Pushed.mark(target);
        RpsManager.reanchor(target);
        int stunTicks = Math.min(ModConfig.skillDurationSeconds("grappler"), Integer.MAX_VALUE / 20) * 20;
        PlayerEffectComponent effect = PlayerEffectComponent.KEY.get(target);
        if (effect != null && effect.stunTicks < stunTicks) effect.setStunTicks(stunTicks);
        target.displayClientMessage(Component.translatable("message.brinswathe.grappler.hooked"), true);
        drawRope(level, hand, caught);
        hooker.swing(InteractionHand.MAIN_HAND, true);
        level.playSound(null, hooker.getX(), hooker.getY(), hooker.getZ(),
            SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 1.0F, 0.6F);
        level.playSound(null, destination.x, destination.y, destination.z,
            SoundEvents.CHAIN_HIT, SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    private static boolean refuse(ServerPlayer hooker, Component reason) {
        hooker.displayClientMessage(reason, true);
        return false;
    }

    private static boolean isAimedAt(Vec3 eye, Vec3 reach, AABB box) {
        AABB lenient = box.inflate(AIM_TOLERANCE);
        return lenient.contains(eye) || lenient.clip(eye, reach).isPresent();
    }

    private static boolean hasClearLine(ServerPlayer hooker, Vec3 eye, Vec3 reach, AABB box) {
        Vec3 center = box.getCenter();
        return isVisible(hooker, eye, box.clip(eye, reach).orElse(center))
            || isVisible(hooker, eye, center)
            || isVisible(hooker, eye, new Vec3(center.x, box.maxY - 0.1D, center.z))
            || isVisible(hooker, eye, new Vec3(center.x, box.minY + 0.1D, center.z));
    }

    private static boolean isVisible(ServerPlayer hooker, Vec3 eye, Vec3 point) {
        return hooker.level().clip(new ClipContext(
            eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, hooker
        )).getType() == HitResult.Type.MISS;
    }

    private static boolean isAnchored(ServerPlayer target) {
        if (target.getVehicle() instanceof ZhuimuBoat) return true;
        BombComponent bomb = BombComponent.KEY.get(target);
        if (bomb != null && bomb.isMinePinned()) return true;
        SniperComponent sniper = SniperComponent.KEY.get(target);
        return sniper != null && sniper.isAiming();
    }

    private static Vec3 pull(ServerPlayer target, Vec3 destination) {
        AABB box = target.getBoundingBox();
        Vec3 position = target.position();
        Vec3 grounded = null;
        for (int i = 0; i < MAX_STEPS; i++) {
            Vec3 footing = land(target, box, position, GROUND_PROBE);
            if (footing != null) grounded = footing;
            Vec3 remaining = destination.subtract(position);
            if (remaining.lengthSqr() < 1.0E-8D) return position;
            if (remaining.horizontalDistanceSqr() <= STOP_DISTANCE * STOP_DISTANCE
                && Math.abs(remaining.y) <= STOP_HEIGHT) {
                Vec3 landing = footing != null ? footing : land(target, box, position, STOP_HEIGHT - remaining.y);
                if (landing != null) return landing;
            }
            Vec3 moved = advance(target, box, remaining);
            if (moved.lengthSqr() < 1.0E-8D) break;
            box = box.move(moved);
            position = position.add(moved);
        }
        Vec3 landing = land(target, box, position, STOP_HEIGHT);
        if (landing != null) return landing;
        return grounded != null ? grounded : position;
    }

    private static Vec3 advance(ServerPlayer target, AABB box, Vec3 remaining) {
        double distance = remaining.length();
        Vec3 step = remaining.scale(Math.min(STEP, distance) / distance);
        Vec3 moved = collide(target, box, step);
        boolean blockedX = moved.x != step.x;
        boolean blockedY = moved.y != step.y;
        boolean blockedZ = moved.z != step.z;
        if (!blockedX && !blockedY && !blockedZ) return moved;
        if (blockedX || blockedZ) {
            Vec3 up = collide(target, box, new Vec3(0.0D, CLIMB_HEIGHT, 0.0D));
            if (up.y > 0.0D) {
                AABB raised = box.move(up);
                Vec3 over = collide(target, raised, new Vec3(step.x, 0.0D, step.z));
                if (over.horizontalDistanceSqr() > moved.horizontalDistanceSqr()) {
                    Vec3 down = collide(target, raised.move(over), new Vec3(0.0D, -up.y, 0.0D));
                    return up.add(over).add(down);
                }
            }
        }
        Vec3 free = new Vec3(
            blockedX ? 0.0D : remaining.x,
            blockedY ? 0.0D : remaining.y,
            blockedZ ? 0.0D : remaining.z
        );
        double reach = free.length();
        if (reach < 1.0E-4D) return moved;
        Vec3 slid = collide(target, box, free.scale(Math.min(STEP, reach) / reach));
        return slid.lengthSqr() > moved.lengthSqr() ? slid : moved;
    }

    @Nullable
    private static Vec3 land(ServerPlayer target, AABB box, Vec3 position, double drop) {
        double fall = collide(target, box, new Vec3(0.0D, -drop, 0.0D)).y;
        return fall > -drop ? position.add(0.0D, fall, 0.0D) : null;
    }

    private static Vec3 collide(ServerPlayer target, AABB box, Vec3 movement) {
        List<VoxelShape> entityShapes = target.level().getEntityCollisions(target, box.expandTowards(movement));
        return Entity.collideBoundingBox(target, movement, box, target.level(), entityShapes);
    }

    private static void drawRope(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 line = to.subtract(from);
        int segments = Math.max(1, (int) Math.ceil(line.length() / ROPE_SPACING));
        for (int i = 0; i <= segments; i++) {
            Vec3 point = from.add(line.scale((double) i / segments));
            level.sendParticles(ParticleTypes.CRIT, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }
}
