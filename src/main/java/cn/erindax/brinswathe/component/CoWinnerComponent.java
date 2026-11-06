package cn.erindax.brinswathe.component;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public final class CoWinnerComponent implements AutoSyncedComponent {
    public static final ComponentKey<CoWinnerComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "co_winners"),
        CoWinnerComponent.class
    );

    private final Level level;
    private boolean subverterWon;
    private boolean gamblerWon;

    public CoWinnerComponent(Level level) {
        this.level = level;
    }

    public boolean subverterWon() {
        return this.subverterWon;
    }

    public boolean gamblerWon() {
        return this.gamblerWon;
    }

    public void set(boolean subverterWon, boolean gamblerWon) {
        this.subverterWon = subverterWon;
        this.gamblerWon = gamblerWon;
        this.sync();
    }

    public void reset() {
        this.set(false, false);
    }

    public void sync() {
        KEY.sync(this.level);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.subverterWon = tag.getBoolean("subverterWon");
        this.gamblerWon = tag.getBoolean("gamblerWon");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putBoolean("subverterWon", this.subverterWon);
        tag.putBoolean("gamblerWon", this.gamblerWon);
    }
}
