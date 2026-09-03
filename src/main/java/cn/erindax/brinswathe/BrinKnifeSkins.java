package cn.erindax.brinswathe;

import cn.erindax.brinswathe.network.BrinKnifeSkinListS2CPacket;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.doctor4t.wathe.index.WatheDataComponentTypes;
import dev.doctor4t.wathe.index.WatheItems;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class BrinKnifeSkins {
    public static final String FOLDER_NAME = "knife_skin-b";
    public static final String PACK_NAME = "wathe_dynamic_skins";
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("brinswathe-knife-skins.json");
    private static final ResourceLocation FAKE_KNIFE_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "fake_knife");
    private static final Map<UUID, String> KNIFE_SKINS = new ConcurrentHashMap<>();
    private static final Map<String, String> DYNAMIC_SKINS = new LinkedHashMap<>();
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
        try {
            Class<?> skinClass = Class.forName("dev.doctor4t.wathe.item.KnifeItem$Skin");
            Object resolved = skinClass.getMethod("resolveName", String.class).invoke(null, raw);
            if (resolved instanceof String name) return name;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
        String bundled = resolveBundled(raw);
        if (bundled != null) return bundled;
        String dynamic = resolveDynamic(raw);
        if (dynamic != null) return dynamic;
        if (isOfficialSkin(raw)) return raw.toLowerCase(Locale.ROOT);
        return raw;
    }

    public static boolean isOfficialSkin(String name) {
        if (name == null) return false;
        try {
            Class<?> skinClass = Class.forName("dev.doctor4t.wathe.item.KnifeItem$Skin");
            Enum.valueOf(skinClass.asSubclass(Enum.class), name.toUpperCase(Locale.ROOT));
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
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
        return names;
    }

    public static List<String> extraModelSkinNames() {
        List<String> names = new ArrayList<>(BUNDLED_SKINS.keySet());
        names.addAll(DYNAMIC_SKINS.keySet());
        return names;
    }

    public static List<String> officialSkinNames() {
        List<String> names = new ArrayList<>();
        try {
            Class<?> skinClass = Class.forName("dev.doctor4t.wathe.item.KnifeItem$Skin");
            Object[] constants = skinClass.getEnumConstants();
            if (constants != null) {
                for (Object constant : constants) {
                    if (constant instanceof Enum<?> value) {
                        names.add(value.name().toLowerCase(Locale.ROOT));
                    }
                }
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
        if (names.isEmpty()) {
            names.add("default");
            names.add("ceremonial");
            names.add("pick");
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
        DYNAMIC_SKINS.putIfAbsent(name, tooltipName);
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
        Path dir = skinsDir();
        if (!Files.isDirectory(dir)) return registered;
        try (var stream = Files.list(dir)) {
            stream.filter(path -> {
                String fileName = path.getFileName().toString();
                return fileName.startsWith("knife_") && fileName.endsWith(".png");
            }).forEach(path -> {
                String fileName = path.getFileName().toString();
                String rawName = fileName.substring("knife_".length(), fileName.length() - 4);
                String skinName = uniqueName(normalizeName(rawName));
                if (!DYNAMIC_SKINS.containsKey(skinName)) {
                    registerDynamic(skinName, rawName);
                    registered.add(skinName);
                }
            });
        } catch (IOException ignored) {
        }
        return registered;
    }

    public static List<BrinKnifeSkinListS2CPacket.SkinEntry> collectSkinEntries() {
        List<BrinKnifeSkinListS2CPacket.SkinEntry> skins = new ArrayList<>();
        Path dir = skinsDir();
        if (!Files.isDirectory(dir)) return skins;
        try (var stream = Files.list(dir)) {
            stream.filter(path -> {
                String fileName = path.getFileName().toString();
                return fileName.startsWith("knife_") && fileName.endsWith(".png");
            }).forEach(path -> {
                try {
                    byte[] bytes = Files.readAllBytes(path);
                    if (bytes.length > 512_000) return;
                    String fileName = path.getFileName().toString();
                    String rawName = fileName.substring("knife_".length(), fileName.length() - 4);
                    String skinName = normalizeName(rawName);
                    skins.add(new BrinKnifeSkinListS2CPacket.SkinEntry(skinName, rawName, bytes));
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
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

    private static String uniqueName(String skinName) {
        if (resolveKnifeSkinNameExisting(skinName) == null) return skinName;
        int index = 2;
        while (resolveKnifeSkinNameExisting(skinName + "_" + index) != null) index++;
        return skinName + "_" + index;
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
