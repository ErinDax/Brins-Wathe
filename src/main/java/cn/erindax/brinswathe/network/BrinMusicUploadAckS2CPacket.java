package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinMusicUploadAckS2CPacket(int received) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_upload_ack");
    public static final Type<BrinMusicUploadAckS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinMusicUploadAckS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinMusicUploadAckS2CPacket::write, BrinMusicUploadAckS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinMusicUploadAckS2CPacket packet) {
        buf.writeVarInt(packet.received);
    }

    private static BrinMusicUploadAckS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinMusicUploadAckS2CPacket(buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
