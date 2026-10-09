package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.admin.BrinAdminPanel;
import cn.erindax.brinswathe.admin.BrinAdminSetting;
import cn.erindax.brinswathe.client.BrinAdminClient;
import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public final class BrinAdminSettingsScreen extends BrinAdminListScreen {
    private static final int ROW_WIDTH = 360;
    private static final int TOGGLE_WIDTH = 60;
    private static final int NUMBER_WIDTH = 70;
    private static final int ITEMS_WIDTH = 200;
    private static final int ITEMS_MAX_LENGTH = 1024;
    private static final int NUMBER_MAX_LENGTH = 16;
    private static final int VALID_COLOR = 0xE0E0E0;
    private static final int INVALID_COLOR = 0xFF5555;
    private final Map<BrinAdminSetting, String> baseline = new EnumMap<>(BrinAdminSetting.class);
    private final Map<BrinAdminSetting, String> values = new EnumMap<>(BrinAdminSetting.class);

    public BrinAdminSettingsScreen(BrinAdminScreen hub, BrinAdminSetting.Page page) {
        super(Component.translatable("gui.brinswathe.admin.page." + page.id()), hub, hub, false);
        for (BrinAdminSetting setting : BrinAdminSetting.values()) {
            if (setting.page() != page) continue;
            String value = hub.snapshot().settings().getOrDefault(setting.id(), "");
            this.baseline.put(setting, value);
            this.values.put(setting, value);
        }
    }

    @Override
    protected List<Row> rows(String filter) {
        List<Row> rows = new ArrayList<>();
        String section = null;
        for (BrinAdminSetting setting : this.values.keySet()) {
            if (!setting.section().equals(section)) {
                section = setting.section();
                rows.add(new HeaderRow(Component.translatable("gui.brinswathe.admin.section." + section)));
            }
            rows.add(setting.kind().isToggle() ? new ToggleRow(setting) : new TextRow(setting));
        }
        return rows;
    }

    @Override
    protected int changes() {
        int count = 0;
        for (BrinAdminSetting setting : this.values.keySet()) {
            if (this.changed(setting)) count++;
        }
        return count;
    }

    @Override
    protected boolean invalid() {
        for (BrinAdminSetting setting : this.values.keySet()) {
            if (this.invalid(setting)) return true;
        }
        return false;
    }

    @Override
    protected void apply() {
        List<JsonArray> ops = new ArrayList<>();
        for (BrinAdminSetting setting : this.values.keySet()) {
            if (!this.changed(setting)) continue;
            String value = setting.normalize(this.values.get(setting));
            if (value != null) ops.add(BrinAdminClient.op(BrinAdminPanel.OP_SETTING, setting.id(), value));
        }
        BrinAdminClient.save(ops);
    }

    @Override
    protected int rowWidth() {
        return ROW_WIDTH;
    }

    private boolean changed(BrinAdminSetting setting) {
        String value = this.values.get(setting);
        String original = this.baseline.get(setting);
        if (value.equals(original)) return false;
        String normalized = setting.normalize(value);
        return normalized == null || !normalized.equals(setting.normalize(original));
    }

    private boolean invalid(BrinAdminSetting setting) {
        return this.changed(setting) && setting.normalize(this.values.get(setting)) == null;
    }

    private Component label(BrinAdminSetting setting) {
        return Component.translatable("gui.brinswathe.admin.setting." + setting.id());
    }

    private List<FormattedCharSequence> settingTooltip(BrinAdminSetting setting) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gui.brinswathe.admin.setting." + setting.id() + ".tip"));
        BrinAdminSetting.Kind kind = setting.kind();
        if (kind == BrinAdminSetting.Kind.INT || kind == BrinAdminSetting.Kind.FLOAT) {
            lines.add(Component.translatable(
                "gui.brinswathe.admin.range",
                BrinAdminSetting.formatNumber(setting.min()),
                BrinAdminSetting.formatNumber(setting.max())
            ).withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.translatable("gui.brinswathe.admin.command", setting.usage()).withStyle(ChatFormatting.GRAY));
        return this.tooltip(lines.toArray(Component[]::new));
    }

    private final class ToggleRow extends Row {
        private final BrinAdminSetting setting;
        private final Component label;
        private final CycleButton<Boolean> button;

        ToggleRow(BrinAdminSetting setting) {
            this.setting = setting;
            this.label = label(setting);
            this.button = CycleButton.onOffBuilder(Boolean.parseBoolean(values.get(setting)))
                .displayOnlyValue()
                .create(0, 0, TOGGLE_WIDTH, BUTTON_HEIGHT, this.label, (button, value) -> {
                    values.put(setting, Boolean.toString(value));
                    refreshFooter();
                });
            this.tooltip = settingTooltip(setting);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.button.setPosition(left + width - TOGGLE_WIDTH, top);
            this.button.render(graphics, mouseX, mouseY, partialTick);
            int color = changed(this.setting) ? CHANGED_COLOR : TEXT_COLOR;
            drawLabel(graphics, this.label, left, top, height, color, width - TOGGLE_WIDTH - GAP);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.button);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.button);
        }
    }

    private final class TextRow extends Row {
        private final BrinAdminSetting setting;
        private final Component label;
        private final EditBox box;
        private final int boxWidth;

        TextRow(BrinAdminSetting setting) {
            this.setting = setting;
            this.label = label(setting);
            BrinAdminSetting.Kind kind = setting.kind();
            boolean items = kind == BrinAdminSetting.Kind.ITEMS;
            this.boxWidth = items ? ITEMS_WIDTH : NUMBER_WIDTH;
            this.box = new EditBox(font, 0, 0, this.boxWidth, BUTTON_HEIGHT, this.label);
            this.box.setMaxLength(items ? ITEMS_MAX_LENGTH : NUMBER_MAX_LENGTH);
            if (!items) {
                boolean decimal = kind == BrinAdminSetting.Kind.FLOAT;
                this.box.setFilter(text -> text.chars().allMatch(c -> Character.isDigit(c) || c == '-'
                    || decimal && (c == '.' || c == 'E' || c == 'e')));
            }
            this.box.setValue(values.get(setting));
            this.box.setResponder(text -> {
                values.put(setting, text);
                this.updateColor();
                refreshFooter();
            });
            this.updateColor();
            this.tooltip = settingTooltip(setting);
        }

        private void updateColor() {
            this.box.setTextColor(invalid(this.setting) ? INVALID_COLOR : VALID_COLOR);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.box.setPosition(left + width - this.boxWidth, top);
            this.box.render(graphics, mouseX, mouseY, partialTick);
            int color = changed(this.setting) ? CHANGED_COLOR : TEXT_COLOR;
            drawLabel(graphics, this.label, left, top, height, color, width - this.boxWidth - GAP);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.box);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.box);
        }
    }
}
