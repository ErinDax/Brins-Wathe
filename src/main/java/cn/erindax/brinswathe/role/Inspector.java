package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.AbilityCost;
import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.InspectorComponent;
import cn.erindax.brinswathe.config.ModConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;

public final class Inspector {
    private Inspector() {
    }

    public static void handleDisarm(ServerPlayer player, GameWorldComponent gameWorld) {
        if (!gameWorld.isRole(player, ModRoles.INSPECTOR) || !GameFunctions.isPlayerAliveAndSurvival(player)) return;
        InspectorComponent inspector = InspectorComponent.KEY.get(player);
        if (inspector == null || inspector.used()) return;
        if (!AbilityCost.spend(player, "inspector")) return;
        inspector.markUsed();
        Disarm.start(player.serverLevel(), ModConfig.skillDurationSeconds("inspector"));
    }
}
