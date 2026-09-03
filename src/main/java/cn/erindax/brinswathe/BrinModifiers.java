package cn.erindax.brinswathe;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.modifiers.Modifier;

public final class BrinModifiers {
    public static final ResourceLocation FAST2FAST =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "fast2fast");
    public static final ResourceLocation STEALTH =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "stealth");
    public static final ResourceLocation PLAN_B =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "conductor_plan_b");
    public static final ResourceLocation GUESSER =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "guesser");
    public static final ResourceLocation GUESSER2 =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "guesser2");
    public static final ResourceLocation MICEYES =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "miceyes");
    public static final ResourceLocation EAGLE_EYE =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "eagle_eye");
    public static final ResourceLocation UNETHICAL =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "unethical");
    public static final ResourceLocation MARKSMAN =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "marksman");
    public static final ResourceLocation GLUTTON =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "glutton");
    public static final ResourceLocation BOTTOMLESS =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "bottomless");
    public static final ResourceLocation SPEED =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "speed");
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
