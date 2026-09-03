package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.BrinShopAccess;
import cn.erindax.brinswathe.component.AvengerComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.index.WatheItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemPickupMixin {

    @Shadow
    public abstract net.minecraft.world.item.ItemStack getItem();

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void brinPreventRevolverPickup(Player player, CallbackInfo ci) {
        if (player.level().isClientSide) return;

        ItemStack stack = getItem();
        if (stack.is(WatheItems.KEY) && brinMergeKeyLore(player, stack)) {
            player.playSound(
                SoundEvents.ITEM_PICKUP,
                1.0F,
                ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
            );
            ((ItemEntity) (Object) this).discard();
            ci.cancel();
            return;
        }

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (gameWorld.isRole(player, BrinRoles.BERSERKER)
            || gameWorld.isRole(player, BrinRoles.NIGHTMARE)
            || gameWorld.isRole(player, BrinRoles.MEDIUM)) {
            if (getItem().is(WatheItems.REVOLVER)) {
                ci.cancel();
            }
        }

        if (gameWorld.isRole(player, BrinRoles.WATCHMAN)
            && BrinShopAccess.isSurvivalExpertExcludedItem(getItem())) {
            ci.cancel();
        }

        if (gameWorld.isRole(player, BrinRoles.AVENGER) && BrinShopAccess.isFirearm(getItem())) {
            AvengerComponent avenger = AvengerComponent.KEY.get(player);
            if (avenger == null || !avenger.hasGunWindow()) {
                ci.cancel();
            }
        }
    }

    private static boolean brinMergeKeyLore(Player player, ItemStack newStack) {
        ItemLore newLore = newStack.get(DataComponents.LORE);
        if (newLore == null || newLore.lines().isEmpty()) return false;
        for (ItemStack inv : player.getInventory().items) {
            if (inv.isEmpty() || !inv.is(WatheItems.KEY) || inv == newStack) continue;
            ItemLore invLore = inv.get(DataComponents.LORE);
            if (invLore == null) continue;
            List<Component> lines = new ArrayList<>(invLore.lines());
            boolean changed = false;
            for (Component line : newLore.lines()) {
                String roomName = line.getString();
                if (lines.stream().noneMatch(existing -> existing.getString().equals(roomName))) {
                    lines.add(Component.literal(roomName).setStyle(line.getStyle()));
                    changed = true;
                }
            }
            if (changed) {
                inv.set(DataComponents.LORE, new ItemLore(lines));
            }
            return true;
        }
        return false;
    }
}
