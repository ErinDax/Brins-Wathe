package cn.autoforged.brinswathe.component;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BrinCustomWinnerComponent implements AutoSyncedComponent {
    public static final ComponentKey<BrinCustomWinnerComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "custom_winner"),
        BrinCustomWinnerComponent.class
    );

    private final Level level;
    private String winningTextId;
    private int color;
    private final Set<UUID> winnerIds = new HashSet<>();

    public BrinCustomWinnerComponent(Level level) {
        this.level = level;
    }

    public boolean hasCustomWinner() {
        return this.winningTextId != null;
    }

    public String getWinningTextId() {
        return this.winningTextId;
    }

    public void setWinningTextId(String winningTextId) {
        this.winningTextId = winningTextId;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setWinners(Collection<UUID> winnerIds) {
        this.winnerIds.clear();
        this.winnerIds.addAll(winnerIds);
    }

    public boolean isWinner(UUID playerId) {
        return this.winnerIds.contains(playerId);
    }

    public void reset() {
        this.winningTextId = null;
        this.color = 0;
        this.winnerIds.clear();
        this.sync();
    }

    public void sync() {
        KEY.sync(this.level);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.winningTextId = tag.contains("winning_text") ? tag.getString("winning_text") : null;
        this.color = tag.getInt("color");
        this.winnerIds.clear();
        ListTag winners = tag.getList("winners", Tag.TAG_INT_ARRAY);
        for (Tag winner : winners) {
            this.winnerIds.add(NbtUtils.loadUUID(winner));
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        if (this.winningTextId != null) {
            tag.putString("winning_text", this.winningTextId);
        }
        tag.putInt("color", this.color);
        ListTag winners = new ListTag();
        for (UUID winnerId : this.winnerIds) {
            winners.add(NbtUtils.createUUID(winnerId));
        }
        tag.put("winners", winners);
    }
}
