package cn.autoforged.brinswathe.component;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

/**
 * The cowboy's single showdown. The flag is synced so the client can grey out the ability once the
 * duel has been spent; everything about a running duel itself lives in {@code CowboyDuel}.
 */
public class CowboyComponent implements AutoSyncedComponent {
    public static final ComponentKey<CowboyComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "cowboy"),
        CowboyComponent.class
    );

    private final Player player;
    private boolean duelUsed;

    public CowboyComponent(Player player) {
        this.player = player;
    }

    public boolean duelUsed() {
        return this.duelUsed;
    }

    public void markDuelUsed() {
        if (this.duelUsed) return;
        this.duelUsed = true;
        this.sync();
    }

    public void reset() {
        if (!this.duelUsed) return;
        this.duelUsed = false;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.duelUsed = tag.getBoolean("duelUsed");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putBoolean("duelUsed", this.duelUsed);
    }
}
