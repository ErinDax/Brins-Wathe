package cn.erindax.brinswathe.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.game.mapeffect.HarpyExpressTrainMapEffect;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HarpyExpressTrainMapEffect.class)
public abstract class BrinSkipStartingLetterMixin {
    @WrapOperation(
        method = "initializeMapEffects",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;addItem(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean brinSkipStartingLetter(ServerPlayer player, ItemStack stack, Operation<Boolean> original) {
        if (stack.is(WatheItems.LETTER)) return false;
        return original.call(player, stack);
    }
}
