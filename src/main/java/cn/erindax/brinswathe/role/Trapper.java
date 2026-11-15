package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.AbilityCost;
import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.TrapperComponent;
import cn.erindax.brinswathe.config.ModConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.level.ServerPlayer;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;

public final class Trapper {
    private Trapper() {
    }

    public static void handlePlaceTrap(ServerPlayer player, GameWorldComponent gameWorld,
                                       AbilityPlayerComponent ability) {
        if (!gameWorld.isRole(player, ModRoles.TRAPPER)) return;
        if (ability.cooldown > 0) return;
        TrapperComponent comp = TrapperComponent.KEY.get(player);
        if (comp == null) return;
        if (!comp.canPlaceTrap()) return;
        if (!AbilityCost.spend(player, "beast_trapper")) return;
        if (!TrapperTraps.place(player, false)) return;
        ability.setAbilityCooldown(ModConfig.skillCooldownSeconds("beast_trapper"));
    }
}
