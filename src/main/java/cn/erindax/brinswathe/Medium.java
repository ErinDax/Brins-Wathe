package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.ConspiratorComponent;
import cn.erindax.brinswathe.component.MediumComponent;
import cn.erindax.brinswathe.config.ModConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;

public final class Medium {
    private Medium() {
    }

    public static void handleJoinVoice(ServerPlayer player, GameWorldComponent gameWorld,
                                       AbilityPlayerComponent ability) {
        if (!gameWorld.isRole(player, ModRoles.MEDIUM)) return;
        if (ability.cooldown > 0) return;
        MediumComponent medium = MediumComponent.KEY.get(player);
        if (medium == null) return;
        ConspiratorComponent conspirator = ConspiratorComponent.KEY.get(player);
        if (conspirator != null && conspirator.isInTemporaryChannel()) return;

        if (!medium.startChannelling(ModConfig.skillDurationSeconds("medium"))) return;
        if (!AbilityCost.spend(player, "medium")) {
            medium.reset();
            return;
        }
        ability.setAbilityCooldown(ModConfig.skillCooldownSeconds("medium"));
        player.playNotifySound(SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
    }
}
