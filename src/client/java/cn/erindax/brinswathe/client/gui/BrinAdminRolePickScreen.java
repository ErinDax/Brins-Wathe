package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.admin.BrinAdminPanel;
import cn.erindax.brinswathe.admin.BrinAdminSnapshot;
import cn.erindax.brinswathe.client.BrinAdminClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class BrinAdminRolePickScreen extends BrinAdminListScreen {
    private static final int ROW_WIDTH = 360;
    private static final int BUTTON_WIDTH = 80;
    private final UUID playerId;
    private final Map<String, List<BrinAdminSnapshot.RoleState>> groups;
    private final Map<ResourceLocation, Component> names = new HashMap<>();

    public BrinAdminRolePickScreen(BrinAdminScreen hub, Screen parent, UUID playerId, String playerName) {
        super(Component.translatable("screen.brinswathe.admin.role_pick", playerName), hub, parent, true);
        this.playerId = playerId;
        this.groups = BrinAdminRolesScreen.group(hub.snapshot().roles());
        for (BrinAdminSnapshot.RoleState role : hub.snapshot().roles()) {
            this.names.put(role.id(), BrinAdminRolesScreen.roleName(role.id()));
        }
    }

    @Override
    protected List<Row> rows(String filter) {
        List<Row> rows = new ArrayList<>();
        for (Map.Entry<String, List<BrinAdminSnapshot.RoleState>> group : this.groups.entrySet()) {
            boolean header = false;
            for (BrinAdminSnapshot.RoleState role : group.getValue()) {
                if (!matches(filter, role.id().toString(), this.names.get(role.id()))) continue;
                if (!header) {
                    rows.add(new HeaderRow(BrinAdminRolesScreen.modName(group.getKey())));
                    header = true;
                }
                rows.add(new PickRow(role.id()));
            }
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

    private final class PickRow extends Row {
        private final String id;
        private final Component name;
        private final Component selectLabel = Component.translatable("gui.brinswathe.admin.role_pick.select");
        private final Button button;

        PickRow(ResourceLocation id) {
            this.id = id.toString();
            this.name = names.get(id);
            this.button = Button.builder(this.selectLabel, pressed -> this.pick())
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("gui.brinswathe.admin.role_pick.tip")))
                .build();
            this.tooltip = tooltip(Component.literal(this.id).withStyle(ChatFormatting.GRAY));
        }

        private void pick() {
            if (!confirmed(this.id)) return;
            BrinAdminClient.run(BrinAdminClient.op(BrinAdminPanel.OP_PLAYER_ROLE_SET, playerId.toString(), this.id));
            onClose();
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.button.setMessage(armedLabel(this.id, this.selectLabel));
            this.button.setPosition(left + width - BUTTON_WIDTH, top);
            this.button.render(graphics, mouseX, mouseY, partialTick);
            drawLabel(graphics, this.name, left, top, height, TEXT_COLOR, width - BUTTON_WIDTH - GAP);
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
