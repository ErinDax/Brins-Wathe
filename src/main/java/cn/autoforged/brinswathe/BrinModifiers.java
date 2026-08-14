package cn.autoforged.brinswathe;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.modifiers.Modifier;

/**
 * Noell's roles mod owns the modifier instances and is not a compile dependency, so its modifiers are
 * matched by identifier through harpymodloader's synced world component instead.
 */
public final class BrinModifiers {
    public static final ResourceLocation FAST2FAST =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "fast2fast");
    public static final ResourceLocation STEALTH =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "stealth");

    private BrinModifiers() {
    }

    public static boolean hasModifier(Player player, ResourceLocation modifierId) {
        WorldModifierComponent modifiers = WorldModifierComponent.KEY.get(player.level());
        if (modifiers == null) return false;
        List<Modifier> list = modifiers.getModifiers(player);
        if (list == null) return false;
        for (Modifier modifier : list) {
            if (modifier != null && modifierId.equals(modifier.identifier())) return true;
        }
        return false;
    }
}
