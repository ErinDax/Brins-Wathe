package cn.erindax.brinswathe;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.roles.cook.CookComponent;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerComponent;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerKillerComponent;
import org.BsXinQin.kinswathe.roles.physician.PhysicianComponent;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.events.ModdedRoleRemoved;
import org.agmas.harpymodloader.events.ResetPlayerEvent;
import org.ladysnake.cca.api.v3.component.ComponentKey;

public final class BrinRoleOverwriteCleanup {
    private static final String[] OPTIONAL_RESET_COMPONENTS = {
        "org.agmas.noellesroles.AbilityPlayerComponent",
        "org.agmas.noellesroles.bartender.BartenderPlayerComponent",
        "org.agmas.noellesroles.voodoo.VoodooPlayerComponent",
        "org.agmas.noellesroles.recaller.RecallerPlayerComponent",
        "org.agmas.noellesroles.morphling.ResurrectedPlayerComponent"
    };
    private BrinRoleOverwriteCleanup() {
    }

    public static int clearReplacedInnocent(ServerPlayer player, Role oldRole) {
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
        int balance = shop == null ? 0 : shop.balance;
        if (oldRole != null && !Harpymodloader.VANNILA_ROLES.contains(oldRole)) {
            ModdedRoleRemoved.EVENT.invoker().removeModdedRole(player, oldRole);
        }
        ResetPlayerEvent.EVENT.invoker().resetPlayer(player);
        resetForeignRoleState(player);
        clearInventoryKeepingLetter(player);
        return balance;
    }

    public static void restoreStartingGold(ServerPlayer player, int balance) {
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
        if (shop == null) return;
        shop.setBalance(balance);
    }
    private static void resetForeignRoleState(ServerPlayer player) {
        AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
        if (ability != null) ability.setAbilityCooldown(0);
        PhysicianComponent physician = PhysicianComponent.KEY.get(player);
        if (physician != null) physician.reset();
        CookComponent cook = CookComponent.KEY.get(player);
        if (cook != null) cook.reset();
        DreamerComponent dreamer = DreamerComponent.KEY.get(player);
        if (dreamer != null) dreamer.reset();

        DreamerKillerComponent dreamerKiller = DreamerKillerComponent.KEY.get(player);
        if (dreamerKiller != null) dreamerKiller.reset();

        for (String className : OPTIONAL_RESET_COMPONENTS) {
            resetOptionalComponent(player, className);
        }
    }
    private static void resetOptionalComponent(ServerPlayer player, String className) {
        try {
            Class<?> type = Class.forName(className);
            Object key = type.getField("KEY").get(null);
            if (!(key instanceof ComponentKey<?> componentKey)) return;
            Object component = componentKey.get(player);
            if (component == null) return;
            type.getMethod("reset").invoke(component);
        } catch (ReflectiveOperationException ignored) {
        }
    }
    private static void clearInventoryKeepingLetter(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || stack.is(WatheItems.LETTER)) continue;
            inventory.setItem(slot, ItemStack.EMPTY);
        }
        if (!player.containerMenu.getCarried().isEmpty()
            && !player.containerMenu.getCarried().is(WatheItems.LETTER)) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }
    }
}
