package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.AbilityCost;
import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.IllusionistComponent;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;

public final class Illusionist {
    private Illusionist() {
    }

    public static void handleClones(ServerPlayer player, GameWorldComponent gameWorld,
                                    AbilityPlayerComponent ability) {
        if (!gameWorld.isRole(player, ModRoles.ILLUSIONIST)) return;
        if (ability.cooldown > 0) return;
        IllusionistComponent comp = IllusionistComponent.KEY.get(player);
        if (comp == null) return;
        if (!AbilityCost.spend(player, "illusionist")) return;
        comp.spawnClones();
        ability.setAbilityCooldown(ModConfig.skillCooldownSeconds("illusionist"));
        player.level().playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public static void handleSwitchControl(ServerPlayer player, GameWorldComponent gameWorld,
                                           AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, ModRoles.ILLUSIONIST)) return;
        IllusionistComponent component = IllusionistComponent.KEY.get(player);
        if (component == null || component.cloneEntityIds.isEmpty()) return;
        component.setControlledClone(payload.targetId());
    }
}
