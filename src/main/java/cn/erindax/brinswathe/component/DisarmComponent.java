package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public final class DisarmComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<DisarmComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "disarm"),
        DisarmComponent.class
    );

    private final Level level;
    private int ticks;

    public DisarmComponent(Level level) {
        this.level = level;
    }

    public boolean isActive() {
        return this.ticks > 0;
    }

    public int ticks() {
        return this.ticks;
    }

    public void start(int ticks) {
        this.ticks = Math.max(this.ticks, ticks);
        this.sync();
    }

    public void reset() {
        if (this.ticks == 0) return;
        this.ticks = 0;
        this.sync();
    }

    @Override
    public void serverTick() {
        if (this.ticks <= 0) return;
        if (CowboyDuel.isActive()
            || GameWorldComponent.KEY.get(this.level).getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) {
            this.reset();
            return;
        }
        this.ticks--;
        if (this.ticks == 0) this.sync();
    }

    @Override
    public void clientTick() {
        if (this.ticks > 0) this.ticks--;
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
