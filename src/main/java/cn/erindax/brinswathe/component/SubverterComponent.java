package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public final class SubverterComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<SubverterComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "subverter"),
        SubverterComponent.class
    );
    public static final int DEADLINE_TICKS = 30 * 20;

    private final Player player;
    private int deadlineTicks;
    @Nullable
    private Side side;

    public SubverterComponent(Player player) {
        this.player = player;
    }

    public boolean isPending() {
        return this.side == null && this.deadlineTicks > 0;
    }

    public int deadlineTicks() {
        return this.deadlineTicks;
    }

    @Nullable
    public Side side() {
        return this.side;
    }

    public void start() {
        this.side = null;
        this.deadlineTicks = DEADLINE_TICKS;
        this.sync();
    }

    public void settle(@Nullable Side side) {
        this.side = side;
        this.deadlineTicks = 0;
        this.sync();
    }

    public void reset() {
        if (this.side == null && this.deadlineTicks == 0) return;
        this.side = null;
        this.deadlineTicks = 0;
        this.sync();
    }

    @Override
    public void serverTick() {
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(serverPlayer.level());
        if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE
            || !gameWorld.isRole(serverPlayer, BrinRoles.SUBVERTER)) {
            this.reset();
            return;
        }
        if (!this.isPending()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
            this.settle(null);
            return;
        }
        this.deadlineTicks--;
        if (this.deadlineTicks > 0) return;
        this.sync();
        serverPlayer.displayClientMessage(Component.translatable("message.brinswathe.subverter.timeout"), false);
        GameFunctions.killPlayer(serverPlayer, true, null, GameConstants.DeathReasons.GENERIC);
    }

    @Override
    public void clientTick() {
        if (this.side == null && this.deadlineTicks > 0) this.deadlineTicks--;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.deadlineTicks = tag.getInt("deadlineTicks");
        this.side = tag.contains("side") ? Side.valueOf(tag.getString("side")) : null;
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("deadlineTicks", this.deadlineTicks);
        if (this.side != null) tag.putString("side", this.side.name());
    }

    public enum Side {
        KILLER,
        CIVILIAN;

        public boolean includes(Role role) {
            return this == KILLER ? role.canUseKiller() : role.isInnocent() && !role.canUseKiller();
        }

        public boolean wins(GameFunctions.WinStatus status) {
            return this == KILLER
                ? status == GameFunctions.WinStatus.KILLERS
                : status == GameFunctions.WinStatus.PASSENGERS || status == GameFunctions.WinStatus.TIME;
        }
    }
}
