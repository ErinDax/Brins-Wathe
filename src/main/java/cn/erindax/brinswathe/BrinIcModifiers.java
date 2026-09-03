package cn.erindax.brinswathe;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;

public final class BrinIcModifiers {
    public static Modifier PLAN_B;
    public static Modifier GUESSER2;
    public static Modifier MICEYES;
    public static Modifier EAGLE_EYE;
    public static Modifier UNETHICAL;
    public static Modifier MARKSMAN;
    public static Modifier GLUTTON;
    public static Modifier BOTTOMLESS;
    public static Modifier SPEED;

    private BrinIcModifiers() {
    }

    public static void register() {
        ArrayList<Role> vigilanteOnly = new ArrayList<>(List.of(WatheRoles.VIGILANTE));
        PLAN_B = HMLModifiers.registerModifier(new Modifier(
            BrinModifiers.PLAN_B,
            0xFFCD54,
            null,
            vigilanteOnly,
            false,
            false
        ));

        GUESSER2 = HMLModifiers.registerModifier(new Modifier(
            BrinModifiers.GUESSER2,
            0x9E2B19,
            null,
            null,
            true,
            false
        ));

        ArrayList<Role> mimicOnly = new ArrayList<>();
        Role mimic = BrinNoelleAccess.findRole(BrinNoelleAccess.MIMIC_ID);
        if (mimic != null) mimicOnly.add(mimic);
        MICEYES = HMLModifiers.registerModifier(new Modifier(
            BrinModifiers.MICEYES,
            0xFF899B,
            null,
            mimicOnly.isEmpty() ? null : mimicOnly,
            false,
            false
        ));

        ArrayList<Role> eagleRoles = new ArrayList<>(List.of(WatheRoles.VIGILANTE));
        Role reporter = BrinNoelleAccess.findRole(BrinNoelleAccess.REPORTER_ID);
        if (reporter != null) eagleRoles.add(reporter);
        EAGLE_EYE = registerIfAbsent(new Modifier(
            BrinModifiers.EAGLE_EYE,
            0x4AF102,
            null,
            eagleRoles,
            false,
            false
        ));
        UNETHICAL = registerIfAbsent(new Modifier(
            BrinModifiers.UNETHICAL,
            0x1C1C1C,
            null,
            null,
            false,
            true
        ));
        MARKSMAN = registerIfAbsent(new Modifier(
            BrinModifiers.MARKSMAN,
            0x87B17B,
            null,
            null,
            false,
            true
        ));
        GLUTTON = registerIfAbsent(new Modifier(
            BrinModifiers.GLUTTON,
            0xFFD700,
            null,
            null,
            false,
            true
        ));
        BOTTOMLESS = registerIfAbsent(new Modifier(
            BrinModifiers.BOTTOMLESS,
            0x5C2F17,
            null,
            null,
            false,
            true
        ));
        SPEED = registerIfAbsent(new Modifier(
            BrinModifiers.SPEED,
            0xFFFFFF,
            null,
            null,
            false,
            false
        ));
    }

    private static Modifier registerIfAbsent(Modifier modifier) {
        Modifier existing = findRegistered(modifier.identifier());
        return existing != null ? existing : HMLModifiers.registerModifier(modifier);
    }

    public static Modifier findRegistered(ResourceLocation id) {
        for (Modifier modifier : HMLModifiers.MODIFIERS) {
            if (modifier != null && id.equals(modifier.identifier())) return modifier;
        }
        return null;
    }
}
