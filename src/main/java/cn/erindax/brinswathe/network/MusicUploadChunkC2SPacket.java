package cn.erindax.brinswathe.network;

import cn.erindax.brinswathe.musicbox.MusicBox;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MusicUploadChunkC2SPacket(int index, byte[] data) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_upload_chunk");
    public static final Type<MusicUploadChunkC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MusicUploadChunkC2SPacket> STREAM_CODEC =
        StreamCodec.of(MusicUploadChunkC2SPacket::write, MusicUploadChunkC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, MusicUploadChunkC2SPacket packet) {
        buf.writeVarInt(packet.index);
        buf.writeByteArray(packet.data);
    }

    private static MusicUploadChunkC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new MusicUploadChunkC2SPacket(buf.readVarInt(), buf.readByteArray(MusicBox.CHUNK_SIZE));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
