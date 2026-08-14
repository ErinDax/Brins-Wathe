package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.entity.ArchivistSealedCorpse;
import cn.autoforged.brinswathe.entity.BoneharvestedCorpse;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.BsXinQin.kinswathe.items.SulfuricAcidBarrelItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SulfuricAcidBarrelItem.class)
public abstract class ArchivistCleanerDissolveMixin {
    @Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
    private void brinPreventCleanerDissolve(
        ItemStack stack,
        Player player,
        LivingEntity target,
        InteractionHand hand,
        CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (target instanceof PlayerBodyEntity body
            && (((ArchivistSealedCorpse) body).brin$isArchivistSealed()
                || ((BoneharvestedCorpse) body).brin$isBoneharvested())) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
