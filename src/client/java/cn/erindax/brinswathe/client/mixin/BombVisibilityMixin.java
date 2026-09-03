package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.BrinItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandLayer.class)
public abstract class BombVisibilityMixin {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void brinHideBombFromOthers(LivingEntity holder, ItemStack stack,
                                        ItemDisplayContext displayContext, HumanoidArm arm,
                                        PoseStack poseStack, MultiBufferSource multiBufferSource,
                                        int packedLight, CallbackInfo ci) {
        if (!stack.is(BrinItems.BOMB) && !stack.is(BrinItems.MINE)) return;
        if (holder == Minecraft.getInstance().player) return;
        ci.cancel();
    }
}
