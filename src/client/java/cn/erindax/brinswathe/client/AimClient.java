package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.role.Grappler;
import cn.erindax.brinswathe.role.WatchmanRescue;
import cn.erindax.brinswathe.component.IllusionistComponent;
import cn.erindax.brinswathe.component.MorticianComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import cn.erindax.brinswathe.component.StalkerComponent;
import cn.erindax.brinswathe.config.Tunable;
import cn.erindax.brinswathe.entity.PuppetEntity;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class AimClient {
    private static final float ILLUSION_AIM_TOLERANCE = 0.5F;

    private AimClient() {
    }

    public static Entity weaponAimOrigin(Player attacker) {
        return attacker;
    }

    public static boolean isWeaponTarget(Player attacker, Entity entity) {
        if (entity == attacker) return false;
        if (entity instanceof Player player) {
            return GameFunctions.isPlayerAliveAndSurvival(player);
        }
        if (!(entity instanceof PlayerBodyEntity body)) return false;
        if (PuppeteerControlComponent.isPuppetBodyProxy(body)) {
            return !attacker.getUUID().equals(((PuppetEntity) body).brin$getPuppeteer());
        }
        if (attacker.getUUID().equals(body.getPlayerUuid())) return false;
        return IllusionistComponent.isIllusionModel(body);
    }

    public static HitResult findWeaponTarget(Player attacker, double range) {
        Entity origin = weaponAimOrigin(attacker);
        Vec3 eye = origin.getEyePosition();
        Vec3 end = eye.add(origin.getViewVector(1.0F).scale(range));
        HitResult blockHit = origin.level().clip(new ClipContext(
            eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, origin));
        double bestDistSqr = blockHit.getType() == HitResult.Type.MISS
            ? range * range
            : blockHit.getLocation().distanceToSqr(eye);
        Entity bestEntity = null;
        Vec3 bestPos = null;
        AABB searchBox = origin.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        for (Entity candidate : origin.level().getEntities(origin, searchBox,
                entity -> isWeaponTarget(attacker, entity))) {
            float tolerance = candidate instanceof PlayerBodyEntity body
                && (IllusionistComponent.isIllusionModel(body)
                    || PuppeteerControlComponent.isPuppetModel(body))
                ? ILLUSION_AIM_TOLERANCE
                : 0.0F;
            AABB box = candidate.getBoundingBox().inflate(tolerance);
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

    @Nullable
    public static Entity findClientEntity(Minecraft client, @Nullable UUID entityId) {
        if (client.level == null || entityId == null) return null;
        for (Entity entity : client.level.entitiesForRendering()) {
            if (entityId.equals(entity.getUUID())) return entity;
        }
        return null;
    }

    @Nullable
    static UUID findSleepingPlayerInCrosshair(Player localPlayer) {
        if (Minecraft.getInstance().getConnection() == null) return null;
        Vec3 eyePos = localPlayer.getEyePosition();
        Vec3 look = localPlayer.getLookAngle();
        double bestDot = -2.0;
        UUID bestTarget = null;
        for (PlayerInfo info : Minecraft.getInstance().getConnection().getListedOnlinePlayers()) {
            if (info.getProfile().getId().equals(localPlayer.getUUID())) continue;
            Player targetPlayer = localPlayer.level().getPlayerByUUID(info.getProfile().getId());
            if (targetPlayer == null || !targetPlayer.isSleeping()) continue;
            Vec3 toTarget = targetPlayer.getEyePosition().subtract(eyePos).normalize();
            double dot = toTarget.dot(look);
            if (dot > bestDot && dot > 0.5) {
                bestDot = dot;
                bestTarget = targetPlayer.getUUID();
            }
        }
        return bestTarget;
    }

    @Nullable
    static UUID findPlayerInCrosshair(Player localPlayer) {
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
            localPlayer,
            entity -> entity instanceof Player target
                && target != localPlayer
                && GameFunctions.isPlayerAliveAndSurvival(target),
            64.0D
        );
        if (hitResult instanceof EntityHitResult entityHit
            && entityHit.getEntity() instanceof Player targetPlayer) {
            return targetPlayer.getUUID();
        }
        return null;
    }

    @Nullable
    static UUID findStalkerAimTarget(Player localPlayer) {
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
            localPlayer,
            entity -> entity instanceof Player target
                && target != localPlayer
                && GameFunctions.isPlayerAliveAndSurvival(target),
            Tunable.STALKER_AIM_RANGE.doubleValue()
        );
        if (hitResult instanceof EntityHitResult entityHit
            && entityHit.getEntity() instanceof Player targetPlayer
            && StalkerComponent.isAimedAt(localPlayer, targetPlayer)) {
            return targetPlayer.getUUID();
        }
        return null;
    }

    @Nullable
    static UUID findGrapplerTarget(Player localPlayer) {
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
            localPlayer,
            entity -> entity instanceof Player target && Grappler.canTarget(localPlayer, target),
            Tunable.GRAPPLER_HOOK_RANGE.doubleValue()
        );
        if (hitResult instanceof EntityHitResult entityHit
            && entityHit.getEntity() instanceof Player targetPlayer) {
            return targetPlayer.getUUID();
        }
        return null;
    }

    @Nullable
    static UUID findWatchmanTarget(Player localPlayer) {
        UUID raycast = findPlayerInCrosshair(localPlayer);
        if (raycast != null && WatchmanRescue.canRescue(localPlayer.level().getPlayerByUUID(raycast))) {
            return raycast;
        }
        Vec3 eyePos = localPlayer.getEyePosition();
        Vec3 look = localPlayer.getLookAngle();
        double bestDot = 0.5;
        UUID bestTarget = null;
        for (Player target : localPlayer.level().players()) {
            if (target == localPlayer || !WatchmanRescue.canRescue(target)) continue;
            if (eyePos.distanceToSqr(target.position()) > 64.0D * 64.0D) continue;
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(eyePos);
            if (toTarget.lengthSqr() < 1.0E-6) continue;
            double dot = toTarget.normalize().dot(look);
            if (dot > bestDot) {
                bestDot = dot;
                bestTarget = target.getUUID();
            }
        }
        if (bestTarget != null) return bestTarget;
        if (WatchmanRescue.canRescue(localPlayer)) return localPlayer.getUUID();
        return findAimedLivingPlayer(localPlayer);
    }

    @Nullable
    private static UUID findAimedLivingPlayer(Player localPlayer) {
        UUID raycast = findPlayerInCrosshair(localPlayer);
        if (raycast != null) return raycast;
        Vec3 eyePos = localPlayer.getEyePosition();
        Vec3 look = localPlayer.getLookAngle();
        double bestDot = -2.0;
        UUID bestTarget = null;
        for (Player target : localPlayer.level().players()) {
            if (target == localPlayer || !GameFunctions.isPlayerAliveAndSurvival(target)) continue;
            if (eyePos.distanceToSqr(target.position()) > 64.0D * 64.0D) continue;
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(eyePos);
            if (toTarget.lengthSqr() < 1.0E-6) continue;
            double dot = toTarget.normalize().dot(look);
            if (dot > bestDot && dot > 0.5) {
                bestDot = dot;
                bestTarget = target.getUUID();
            }
        }
        return bestTarget;
    }

    @Nullable
    static UUID findAimedAnyPlayer(Player localPlayer) {
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
            localPlayer,
            entity -> entity instanceof Player target && target != localPlayer,
            64.0D
        );
        if (hitResult instanceof EntityHitResult entityHit
            && entityHit.getEntity() instanceof Player targetPlayer) {
            return targetPlayer.getUUID();
        }
        Vec3 eyePos = localPlayer.getEyePosition();
        Vec3 look = localPlayer.getLookAngle();
        double bestDot = -2.0;
        UUID bestTarget = null;
        for (Player target : localPlayer.level().players()) {
            if (target == localPlayer) continue;
            if (eyePos.distanceToSqr(target.position()) > 64.0D * 64.0D) continue;
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(eyePos);
            if (toTarget.lengthSqr() < 1.0E-6) continue;
            double dot = toTarget.normalize().dot(look);
            if (dot > bestDot && dot > 0.5) {
                bestDot = dot;
                bestTarget = target.getUUID();
            }
        }
        return bestTarget;
    }

    @Nullable
    static UUID findPlayerBodyInCrosshair(Player localPlayer) {
        PlayerBodyEntity body = getPlayerBodyInCrosshair(localPlayer);
        return body == null ? null : body.getUUID();
    }

    @Nullable
    public static PlayerBodyEntity getPlayerBodyInCrosshair(Player localPlayer) {
        return brinBodyInCrosshair(localPlayer, false);
    }

    @Nullable
    public static PlayerBodyEntity getInspectableBodyInCrosshair(Player localPlayer) {
        return brinBodyInCrosshair(localPlayer, true);
    }

    @Nullable
    private static PlayerBodyEntity brinBodyInCrosshair(Player localPlayer, boolean includeDisguises) {
        Vec3 eye = localPlayer.getEyePosition();
        Vec3 look = localPlayer.getLookAngle();
        double range = 8.0D;
        Vec3 end = eye.add(look.scale(range));
        HitResult blockHit = localPlayer.level().clip(new ClipContext(
            eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, localPlayer));
        double bestDistanceSqr = blockHit.getType() == HitResult.Type.MISS
            ? range * range
            : eye.distanceToSqr(blockHit.getLocation());
        PlayerBodyEntity bestBody = null;
        AABB searchBox = localPlayer.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);
        for (PlayerBodyEntity body : localPlayer.level().getEntitiesOfClass(
            PlayerBodyEntity.class,
            searchBox,
            candidate -> !candidate.isInvisible()
                && !IllusionistComponent.isIllusionModel(candidate)
                && !PuppeteerControlComponent.isPuppetModel(candidate)
                && (includeDisguises || !MorticianComponent.isDisguiseBody(candidate)))) {
            var hit = body.getBoundingBox().inflate(0.25D).clip(eye, end);
            if (hit.isEmpty()) continue;
            double distanceSqr = eye.distanceToSqr(hit.get());
            if (distanceSqr <= bestDistanceSqr) {
                bestDistanceSqr = distanceSqr;
                bestBody = body;
            }
        }
        return bestBody;
    }
}
