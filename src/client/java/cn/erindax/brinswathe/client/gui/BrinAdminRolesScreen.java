package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.admin.BrinAdminPanel;
import cn.erindax.brinswathe.admin.BrinAdminSnapshot;
import cn.erindax.brinswathe.client.BrinAdminClient;
import com.google.gson.JsonArray;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.agmas.harpymodloader.Harpymodloader;

@Environment(EnvType.CLIENT)
public final class BrinAdminRolesScreen extends BrinAdminListScreen {
    private static final int ROW_WIDTH = 440;
    private static final int ENABLED_WIDTH = 72;
    private static final int FORCED_WIDTH = 96;
    private static final int BLOCKED_WIDTH = 96;
    private final List<BrinAdminSnapshot.RoleState> roles;
    private final Map<String, List<BrinAdminSnapshot.RoleState>> groups;
    private final Map<ResourceLocation, Component> names = new HashMap<>();
    private final Map<ResourceLocation, Boolean> enabled = new HashMap<>();
    private final Map<ResourceLocation, Boolean> forced = new HashMap<>();
    private final Map<ResourceLocation, Set<String>> blocked = new HashMap<>();

    public BrinAdminRolesScreen(BrinAdminScreen hub) {
        super(Component.translatable("gui.brinswathe.admin.page.roles"), hub, hub, true);
        this.roles = hub.snapshot().roles();
        this.groups = group(this.roles);
        for (BrinAdminSnapshot.RoleState role : this.roles) {
            this.names.put(role.id(), roleName(role.id()));
            this.enabled.put(role.id(), role.enabled());
            this.forced.put(role.id(), role.forced());
            this.blocked.put(role.id(), new LinkedHashSet<>(role.blocked()));
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
                    rows.add(new HeaderRow(modName(group.getKey())));
                    header = true;
                }
                rows.add(new RoleRow(role));
            }
        }
        return rows;
    }

    @Override
    protected int changes() {
        int count = 0;
        for (BrinAdminSnapshot.RoleState role : this.roles) count += this.changes(role);
        return count;
    }

    @Override
    protected boolean invalid() {
        return false;
    }

    @Override
    protected void apply() {
        List<JsonArray> ops = new ArrayList<>();
        for (BrinAdminSnapshot.RoleState role : this.roles) {
            String id = role.id().toString();
            boolean enabledNow = this.enabled.get(role.id());
            if (enabledNow != role.enabled()) {
                ops.add(BrinAdminClient.op(BrinAdminPanel.OP_ROLE_ENABLED, id, Boolean.toString(enabledNow)));
            }
            boolean forcedNow = this.forced.get(role.id());
            if (forcedNow != role.forced()) {
                ops.add(BrinAdminClient.op(BrinAdminPanel.OP_ROLE_FORCED, id, Boolean.toString(forcedNow)));
            }
            Set<String> current = this.blocked.get(role.id());
            for (String modifier : current) {
                if (!role.blocked().contains(modifier)) {
                    ops.add(BrinAdminClient.op(BrinAdminPanel.OP_ROLE_BLOCKED, id, modifier, "true"));
                }
            }
            for (String modifier : role.blocked()) {
                if (!current.contains(modifier)) {
                    ops.add(BrinAdminClient.op(BrinAdminPanel.OP_ROLE_BLOCKED, id, modifier, "false"));
                }
            }
        }
        BrinAdminClient.save(ops);
    }

    @Override
    protected int rowWidth() {
        return ROW_WIDTH;
    }

    static Component roleName(ResourceLocation id) {
        for (Role role : WatheRoles.ROLES) {
            if (!role.identifier().equals(id)) continue;
            MutableComponent name = Harpymodloader.getRoleName(role);
            return role.color() == 0 ? name : name.withColor(role.color());
        }
        return Component.literal(id.toString());
    }

    static Component modName(String namespace) {
        return Component.literal(FabricLoader.getInstance().getModContainer(namespace)
            .map(container -> container.getMetadata().getName())
            .orElse(namespace));
    }

    static Map<String, List<BrinAdminSnapshot.RoleState>> group(List<BrinAdminSnapshot.RoleState> roles) {
        Map<String, List<BrinAdminSnapshot.RoleState>> groups = new LinkedHashMap<>();
        for (BrinAdminSnapshot.RoleState role : roles) {
            groups.computeIfAbsent(role.id().getNamespace(), namespace -> new ArrayList<>()).add(role);
        }
        return groups;
    }

    private int changes(BrinAdminSnapshot.RoleState role) {
        int count = 0;
        if (this.enabled.get(role.id()) != role.enabled()) count++;
        if (this.forced.get(role.id()) != role.forced()) count++;
        Set<String> current = this.blocked.get(role.id());
        for (String modifier : current) {
            if (!role.blocked().contains(modifier)) count++;
        }
        for (String modifier : role.blocked()) {
            if (!current.contains(modifier)) count++;
        }
        return count;
    }

    private void openBlacklist(BrinAdminSnapshot.RoleState role) {
        this.minecraft.setScreen(new BrinAdminBlacklistScreen(
            this.hub,
            this,
            this.names.get(role.id()),
            this.blocked.get(role.id()),
            result -> this.blocked.put(role.id(), new LinkedHashSet<>(result))
        ));
    }

    private final class RoleRow extends Row {
        private final BrinAdminSnapshot.RoleState role;
        private final Component name;
        private final CycleButton<Boolean> enabledButton;
        private final CycleButton<Boolean> forcedButton;
        private final Button blockedButton;
        private final List<AbstractWidget> widgets;

        RoleRow(BrinAdminSnapshot.RoleState role) {
            this.role = role;
            this.name = names.get(role.id());
            this.enabledButton = CycleButton.onOffBuilder(enabled.get(role.id()))
                .create(0, 0, ENABLED_WIDTH, BUTTON_HEIGHT, Component.translatable("gui.brinswathe.admin.enabled"),
                    (button, value) -> {
                        enabled.put(role.id(), value);
                        refreshFooter();
                    });
            this.forcedButton = CycleButton.onOffBuilder(forced.get(role.id()))
                .create(0, 0, FORCED_WIDTH, BUTTON_HEIGHT, Component.translatable("gui.brinswathe.admin.role.forced"),
                    (button, value) -> {
                        forced.put(role.id(), value);
                        refreshFooter();
                    });
            this.blockedButton = Button.builder(
                    Component.translatable("gui.brinswathe.admin.role.blocked", blocked.get(role.id()).size()),
                    button -> openBlacklist(role))
                .bounds(0, 0, BLOCKED_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("gui.brinswathe.admin.role.blocked.tip")))
                .build();
            if (role.vanilla()) {
                this.enabledButton.active = false;
                this.forcedButton.active = false;
                Tooltip vanilla = Tooltip.create(Component.translatable("gui.brinswathe.admin.role.vanilla"));
                this.enabledButton.setTooltip(vanilla);
                this.forcedButton.setTooltip(vanilla);
            } else {
                this.enabledButton.setTooltip(Tooltip.create(Component.translatable("gui.brinswathe.admin.role.enabled.tip")));
                this.forcedButton.setTooltip(Tooltip.create(Component.translatable("gui.brinswathe.admin.role.forced.tip")));
            }
            this.widgets = List.of(this.enabledButton, this.forcedButton, this.blockedButton);
            this.tooltip = tooltip(Component.literal(role.id().toString()).withStyle(ChatFormatting.GRAY));
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            int right = left + width;
            this.blockedButton.setPosition(right - BLOCKED_WIDTH, top);
            this.forcedButton.setPosition(right - BLOCKED_WIDTH - GAP - FORCED_WIDTH, top);
            this.enabledButton.setPosition(right - BLOCKED_WIDTH - FORCED_WIDTH - GAP * 2 - ENABLED_WIDTH, top);
            for (AbstractWidget widget : this.widgets) widget.render(graphics, mouseX, mouseY, partialTick);
            int labelWidth = width - BLOCKED_WIDTH - FORCED_WIDTH - ENABLED_WIDTH - GAP * 3;
            drawLabel(graphics, marked(this.name, changes(this.role) > 0), left, top, height, TEXT_COLOR, labelWidth);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.widgets;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.widgets;
        }
    }
}
