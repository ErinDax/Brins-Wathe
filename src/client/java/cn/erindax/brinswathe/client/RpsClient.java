package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.RpsManager;
import cn.erindax.brinswathe.config.Tunable;
import cn.erindax.brinswathe.network.RpsActionC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class RpsClient {
    private RpsClient() {
    }

    static void tryInviteRps(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || !RpsManager.canPlay(player)) return;
        HitResult hit = findRpsTarget(player, Tunable.RPS_RANGE.doubleValue());
        if (!(hit instanceof EntityHitResult entityHit)
            || !(entityHit.getEntity() instanceof Player target)
            || !RpsManager.canPlayTogether(player, target)) {
            player.displayClientMessage(Component.translatable("message.brinswathe.rps.no_target"), true);
            return;
        }
        ClientPlayNetworking.send(RpsActionC2SPacket.invite(target.getUUID()));
    }

    private static HitResult findRpsTarget(Player attacker, double range) {
        Vec3 eye = attacker.getEyePosition();
        Vec3 end = eye.add(attacker.getViewVector(1.0F).scale(range));
        HitResult blockHit = attacker.isSpectator()
            ? BlockHitResult.miss(end, Direction.UP, BlockPos.containing(end))
            : attacker.level().clip(new ClipContext(
                eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, attacker));
        double bestDistSqr = blockHit.getType() == HitResult.Type.MISS
            ? range * range
            : blockHit.getLocation().distanceToSqr(eye);
        Entity bestEntity = null;
        Vec3 bestPos = null;
        AABB searchBox = attacker.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        for (Entity candidate : attacker.level().getEntities(attacker, searchBox,
                entity -> entity instanceof Player other && RpsManager.canPlayTogether(attacker, other))) {
            AABB box = candidate.getBoundingBox();
            if (box.contains(eye)) {
                bestEntity = candidate;
                bestPos = eye;
                bestDistSqr = 0.0D;
                continue;
            }
            var clip = box.clip(eye, end);
            if (clip.isEmpty()) continue;
            double distSqr = eye.distanceToSqr(clip.get());
            if (distSqr < bestDistSqr) {
                bestEntity = candidate;
                bestPos = clip.get();
                bestDistSqr = distSqr;
            }
        }
        return bestEntity != null ? new EntityHitResult(bestEntity, bestPos) : blockHit;
    }
}
