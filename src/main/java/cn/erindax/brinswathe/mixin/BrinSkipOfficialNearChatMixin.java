package cn.erindax.brinswathe.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "dev.doctor4t.wathe.Wathe", remap = false)
public abstract class BrinSkipOfficialNearChatMixin {
    @WrapOperation(
        method = "onInitialize",
        at = @At(
            value = "INVOKE",
            target = "Lnet/fabricmc/fabric/api/event/Event;register(Ljava/lang/Object;)V"
        ),
        require = 0
    )
    private void brinSkipOfficialAllowChat(Event<?> event, Object listener, Operation<Void> original) {
        if (event == ServerMessageEvents.ALLOW_CHAT_MESSAGE) {
            return;
        }
        original.call(event, listener);
    }
}
