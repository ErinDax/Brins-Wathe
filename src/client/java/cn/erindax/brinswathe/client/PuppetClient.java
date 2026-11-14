package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import cn.erindax.brinswathe.entity.PuppetEntity;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import java.util.UUID;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import org.jetbrains.annotations.Nullable;

public final class PuppetClient {
    @Nullable
    private static UUID controlledPuppetId;
    @Nullable
    private static CameraType previousPuppetCameraType;
    private static int puppetModelRenderDepth;
    @Nullable
    private static AbstractClientPlayer puppetSkinCarrier;
    @Nullable
    private static PlayerSkin puppetSkinValue;

    private PuppetClient() {
    }

    static void tickPuppetControl(Minecraft client) {
        if (client.player == null || client.level == null) {
            stopPuppetControl(client);
            return;
        }
        PuppeteerControlComponent component = PuppeteerControlComponent.KEY.get(client.player);
        UUID targetId = component == null ? null : component.puppetEntityId;
        if (targetId == null || !(AimClient.findClientEntity(client, targetId) instanceof PlayerBodyEntity puppet)
            || !PuppeteerControlComponent.isPuppet(puppet)) {
            stopPuppetControl(client);
            return;
        }
        if (controlledPuppetId == null) {
            previousPuppetCameraType = client.options.getCameraType();
        }
        controlledPuppetId = targetId;
        client.options.setCameraType(CameraType.FIRST_PERSON);
    }

    private static void stopPuppetControl(Minecraft client) {
        if (controlledPuppetId == null) return;
        controlledPuppetId = null;
        if (previousPuppetCameraType != null) {
            client.options.setCameraType(previousPuppetCameraType);
            previousPuppetCameraType = null;
        }
    }

    public static boolean isControllingPuppet() {
        return controlledPuppetId != null;
    }

    public static boolean isControllingPuppet(UUID puppetId) {
        return puppetId.equals(controlledPuppetId);
    }

    public static void beginPuppetModelRender(AbstractClientPlayer carrier, @Nullable PlayerSkin skin) {
        puppetModelRenderDepth++;
        puppetSkinCarrier = carrier;
        puppetSkinValue = skin;
    }

    public static void endPuppetModelRender() {
        puppetModelRenderDepth = Math.max(0, puppetModelRenderDepth - 1);
        if (puppetModelRenderDepth == 0) {
            puppetSkinCarrier = null;
            puppetSkinValue = null;
        }
    }

    public static boolean isRenderingPuppetModel() {
        return puppetModelRenderDepth > 0;
    }

    @Nullable
    public static PlayerSkin puppetSkinOverride(AbstractClientPlayer player) {
        return puppetModelRenderDepth > 0 && player == puppetSkinCarrier ? puppetSkinValue : null;
    }

    @Nullable
    public static AbstractClientPlayer puppetController(PlayerBodyEntity entity) {
        UUID puppeteerId = ((PuppetEntity) entity).brin$getPuppeteer();
        if (puppeteerId != null
            && entity.level().getPlayerByUUID(puppeteerId) instanceof AbstractClientPlayer puppeteer
            && !puppeteer.isSpectator()) {
            return puppeteer;
        }
        return null;
    }

    @Nullable
    public static AbstractClientPlayer puppetRenderCarrier(PlayerBodyEntity entity) {
        UUID skinId = entity.getPlayerUuid();
        if (skinId != null && entity.level().getPlayerByUUID(skinId) instanceof AbstractClientPlayer direct
            && !direct.isSpectator()) {
            return direct;
        }
        UUID puppeteerId = ((PuppetEntity) entity).brin$getPuppeteer();
        if (puppeteerId != null
            && entity.level().getPlayerByUUID(puppeteerId) instanceof AbstractClientPlayer puppeteer
            && !puppeteer.isSpectator()) {
            return puppeteer;
        }
        AbstractClientPlayer local = Minecraft.getInstance().player;
        return local == null || local.isSpectator() ? null : local;
    }

    @Nullable
    public static PlayerSkin puppetRenderSkin(PlayerBodyEntity entity) {
        UUID skinId = entity.getPlayerUuid();
        if (skinId == null) return null;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo info = connection == null ? null : connection.getPlayerInfo(skinId);
        return info == null ? DefaultPlayerSkin.get(skinId) : info.getSkin();
    }
}
