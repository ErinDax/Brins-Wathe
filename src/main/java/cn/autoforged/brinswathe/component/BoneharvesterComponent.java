package cn.autoforged.brinswathe.component;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class BoneharvesterComponent implements AutoSyncedComponent {
    public static final ComponentKey<BoneharvesterComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "boneharvester"),
        BoneharvesterComponent.class
    );

    private final Player player;
    private int shieldLayers;

    public BoneharvesterComponent(Player player) {
        this.player = player;
    }

    public boolean applyBoneShield() {
        if (shieldLayers == Integer.MAX_VALUE) return false;
        shieldLayers++;
        sync();
        return true;
    }

    public boolean consumeShield() {
        if (shieldLayers <= 0) return false;
        shieldLayers--;
        sync();
        return true;
    }

    public int getShieldLayers() {
        return shieldLayers;
    }

    public void reset() {
        if (shieldLayers == 0) return;
        shieldLayers = 0;
        sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        shieldLayers = tag.getInt("shieldLayers");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("shieldLayers", shieldLayers);
    }
}
