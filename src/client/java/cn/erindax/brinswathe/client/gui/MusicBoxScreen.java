package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.client.musicbox.MusicBoxClient;
import cn.erindax.brinswathe.musicbox.MusicFormats;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class MusicBoxScreen extends Screen {
    private static final int TEXT_WIDTH = 320;
    private static final int LINE_HEIGHT = 11;
    @Nullable
    private final Screen parent;
    private Button pickButton;
    private Button previewButton;
    private Button deleteButton;

    public MusicBoxScreen(@Nullable Screen parent) {
        super(Component.translatable("screen.brinswathe.music_box"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int top = this.height / 2 + 44;
        this.pickButton = this.addRenderableWidget(Button.builder(
                Component.translatable("gui.brinswathe.music_box.pick"),
                button -> MusicBoxClient.pickFile())
            .bounds(center - 154, top, 100, 20)
            .build());
        this.previewButton = this.addRenderableWidget(Button.builder(
                Component.translatable("gui.brinswathe.music_box.preview"),
                button -> MusicBoxClient.togglePreview())
            .bounds(center - 50, top, 100, 20)
            .build());
        this.deleteButton = this.addRenderableWidget(Button.builder(
                Component.translatable("gui.brinswathe.music_box.delete"),
                button -> MusicBoxClient.delete())
            .bounds(center + 54, top, 100, 20)
            .build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
            .bounds(center - 100, top + 26, 200, 20)
            .build());
        this.refreshButtons();
    }

    @Override
    public void tick() {
        super.tick();
        this.refreshButtons();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int center = this.width / 2;
        int width = Math.max(120, Math.min(TEXT_WIDTH, this.width - 20));
        int y = this.height / 2 - 84;
        graphics.drawCenteredString(this.font, this.title, center, y, 0xFFD54F);
        y += LINE_HEIGHT + 8;
        for (Component line : this.lines()) {
            for (FormattedCharSequence part : this.font.split(line, width)) {
                graphics.drawCenteredString(this.font, part, center, y, 0xFFFFFF);
                y += LINE_HEIGHT;
            }
            y += 3;
        }
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        for (Path path : paths) {
            Path fileName = path.getFileName();
            String name = fileName == null ? "" : fileName.toString().toLowerCase(Locale.ROOT);
            if (name.endsWith(".mp3") || name.endsWith(".ogg")) {
                MusicBoxClient.acceptDroppedFile(path);
                return;
            }
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void removed() {
        MusicBoxClient.stopPreview();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void refreshButtons() {
        if (this.pickButton == null) return;
        this.pickButton.active = MusicBoxClient.canPick();
        boolean previewing = MusicBoxClient.isPreviewing();
        this.previewButton.setMessage(Component.translatable(
            previewing ? "gui.brinswathe.music_box.stop_preview" : "gui.brinswathe.music_box.preview"
        ));
        this.previewButton.active = previewing || MusicBoxClient.canPreview();
        this.deleteButton.active = MusicBoxClient.canDelete();
    }

    private List<Component> lines() {
        List<Component> lines = new ArrayList<>();
        if (!MusicBoxClient.statusKnown()) {
            lines.add(Component.translatable("gui.brinswathe.music_box.loading").withStyle(ChatFormatting.GRAY));
        } else if (!MusicBoxClient.enabled()) {
            lines.add(Component.translatable("gui.brinswathe.music_box.disabled").withStyle(ChatFormatting.RED));
        } else if (MusicBoxClient.hasTrack()) {
            lines.add(Component.translatable(
                "gui.brinswathe.music_box.current",
                Component.literal(MusicBoxClient.trackName()).withStyle(ChatFormatting.YELLOW),
                MusicFormats.describeSize(MusicBoxClient.trackSize())
            ));
        } else {
            lines.add(Component.translatable("gui.brinswathe.music_box.none").withStyle(ChatFormatting.GRAY));
        }
        MusicBoxClient.Track playing = MusicBoxClient.nowPlaying();
        if (playing != null) {
            Component nowPlaying = Component.translatable(
                "gui.brinswathe.music_box.now_playing",
                playing.ownerName(),
                playing.trackName()
            ).withStyle(ChatFormatting.AQUA);
            int loading = MusicBoxClient.broadcastLoadPercent();
            lines.add(loading >= 0
                ? Component.empty().append(nowPlaying).append(Component.literal(" " + loading + "%").withStyle(ChatFormatting.GRAY))
                : nowPlaying);
        }
        Component state = this.stateLine();
        if (state != null) lines.add(state);
        if (MusicBoxClient.statusKnown() && MusicBoxClient.enabled()) {
            lines.add(Component.translatable(
                "gui.brinswathe.music_box.hint",
                MusicFormats.describeSize(MusicBoxClient.maxBytes())
            ).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("gui.brinswathe.music_box.mvp_hint").withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("gui.brinswathe.music_box.volume_hint").withStyle(ChatFormatting.DARK_GRAY));
        }
        return lines;
    }

    private Component stateLine() {
        if (MusicBoxClient.isPicking()) {
            return Component.translatable("gui.brinswathe.music_box.picking").withStyle(ChatFormatting.YELLOW);
        }
        if (MusicBoxClient.isProcessing()) {
            return Component.translatable("gui.brinswathe.music_box.processing").withStyle(ChatFormatting.YELLOW);
        }
        if (MusicBoxClient.isUploading()) {
            return Component.translatable("gui.brinswathe.music_box.uploading", MusicBoxClient.uploadPercent())
                .withStyle(ChatFormatting.YELLOW);
        }
        if (MusicBoxClient.isPreviewLoading()) {
            return Component.translatable("gui.brinswathe.music_box.preview_loading", MusicBoxClient.previewLoadPercent())
                .withStyle(ChatFormatting.YELLOW);
        }
        return MusicBoxClient.notice();
    }
}
