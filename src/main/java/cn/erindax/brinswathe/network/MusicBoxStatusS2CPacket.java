package cn.erindax.brinswathe.network;

import cn.erindax.brinswathe.musicbox.MusicFormats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MusicBoxStatusS2CPacket(
    boolean enabled,
    int maxBytes,
    String trackId,
    String trackName,
    String format,
    int trackSize,
    String messageKey,
    String messageArg
) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_box_status");
    public static final Type<MusicBoxStatusS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MusicBoxStatusS2CPacket> STREAM_CODEC =
        StreamCodec.of(MusicBoxStatusS2CPacket::write, MusicBoxStatusS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, MusicBoxStatusS2CPacket packet) {
        buf.writeBoolean(packet.enabled);
        buf.writeVarInt(packet.maxBytes);
        buf.writeUtf(packet.trackId, 64);
        buf.writeUtf(packet.trackName, MusicFormats.MAX_NAME_LENGTH);
        buf.writeUtf(packet.format, 8);
        buf.writeVarInt(packet.trackSize);
        buf.writeUtf(packet.messageKey, 128);
        buf.writeUtf(packet.messageArg, 256);
    }

    private static MusicBoxStatusS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new MusicBoxStatusS2CPacket(
            buf.readBoolean(),
            buf.readVarInt(),
            buf.readUtf(64),
            buf.readUtf(MusicFormats.MAX_NAME_LENGTH),
            buf.readUtf(8),
            buf.readVarInt(),
            buf.readUtf(128),
            buf.readUtf(256)
        );
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
