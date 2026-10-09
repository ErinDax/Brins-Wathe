package cn.erindax.brinswathe;

import cn.erindax.brinswathe.network.BrinBuySlotC2SPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class BrinBuySlots {
    public static final int HOTBAR_SIZE = 9;
    private static final Map<UUID, Integer> PREFERRED = new HashMap<>();
    @Nullable
    private static UUID buyer;

    private BrinBuySlots() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> PREFERRED.clear());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PREFERRED.remove(handler.player.getUUID()));
        ServerPlayNetworking.registerGlobalReceiver(
            BrinBuySlotC2SPacket.TYPE,
            (payload, context) -> set(context.player(), payload.slot())
        );
    }

    public static void beginPurchase(Player player) {
        if (!player.level().isClientSide()) buyer = player.getUUID();
    }

    public static void endPurchase() {
        buyer = null;
    }

    public static boolean insert(Player player, ItemStack stack) {
        if (buyer == null || !buyer.equals(player.getUUID())) return false;
        Integer preferred = PREFERRED.get(buyer);
        if (preferred == null) return false;
        Inventory inventory = player.getInventory();
        int origin = preferred - 1;
        for (int distance = 0; distance < HOTBAR_SIZE; distance++) {
            if (place(inventory, origin + distance, stack)) return true;
            if (distance > 0 && place(inventory, origin - distance, stack)) return true;
        }
        return false;
    }

    private static void set(ServerPlayer player, int slot) {
        if (slot >= 1 && slot <= HOTBAR_SIZE) {
            PREFERRED.put(player.getUUID(), slot);
        } else {
            PREFERRED.remove(player.getUUID());
        }
    }

    private static boolean place(Inventory inventory, int slot, ItemStack stack) {
        if (slot < 0 || slot >= HOTBAR_SIZE || !inventory.getItem(slot).isEmpty()) return false;
        inventory.setItem(slot, stack);
        return true;
    }
}
