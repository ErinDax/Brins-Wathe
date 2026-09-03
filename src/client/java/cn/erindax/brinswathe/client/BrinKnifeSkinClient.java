package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinKnifeSkins;
import cn.erindax.brinswathe.network.BrinKnifeSkinListS2CPacket;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import dev.doctor4t.wathe.client.model.item.KnifeModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;

public final class BrinKnifeSkinClient {
    private static final ModelResourceLocation FAKE_KNIFE_MODEL_ID = new ModelResourceLocation(
        ResourceLocation.fromNamespaceAndPath("noellesroles", "fake_knife"),
        "inventory"
    );

    private BrinKnifeSkinClient() {
    }

    public static void init() {
        try {
            Files.createDirectories(BrinKnifeSkins.skinsDir());
            BrinKnifeSkins.scanAndRegister();
            syncPack();
        } catch (IOException ignored) {
        }
        ModelLoadingPlugin.register(plugin -> {
            for (String name : BrinKnifeSkins.extraModelSkinNames()) {
                plugin.addModels(modelId(name, false), modelId(name, true));
            }
            plugin.modifyModelAfterBake().register((baked, context) -> {
                if (baked == null) return baked;
                BrinKnifeExtraBakes.remember(context.resourceId(), baked);
                var top = context.topLevelId();
                if (top == null || baked instanceof BrinKnifeItemModel) return baked;
                if (KnifeModelLoadingPlugin.KNIFE_MODEL_ID.equals(top) || FAKE_KNIFE_MODEL_ID.equals(top)) {
                    return new BrinKnifeItemModel(baked);
                }
                return baked;
            });
        });
    }

    public static ResourceLocation modelId(String skinName, boolean inHand) {
        String path = inHand ? "item/knife_" + skinName + "_in_hand" : "item/knife_" + skinName;
        return ResourceLocation.fromNamespaceAndPath("wathe", path);
    }

    public static void applyRemoteSkins(List<BrinKnifeSkinListS2CPacket.SkinEntry> skins) {
        try {
            Files.createDirectories(BrinKnifeSkins.skinsDir());
            if (skins != null) {
                for (BrinKnifeSkinListS2CPacket.SkinEntry entry : skins) {
                    Path target = BrinKnifeSkins.skinsDir().resolve("knife_" + entry.name() + ".png");
                    if (!Files.exists(target)) {
                        Files.write(target, entry.texture());
                    }
                    BrinKnifeSkins.registerDynamic(entry.name(), entry.tooltipName());
                }
            }
            reload();
        } catch (IOException ignored) {
        }
    }

    public static void reload() {
        BrinKnifeSkins.scanAndRegister();
        syncPack();
        if (!BrinKnifeSkins.dynamicSkinNames().isEmpty()) {
            enablePackAndReload();
        }
    }

    public static void syncPack() {
        try {
            Path skinsDir = BrinKnifeSkins.skinsDir();
            Path packDir = FabricLoader.getInstance().getGameDir()
                .resolve("resourcepacks")
                .resolve(BrinKnifeSkins.PACK_NAME);
            Path texDir = packDir.resolve("assets/wathe/textures/item");
            Path modelDir = packDir.resolve("assets/wathe/models/item");
            Files.createDirectories(texDir);
            Files.createDirectories(modelDir);
            clearPrefix(texDir, "knife_");
            clearPrefix(modelDir, "knife_");
            if (Files.isDirectory(skinsDir)) {
                try (var stream = Files.list(skinsDir)) {
                    stream.filter(path -> {
                        String fileName = path.getFileName().toString();
                        return fileName.startsWith("knife_") && fileName.endsWith(".png");
                    }).forEach(path -> {
                        try {
                            String fileName = path.getFileName().toString();
                            String rawName = fileName.substring("knife_".length(), fileName.length() - 4);
                            String skinName = BrinKnifeSkins.normalizeName(rawName);
                            Files.copy(
                                path,
                                texDir.resolve("knife_" + skinName + ".png"),
                                StandardCopyOption.REPLACE_EXISTING
                            );
                            writeModel(modelDir, skinName);
                        } catch (IOException ignored) {
                        }
                    });
                }
            }
            Files.writeString(
                packDir.resolve("pack.mcmeta"),
                "{\"pack\":{\"pack_format\":34,\"description\":\"wathe dynamic knife skins\"}}",
                StandardCharsets.UTF_8
            );
        } catch (IOException ignored) {
        }
    }

    private static void writeModel(Path modelDir, String skinName) throws IOException {
        String model = "{\n  \"parent\": \"wathe:item/template_knife\",\n  \"textures\": {\n    \"layer0\": \"wathe:item/knife_"
            + skinName
            + "\"\n  }\n}\n";
        Files.writeString(modelDir.resolve("knife_" + skinName + ".json"), model, StandardCharsets.UTF_8);
        Files.writeString(modelDir.resolve("knife_" + skinName + "_in_hand.json"), model, StandardCharsets.UTF_8);
    }

    private static void clearPrefix(Path dir, String prefix) throws IOException {
        if (!Files.isDirectory(dir)) return;
        try (var stream = Files.list(dir)) {
            stream.filter(path -> path.getFileName().toString().startsWith(prefix))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
        }
    }

    private static void enablePackAndReload() {
        Minecraft client = Minecraft.getInstance();
        PackRepository repository = client.getResourcePackRepository();
        repository.reload();
        String packId = "file/" + BrinKnifeSkins.PACK_NAME;
        List<String> selected = new ArrayList<>(repository.getSelectedIds());
        if (!selected.contains(packId)) {
            selected.add(packId);
            repository.setSelected(selected);
        }
        client.reloadResourcePacks();
    }
}
