package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.network.BrinInstinctSnapshotS2CPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameConstants;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BrinInstinctClient {
    private static final double BOX_HALF = 0.32;
    private static final double BOX_HEIGHT = 1.9;
    private static final double VISIBILITY_RANGE_SQUARED = 4194304.0;
    private static List<BrinInstinctSnapshotS2CPacket.Entry> snapshots = List.of();

    private BrinInstinctClient() {
    }

    public static void init() {
        WorldRenderEvents.LAST.register(BrinInstinctClient::render);
    }

    public static void apply(List<BrinInstinctSnapshotS2CPacket.Entry> entries) {
        snapshots = entries == null ? List.of() : List.copyOf(entries);
    }

    public static void clear() {
        snapshots = List.of();
    }

    public static List<BrinInstinctSnapshotS2CPacket.Entry> snapshots() {
        return snapshots;
    }

    public static BrinInstinctSnapshotS2CPacket.Entry lookedAt(
        Vec3 eye,
        Vec3 end,
        Player viewer
    ) {
        BrinInstinctSnapshotS2CPacket.Entry best = null;
        double bestDist = Double.MAX_VALUE;
        for (BrinInstinctSnapshotS2CPacket.Entry snapshot : snapshots) {
            if (shouldSkip(Minecraft.getInstance(), snapshot)) continue;
            if (highlightColor(snapshot) == -1) continue;
            AABB box = box(snapshot).inflate(0.15);
            Optional<Vec3> hit = box.clip(eye, end);
            if (hit.isEmpty()) continue;
            double dist = eye.distanceToSqr(hit.get());
            if (dist < bestDist) {
                bestDist = dist;
                best = snapshot;
            }
        }
        return best;
    }

    private static void render(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.getCameraEntity() == null) return;
        if (!WatheClient.isInstinctEnabled()) return;
        var pose = context.matrixStack();
        MultiBufferSource consumers = context.consumers();
        if (pose == null || consumers == null) return;

        Vec3 camera = context.camera().getPosition();
        var buffer = consumers.getBuffer(RenderType.lines());
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        RenderSystem.disableDepthTest();
        for (BrinInstinctSnapshotS2CPacket.Entry snapshot : snapshots) {
            if (shouldSkip(client, snapshot)) continue;
            int color = highlightColor(snapshot);
            if (color == -1) continue;
            float red = (color >> 16 & 0xFF) / 255.0F;
            float green = (color >> 8 & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            AABB box = box(snapshot);
            LevelRenderer.renderLineBox(pose, buffer, box, red, green, blue, 1.0F);
        }
        pose.popPose();
        RenderSystem.enableDepthTest();
    }

    private static boolean shouldSkip(Minecraft client, BrinInstinctSnapshotS2CPacket.Entry snapshot) {
        if (snapshot.spectatorOrCreative()) return true;
        if (snapshot.uuid().equals(client.player.getUUID())) return true;
        Entity camera = client.getCameraEntity();
        if (camera == null || camera.distanceToSqr(snapshot.position()) > VISIBILITY_RANGE_SQUARED) return true;
        ClientPacketListener connection = client.getConnection();
        PlayerInfo info = connection == null ? null : connection.getPlayerInfo(snapshot.uuid());
        if (info == null || info.getGameMode() == GameType.SPECTATOR) return true;
        Player loaded = client.level.getPlayerByUUID(snapshot.uuid());
        return loaded != null && loaded.isAlive();
    }

    private static AABB box(BrinInstinctSnapshotS2CPacket.Entry snapshot) {
        return new AABB(
            snapshot.x() - BOX_HALF,
            snapshot.y(),
            snapshot.z() - BOX_HALF,
            snapshot.x() + BOX_HALF,
            snapshot.y() + BOX_HEIGHT,
            snapshot.z() + BOX_HALF
        );
    }

    private static int highlightColor(BrinInstinctSnapshotS2CPacket.Entry snapshot) {
        if (snapshot.canUseKillerFeatures()) return Mth.hsvToRgb(0.0F, 1.0F, 0.6F);
        if (!snapshot.innocent()) return -1;
        if (snapshot.mood() < GameConstants.DEPRESSIVE_MOOD_THRESHOLD) return 0x171DC6;
        if (snapshot.mood() < GameConstants.MID_MOOD_THRESHOLD) return 0x1FAFAF;
        return 0x4EDD35;
    }
}
