package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.BombComponent;
import cn.erindax.brinswathe.config.BrinConfig;
import cn.erindax.brinswathe.entity.BomberMine;
import cn.erindax.brinswathe.entity.TrapperFangs;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.jetbrains.annotations.Nullable;

public final class BomberBombs {
    private static final double MINE_TRIGGER_REACH = 0.3D;
    private static final double MINE_MOVE_TOLERANCE = 0.5D;
    private static final int MAX_FUSE_SECONDS = Integer.MAX_VALUE / 20;

    private BomberBombs() {
    }

    public static boolean canPlantBombs(ServerPlayer player) {
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return false;
        if (GameSafeComponent.KEY.get(player.level()).isGameSafe) return false;
        return GameWorldComponent.KEY.get(player.level()).isRole(player, BrinRoles.BOMBER);
    }

    public static boolean placeMine(ServerPlayer bomber, Vec3 position) {
        EvokerFangs mine = new EvokerFangs(
            bomber.level(),
            position.x,
            position.y,
            position.z,
            bomber.getYRot() * Mth.DEG_TO_RAD,
            Integer.MAX_VALUE,
            bomber
        );
        mine.setSilent(true);
        ((TrapperFangs) mine).brin$setTrapperTrap(true);
        ((BomberMine) mine).brin$setBomberMine(true);
        mine.addTag("brin_bomber_mine");
        if (!bomber.level().addFreshEntity(mine)) return false;

        bomber.playNotifySound(SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.0F, 0.8F);
        return true;
    }

    public static boolean attachBomb(ServerPlayer bomber, Player target) {
        if (target == bomber || !(target instanceof ServerPlayer victim)) return false;
        if (!GameFunctions.isPlayerAliveAndSurvival(victim)) return false;

        BombComponent component = BombComponent.KEY.get(victim);
        if (component == null || component.hasAttachedBomb()) return false;

        component.attach(bomber.getUUID(), fuseTicks(), false);
        bomber.playNotifySound(SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 0.6F, 1.4F);
        return true;
    }

