package cn.erindax.brinswathe.client.role;

import cn.erindax.brinswathe.component.SniperComponent;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.world.phys.Vec3;

public final class SniperClient {
    private static boolean sniperCancelSent;

    private SniperClient() {
    }

    public static void tickSniperAiming(Minecraft client) {
        if (client.player == null || client.level == null) {
            sniperCancelSent = false;
            return;
        }

        SniperComponent component = SniperComponent.KEY.get(client.player);
        if (component == null || !component.isAiming()) {
            sniperCancelSent = false;
            return;
        }
        client.player.setDeltaMovement(Vec3.ZERO);
        if (client.screen instanceof PauseScreen && !sniperCancelSent) {
            sniperCancelSent = true;
            ClientPlayNetworking.send(new AbilityC2SPacket(
                AbilityC2SPacket.ABILITY_SNIPER_CANCEL,
                null
            ));
            client.setScreen(null);
        }
    }

    public static boolean isSniperAiming() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return false;
        SniperComponent component = SniperComponent.KEY.get(client.player);
        return component != null && component.isAiming();
    }
}
