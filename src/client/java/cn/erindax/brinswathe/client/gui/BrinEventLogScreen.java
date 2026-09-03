package cn.erindax.brinswathe.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

public class BrinEventLogScreen extends Screen {
    private static final int LINE_HEIGHT = 12;
    private final List<Component> lines;
    private double scroll;

    public BrinEventLogScreen(Component rawLog) {
        super(Component.translatable("screen.brinswathe.event_log"));
        this.lines = splitLines(rawLog);
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
            .bounds((this.width - buttonWidth) / 2, this.height - 32, buttonWidth, 20)
            .build());
        this.scroll = 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFD54F);
        int top = 32;
        int bottom = this.height - 40;
        int maxScroll = Math.max(0, this.lines.size() * LINE_HEIGHT - (bottom - top));
        this.scroll = Mth.clamp(this.scroll, 0, maxScroll);

        graphics.enableScissor(20, top, this.width - 20, bottom);
        int y = top - (int) this.scroll;
        for (Component line : this.lines) {
            if (y + LINE_HEIGHT >= top && y <= bottom) {
                graphics.drawString(this.font, line, 28, y, 0xFFFFFF, false);
            }
            y += LINE_HEIGHT;
        }
        graphics.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll -= scrollY * LINE_HEIGHT * 2;
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static boolean isEventLog(Component message) {
        return message != null && message.getString().contains("游戏事件日志");
    }

    private static List<Component> splitLines(Component message) {
        List<Component> lines = new ArrayList<>();
        MutableComponent[] current = {Component.empty()};
        message.visit((style, text) -> {
            String[] parts = text.split("\n", -1);
            for (int index = 0; index < parts.length; index++) {
                if (index > 0) {
                    lines.add(current[0]);
                    current[0] = Component.empty();
                }
                if (!parts[index].isEmpty()) {
                    current[0] = current[0].append(Component.literal(parts[index]).withStyle(style));
                }
            }
            return Optional.empty();
        }, Style.EMPTY);
        if (!current[0].getString().isEmpty() || lines.isEmpty()) {
            lines.add(current[0]);
        }
        return lines;
    }
}
