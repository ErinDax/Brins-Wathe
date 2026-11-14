package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.ResurrectedComponent;
import cn.erindax.brinswathe.config.Tunable;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class InsaneKiller {
    private InsaneKiller() {
    }

    public static void handleRevive(ServerPlayer player, GameWorldComponent gameWorld, AbilityC2SPacket payload) {
        if (!NoelleAccess.isRole(gameWorld, player, NoelleAccess.INSANE_KILLER_ID)) return;
        int glowTicks = Tunable.INSANE_KILLER_REVIVE_GLOW_SECONDS.ticks();
        if (glowTicks > 0) player.addEffect(new MobEffectInstance(MobEffects.GLOWING, glowTicks, 0, false, false));
        if (payload.targetId() == null) return;
        if (NoelleAccess.noelleAbilityCooldown(player) > 0) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null) return;
        if (!target.isSpectator()) {
            player.displayClientMessage(Component.literal("他还没有死"), true);
            return;
        }
        ResurrectedComponent resurrected = ResurrectedComponent.KEY.get(target);
        if (resurrected == null || !resurrected.resurrect(player.getUUID())) return;
        NoelleAccess.setNoelleAbilityCooldown(player, Tunable.INSANE_KILLER_REVIVE_COOLDOWN_SECONDS.ticks());
    }
}
