package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinsWathe;
import net.minecraft.client.Minecraft;

public final class ResourceReloadClient {
    private ResourceReloadClient() {
    }

    static void reloadClientResources(Minecraft client) {
        if (client.player == null || client.level == null) return;
        client.getLanguageManager().onResourceManagerReload(client.getResourceManager());
        reloadStarryGuidebook(client);
    }

    private static void reloadStarryGuidebook(Minecraft client) {
        if (client.player == null || client.level == null) return;
        try {
            Class<?> collectorClass = Class.forName(
                "org.aussiebox.starexpress.client.guidebook.GuidebookEntryCollector"
            );
            Object collector = collectorClass.getField("INSTANCE").get(null);
            for (java.lang.reflect.Method method : collectorClass.getMethods()) {
                if (!method.getName().equals("reload") || method.getParameterCount() != 1) continue;
                method.invoke(collector, client.getResourceManager());
                return;
            }
            BrinsWathe.LOGGER.warn("Could not find Starry Express guidebook reload method");
        } catch (ClassNotFoundException ignored) {
        } catch (ReflectiveOperationException | IllegalArgumentException exception) {
            BrinsWathe.LOGGER.warn("Failed to reload Starry Express guidebook: {}", exception.getMessage());
        }
    }
}
