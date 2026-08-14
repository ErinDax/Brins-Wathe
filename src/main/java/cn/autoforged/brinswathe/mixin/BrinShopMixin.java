package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.BrinShopAccess;
import cn.autoforged.brinswathe.component.BerserkerComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.util.ShopEntry;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.KinsWatheShops;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerShopComponent.class, priority = 1100)
public abstract class BrinShopMixin {
    @Shadow
    public int balance;

    @Shadow
    @Final
    private Player player;

    @Shadow
    public abstract void sync();

    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
    private void brinTryBuy(int index, CallbackInfo callback) {
        GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
        if (game.isRole(this.player, KinsWatheRoles.HUNTER)
            && GameSafeComponent.KEY.get(this.player.level()).isGameSafe) {
            List<ShopEntry> hunterShop = KinsWatheShops.getHunterShop(this.player.level());
            if (index >= 0 && index < hunterShop.size()
                && hunterShop.get(index).stack().is(KinsWatheItems.HUNTING_KNIFE)) {
                callback.cancel();
                return;
            }
        }

        List<ShopEntry> entries = BrinShopAccess.getShopEntries(game, this.player);
        if (entries == null) return;

        if (index >= 0 && index < entries.size()) {
            ShopEntry entry = entries.get(index);

            boolean purchased;
            if (game.isRole(this.player, BrinRoles.BERSERKER)
                && entry.stack().is(WatheItems.PSYCHO_MODE)) {
                BerserkerComponent component = BerserkerComponent.KEY.get(this.player);
                purchased = this.balance >= entry.price()
                    && !this.player.getCooldowns().isOnCooldown(entry.stack().getItem())
                    && component != null
                    && !component.psychoActive
                    && component.activatePsycho();
            } else {
                purchased = this.balance >= entry.price()
                    && !this.player.getCooldowns().isOnCooldown(entry.stack().getItem())
                    && !this.player.getInventory().contains(entry.stack())
                    && entry.onBuy(this.player);
            }

            if (purchased) {
                this.balance -= entry.price();
                this.brinPlayShopSound(WatheSounds.UI_SHOP_BUY);
            } else {
                this.player.displayClientMessage(
                    Component.literal("Purchase Failed").withStyle(ChatFormatting.RED),
                    true
                );
                this.brinPlayShopSound(WatheSounds.UI_SHOP_BUY_FAIL);
            }
            this.sync();
        }

        callback.cancel();
    }

    private void brinPlayShopSound(net.minecraft.sounds.SoundEvent sound) {
        if (this.player instanceof ServerPlayer serverPlayer) {
            serverPlayer.playNotifySound(sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
