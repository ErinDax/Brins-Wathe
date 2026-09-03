package cn.erindax.brinswathe.component;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class MuzzlerAbilityComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<MuzzlerAbilityComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "muzzler_ability"),
        MuzzlerAbilityComponent.class
    );

    public static final int ABILITY_COST = 100;
    public static final int ABILITY_COOLDOWN_TICKS = 60 * 20;
    public static final int ABILITY_DURATION_TICKS = 30 * 20;
    public static final int SILENCE_KILL_REWARD = 50;

    private final Player player;
    private int ticks;

    public MuzzlerAbilityComponent(Player player) {
        this.player = player;
    }

    public static boolean isActive(Entity entity) {
        return entity instanceof Player player && isActive(player);
    }

    public static boolean isActive(Player player) {
        MuzzlerAbilityComponent component = KEY.get(player);
        return component != null && component.ticks > 0;
    }

    public int getTicks() {
        return this.ticks;
    }

    public void setTicks(int ticks) {
        this.ticks = Math.max(0, ticks);
        this.sync();
    }

    public void reset() {
        this.ticks = 0;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void serverTick() {
        if (this.ticks <= 0) return;
        this.ticks--;
        this.sync();
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.ticks = tag.contains("ticks") ? tag.getInt("ticks") : 0;
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("ticks", this.ticks);
    }
}
