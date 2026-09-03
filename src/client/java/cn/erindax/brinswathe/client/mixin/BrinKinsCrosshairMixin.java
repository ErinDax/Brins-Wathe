package cn.erindax.brinswathe.client.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.BsXinQin.kinswathe.client.mixin.gui.ItemCrosshairMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ItemCrosshairMixin.class, remap = false)
public abstract class BrinKinsCrosshairMixin {
    @Unique
    private static final ResourceLocation KNIFE_ATTACK =
        ResourceLocation.fromNamespaceAndPath("wathe", "hud/knife_attack");

    @WrapWithCondition(
        method = "renderCrosshair",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V",
            remap = true
        )
    )
    private static boolean brinSkipKnifeAttackIcon(
        GuiGraphics graphics,
        ResourceLocation sprite,
        int x,
        int y,
        int width,
        int height
    ) {
        return !KNIFE_ATTACK.equals(sprite);
    }
}
