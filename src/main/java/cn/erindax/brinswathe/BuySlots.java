package cn.erindax.brinswathe;

import cn.erindax.brinswathe.network.BuySlotC2SPacket;
import cn.erindax.brinswathe.network.ItemSlotsC2SPacket;
import dev.doctor4t.wathe.index.WatheItems;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public final class BuySlots {
    public static final int HOTBAR_SIZE = 9;
    private static final Map<UUID, Integer> PREFERRED = new HashMap<>();
    private static final Map<UUID, Map<Item, Integer>> ITEM_SLOTS = new HashMap<>();
    @Nullable
    private static UUID buyer;

    private BuySlots() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            PREFERRED.clear();
            ITEM_SLOTS.clear();
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PREFERRED.remove(handler.player.getUUID());
            ITEM_SLOTS.remove(handler.player.getUUID());
        });
        ServerPlayNetworking.registerGlobalReceiver(
            BuySlotC2SPacket.TYPE,
            (payload, context) -> set(context.player(), payload.slot())
        );
        ServerPlayNetworking.registerGlobalReceiver(
            ItemSlotsC2SPacket.TYPE,
            (payload, context) -> setItemSlots(context.player(), payload.slots())
        );
    }

    public static void beginPurchase(Player player) {
        if (!player.level().isClientSide()) buyer = player.getUUID();
    }

    public static void endPurchase() {
        buyer = null;
    }

    public static boolean insert(Player player, ItemStack stack) {
        int slot = claim(player, stack);
        if (slot < 0) return false;
        player.getInventory().setItem(slot, stack);
        return true;
    }

    public static boolean add(Inventory inventory, ItemStack stack) {
        if (stack.getCount() > stack.getMaxStackSize() || inventory.getSlotWithRemainingSpace(stack) != -1) return false;
        int slot = claim(inventory.player, stack);
        if (slot < 0) return false;
        ItemStack placed = stack.copyAndClear();
        placed.setPopTime(Inventory.POP_TIME_DURATION);
        inventory.setItem(slot, placed);
        return true;
    }

    private static void set(ServerPlayer player, int slot) {
        if (slot >= 1 && slot <= HOTBAR_SIZE) {
            PREFERRED.put(player.getUUID(), slot);
        } else {
            PREFERRED.remove(player.getUUID());
        }
    }

    private static void setItemSlots(ServerPlayer player, Map<ResourceLocation, Integer> slots) {
        Map<Item, Integer> resolved = new HashMap<>();
        for (Map.Entry<ResourceLocation, Integer> entry : slots.entrySet()) {
            int slot = entry.getValue();
            if (slot < 1 || slot > HOTBAR_SIZE) continue;
            Item item = BuiltInRegistries.ITEM.get(entry.getKey());
            if (item != Items.AIR) resolved.put(item, slot);
        }
        if (resolved.isEmpty()) {
            ITEM_SLOTS.remove(player.getUUID());
        } else {
            ITEM_SLOTS.put(player.getUUID(), resolved);
        }
    }

    private static int claim(Player player, ItemStack stack) {
        if (stack.isEmpty() || !(player instanceof ServerPlayer serverPlayer)) return -1;
        Inventory inventory = serverPlayer.getInventory();
        Integer pinned = itemSlot(serverPlayer, stack);
        if (pinned != null) {
            int slot = pinned - 1;
            return vacate(serverPlayer, inventory, slot) ? slot : nearestEmpty(inventory, slot);
        }
        Integer preferred = serverPlayer.getUUID().equals(buyer) ? PREFERRED.get(buyer) : null;
        return preferred == null ? -1 : nearestEmpty(inventory, preferred - 1);
    }

    private static boolean vacate(ServerPlayer player, Inventory inventory, int slot) {
        ItemStack occupant = inventory.getItem(slot);
        if (occupant.isEmpty()) return true;
        Integer own = itemSlot(player, occupant);
        if (own != null && own - 1 == slot) return false;
        if (occupant.is(WatheItems.BAT) || CowboyDuel.isDuelGun(occupant)) return false;
        boolean held = inventory.selected == slot;
        if (held && player.isUsingItem()) return false;
        int destination = own != null && inventory.getItem(own - 1).isEmpty() ? own - 1 : nearestEmpty(inventory, slot);
        if (destination < 0) return false;
        inventory.setItem(slot, ItemStack.EMPTY);
        inventory.setItem(destination, occupant);
        if (held) {
            inventory.selected = destination;
            if (player.connection != null) player.connection.send(new ClientboundSetCarriedItemPacket(destination));
        }
        return true;
    }

    @Nullable
    private static Integer itemSlot(ServerPlayer player, ItemStack stack) {
        Map<Item, Integer> slots = ITEM_SLOTS.get(player.getUUID());
        return slots == null ? null : slots.get(stack.getItem());
    }

    private static int nearestEmpty(Inventory inventory, int origin) {
        for (int distance = 0; distance < HOTBAR_SIZE; distance++) {
            if (isEmpty(inventory, origin + distance)) return origin + distance;
            if (distance > 0 && isEmpty(inventory, origin - distance)) return origin - distance;
        }
        return -1;
    }

    private static boolean isEmpty(Inventory inventory, int slot) {
        return slot >= 0 && slot < HOTBAR_SIZE && inventory.getItem(slot).isEmpty();
    }
}
