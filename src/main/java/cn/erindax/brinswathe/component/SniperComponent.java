package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.BrinModifiers;
import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.CowboyDuel;
import cn.erindax.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class SniperComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<SniperComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "sniper"),
        SniperComponent.class
    );
    public static final double MAX_RANGE = 300.0D;

    private final Player player;
    private boolean aiming;
    private int cooldownTicks;
    private int killsSinceReset;
    private int tasksSinceReset;
    private boolean recoverByTasks;
    private boolean shotKillPending;
    private int glowTicks;
    private double anchorX;
    private double anchorY;
    private double anchorZ;

    public SniperComponent(Player player) {
        this.player = player;
    }

    public boolean isAiming() {
        return this.aiming;
    }

    public boolean isAbilityActive() {
        return this.aiming;
    }

    public int getCooldownTicks() {
        return this.cooldownTicks;
    }

    public void startCooldown(int seconds) {
        if (BrinModifiers.hasModifier(this.player, BrinModifiers.FAST2FAST)) {
            this.clearCooldown();
            return;
        }
        long ticks = Math.max(0L, (long) seconds * 20L);
        this.cooldownTicks = (int) Math.min(Integer.MAX_VALUE, ticks);
        this.killsSinceReset = 0;
        this.tasksSinceReset = 0;
        this.recoverByTasks = false;
        this.sync();
    }

    public void startShotCooldown(int seconds) {
        this.startCooldown(seconds);
        if (this.cooldownTicks > 0 && BrinConfig.sniperTasksToReset() > 0) {
            this.recoverByTasks = true;
            this.sync();
        }
    }

    public void clearCooldown() {
        if (this.cooldownTicks == 0 && this.killsSinceReset == 0 && this.tasksSinceReset == 0
            && !this.recoverByTasks) {
            return;
        }
        this.cooldownTicks = 0;
        this.killsSinceReset = 0;
        this.tasksSinceReset = 0;
        this.recoverByTasks = false;
        this.sync();
    }

    public int getTasksSinceReset() {
        return this.tasksSinceReset;
    }

    public boolean isRecoveringByTasks() {
        return this.recoverByTasks && this.cooldownTicks > 0;
    }

    public boolean recordTask(int tasksToReset) {
        return this.addRecoveryProgress(1, tasksToReset);
    }

    public boolean addRecoveryProgress(int amount, int tasksToReset) {
        if (!this.recoverByTasks || this.cooldownTicks <= 0) return false;
        if (tasksToReset < 1 || amount < 1) {
            if (this.tasksSinceReset != 0) {
                this.tasksSinceReset = 0;
                this.sync();
            }
            return false;
        }

        this.tasksSinceReset += amount;
        if (this.tasksSinceReset >= tasksToReset) {
            this.clearCooldown();
            return true;
        }
        this.sync();
        return false;
    }

    public boolean recordKill(int killsToReset) {
        if (killsToReset < 1) {
            if (this.killsSinceReset != 0) {
                this.killsSinceReset = 0;
                this.sync();
            }
            return false;
        }

        this.killsSinceReset++;
        if (this.killsSinceReset >= killsToReset) {
            this.killsSinceReset = 0;
            this.sync();
            return true;
        }
        this.sync();
        return false;
    }

    public boolean consumeShotKillPending() {
        boolean pending = this.shotKillPending;
        this.shotKillPending = false;
        return pending;
    }

    public void startAiming() {
        this.aiming = true;
        this.anchorX = this.player.getX();
        this.anchorY = this.player.getY();
        this.anchorZ = this.player.getZ();
        this.player.setDeltaMovement(Vec3.ZERO);
        this.applyGlow();
        this.sync();
    }

    public void cancelAiming() {
        if (!this.aiming) return;
        this.aiming = false;
        this.glowTicks = 0;
        this.applyGlow();
        this.sync();
    }

    public void fire(Vec3 origin, Vec3 direction) {
        this.aiming = false;
        this.glowTicks = Math.max(0, BrinConfig.sniperGlowSeconds()) * 20;
        this.applyGlow();
        if (!(this.player instanceof ServerPlayer sniper)
            || !isFinite(origin)
            || !isFinite(direction)
            || direction.lengthSqr() < 1.0E-8D) {
            this.sync();
            return;
        }

        ServerLevel level = sniper.serverLevel();
        Vec3 end = origin.add(direction.normalize().scale(MAX_RANGE));
        AABB searchBox = new AABB(origin, end);
        ServerPlayer nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;

        for (ServerPlayer target : level.getEntitiesOfClass(
            ServerPlayer.class,
            searchBox,
            candidate -> candidate != sniper && GameFunctions.isPlayerAliveAndSurvival(candidate)
        )) {
            Optional<Vec3> hit = target.getBoundingBox().clip(origin, end);
            if (hit.isEmpty()) continue;
            double distance = origin.distanceToSqr(hit.get());
            if (distance < nearestDistance) {
                nearestTarget = target;
                nearestDistance = distance;
            }
        }

        if (nearestTarget != null) {
            this.shotKillPending = true;
            GameFunctions.killPlayer(
                nearestTarget,
                true,
                sniper,
                GameConstants.DeathReasons.GUN
            );
            this.shotKillPending = false;
        }
        this.sync();
    }

    @Override
    public void serverTick() {
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer sniper)) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(sniper.level());
        if (!gameWorld.isRole(sniper, BrinRoles.SNIPER)
            || !GameFunctions.isPlayerAliveAndSurvival(sniper)) {
            if (this.isAbilityActive() || this.cooldownTicks > 0 || this.glowTicks > 0) this.reset();
            return;
        }

        if (this.cooldownTicks > 0) {
            if (BrinModifiers.hasModifier(sniper, BrinModifiers.FAST2FAST)) {
                this.clearCooldown();
            } else if (!this.recoverByTasks) {
                this.cooldownTicks--;
                this.sync();
            }
        }

        if (this.glowTicks > 0) {
            this.glowTicks--;
            if (this.glowTicks <= 0) this.applyGlow();
        }

        if (this.aiming) {
            if (sniper.distanceToSqr(this.anchorX, this.anchorY, this.anchorZ) > 1.0E-6D) {
                sniper.setPos(this.anchorX, this.anchorY, this.anchorZ);
            }
            sniper.setDeltaMovement(Vec3.ZERO);
            sniper.fallDistance = 0.0F;
            sniper.setSprinting(false);
        }

    }

    public void reset() {
        boolean ownedGlow = this.aiming || this.glowTicks > 0;
        boolean changed = ownedGlow || this.cooldownTicks != 0
            || this.killsSinceReset != 0 || this.tasksSinceReset != 0 || this.recoverByTasks;
        this.aiming = false;
        this.glowTicks = 0;
        this.cooldownTicks = 0;
        this.killsSinceReset = 0;
        this.tasksSinceReset = 0;
        this.recoverByTasks = false;
        this.shotKillPending = false;
        if (ownedGlow) this.applyGlow();
        if (changed) this.sync();
    }

    private void applyGlow() {
        this.player.setGlowingTag(this.aiming || this.glowTicks > 0);
    }

    private static boolean isFinite(Vec3 vector) {
        return Double.isFinite(vector.x)
            && Double.isFinite(vector.y)
            && Double.isFinite(vector.z);
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.aiming = tag.getBoolean("aiming");
        this.cooldownTicks = tag.getInt("cooldownTicks");
        this.killsSinceReset = tag.getInt("killsSinceReset");
        this.tasksSinceReset = tag.getInt("tasksSinceReset");
        this.recoverByTasks = tag.getBoolean("recoverByTasks");
        this.glowTicks = tag.getInt("glowTicks");
        this.anchorX = tag.getDouble("anchorX");
        this.anchorY = tag.getDouble("anchorY");
        this.anchorZ = tag.getDouble("anchorZ");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putBoolean("aiming", this.aiming);
        tag.putInt("cooldownTicks", this.cooldownTicks);
        tag.putInt("killsSinceReset", this.killsSinceReset);
        tag.putInt("tasksSinceReset", this.tasksSinceReset);
        tag.putBoolean("recoverByTasks", this.recoverByTasks);
        tag.putInt("glowTicks", this.glowTicks);
        tag.putDouble("anchorX", this.anchorX);
        tag.putDouble("anchorY", this.anchorY);
        tag.putDouble("anchorZ", this.anchorZ);
    }
}
