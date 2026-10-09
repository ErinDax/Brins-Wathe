package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.admin.BrinAdminPanel;
import cn.erindax.brinswathe.admin.BrinAdminSetting;
import cn.erindax.brinswathe.admin.BrinAdminSnapshot;
import cn.erindax.brinswathe.client.BrinAdminClient;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class BrinAdminScreen extends Screen {
    private static final int TITLE_COLOR = 0xFFD54F;
    private static final int LABEL_COLOR = 0xA0A0A0;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 4;
    private static final int ROW = BUTTON_HEIGHT + GAP;
    private static final int PAGES = 5;
    private static final int CONTENT_HEIGHT = 214;
    private static final long CONFIRM_MS = 3000L;
    private static final List<String> ACTIONS = List.of(
        BrinAdminPanel.ACTION_RELOAD,
        BrinAdminPanel.ACTION_MUSIC_STOP,
        BrinAdminPanel.ACTION_ROUNDS_CLEAR,
        BrinAdminPanel.ACTION_STAMINA_RESET
    );
    private static final Set<String> CONFIRMED = Set.of(
        BrinAdminPanel.ACTION_RELOAD,
        BrinAdminPanel.ACTION_ROUNDS_CLEAR,
        BrinAdminPanel.ACTION_STAMINA_RESET
    );
    private final Map<String, Button> actionButtons = new LinkedHashMap<>();
    private BrinAdminSnapshot snapshot;
    @Nullable
    private String armed;
    private long armedUntil;
    private int top;

    public BrinAdminScreen(BrinAdminSnapshot snapshot) {
        super(Component.translatable("screen.brinswathe.admin"));
        this.snapshot = snapshot;
    }

    public BrinAdminSnapshot snapshot() {
        return this.snapshot;
    }

    public void update(BrinAdminSnapshot snapshot) {
        this.snapshot = snapshot;
        if (this.minecraft != null && this.minecraft.screen == this) this.rebuildWidgets();
    }

    @Override
    protected void init() {
        this.actionButtons.clear();
        int center = this.width / 2;
        int left = center - BUTTON_WIDTH / 2;
        this.top = Math.max(30, (this.height - CONTENT_HEIGHT) / 2 + 10);
        this.pageButton(BrinAdminSetting.Page.RULES.id(), left, this.top,
            () -> new BrinAdminSettingsScreen(this, BrinAdminSetting.Page.RULES));
        this.pageButton(BrinAdminSetting.Page.NUMBERS.id(), left, this.top + ROW,
            () -> new BrinAdminSettingsScreen(this, BrinAdminSetting.Page.NUMBERS));
        this.pageButton("roles", left, this.top + ROW * 2, () -> new BrinAdminRolesScreen(this));
        this.pageButton("modifiers", left, this.top + ROW * 3, () -> new BrinAdminModifiersScreen(this));
        this.pageButton("players", left, this.top + ROW * 4, () -> new BrinAdminPlayersScreen(this));
        int actionsTop = this.top + ROW * PAGES + 18;
        int half = (BUTTON_WIDTH - GAP) / 2;
        for (int index = 0; index < ACTIONS.size(); index++) {
            String action = ACTIONS.get(index);
            Button button = Button.builder(this.actionLabel(action), pressed -> this.press(action))
                .bounds(left + (index % 2) * (half + GAP), actionsTop + (index / 2) * ROW, half, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("gui.brinswathe.admin.action." + action + ".tip")))
                .build();
            this.actionButtons.put(action, this.addRenderableWidget(button));
        }
        int doneTop = actionsTop + ((ACTIONS.size() + 1) / 2) * ROW + 8;
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
            .bounds(left, doneTop, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.refreshActionLabels();
        super.render(graphics, mouseX, mouseY, partialTick);
        int center = this.width / 2;
        graphics.drawCenteredString(this.font, this.title, center, this.top - 20, TITLE_COLOR);
        graphics.drawCenteredString(
            this.font,
            Component.translatable("gui.brinswathe.admin.actions"),
            center,
            this.top + ROW * PAGES + 5,
            LABEL_COLOR
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void pageButton(String page, int left, int y, Supplier<Screen> screen) {
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.brinswathe.admin.page." + page),
                button -> this.minecraft.setScreen(screen.get()))
            .bounds(left, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .tooltip(Tooltip.create(Component.translatable("gui.brinswathe.admin.page." + page + ".tip")))
            .build());
    }

    private void press(String action) {
        long now = Util.getMillis();
        if (CONFIRMED.contains(action) && !(action.equals(this.armed) && now < this.armedUntil)) {
            this.armed = action;
            this.armedUntil = now + CONFIRM_MS;
            return;
        }
        this.armed = null;
        BrinAdminClient.action(action);
    }

    private void refreshActionLabels() {
        boolean waiting = this.armed != null && Util.getMillis() < this.armedUntil;
        for (Map.Entry<String, Button> entry : this.actionButtons.entrySet()) {
            entry.getValue().setMessage(waiting && entry.getKey().equals(this.armed)
                ? Component.translatable("gui.brinswathe.admin.confirm").withStyle(ChatFormatting.RED)
                : this.actionLabel(entry.getKey()));
        }
    }

    private Component actionLabel(String action) {
        return Component.translatable("gui.brinswathe.admin.action." + action);
    }
}
