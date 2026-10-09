package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.admin.BrinAdminPanel;
import cn.erindax.brinswathe.admin.BrinAdminSetting;
import cn.erindax.brinswathe.admin.BrinAdminSnapshot;
import cn.erindax.brinswathe.client.BrinAdminClient;
import dev.doctor4t.wathe.api.WatheRoles;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class BrinAdminPlayerScreen extends BrinAdminListScreen {
    private static final int ROW_WIDTH = 420;
    private static final int BUTTON_WIDTH = 56;
    private static final int WIDE_BUTTON_WIDTH = 80;
    private static final int NUMBER_WIDTH = 44;
    private static final int ITEM_WIDTH = 120;
    private static final int NUMBER_MAX_LENGTH = 6;
    private static final int ITEM_MAX_LENGTH = 128;
    private static final int VALID_COLOR = 0xE0E0E0;
    private static final int INVALID_COLOR = 0xFF5555;
    private static final String DEFAULT_ITEM = "wathe:knife";
    private static final String CONFIRM_ROLE_REMOVE = "role_remove";
    private static final String CONFIRM_PSYCHO = "psycho";
    private static final String CONFIRM_MUSIC = "music";
    private final UUID playerId;
    private final String playerName;
    private String armor;
    private String psychoSeconds = "30";
    private String psychoArmour = "1";
    private String cooldown = "0";
    private String itemId;
    private String itemTicks = "0";

    public BrinAdminPlayerScreen(BrinAdminScreen hub, Screen parent, UUID playerId, String playerName) {
        super(Component.translatable("screen.brinswathe.admin.player", playerName), hub, parent, false);
        this.playerId = playerId;
        this.playerName = playerName;
        BrinAdminSnapshot.PlayerState player = this.find(hub.snapshot());
        this.armor = Integer.toString(player == null ? 0 : player.armor());
        this.itemId = firstItem(hub.snapshot());
    }

    @Override
    public void onSnapshot(BrinAdminSnapshot snapshot) {
        this.refreshRows();
    }

    @Override
    protected List<Row> rows(String filter) {
        BrinAdminSnapshot.PlayerState player = this.find(this.hub.snapshot());
        if (player == null) {
            return List.of(new HeaderRow(Component.translatable("gui.brinswathe.admin.player.offline")));
        }
        String id = player.id().toString();
        List<Row> rows = new ArrayList<>();

        Component removeLabel = Component.translatable("gui.brinswathe.admin.player.role.remove");
        Button setRole = this.button(Component.translatable("gui.brinswathe.admin.player.role.set"), BUTTON_WIDTH, () ->
            this.minecraft.setScreen(new BrinAdminRolePickScreen(this.hub, this, this.playerId, this.playerName)));
        Button removeRole = this.button(removeLabel, BUTTON_WIDTH, () -> {
            if (this.confirmed(CONFIRM_ROLE_REMOVE)) this.send(BrinAdminPanel.OP_PLAYER_ROLE_REMOVE, id);
        });
        removeRole.active = !player.role().isEmpty() && !player.role().equals(WatheRoles.CIVILIAN.identifier().toString());
        rows.add(new WidgetRow(
            Component.translatable("gui.brinswathe.admin.player.role", BrinAdminPlayersScreen.roleLabel(player.role())),
            "gui.brinswathe.admin.player.role.tip",
            () -> removeRole.setMessage(this.armedLabel(CONFIRM_ROLE_REMOVE, removeLabel)),
            setRole,
            removeRole
        ));

        Button punish = this.button(Component.translatable(player.punished()
            ? "gui.brinswathe.admin.player.punish.cancel"
            : "gui.brinswathe.admin.player.punish.add"), WIDE_BUTTON_WIDTH, () ->
            this.send(BrinAdminPanel.OP_PLAYER_PUNISH, id, Boolean.toString(!player.punished())));
        rows.add(new WidgetRow(
            Component.translatable("gui.brinswathe.admin.player.punish", Component.translatable(player.punished()
                ? "gui.brinswathe.admin.player.punish.on"
                : "gui.brinswathe.admin.player.punish.off")),
            "gui.brinswathe.admin.player.punish.tip",
            () -> {},
            punish
        ));

        Button food = this.allergyButton(player, id, BrinAdminPanel.ALLERGY_FOOD);
        Button drink = this.allergyButton(player, id, BrinAdminPanel.ALLERGY_DRINK);
        rows.add(new WidgetRow(
            player.allergic()
                ? Component.translatable("gui.brinswathe.admin.player.allergy", allergyName(player.allergy()))
                : Component.translatable("gui.brinswathe.admin.player.allergy.none"),
            "gui.brinswathe.admin.player.allergy.tip",
            () -> {},
            food,
            drink
        ));

        EditBox armorBox = this.numberBox(this.armor, "gui.brinswathe.admin.player.armor.box", value -> this.armor = value,
            0, BrinAdminPanel.ARMOR_MAX);
        Button armorApply = this.button(Component.translatable("gui.brinswathe.admin.apply"), BUTTON_WIDTH, () ->
            this.send(BrinAdminPanel.OP_PLAYER_ARMOR, id, this.armor.trim()));
        rows.add(new WidgetRow(
            Component.translatable("gui.brinswathe.admin.player.armor", player.armor()),
            "gui.brinswathe.admin.player.armor.tip",
            () -> armorApply.active = inRange(this.armor, 0, BrinAdminPanel.ARMOR_MAX),
            armorBox,
            armorApply
        ));

        Component triggerLabel = Component.translatable("gui.brinswathe.admin.trigger");
        EditBox secondsBox = this.numberBox(this.psychoSeconds, "gui.brinswathe.admin.player.psycho.seconds",
            value -> this.psychoSeconds = value, BrinAdminPanel.PSYCHO_SECONDS_MIN, BrinAdminPanel.PSYCHO_SECONDS_MAX);
        EditBox armourBox = this.numberBox(this.psychoArmour, "gui.brinswathe.admin.player.psycho.armour",
            value -> this.psychoArmour = value, 0, BrinAdminPanel.PSYCHO_ARMOUR_MAX);
        Button trigger = this.button(triggerLabel, BUTTON_WIDTH, () -> {
            if (this.confirmed(CONFIRM_PSYCHO)) {
                this.send(BrinAdminPanel.OP_PLAYER_PSYCHO, id, this.psychoSeconds.trim(), this.psychoArmour.trim());
            }
        });
        rows.add(new WidgetRow(
            Component.translatable(player.psycho()
                ? "gui.brinswathe.admin.player.psycho.active"
                : "gui.brinswathe.admin.player.psycho"),
            "gui.brinswathe.admin.player.psycho.tip",
            () -> {
                trigger.active = !player.psycho()
                    && inRange(this.psychoSeconds, BrinAdminPanel.PSYCHO_SECONDS_MIN, BrinAdminPanel.PSYCHO_SECONDS_MAX)
                    && inRange(this.psychoArmour, 0, BrinAdminPanel.PSYCHO_ARMOUR_MAX);
                trigger.setMessage(this.armedLabel(CONFIRM_PSYCHO, triggerLabel));
            },
            secondsBox,
            armourBox,
            trigger
        ));

        EditBox cooldownBox = this.numberBox(this.cooldown, "gui.brinswathe.admin.player.cooldown.box",
            value -> this.cooldown = value, 0, BrinAdminPanel.COOLDOWN_TICKS_MAX);
        Button cooldownApply = this.button(Component.translatable("gui.brinswathe.admin.apply"), BUTTON_WIDTH, () ->
            this.send(BrinAdminPanel.OP_PLAYER_COOLDOWN, id, this.cooldown.trim()));
        rows.add(new WidgetRow(
            Component.translatable("gui.brinswathe.admin.player.cooldown"),
            "gui.brinswathe.admin.player.cooldown.tip",
            () -> cooldownApply.active = inRange(this.cooldown, 0, BrinAdminPanel.COOLDOWN_TICKS_MAX),
            cooldownBox,
            cooldownApply
        ));

        EditBox itemBox = new EditBox(this.font, 0, 0, ITEM_WIDTH, BUTTON_HEIGHT,
            Component.translatable("gui.brinswathe.admin.player.item_cd.item"));
        itemBox.setMaxLength(ITEM_MAX_LENGTH);
        itemBox.setValue(this.itemId);
        itemBox.setTooltip(Tooltip.create(Component.translatable("gui.brinswathe.admin.player.item_cd.item")));
        itemBox.setTextColor(BrinAdminSetting.isItem(this.itemId) ? VALID_COLOR : INVALID_COLOR);
        itemBox.setResponder(value -> {
            this.itemId = value;
            itemBox.setTextColor(BrinAdminSetting.isItem(value) ? VALID_COLOR : INVALID_COLOR);
        });
        EditBox ticksBox = this.numberBox(this.itemTicks, "gui.brinswathe.admin.player.item_cd.ticks",
            value -> this.itemTicks = value, 0, BrinAdminPanel.COOLDOWN_TICKS_MAX);
        Button itemApply = this.button(Component.translatable("gui.brinswathe.admin.apply"), BUTTON_WIDTH, () ->
            this.send(BrinAdminPanel.OP_PLAYER_ITEM_COOLDOWN, id, this.itemId.trim(), this.itemTicks.trim()));
        rows.add(new WidgetRow(
            Component.translatable("gui.brinswathe.admin.player.item_cd"),
            "gui.brinswathe.admin.player.item_cd.tip",
            () -> itemApply.active = BrinAdminSetting.isItem(this.itemId)
                && inRange(this.itemTicks, 0, BrinAdminPanel.COOLDOWN_TICKS_MAX),
            itemBox,
            ticksBox,
            itemApply
        ));

        Component clearLabel = Component.translatable("gui.brinswathe.admin.player.music.clear");
        Button clearMusic = this.button(clearLabel, BUTTON_WIDTH, () -> {
            if (this.confirmed(CONFIRM_MUSIC)) this.send(BrinAdminPanel.OP_PLAYER_MUSIC_CLEAR, id);
        });
        clearMusic.active = player.music();
        rows.add(new WidgetRow(
            Component.translatable("gui.brinswathe.admin.player.music", Component.translatable(player.music()
                ? "gui.brinswathe.admin.player.music.has"
                : "gui.brinswathe.admin.player.music.none")),
            "gui.brinswathe.admin.player.music.tip",
            () -> clearMusic.setMessage(this.armedLabel(CONFIRM_MUSIC, clearLabel)),
            clearMusic
        ));
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

    @Nullable
    private BrinAdminSnapshot.PlayerState find(BrinAdminSnapshot snapshot) {
        for (BrinAdminSnapshot.PlayerState state : snapshot.players()) {
            if (state.id().equals(this.playerId)) return state;
        }
        return null;
    }

    private void send(String... op) {
        BrinAdminClient.run(BrinAdminClient.op(op));
    }

    private Button button(Component label, int width, Runnable action) {
        return Button.builder(label, pressed -> action.run()).bounds(0, 0, width, BUTTON_HEIGHT).build();
    }

    private Button allergyButton(BrinAdminSnapshot.PlayerState player, String id, String type) {
        Button button = this.button(allergyName(type), BUTTON_WIDTH, () -> this.send(BrinAdminPanel.OP_PLAYER_ALLERGY, id, type));
        button.active = player.allergic() && !type.equals(player.allergy());
        return button;
    }

    private EditBox numberBox(String value, String tipKey, Consumer<String> setter, int min, int max) {
        Component tip = Component.translatable(tipKey);
        EditBox box = new EditBox(this.font, 0, 0, NUMBER_WIDTH, BUTTON_HEIGHT, tip);
        box.setMaxLength(NUMBER_MAX_LENGTH);
        box.setFilter(text -> text.chars().allMatch(Character::isDigit));
        box.setValue(value);
        box.setTooltip(Tooltip.create(tip));
        box.setTextColor(inRange(value, min, max) ? VALID_COLOR : INVALID_COLOR);
        box.setResponder(text -> {
            setter.accept(text);
            box.setTextColor(inRange(text, min, max) ? VALID_COLOR : INVALID_COLOR);
        });
        return box;
    }

    private static Component allergyName(String type) {
        return Component.translatable("hud.allergic.type." + type);
    }

    private static boolean inRange(String text, int min, int max) {
        try {
            int value = Integer.parseInt(text.trim());
            return value >= min && value <= max;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static String firstItem(BrinAdminSnapshot snapshot) {
        String list = snapshot.settings().getOrDefault(BrinAdminSetting.RESET_ITEMS_LIST.id(), "");
        for (String part : list.split(",")) {
            String id = part.trim();
            if (BrinAdminSetting.isItem(id)) return id;
        }
        return DEFAULT_ITEM;
    }

    private final class WidgetRow extends Row {
        private final Component label;
        private final Runnable refresh;
        private final List<AbstractWidget> widgets;

        WidgetRow(Component label, String tipKey, Runnable refresh, AbstractWidget... widgets) {
            this.label = label;
            this.refresh = refresh;
            this.widgets = List.of(widgets);
            this.tooltip = tooltip(Component.translatable(tipKey));
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.refresh.run();
            int x = left + width;
            for (int position = this.widgets.size() - 1; position >= 0; position--) {
                AbstractWidget current = this.widgets.get(position);
                x -= current.getWidth();
                current.setPosition(x, top);
                current.render(graphics, mouseX, mouseY, partialTick);
                x -= GAP;
            }
            drawLabel(graphics, this.label, left, top, height, TEXT_COLOR, x - left);
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
