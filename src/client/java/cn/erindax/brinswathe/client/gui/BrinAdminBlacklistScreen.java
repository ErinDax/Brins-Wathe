package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.admin.BrinAdminSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class BrinAdminBlacklistScreen extends BrinAdminListScreen {
    private static final int ROW_WIDTH = 320;
    private static final int TOGGLE_WIDTH = 72;
    private final List<BrinAdminSnapshot.ModifierState> modifiers;
    private final Map<ResourceLocation, Component> names = new HashMap<>();
    private final Set<String> original;
    private final Set<String> blocked;
    private final Consumer<Set<String>> result;

    public BrinAdminBlacklistScreen(
        BrinAdminScreen hub,
        Screen parent,
        Component roleName,
        Set<String> blocked,
        Consumer<Set<String>> result
    ) {
        super(Component.translatable("screen.brinswathe.admin.blacklist", roleName), hub, parent, true);
        this.modifiers = hub.snapshot().modifiers();
        for (BrinAdminSnapshot.ModifierState modifier : this.modifiers) {
            this.names.put(modifier.id(), BrinAdminModifiersScreen.modifierName(modifier.id()));
        }
        this.original = Set.copyOf(blocked);
        this.blocked = new LinkedHashSet<>(blocked);
        this.result = result;
    }

    @Override
    protected List<Row> rows(String filter) {
        List<Row> rows = new ArrayList<>();
        for (BrinAdminSnapshot.ModifierState modifier : this.modifiers) {
            if (matches(filter, modifier.id().toString(), this.names.get(modifier.id()))) {
                rows.add(new BlockRow(modifier.id()));
            }
        }
        return rows;
    }

    @Override
    protected int changes() {
        int count = 0;
        for (String modifier : this.blocked) {
            if (!this.original.contains(modifier)) count++;
        }
        for (String modifier : this.original) {
            if (!this.blocked.contains(modifier)) count++;
        }
        return count;
    }

    @Override
    protected boolean invalid() {
        return false;
    }

    @Override
    protected void apply() {
        this.result.accept(Set.copyOf(this.blocked));
    }

    @Override
    protected int rowWidth() {
        return ROW_WIDTH;
    }

    @Override
    protected Component confirmLabel() {
        return Component.translatable("gui.brinswathe.admin.ok");
    }

    private final class BlockRow extends Row {
        private final String id;
        private final Component name;
        private final CycleButton<Boolean> button;

        BlockRow(ResourceLocation id) {
            this.id = id.toString();
            this.name = names.get(id);
            this.button = CycleButton.<Boolean>builder(value -> value
                    ? Component.translatable("gui.brinswathe.admin.blacklist.blocked").withStyle(ChatFormatting.RED)
                    : Component.translatable("gui.brinswathe.admin.blacklist.allowed").withStyle(ChatFormatting.GREEN))
                .withValues(List.of(false, true))
                .withInitialValue(blocked.contains(this.id))
                .displayOnlyValue()
                .create(0, 0, TOGGLE_WIDTH, BUTTON_HEIGHT, this.name, (button, value) -> {
                    if (value) {
                        blocked.add(this.id);
                    } else {
                        blocked.remove(this.id);
                    }
                    refreshFooter();
                });
            this.tooltip = tooltip(Component.literal(this.id).withStyle(ChatFormatting.GRAY));
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.button.setPosition(left + width - TOGGLE_WIDTH, top);
            this.button.render(graphics, mouseX, mouseY, partialTick);
            boolean changed = blocked.contains(this.id) != original.contains(this.id);
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
