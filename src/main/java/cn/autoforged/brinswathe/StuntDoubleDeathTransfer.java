package cn.autoforged.brinswathe;

import cn.autoforged.brinswathe.component.StuntDoubleComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class StuntDoubleDeathTransfer {
    private StuntDoubleDeathTransfer() {
    }

    public static boolean tryTransfer(Player victim, Player attacker, ResourceLocation deathReason) {
        if (!(victim instanceof ServerPlayer target) || target.getServer() == null) return false;

        for (ServerPlayer stuntDouble : target.getServer().getPlayerList().getPlayers()) {
            if (!GameFunctions.isPlayerAliveAndSurvival(stuntDouble)) continue;

            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(stuntDouble.level());
            if (!gameWorld.isRole(stuntDouble, BrinRoles.STUNT_DOUBLE)) continue;

            StuntDoubleComponent component = StuntDoubleComponent.KEY.get(stuntDouble);
            if (component == null
                || !component.isMimicking()
                || !target.getUUID().equals(component.mimicTarget)) continue;

            component.stopMimic();
            GameFunctions.killPlayer(stuntDouble, true, attacker, deathReason);
            return true;
        }
        return false;
    }
}
