package cn.erindax.brinswathe;

import cn.erindax.brinswathe.config.ModConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.jetbrains.annotations.Nullable;

public final class HalfSpirit {
    private static final double REACH = 1.5D;
    private static final double MAX_SPAN = 1.25D;
    private static final double SCAN_STEP = 0.0625D;
    private static final int LIFT_STEPS = 10;
    private static final double STEP_HEIGHT = 0.6D;
    private static final double MAX_DROP = 3.0D;
    private static final double GROUND_PROBE = 0.1D;
    private static final double FOOTING_INSET = 0.2D;
    private static final double TOLERANCE = 1.0E-5D;
    private static final int NO_WALL = 0;
    private static final int TOO_THICK = 1;
    private static final int UNSAFE = 2;
    private static final String[] PROBLEMS = {"no_wall", "too_thick", "unsafe"};

    private HalfSpirit() {
    }

    @Nullable
    public static Vec3 findLanding(ServerPlayer player) {
        if (player.isPassenger() || player.isSleeping() || isStunned(player)) return refuse(player, "busy");
        Direction facing = player.getDirection();
        Vec3 forward = new Vec3(facing.getStepX(), 0.0D, facing.getStepZ());
        Vec3 position = player.position();
        AABB box = player.getBoundingBox();
        Vec3 centring = new Vec3(
            Math.abs(forward.z) * (Math.floor(position.x) + 0.5D - position.x),
            0.0D,
            Math.abs(forward.x) * (Math.floor(position.z) + 0.5D - position.z)
        );
        Search centred = search(player, box.move(centring), forward);
        if (centred.landing() != null) return centred.landing();
        Search direct = search(player, box, forward);
        if (direct.landing() != null) return direct.landing();
        return refuse(player, PROBLEMS[Math.max(centred.problem(), direct.problem())]);
    }

    public static void phase(ServerPlayer player, Vec3 landing) {
        ServerLevel level = player.serverLevel();
        Vec3 origin = player.position();
        double waist = player.getBbHeight() / 2.0D;
        player.connection.teleport(
            landing.x, landing.y, landing.z,
            player.getYRot(), player.getXRot(), RelativeMovement.ROTATION
        );
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.resetFallDistance();
        RpsManager.reanchor(player);
        for (Vec3 spot : List.of(origin, landing)) {
            level.sendParticles(ParticleTypes.SOUL, spot.x, spot.y + waist, spot.z, 12, 0.25D, 0.45D, 0.25D, 0.02D);
            level.playSound(null, spot.x, spot.y, spot.z,
                SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static void handlePhase(ServerPlayer player, GameWorldComponent gameWorld,
                                   AbilityPlayerComponent ability) {
        if (!gameWorld.isRole(player, ModRoles.HALF_SPIRIT) || ability.cooldown > 0) return;
        var landing = HalfSpirit.findLanding(player);
        if (landing == null) return;
        if (!AbilityCost.spend(player, "half_spirit")) return;
        HalfSpirit.phase(player, landing);
        ability.setAbilityCooldown(ModConfig.skillCooldownSeconds("half_spirit"));
    }

    private static Search search(ServerPlayer player, AABB box, Vec3 forward) {
        double lift = Math.min(STEP_HEIGHT, box.getYsize() / 2.0D);
        AABB torso = new AABB(box.minX, box.minY + lift, box.minZ, box.maxX, box.maxY, box.maxZ);
        double gap = sweep(player, torso, forward.scale(REACH)).dot(forward);
        if (gap > REACH - TOLERANCE) return new Search(null, NO_WALL);
        double front = Math.max(box.getMinPosition().dot(forward), box.getMaxPosition().dot(forward));
        double depth = Math.abs(forward.x) * box.getXsize() + Math.abs(forward.z) * box.getZsize();
        double thickness = Math.ceil(front + gap + TOLERANCE) - front - gap;
        int inside = (int) Math.ceil((thickness - TOLERANCE) / SCAN_STEP) - 1;
        int outside = (int) Math.floor((MAX_SPAN - thickness + TOLERANCE) / SCAN_STEP);
        int problem = TOO_THICK;
        for (int i = 0; i <= inside + outside; i++) {
            int offset = i == 0 ? 0 : i <= inside ? i - inside - 1 : i - inside;
            Vec3 through = forward.scale(gap + thickness + offset * SCAN_STEP + depth);
            if (!fits(player, torso.move(through))) continue;
            problem = UNSAFE;
            Vec3 landing = settle(player, box.move(through));
            if (landing != null) return new Search(landing, problem);
        }
        return new Search(null, problem);
    }

    @Nullable
    private static Vec3 settle(ServerPlayer player, AABB arrival) {
        AABB body = stand(player, arrival);
        if (body == null) return null;
        AABB slim = body.deflate(TOLERANCE, 0.0D, TOLERANCE);
        double fall = sweep(player, slim, new Vec3(0.0D, -MAX_DROP - GROUND_PROBE, 0.0D)).y;
        if (fall < -MAX_DROP - TOLERANCE) return null;
        AABB rest = body.move(0.0D, fall, 0.0D);
        Vec3 landing = rest.getBottomCenter();
        return hasFooting(player, rest) && isInsidePlayArea(player, landing) ? landing : null;
    }

    @Nullable
    private static AABB stand(ServerPlayer player, AABB body) {
        for (int i = 0; i <= LIFT_STEPS; i++) {
            AABB raised = body.move(0.0D, i * SCAN_STEP, 0.0D);
            if (fits(player, raised)) return raised;
        }
        return null;
    }

    private static boolean hasFooting(ServerPlayer player, AABB rest) {
        AABB footing = rest.deflate(FOOTING_INSET, 0.0D, FOOTING_INSET);
        return sweep(player, footing, new Vec3(0.0D, -GROUND_PROBE, 0.0D)).y > -GROUND_PROBE;
    }

    private static boolean isInsidePlayArea(ServerPlayer player, Vec3 position) {
        AABB playArea = MapVariablesWorldComponent.KEY.get(player.level()).getPlayArea();
        return playArea == null || playArea.contains(position);
    }

    private static boolean isStunned(ServerPlayer player) {
        PlayerEffectComponent effect = PlayerEffectComponent.KEY.get(player);
        return effect != null && effect.stunTicks > 0;
    }

    private static boolean fits(ServerPlayer player, AABB box) {
        return player.level().noCollision(player, box.deflate(TOLERANCE));
    }

    private static Vec3 sweep(ServerPlayer player, AABB box, Vec3 movement) {
        return Entity.collideBoundingBox(player, movement, box, player.level(), List.of());
    }

    @Nullable
    private static Vec3 refuse(ServerPlayer player, String problem) {
        player.displayClientMessage(Component.translatable("message.brinswathe.half_spirit." + problem), true);
        return null;
    }

    private record Search(@Nullable Vec3 landing, int problem) {
    }
}
