package cn.autoforged.brinswathe.component;

import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class GamblerComponent implements AutoSyncedComponent {
    public static final ComponentKey<GamblerComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "gambler"),
        GamblerComponent.class
    );

    private final Player player;
    public UUID betTarget = null;
    public int betTicks = 0;
    public boolean poisoned = false;

    public static final int BET_WIN_TICKS = 7200;

    public GamblerComponent(Player player) {
        this.player = player;
    }

    public void setBetTarget(UUID targetId) {
        this.betTarget = targetId;
        this.betTicks = BET_WIN_TICKS;
        this.sync();
    }

    public void onBetTargetDied() {
        if (this.betTarget != null) {
            this.poisoned = true;
            this.betTarget = null;
            this.betTicks = 0;
            this.sync();
        }
    }

    public void reset() {
        this.betTarget = null;
        this.betTicks = 0;
        this.poisoned = false;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        if (tag.hasUUID("betTarget")) {
            this.betTarget = tag.getUUID("betTarget");
            this.betTicks = tag.getInt("betTicks");
        } else {
            this.betTarget = null;
            this.betTicks = 0;
        }
        this.poisoned = tag.getBoolean("poisoned");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        if (this.betTarget != null) {
            tag.putUUID("betTarget", this.betTarget);
            tag.putInt("betTicks", this.betTicks);
        }
        tag.putBoolean("poisoned", this.poisoned);
    }
}
