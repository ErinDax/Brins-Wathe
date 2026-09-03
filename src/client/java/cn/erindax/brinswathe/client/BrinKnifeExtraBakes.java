package cn.erindax.brinswathe.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class BrinKnifeExtraBakes {
    private static final Map<String, BakedModel[]> MODELS = new ConcurrentHashMap<>();
    private BrinKnifeExtraBakes() {
    }
    public static void remember(ResourceLocation id, BakedModel model) {
        if (id == null || model == null) return;
        if (!"wathe".equals(id.getNamespace())) return;
        String path = id.getPath();
        if (!path.startsWith("item/knife_")) return;
        String rest = path.substring("item/knife_".length());
        boolean inHand = rest.endsWith("_in_hand");
        String skin = inHand ? rest.substring(0, rest.length() - "_in_hand".length()) : rest;
        if (skin.isEmpty()) return;
        BakedModel[] variants = MODELS.computeIfAbsent(skin, ignored -> new BakedModel[2]);
        variants[inHand ? 1 : 0] = model;
    }
    public static BakedModel get(String skin, boolean inHand) {
        if (skin == null) return null;
        BakedModel[] variants = MODELS.get(skin);
        if (variants == null) return null;
        return variants[inHand ? 1 : 0];
    }
}
