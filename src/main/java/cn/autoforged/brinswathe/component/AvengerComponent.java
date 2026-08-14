package cn.autoforged.brinswathe.component;

import cn.autoforged.brinswathe.CowboyDuel;
import cn.autoforged.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

/**
 * The avenger's borrowed courage: witnessing a kill hands him a revolver and a short burst of instinct,
 * both on independent timers. The client ticks its own copy down between the once-a-second syncs so the
 * instinct overlay ends on time instead of on the next packet.
 */
public class AvengerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<AvengerComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "avenger"),
        AvengerComponent.class
    );

    private final Player player;
    private int gunTicks;
    private int instinctTicks;

    public AvengerComponent(Player player) {
        this.player = player;
    }

    public int gunTicks() {
        return this.gunTicks;
    }

    public int instinctTicks() {
        return this.instinctTicks;
    }

    public boolean hasGunWindow() {
        return this.gunTicks > 0;
    }

    /** Restarts both windows; a second witnessed kill refreshes rather than stacks. */
    public void witness() {
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;

        this.gunTicks = BrinConfig.avengerGunSeconds() * 20;
        this.instinctTicks = Math.max(0, BrinConfig.avengerInstinctSeconds()) * 20;
        if (!hasFirearm(serverPlayer)) {
            ItemStack revolver = WatheItems.REVOLVER.getDefaultInstance();
            if (!serverPlayer.addItem(revolver)) {
                serverPlayer.drop(revolver, false);
            }
        }
        serverPlayer.playNotifySound(SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.0F, 1.3F);
        serverPlayer.displayClientMessage(
            Component.translatable("message.brinswathe.avenger.witness").withStyle(ChatFormatting.DARK_RED),
            false
        );
        this.sync();
    }

    @Override
    public void serverTick() {
        // Frozen while a cowboy duel benches the crowd as spectators; nothing may reset or tick down.
        if (CowboyDuel.isActive()) return;
        if (this.gunTicks <= 0 && this.instinctTicks <= 0) return;
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;

        if (!GameFunctions.isPlayerAliveAndSurvival(serverPlayer)
            || GameWorldComponent.KEY.get(serverPlayer.level()).getGameStatus()
                != GameWorldComponent.GameStatus.ACTIVE) {
            this.reset();
            return;
        }

        if (this.instinctTicks > 0) {
            this.instinctTicks--;
            if (this.instinctTicks == 0) this.sync();
        }

        if (this.gunTicks > 0) {
            this.gunTicks--;
            if (this.gunTicks == 0) {
                this.stripFirearms(serverPlayer);
                serverPlayer.playNotifySound(SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);
                serverPlayer.displayClientMessage(
                    Component.translatable("message.brinswathe.avenger.expired").withStyle(ChatFormatting.GRAY),
                    true
                );
                this.sync();
            } else {
                if (this.gunTicks % 20 == 0) this.sync();
                serverPlayer.displayClientMessage(
                    Component.translatable(
                        "tip.brinswathe.avenger.gun",
                        Mth.ceil(this.gunTicks / 20.0F)
                    ).withStyle(ChatFormatting.RED),
                    true
                );
            }
        }
    }

    @Override
    public void clientTick() {
        if (this.instinctTicks > 0) this.instinctTicks--;
        if (this.gunTicks > 0) this.gunTicks--;
    }

    public void reset() {
        if (this.gunTicks <= 0 && this.instinctTicks <= 0) return;
        boolean hadGun = this.gunTicks > 0;
        this.gunTicks = 0;
        this.instinctTicks = 0;
        if (hadGun && this.player instanceof ServerPlayer serverPlayer) {
            this.stripFirearms(serverPlayer);
        }
        this.sync();
    }

    /**
     * Also removes derringers: the only way an avenger can hold one is having grabbed it during the
     * window, and letting it outlive the window would be a permanent gun the role is not allowed.
     */
    private void stripFirearms(ServerPlayer serverPlayer) {
        var inventory = serverPlayer.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(WatheItems.REVOLVER) || stack.is(WatheItems.DERRINGER)) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    private static boolean hasFirearm(ServerPlayer serverPlayer) {
        var inventory = serverPlayer.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(WatheItems.REVOLVER) || stack.is(WatheItems.DERRINGER)) return true;
        }
        return false;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.gunTicks = tag.getInt("gunTicks");
        this.instinctTicks = tag.getInt("instinctTicks");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("gunTicks", this.gunTicks);
        tag.putInt("instinctTicks", this.instinctTicks);
    }
}
