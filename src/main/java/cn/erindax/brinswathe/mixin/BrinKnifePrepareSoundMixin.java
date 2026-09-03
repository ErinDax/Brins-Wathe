package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinSounds;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import dev.doctor4t.wathe.item.KnifeItem;

@Mixin(KnifeItem.class)
public abstract class BrinKnifePrepareSoundMixin {
    @WrapOperation(
        method = "use",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"
        )
    )
    private void brinPlaySkinPrepareSound(
        Player user,
        SoundEvent sound,
        float volume,
        float pitch,
        Operation<Void> original
    ) {
        ItemStack stack = user.getUseItem();
        if (stack.isEmpty()) stack = user.getMainHandItem();
        original.call(user, BrinSounds.knifePrepare(stack), BrinSounds.knifePrepareVolume(stack), pitch);
    }
}
