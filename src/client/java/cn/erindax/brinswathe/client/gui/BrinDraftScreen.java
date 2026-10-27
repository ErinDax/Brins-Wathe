package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.client.BrinRoleLabels;
import cn.erindax.brinswathe.network.BrinDraftChoiceC2SPacket;
import cn.erindax.brinswathe.network.BrinDraftStateS2CPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.wathe.api.Role;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class BrinDraftScreen extends Screen {
    private static final int NONE = -1;
    private static final int GOLD = 0xFFFFD54F;
    private static final int CARD_HEIGHT = 136;
    private static final int CARD_GAP = 12;
    private static final int HEADER_HEIGHT = 18;
    private static final int GOAL_TOP = 50;
    private static final int LINE_HEIGHT = 10;
    private static final int MAX_GOAL_LINES = 7;
    private static final int CHIP_SIZE = 14;
    private static final int CHIP_GAP = 2;
    private static final int CARDS_OFFSET = 64;
    private static final long FLIP_MILLIS = 260L;
    private static final long FLIP_STAGGER = 120L;
    private final List<String> roleIds = new ArrayList<>();
    private final List<Role> roles = new ArrayList<>();
    private int number;
    private int total;
    private int current;
    private long deadline;
    private long dealtAt;
    private String pickedId = "";
    private Role picked;
    private boolean random;
    private boolean sent;
    private boolean finished;

    public BrinDraftScreen(BrinDraftStateS2CPacket state) {
        super(Component.translatable("screen.brinswathe.draft"));
        this.apply(state);
    }

    public void update(BrinDraftStateS2CPacket state) {
        boolean wasTurn = this.myTurn();
        this.apply(state);
        if (wasTurn != this.myTurn() || this.myTurn()) this.rebuildWidgets();
    }

    public void finish() {
        this.finished = true;
        this.onClose();
    }

    private void apply(BrinDraftStateS2CPacket state) {
        boolean newOffers = !state.offers().equals(this.roleIds);
        this.number = state.number();
        this.total = state.total();
        this.current = state.current();
        this.deadline = Util.getMillis() + state.seconds() * 1000L;
        this.pickedId = state.picked();
        this.picked = BrinRoleLabels.find(state.picked());
        this.random = state.random();
        if (newOffers) {
            this.roleIds.clear();
            this.roles.clear();
            this.roleIds.addAll(state.offers());
            for (String roleId : this.roleIds) this.roles.add(BrinRoleLabels.find(roleId));
            this.dealtAt = Util.getMillis();
            this.sent = false;
        }
    }

    private boolean myTurn() {
        return !this.roleIds.isEmpty() && !this.decided();
    }

    private boolean decided() {
        return this.random || !this.pickedId.isEmpty();
    }

    @Override
    protected void init() {
        if (!this.myTurn()) return;
        int width = 150;
        Button randomButton = this.addRenderableWidget(Button.builder(
                Component.translatable("gui.brinswathe.draft.random"),
                pressed -> this.choose(NONE))
            .bounds(this.width / 2 - width / 2, this.cardTop() + CARD_HEIGHT + 8, width, 20)
            .build());
        randomButton.active = !this.sent;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        long now = Util.getMillis();
        int top = this.cardTop();
        if (this.myTurn()) {
            int hovered = this.sent ? NONE : this.cardAt(mouseX, mouseY);
            int count = this.roleIds.size();
            for (int i = 0; i < count; i++) {
                float flip = Mth.clamp((now - this.dealtAt - i * FLIP_STAGGER) / (float) FLIP_MILLIS, 0.0F, 1.0F);
                Component footer = Component.translatable("gui.brinswathe.draft.click").withStyle(ChatFormatting.DARK_GRAY);
                this.renderCard(graphics, this.cardLeft(i, count), top, this.roles.get(i), this.roleIds.get(i),
                    i == hovered, false, flip, footer);
            }
        } else if (this.decided()) {
            Component footer = Component.translatable("gui.brinswathe.draft.picked").withStyle(ChatFormatting.GOLD);
            this.renderCard(graphics, this.cardLeft(0, 1), top, this.random ? null : this.picked, this.pickedId,
                false, true, 1.0F, footer);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int center = this.width / 2;
        int top = this.contentTop();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(center, top, 0.0F);
        pose.scale(1.5F, 1.5F, 1.0F);
        graphics.drawCenteredString(this.font, this.title, 0, 0, GOLD);
        pose.popPose();
        graphics.drawCenteredString(
            this.font,
            Component.translatable("gui.brinswathe.draft.number", this.number).withStyle(ChatFormatting.WHITE),
            center,
            top + 18,
            0xFFFFFF
        );
        this.renderChips(graphics, top + 30);
        long remaining = Math.max(0L, (this.deadline - Util.getMillis() + 999L) / 1000L);
        ChatFormatting timerColor = remaining <= 5 ? ChatFormatting.RED : ChatFormatting.YELLOW;
        Component status;
        if (this.current == 0) {
            status = Component.translatable("gui.brinswathe.draft.done").withStyle(ChatFormatting.GREEN);
        } else if (this.myTurn()) {
            status = Component.translatable("gui.brinswathe.draft.your_turn", remaining).withStyle(timerColor);
        } else {
            status = Component.translatable("gui.brinswathe.draft.current", this.current, remaining)
                .withStyle(ChatFormatting.YELLOW);
        }
        graphics.drawCenteredString(this.font, status, center, top + 50, 0xFFFFFF);
        if (!this.myTurn() && !this.decided()) this.renderWaiting(graphics, center);
    }

    private void renderWaiting(GuiGraphics graphics, int center) {
        int y = this.cardTop() + CARD_HEIGHT / 2 - 20;
        int ahead = Math.max(0, this.number - Math.max(1, this.current));
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(center, y, 0.0F);
        pose.scale(2.0F, 2.0F, 1.0F);
        graphics.drawCenteredString(this.font,
            Component.translatable("gui.brinswathe.draft.ahead", ahead), 0, 0, 0xFFFFFFFF);
        pose.popPose();
        List<FormattedCharSequence> lines = this.font.split(
            Component.translatable("gui.brinswathe.draft.close_hint").withStyle(ChatFormatting.GRAY),
            Math.max(120, this.width - 40)
        );
        int lineY = y + 28;
        for (FormattedCharSequence line : lines) {
            graphics.drawCenteredString(this.font, line, center, lineY, 0xFFFFFF);
            lineY += LINE_HEIGHT + 1;
        }
    }

    private void renderChips(GuiGraphics graphics, int y) {
        if (this.total <= 0) return;
        int size = Mth.clamp((this.width - 40) / this.total - CHIP_GAP, 8, CHIP_SIZE);
        int rowWidth = this.total * size + (this.total - 1) * CHIP_GAP;
        int left = (this.width - rowWidth) / 2;
        for (int n = 1; n <= this.total; n++) {
            int x = left + (n - 1) * (size + CHIP_GAP);
            boolean done = this.current == 0 || n < this.current;
            boolean active = n == this.current;
            int fill = active ? GOLD : done ? 0xFF3A3A3A : 0xFF1E1E1E;
            int text = active ? 0xFF000000 : done ? 0xFF808080 : 0xFFE0E0E0;
            graphics.fill(x, y, x + size, y + size, fill);
            if (n == this.number) graphics.renderOutline(x - 1, y - 1, size + 2, size + 2, 0xFFFFFFFF);
            String label = Integer.toString(n);
            graphics.drawString(this.font, label, x + (size - this.font.width(label)) / 2 + 1,
                y + (size - 8) / 2 + 1, text, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.myTurn() && !this.sent) {
            int index = this.cardAt(mouseX, mouseY);
            if (index >= 0) {
                this.choose(index);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.finished || !this.myTurn();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderCard(
        GuiGraphics graphics,
        int left,
        int baseTop,
        Role role,
        String roleId,
        boolean hovered,
        boolean selected,
        float flip,
        Component footer
    ) {
        if (flip <= 0.0F) return;
        boolean hidden = role == null && (roleId == null || roleId.isEmpty());
        int color = role == null ? 0x808080 : role.color() & 0xFFFFFF;
        int width = this.cardWidth(selected ? 1 : this.roleIds.size());
        int top = baseTop - (selected ? 6 : hovered ? 3 : 0);
        int center = left + width / 2;
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(center, 0.0F, 0.0F);
        pose.scale(flip, 1.0F, 1.0F);
        pose.translate(-center, 0.0F, 0.0F);
        graphics.fill(left, top, left + width, top + CARD_HEIGHT, 0xF0141414);
        graphics.fill(left, top, left + width, top + HEADER_HEIGHT, 0xFF000000 | color);
        Component camp = hidden ? Component.translatable("gui.brinswathe.draft.random_title") : this.camp(role);
        graphics.drawCenteredString(this.font, camp, center, top + 5, 0xFFFFFF);
        pose.pushPose();
        pose.translate(center, top + HEADER_HEIGHT + 9, 0.0F);
        pose.scale(1.4F, 1.4F, 1.0F);
        MutableComponent name = hidden
            ? Component.literal("?")
            : role != null ? BrinRoleLabels.of(role) : BrinRoleLabels.of(roleId);
        graphics.drawCenteredString(this.font, name.withColor(0xFFFFFF).withStyle(ChatFormatting.BOLD), 0, 0, 0xFFFFFF);
        pose.popPose();
        graphics.fill(left + 10, top + GOAL_TOP - 6, left + width - 10, top + GOAL_TOP - 5, 0x60FFFFFF);
        Component goal = hidden ? Component.translatable("gui.brinswathe.draft.random_goal") : this.goal(role);
        List<FormattedCharSequence> lines = this.font.split(goal, width - 14);
        int y = top + GOAL_TOP;
        for (int line = 0; line < Math.min(MAX_GOAL_LINES, lines.size()); line++) {
            graphics.drawCenteredString(this.font, lines.get(line), center, y, 0xFFB0B0B0);
            y += LINE_HEIGHT;
        }
        graphics.drawCenteredString(this.font, footer, center, top + CARD_HEIGHT - 13, 0xFFFFFF);
        graphics.renderOutline(left, top, width, CARD_HEIGHT, selected ? GOLD : hovered ? 0xFFFFFFFF : 0xFF000000 | darker(color));
        if (selected) graphics.renderOutline(left - 1, top - 1, width + 2, CARD_HEIGHT + 2, GOLD);
        pose.popPose();
    }

    private void choose(int choice) {
        if (this.sent || !this.myTurn()) return;
        if (choice != NONE && (choice < 0 || choice >= this.roleIds.size())) return;
        this.sent = true;
        if (ClientPlayNetworking.canSend(BrinDraftChoiceC2SPacket.TYPE)) {
            ClientPlayNetworking.send(new BrinDraftChoiceC2SPacket(choice == NONE ? "" : this.roleIds.get(choice)));
        }
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        this.rebuildWidgets();
    }

    private int cardAt(double mouseX, double mouseY) {
        int count = this.roleIds.size();
        int width = this.cardWidth(count);
        int top = this.cardTop();
        if (mouseY < top - 6 || mouseY >= top + CARD_HEIGHT) return NONE;
        for (int i = 0; i < count; i++) {
            int left = this.cardLeft(i, count);
            if (mouseX >= left && mouseX < left + width) return i;
        }
        return NONE;
    }

    private int cardWidth(int count) {
        int cards = Math.max(1, count);
        return Mth.clamp((this.width - 40 - (cards - 1) * CARD_GAP) / cards, 96, 150);
    }

    private int cardLeft(int index, int count) {
        int cards = Math.max(1, count);
        int width = this.cardWidth(cards);
        int rowWidth = cards * width + (cards - 1) * CARD_GAP;
        return (this.width - rowWidth) / 2 + index * (width + CARD_GAP);
    }

    private int contentTop() {
        return Math.max(6, (this.height - (CARDS_OFFSET + CARD_HEIGHT + 28)) / 2);
    }

    private int cardTop() {
        return this.contentTop() + CARDS_OFFSET;
    }

    private Component camp(Role role) {
        if (role == null) return Component.empty();
        if (role.canUseKiller()) return Component.translatable("gui.brinswathe.draft.camp.killer");
        if (role.isInnocent()) return Component.translatable("gui.brinswathe.draft.camp.innocent");
        return Component.translatable("gui.brinswathe.draft.camp.neutral");
    }

    private Component goal(Role role) {
        if (role == null) return Component.empty();
        ResourceLocation id = role.identifier();
        Language language = Language.getInstance();
        String namespaced = "announcement.goals." + id.getNamespace() + "." + id.getPath();
        if (language.has(namespaced)) return Component.translatable(namespaced);
        String plain = "announcement.goals." + id.getPath();
        return language.has(plain) ? Component.translatable(plain) : Component.empty();
    }

    private static int darker(int color) {
        int red = (color >> 16 & 0xFF) * 3 / 5;
        int green = (color >> 8 & 0xFF) * 3 / 5;
        int blue = (color & 0xFF) * 3 / 5;
        return red << 16 | green << 8 | blue;
    }
}
