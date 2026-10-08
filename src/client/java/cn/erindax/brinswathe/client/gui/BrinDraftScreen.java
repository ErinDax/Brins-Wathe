package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.client.BrinRoleLabels;
import cn.erindax.brinswathe.network.BrinDraftChoiceC2SPacket;
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
    private static final int RANDOM = -2;
    private static final int GOLD = 0xFFFFD54F;
    private static final int CARD_HEIGHT = 136;
    private static final int CARD_GAP = 12;
    private static final int HEADER_HEIGHT = 18;
    private static final int GOAL_TOP = 50;
    private static final int LINE_HEIGHT = 10;
    private static final int MAX_GOAL_LINES = 7;
    private static final long FLIP_MILLIS = 260L;
    private static final long FLIP_STAGGER = 120L;
    private final List<String> roleIds;
    private final List<Role> roles = new ArrayList<>();
    private final long openedAt;
    private final long deadline;
    private int chosen = NONE;
    private int decided;
    private int total;
    private boolean finished;

    public BrinDraftScreen(List<String> roleIds, int seconds) {
        super(Component.translatable("screen.brinswathe.draft"));
        this.roleIds = List.copyOf(roleIds);
        for (String roleId : this.roleIds) this.roles.add(BrinRoleLabels.find(roleId));
        this.openedAt = Util.getMillis();
        this.deadline = this.openedAt + seconds * 1000L;
    }

    public void finish() {
        this.finished = true;
        this.onClose();
    }

    public void updateProgress(int decided, int total) {
        this.decided = decided;
        this.total = total;
    }

    @Override
    protected void init() {
        int width = 150;
        Button random = this.addRenderableWidget(Button.builder(
                Component.translatable(this.chosen == RANDOM
                    ? "gui.brinswathe.draft.random_picked"
                    : "gui.brinswathe.draft.random"),
                pressed -> this.choose(RANDOM))
            .bounds(this.width / 2 - width / 2, this.cardTop() + CARD_HEIGHT + 8, width, 20)
            .build());
        random.active = this.chosen != RANDOM;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        long now = Util.getMillis();
        int hovered = this.cardAt(mouseX, mouseY);
        for (int i = 0; i < this.roleIds.size(); i++) this.renderCard(graphics, i, i == hovered, now);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int center = this.width / 2;
        int top = this.cardTop();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(center, top - 36, 0.0F);
        pose.scale(1.5F, 1.5F, 1.0F);
        graphics.drawCenteredString(this.font, this.title, 0, 0, GOLD);
        pose.popPose();
        long remaining = Math.max(0L, (this.deadline - Util.getMillis() + 999L) / 1000L);
        graphics.drawCenteredString(
            this.font,
            Component.translatable("gui.brinswathe.draft.timer", remaining)
                .withStyle(remaining <= 5 ? ChatFormatting.RED : ChatFormatting.YELLOW),
            center,
            top - 18,
            0xFFFFFF
        );
        int y = top + CARD_HEIGHT + 34;
        for (Component line : this.statusLines()) {
            for (FormattedCharSequence part : this.font.split(line, Math.max(120, this.width - 40))) {
                graphics.drawCenteredString(this.font, part, center, y, 0xFFFFFF);
                y += LINE_HEIGHT + 1;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
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
        return this.finished || this.chosen != NONE;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderCard(GuiGraphics graphics, int index, boolean hovered, long now) {
        float flip = Mth.clamp((now - this.openedAt - index * FLIP_STAGGER) / (float) FLIP_MILLIS, 0.0F, 1.0F);
        if (flip <= 0.0F) return;
        boolean selected = index == this.chosen;
        Role role = this.roles.get(index);
        int color = role == null ? 0x808080 : role.color() & 0xFFFFFF;
        int width = this.cardWidth();
        int left = this.cardLeft(index);
        int top = this.cardTop() - (selected ? 6 : hovered ? 3 : 0);
        int center = left + width / 2;
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(center, 0.0F, 0.0F);
        pose.scale(flip, 1.0F, 1.0F);
        pose.translate(-center, 0.0F, 0.0F);
        graphics.fill(left, top, left + width, top + CARD_HEIGHT, 0xF0141414);
        graphics.fill(left, top, left + width, top + HEADER_HEIGHT, 0xFF000000 | color);
        graphics.drawCenteredString(this.font, this.camp(role), center, top + 5, 0xFFFFFF);
        pose.pushPose();
        pose.translate(center, top + HEADER_HEIGHT + 9, 0.0F);
        pose.scale(1.4F, 1.4F, 1.0F);
        graphics.drawCenteredString(this.font, this.roleName(index), 0, 0, 0xFFFFFF);
        pose.popPose();
        graphics.fill(left + 10, top + GOAL_TOP - 6, left + width - 10, top + GOAL_TOP - 5, 0x60FFFFFF);
        List<FormattedCharSequence> lines = this.font.split(this.goal(role), width - 14);
        int y = top + GOAL_TOP;
        for (int line = 0; line < Math.min(MAX_GOAL_LINES, lines.size()); line++) {
            graphics.drawCenteredString(this.font, lines.get(line), center, y, 0xFFB0B0B0);
            y += LINE_HEIGHT;
        }
        Component footer = selected
            ? Component.translatable("gui.brinswathe.draft.picked").withStyle(ChatFormatting.GOLD)
            : Component.translatable("gui.brinswathe.draft.click").withStyle(ChatFormatting.DARK_GRAY);
        graphics.drawCenteredString(this.font, footer, center, top + CARD_HEIGHT - 13, 0xFFFFFF);
        graphics.renderOutline(left, top, width, CARD_HEIGHT, selected ? GOLD : hovered ? 0xFFFFFFFF : 0xFF000000 | darker(color));
        if (selected) graphics.renderOutline(left - 1, top - 1, width + 2, CARD_HEIGHT + 2, GOLD);
        pose.popPose();
    }

    private List<Component> statusLines() {
        List<Component> lines = new ArrayList<>(2);
        if (this.total > 0) {
            lines.add(Component.translatable("gui.brinswathe.draft.progress", this.decided, this.total)
                .withStyle(ChatFormatting.WHITE));
        }
        if (this.chosen >= 0) {
            lines.add(Component.translatable("gui.brinswathe.draft.waiting", this.roleName(this.chosen))
                .withStyle(ChatFormatting.GREEN));
        } else if (this.chosen == RANDOM) {
            lines.add(Component.translatable("gui.brinswathe.draft.waiting_random").withStyle(ChatFormatting.GREEN));
        } else {
            lines.add(Component.translatable("gui.brinswathe.draft.hint").withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }

    private void choose(int choice) {
        if (choice != RANDOM && (choice < 0 || choice >= this.roleIds.size())) return;
        if (choice == this.chosen) return;
        this.chosen = choice;
        if (ClientPlayNetworking.canSend(BrinDraftChoiceC2SPacket.TYPE)) {
            ClientPlayNetworking.send(new BrinDraftChoiceC2SPacket(choice == RANDOM ? "" : this.roleIds.get(choice)));
        }
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        this.rebuildWidgets();
    }

    private int cardAt(double mouseX, double mouseY) {
        int width = this.cardWidth();
        int top = this.cardTop();
        if (mouseY < top - 6 || mouseY >= top + CARD_HEIGHT) return NONE;
        for (int i = 0; i < this.roleIds.size(); i++) {
            int left = this.cardLeft(i);
            if (mouseX >= left && mouseX < left + width) return i;
        }
        return NONE;
    }

    private int cardWidth() {
        int count = Math.max(1, this.roleIds.size());
        return Mth.clamp((this.width - 40 - (count - 1) * CARD_GAP) / count, 96, 150);
    }

    private int cardLeft(int index) {
        int count = this.roleIds.size();
        int total = count * this.cardWidth() + (count - 1) * CARD_GAP;
        return (this.width - total) / 2 + index * (this.cardWidth() + CARD_GAP);
    }

    private int cardTop() {
        return Math.max(40, (this.height - CARD_HEIGHT) / 2 - 6);
    }

    private MutableComponent roleName(int index) {
        Role role = this.roles.get(index);
        MutableComponent name = role != null ? BrinRoleLabels.of(role) : BrinRoleLabels.of(this.roleIds.get(index));
        return name.withColor(0xFFFFFF).withStyle(ChatFormatting.BOLD);
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
