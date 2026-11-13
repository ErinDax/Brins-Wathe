package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BuySlots;
import cn.erindax.brinswathe.client.gui.PersonalSettingsScreen;
import cn.erindax.brinswathe.network.BuySlotC2SPacket;
import cn.erindax.brinswathe.network.ItemSlotsC2SPacket;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class PersonalSettingsClient {
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("brinswathe-client.json");
    private static final Map<ResourceLocation, Integer> ITEM_SLOTS = new LinkedHashMap<>();
    private static KeyMapping openBind;
    private static int buySlot;

    private PersonalSettingsClient() {
    }

    public static void init() {
        load();
        openBind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.brinswathe.music_box",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "key.categories.brinswathe"
        ));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            syncBuySlot();
            syncItemSlots();
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openBind.consumeClick()) {
                if (client.screen == null && client.player != null) {
                    client.setScreen(new PersonalSettingsScreen());
                }
            }
        });
    }

    public static int buySlot() {
        return buySlot;
    }

    public static void setBuySlot(int slot) {
        buySlot = clamp(slot);
        save();
        syncBuySlot();
    }

    public static boolean serverSupportsBuySlot() {
        return ClientPlayNetworking.canSend(BuySlotC2SPacket.TYPE);
    }

    public static int itemSlot(Item item) {
        return ITEM_SLOTS.getOrDefault(BuiltInRegistries.ITEM.getKey(item), 0);
    }

    public static void setItemSlot(Item item, int slot) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        int value = clamp(slot);
        if (value == 0) {
            ITEM_SLOTS.remove(id);
        } else {
            ITEM_SLOTS.put(id, value);
        }
        save();
        syncItemSlots();
    }

    public static List<ResourceLocation> itemSlotIds() {
        return new ArrayList<>(ITEM_SLOTS.keySet());
    }

    public static boolean serverSupportsItemSlots() {
        return ClientPlayNetworking.canSend(ItemSlotsC2SPacket.TYPE);
    }

    private static void syncBuySlot() {
        if (serverSupportsBuySlot()) ClientPlayNetworking.send(new BuySlotC2SPacket(buySlot));
    }

    private static void syncItemSlots() {
        if (!serverSupportsItemSlots()) return;
        Map<ResourceLocation, Integer> slots = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Integer> entry : ITEM_SLOTS.entrySet()) {
            if (slots.size() >= ItemSlotsC2SPacket.MAX_ENTRIES) break;
            slots.put(entry.getKey(), entry.getValue());
        }
        ClientPlayNetworking.send(new ItemSlotsC2SPacket(slots));
    }

    private static int clamp(int slot) {
        return slot >= 1 && slot <= BuySlots.HOTBAR_SIZE ? slot : 0;
    }

    private static void load() {
        if (!Files.isRegularFile(PATH)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(PATH, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.has("buy_slot")) buySlot = clamp(root.get("buy_slot").getAsInt());
            if (root.has("item_slots")) {
                for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("item_slots").entrySet()) {
                    ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
                    int slot = clamp(entry.getValue().getAsInt());
                    if (id != null && slot != 0) ITEM_SLOTS.put(id, slot);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("buy_slot", buySlot);
        JsonObject itemSlots = new JsonObject();
        ITEM_SLOTS.forEach((id, slot) -> itemSlots.addProperty(id.toString(), slot));
        root.add("item_slots", itemSlots);
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, root.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
