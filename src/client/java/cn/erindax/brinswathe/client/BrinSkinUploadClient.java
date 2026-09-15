package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinKnifeSkins;
import cn.erindax.brinswathe.network.BrinSkinSoundS2CPacket;
import cn.erindax.brinswathe.network.BrinSkinUploadC2SPacket;
import cn.erindax.brinswathe.network.BrinSkinUploadPromptS2CPacket;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Window;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class BrinSkinUploadClient {
    private static final int MAX_PACKET_BYTES = 900_000;

    private BrinSkinUploadClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(BrinSkinUploadPromptS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> beginUpload(payload.kind(), payload.name(), payload.tooltipName())));
        ClientPlayNetworking.registerGlobalReceiver(BrinSkinSoundS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> BrinKnifeSkinClient.playWorldSound(
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
            System.setProperty("java.awt.headless", "false");
            tell("message.brinswathe.skin.pick_texture");
            byte[] texture = pickFile("png");
            if (texture == null) {
                tell("message.brinswathe.skin.cancelled");
                return;
            }
            if (!BrinKnifeSkins.isPng(texture) || texture.length > BrinKnifeSkins.MAX_TEXTURE_BYTES) {
                tell("message.brinswathe.skin.upload_failed");
                return;
            }
            tell("message.brinswathe.skin.pick_sound");
            byte[] sound = pickFile("ogg");
            if (sound != null && (!BrinKnifeSkins.isOgg(sound) || sound.length > BrinKnifeSkins.MAX_SOUND_BYTES)) {
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
                ClientPlayNetworking.send(new BrinSkinUploadC2SPacket(type, name, tooltip, texture, soundBytes));
            });
        } catch (Exception ignored) {
            tell("message.brinswathe.skin.cancelled");
        }
    }

    private static byte[] pickFile(String extension) throws Exception {
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            return pickFileWindows(extension);
        }
        return pickFileSwing(extension);
    }

    private static byte[] pickFileWindows(String extension) throws Exception {
        Path reply = Files.createTempFile("brin-skin-", ".txt");
        Files.deleteIfExists(reply);
        String replyPath = reply.toAbsolutePath().toString().replace("'", "''");
        String filter = extension.toUpperCase(Locale.ROOT) + " (*." + extension + ")|*." + extension;
        String script = String.join(" ",
            "Add-Type -AssemblyName System.Windows.Forms;",
            "$form = New-Object System.Windows.Forms.Form;",
            "$form.TopMost = $true;",
            "$form.ShowInTaskbar = $false;",
            "$d = New-Object System.Windows.Forms.OpenFileDialog;",
            "$d.Filter = '" + filter + "';",
            "$d.Title = '" + extension.toUpperCase(Locale.ROOT) + "';",
            "$result = $d.ShowDialog($form);",
            "$form.Dispose();",
            "if ($result -eq [System.Windows.Forms.DialogResult]::OK) {",
            "[System.IO.File]::WriteAllText('" + replyPath + "', $d.FileName, [System.Text.UTF8Encoding]::new($false))",
            "}"
        );
        Process process = new ProcessBuilder(
            "powershell.exe",
            "-NoProfile",
            "-STA",
            "-WindowStyle",
            "Hidden",
            "-ExecutionPolicy",
            "Bypass",
            "-Command",
            script
        ).redirectErrorStream(true).start();
        process.getInputStream().readAllBytes();
        int code = process.waitFor();
        if (code != 0) {
            Files.deleteIfExists(reply);
            return pickFileSwing(extension);
        }
        if (!Files.isRegularFile(reply) || Files.size(reply) == 0) {
            Files.deleteIfExists(reply);
            return null;
        }
        String selected = Files.readString(reply, StandardCharsets.UTF_8).trim();
        Files.deleteIfExists(reply);
        Path path = Path.of(selected);
        if (!Files.isRegularFile(path)) return null;
        return Files.readAllBytes(path);
    }

    private static byte[] pickFileSwing(String extension) throws Exception {
        AtomicReference<Path> chosen = new AtomicReference<>();
        EventQueue.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            Frame owner = new Frame();
            owner.setAlwaysOnTop(true);
            owner.setUndecorated(true);
            owner.setType(Window.Type.UTILITY);
            owner.setSize(1, 1);
            owner.setLocationRelativeTo(null);
            owner.setVisible(true);
            owner.toFront();
            try {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle(extension.toUpperCase(Locale.ROOT));
                chooser.setFileFilter(new FileNameExtensionFilter(extension.toUpperCase(Locale.ROOT), extension));
                int result = chooser.showOpenDialog(owner);
                if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
                    chosen.set(chooser.getSelectedFile().toPath());
                }
            } finally {
                owner.dispose();
            }
        });
        Path path = chosen.get();
        if (path == null || !Files.isRegularFile(path)) return null;
        return Files.readAllBytes(path);
    }

    private static void tell(String key) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.translatable(key), false);
            }
        });
    }
}
