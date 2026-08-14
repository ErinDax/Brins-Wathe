package cn.autoforged.brinswathe.component;

import cn.autoforged.brinswathe.CowboyDuel;
import cn.autoforged.brinswathe.config.BrinConfig;
import cn.autoforged.brinswathe.entity.TrapperFangs;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;

public class TrapperComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<TrapperComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "trapper"),
        TrapperComponent.class
    );

    public static final int TRAP_STUN_TICKS = Integer.MAX_VALUE;

    private final Player player;
    private final Set<UUID> activeTrapEntityIds = new LinkedHashSet<>();
    public final Set<UUID> trappedPlayerIds = new HashSet<>();

    public TrapperComponent(Player player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        // Frozen while a cowboy duel benches the crowd as spectators; nothing may reset or tick down.
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer sp)) return;

        boolean changed = trimActiveTraps(sp.server);
        if (this.trappedPlayerIds.isEmpty()) {
            if (changed) this.sync();
            return;
        }

        for (UUID trappedPlayerId : new ArrayList<>(this.trappedPlayerIds)) {
            Player trapped = sp.server.getPlayerList().getPlayer(trappedPlayerId);
            if (trapped == null) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(trapped)) {
                releaseTrappedPlayer(trapped);
                this.trappedPlayerIds.remove(trappedPlayerId);
                changed = true;
                continue;
            }

            PlayerEffectComponent effect = PlayerEffectComponent.KEY.get(trapped);
            if (effect != null && effect.stunTicks <= 0) {
                effect.setStunTicks(TRAP_STUN_TICKS);
            }
        }
        if (changed) this.sync();
    }

    public boolean hasActiveTrap() {
        return !this.activeTrapEntityIds.isEmpty();
    }

    public boolean canPlaceTrap() {
        return this.activeTrapEntityIds.size() < BrinConfig.trapperTrapLimit();
    }

    public List<UUID> activeTrapEntityIds() {
        return List.copyOf(this.activeTrapEntityIds);
    }

    public void setTrap(UUID entityId) {
        this.activeTrapEntityIds.add(entityId);
        this.sync();
    }

    public void removeTrap(UUID entityId) {
        if (!this.activeTrapEntityIds.remove(entityId)) return;
        this.sync();
    }

    public void trapPlayer(UUID trapEntityId, UUID playerId) {
        this.activeTrapEntityIds.remove(trapEntityId);
        this.trappedPlayerIds.add(playerId);
        this.sync();
    }

    public boolean isTrapped(UUID playerId) {
        return this.trappedPlayerIds.contains(playerId);
    }

    public boolean rescuePlayer(UUID playerId) {
        if (!this.trappedPlayerIds.remove(playerId)) return false;

        if (this.player instanceof ServerPlayer sp) {
            Player trapped = sp.server.getPlayerList().getPlayer(playerId);
            if (trapped != null) releaseTrappedPlayer(trapped);
        }
        this.sync();
        return true;
    }

    public void clearTrap() {
        ArrayList<UUID> trapIds = new ArrayList<>(this.activeTrapEntityIds);
        ArrayList<UUID> trappedIds = new ArrayList<>(this.trappedPlayerIds);
        this.activeTrapEntityIds.clear();
        this.trappedPlayerIds.clear();

        if (this.player instanceof ServerPlayer sp) {
            trapIds.forEach(trapId -> discardTrap(sp.server, trapId));
            for (UUID trappedId : trappedIds) {
                Player trapped = sp.server.getPlayerList().getPlayer(trappedId);
                if (trapped != null) releaseTrappedPlayer(trapped);
            }
        }
        this.sync();
    }

    private boolean trimActiveTraps(MinecraftServer server) {
        int limit = BrinConfig.trapperTrapLimit();
        if (this.activeTrapEntityIds.size() <= limit) return false;

        int retained = 0;
        boolean changed = false;
        for (var iterator = this.activeTrapEntityIds.iterator(); iterator.hasNext();) {
            UUID trapId = iterator.next();
            if (retained++ < limit) continue;
            iterator.remove();
            discardTrap(server, trapId);
            changed = true;
        }
        return changed;
    }

    private static void discardTrap(MinecraftServer server, UUID entityId) {
        for (var level : server.getAllLevels()) {
            Entity entity = level.getEntity(entityId);
            if (entity == null) continue;
            entity.discard();
            return;
        }
    }

    private static void releaseTrappedPlayer(Player trapped) {
        PlayerEffectComponent effect = PlayerEffectComponent.KEY.get(trapped);
        if (effect != null && effect.stunTicks > 0) effect.reset();
        trapped.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    public void reset() {
        this.clearTrap();
    }

    public static void clearAllTrapEntities(MinecraftServer server) {
        ArrayList<Entity> traps = new ArrayList<>();
        for (var level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof EvokerFangs
                    && ((TrapperFangs) entity).brin$isTrapperTrap()) {
                    traps.add(entity);
                }
            }
        }
        traps.forEach(Entity::discard);
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.activeTrapEntityIds.clear();
        if (tag.contains("activeTrapEntityIds", Tag.TAG_LIST)) {
            ListTag activeTrapIds = tag.getList("activeTrapEntityIds", Tag.TAG_INT_ARRAY);
            for (Tag activeTrapId : activeTrapIds) {
                this.activeTrapEntityIds.add(NbtUtils.loadUUID(activeTrapId));
            }
        } else if (tag.hasUUID("activeTrapEntityId")) {
            this.activeTrapEntityIds.add(tag.getUUID("activeTrapEntityId"));
        }
        this.trappedPlayerIds.clear();
        if (tag.contains("trappedPlayerIds", Tag.TAG_LIST)) {
            ListTag trappedIds = tag.getList("trappedPlayerIds", Tag.TAG_INT_ARRAY);
            for (Tag trappedId : trappedIds) {
                this.trappedPlayerIds.add(NbtUtils.loadUUID(trappedId));
            }
        } else if (tag.hasUUID("trappedPlayerId")) {
            this.trappedPlayerIds.add(tag.getUUID("trappedPlayerId"));
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        if (!this.activeTrapEntityIds.isEmpty()) {
            ListTag activeTrapIds = new ListTag();
            for (UUID activeTrapEntityId : this.activeTrapEntityIds) {
                activeTrapIds.add(NbtUtils.createUUID(activeTrapEntityId));
            }
            tag.put("activeTrapEntityIds", activeTrapIds);
        }
        if (!this.trappedPlayerIds.isEmpty()) {
            ListTag trappedIds = new ListTag();
            for (UUID trappedPlayerId : this.trappedPlayerIds) {
                trappedIds.add(NbtUtils.createUUID(trappedPlayerId));
            }
            tag.put("trappedPlayerIds", trappedIds);
        }
    }
}
