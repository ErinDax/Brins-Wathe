package cn.erindax.brinswathe.client.gui;

import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class BrinRoleSelectWidget extends Button {
    private final ItemStack icon;
    private final Component label;

    public BrinRoleSelectWidget(
        LimitedInventoryScreen screen,
        int x,
        int y,
        Item item,
        Component label,
        String commandId
    ) {
        super(x, y, 16, 16, Component.empty(), button -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                client.player.connection.sendCommand("iWantBe " + commandId);
                screen.onClose();
            }
        }, DEFAULT_NARRATION);
        this.icon = item.getDefaultInstance();
        this.label = label;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.blitSprite(ShopEntry.Type.WEAPON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
        graphics.renderItem(this.icon, this.getX(), this.getY());
        if (this.isHovered()) {
            int color = -1862287543;
            graphics.fillGradient(RenderType.guiOverlay(), this.getX(), this.getY(), this.getX() + 16, this.getY() + 14, color, color, 0);
            graphics.fillGradient(RenderType.guiOverlay(), this.getX(), this.getY() + 14, this.getX() + 15, this.getY() + 15, color, color, 0);
            graphics.fillGradient(RenderType.guiOverlay(), this.getX(), this.getY() + 15, this.getX() + 14, this.getY() + 16, color, color, 0);
            graphics.renderTooltip(Minecraft.getInstance().font, this.label, mouseX, mouseY);
        }
    }
}
