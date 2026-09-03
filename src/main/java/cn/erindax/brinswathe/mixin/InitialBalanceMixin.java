package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.ShopAccess;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.network.ConfigS2CPacket;
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
public abstract class InitialBalanceMixin {
    @Inject(method = "initializeGame", at = @At("RETURN"))
    private void brinRestoreConfiguredInitialBalances(
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci
    ) {
        for (ServerPlayer player : players) {
            String roleId = ModRoles.getRoleId(gameWorld, player);
            if (roleId != null) {
                Integer balance = ModConfig.initialBalance(roleId);
                if (balance != null) {
                    PlayerShopComponent.KEY.get(player).setBalance(balance);
                }
            }
            if (gameWorld.isRole(player, ModRoles.SNIPER)) {
                ModRoles.initializeSniper(player);
            }
            if (gameWorld.isRole(player, ModRoles.WATCHMAN)) {
                brinStripSurvivalExpertLeftoverKits(player);
                ModRoles.ensureWatchmanRoomKey(player);
            }
            ServerPlayNetworking.send(player, new ConfigS2CPacket(ModConfig.toJson()));
        }
    }

    @Unique
    private static void brinStripSurvivalExpertLeftoverKits(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (ShopAccess.isSurvivalExpertExcludedItem(inventory.getItem(slot))) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }
}
