package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.Vagrant;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class VagrantSleepRenderMixin {
    @Unique
    private static final float GROUND_SLEEP_LIFT = 0.125F;

    @Inject(method = "setupRotations", at = @At("HEAD"))
    private void brinLiftGroundSleeper(LivingEntity entity, PoseStack poseStack, float bob, float yBodyRot,
                                       float partialTick, float scale, CallbackInfo ci) {
        if (entity instanceof Player player && Vagrant.keepsGroundSleep(player)) {
            poseStack.translate(0.0F, GROUND_SLEEP_LIFT, 0.0F);
        }
    }
}
