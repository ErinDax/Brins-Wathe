package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinKnifeSkins;
import cn.erindax.brinswathe.BrinsWathe;
import cn.erindax.brinswathe.network.BrinKnifeSkinListS2CPacket;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public final class BrinKnifeSkinClient {
    private static final ModelResourceLocation FAKE_KNIFE_MODEL_ID = new ModelResourceLocation(
        ResourceLocation.fromNamespaceAndPath("noellesroles", "fake_knife"),
        "inventory"
    );
    private static final ModelResourceLocation REVOLVER_MODEL_ID = new ModelResourceLocation(
        ResourceLocation.fromNamespaceAndPath("wathe", "revolver"),
        "inventory"
    );

    private BrinKnifeSkinClient() {
    }

    public static void init() {
        try {
            Files.createDirectories(BrinKnifeSkins.skinsDir());
            Files.createDirectories(BrinKnifeSkins.gunSkinsDir());
            BrinKnifeSkins.scanAndRegister();
            syncPack();
        } catch (IOException ignored) {
        }
        ModelLoadingPlugin.register(plugin -> {
            for (String name : BrinKnifeSkins.extraModelSkinNames()) {
                plugin.addModels(modelId(name, false), modelId(name, true));
            }
            for (String name : BrinKnifeSkins.extraGunModelSkinNames()) {
                plugin.addModels(gunModelId(name));
            }
            plugin.modifyModelAfterBake().register((baked, context) -> {
                if (baked == null) return baked;
                BrinKnifeExtraBakes.remember(context.resourceId(), baked);
                BrinGunExtraBakes.remember(context.resourceId(), baked);
                var top = context.topLevelId();
                if (top == null || baked instanceof BrinKnifeItemModel) return baked;
                if (KnifeModelLoadingPlugin.KNIFE_MODEL_ID.equals(top) || FAKE_KNIFE_MODEL_ID.equals(top)) {
                    return new BrinKnifeItemModel(baked);
                }
                if (REVOLVER_MODEL_ID.equals(top)) {
                    return new BrinGunItemModel(baked);
                }
                return baked;
            });
        });
    }

    public static ResourceLocation modelId(String skinName, boolean inHand) {
        String path = inHand ? "item/knife_" + skinName + "_in_hand" : "item/knife_" + skinName;
        return ResourceLocation.fromNamespaceAndPath("wathe", path);
    }

    public static ResourceLocation gunModelId(String skinName) {
        return ResourceLocation.fromNamespaceAndPath("wathe", "item/revolver_" + skinName);
    }

    public static void applyRemoteSkins(List<BrinKnifeSkinListS2CPacket.SkinEntry> skins) {
        try {
            Files.createDirectories(BrinKnifeSkins.skinsDir());
            Files.createDirectories(BrinKnifeSkins.gunSkinsDir());
            boolean localServer = Minecraft.getInstance().getSingleplayerServer() != null;
            BrinKnifeSkins.clearDynamic();
            if (!localServer) {
                BrinKnifeSkins.wipeFolderAssets(BrinKnifeSkins.skinsDir(), "knife_");
                BrinKnifeSkins.wipeFolderAssets(BrinKnifeSkins.gunSkinsDir(), "gun_");
            }
            if (skins != null) {
                for (BrinKnifeSkinListS2CPacket.SkinEntry entry : skins) {
                    boolean gun = "gun".equalsIgnoreCase(entry.type());
                    Path dir = gun ? BrinKnifeSkins.gunSkinsDir() : BrinKnifeSkins.skinsDir();
                    String prefix = gun ? "gun_" : "knife_";
                    Path texture = dir.resolve(prefix + entry.name() + ".png");
                    Path sound = dir.resolve(gun
                        ? "gun_" + entry.name() + "_shoot.ogg"
                        : "knife_" + entry.name() + "_prepare.ogg");
                    if (!localServer || !Files.isRegularFile(texture)) {
                        Files.write(texture, entry.texture());
                    }
                    if (entry.sound() != null && entry.sound().length > 0) {
                        if (!localServer || !Files.isRegularFile(sound)) {
                            Files.write(sound, entry.sound());
                        }
                    }
                    if (gun) {
                        BrinKnifeSkins.registerDynamicGun(entry.name(), entry.tooltipName());
                    } else {
                        BrinKnifeSkins.registerDynamic(entry.name(), entry.tooltipName());
                    }
                    if ((entry.sound() != null && entry.sound().length > 0) || Files.isRegularFile(sound)) {
                        BrinKnifeSkins.registerDynamicSound(entry.type(), entry.name());
                    }
                }
            }
            syncPack();
            enablePackAndReload();
        } catch (IOException ignored) {
        }
    }

    public static void reload() {
        BrinKnifeSkins.scanAndRegister();
        syncPack();
        enablePackAndReload();
    }

    public static void syncPack() {
        try {
            Path skinsDir = BrinKnifeSkins.skinsDir();
            Path gunDir = BrinKnifeSkins.gunSkinsDir();
            Path packDir = FabricLoader.getInstance().getGameDir()
                .resolve("resourcepacks")
                .resolve(BrinKnifeSkins.PACK_NAME);
            Path texDir = packDir.resolve("assets/wathe/textures/item");
            Path modelDir = packDir.resolve("assets/wathe/models/item");
            Path soundDir = packDir.resolve("assets/brinswathe/sounds/item");
            Files.createDirectories(texDir);
            Files.createDirectories(modelDir);
            Files.createDirectories(soundDir);
            clearPrefix(texDir, "knife_");
            clearPrefix(texDir, "revolver_");
            clearPrefix(modelDir, "knife_");
            clearPrefix(modelDir, "revolver_");
            clearPrefix(soundDir, "knife_");
            clearPrefix(soundDir, "gun_");
            copyKnifeAssets(skinsDir, texDir, modelDir, soundDir);
            copyGunAssets(gunDir, texDir, modelDir, soundDir);
            writeSoundsJson(packDir, skinsDir, gunDir);
            Files.writeString(
                packDir.resolve("pack.mcmeta"),
                "{\"pack\":{\"pack_format\":34,\"description\":\"wathe dynamic weapon skins\"}}",
                StandardCharsets.UTF_8
            );
        } catch (IOException ignored) {
        }
    }

    public static void refreshHeldItems() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameRenderer == null) return;
        try {
            Object renderer = client.gameRenderer.itemInHandRenderer;
            var main = renderer.getClass().getDeclaredField("mainHandItem");
            var off = renderer.getClass().getDeclaredField("offHandItem");
            main.setAccessible(true);
            off.setAccessible(true);
            main.set(renderer, ItemStack.EMPTY);
            off.set(renderer, ItemStack.EMPTY);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static void playWorldSound(String type, String skin, Entity entity, float volume, float pitch) {
        if (entity == null) return;
        playWorldSound(type, skin, entity.getX(), entity.getY(), entity.getZ(), volume, pitch);
    }

    public static void playWorldSound(String type, String skin, double x, double y, double z, float volume, float pitch) {
        if (skin == null || skin.isBlank()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.getSoundManager() == null) return;
        client.getSoundManager().play(new SimpleSoundInstance(
            BrinKnifeSkins.soundLocation(type, skin),
            SoundSource.PLAYERS,
            volume,
            pitch,
            RandomSource.create(),
            false,
            0,
            SoundInstance.Attenuation.LINEAR,
            x,
            y,
            z,
            false
        ));
    }

    private static void copyKnifeAssets(Path skinsDir, Path texDir, Path modelDir, Path soundDir) throws IOException {
        if (!Files.isDirectory(skinsDir)) return;
        try (var stream = Files.list(skinsDir)) {
            stream.filter(path -> {
                String fileName = path.getFileName().toString();
                return fileName.startsWith("knife_") && fileName.endsWith(".png");
            }).forEach(path -> {
                try {
                    String fileName = path.getFileName().toString();
                    String rawName = fileName.substring("knife_".length(), fileName.length() - 4);
                    String skinName = BrinKnifeSkins.normalizeName(rawName);
                    if (!BrinKnifeSkins.isKnownSkin(skinName)) return;
                    Files.copy(path, texDir.resolve("knife_" + skinName + ".png"), StandardCopyOption.REPLACE_EXISTING);
                    writeModel(modelDir, skinName);
                    Path sound = skinsDir.resolve("knife_" + skinName + "_prepare.ogg");
                    if (Files.isRegularFile(sound)) {
                        Files.copy(
                            sound,
                            soundDir.resolve("knife_prepare_" + skinName + ".ogg"),
                            StandardCopyOption.REPLACE_EXISTING
                        );
                    }
                } catch (IOException ignored) {
                }
            });
        }
    }

    private static void copyGunAssets(Path gunDir, Path texDir, Path modelDir, Path soundDir) throws IOException {
        if (!Files.isDirectory(gunDir)) return;
        try (var stream = Files.list(gunDir)) {
            stream.filter(path -> {
                String fileName = path.getFileName().toString();
                return fileName.startsWith("gun_") && fileName.endsWith(".png");
            }).forEach(path -> {
                try {
                    String fileName = path.getFileName().toString();
                    String rawName = fileName.substring("gun_".length(), fileName.length() - 4);
                    String skinName = BrinKnifeSkins.normalizeName(rawName);
                    if (!BrinKnifeSkins.isKnownGunSkin(skinName)) return;
                    Files.copy(path, texDir.resolve("revolver_" + skinName + ".png"), StandardCopyOption.REPLACE_EXISTING);
                    writeGunModel(modelDir, skinName);
                    Path sound = gunDir.resolve("gun_" + skinName + "_shoot.ogg");
                    if (Files.isRegularFile(sound)) {
                        Files.copy(
                            sound,
                            soundDir.resolve("gun_shoot_" + skinName + ".ogg"),
                            StandardCopyOption.REPLACE_EXISTING
                        );
                    }
                } catch (IOException ignored) {
                }
            });
        }
    }

    private static void writeSoundsJson(Path packDir, Path skinsDir, Path gunDir) throws IOException {
        JsonObject root = new JsonObject();
        addSoundEvents(root, skinsDir, "knife_", "_prepare.ogg", "item.knife.prepare.", "item/knife_prepare_");
        addSoundEvents(root, gunDir, "gun_", "_shoot.ogg", "item.gun.shoot.", "item/gun_shoot_");
        Files.writeString(
            packDir.resolve("assets/brinswathe/sounds.json"),
            root.toString(),
            StandardCharsets.UTF_8
        );
    }

    private static void addSoundEvents(
        JsonObject root,
        Path dir,
        String prefix,
        String suffix,
        String eventPrefix,
        String filePrefix
    ) throws IOException {
        if (!Files.isDirectory(dir)) return;
        try (var stream = Files.list(dir)) {
            stream.filter(path -> {
                String fileName = path.getFileName().toString();
                return fileName.startsWith(prefix) && fileName.endsWith(suffix);
            }).forEach(path -> {
                String fileName = path.getFileName().toString();
                String raw = fileName.substring(prefix.length(), fileName.length() - suffix.length());
                String skinName = BrinKnifeSkins.normalizeName(raw);
                if (eventPrefix.contains("gun")
                    ? !BrinKnifeSkins.isKnownGunSkin(skinName)
                    : !BrinKnifeSkins.isKnownSkin(skinName)) {
                    return;
                }
                JsonObject event = new JsonObject();
                JsonArray sounds = new JsonArray();
                sounds.add(BrinsWathe.MOD_ID + ":" + filePrefix + skinName);
                event.add("sounds", sounds);
                root.add(eventPrefix + skinName, event);
            });
        }
    }

    private static void writeGunModel(Path modelDir, String skinName) throws IOException {
        String model = "{\n"
            + "  \"parent\": \"item/generated\",\n"
            + "  \"textures\": {\n"
            + "    \"layer0\": \"wathe:item/revolver_" + skinName + "\"\n"
            + "  },\n"
            + "  \"display\": {\n"
            + "    \"thirdperson_righthand\": {\"rotation\": [0, -90, 55], \"translation\": [0, 0.6, -0.6], \"scale\": [0.5, 0.5, 0.5]},\n"
            + "    \"thirdperson_lefthand\": {\"rotation\": [0, 90, -55], \"translation\": [0, 0.6, -0.6], \"scale\": [0.5, 0.5, 0.5]},\n"
            + "    \"firstperson_righthand\": {\"rotation\": [0, -90, 45], \"translation\": [1.13, 3.2, 1.13], \"scale\": [0.6, 0.6, 0.6]},\n"
            + "    \"firstperson_lefthand\": {\"rotation\": [0, 90, -45], \"translation\": [1.13, 3.2, 1.13], \"scale\": [0.6, 0.6, 0.6]}\n"
            + "  }\n"
            + "}\n";
        Files.writeString(modelDir.resolve("revolver_" + skinName + ".json"), model, StandardCharsets.UTF_8);
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
        client.reloadResourcePacks().whenComplete((ignored, error) ->
            client.execute(BrinKnifeSkinClient::refreshHeldItems));
    }
}
