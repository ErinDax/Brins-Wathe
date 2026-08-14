package cn.autoforged.brinswathe.component;

import cn.autoforged.brinswathe.CowboyDuel;
import cn.autoforged.brinswathe.voice.BrinVoiceChatPlugin;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.UUID;

public class MediumComponent implements AutoSyncedComponent, ServerTickingComponent {
    private static MethodHandle trainAddPlayer;
    private static MethodHandle trainResetPlayer;
    private static MethodHandle trainVoiceChatMissing;
    public static final ComponentKey<MediumComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "medium"),
        MediumComponent.class
    );

    private final Player player;
    public boolean inTrainChannel = false;
    public int channelTicks = 0;

    public MediumComponent(Player player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        // Frozen while a cowboy duel benches the crowd as spectators; nothing may reset or tick down.
        if (CowboyDuel.isActive()) return;
        if (this.channelTicks <= 0) return;
        this.channelTicks--;
        if (this.channelTicks <= 0) {
            this.leaveTrainChannel();
        } else if (this.channelTicks % 20 == 0) {
            this.sync();
        }
    }

    public boolean startChannelling(int seconds) {
        if (this.channelTicks > 0) return false;
        this.joinTrainChannel();
        if (!this.inTrainChannel) return false;
        this.channelTicks = Math.max(1, seconds * 20);
        this.sync();
        return true;
    }

    public void joinTrainChannel() {
        if (BrinVoiceChatPlugin.isInTrainChannel(this.player.getUUID())) {
            if (!this.inTrainChannel) {
                this.inTrainChannel = true;
                KEY.sync(this.player);
            }
            return;
        }
        if (addToTrainChannel(this.player.getUUID())) {
            this.inTrainChannel = true;
            KEY.sync(this.player);
        }
    }

    public void leaveTrainChannel() {
        if (this.inTrainChannel || BrinVoiceChatPlugin.isInTrainChannel(this.player.getUUID())) {
            resetTrainPlayer(this.player.getUUID());
            this.inTrainChannel = false;
            KEY.sync(this.player);
        }
    }

    public void reset() {
        this.channelTicks = 0;
        this.leaveTrainChannel();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.inTrainChannel = tag.getBoolean("inTrainChannel");
        this.channelTicks = tag.getInt("channelTicks");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putBoolean("inTrainChannel", this.inTrainChannel);
        tag.putInt("channelTicks", this.channelTicks);
    }

    public static boolean addToTrainChannel(UUID uuid) {
        resolveTrainMethods();
        if (trainAddPlayer == null || !BrinVoiceChatPlugin.hasVoicechatConnection(uuid)) return false;
        try {
            if (trainVoiceChatMissing != null && (boolean) trainVoiceChatMissing.invokeExact()) return false;
            trainAddPlayer.invokeExact(uuid);
            return BrinVoiceChatPlugin.isInTrainChannel(uuid);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void resetTrainPlayer(UUID uuid) {
        resolveTrainMethods();
        if (trainResetPlayer == null) return;
        try {
            trainResetPlayer.invokeExact(uuid);
        } catch (Throwable ignored) {
        }
    }

    private static synchronized void resolveTrainMethods() {
        if (trainAddPlayer != null || trainResetPlayer != null || trainVoiceChatMissing != null) return;
        try {
            Class<?> clazz = Class.forName("dev.doctor4t.wathe.compat.TrainVoicePlugin");
            trainAddPlayer = MethodHandles.publicLookup().findStatic(clazz, "addPlayer",
                MethodType.methodType(void.class, UUID.class));
            trainResetPlayer = MethodHandles.publicLookup().findStatic(clazz, "resetPlayer",
                MethodType.methodType(void.class, UUID.class));
            trainVoiceChatMissing = MethodHandles.publicLookup().findStatic(clazz, "isVoiceChatMissing",
                MethodType.methodType(boolean.class));
        } catch (Throwable ignored) {
            trainAddPlayer = null;
            trainResetPlayer = null;
            trainVoiceChatMissing = null;
        }
    }
}
