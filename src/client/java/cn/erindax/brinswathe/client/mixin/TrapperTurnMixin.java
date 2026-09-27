package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.component.TrapperComponent;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(MouseHandler.class)
public abstract class TrapperTurnMixin {
    @WrapMethod(method = "turnPlayer")
    private void brinAllowTrappedTurn(double movementTime, Operation<Void> original) {
        Minecraft client = Minecraft.getInstance();
        PlayerEffectComponent effect = client.player == null ? null : PlayerEffectComponent.KEY.get(client.player);
        if (effect == null || !TrapperComponent.isTrapStun(effect.stunTicks)) {
            original.call(movementTime);
            return;
        }
        int stunTicks = effect.stunTicks;
        effect.stunTicks = 0;
        try {
            original.call(movementTime);
        } finally {
            effect.stunTicks = stunTicks;
        }
    }
}
