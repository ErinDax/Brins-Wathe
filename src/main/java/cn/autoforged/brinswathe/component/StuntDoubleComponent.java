package cn.autoforged.brinswathe.component;

import cn.autoforged.brinswathe.CowboyDuel;
import cn.autoforged.brinswathe.config.BrinConfig;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class StuntDoubleComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<StuntDoubleComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "stunt_double"),
        StuntDoubleComponent.class
    );

    private final Player player;
    public int mimicTicks = 0;
    public UUID mimicTarget = null;

    public StuntDoubleComponent(Player player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        // Frozen while a cowboy duel benches the crowd as spectators; nothing may reset or tick down.
        if (CowboyDuel.isActive()) return;
        if (this.mimicTicks > 0) {
            this.mimicTicks--;
            if (this.mimicTicks <= 0) {
                this.mimicTarget = null;
            }
            this.sync();
        }
    }

    public boolean isMimicking() {
        return this.mimicTicks > 0 && this.mimicTarget != null;
    }

    public void startMimic(UUID target) {
        this.mimicTarget = target;
        this.mimicTicks = BrinConfig.skillDurationSeconds("stunt_double") * 20;
        this.sync();
    }

    public void stopMimic() {
        this.mimicTicks = 0;
        this.mimicTarget = null;
        this.sync();
    }

    public void reset() {
        this.stopMimic();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.mimicTicks = tag.getInt("mimicTicks");
        if (tag.hasUUID("mimicTarget")) {
            this.mimicTarget = tag.getUUID("mimicTarget");
        } else {
            this.mimicTarget = null;
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("mimicTicks", this.mimicTicks);
        if (this.mimicTarget != null) {
            tag.putUUID("mimicTarget", this.mimicTarget);
        }
    }
}
