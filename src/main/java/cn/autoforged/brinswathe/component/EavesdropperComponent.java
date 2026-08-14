package cn.autoforged.brinswathe.component;

import cn.autoforged.brinswathe.voice.BrinVoiceChatPlugin;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class EavesdropperComponent implements AutoSyncedComponent {
    public static final ComponentKey<EavesdropperComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "eavesdropper"),
        EavesdropperComponent.class
    );

    private final Player player;
    public UUID channelPartner;
    public UUID channelId;
    public int channelTicks;
    public boolean channelOwner;

    public EavesdropperComponent(Player player) {
        this.player = player;
    }

    public void reset() {
        if (this.channelId != null && this.player instanceof ServerPlayer serverPlayer) {
            BrinVoiceChatPlugin.endTemporaryChannel(serverPlayer);
        }
    }

    public boolean isInTemporaryChannel() {
        return this.channelId != null && this.channelPartner != null;
    }

    public void setTemporaryChannel(UUID partner, UUID groupId, int ticks, boolean owner) {
        this.channelPartner = partner;
        this.channelId = groupId;
        this.channelTicks = ticks;
        this.channelOwner = owner;
        this.sync();
    }

    public void clearTemporaryChannel() {
        this.channelPartner = null;
        this.channelId = null;
        this.channelTicks = 0;
        this.channelOwner = false;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.channelPartner = tag.hasUUID("channelPartner") ? tag.getUUID("channelPartner") : null;
        this.channelId = tag.hasUUID("channelId") ? tag.getUUID("channelId") : null;
        this.channelTicks = tag.getInt("channelTicks");
        this.channelOwner = tag.getBoolean("channelOwner");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        if (this.channelPartner != null) tag.putUUID("channelPartner", this.channelPartner);
        if (this.channelId != null) tag.putUUID("channelId", this.channelId);
        tag.putInt("channelTicks", this.channelTicks);
        tag.putBoolean("channelOwner", this.channelOwner);
    }
}
