package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.component.StalkerComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public final class StalkerTrackerHud {
    private static final int EDGE_MARGIN = 10;
    private static final int FRAME_COLOR = 0xE0B01030;
    private static final int BACKING_COLOR = 0xFF101010;
    private static final int POINTER_COLOR = 0xFFCC2233;
    @Nullable
    private static Matrix4f viewProjection;
    @Nullable
    private static Vec3 cameraPosition;

    private StalkerTrackerHud() {
    }
    public static void init() {
        WorldRenderEvents.AFTER_SETUP.register(context -> {
            viewProjection = new Matrix4f(context.projectionMatrix()).mul(context.positionMatrix());
            cameraPosition = context.camera().getPosition();
        });
        HudRenderCallback.EVENT.register(StalkerTrackerHud::render);
    }
    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.options.hideGui) return;
        if (viewProjection == null || cameraPosition == null) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (!gameWorld.isRole(player, BrinRoles.STALKER)) return;
        if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;

        StalkerComponent component = StalkerComponent.KEY.get(player);
        UUID targetId = component == null ? null : component.targetId();
        if (targetId == null) return;
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        Vec3 anchor;
        Player target = minecraft.level.getPlayerByUUID(targetId);
        if (target != null) {
            anchor = target.getPosition(partialTick).add(0.0D, target.getBbHeight() + 0.6D, 0.0D);
        } else {
            anchor = component.lastKnownTargetPos().add(0.0D, 2.4D, 0.0D);
        }
        Vector4f clip = new Vector4f(
            (float) (anchor.x - cameraPosition.x),
            (float) (anchor.y - cameraPosition.y),
            (float) (anchor.z - cameraPosition.z),
            1.0F
        );
        viewProjection.transform(clip);
        boolean behind = clip.w() <= 0.001F;
        float ndcX;
        float ndcY;
        if (behind) {

            float magnitude = Math.max(Math.abs(clip.x()), Math.abs(clip.y()));
            if (magnitude < 1.0E-4F) {
                ndcX = 0.0F;
                ndcY = -10.0F;
            } else {
                ndcX = -clip.x() / magnitude * 10.0F;
                ndcY = -clip.y() / magnitude * 10.0F;
            }
        } else {
            ndcX = clip.x() / clip.w();
            ndcY = clip.y() / clip.w();
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        float screenX = (0.5F + 0.5F * ndcX) * width;
        float screenY = (0.5F - 0.5F * ndcY) * height;
        boolean clamped = behind
            || screenX < EDGE_MARGIN || screenX > width - EDGE_MARGIN
            || screenY < EDGE_MARGIN || screenY > height - EDGE_MARGIN;
        int x = Mth.clamp(Math.round(screenX), EDGE_MARGIN, width - EDGE_MARGIN);
        int y = Mth.clamp(Math.round(screenY), EDGE_MARGIN, height - EDGE_MARGIN);
        graphics.flush();
        graphics.fill(x - 6, y - 6, x + 6, y + 6, FRAME_COLOR);
        graphics.fill(x - 5, y - 5, x + 5, y + 5, BACKING_COLOR);
        PlayerFaceRenderer.draw(graphics, resolveSkin(minecraft, targetId), x - 4, y - 4, 8);
        if (!clamped) {
            graphics.drawCenteredString(minecraft.font, "▼", x, y + 7, POINTER_COLOR);
        }
        graphics.flush();
    }
    private static PlayerSkin resolveSkin(Minecraft minecraft, UUID targetId) {
        PlayerInfo info = minecraft.getConnection() == null
            ? null
            : minecraft.getConnection().getPlayerInfo(targetId);
        return info == null ? DefaultPlayerSkin.get(targetId) : info.getSkin();
    }
}
