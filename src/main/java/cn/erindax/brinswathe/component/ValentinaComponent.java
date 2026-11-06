package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.CowboyDuel;
import cn.erindax.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public final class ValentinaComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<ValentinaComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "valentina"),
        ValentinaComponent.class
    );
    public static final int MAX_EYES = 2;
    public static final int TASKS_PER_EYE = 4;
    public static final int FORESIGHT_COST = 2;

    private final Player player;
    private int eyes;
    private int taskProgress;
    private int slownessLevel;
    private int instinctTicks;

    public ValentinaComponent(Player player) {
        this.player = player;
    }

    public int eyes() {
        return this.eyes;
    }

    public int taskProgress() {
        return this.taskProgress;
    }

    public int instinctTicks() {
        return this.instinctTicks;
    }

    public boolean canForesee() {
        return this.eyes >= FORESIGHT_COST;
    }

    public void recordTask() {
        if (this.eyes >= MAX_EYES) return;
        this.taskProgress++;
        if (this.taskProgress >= TASKS_PER_EYE) {
            this.taskProgress = 0;
            this.eyes++;
        }
        this.sync();
    }

    public boolean consumeEye() {
        if (this.eyes <= 0) return false;
        this.eyes--;
        this.sync();
        return true;
    }

    public boolean foresee() {
        if (!this.canForesee()) return false;
        this.eyes -= FORESIGHT_COST;
        this.slownessLevel++;
        this.instinctTicks = Math.max(0, BrinConfig.skillDurationSeconds("valentina")) * 20;
        this.applySlowness();
        this.sync();
        return true;
    }

    @Override
    public void serverTick() {
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(serverPlayer.level());
        if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE
            || !gameWorld.isRole(serverPlayer, BrinRoles.VALENTINA)
            || !GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
            this.reset();
            return;
        }
        this.applySlowness();
        if (this.instinctTicks > 0) {
            this.instinctTicks--;
            if (this.instinctTicks == 0) this.sync();
        }
    }

    @Override
    public void clientTick() {
        if (this.instinctTicks > 0) this.instinctTicks--;
    }

    public void reset() {
        if (this.eyes == 0 && this.taskProgress == 0 && this.slownessLevel == 0 && this.instinctTicks == 0) return;
        boolean slowed = this.slownessLevel > 0;
        this.eyes = 0;
        this.taskProgress = 0;
        this.slownessLevel = 0;
        this.instinctTicks = 0;
        if (slowed) this.player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        this.sync();
    }

    private void applySlowness() {
        if (this.slownessLevel <= 0) return;
        int amplifier = this.slownessLevel - 1;
        MobEffectInstance current = this.player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (current != null && current.getAmplifier() >= amplifier) return;
        this.player.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffectInstance.INFINITE_DURATION,
            amplifier,
            false,
            false,
            true
        ));
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.eyes = tag.getInt("eyes");
        this.taskProgress = tag.getInt("taskProgress");
        this.slownessLevel = tag.getInt("slownessLevel");
        this.instinctTicks = tag.getInt("instinctTicks");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("eyes", this.eyes);
        tag.putInt("taskProgress", this.taskProgress);
        tag.putInt("slownessLevel", this.slownessLevel);
        tag.putInt("instinctTicks", this.instinctTicks);
    }
}
