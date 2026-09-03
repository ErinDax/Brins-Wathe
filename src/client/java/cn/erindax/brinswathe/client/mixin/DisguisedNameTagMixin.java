package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinsWatheClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(RoleNameRenderer.class)
public abstract class DisguisedNameTagMixin {
    @WrapOperation(
        method = "renderHud",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getDisplayName()Lnet/minecraft/network/chat/Component;"
        )
    )
    private static Component brinDisguisedName(Player player, Operation<Component> original) {
        Component disguised = BrinsWatheClient.disguisedName(player);
        return disguised == null ? original.call(player) : disguised;
    }
}