    public static InteractionResult tryPassBomb(Player carrier, Entity clicked) {
        if (!(carrier instanceof ServerPlayer serverCarrier) || !(clicked instanceof ServerPlayer target)) {
            return InteractionResult.PASS;
        }
        if (!serverCarrier.getMainHandItem().isEmpty() || target == serverCarrier) return InteractionResult.PASS;
        BombComponent carried = BombComponent.KEY.get(serverCarrier);
        if (carried == null || !carried.hasAttachedBomb()) return InteractionResult.PASS;
        if (!GameFunctions.isPlayerAliveAndSurvival(serverCarrier)
            || !GameFunctions.isPlayerAliveAndSurvival(target)) {
            return InteractionResult.PASS;
        }
        BombComponent receiver = BombComponent.KEY.get(target);
        if (receiver == null || receiver.hasAttachedBomb()) return InteractionResult.PASS;

        receiver.attach(carried.bombOwner(), carried.fuseTicks(), carried.hasWarned());
        carried.clearBomb();
        serverCarrier.playNotifySound(SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 0.6F, 1.4F);
        serverCarrier.displayClientMessage(
            Component.translatable("message.brinswathe.bomber.bomb_passed", target.getName())
                .withStyle(ChatFormatting.GREEN),
            true
        );
        return InteractionResult.SUCCESS;
    }
    public static void tick(ServerPlayer player, BombComponent component) {
        if (component.isMinePinned()) {
            if (!tickPinnedPlayer(player, component)) return;
        } else {
            checkMineStep(player, component);
            if (component.isMinePinned()) return;
        }
        if (component.hasAttachedBomb()) tickAttachedBomb(player, component);
    }
    public static void releaseStun(Player player) {
        PlayerEffectComponent effect = PlayerEffectComponent.KEY.get(player);
        if (effect != null && effect.stunTicks > 0) effect.reset();
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }
    private static void checkMineStep(ServerPlayer player, BombComponent component) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (gameWorld.canUseKillerFeatures(player)) return;
        if (gameWorld.isRole(player, BrinRoles.WATCHMAN)) return;
        List<EvokerFangs> mines = player.level().getEntitiesOfClass(
            EvokerFangs.class,
            player.getBoundingBox().inflate(MINE_TRIGGER_REACH),
            candidate -> ((BomberMine) candidate).brin$isBomberMine()
        );
        if (mines.isEmpty()) return;
        EvokerFangs mine = mines.getFirst();
        LivingEntity owner = mine.getOwner();
        mine.discard();
        component.pin(owner == null ? null : owner.getUUID(), player.position());
        applyMineStun(player);
        player.level().playSound(
            null, player.blockPosition(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 1.0F, 0.6F);
        player.playNotifySound(SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 1.0F, 0.8F);
        player.displayClientMessage(
            Component.translatable("message.brinswathe.bomber.mine_triggered").withStyle(ChatFormatting.RED),
            false
        );
    }
    private static boolean tickPinnedPlayer(ServerPlayer player, BombComponent component) {
        Vec3 anchor = component.mineAnchor();
        if (anchor == null) {
            component.clearMine();
            return true;
        }
        boolean shoved = BrinPushed.wasPushed(player);
        if (player.position().distanceToSqr(anchor) <= MINE_MOVE_TOLERANCE * MINE_MOVE_TOLERANCE) {
            return true;
        }
        if (shoved) {
            player.setPos(anchor.x, anchor.y, anchor.z);
            player.setDeltaMovement(Vec3.ZERO);
            return true;
        }
        UUID ownerId = component.mineOwner();
        component.clearMine();
        TerroristExplosion.playExplosionEffects(player.serverLevel(), player);
        killWithChainReaction(player, resolvePlayer(player.server, ownerId));
        return false;
    }
    private static void tickAttachedBomb(ServerPlayer carrier, BombComponent component) {
        int remaining = component.tickFuse();
        int warningTicks = Math.min(BrinConfig.bomberBombWarningSeconds(), MAX_FUSE_SECONDS) * 20;
        if (!component.hasWarned() && remaining <= warningTicks) {
            component.markWarned();
            carrier.displayClientMessage(
                Component.translatable("message.brinswathe.bomber.bomb_warning", Mth.ceil(remaining / 20.0F))
                    .withStyle(ChatFormatting.RED),
                false
            );
            carrier.playNotifySound(SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (remaining > 0 && remaining <= warningTicks
            && remaining % beepIntervalTicks(remaining, warningTicks) == 0) {
            carrier.playNotifySound(SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 0.6F, 1.8F);
        }
        if (remaining > 0) return;
        UUID ownerId = component.bombOwner();
        component.clearBomb();
        explode(carrier, ownerId);
    }
    private static void explode(ServerPlayer carrier, @Nullable UUID ownerId) {
        ServerLevel level = carrier.serverLevel();
        TerroristExplosion.playExplosionEffects(level, carrier);
        double size = BrinConfig.bomberBombExplosionSize();
        AABB bounds = AABB.ofSize(carrier.position(), size, size, size);
        List<ServerPlayer> targets = level.getPlayers(target ->
            GameFunctions.isPlayerAliveAndSurvival(target) && bounds.contains(target.position())
        );
        ServerPlayer owner = resolvePlayer(carrier.server, ownerId);
        for (ServerPlayer target : targets) {

            if (!GameFunctions.isPlayerAliveAndSurvival(target)) continue;
            killWithChainReaction(target, owner);
        }
    }
    private static void killWithChainReaction(ServerPlayer target, @Nullable ServerPlayer attacker) {
        boolean wasEliminated = GameFunctions.isPlayerEliminated(target);
        GameFunctions.killPlayer(target, true, attacker, GameConstants.DeathReasons.GRENADE);
        if (!wasEliminated && GameFunctions.isPlayerEliminated(target)) {
            TerroristExplosion.triggerChainExplosion(target);
        }
    }
    private static void applyMineStun(ServerPlayer player) {
        int seconds = BrinConfig.bomberMineStunSeconds();
        if (seconds <= 0) return;
        PlayerEffectComponent effect = PlayerEffectComponent.KEY.get(player);
        if (effect == null) return;
        effect.setStunTicks(Math.min(seconds, MAX_FUSE_SECONDS) * 20);
    }

    private static int beepIntervalTicks(int remaining, int warningTicks) {
        if (remaining > warningTicks * 2 / 3) return 20;
        if (remaining > warningTicks / 3) return 10;
        if (remaining > warningTicks / 6) return 5;
        return 2;
    }
    private static int fuseTicks() {
        return Math.min(BrinConfig.bomberBombFuseSeconds(), MAX_FUSE_SECONDS) * 20;
    }
    @Nullable
    private static ServerPlayer resolvePlayer(MinecraftServer server, @Nullable UUID playerId) {
        return playerId == null ? null : server.getPlayerList().getPlayer(playerId);
    }
}
