package cn.erindax.brinswathe.client;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Window;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class BrinFilePicker {
    private BrinFilePicker() {
    }

    @Nullable
    public static byte[] pickBytes(String extension) throws Exception {
        Path path = pick(extension.toUpperCase(Locale.ROOT), extension);
        return path == null ? null : Files.readAllBytes(path);
    }

    @Nullable
    public static Path pick(String title, String... extensions) throws Exception {
        System.setProperty("java.awt.headless", "false");
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            return pickWindows(title, extensions);
        }
        return pickSwing(title, extensions);
    }

    @Nullable
    private static Path pickWindows(String title, String... extensions) throws Exception {
        Path reply = Files.createTempFile("brin-pick-", ".txt");
        Files.deleteIfExists(reply);
        String replyPath = reply.toAbsolutePath().toString().replace("'", "''");
        String patterns = Arrays.stream(extensions)
            .map(extension -> "*." + extension)
            .collect(Collectors.joining(";"));
        String filter = (title + " (" + patterns + ")|" + patterns).replace("'", "''");
        String script = String.join(" ",
            "Add-Type -AssemblyName System.Windows.Forms;",
            "$form = New-Object System.Windows.Forms.Form;",
            "$form.TopMost = $true;",
            "$form.ShowInTaskbar = $false;",
            "$d = New-Object System.Windows.Forms.OpenFileDialog;",
            "$d.Filter = '" + filter + "';",
            "$d.Title = '" + title.replace("'", "''") + "';",
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
            return pickSwing(title, extensions);
        }
        if (!Files.isRegularFile(reply) || Files.size(reply) == 0) {
            Files.deleteIfExists(reply);
            return null;
        }
        String selected = Files.readString(reply, StandardCharsets.UTF_8).trim();
        Files.deleteIfExists(reply);
        Path path = Path.of(selected);
        return Files.isRegularFile(path) ? path : null;
    }

    @Nullable
    private static Path pickSwing(String title, String... extensions) throws Exception {
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
                chooser.setDialogTitle(title);
                chooser.setFileFilter(new FileNameExtensionFilter(title, extensions));
                int result = chooser.showOpenDialog(owner);
                if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
                    chosen.set(chooser.getSelectedFile().toPath());
                }
            } finally {
                owner.dispose();
            }
        });
        Path path = chosen.get();
        return path != null && Files.isRegularFile(path) ? path : null;
    }
}
