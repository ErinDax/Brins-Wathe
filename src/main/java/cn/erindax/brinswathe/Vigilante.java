package cn.erindax.brinswathe;

import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.config.Tunable;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;

public final class Vigilante {
    private Vigilante() {
    }

    public static void handleDetect(ServerPlayer player, GameWorldComponent gameWorld,
                                    AbilityPlayerComponent ability, AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, WatheRoles.VIGILANTE)) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
        if (ability == null || ability.cooldown > 0 || payload.targetId() == null) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null || target == player || !GameFunctions.isPlayerAliveAndSurvival(target)) return;
        if (target.distanceTo(player) > Tunable.VIGILANTE_DETECT_RANGE.doubleValue()) return;
        Role targetRole = gameWorld.getRole(target);
        if (targetRole == null) return;
        if (!AbilityCost.spend(player, "vigilante")) return;
        if (targetRole.isInnocent() || ModRoles.SPY.equals(targetRole)) {
            player.displayClientMessage(
                Component.translatable("tip.kinswathe.detective.innocent", target.getName().getString())
                    .withColor(WatheRoles.CIVILIAN.color()),
                true
            );
            player.playNotifySound(SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            player.displayClientMessage(
                Component.translatable("tip.kinswathe.detective.notinnocent", target.getName().getString())
                    .withColor(WatheRoles.KILLER.color()),
                true
            );
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        ability.setAbilityCooldown(ModConfig.skillCooldownSeconds("vigilante"));
    }
}
