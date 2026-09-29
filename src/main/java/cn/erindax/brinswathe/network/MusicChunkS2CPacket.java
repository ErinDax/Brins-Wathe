package cn.erindax.brinswathe.network;

import cn.erindax.brinswathe.musicbox.MusicBox;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MusicChunkS2CPacket(String trackId, int index, byte[] data) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_chunk");
    public static final Type<MusicChunkS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MusicChunkS2CPacket> STREAM_CODEC =
        StreamCodec.of(MusicChunkS2CPacket::write, MusicChunkS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, MusicChunkS2CPacket packet) {
        buf.writeUtf(packet.trackId, 64);
        buf.writeVarInt(packet.index);
        buf.writeByteArray(packet.data);
    }

    private static MusicChunkS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new MusicChunkS2CPacket(buf.readUtf(64), buf.readVarInt(), buf.readByteArray(MusicBox.CHUNK_SIZE));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
