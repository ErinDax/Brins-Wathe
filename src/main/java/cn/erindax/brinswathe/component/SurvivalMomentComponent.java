package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.role.SurvivalMoment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public final class SurvivalMomentComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<SurvivalMomentComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "survival_moment"),
        SurvivalMomentComponent.class
    );

    private final Level level;
    private int ticks;

    public SurvivalMomentComponent(Level level) {
        this.level = level;
    }

    public boolean isActive() {
        return this.ticks > 0;
    }

    public int seconds() {
        return (this.ticks + 19) / 20;
    }

    public void start(int ticks) {
        this.ticks = Math.max(1, ticks);
        this.sync();
    }

    public boolean countDown() {
        if (this.ticks <= 0) return false;
        this.ticks--;
        if (this.ticks % 20 == 0) this.sync();
        return this.ticks == 0;
    }

    public void reset() {
        if (this.ticks == 0) return;
        this.ticks = 0;
        this.sync();
    }

    @Override
    public void serverTick() {
        if (this.level instanceof ServerLevel serverLevel) SurvivalMoment.tick(serverLevel, this);
    }

    public void sync() {
        KEY.sync(this.level);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.ticks = tag.getInt("ticks");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("ticks", this.ticks);
    }
}
