package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.component.IllusionistComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import java.util.UUID;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class IllusionistClient {
    @Nullable
    private static UUID controlledCloneId;
    @Nullable
    private static CameraType previousCameraType;
    @Nullable
    private static int illusionModelRenderDepth;

    private IllusionistClient() {
    }

    static void tickIllusionistControl(Minecraft client) {
        if (client.player == null || client.level == null) {
            stopClientControl(client);
            return;
        }
        IllusionistComponent component = IllusionistComponent.KEY.get(client.player);
        UUID targetId = component == null ? null : component.controlledCloneId;
        Entity target = AimClient.findClientEntity(client, targetId);
        if (!(target instanceof PlayerBodyEntity clone) || !IllusionistComponent.isClone(clone)) {
            stopClientControl(client);
            return;
        }

        if (controlledCloneId == null) {
            previousCameraType = client.options.getCameraType();
        }
        controlledCloneId = targetId;
        client.options.setCameraType(CameraType.FIRST_PERSON);
        if (client.getCameraEntity() == clone) {
            client.setCameraEntity(client.player);
        }
    }

    private static void stopClientControl(Minecraft client) {
        if (controlledCloneId == null) return;
        controlledCloneId = null;
        if (client.player != null && client.getCameraEntity() instanceof PlayerBodyEntity body
            && IllusionistComponent.isClone(body)) {
            client.setCameraEntity(client.player);
        }
        if (previousCameraType != null) {
            client.options.setCameraType(previousCameraType);
            previousCameraType = null;
        }
    }

    public static boolean isControllingClone() {
        return controlledCloneId != null;
    }

    public static boolean isControllingClone(UUID cloneId) {
        return cloneId.equals(controlledCloneId);
    }

    public static void beginIllusionModelRender() {
        illusionModelRenderDepth++;
    }

    public static void endIllusionModelRender() {
        illusionModelRenderDepth = Math.max(0, illusionModelRenderDepth - 1);
    }

    public static boolean isRenderingIllusionModel() {
        return illusionModelRenderDepth > 0;
    }
}
