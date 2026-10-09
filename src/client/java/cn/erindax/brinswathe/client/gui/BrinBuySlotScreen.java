package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.BrinBuySlots;
import cn.erindax.brinswathe.client.BrinPersonalSettingsClient;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public final class BrinBuySlotScreen extends Screen {
    private static final int SLOT_SIZE = 20;
    private static final int SLOT_GAP = 4;
    private static final int TEXT_WIDTH = 300;
    private final Screen parent;
    private final List<Button> slotButtons = new ArrayList<>();
    private Button defaultButton;

    public BrinBuySlotScreen(Screen parent) {
        super(Component.translatable("screen.brinswathe.buy_slot"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.slotButtons.clear();
        int center = this.width / 2;
        int top = this.height / 2 - 4;
        int rowWidth = BrinBuySlots.HOTBAR_SIZE * SLOT_SIZE + (BrinBuySlots.HOTBAR_SIZE - 1) * SLOT_GAP;
        int left = center - rowWidth / 2;
        for (int slot = 1; slot <= BrinBuySlots.HOTBAR_SIZE; slot++) {
            int value = slot;
            this.slotButtons.add(this.addRenderableWidget(Button.builder(Component.empty(), button -> this.select(value))
                .bounds(left + (slot - 1) * (SLOT_SIZE + SLOT_GAP), top, SLOT_SIZE, SLOT_SIZE)
                .build()));
        }
        this.defaultButton = this.addRenderableWidget(Button.builder(Component.empty(), button -> this.select(0))
            .bounds(center - 100, top + 28, 200, 20)
            .build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose())
            .bounds(center - 100, top + 56, 200, 20)
            .build());
        this.refresh();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int center = this.width / 2;
        int width = Math.max(120, Math.min(TEXT_WIDTH, this.width - 20));
        int y = this.height / 2 - 70;
        graphics.drawCenteredString(this.font, this.title, center, y, 0xFFD54F);
        y += 16;
        for (FormattedCharSequence line : this.font.split(Component.translatable("gui.brinswathe.buy_slot.hint"), width)) {
            graphics.drawCenteredString(this.font, line, center, y, 0xFFFFFF);
            y += 11;
        }
        int current = BrinPersonalSettingsClient.buySlot();
        Component state = current == 0
            ? Component.translatable("gui.brinswathe.buy_slot.current_default")
            : Component.translatable("gui.brinswathe.buy_slot.current", current);
        graphics.drawCenteredString(this.font, state.copy().withStyle(ChatFormatting.YELLOW), center, y + 3, 0xFFFFFF);
        if (!BrinPersonalSettingsClient.serverSupportsBuySlot()) {
            graphics.drawCenteredString(
                this.font,
                Component.translatable("gui.brinswathe.buy_slot.server_missing").withStyle(ChatFormatting.GRAY),
                center,
                this.height / 2 + 82,
                0xFFFFFF
            );
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void select(int slot) {
        BrinPersonalSettingsClient.setBuySlot(slot);
        this.refresh();
    }

    private void refresh() {
        int current = BrinPersonalSettingsClient.buySlot();
        for (int index = 0; index < this.slotButtons.size(); index++) {
            int slot = index + 1;
            this.slotButtons.get(index).setMessage(slot == current
                ? Component.literal("[" + slot + "]").withStyle(ChatFormatting.YELLOW)
                : Component.literal(Integer.toString(slot)));
        }
        Component label = Component.translatable("gui.brinswathe.buy_slot.default");
        this.defaultButton.setMessage(current == 0 ? label.copy().withStyle(ChatFormatting.YELLOW) : label);
    }
}
