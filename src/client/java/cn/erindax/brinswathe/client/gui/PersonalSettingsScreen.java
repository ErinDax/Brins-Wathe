package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.client.AdminClient;
import cn.erindax.brinswathe.client.musicbox.MusicBoxClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class PersonalSettingsScreen extends Screen {
    private int top;

    public PersonalSettingsScreen() {
        super(Component.translatable("screen.brinswathe.personal_settings"));
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        boolean admin = AdminClient.canOpen(this.minecraft);
        this.top = this.height / 2 - (admin ? 58 : 46);
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.brinswathe.settings.buy_slot"),
                button -> this.minecraft.setScreen(new BuySlotScreen(this)))
            .bounds(center - 100, this.top, 200, 20)
            .build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.brinswathe.settings.item_slots"),
                button -> this.minecraft.setScreen(new ItemSlotScreen(this)))
            .bounds(center - 100, this.top + 24, 200, 20)
            .build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.brinswathe.settings.music_box"),
                button -> MusicBoxClient.openScreen(this.minecraft, this))
            .bounds(center - 100, this.top + 48, 200, 20)
            .build());
        int doneTop = this.top + 80;
        if (admin) {
            this.addRenderableWidget(Button.builder(
                    Component.translatable("gui.brinswathe.settings.admin"),
                    button -> AdminClient.open())
                .bounds(center - 100, this.top + 72, 200, 20)
                .build());
            doneTop += 24;
        }
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
            .bounds(center - 100, doneTop, 200, 20)
            .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.top - 26, 0xFFD54F);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
