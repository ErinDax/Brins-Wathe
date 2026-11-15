package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.AbilityCost;
import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.StuntDoubleComponent;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;

public final class StuntDouble {
    private StuntDouble() {
    }

    public static void handleMimic(ServerPlayer player, GameWorldComponent gameWorld,
                                   AbilityPlayerComponent ability,
                                   AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, ModRoles.STUNT_DOUBLE)) return;
        if (ability.cooldown > 0) return;
        if (payload.targetId() == null) return;

        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null || !target.isAlive()) return;
        if (player.equals(target)) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
        StuntDoubleComponent comp = StuntDoubleComponent.KEY.get(player);
        if (comp == null) return;
        if (!AbilityCost.spend(player, "stunt_double")) return;
        comp.startMimic(target.getUUID());
        ability.setAbilityCooldown(ModConfig.skillCooldownSeconds("stunt_double"));

        player.level().playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
