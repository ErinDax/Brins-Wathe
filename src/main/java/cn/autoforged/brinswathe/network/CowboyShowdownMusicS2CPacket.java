package cn.autoforged.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Starts or stops the showdown theme as a local 2D track. Volume is the server's 0–1 gain so a
 * client whose local config never got the new key cannot mute the sting by accident.
 */
public record CowboyShowdownMusicS2CPacket(boolean play, float volume) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "cowboy_showdown_music");
    public static final Type<CowboyShowdownMusicS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, CowboyShowdownMusicS2CPacket> STREAM_CODEC =
        StreamCodec.of(CowboyShowdownMusicS2CPacket::write, CowboyShowdownMusicS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, CowboyShowdownMusicS2CPacket packet) {
        buf.writeBoolean(packet.play);
        buf.writeFloat(packet.volume);
    }

    private static CowboyShowdownMusicS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new CowboyShowdownMusicS2CPacket(buf.readBoolean(), buf.readFloat());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
