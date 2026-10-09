package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.admin.BrinAdminPanel;
import cn.erindax.brinswathe.admin.BrinAdminSnapshot;
import cn.erindax.brinswathe.client.BrinAdminClient;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class BrinAdminPlayersScreen extends BrinAdminListScreen {
    private static final int ROW_WIDTH = 400;
    private static final int BUTTON_WIDTH = 80;

    public BrinAdminPlayersScreen(BrinAdminScreen hub) {
        super(Component.translatable("gui.brinswathe.admin.page.players"), hub, hub, true);
    }

    @Override
    public void onSnapshot(BrinAdminSnapshot snapshot) {
        this.refreshRows();
    }

    @Override
    protected List<Row> rows(String filter) {
        BrinAdminSnapshot snapshot = this.hub.snapshot();
        List<Row> rows = new ArrayList<>();
        List<Row> players = new ArrayList<>();
        for (BrinAdminSnapshot.PlayerState player : snapshot.players()) {
            if (matches(filter, player.name(), roleLabel(player.role()))) players.add(new PlayerRow(player));
        }
        rows.add(new HeaderRow(Component.translatable("gui.brinswathe.admin.players.online", players.size())));
        rows.addAll(players);
        List<Row> punished = new ArrayList<>();
        for (String name : snapshot.punishments()) {
            if (matches(filter, name, Component.literal(name))) punished.add(new PunishRow(name));
        }
        if (!punished.isEmpty()) {
            rows.add(new HeaderRow(Component.translatable("gui.brinswathe.admin.players.punished", punished.size())));
            rows.addAll(punished);
        }
        return rows;
    }

    @Override
    protected int rowWidth() {
        return ROW_WIDTH;
    }

    @Override
    protected boolean editable() {
        return false;
    }

    static Component roleLabel(String role) {
        ResourceLocation id = role.isEmpty() ? null : ResourceLocation.tryParse(role);
        if (id == null) {
            return Component.translatable("gui.brinswathe.admin.players.no_role").withStyle(ChatFormatting.GRAY);
        }
        return BrinAdminRolesScreen.roleName(id);
    }

    private final class PlayerRow extends Row {
        private final Component label;
        private final Button button;

        PlayerRow(BrinAdminSnapshot.PlayerState player) {
            MutableComponent label = Component.literal(player.name()).append("  ").append(roleLabel(player.role()));
            if (player.punished()) {
                label.append("  ").append(Component.translatable("gui.brinswathe.admin.players.tag.punished")
                    .withStyle(ChatFormatting.RED));
            }
            if (player.psycho()) {
                label.append("  ").append(Component.translatable("gui.brinswathe.admin.players.tag.psycho")
                    .withStyle(ChatFormatting.DARK_RED));
            }
            this.label = label;
            this.button = Button.builder(Component.translatable("gui.brinswathe.admin.players.manage"), pressed ->
                    minecraft.setScreen(new BrinAdminPlayerScreen(hub, BrinAdminPlayersScreen.this, player.id(), player.name())))
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.button.setPosition(left + width - BUTTON_WIDTH, top);
            this.button.render(graphics, mouseX, mouseY, partialTick);
            drawLabel(graphics, this.label, left, top, height, TEXT_COLOR, width - BUTTON_WIDTH - GAP);
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

    private final class PunishRow extends Row {
        private final Component label;
        private final Button button;

        PunishRow(String name) {
            this.label = Component.literal(name);
            this.button = Button.builder(Component.translatable("gui.brinswathe.admin.players.cancel_punish"), pressed ->
                    BrinAdminClient.run(BrinAdminClient.op(BrinAdminPanel.OP_PUNISH_CANCEL, name)))
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("gui.brinswathe.admin.players.cancel_punish.tip")))
                .build();
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.button.setPosition(left + width - BUTTON_WIDTH, top);
            this.button.render(graphics, mouseX, mouseY, partialTick);
            drawLabel(graphics, this.label, left, top, height, TEXT_COLOR, width - BUTTON_WIDTH - GAP);
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
