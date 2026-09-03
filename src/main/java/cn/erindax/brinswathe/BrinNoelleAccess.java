package cn.erindax.brinswathe;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.lang.reflect.Field;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.component.ComponentKey;

public final class BrinNoelleAccess {
    public static final ResourceLocation JESTER_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "jester");
    public static final ResourceLocation BARTENDER_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "bartender");
    public static final ResourceLocation REPORTER_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "awesome_binglus");
    public static final ResourceLocation CONDUCTOR_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "conductor");
    public static final ResourceLocation EXECUTIONER_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "executioner");
    public static final ResourceLocation VULTURE_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "vulture");
    public static final ResourceLocation MORPHLING_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "morphling");
    public static final ResourceLocation MIMIC_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "mimic");
    public static final ResourceLocation NOISEMAKER_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "noisemaker");
    public static final ResourceLocation INSANE_KILLER_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "the_insane_damned_paranoid_killer");
    public static final ResourceLocation VOODOO_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "voodoo");
    public static final ResourceLocation INITIATE_ID =
        ResourceLocation.fromNamespaceAndPath("stupid_express", "initiate");

    public static final int CONDUCTOR_ARMOR_COST = 200;
    public static final int CONDUCTOR_ARMOR_COOLDOWN_TICKS = 100 * 20;

    private static boolean bartenderLookupAttempted;
    private static ComponentKey<?> bartenderKey;
    private static Field bartenderArmorField;
    private static boolean executionerLookupAttempted;
    private static ComponentKey<?> executionerKey;
    private static Field executionerTargetField;
    private static Field executionerWonField;
    private static boolean vultureLookupAttempted;
    private static ComponentKey<?> vultureBodyKey;
    private static Field vulturedField;
    private static boolean voodooLookupAttempted;
    private static ComponentKey<?> voodooKey;
    private static Field voodooTargetField;
    private static boolean abilityLookupAttempted;
    private static ComponentKey<?> abilityKey;
    private static Field abilityCooldownField;

    private BrinNoelleAccess() {
    }

    public static boolean isRole(GameWorldComponent game, Player player, ResourceLocation id) {
        if (game == null || player == null) return false;
        Role role = game.getRole(player);
        return role != null && id.equals(role.identifier());
    }

    public static Role findRole(ResourceLocation id) {
        for (Role role : WatheRoles.ROLES) {
            if (id.equals(role.identifier())) return role;
        }
        return null;
    }

    public static boolean isInitiate(GameWorldComponent game, Player player) {
        return isRole(game, player, INITIATE_ID);
    }

    public static int initiateCount(GameWorldComponent game, Player viewer) {
        Role role = findRole(INITIATE_ID);
        if (game == null || role == null || viewer == null) return 0;
        int count = 0;
        for (java.util.UUID id : game.getAllWithRole(role)) {
            Player other = viewer.level().getPlayerByUUID(id);
            if (other == null || other.isSpectator() || other.getAbilities().instabuild) continue;
            count++;
        }
        return count;
    }

    public static boolean setStupidAbilityCooldown(Player player, int ticks) {
        try {
            Class<?> clazz = Class.forName("pro.fazeclan.river.stupid_express.cca.AbilityCooldownComponent");
            ComponentKey<?> key = componentKey(clazz);
            if (key == null) return false;
            Object component = key.get(player);
            if (component == null) return false;
            try {
                component.getClass().getMethod("setCooldown", int.class).invoke(component, Math.max(0, ticks));
            } catch (NoSuchMethodException exception) {
                Field field = field(clazz, "cooldown");
                field.setInt(component, Math.max(0, ticks));
            }
            try {
                component.getClass().getMethod("sync").invoke(component);
            } catch (ReflectiveOperationException ignored) {
                key.sync(player);
            }
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    public static int bartenderArmor(Player player) {
        resolveBartender();
        if (bartenderKey == null || bartenderArmorField == null) return 0;
        try {
            Object component = bartenderKey.get(player);
            return component == null ? 0 : Math.max(0, bartenderArmorField.getInt(component));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return 0;
        }
    }

    public static boolean giveBartenderArmor(Player player) {
        resolveBartender();
        if (bartenderKey == null || bartenderArmorField == null) return false;
        try {
            Object component = bartenderKey.get(player);
            if (component == null) return false;
            try {
                component.getClass().getMethod("giveArmor").invoke(component);
                return true;
            } catch (NoSuchMethodException ignored) {
                bartenderArmorField.setInt(component, 1);
            }
            try {
                component.getClass().getMethod("sync").invoke(component);
            } catch (ReflectiveOperationException ignored) {
                bartenderKey.sync(player);
            }
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    public static boolean addBartenderArmor(Player player) {
        resolveBartender();
        if (bartenderKey == null || bartenderArmorField == null) return false;
        try {
            Object component = bartenderKey.get(player);
            if (component == null) return false;
            int armor = bartenderArmorField.getInt(component);
            if (armor >= 3) return false;
            bartenderArmorField.setInt(component, armor + 1);
            try {
                component.getClass().getMethod("sync").invoke(component);
            } catch (ReflectiveOperationException ignored) {
                bartenderKey.sync(player);
            }
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    public static void setBartenderArmor(Player player, int layers) {
        resolveBartender();
        if (bartenderKey == null || bartenderArmorField == null) return;
        try {
            Object component = bartenderKey.get(player);
            if (component == null) return;
            bartenderArmorField.setInt(component, Math.max(0, layers));
            try {
                component.getClass().getMethod("sync").invoke(component);
            } catch (ReflectiveOperationException ignored) {
                bartenderKey.sync(player);
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    public static UUID executionerTarget(Player player) {
        resolveExecutioner();
        if (executionerKey == null || executionerTargetField == null) return null;
        try {
            Object component = executionerKey.get(player);
            if (component == null) return null;
            Object target = executionerTargetField.get(component);
            return target instanceof UUID uuid ? uuid : null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    public static void resetExecutionerWon(Player player) {
        setExecutionerWon(player, false);
    }
    public static void setExecutionerWon(Player player, boolean won) {
        resolveExecutioner();
        if (executionerKey == null || executionerWonField == null) return;
        try {
            Object component = executionerKey.get(player);
            if (component == null || executionerWonField.getBoolean(component) == won) return;
            executionerWonField.setBoolean(component, won);
            syncComponent(executionerKey, component, player);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }
    public static void setExecutionerTarget(Player player, UUID targetId) {
        resolveExecutioner();
        if (executionerKey == null || executionerTargetField == null || targetId == null) return;
        try {
            Object component = executionerKey.get(player);
            if (component == null) return;
            executionerTargetField.set(component, targetId);
            syncComponent(executionerKey, component, player);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }
    private static void syncComponent(ComponentKey<?> key, Object component, Player player) {
        try {
            component.getClass().getMethod("sync").invoke(component);
        } catch (ReflectiveOperationException ignored) {
            key.sync(player);
        }
    }
    public static int noelleAbilityCooldown(Player player) {
        resolveAbility();
        if (abilityKey == null || abilityCooldownField == null) return 0;
        try {
            Object component = abilityKey.get(player);
            return component == null ? 0 : Math.max(0, abilityCooldownField.getInt(component));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return 0;
        }
    }
    public static void setNoelleAbilityCooldown(Player player, int ticks) {
        resolveAbility();
        if (abilityKey == null || abilityCooldownField == null) return;
        try {
            Object component = abilityKey.get(player);
            if (component == null) return;
            abilityCooldownField.setInt(component, Math.max(0, ticks));
            try {
                component.getClass().getMethod("sync").invoke(component);
            } catch (ReflectiveOperationException ignored) {
                abilityKey.sync(player);
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }
    public static boolean setVoodooTarget(Player player, UUID targetId) {
        resolveVoodoo();
        if (voodooKey == null || voodooTargetField == null) return false;
        try {
            Object component = voodooKey.get(player);
            if (component == null) return false;
            try {
                component.getClass().getMethod("setTarget", UUID.class).invoke(component, targetId);
            } catch (NoSuchMethodException exception) {
                voodooTargetField.set(component, targetId);
            }
            try {
                component.getClass().getMethod("sync").invoke(component);
            } catch (ReflectiveOperationException ignored) {
                voodooKey.sync(player);
            }
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
    public static UUID voodooTarget(Player player) {
        resolveVoodoo();
        if (voodooKey == null || voodooTargetField == null) return null;
        try {
            Object component = voodooKey.get(player);
            if (component == null) return null;
            Object target = voodooTargetField.get(component);
            return target instanceof UUID uuid ? uuid : null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
    public static boolean isVultured(Entity entity) {
        resolveVultureBody();
        if (vultureBodyKey == null || vulturedField == null) return false;
        try {
            Object component = vultureBodyKey.get(entity);
            return component != null && vulturedField.getBoolean(component);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
    private static void resolveBartender() {
        if (bartenderLookupAttempted) return;
        bartenderLookupAttempted = true;
        try {
            Class<?> clazz = Class.forName("org.agmas.noellesroles.bartender.BartenderPlayerComponent");
            bartenderKey = componentKey(clazz);
            bartenderArmorField = field(clazz, "armor");
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }
    private static void resolveExecutioner() {
        if (executionerLookupAttempted) return;
        executionerLookupAttempted = true;
        try {
            Class<?> clazz = Class.forName("org.agmas.noellesroles.executioner.ExecutionerPlayerComponent");
            executionerKey = componentKey(clazz);
            executionerTargetField = field(clazz, "target");
            executionerWonField = field(clazz, "won");
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }
    private static void resolveAbility() {
        if (abilityLookupAttempted) return;
        abilityLookupAttempted = true;
        try {
            Class<?> clazz = Class.forName("org.agmas.noellesroles.AbilityPlayerComponent");
            abilityKey = componentKey(clazz);
            abilityCooldownField = field(clazz, "cooldown");
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }
    private static void resolveVoodoo() {
        if (voodooLookupAttempted) return;
        voodooLookupAttempted = true;
        try {
            Class<?> clazz = Class.forName("org.agmas.noellesroles.voodoo.VoodooPlayerComponent");
            voodooKey = componentKey(clazz);
            voodooTargetField = field(clazz, "target");
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }
    private static void resolveVultureBody() {
        if (vultureLookupAttempted) return;
        vultureLookupAttempted = true;
        try {
            Class<?> clazz = Class.forName("org.agmas.noellesroles.coroner.BodyDeathReasonComponent");
            vultureBodyKey = componentKey(clazz);
            vulturedField = field(clazz, "vultured");
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }
    private static ComponentKey<?> componentKey(Class<?> clazz) throws ReflectiveOperationException {
        Field keyField;
        try {
            keyField = clazz.getField("KEY");
        } catch (NoSuchFieldException exception) {
            keyField = clazz.getDeclaredField("KEY");
            keyField.trySetAccessible();
        }
        Object key = keyField.get(null);
        return key instanceof ComponentKey<?> componentKey ? componentKey : null;
    }
    private static Field field(Class<?> clazz, String name) throws ReflectiveOperationException {
        try {
            return clazz.getField(name);
        } catch (NoSuchFieldException exception) {
            Field field = clazz.getDeclaredField(name);
            field.trySetAccessible();
            return field;
        }
    }
}
