package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinExecutioner;
import cn.erindax.brinswathe.BrinModifiers;
import cn.erindax.brinswathe.BrinNoelleAccess;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.GunShootPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GunShootPayload.Receiver.class)
public abstract class BrinGunShootIcMixin {
    @WrapOperation(
        method = "receive(Ldev/doctor4t/wathe/util/GunShootPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;isInnocent(Lnet/minecraft/world/entity/player/Player;)Z"
        )
    )
    private boolean brinSkipBackfireForUnethical(
        GameWorldComponent game,
        Player target,
        Operation<Boolean> original,
        GunShootPayload payload,
        ServerPlayNetworking.Context context
    ) {
        ServerPlayer shooter = context.player();
        if (BrinModifiers.hasModifier(shooter, BrinModifiers.UNETHICAL)) {
            return false;
        }
        if (BrinNoelleAccess.isRole(game, target, BrinNoelleAccess.MIMIC_ID)) {
            return false;
        }
        if (BrinExecutioner.isInRound(game)) {
            return false;
        }
        return original.call(game, target);
    }

    @WrapOperation(
        method = "receive(Ldev/doctor4t/wathe/util/GunShootPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"
        )
    )
    private void brinHalveMarksmanGunCooldown(
        ItemCooldowns cooldowns,
        Item item,
        int ticks,
        Operation<Void> original,
        GunShootPayload payload,
        ServerPlayNetworking.Context context
    ) {
        ServerPlayer shooter = context.player();
        if (shooter.isCreative()) return;
        int applied = ticks;
        if (BrinModifiers.hasModifier(shooter, BrinModifiers.MARKSMAN)) {
            applied = ticks / 2;
        }
        if (applied > 0) original.call(cooldowns, item, applied);
    }
}
