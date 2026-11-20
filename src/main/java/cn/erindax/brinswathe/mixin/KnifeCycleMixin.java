package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.KnifeSkins;
import cn.erindax.brinswathe.SkinEditors;
import dev.doctor4t.wathe.index.WatheCosmetics;
import dev.doctor4t.wathe.item.KnifeItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KnifeItem.class)
public abstract class KnifeCycleMixin {
    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void brinCycleRegisteredSkins(
        ItemStack stack,
        ItemStack other,
        Slot slot,
        ClickAction clickType,
        Player player,
        SlotAccess access,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (clickType != ClickAction.SECONDARY || !other.isEmpty()) return;
        if (!SkinEditors.canEdit(player)) {
            cir.setReturnValue(true);
            return;
        }

        String current = KnifeSkins.resolveKnifeSkinName(WatheCosmetics.getSkin(stack));
        String next = KnifeSkins.nextName(current);
        WatheCosmetics.setSkin(player, stack, next);
        KnifeSkins.applySkin(stack, "knife", next);
        if (player instanceof ServerPlayer serverPlayer) {
            KnifeSkins.setKnifeSkin(serverPlayer.getUUID(), next);
        }
        cir.setReturnValue(true);
    }
}
