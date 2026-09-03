package cn.erindax.brinswathe;

import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class BrinItemCooldowns {
    private static volatile boolean pending;

    private BrinItemCooldowns() {
    }

    public static void schedule() {
        pending = true;
    }

    public static void applyKnifeStab(ServerPlayer attacker) {
        if (attacker.isCreative()) return;
        if (GameWorldComponent.KEY.get(attacker.level()).getGameMode() == WatheGameModes.LOOSE_ENDS) return;
        attacker.getCooldowns().addCooldown(WatheItems.KNIFE, GameConstants.ITEM_COOLDOWNS.get(WatheItems.KNIFE));
    }
    public static void tick(MinecraftServer server) {
        if (!pending) return;
        pending = false;
        int seconds = BrinIcFlags.resetItemsCooldownSeconds;
        if (seconds <= 0) return;
        int cooldown = GameConstants.getInTicks(0, seconds);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.getCooldowns().addCooldown(WatheItems.REVOLVER, cooldown);
            player.getCooldowns().addCooldown(WatheItems.KNIFE, cooldown);
            for (String id : BrinIcFlags.resetItemsList) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
                if (item == null || item == Items.AIR) continue;
                if (item == WatheItems.REVOLVER || item == WatheItems.KNIFE) continue;
                player.getCooldowns().addCooldown(item, cooldown);
            }
        }
    }
}
