package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.ValentinaComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class Valentina {
    private Valentina() {
    }

    public static void handleForesight(ServerPlayer player, GameWorldComponent gameWorld) {
        if (!gameWorld.isRole(player, ModRoles.VALENTINA)) return;
        ValentinaComponent valentina = ValentinaComponent.KEY.get(player);
        if (valentina == null || !valentina.foresee()) return;
        player.playNotifySound(SoundEvents.ENDER_EYE_LAUNCH, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
