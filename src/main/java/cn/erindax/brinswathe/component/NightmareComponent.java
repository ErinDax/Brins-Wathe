package cn.erindax.brinswathe.component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import cn.erindax.brinswathe.BrinModifiers;
import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class NightmareComponent implements AutoSyncedComponent {
    public static final ComponentKey<NightmareComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "nightmare"),
        NightmareComponent.class
    );

    public static final int MAX_SHIELD_LAYERS = 2;

    private final Player player;
    public Set<UUID> markedPlayers = new HashSet<>();
    public int forcedSleepTaskCooldown;
    private int shieldLayers;
    private boolean nightmareHour;

    public NightmareComponent(Player player) {
        this.player = player;
    }

    public static boolean isNightmareHour(GameWorldComponent game, Player player) {
        if (game == null || player == null || !game.isRole(player, BrinRoles.NIGHTMARE)) return false;
        NightmareComponent component = KEY.get(player);
        return component != null && component.nightmareHour;
    }
    public boolean isNightmareHour() {
        return this.nightmareHour;
    }
    public void beginNightmareHour() {
        if (this.nightmareHour) return;
        this.nightmareHour = true;
        this.sync();
    }
    public boolean isMarked(UUID playerId) {
        return this.markedPlayers.contains(playerId);
    }
    public void markPlayer(UUID playerId) {
        this.markedPlayers.add(playerId);
        this.sync();
    }
    public boolean addShieldLayer() {
        if (this.shieldLayers >= MAX_SHIELD_LAYERS) return false;
        this.shieldLayers++;
        this.sync();
        return true;
    }
    public boolean consumeShield() {
        if (this.shieldLayers <= 0) return false;
        this.shieldLayers--;
        this.sync();
        return true;
    }
    public int getShieldLayers() {
        return this.shieldLayers;
    }
    public void reset() {
        this.markedPlayers.clear();
        this.forcedSleepTaskCooldown = 0;
        this.shieldLayers = 0;
        this.nightmareHour = false;
        this.sync();
    }
    public void startForcedSleepTaskCooldown() {
        if (BrinModifiers.hasModifier(this.player, BrinModifiers.FAST2FAST)) {
            this.forcedSleepTaskCooldown = 0;
            this.sync();
            return;
        }
        this.forcedSleepTaskCooldown = BrinConfig.nightmareForcedSleepCooldownSeconds() * 20;
        this.sync();
    }
    public void clearForcedSleepTaskCooldown() {
        if (this.forcedSleepTaskCooldown == 0) return;
        this.forcedSleepTaskCooldown = 0;
        this.sync();
    }
    public void tickForcedSleepTaskCooldown() {
        if (this.forcedSleepTaskCooldown <= 0) return;
        if (BrinModifiers.hasModifier(this.player, BrinModifiers.FAST2FAST)) {
            this.forcedSleepTaskCooldown = 0;
            this.sync();
            return;
        }
        this.forcedSleepTaskCooldown--;
        if (this.forcedSleepTaskCooldown == 0 || this.forcedSleepTaskCooldown % 20 == 0) {
            this.sync();
        }
    }
    public void sync() {
        KEY.sync(this.player);
    }
    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.markedPlayers.clear();
        ListTag list = tag.getList("markedPlayers", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            this.markedPlayers.add(entry.getUUID("uuid"));
        }
        this.forcedSleepTaskCooldown = tag.getInt("forcedSleepTaskCooldown");
        this.shieldLayers = tag.getInt("shieldLayers");
        this.nightmareHour = tag.getBoolean("nightmareHour");
    }
    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        ListTag list = new ListTag();
        for (UUID id : this.markedPlayers) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", id);
            list.add(entry);
        }
        tag.put("markedPlayers", list);
        tag.putInt("forcedSleepTaskCooldown", this.forcedSleepTaskCooldown);
        tag.putInt("shieldLayers", this.shieldLayers);
        tag.putBoolean("nightmareHour", this.nightmareHour);
    }
}
