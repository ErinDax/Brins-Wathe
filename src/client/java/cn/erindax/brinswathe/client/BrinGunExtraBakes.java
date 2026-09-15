package cn.erindax.brinswathe.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class BrinGunExtraBakes {
    private static final Map<String, BakedModel> MODELS = new ConcurrentHashMap<>();

    private BrinGunExtraBakes() {
    }

    public static void remember(ResourceLocation id, BakedModel model) {
        if (id == null || model == null) return;
        if (!"wathe".equals(id.getNamespace())) return;
        String path = id.getPath();
        if (!path.startsWith("item/revolver_")) return;
        String skin = path.substring("item/revolver_".length());
        if (!skin.isEmpty()) MODELS.put(skin, model);
    }

    public static BakedModel get(String skin) {
        return skin == null ? null : MODELS.get(skin);
    }
}
