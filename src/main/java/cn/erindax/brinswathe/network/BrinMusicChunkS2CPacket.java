package cn.erindax.brinswathe.network;

import cn.erindax.brinswathe.musicbox.BrinMusicBox;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinMusicChunkS2CPacket(String trackId, int index, byte[] data) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_chunk");
    public static final Type<BrinMusicChunkS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinMusicChunkS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinMusicChunkS2CPacket::write, BrinMusicChunkS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinMusicChunkS2CPacket packet) {
        buf.writeUtf(packet.trackId, 64);
        buf.writeVarInt(packet.index);
        buf.writeByteArray(packet.data);
    }

    private static BrinMusicChunkS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinMusicChunkS2CPacket(buf.readUtf(64), buf.readVarInt(), buf.readByteArray(BrinMusicBox.CHUNK_SIZE));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
