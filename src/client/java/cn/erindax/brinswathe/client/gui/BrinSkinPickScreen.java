package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.BrinKnifeSkins;
import cn.erindax.brinswathe.BrinSkinPicks;
import cn.erindax.brinswathe.network.BrinSkinPickChoiceC2SPacket;
import cn.erindax.brinswathe.network.BrinSkinPickOpenS2CPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.wathe.index.WatheItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class BrinSkinPickScreen extends Screen {
    private static final int REEL_SIZE = 46;
    private static final int REEL_START = 4;
    private static final int REEL_TARGET = 40;
    private static final int SLOT = 44;
    private static final int MAX_SIDE_SLOTS = 4;
    private static final long ROLL_MILLIS = 4600L;
    private static final long TICK_SOUND_GAP = 40L;
    private static final int TEXT_WIDTH = 300;
    private static final int LINE_HEIGHT = 11;
    private static final int GOLD = 0xFFFFD54F;
    private final int token;
    private final String kind;
    private final String offered;
    private final String senderName;
    private final int poolSize;
    private final Map<String, ItemStack> stacks = new HashMap<>();
    private State state = State.CHOOSING;
    private List<String> reel = List.of();
    private String result;
    private int applied;
    private boolean unavailable;
    private boolean announced;
    private long rollStart;
    private long lastTickSound;
    private int lastTickIndex = -1;

    public BrinSkinPickScreen(BrinSkinPickOpenS2CPacket payload) {
        super(Component.translatable("screen.brinswathe.skin_pick"));
        this.token = payload.token();
        this.kind = payload.kind();
        this.offered = payload.skin();
        this.senderName = payload.senderName();
        this.poolSize = payload.poolSize();
    }

    public int token() {
        return this.token;
    }

    public void onResult(int status, String skin, int appliedCount) {
        if (status == BrinSkinPicks.RESULT_UNAVAILABLE) {
            this.unavailable = true;
            this.state = State.DONE;
            this.rebuildWidgets();
            return;
        }
        if (status != BrinSkinPicks.RESULT_DRAWN || this.state != State.WAITING) return;
        this.result = skin;
        this.applied = appliedCount;
        this.reel = this.reelNames(skin);
        this.rollStart = Util.getMillis();
        this.lastTickSound = 0L;
        this.lastTickIndex = -1;
        this.state = State.ROLLING;
        this.rebuildWidgets();
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int top = this.height / 2 + 14;
        if (this.state == State.CHOOSING) {
            this.addRenderableWidget(Button.builder(
                    Component.translatable("gui.brinswathe.skin_pick.accept"),
                    button -> this.accept())
                .bounds(center - 114, top, 110, 20)
                .build());
            Button draw = this.addRenderableWidget(Button.builder(
                    Component.translatable("gui.brinswathe.skin_pick.draw"),
                    button -> this.draw())
                .bounds(center + 4, top, 110, 20)
                .build());
            draw.active = this.poolSize >= 2;
        } else if (this.state == State.DONE) {
            this.addRenderableWidget(Button.builder(
                    Component.translatable("gui.brinswathe.skin_pick.done"),
                    button -> this.onClose())
                .bounds(center - 55, top, 110, 20)
                .build());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.state == State.ROLLING) this.advance(Util.getMillis());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        if (this.state == State.ROLLING) this.advance(now);
        super.render(graphics, mouseX, mouseY, partialTick);
        int center = this.width / 2;
        int middle = this.height / 2;
        graphics.drawCenteredString(this.font, this.title, center, middle - 96, GOLD);
        graphics.drawCenteredString(this.font, Component.translatable(
            "gui.brinswathe.skin_pick.from",
            Component.literal(this.senderName).withStyle(ChatFormatting.YELLOW),
            BrinSkinPicks.typeLabel(this.kind)
        ), center, middle - 82, 0xFFFFFF);
        double position = this.reelPosition(now);
        if (this.reel.isEmpty()) {
            this.renderOffered(graphics, center, middle - 44);
        } else {
            this.renderReel(graphics, center, middle - 66, position);
        }
        graphics.drawCenteredString(this.font, this.nameLine(position), center, middle - 14, 0xFFFFFF);
        Component status = this.statusLine();
        if (status != null) graphics.drawCenteredString(this.font, status, center, middle - 2, 0xFFFFFF);
        int width = Math.max(120, Math.min(TEXT_WIDTH, this.width - 20));
        int y = middle + 44;
        for (Component hint : this.hints()) {
            for (FormattedCharSequence part : this.font.split(hint, width)) {
                graphics.drawCenteredString(this.font, part, center, y, 0xFFFFFF);
                y += LINE_HEIGHT;
            }
            y += 3;
        }
    }

    @Override
    public void removed() {
        if (this.state == State.CHOOSING) {
            this.send(BrinSkinPicks.ACTION_CANCEL);
        } else if (this.state == State.ROLLING) {
            this.announce();
        }
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void accept() {
        if (this.state != State.CHOOSING) return;
        this.state = State.WAITING;
        this.send(BrinSkinPicks.ACTION_ACCEPT);
        this.onClose();
    }

    private void draw() {
        if (this.state != State.CHOOSING || this.poolSize < 2) return;
        this.state = State.WAITING;
        this.send(BrinSkinPicks.ACTION_DRAW);
        this.rebuildWidgets();
    }

    private void send(int action) {
        if (!ClientPlayNetworking.canSend(BrinSkinPickChoiceC2SPacket.TYPE)) return;
        ClientPlayNetworking.send(new BrinSkinPickChoiceC2SPacket(this.token, action));
    }

    private void advance(long now) {
        int index = (int) Math.floor(this.reelPosition(now) + 0.5);
        if (index != this.lastTickIndex) {
            this.lastTickIndex = index;
            if (now - this.lastTickSound >= TICK_SOUND_GAP) {
                this.lastTickSound = now;
                this.playSound(SimpleSoundInstance.forUI(
                    SoundEvents.NOTE_BLOCK_HAT,
                    0.8F + 0.7F * (float) this.progress(now)
                ));
            }
        }
        if (now - this.rollStart >= ROLL_MILLIS) this.finishRoll();
    }

    private void finishRoll() {
        this.state = State.DONE;
        this.playSound(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0F, 0.6F));
        this.announce();
        this.rebuildWidgets();
    }

    private void announce() {
        if (this.announced || this.result == null || this.minecraft == null || this.minecraft.player == null) return;
        this.announced = true;
        this.minecraft.player.displayClientMessage(
            BrinSkinPicks.appliedMessage(this.kind, this.displayName(this.result), this.applied, true),
            false
        );
    }

    private void playSound(SoundInstance sound) {
        if (this.minecraft != null) this.minecraft.getSoundManager().play(sound);
    }

    private double progress(long now) {
        return Mth.clamp((now - this.rollStart) / (double) ROLL_MILLIS, 0.0, 1.0);
    }

    private double reelPosition(long now) {
        if (this.reel.isEmpty()) return 0.0;
        if (this.state != State.ROLLING) return REEL_TARGET;
        double eased = 1.0 - Math.pow(1.0 - this.progress(now), 4.0);
        return REEL_START + eased * (REEL_TARGET - REEL_START);
    }

    private List<String> reelNames(String target) {
        List<String> pool = BrinKnifeSkins.drawPool(this.kind);
        if (!pool.contains(target)) pool.add(target);
        RandomSource random = RandomSource.create();
        List<String> names = new ArrayList<>(REEL_SIZE);
        for (int i = 0; i < REEL_SIZE; i++) {
            if (i == REEL_TARGET) {
                names.add(target);
                continue;
            }
            List<String> options = new ArrayList<>(pool);
            if (i > 0) options.remove(names.get(i - 1));
            if (i == REEL_TARGET - 1 && options.size() > 1) options.remove(target);
            if (options.isEmpty()) options = pool;
            names.add(options.get(random.nextInt(options.size())));
        }
        return names;
    }

    private void renderOffered(GuiGraphics graphics, int center, int middle) {
        int half = 27;
        graphics.fill(center - half, middle - half, center + half, middle + half, 0xC0101010);
        graphics.renderOutline(center - half, middle - half, half * 2, half * 2, GOLD);
        this.renderStack(graphics, this.stack(this.offered), center, middle, 3.0F);
    }

    private void renderReel(GuiGraphics graphics, int center, int top, double position) {
        int side = Mth.clamp((this.width / 2 - SLOT / 2 - 10) / SLOT, 1, MAX_SIDE_SLOTS);
        int half = side * SLOT + SLOT / 2;
        int left = center - half;
        int right = center + half;
        int bottom = top + SLOT;
        graphics.fill(left - 2, top - 2, right + 2, bottom + 2, 0xC0101010);
        graphics.enableScissor(left, top, right, bottom);
        int first = Math.max(0, (int) Math.floor(position) - side - 1);
        int last = Math.min(this.reel.size() - 1, (int) Math.ceil(position) + side + 1);
        for (int i = first; i <= last; i++) {
            float slotCenter = (float) (center + (i - position) * SLOT);
            int slotLeft = Math.round(slotCenter - SLOT / 2.0F);
            graphics.fill(slotLeft + 2, top + 2, slotLeft + SLOT - 2, bottom - 2, 0x30FFFFFF);
            this.renderStack(graphics, this.stack(this.reel.get(i)), slotCenter, top + SLOT / 2.0F, 2.0F);
        }
        graphics.disableScissor();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, 400.0F);
        graphics.renderOutline(center - SLOT / 2 - 1, top - 1, SLOT + 2, SLOT + 2, GOLD);
        graphics.renderOutline(center - SLOT / 2, top, SLOT, SLOT, GOLD);
        pose.popPose();
    }

    private void renderStack(GuiGraphics graphics, ItemStack stack, float centerX, float centerY, float scale) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(centerX - 8.0F * scale, centerY - 8.0F * scale, 0.0F);
        pose.scale(scale, scale, scale);
        graphics.renderItem(stack, 0, 0);
        pose.popPose();
    }

    private ItemStack stack(String skin) {
        return this.stacks.computeIfAbsent(skin == null ? "" : skin, name -> {
            ItemStack stack = new ItemStack("gun".equalsIgnoreCase(this.kind) ? WatheItems.REVOLVER : WatheItems.KNIFE);
            if (!name.isEmpty()) BrinKnifeSkins.applySkin(stack, this.kind, name);
            return stack;
        });
    }

    private String displayName(String skin) {
        return BrinKnifeSkins.displayName(this.kind, skin);
    }

    private Component nameLine(double position) {
        if (this.state == State.ROLLING) {
            int index = Mth.clamp((int) Math.floor(position + 0.5), 0, this.reel.size() - 1);
            return Component.literal(this.displayName(this.reel.get(index))).withStyle(ChatFormatting.GRAY);
        }
        if (this.state == State.DONE && this.result != null) {
            return Component.translatable(
                "gui.brinswathe.skin_pick.result",
                Component.literal(this.displayName(this.result)).withStyle(ChatFormatting.YELLOW)
            ).withStyle(ChatFormatting.GOLD);
        }
        return Component.literal(this.displayName(this.offered)).withStyle(ChatFormatting.YELLOW);
    }

    private Component statusLine() {
        if (this.state == State.WAITING) {
            return Component.translatable("gui.brinswathe.skin_pick.waiting").withStyle(ChatFormatting.GRAY);
        }
        if (this.state != State.DONE) return null;
        if (this.unavailable) {
            return Component.translatable("gui.brinswathe.skin_pick.unavailable").withStyle(ChatFormatting.RED);
        }
        if (this.applied <= 0) return BrinSkinPicks.nothingStatus(this.kind, this.displayName(this.result));
        return Component.translatable("gui.brinswathe.skin_pick.equipped." + this.kindKey())
            .withStyle(ChatFormatting.GREEN);
    }

    private List<Component> hints() {
        List<Component> hints = new ArrayList<>();
        if (this.state == State.CHOOSING) {
            hints.add(this.poolSize >= 2
                ? Component.translatable(
                    "gui.brinswathe.skin_pick.draw_hint",
                    this.poolSize,
                    BrinSkinPicks.typeLabel(this.kind)
                ).withStyle(ChatFormatting.GRAY)
                : Component.translatable("gui.brinswathe.skin_pick.draw_disabled").withStyle(ChatFormatting.GRAY));
        }
        if (!this.unavailable) {
            hints.add(Component.translatable("gui.brinswathe.skin_pick.note." + this.kindKey())
                .withStyle(ChatFormatting.GRAY));
        }
        return hints;
    }

    private String kindKey() {
        return "gun".equalsIgnoreCase(this.kind) ? "gun" : "knife";
    }

    private enum State {
        CHOOSING,
        WAITING,
        ROLLING,
        DONE
    }
}
