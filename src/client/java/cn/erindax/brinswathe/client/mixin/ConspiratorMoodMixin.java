package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.BrinConspirator;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.MoodRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Environment(EnvType.CLIENT)
@Mixin(MoodRenderer.class)
public abstract class ConspiratorMoodMixin {
    @ModifyArg(
        method = "renderCivilian",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"
        ),
        index = 0
    )
    private static ResourceLocation brinShowKillerMoodForConspirator(ResourceLocation sprite) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return sprite;
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        return BrinConspirator.isConspirator(game, player) ? MoodRenderer.MOOD_KILLER : sprite;
    }
}
