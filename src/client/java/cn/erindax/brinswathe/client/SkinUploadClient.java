package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.KnifeSkins;
import cn.erindax.brinswathe.network.SkinSoundS2CPacket;
import cn.erindax.brinswathe.network.SkinUploadC2SPacket;
import cn.erindax.brinswathe.network.SkinUploadPromptS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class SkinUploadClient {
    private static final int MAX_PACKET_BYTES = 900_000;

    private SkinUploadClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SkinUploadPromptS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> beginUpload(payload.kind(), payload.name(), payload.tooltipName())));
        ClientPlayNetworking.registerGlobalReceiver(SkinSoundS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> KnifeSkinClient.playWorldSound(
                payload.kind(),
                payload.skin(),
                payload.x(),
                payload.y(),
                payload.z(),
                payload.volume(),
                payload.pitch()
            )));
    }

    private static void beginUpload(String type, String name, String tooltip) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.getConnection() == null) return;
        client.mouseHandler.releaseMouse();
        Thread thread = new Thread(() -> pickAndSend(type, name, tooltip), "brin-skin-upload");
        thread.setDaemon(true);
        thread.start();
    }

    private static void pickAndSend(String type, String name, String tooltip) {
        try {
            Thread.sleep(300);
            tell("message.brinswathe.skin.pick_texture");
            byte[] texture = FilePicker.pickBytes("png");
            if (texture == null) {
                tell("message.brinswathe.skin.cancelled");
                return;
            }
            if (!KnifeSkins.isPng(texture) || texture.length > KnifeSkins.MAX_TEXTURE_BYTES) {
                tell("message.brinswathe.skin.upload_failed");
                return;
            }
            tell("message.brinswathe.skin.pick_sound");
            byte[] sound = FilePicker.pickBytes("ogg");
            if (sound != null && (!KnifeSkins.isOgg(sound) || sound.length > KnifeSkins.MAX_SOUND_BYTES)) {
                tell("message.brinswathe.skin.upload_failed");
                return;
            }
            if (sound == null) sound = new byte[0];
            if (texture.length + sound.length > MAX_PACKET_BYTES) {
                tell("message.brinswathe.skin.upload_failed");
                return;
            }
            byte[] soundBytes = sound;
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().getConnection() == null) return;
                ClientPlayNetworking.send(new SkinUploadC2SPacket(type, name, tooltip, texture, soundBytes));
            });
        } catch (Exception ignored) {
            tell("message.brinswathe.skin.cancelled");
        }
    }

    private static void tell(String key) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.translatable(key), false);
            }
        });
    }
}
