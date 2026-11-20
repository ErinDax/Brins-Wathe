package cn.erindax.brinswathe.client.mixin;

import dev.doctor4t.wathe.client.gui.CrosshairRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CrosshairRenderer.class)
public abstract class KinsCrosshairMixin {
    @Unique
    private static final ResourceLocation KNIFE_ATTACK_ICON =
        ResourceLocation.fromNamespaceAndPath("wathe", "hud/knife_attack");

    @Unique
    private static void brinDrawUnlessKnifeAttack(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        if (KNIFE_ATTACK_ICON.equals(sprite)) return;
        graphics.blitSprite(sprite, x, y, width, height);
    }
}
