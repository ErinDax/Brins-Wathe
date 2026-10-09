package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.admin.BrinAdminPanel;
import cn.erindax.brinswathe.admin.BrinAdminSnapshot;
import cn.erindax.brinswathe.client.gui.BrinAdminListScreen;
import cn.erindax.brinswathe.client.gui.BrinAdminScreen;
import cn.erindax.brinswathe.client.gui.BrinPersonalSettingsScreen;
import cn.erindax.brinswathe.network.BrinAdminActionC2SPacket;
import cn.erindax.brinswathe.network.BrinAdminSaveC2SPacket;
import cn.erindax.brinswathe.network.BrinAdminSnapshotS2CPacket;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public final class BrinAdminClient {
    private static final int CHUNK_BYTES = 24000;

    private BrinAdminClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(BrinAdminSnapshotS2CPacket.TYPE, (payload, context) ->
            receive(context.client(), payload));
    }

    public static JsonArray op(String... parts) {
        JsonArray op = new JsonArray();
        for (String part : parts) op.add(part);
        return op;
    }

    public static void save(List<JsonArray> ops) {
        save(ops, false);
    }

    public static void run(JsonArray op) {
        save(List.of(op), true);
    }

    private static void save(List<JsonArray> ops, boolean quiet) {
        if (ops.isEmpty() || !ClientPlayNetworking.canSend(BrinAdminSaveC2SPacket.TYPE)) return;
        JsonArray chunk = new JsonArray();
        int bytes = 0;
        for (JsonArray op : ops) {
            int size = op.toString().getBytes(StandardCharsets.UTF_8).length + 1;
            if (chunk.size() > 0 && bytes + size > CHUNK_BYTES) {
                send(chunk, false, quiet);
                chunk = new JsonArray();
                bytes = 0;
            }
            chunk.add(op);
            bytes += size;
        }
        send(chunk, true, quiet);
    }

    public static void action(String action) {
        if (ClientPlayNetworking.canSend(BrinAdminActionC2SPacket.TYPE)) {
            ClientPlayNetworking.send(new BrinAdminActionC2SPacket(action));
        }
    }

    public static boolean canOpen(Minecraft client) {
        return client.player != null
            && client.player.hasPermissions(BrinAdminPanel.PERMISSION)
            && ClientPlayNetworking.canSend(BrinAdminActionC2SPacket.TYPE);
    }

    public static void open() {
        action(BrinAdminPanel.ACTION_OPEN);
    }

    private static void send(JsonArray ops, boolean done, boolean quiet) {
        JsonObject root = new JsonObject();
        root.add("ops", ops);
        root.addProperty("done", done);
        root.addProperty("quiet", quiet);
        ClientPlayNetworking.send(new BrinAdminSaveC2SPacket(root.toString()));
    }

    private static void receive(Minecraft client, BrinAdminSnapshotS2CPacket payload) {
        BrinAdminSnapshot snapshot;
        try {
            snapshot = BrinAdminSnapshot.fromJson(payload.json());
        } catch (RuntimeException exception) {
            return;
        }
        if (client.screen instanceof BrinAdminScreen hub) {
            hub.update(snapshot);
        } else if (client.screen instanceof BrinAdminListScreen page) {
            page.hub().update(snapshot);
            page.onSnapshot(snapshot);
        } else if (payload.open()) {
            Screen parent = client.screen instanceof BrinPersonalSettingsScreen ? client.screen : null;
            client.setScreen(new BrinAdminScreen(snapshot, parent));
        }
    }
}
