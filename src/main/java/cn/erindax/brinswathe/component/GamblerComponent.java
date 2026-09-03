package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class GamblerComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<GamblerComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "gambler"),
        GamblerComponent.class
    );

    private final Player player;
    public UUID betTarget = null;
    public int betTicks = 0;
    public boolean poisoned = false;
    private int actionBarRefreshTicks;

    public static final int BET_WIN_TICKS = 7200;

    public GamblerComponent(Player player) {
        this.player = player;
    }

    public void setBetTarget(UUID targetId) {
        this.betTarget = targetId;
        this.betTicks = BET_WIN_TICKS;
        this.actionBarRefreshTicks = 0;
        this.sync();
    }

    public void onBetTargetDied() {
        if (this.betTarget != null) {
            this.poisoned = true;
            this.betTarget = null;
            this.betTicks = 0;
            this.actionBarRefreshTicks = 0;
            this.sync();
        }
    }

    public void reset() {
        this.betTarget = null;
        this.betTicks = 0;
        this.poisoned = false;
        this.actionBarRefreshTicks = 0;
        this.sync();
    }

    @Override
    public void serverTick() {
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer owner)) return;
        if (this.poisoned || this.betTarget == null || this.betTicks <= 0) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(owner.level());
        if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE
            || !gameWorld.isRole(owner, BrinRoles.GAMBLER)) {
            return;
        }

        if (this.actionBarRefreshTicks <= 0) {
            owner.displayClientMessage(betTimerLine(), true);
            this.actionBarRefreshTicks = 10;
        } else {
            this.actionBarRefreshTicks--;
        }
    }

    private Component betTimerLine() {
        int totalSeconds = (this.betTicks + 19) / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return Component.translatable(
            "message.brinswathe.gambler.bet_timer",
            minutes + ":" + (seconds < 10 ? "0" : "") + seconds
        );
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
        this.actionBarRefreshTicks = 0;
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
