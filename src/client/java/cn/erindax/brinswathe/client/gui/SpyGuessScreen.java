package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.role.Spy;
import cn.erindax.brinswathe.client.RoleLabels;
import cn.erindax.brinswathe.component.RoundRecapComponent;
import cn.erindax.brinswathe.component.SpyComponent;
import cn.erindax.brinswathe.config.Tunable;
import cn.erindax.brinswathe.network.SpyGuessC2SPacket;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class SpyGuessScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int ROW_WIDTH = 300;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 20;
    private static final int FOOTER_HEIGHT = 36;
    private static final int SEARCH_WIDTH = 200;
    private static final int SEARCH_TOP = 38;
    private static final int LIST_TOP = 62;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int PROGRESS_COLOR = 0xAAAAAA;
    private static final int SECTION_COLOR = 0xE6C35C;
    private static final long CONFIRM_MS = 3000L;
    @Nullable
    private final UUID targetId;
    private String filter = "";
    @Nullable
    private RowList list;
    @Nullable
    private String armed;
    private long armedUntil;

    public SpyGuessScreen() {
        this(null, "");
    }

    private SpyGuessScreen(@Nullable UUID targetId, String targetName) {
        super(targetId == null
            ? Component.translatable("screen.brinswathe.spy.players")
            : Component.translatable("screen.brinswathe.spy.roles", targetName));
        this.targetId = targetId;
    }

    @Override
    protected void init() {
        Component hint = Component.translatable(this.targetId == null
            ? "gui.brinswathe.spy.search_player"
            : "gui.brinswathe.spy.search_role");
        EditBox search = new EditBox(
            this.font,
            (this.width - SEARCH_WIDTH) / 2,
            SEARCH_TOP,
            SEARCH_WIDTH,
            18,
            hint
        );
        search.setHint(hint.copy().withStyle(ChatFormatting.DARK_GRAY));
        search.setValue(this.filter);
        search.setResponder(value -> {
            this.filter = value;
            this.refreshRows();
        });
        this.addRenderableWidget(search);
        this.list = this.addRenderableWidget(new RowList());
        this.refreshRows();
        this.addRenderableWidget(Button.builder(
                this.targetId == null ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_BACK,
                button -> this.onClose())
            .bounds(this.width / 2 - 100, this.height - FOOTER_HEIGHT / 2 - BUTTON_HEIGHT / 2, 200, BUTTON_HEIGHT)
            .build());
        this.setInitialFocus(search);
    }

    @Override
    public void tick() {
        if (!this.available()) this.minecraft.setScreen(null);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, ModRoles.SPY.color());
        LocalPlayer player = this.minecraft.player;
        if (player == null) return;
        SpyComponent spy = SpyComponent.KEY.get(player);
        graphics.drawCenteredString(
            this.font,
            Component.translatable(
                "gui.brinswathe.spy.progress",
                spy.correct(),
                spy.required(),
                spy.wrong(),
                Tunable.SPY_MAX_WRONG_GUESSES.intValue()
            ),
            this.width / 2,
            24,
            PROGRESS_COLOR
        );
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.targetId == null ? null : new SpyGuessScreen());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean available() {
        LocalPlayer player = this.minecraft.player;
        if (player == null || !GameFunctions.isPlayerAliveAndSurvival(player)) return false;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE
            || !gameWorld.isRole(player, ModRoles.SPY)) {
            return false;
        }
        SpyComponent spy = SpyComponent.KEY.get(player);
        if (spy.required() <= 0 || spy.completed()) return false;
        return this.targetId == null || !spy.hasGuessed(this.targetId);
    }

    private void refreshRows() {
        if (this.list == null) return;
        String needle = this.filter.trim().toLowerCase(Locale.ROOT);
        this.list.replace(this.targetId == null ? this.playerRows(needle) : this.roleRows(needle));
    }

    private List<Row> playerRows(String needle) {
        LocalPlayer player = this.minecraft.player;
        ClientPacketListener connection = this.minecraft.getConnection();
        if (player == null || connection == null) return List.of();
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        RoundRecapComponent recap = RoundRecapComponent.KEY.get(player.level());
        SpyComponent spy = SpyComponent.KEY.get(player);
        List<PlayerRow> found = new ArrayList<>();
        for (Map.Entry<UUID, Role> entry : gameWorld.getRoles().entrySet()) {
            UUID id = entry.getKey();
            if (entry.getValue() == null || id.equals(player.getUUID()) || spy.hasGuessed(id)) continue;
            PlayerInfo info = connection.getPlayerInfo(id);
            String name = playerName(info, recap, id);
            if (!needle.isEmpty() && !name.toLowerCase(Locale.ROOT).contains(needle)) continue;
            found.add(new PlayerRow(id, name, info == null ? DefaultPlayerSkin.get(id) : info.getSkin()));
        }
        found.sort(Comparator.comparing(row -> row.name.toLowerCase(Locale.ROOT)));
        return new ArrayList<>(found);
    }

    private List<Row> roleRows(String needle) {
        List<Row> innocents = new ArrayList<>();
        List<Row> neutrals = new ArrayList<>();
        List<Row> killers = new ArrayList<>();
        for (Role role : WatheRoles.ROLES) {
            if (!Spy.isGuessable(role)) continue;
            RoleRow row = new RoleRow(role);
            if (!needle.isEmpty()
                && !row.id.toLowerCase(Locale.ROOT).contains(needle)
                && !row.name.getString().toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }
            if (role.canUseKiller()) {
                killers.add(row);
            } else if (role.isInnocent()) {
                innocents.add(row);
            } else {
                neutrals.add(row);
            }
        }
        List<Row> rows = new ArrayList<>();
        this.addGroup(rows, "gui.brinswathe.draft.camp.innocent", innocents);
        this.addGroup(rows, "gui.brinswathe.draft.camp.neutral", neutrals);
        this.addGroup(rows, "gui.brinswathe.draft.camp.killer", killers);
        return rows;
    }

    private void addGroup(List<Row> rows, String key, List<Row> group) {
        if (group.isEmpty()) return;
        rows.add(new HeaderRow(Component.translatable(key)));
        rows.addAll(group);
    }

    private static String playerName(@Nullable PlayerInfo info, RoundRecapComponent recap, UUID id) {
        if (info != null) return info.getProfile().getName();
        RoundRecapComponent.Identity identity = recap.identityOf(id);
        return identity == null ? id.toString().substring(0, 8) : identity.name();
    }

    private abstract static class Row extends ContainerObjectSelectionList.Entry<Row> {
    }

    private final class HeaderRow extends Row {
        private final Component text;

        HeaderRow(Component text) {
            this.text = text;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.drawString(SpyGuessScreen.this.font, this.text, left + 2, top + height - 10, SECTION_COLOR);
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

    private final class PlayerRow extends Row {
        private final String name;
        private final PlayerSkin skin;
        private final Button button;

        PlayerRow(UUID id, String name, PlayerSkin skin) {
            this.name = name;
            this.skin = skin;
            this.button = Button.builder(
                    Component.translatable("gui.brinswathe.spy.pick"),
                    pressed -> SpyGuessScreen.this.minecraft.setScreen(new SpyGuessScreen(id, name)))
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.button.setPosition(left + width - BUTTON_WIDTH, top);
            this.button.render(graphics, mouseX, mouseY, partialTick);
            PlayerFaceRenderer.draw(graphics, this.skin, left + 2, top + (height - 16) / 2, 16);
            graphics.drawString(
                SpyGuessScreen.this.font,
                this.name,
                left + 24,
                top + (height - 8) / 2,
                TEXT_COLOR
            );
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

    private final class RoleRow extends Row {
        private final String id;
        private final Component name;
        private final Component guessLabel = Component.translatable("gui.brinswathe.spy.guess");
        private final Button button;

        RoleRow(Role role) {
            this.id = role.identifier().toString();
            this.name = RoleLabels.of(role);
            this.button = Button.builder(this.guessLabel, pressed -> this.guess())
                .bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal(this.id).withStyle(ChatFormatting.GRAY)))
                .build();
        }

        private boolean isArmed() {
            return this.id.equals(SpyGuessScreen.this.armed)
                && Util.getMillis() < SpyGuessScreen.this.armedUntil;
        }

        private void guess() {
            UUID target = SpyGuessScreen.this.targetId;
            if (target == null) return;
            if (!this.isArmed()) {
                SpyGuessScreen.this.armed = this.id;
                SpyGuessScreen.this.armedUntil = Util.getMillis() + CONFIRM_MS;
                return;
            }
            SpyGuessScreen.this.armed = null;
            ClientPlayNetworking.send(new SpyGuessC2SPacket(target, this.id));
            SpyGuessScreen.this.minecraft.setScreen(null);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.button.setMessage(this.isArmed()
                ? Component.translatable("gui.brinswathe.spy.confirm").withStyle(ChatFormatting.RED)
                : this.guessLabel);
            this.button.setPosition(left + width - BUTTON_WIDTH, top);
            this.button.render(graphics, mouseX, mouseY, partialTick);
            graphics.drawString(
                SpyGuessScreen.this.font,
                this.name,
                left + 4,
                top + (height - 8) / 2,
                TEXT_COLOR
            );
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

    private final class RowList extends ContainerObjectSelectionList<Row> {
        RowList() {
            super(
                SpyGuessScreen.this.minecraft,
                SpyGuessScreen.this.width,
                SpyGuessScreen.this.height - LIST_TOP - FOOTER_HEIGHT,
                LIST_TOP,
                ROW_HEIGHT
            );
        }

        void replace(List<Row> rows) {
            this.replaceEntries(rows);
            this.setClampedScrollAmount(0.0);
        }

        @Override
        public int getRowWidth() {
            return Math.min(ROW_WIDTH, SpyGuessScreen.this.width - 40);
        }

        @Override
        protected int getScrollbarPosition() {
            return (SpyGuessScreen.this.width + this.getRowWidth()) / 2 + 6;
        }
    }
}
