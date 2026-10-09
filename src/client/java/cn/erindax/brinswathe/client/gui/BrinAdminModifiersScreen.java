package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.admin.BrinAdminPanel;
import cn.erindax.brinswathe.admin.BrinAdminSnapshot;
import cn.erindax.brinswathe.client.BrinAdminClient;
import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;

@Environment(EnvType.CLIENT)
public final class BrinAdminModifiersScreen extends BrinAdminListScreen {
    private static final int ROW_WIDTH = 320;
    private static final int TOGGLE_WIDTH = 72;
    private final List<BrinAdminSnapshot.ModifierState> modifiers;
    private final Map<ResourceLocation, Component> names = new HashMap<>();
    private final Map<ResourceLocation, Boolean> enabled = new HashMap<>();

    public BrinAdminModifiersScreen(BrinAdminScreen hub) {
        super(Component.translatable("gui.brinswathe.admin.page.modifiers"), hub, hub, true);
        this.modifiers = hub.snapshot().modifiers();
        for (BrinAdminSnapshot.ModifierState modifier : this.modifiers) {
            this.names.put(modifier.id(), modifierName(modifier.id()));
            this.enabled.put(modifier.id(), modifier.enabled());
        }
    }

    @Override
    protected List<Row> rows(String filter) {
        List<Row> rows = new ArrayList<>();
        for (BrinAdminSnapshot.ModifierState modifier : this.modifiers) {
            if (matches(filter, modifier.id().toString(), this.names.get(modifier.id()))) {
                rows.add(new ModifierRow(modifier));
            }
        }
        return rows;
    }

    @Override
    protected int changes() {
        int count = 0;
        for (BrinAdminSnapshot.ModifierState modifier : this.modifiers) {
            if (this.enabled.get(modifier.id()) != modifier.enabled()) count++;
        }
        return count;
    }

    @Override
    protected boolean invalid() {
        return false;
    }

    @Override
    protected void apply() {
        List<JsonArray> ops = new ArrayList<>();
        for (BrinAdminSnapshot.ModifierState modifier : this.modifiers) {
            boolean enabledNow = this.enabled.get(modifier.id());
            if (enabledNow == modifier.enabled()) continue;
            ops.add(BrinAdminClient.op(
                BrinAdminPanel.OP_MODIFIER_ENABLED,
                modifier.id().toString(),
                Boolean.toString(enabledNow)
            ));
        }
        BrinAdminClient.save(ops);
    }

    @Override
    protected int rowWidth() {
        return ROW_WIDTH;
    }

    static Component modifierName(ResourceLocation id) {
        for (Modifier modifier : HMLModifiers.MODIFIERS) {
            if (!modifier.identifier().equals(id)) continue;
            Language language = Language.getInstance();
            String fullKey = "announcement.modifier." + id.toLanguageKey();
            String pathKey = "announcement.modifier." + id.getPath();
            MutableComponent name = Component.translatable(
                !language.has(fullKey) && language.has(pathKey) ? pathKey : fullKey
            );
            return modifier.color() == 0 ? name : name.withColor(modifier.color());
        }
        return Component.literal(id.toString());
    }

    private final class ModifierRow extends Row {
        private final BrinAdminSnapshot.ModifierState modifier;
        private final Component name;
        private final CycleButton<Boolean> button;

        ModifierRow(BrinAdminSnapshot.ModifierState modifier) {
            this.modifier = modifier;
            this.name = names.get(modifier.id());
            this.button = CycleButton.onOffBuilder(enabled.get(modifier.id()))
                .create(0, 0, TOGGLE_WIDTH, BUTTON_HEIGHT, Component.translatable("gui.brinswathe.admin.enabled"),
                    (button, value) -> {
                        enabled.put(modifier.id(), value);
                        refreshFooter();
                    });
            this.button.setTooltip(Tooltip.create(Component.translatable("gui.brinswathe.admin.modifier.enabled.tip")));
            this.tooltip = tooltip(Component.literal(modifier.id().toString()).withStyle(ChatFormatting.GRAY));
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.button.setPosition(left + width - TOGGLE_WIDTH, top);
            this.button.render(graphics, mouseX, mouseY, partialTick);
            boolean changed = enabled.get(this.modifier.id()) != this.modifier.enabled();
            drawLabel(graphics, marked(this.name, changed), left, top, height, TEXT_COLOR, width - TOGGLE_WIDTH - GAP);
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
}
