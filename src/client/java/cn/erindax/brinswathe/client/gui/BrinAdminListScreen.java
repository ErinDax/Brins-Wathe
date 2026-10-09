package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.admin.BrinAdminSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class BrinAdminListScreen extends Screen {
    protected static final int BUTTON_HEIGHT = 20;
    protected static final int GAP = 4;
    protected static final int TEXT_COLOR = 0xFFFFFF;
    protected static final int CHANGED_COLOR = 0xFFFF55;
    private static final int ROW_HEIGHT = 24;
    private static final int TITLE_COLOR = 0xFFD54F;
    private static final int SECTION_COLOR = 0xE6C35C;
    private static final int FOOTER_HEIGHT = 36;
    private static final int LIST_TOP = 32;
    private static final int SEARCH_LIST_TOP = 50;
    private static final int SEARCH_WIDTH = 200;
    private static final int TOOLTIP_WIDTH = 240;
    private static final long CONFIRM_MS = 3000L;
    protected final BrinAdminScreen hub;
    private final Screen parent;
    private final boolean searchable;
    private String filter = "";
    @Nullable
    private RowList list;
    @Nullable
    private Button saveButton;
    @Nullable
    private Button cancelButton;
    private long discardUntil;
    @Nullable
    private String armed;
    private long armedUntil;

    protected BrinAdminListScreen(Component title, BrinAdminScreen hub, Screen parent, boolean searchable) {
        super(title);
        this.hub = hub;
        this.parent = parent;
        this.searchable = searchable;
    }

    public BrinAdminScreen hub() {
        return this.hub;
    }

    protected abstract List<Row> rows(String filter);

    protected abstract int rowWidth();

    protected int changes() {
        return 0;
    }

    protected boolean invalid() {
        return false;
    }

    protected void apply() {
    }

    protected boolean editable() {
        return true;
    }

    protected Component confirmLabel() {
        return Component.translatable("gui.brinswathe.admin.save");
    }

    public void onSnapshot(BrinAdminSnapshot snapshot) {
    }

    @Override
    protected void init() {
        double scroll = this.list == null ? 0.0 : this.list.getScrollAmount();
        int listTop = LIST_TOP;
        EditBox search = null;
        if (this.searchable) {
            search = new EditBox(
                this.font,
                (this.width - SEARCH_WIDTH) / 2,
                LIST_TOP - 6,
                SEARCH_WIDTH,
                18,
                Component.translatable("gui.brinswathe.admin.search")
            );
            search.setHint(Component.translatable("gui.brinswathe.admin.search").withStyle(ChatFormatting.DARK_GRAY));
            search.setValue(this.filter);
            search.setResponder(value -> {
                this.filter = value;
                this.refreshRows();
                if (this.list != null) this.list.setClampedScrollAmount(0.0);
            });
            this.addRenderableWidget(search);
            listTop = SEARCH_LIST_TOP;
        }
        this.list = this.addRenderableWidget(new RowList(listTop));
        this.refreshRows();
        this.list.setClampedScrollAmount(scroll);
        int y = this.height - FOOTER_HEIGHT / 2 - BUTTON_HEIGHT / 2;
        int center = this.width / 2;
        if (this.editable()) {
            this.saveButton = this.addRenderableWidget(Button.builder(Component.empty(), button -> this.confirm())
                .bounds(center - 100 - GAP / 2, y, 100, BUTTON_HEIGHT)
                .build());
            this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose())
                .bounds(center + GAP / 2, y, 100, BUTTON_HEIGHT)
                .build());
            this.refreshFooter();
        } else {
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose())
                .bounds(center - 100, y, 200, BUTTON_HEIGHT)
                .build());
        }
        if (search != null) this.setInitialFocus(search);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.cancelButton != null) {
            this.cancelButton.setMessage(Util.getMillis() < this.discardUntil
                ? Component.translatable("gui.brinswathe.admin.discard").withStyle(ChatFormatting.YELLOW)
                : CommonComponents.GUI_CANCEL);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, TITLE_COLOR);
    }

    @Override
    public void onClose() {
        long now = Util.getMillis();
        if (this.changes() > 0 && now >= this.discardUntil) {
            this.discardUntil = now + CONFIRM_MS;
            return;
        }
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected final void refreshRows() {
        if (this.list != null) this.list.replace(this.rows(this.filter.trim().toLowerCase(Locale.ROOT)));
    }

    protected final void refreshFooter() {
        if (this.saveButton == null) return;
        int changes = this.changes();
        this.saveButton.active = changes > 0 && !this.invalid();
        this.saveButton.setMessage(changes > 0
            ? Component.translatable("gui.brinswathe.admin.count", this.confirmLabel(), changes)
            : this.confirmLabel());
    }

    protected final List<FormattedCharSequence> tooltip(Component... lines) {
        List<FormattedCharSequence> result = new ArrayList<>();
        for (Component line : lines) result.addAll(this.font.split(line, TOOLTIP_WIDTH));
        return result;
    }

    protected final void drawLabel(GuiGraphics graphics, Component label, int left, int top, int height, int color, int maxWidth) {
        List<FormattedCharSequence> lines = this.font.split(label, Math.max(1, maxWidth - 4));
        if (lines.isEmpty()) return;
        graphics.drawString(this.font, lines.get(0), left + 4, top + (height - 8) / 2, color);
    }

    protected final boolean confirmed(String key) {
        long now = Util.getMillis();
        if (key.equals(this.armed) && now < this.armedUntil) {
            this.armed = null;
            return true;
        }
        this.armed = key;
        this.armedUntil = now + CONFIRM_MS;
        return false;
    }

    protected final Component armedLabel(String key, Component label) {
        return key.equals(this.armed) && Util.getMillis() < this.armedUntil
            ? Component.translatable("gui.brinswathe.admin.confirm").withStyle(ChatFormatting.RED)
            : label;
    }

    protected static Component marked(Component label, boolean changed) {
        return changed ? Component.literal("* ").withStyle(ChatFormatting.YELLOW).append(label) : label;
    }

    protected static boolean matches(String filter, String id, Component name) {
        return filter.isEmpty()
            || id.toLowerCase(Locale.ROOT).contains(filter)
            || name.getString().toLowerCase(Locale.ROOT).contains(filter);
    }

    private void confirm() {
        if (this.changes() == 0 || this.invalid()) return;
        this.apply();
        this.minecraft.setScreen(this.parent);
    }

    protected abstract static class Row extends ContainerObjectSelectionList.Entry<Row> {
        @Nullable
        protected List<FormattedCharSequence> tooltip;
    }

    protected final class HeaderRow extends Row {
        private final Component text;

        HeaderRow(Component text) {
            this.text = text;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.drawString(BrinAdminListScreen.this.font, this.text, left + 2, top + height - 10, SECTION_COLOR);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }

    private final class RowList extends ContainerObjectSelectionList<Row> {
        RowList(int top) {
            super(
                BrinAdminListScreen.this.minecraft,
                BrinAdminListScreen.this.width,
                BrinAdminListScreen.this.height - top - FOOTER_HEIGHT,
                top,
                ROW_HEIGHT
            );
        }

        void replace(List<Row> rows) {
            this.replaceEntries(rows);
            this.setClampedScrollAmount(this.getScrollAmount());
        }

        @Override
        public int getRowWidth() {
            return Math.min(BrinAdminListScreen.this.rowWidth(), BrinAdminListScreen.this.width - 40);
        }

        @Override
        protected int getScrollbarPosition() {
            return (BrinAdminListScreen.this.width + this.getRowWidth()) / 2 + 6;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            Row hovered = this.getHovered();
            if (hovered == null || hovered.tooltip == null) return;
            for (GuiEventListener child : hovered.children()) {
                if (child instanceof AbstractWidget widget && widget.getTooltip() != null && widget.isMouseOver(mouseX, mouseY)) {
                    return;
                }
            }
            BrinAdminListScreen.this.setTooltipForNextRenderPass(hovered.tooltip);
        }
    }
}
