package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.component.SubverterComponent;
import cn.erindax.brinswathe.network.BrinAbilityC2SPacket;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class BrinSubverterScreen extends Screen {
    private final UUID targetId;
    private int top;

    public BrinSubverterScreen(UUID targetId, Component targetName) {
        super(Component.translatable("screen.brinswathe.subverter", targetName));
        this.targetId = targetId;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        this.top = this.height / 2 - 34;
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.brinswathe.subverter.killer"),
                button -> this.choose(BrinAbilityC2SPacket.ABILITY_SUBVERT_KILLER))
            .bounds(center - 100, this.top, 200, 20)
            .build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.brinswathe.subverter.civilian"),
                button -> this.choose(BrinAbilityC2SPacket.ABILITY_SUBVERT_CIVILIAN))
            .bounds(center - 100, this.top + 24, 200, 20)
            .build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose())
            .bounds(center - 100, this.top + 56, 200, 20)
            .build());
    }

    @Override
    public void tick() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        SubverterComponent subverter = SubverterComponent.KEY.get(this.minecraft.player);
        if (subverter == null || !subverter.isPending()) this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.top - 26, BrinRoles.SUBVERTER.color());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void choose(int action) {
        ClientPlayNetworking.send(new BrinAbilityC2SPacket(action, this.targetId));
        this.onClose();
    }
}
