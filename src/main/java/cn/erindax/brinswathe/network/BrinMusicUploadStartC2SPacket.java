package cn.erindax.brinswathe.network;

import cn.erindax.brinswathe.musicbox.BrinMusicFormats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinMusicUploadStartC2SPacket(String fileName, String format, int size) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_upload_start");
    public static final Type<BrinMusicUploadStartC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinMusicUploadStartC2SPacket> STREAM_CODEC =
        StreamCodec.of(BrinMusicUploadStartC2SPacket::write, BrinMusicUploadStartC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinMusicUploadStartC2SPacket packet) {
        buf.writeUtf(packet.fileName, BrinMusicFormats.MAX_NAME_LENGTH);
        buf.writeUtf(packet.format, 8);
        buf.writeVarInt(packet.size);
    }

    private static BrinMusicUploadStartC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinMusicUploadStartC2SPacket(
            buf.readUtf(BrinMusicFormats.MAX_NAME_LENGTH),
            buf.readUtf(8),
            buf.readVarInt()
        );
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
