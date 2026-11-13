package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.ModRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public final class SpyComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<SpyComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "spy"),
        SpyComponent.class
    );
    public static final int MAX_WRONG = 2;

    private final Player player;
    private final Set<UUID> guessed = new HashSet<>();
    private int required;
    private int wrong;

    public SpyComponent(Player player) {
        this.player = player;
    }

    public int required() {
        return this.required;
    }

    public int correct() {
        return this.guessed.size();
    }

    public int wrong() {
        return this.wrong;
    }

    public boolean hasGuessed(UUID targetId) {
        return this.guessed.contains(targetId);
    }

    public boolean completed() {
        return this.required > 0 && this.guessed.size() >= this.required;
    }

    public void recordCorrect(UUID targetId) {
        if (this.guessed.add(targetId)) this.sync();
    }

    public void recordWrong() {
        this.wrong++;
        this.sync();
    }

    public void reset() {
        if (this.required == 0 && this.wrong == 0 && this.guessed.isEmpty()) return;
        this.required = 0;
        this.wrong = 0;
        this.guessed.clear();
        this.sync();
    }

    @Override
    public void serverTick() {
        if (this.required > 0) return;
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(serverPlayer.level());
        if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE
            || !gameWorld.isRole(serverPlayer, ModRoles.SPY)) {
            return;
        }
        this.required = Math.max(1, gameWorld.getRoles().size() / 2 - 1);
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.required = tag.getInt("required");
        this.wrong = tag.getInt("wrong");
        this.guessed.clear();
        ListTag targets = tag.getList("guessed", Tag.TAG_INT_ARRAY);
        for (Tag target : targets) {
            this.guessed.add(NbtUtils.loadUUID(target));
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("required", this.required);
        tag.putInt("wrong", this.wrong);
        ListTag targets = new ListTag();
        for (UUID targetId : this.guessed) {
            targets.add(NbtUtils.createUUID(targetId));
        }
        tag.put("guessed", targets);
    }
}
