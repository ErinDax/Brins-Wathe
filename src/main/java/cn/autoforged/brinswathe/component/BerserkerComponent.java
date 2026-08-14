package cn.autoforged.brinswathe.component;

import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class BerserkerComponent implements AutoSyncedComponent {
    public static final ComponentKey<BerserkerComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "berserker"),
        BerserkerComponent.class
    );

    private final Player player;
    public boolean psychoActive;

    public BerserkerComponent(Player player) {
        this.player = player;
    }

    public boolean activatePsycho() {
        if (this.psychoActive) return false;

        PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(this.player);
        if (psycho == null || !psycho.startPsycho()) return false;

        psycho.setPsychoTicks(Integer.MAX_VALUE);
        this.psychoActive = true;
        this.sync();
        return true;
    }

    public void reset() {
        this.psychoActive = false;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.psychoActive = tag.getBoolean("psychoActive");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putBoolean("psychoActive", this.psychoActive);
    }
}
