package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.BrinShopAccess;
import cn.erindax.brinswathe.config.BrinConfig;
import cn.erindax.brinswathe.network.BrinConfigS2CPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModdedMurderGameMode.class, priority = 800)
public abstract class BrinInitialBalanceMixin {
    @Inject(method = "initializeGame", at = @At("RETURN"))
    private void brinRestoreConfiguredInitialBalances(
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci
    ) {
        for (ServerPlayer player : players) {
            String roleId = BrinRoles.getRoleId(gameWorld, player);
            if (roleId != null) {
                Integer balance = BrinConfig.initialBalance(roleId);
                if (balance != null) {
                    PlayerShopComponent.KEY.get(player).setBalance(balance);
                }
            }
            if (gameWorld.isRole(player, BrinRoles.SNIPER)) {
                BrinRoles.initializeSniper(player);
            }
            if (gameWorld.isRole(player, BrinRoles.WATCHMAN)) {
                brinStripSurvivalExpertLeftoverKits(player);
                BrinRoles.ensureWatchmanRoomKey(player);
            }
            ServerPlayNetworking.send(player, new BrinConfigS2CPacket(BrinConfig.toJson()));
        }
    }

    @Unique
    private static void brinStripSurvivalExpertLeftoverKits(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (BrinShopAccess.isSurvivalExpertExcludedItem(inventory.getItem(slot))) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }
}
