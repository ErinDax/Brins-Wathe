package cn.erindax.brinswathe.network;

import cn.erindax.brinswathe.musicbox.MusicFormats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MusicUploadStartC2SPacket(String fileName, String format, int size) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_upload_start");
    public static final Type<MusicUploadStartC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MusicUploadStartC2SPacket> STREAM_CODEC =
        StreamCodec.of(MusicUploadStartC2SPacket::write, MusicUploadStartC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, MusicUploadStartC2SPacket packet) {
        buf.writeUtf(packet.fileName, MusicFormats.MAX_NAME_LENGTH);
        buf.writeUtf(packet.format, 8);
        buf.writeVarInt(packet.size);
    }

    private static MusicUploadStartC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new MusicUploadStartC2SPacket(
            buf.readUtf(MusicFormats.MAX_NAME_LENGTH),
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
