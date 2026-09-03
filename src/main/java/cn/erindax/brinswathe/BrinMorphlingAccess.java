package cn.erindax.brinswathe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.component.ComponentKey;

public final class BrinMorphlingAccess {
    private static boolean lookupAttempted;
    private static ComponentKey<?> key;
    private static Field morphTicksField;
    private static Method getMorphTicks;
    private static Method startMorph;

    private BrinMorphlingAccess() {
    }

    public static int morphTicks(Player player) {
        Object component = component(player);
        if (component == null) return 0;
        if (morphTicksField != null) {
            try {
                return morphTicksField.getInt(component);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                return 0;
            }
        }
        if (getMorphTicks == null) return 0;
        try {
            return (Integer) getMorphTicks.invoke(component);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return 0;
        }
    }
    public static UUID disguise(Player player) {
        Object component = component(player);
        if (component == null) return null;
        try {
            Object value = component.getClass().getField("disguise").get(component);
            return value instanceof UUID uuid ? uuid : null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
    public static boolean startMorph(Player player, UUID targetId) {
        Object component = component(player);
        if (component == null || startMorph == null || targetId == null) return false;
        try {
            Object result = startMorph.invoke(component, targetId);
            return result instanceof Boolean ok ? ok : true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
    private static Object component(Player player) {
        resolve();
        if (key == null || player == null) return null;
        try {
            return key.get(player);
        } catch (RuntimeException ignored) {
            return null;
        }
    }
    private static void resolve() {
        if (lookupAttempted) return;
        lookupAttempted = true;
        try {
            Class<?> clazz = Class.forName("org.agmas.noellesroles.morphling.MorphlingPlayerComponent");
            key = (ComponentKey<?>) clazz.getField("KEY").get(null);
            getMorphTicks = clazz.getMethod("getMorphTicks");
            startMorph = clazz.getMethod("startMorph", UUID.class);
            try {
                morphTicksField = clazz.getField("morphTicks");
            } catch (NoSuchFieldException exception) {
                morphTicksField = clazz.getDeclaredField("morphTicks");
                morphTicksField.trySetAccessible();
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }
}
