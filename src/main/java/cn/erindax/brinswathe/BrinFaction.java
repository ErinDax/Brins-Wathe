package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.GamblerComponent;
import cn.erindax.brinswathe.component.SubverterComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public enum BrinFaction {
    KILLER,
    CIVILIAN;

    public boolean includes(Role role) {
        return this == KILLER ? role.canUseKiller() : role.isInnocent() && !role.canUseKiller();
    }

    public boolean wins(GameFunctions.WinStatus status) {
        return this == KILLER
            ? status == GameFunctions.WinStatus.KILLERS
            : status == GameFunctions.WinStatus.PASSENGERS || status == GameFunctions.WinStatus.TIME;
    }

    public RoleAnnouncementTexts.RoleAnnouncementText endText() {
        return this == KILLER ? RoleAnnouncementTexts.KILLER : RoleAnnouncementTexts.CIVILIAN;
    }

    public static boolean isFollower(GameWorldComponent game, UUID playerId) {
        return game.isRole(playerId, BrinRoles.SUBVERTER) || game.isRole(playerId, BrinRoles.GAMBLER);
    }

    @Nullable
    public static BrinFaction followedBy(GameWorldComponent game, Player player) {
        if (game.isRole(player, BrinRoles.SUBVERTER)) {
            SubverterComponent subverter = SubverterComponent.KEY.get(player);
            return subverter == null ? null : subverter.side();
        }
        if (game.isRole(player, BrinRoles.GAMBLER)) {
            GamblerComponent gambler = GamblerComponent.KEY.get(player);
            return gambler == null ? null : gambler.bet();
        }
        return null;
    }
}
