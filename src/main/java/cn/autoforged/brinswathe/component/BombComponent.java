package cn.autoforged.brinswathe.component;

import cn.autoforged.brinswathe.BomberBombs;
import cn.autoforged.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

/**
 * Everything the bomber leaves on someone else: a bomb strapped to their chest, or the mine they are
 * standing on. The fuse itself is deliberately not synced every tick - clients only need to know that
 * a bomb exists so the survival expert can spot it.
 */
public class BombComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<BombComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "bomb"),
        BombComponent.class
    );

    private final Player player;
    private int fuseTicks;
    @Nullable
    private UUID bombOwnerId;
    private boolean warned;
    private boolean minePinned;
    @Nullable
    private UUID mineOwnerId;
    @Nullable
    private Vec3 mineAnchor;
    private int purchaseCooldownTicks;

    public BombComponent(Player player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        // Frozen while a cowboy duel benches the crowd as spectators; a strapped bomb must neither
        // count down nor be defused for free by the spectator switch.
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;

        if (!GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
            if (this.hasAttachedBomb() || this.minePinned) this.reset();
            return;
        }
        if (GameWorldComponent.KEY.get(serverPlayer.level()).getGameStatus()
            != GameWorldComponent.GameStatus.ACTIVE) {
            return;
        }

        if (this.purchaseCooldownTicks > 0) {
            this.purchaseCooldownTicks--;
            // Once a second is enough for the shop slot to count down, including the tick it reaches zero.
            if (this.purchaseCooldownTicks % 20 == 0) this.sync();
        }
        BomberBombs.tick(serverPlayer, this);
    }

    public int purchaseCooldownTicks() {
        return this.purchaseCooldownTicks;
    }

    public void startPurchaseCooldown(int seconds) {
        this.purchaseCooldownTicks = Math.max(0, seconds) * 20;
        this.sync();
    }

    public boolean hasAttachedBomb() {
        return this.fuseTicks > 0;
    }

    public int fuseTicks() {
        return this.fuseTicks;
    }

    @Nullable
    public UUID bombOwner() {
        return this.bombOwnerId;
    }

    public boolean hasWarned() {
        return this.warned;
    }

    public boolean isMinePinned() {
        return this.minePinned;
    }

    @Nullable
    public UUID mineOwner() {
        return this.mineOwnerId;
    }

    @Nullable
    public Vec3 mineAnchor() {
        return this.mineAnchor;
    }

    public boolean canBeDefused() {
        return this.hasAttachedBomb() || this.minePinned;
    }

    public void attach(@Nullable UUID ownerId, int fuseTicks, boolean warned) {
        this.fuseTicks = Math.max(1, fuseTicks);
        this.bombOwnerId = ownerId;
        this.warned = warned;
        this.sync();
    }

    /** Runs the fuse down by one tick and reports what is left. */
    public int tickFuse() {
        if (this.fuseTicks > 0) this.fuseTicks--;
        return this.fuseTicks;
    }

    public void markWarned() {
        this.warned = true;
    }

    public void clearBomb() {
        if (this.fuseTicks == 0 && this.bombOwnerId == null && !this.warned) return;
        this.fuseTicks = 0;
        this.bombOwnerId = null;
        this.warned = false;
        this.sync();
    }

    public void pin(@Nullable UUID ownerId, Vec3 anchor) {
        this.minePinned = true;
        this.mineOwnerId = ownerId;
        this.mineAnchor = anchor;
        this.sync();
    }

    public void clearMine() {
        if (!this.minePinned) return;
        this.minePinned = false;
        this.mineOwnerId = null;
        this.mineAnchor = null;
        BomberBombs.releaseStun(this.player);
        this.sync();
    }

    public void reset() {
        this.purchaseCooldownTicks = 0;
        this.clearBomb();
        this.clearMine();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.fuseTicks = tag.getInt("fuseTicks");
        this.bombOwnerId = tag.hasUUID("bombOwner") ? tag.getUUID("bombOwner") : null;
        this.warned = tag.getBoolean("warned");
        this.minePinned = tag.getBoolean("minePinned");
        this.purchaseCooldownTicks = tag.getInt("purchaseCooldownTicks");
        this.mineOwnerId = tag.hasUUID("mineOwner") ? tag.getUUID("mineOwner") : null;
        this.mineAnchor = null;
        if (tag.contains("mineAnchor", Tag.TAG_LIST)) {
            ListTag anchor = tag.getList("mineAnchor", Tag.TAG_DOUBLE);
            if (anchor.size() == 3) {
                this.mineAnchor = new Vec3(anchor.getDouble(0), anchor.getDouble(1), anchor.getDouble(2));
            }
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("fuseTicks", this.fuseTicks);
        if (this.bombOwnerId != null) tag.putUUID("bombOwner", this.bombOwnerId);
        tag.putBoolean("warned", this.warned);
        tag.putBoolean("minePinned", this.minePinned);
        tag.putInt("purchaseCooldownTicks", this.purchaseCooldownTicks);
        if (this.mineOwnerId != null) tag.putUUID("mineOwner", this.mineOwnerId);
        if (this.mineAnchor != null) {
            ListTag anchor = new ListTag();
            anchor.add(DoubleTag.valueOf(this.mineAnchor.x));
            anchor.add(DoubleTag.valueOf(this.mineAnchor.y));
            anchor.add(DoubleTag.valueOf(this.mineAnchor.z));
            tag.put("mineAnchor", anchor);
        }
    }
}
