package cn.erindax.brinswathe;

import cn.erindax.brinswathe.network.BrinKnifeSkinListS2CPacket;
import cn.erindax.brinswathe.network.BrinSkinSoundS2CPacket;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.doctor4t.wathe.index.WatheDataComponentTypes;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.item.KnifeItem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class BrinKnifeSkins {
    public static final String FOLDER_NAME = "knife_skin-b";
    public static final String GUN_FOLDER_NAME = "gun_skin-b";
    public static final String PACK_NAME = "wathe_dynamic_skins";
    public static final int MAX_TEXTURE_BYTES = 512_000;
    public static final int MAX_SOUND_BYTES = 512_000;
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("brinswathe-knife-skins.json");
    private static final ResourceLocation FAKE_KNIFE_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "fake_knife");
    private static final Map<UUID, String> KNIFE_SKINS = new ConcurrentHashMap<>();
    private static final Map<String, String> DYNAMIC_SKINS = new LinkedHashMap<>();
    private static final Map<String, String> DYNAMIC_GUNS = new LinkedHashMap<>();
    private static final Set<String> DYNAMIC_SOUNDS = new LinkedHashSet<>();
    private static final Map<String, String> BUNDLED_SKINS = new LinkedHashMap<>();

    static {
        BUNDLED_SKINS.put("emerald", "绿宝石");
        BUNDLED_SKINS.put("bowtie", "蝴蝶结");
        BUNDLED_SKINS.put("red_sickle", "红镰刀");
        BUNDLED_SKINS.put("kunai", "苦无");
        BUNDLED_SKINS.put("shadow_blade", "影刀");
        BUNDLED_SKINS.put("qiaolezi", "巧乐兹");
        BUNDLED_SKINS.put("puruisaissi", "普瑞赛斯");
        BUNDLED_SKINS.put("maomochui", "猫陌锤");
        BUNDLED_SKINS.put("sha_bi", "鲨匕");
    }

    private BrinKnifeSkins() {
    }

    public static Path skinsDir() {
        return FabricLoader.getInstance().getGameDir().resolve(FOLDER_NAME);
    }

    public static Path gunSkinsDir() {
        return FabricLoader.getInstance().getGameDir().resolve(GUN_FOLDER_NAME);
    }

    public static Path dirFor(String type) {
        return "gun".equalsIgnoreCase(type) ? gunSkinsDir() : skinsDir();
    }

    public static void load() {
        KNIFE_SKINS.clear();
        if (!Files.isRegularFile(PATH)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(PATH, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                try {
                    KNIFE_SKINS.put(UUID.fromString(entry.getKey()), entry.getValue().getAsString());
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        for (Map.Entry<UUID, String> entry : KNIFE_SKINS.entrySet()) {
            root.addProperty(entry.getKey().toString(), entry.getValue());
        }
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, root.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static void setKnifeSkin(UUID playerId, String skinName) {
        if (skinName == null || skinName.isBlank()) {
            KNIFE_SKINS.remove(playerId);
        } else {
            KNIFE_SKINS.put(playerId, skinName);
        }
        save();
    }

    public static String knifeSkin(UUID playerId) {
        return KNIFE_SKINS.get(playerId);
    }

    public static void syncHeldSkins(ServerPlayer player) {
        List<Pair<EquipmentSlot, ItemStack>> equipment = List.of(
            Pair.of(EquipmentSlot.MAINHAND, player.getMainHandItem().copy()),
            Pair.of(EquipmentSlot.OFFHAND, player.getOffhandItem().copy())
        );
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(player.getId(), equipment);
        for (ServerPlayer watcher : PlayerLookup.tracking(player)) {
            watcher.connection.send(packet);
        }
        player.connection.send(packet);
    }

    public static boolean applySkin(ItemStack stack, String type, String skinName) {
        if (stack.isEmpty()) return false;
        boolean match = "knife".equalsIgnoreCase(type)
            ? isKnifeLike(stack)
            : stack.getItem() instanceof dev.doctor4t.wathe.item.RevolverItem;
        if (!match) return false;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> nbt.putString("wathe_skin", skinName));
        return true;
    }

    public static void stampOwnerAndSkin(ServerPlayer player, ItemStack stack) {
        if (!isKnifeLike(stack)) return;
        stack.set(WatheDataComponentTypes.OWNER, player.getUUID().toString());
        String skin = KNIFE_SKINS.get(player.getUUID());
        if (skin == null) return;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> {
            nbt.putString("wathe_skin", skin);
            nbt.putString("wathe_skin_owner", player.getUUID().toString());
        });
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String desired = KNIFE_SKINS.get(player.getUUID());
            boolean dirty = false;
            for (ItemStack stack : player.getInventory().items) {
                if (syncStack(stack, player, desired)) dirty = true;
            }
            for (ItemStack stack : player.getInventory().offhand) {
                if (syncStack(stack, player, desired)) dirty = true;
            }
            if (dirty) {
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
            }
        }
    }

    public static String normalizeName(String rawName) {
        String normalized = rawName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_").replaceAll("_+", "_");
        if (normalized.startsWith("_")) normalized = normalized.substring(1);
        if (normalized.endsWith("_")) normalized = normalized.substring(0, normalized.length() - 1);
        return normalized.isEmpty() ? "skin" : normalized;
    }

    public static String resolveKnifeSkinName(String raw) {
        if (raw == null || raw.isBlank()) return raw;
        KnifeItem.Skin official = officialSkin(raw);
        if (official != null) return official.getName();
        String bundled = resolveBundled(raw);
        if (bundled != null) return bundled;
        String dynamic = resolveDynamic(raw);
        if (dynamic != null) return dynamic;
        return raw;
    }

    public static boolean isOfficialSkin(String name) {
        return officialSkin(name) != null;
    }

    public static boolean isDynamic(String name) {
        return name != null && DYNAMIC_SKINS.containsKey(name);
    }

    public static boolean isBundledSkin(String name) {
        return name != null && BUNDLED_SKINS.containsKey(name);
    }

    public static boolean shouldBrinRender(String name) {
        return isBundledSkin(name) || isDynamic(name);
    }

    public static String tooltipName(String name) {
        if (name == null) return "";
        String bundled = BUNDLED_SKINS.get(name);
        if (bundled != null) return bundled;
        return DYNAMIC_SKINS.getOrDefault(name, name);
    }

    public static List<String> suggestionNames() {
        List<String> names = new ArrayList<>(allKnownNames());
        names.addAll(BUNDLED_SKINS.values());
        names.addAll(DYNAMIC_SKINS.values());
        names.addAll(DYNAMIC_GUNS.keySet());
        names.addAll(DYNAMIC_GUNS.values());
        return names;
    }

    public static List<String> extraModelSkinNames() {
        List<String> names = new ArrayList<>(BUNDLED_SKINS.keySet());
        names.addAll(DYNAMIC_SKINS.keySet());
        return names;
    }

    public static List<String> officialSkinNames() {
        List<String> names = new ArrayList<>();
        for (KnifeItem.Skin skin : KnifeItem.Skin.values()) {
            names.add(skin.getName());
        }
        return names;
    }

    public static List<String> allKnownNames() {
        List<String> names = new ArrayList<>(officialSkinNames());
        for (String name : extraModelSkinNames()) {
            if (!names.contains(name)) names.add(name);
        }
        return names;
    }

    public static boolean isKnownSkin(String name) {
        if (name == null || name.isBlank()) return false;
        String resolved = resolveKnifeSkinName(name);
        return allKnownNames().contains(resolved);
    }

    public static String nextName(String current) {
        List<String> names = allKnownNames();
        if (names.isEmpty()) return "default";
        String resolved = resolveKnifeSkinName(current);
        int index = names.indexOf(resolved);
        return names.get((index + 1) % names.size());
    }

    public static String knownNamesText() {
        return String.join(" / ", allKnownNames());
    }

    public static String knownGunNamesText() {
        return DYNAMIC_GUNS.isEmpty() ? "-" : String.join(" / ", DYNAMIC_GUNS.keySet());
    }

    public static List<String> dynamicSkinNames() {
        return new ArrayList<>(DYNAMIC_SKINS.keySet());
    }

    public static String resolveBundled(String input) {
        if (input == null) return null;
        for (Map.Entry<String, String> entry : BUNDLED_SKINS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(input) || entry.getValue().equals(input)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static synchronized void registerDynamic(String name, String tooltipName) {
        DYNAMIC_SKINS.put(name, tooltipName);
    }

    public static synchronized void registerDynamicGun(String name, String tooltipName) {
        DYNAMIC_GUNS.put(name, tooltipName);
    }

    public static synchronized void registerDynamicSound(String type, String name) {
        if (name == null || name.isBlank()) return;
        DYNAMIC_SOUNDS.add(soundKey(type, name));
    }

    public static synchronized void clearDynamic() {
        DYNAMIC_SKINS.clear();
        DYNAMIC_GUNS.clear();
        DYNAMIC_SOUNDS.clear();
    }

    public static List<String> deletableNames(String type) {
        return "gun".equalsIgnoreCase(type)
            ? extraGunModelSkinNames()
            : new ArrayList<>(DYNAMIC_SKINS.keySet());
    }

    public static void wipeFolderAssets(Path dir, String prefix) {
        if (!Files.isDirectory(dir)) return;
        try (var stream = Files.list(dir)) {
            stream.filter(path -> {
                String fileName = path.getFileName().toString();
                return fileName.startsWith(prefix) && (fileName.endsWith(".png") || fileName.endsWith(".ogg"));
            }).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    public static List<String> extraGunModelSkinNames() {
        return new ArrayList<>(DYNAMIC_GUNS.keySet());
    }

    public static String tooltipGunName(String name) {
        if (name == null) return "";
        return DYNAMIC_GUNS.getOrDefault(name, name);
    }

    public static boolean isDynamicGun(String name) {
        return name != null && DYNAMIC_GUNS.containsKey(name);
    }

    public static boolean shouldBrinRenderGun(String name) {
        return isDynamicGun(name);
    }

    public static String resolveGunSkinName(String input) {
        if (input == null || input.isBlank()) return input;
        for (Map.Entry<String, String> entry : DYNAMIC_GUNS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(input) || entry.getValue().equals(input)) {
                return entry.getKey();
            }
        }
        return normalizeName(input);
    }

    public static boolean isKnownGunSkin(String name) {
        if (name == null || name.isBlank()) return false;
        return DYNAMIC_GUNS.containsKey(resolveGunSkinName(name));
    }

    public static boolean hasSound(String type, String name) {
        return name != null && DYNAMIC_SOUNDS.contains(soundKey(type, name));
    }

    public static String soundEventPath(String type, String name) {
        return "gun".equalsIgnoreCase(type)
            ? "item.gun.shoot." + name
            : "item.knife.prepare." + name;
    }

    public static ResourceLocation soundLocation(String type, String name) {
        return ResourceLocation.fromNamespaceAndPath(BrinsWathe.MOD_ID, soundEventPath(type, name));
    }

    public static String acceptUpload(String type, String rawName, String tooltip, byte[] texture, byte[] sound) {
        if (!BrinSkinEditors.isType(type) || texture == null || !isPng(texture)) return null;
        if (texture.length > MAX_TEXTURE_BYTES) return null;
        if (sound != null && sound.length > 0 && (!isOgg(sound) || sound.length > MAX_SOUND_BYTES)) return null;
        int soundSize = sound == null ? 0 : sound.length;
        if (texture.length + soundSize > 900_000) return null;
        String skinName = uniqueUploadName(type, normalizeName(rawName));
        if (isOfficialSkin(skinName) || isBundledSkin(skinName)) {
            skinName = uniqueUploadName(type, skinName + "_custom");
        }
        Path dir = dirFor(type);
        try {
            Files.createDirectories(dir);
            String prefix = "gun".equalsIgnoreCase(type) ? "gun_" : "knife_";
            Files.write(dir.resolve(prefix + skinName + ".png"), texture);
            if (sound != null && sound.length > 0) {
                Files.write(dir.resolve(soundFileName(type, skinName)), sound);
                DYNAMIC_SOUNDS.add(soundKey(type, skinName));
            }
        } catch (IOException exception) {
            return null;
        }
        String label = tooltip == null || tooltip.isBlank() ? rawName : tooltip;
        if ("gun".equalsIgnoreCase(type)) {
            registerDynamicGun(skinName, label);
        } else {
            registerDynamic(skinName, label);
        }
        return skinName;
    }

    public static synchronized String deleteUploaded(String type, String rawName, boolean deleteFiles) {
        if (!BrinSkinEditors.isType(type) || rawName == null || rawName.isBlank()) return null;
        boolean gun = "gun".equalsIgnoreCase(type);
        String skinName = gun ? resolveGunSkinName(rawName) : resolveKnifeSkinName(rawName);
        if (!gun && (isOfficialSkin(skinName) || isBundledSkin(skinName))) return null;
        if (gun && !DYNAMIC_GUNS.containsKey(skinName)) return null;
        if (!gun && !DYNAMIC_SKINS.containsKey(skinName)) return null;
        if (deleteFiles) {
            Path dir = dirFor(type);
            String prefix = gun ? "gun_" : "knife_";
            try {
                Files.deleteIfExists(dir.resolve(prefix + skinName + ".png"));
                Files.deleteIfExists(dir.resolve(soundFileName(type, skinName)));
            } catch (IOException ignored) {
            }
        }
        if (gun) DYNAMIC_GUNS.remove(skinName);
        else DYNAMIC_SKINS.remove(skinName);
        DYNAMIC_SOUNDS.remove(soundKey(type, skinName));
        return skinName;
    }

    public static void broadcastSound(ServerPlayer source, String type, String skin, float volume, float pitch) {
        if (source == null || !hasSound(type, skin)) return;
        BrinSkinSoundS2CPacket packet = new BrinSkinSoundS2CPacket(
            type,
            skin,
            source.getX(),
            source.getY(),
            source.getZ(),
            volume,
            pitch
        );
        for (ServerPlayer player : source.serverLevel().players()) {
            if (player.distanceToSqr(source) <= 64 * 64) {
                ServerPlayNetworking.send(player, packet);
            }
        }
    }

    public static boolean isPng(byte[] bytes) {
        return bytes != null
            && bytes.length >= 8
            && (bytes[0] & 0xFF) == 0x89
            && bytes[1] == 0x50
            && bytes[2] == 0x4E
            && bytes[3] == 0x47
            && bytes[4] == 0x0D
            && bytes[5] == 0x0A
            && bytes[6] == 0x1A
            && bytes[7] == 0x0A;
    }

    public static boolean isOgg(byte[] bytes) {
        return bytes != null
            && bytes.length >= 4
            && bytes[0] == 'O'
            && bytes[1] == 'g'
            && bytes[2] == 'g'
            && bytes[3] == 'S';
    }

    public static String resolveDynamic(String input) {
        if (input == null) return null;
        for (Map.Entry<String, String> entry : DYNAMIC_SKINS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(input) || entry.getValue().equals(input)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static synchronized List<String> scanAndRegister() {
        List<String> registered = new ArrayList<>();
        registered.addAll(scanFolder("knife", skinsDir(), "knife_", DYNAMIC_SKINS, true));
        registered.addAll(scanFolder("gun", gunSkinsDir(), "gun_", DYNAMIC_GUNS, false));
        return registered;
    }

    public static List<String> reloadFromDisk(MinecraftServer server) {
        try {
            Files.createDirectories(skinsDir());
            Files.createDirectories(gunSkinsDir());
        } catch (IOException ignored) {
        }
        List<String> registered = scanAndRegister();
        if (server != null) syncToAll(server);
        return registered;
    }

    public static List<BrinKnifeSkinListS2CPacket.SkinEntry> collectSkinEntries() {
        List<BrinKnifeSkinListS2CPacket.SkinEntry> skins = new ArrayList<>();
        collectFolder(skins, "knife", skinsDir(), "knife_");
        collectFolder(skins, "gun", gunSkinsDir(), "gun_");
        return skins;
    }

    public static void syncTo(ServerPlayer player) {
        ServerPlayNetworking.send(player, new BrinKnifeSkinListS2CPacket(collectSkinEntries()));
    }

    public static void syncToAll(MinecraftServer server) {
        BrinKnifeSkinListS2CPacket packet = new BrinKnifeSkinListS2CPacket(collectSkinEntries());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, packet);
        }
    }

    private static List<String> scanFolder(
        String type,
        Path dir,
        String prefix,
        Map<String, String> target,
        boolean knifeNames
    ) {
        List<String> registered = new ArrayList<>();
        if (!Files.isDirectory(dir)) return registered;
        try (var stream = Files.list(dir)) {
            stream.filter(path -> {
                String fileName = path.getFileName().toString();
                return fileName.startsWith(prefix) && fileName.endsWith(".png");
            }).forEach(path -> {
                String fileName = path.getFileName().toString();
                String rawName = fileName.substring(prefix.length(), fileName.length() - 4);
                String fileSkin = normalizeName(rawName);
                String skinName = fileSkin;
                if (!target.containsKey(fileSkin)) {
                    skinName = knifeNames ? uniqueName(fileSkin) : uniqueGunName(fileSkin);
                    target.put(skinName, rawName);
                    registered.add(skinName);
                }
                if (Files.isRegularFile(dir.resolve(soundFileName(type, fileSkin)))) {
                    DYNAMIC_SOUNDS.add(soundKey(type, target.containsKey(fileSkin) ? fileSkin : skinName));
                }
            });
        } catch (IOException ignored) {
        }
        return registered;
    }

    private static void collectFolder(
        List<BrinKnifeSkinListS2CPacket.SkinEntry> skins,
        String type,
        Path dir,
        String prefix
    ) {
        if (!Files.isDirectory(dir)) return;
        try (var stream = Files.list(dir)) {
            stream.filter(path -> {
                String fileName = path.getFileName().toString();
                return fileName.startsWith(prefix) && fileName.endsWith(".png");
            }).forEach(path -> {
                try {
                    byte[] bytes = Files.readAllBytes(path);
                    if (bytes.length > MAX_TEXTURE_BYTES) return;
                    String fileName = path.getFileName().toString();
                    String rawName = fileName.substring(prefix.length(), fileName.length() - 4);
                    String skinName = normalizeName(rawName);
                    if ("gun".equalsIgnoreCase(type) ? !DYNAMIC_GUNS.containsKey(skinName) : !DYNAMIC_SKINS.containsKey(skinName)) {
                        return;
                    }
                    byte[] sound = new byte[0];
                    Path soundPath = dir.resolve(soundFileName(type, skinName));
                    if (Files.isRegularFile(soundPath)) {
                        byte[] soundBytes = Files.readAllBytes(soundPath);
                        if (soundBytes.length <= MAX_SOUND_BYTES && isOgg(soundBytes)) {
                            sound = soundBytes;
                            DYNAMIC_SOUNDS.add(soundKey(type, skinName));
                        }
                    }
                    skins.add(new BrinKnifeSkinListS2CPacket.SkinEntry(type, skinName, rawName, bytes, sound));
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    private static String soundKey(String type, String name) {
        return BrinSkinEditors.normalizeType(type) + ":" + name;
    }

    private static String soundFileName(String type, String name) {
        return "gun".equalsIgnoreCase(type) ? "gun_" + name + "_shoot.ogg" : "knife_" + name + "_prepare.ogg";
    }

    private static String uniqueUploadName(String type, String skinName) {
        if ("gun".equalsIgnoreCase(type)) {
            return DYNAMIC_GUNS.containsKey(skinName) ? skinName : uniqueGunName(skinName);
        }
        if (DYNAMIC_SKINS.containsKey(skinName)) return skinName;
        return uniqueName(skinName);
    }

    private static String uniqueGunName(String skinName) {
        if (!DYNAMIC_GUNS.containsKey(skinName)) return skinName;
        int index = 2;
        while (DYNAMIC_GUNS.containsKey(skinName + "_" + index)) index++;
        return skinName + "_" + index;
    }

    private static String uniqueName(String skinName) {
        if (resolveKnifeSkinNameExisting(skinName) == null) return skinName;
        int index = 2;
        while (resolveKnifeSkinNameExisting(skinName + "_" + index) != null) index++;
        return skinName + "_" + index;
    }

    private static KnifeItem.Skin officialSkin(String name) {
        if (name == null || name.isBlank()) return null;
        for (KnifeItem.Skin skin : KnifeItem.Skin.values()) {
            if (skin.getName().equalsIgnoreCase(name)
                || skin.name().equalsIgnoreCase(name)
                || skin.tooltipName.equals(name)) {
                return skin;
            }
        }
        return null;
    }

    private static String resolveKnifeSkinNameExisting(String name) {
        if (isOfficialSkin(name) || isBundledSkin(name) || DYNAMIC_SKINS.containsKey(name)) return name;
        String bundled = resolveBundled(name);
        if (bundled != null) return bundled;
        return resolveDynamic(name);
    }

    private static boolean syncStack(ItemStack stack, ServerPlayer player, String desiredSkin) {
        if (!stack.is(WatheItems.KNIFE)) return false;
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag nbt = data.copyTag();
        boolean hasSkin = nbt.contains("wathe_skin");
        String owner = nbt.contains("wathe_skin_owner") ? nbt.getString("wathe_skin_owner") : null;
        if (desiredSkin != null && !hasSkin) {
            nbt.putString("wathe_skin", desiredSkin);
            nbt.putString("wathe_skin_owner", player.getUUID().toString());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            return true;
        }
        if (hasSkin && owner != null && !owner.equals(player.getUUID().toString())) {
            nbt.remove("wathe_skin");
            nbt.remove("wathe_skin_owner");
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            return true;
        }
        return false;
    }

    private static boolean isKnifeLike(ItemStack stack) {
        return stack.is(WatheItems.KNIFE)
            || FAKE_KNIFE_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
