package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.SubverterComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.jetbrains.annotations.Nullable;

public final class BrinSubversion {
    private static final double MAX_TARGET_DISTANCE = 80.0D;
    private static final int GLOW_TICKS = 3 * 20;
    private static UUID punishing;

    private BrinSubversion() {
    }

    public static boolean isPunishing(Player player) {
        return punishing != null && punishing.equals(player.getUUID());
    }

    public static void punish(ServerPlayer player) {
        punishing = player.getUUID();
        try {
            GameFunctions.killPlayer(player, true, null, GameConstants.DeathReasons.GENERIC);
        } finally {
            punishing = null;
        }
    }

    public static void subvert(
        ServerPlayer player,
        GameWorldComponent game,
        @Nullable UUID targetId,
        BrinFaction side
    ) {
        if (targetId == null || !game.isRole(player, BrinRoles.SUBVERTER)) return;
        SubverterComponent subverter = SubverterComponent.KEY.get(player);
        if (subverter == null || !subverter.isPending()) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(targetId);
        if (target == null || target == player || target.level() != player.level()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(target) || player.distanceTo(target) > MAX_TARGET_DISTANCE) return;
        Role role = game.getRole(target);
        if (role == null) return;
        if (side.includes(role)) {
            subverter.settle(null);
            player.displayClientMessage(Component.translatable("message.brinswathe.subverter.failed"), false);
            punish(player);
            return;
        }
        boolean killerPool = side == BrinFaction.KILLER;
        Role newRole = drawRole(game, killerPool);
        game.addRole(target, newRole);
        ModdedRoleAssigned.EVENT.invoker().assignModdedRole(target, newRole);
        game.sync();
        BrinWelcome.announceRoleChange(target, game, newRole, killerPool);
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_TICKS, 0, false, false));
        subverter.settle(side);
        player.playNotifySound(SoundEvents.EVOKER_PREPARE_WOLOLO, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable(
            killerPool ? "message.brinswathe.subverter.success_killer" : "message.brinswathe.subverter.success_civilian",
            target.getDisplayName()
        ), false);
    }

    private static Role drawRole(GameWorldComponent game, boolean killerPool) {
        ArrayList<Role> candidates = new ArrayList<>(WatheRoles.ROLES);
        candidates.removeIf(role ->
            Harpymodloader.VANNILA_ROLES.contains(role)
                || Harpymodloader.NON_MURDER_ROLES.contains(role)
                || (killerPool ? !role.canUseKiller() : !role.isInnocent() || role.canUseKiller())
                || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().getPath())
                || isFull(game, role)
        );
        if (candidates.isEmpty()) {
            candidates.add(killerPool ? WatheRoles.KILLER : WatheRoles.CIVILIAN);
        }
        Collections.shuffle(candidates);
        return candidates.getFirst();
    }

    private static boolean isFull(GameWorldComponent game, Role role) {
        Integer maximum = Harpymodloader.ROLE_MAX.get(role.identifier());
        if (maximum == null) return false;
        long holders = game.getRoles().values().stream().filter(role::equals).count();
        return holders >= maximum;
    }
}
