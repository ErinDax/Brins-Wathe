package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.CowboyDuel;
import cn.erindax.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class AvengerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<AvengerComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "avenger"),
        AvengerComponent.class
    );
    private final Player player;
    private int instinctTicks;

    public AvengerComponent(Player player) {
        this.player = player;
    }

    public int instinctTicks() {
        return this.instinctTicks;
    }

    public void witness() {
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;
        this.instinctTicks = Math.max(0, BrinConfig.avengerInstinctSeconds()) * 20;
        serverPlayer.playNotifySound(SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.0F, 1.3F);
        serverPlayer.displayClientMessage(
            Component.translatable("message.brinswathe.avenger.witness").withStyle(ChatFormatting.DARK_RED),
            false
        );
        this.sync();
    }
    @Override
    public void serverTick() {
        if (CowboyDuel.isActive()) return;
        if (this.instinctTicks <= 0) return;
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(serverPlayer)
            || GameWorldComponent.KEY.get(serverPlayer.level()).getGameStatus()
                != GameWorldComponent.GameStatus.ACTIVE) {
            this.reset();
            return;
        }
        this.instinctTicks--;
        if (this.instinctTicks == 0) this.sync();
    }
    @Override
    public void clientTick() {
        if (this.instinctTicks > 0) this.instinctTicks--;
    }
    public void reset() {
        if (this.instinctTicks <= 0) return;
        this.instinctTicks = 0;
        this.sync();
    }
    public void sync() {
        KEY.sync(this.player);
    }
    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.instinctTicks = tag.getInt("instinctTicks");
    }
    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("instinctTicks", this.instinctTicks);
    }
}
