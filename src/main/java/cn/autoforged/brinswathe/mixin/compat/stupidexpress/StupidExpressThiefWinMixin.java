package cn.autoforged.brinswathe.mixin.compat.stupidexpress;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The thief is a killer sided neutral in this pack, so being the last one standing settles him with the
 * wolves instead of crowning him alone. Dropping the custom winner disables Stupid Express' own victory
 * screen, announcement and {@code didWin} override in one place; the arsonist and the lovers keep theirs.
 */
@Pseudo
@Mixin(targets = "pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent", remap = false)
public abstract class StupidExpressThiefWinMixin {
    @Unique
    private static final String BRIN_THIEF_TEXT_ID = "thief";

    @Shadow
    private String winningTextId;

    @Inject(method = "hasCustomWinner", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void brinDropThiefIndependentWin(CallbackInfoReturnable<Boolean> cir) {
        if (BRIN_THIEF_TEXT_ID.equals(this.winningTextId)) {
            cir.setReturnValue(false);
        }
    }
}
