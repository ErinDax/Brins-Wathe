package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BuySlots;
import cn.erindax.brinswathe.client.gui.PersonalSettingsScreen;
import cn.erindax.brinswathe.network.BuySlotC2SPacket;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class PersonalSettingsClient {
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("brinswathe-client.json");
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
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> syncBuySlot());
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

    private static void syncBuySlot() {
        if (serverSupportsBuySlot()) ClientPlayNetworking.send(new BuySlotC2SPacket(buySlot));
    }

    private static int clamp(int slot) {
        return slot >= 1 && slot <= BuySlots.HOTBAR_SIZE ? slot : 0;
    }

    private static void load() {
        if (!Files.isRegularFile(PATH)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(PATH, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.has("buy_slot")) buySlot = clamp(root.get("buy_slot").getAsInt());
        } catch (Exception ignored) {
        }
    }

    private static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("buy_slot", buySlot);
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, root.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
