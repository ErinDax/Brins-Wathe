package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.ModModifiers;
import cn.erindax.brinswathe.NoelleAccess;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerPoisonComponent.class, priority = 900)
public abstract class Fast2FastArmorMixin {
    @Shadow
    @Final
    private Player player;

    @Shadow
    public abstract void reset();

    @Inject(method = "setPoisonTicks", at = @At("HEAD"), cancellable = true)
    private void brinStackBartenderArmorWithFast2Fast(int ticks, UUID poisoner, CallbackInfo ci) {
        if (this.player.level().isClientSide || poisoner == null) return;
        if (!ModModifiers.hasModifier(this.player, ModModifiers.FAST2FAST)) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
        Role bartender = NoelleAccess.findRole(NoelleAccess.BARTENDER_ID);
        if (bartender == null || !game.isRole(poisoner, bartender)) return;
        NoelleAccess.addBartenderArmor(this.player);
        this.reset();
        ci.cancel();
    }
}
