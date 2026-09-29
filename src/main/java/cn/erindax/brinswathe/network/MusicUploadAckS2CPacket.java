package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MusicUploadAckS2CPacket(int received) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_upload_ack");
    public static final Type<MusicUploadAckS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MusicUploadAckS2CPacket> STREAM_CODEC =
        StreamCodec.of(MusicUploadAckS2CPacket::write, MusicUploadAckS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, MusicUploadAckS2CPacket packet) {
        buf.writeVarInt(packet.received);
    }

    private static MusicUploadAckS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new MusicUploadAckS2CPacket(buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
