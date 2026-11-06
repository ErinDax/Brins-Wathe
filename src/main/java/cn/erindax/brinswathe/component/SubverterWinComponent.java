package cn.erindax.brinswathe.component;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public final class SubverterWinComponent implements AutoSyncedComponent {
    public static final ComponentKey<SubverterWinComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "subverter_win"),
        SubverterWinComponent.class
    );

    private final Level level;
    private boolean subverterWon;

    public SubverterWinComponent(Level level) {
        this.level = level;
    }

    public boolean subverterWon() {
        return this.subverterWon;
    }

    public void setSubverterWon(boolean subverterWon) {
        this.subverterWon = subverterWon;
        this.sync();
    }

    public void reset() {
        this.setSubverterWon(false);
    }

    public void sync() {
        KEY.sync(this.level);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.subverterWon = tag.getBoolean("subverterWon");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putBoolean("subverterWon", this.subverterWon);
    }
}
