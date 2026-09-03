package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class BerserkerComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<BerserkerComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "berserker"),
        BerserkerComponent.class
    );

    private final Player player;
    public boolean psychoActive;

    public BerserkerComponent(Player player) {
        this.player = player;
    }

    public boolean activatePsycho() {
        return activatePsycho(false);
    }

    public boolean activatePsycho(boolean force) {
        if (this.psychoActive) return false;
        PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(this.player);
        if (psycho == null) return false;

        if (!psycho.startPsycho()) {
            if (!force) return false;
            if (this.player instanceof ServerPlayer serverPlayer) {
                serverPlayer.drop(new ItemStack(WatheItems.BAT), false);
            }
            psycho.setArmour(1);
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.level());
            gameWorld.setPsychosActive(gameWorld.getPsychosActive() + 1);
        }
        psycho.setPsychoTicks(Integer.MAX_VALUE);
        this.psychoActive = true;
        this.sync();
        return true;
    }
    @Override
    public void serverTick() {
        if (this.psychoActive) return;
        if (!(this.player instanceof ServerPlayer berserker)) return;
        if (CowboyDuel.isActive()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(berserker)) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(berserker.level());
        if (!gameWorld.isRole(berserker, BrinRoles.BERSERKER)) return;
        if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
        if (GameSafeComponent.KEY.get(berserker.level()).isGameSafe) return;
        if (hasCompetingThreat(berserker, gameWorld)) return;
        if (!activatePsycho(true)) return;
        berserker.playNotifySound(SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
        berserker.displayClientMessage(
            Component.translatable("message.brinswathe.berserker.free_psycho").withStyle(ChatFormatting.RED),
            false
        );
    }
    private static boolean hasCompetingThreat(ServerPlayer berserker, GameWorldComponent gameWorld) {
        for (ServerPlayer other : berserker.server.getPlayerList().getPlayers()) {
            if (other == berserker) continue;
            if (GameWorldComponent.KEY.get(other.level()) != gameWorld) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(other)) continue;
            if (isCompetingThreat(gameWorld, other)) return true;
        }
        return false;
    }
    private static boolean isCompetingThreat(GameWorldComponent gameWorld, ServerPlayer other) {
        Role role = gameWorld.getRole(other);
        if (role == null) return false;
        if (role.canUseKiller() || gameWorld.canUseKillerFeatures(other) || isWolfSidedNeutral(role)) {
            return true;
        }
        if (role.isInnocent()) return false;
        return !isKillerSidedOnlyNeutral(role);
    }
    private static boolean isWolfSidedNeutral(Role role) {
        ResourceLocation id = role.identifier();
        return "stupid_express".equals(id.getNamespace()) && "thief".equals(id.getPath());
    }
    private static boolean isKillerSidedOnlyNeutral(Role role) {
        ResourceLocation id = role.identifier();
        return "noellesroles".equals(id.getNamespace()) && "vulture".equals(id.getPath());
    }
    public void reset() {
        this.psychoActive = false;
        this.sync();
    }
    public void sync() {
        KEY.sync(this.player);
    }
    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.psychoActive = tag.getBoolean("psychoActive");
    }
    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putBoolean("psychoActive", this.psychoActive);
    }
}
