package cn.erindax.brinswathe.network;

import cn.erindax.brinswathe.musicbox.BrinMusicBox;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinMusicUploadChunkC2SPacket(int index, byte[] data) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_upload_chunk");
    public static final Type<BrinMusicUploadChunkC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinMusicUploadChunkC2SPacket> STREAM_CODEC =
        StreamCodec.of(BrinMusicUploadChunkC2SPacket::write, BrinMusicUploadChunkC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinMusicUploadChunkC2SPacket packet) {
        buf.writeVarInt(packet.index);
        buf.writeByteArray(packet.data);
    }

    private static BrinMusicUploadChunkC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinMusicUploadChunkC2SPacket(buf.readVarInt(), buf.readByteArray(BrinMusicBox.CHUNK_SIZE));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
