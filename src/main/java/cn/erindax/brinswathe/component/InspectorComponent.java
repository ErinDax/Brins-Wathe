package cn.erindax.brinswathe.component;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public final class InspectorComponent implements AutoSyncedComponent {
    public static final ComponentKey<InspectorComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "inspector"),
        InspectorComponent.class
    );

    private final Player player;
    private boolean used;

    public InspectorComponent(Player player) {
        this.player = player;
    }

    public boolean used() {
        return this.used;
    }

    public void markUsed() {
        this.used = true;
        this.sync();
    }

    public void reset() {
        if (!this.used) return;
        this.used = false;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.used = tag.getBoolean("used");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putBoolean("used", this.used);
    }
}
