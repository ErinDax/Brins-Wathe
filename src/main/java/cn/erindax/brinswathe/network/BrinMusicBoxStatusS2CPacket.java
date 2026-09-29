package cn.erindax.brinswathe.network;

import cn.erindax.brinswathe.musicbox.BrinMusicFormats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinMusicBoxStatusS2CPacket(
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
    public static final Type<BrinMusicBoxStatusS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinMusicBoxStatusS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinMusicBoxStatusS2CPacket::write, BrinMusicBoxStatusS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinMusicBoxStatusS2CPacket packet) {
        buf.writeBoolean(packet.enabled);
        buf.writeVarInt(packet.maxBytes);
        buf.writeUtf(packet.trackId, 64);
        buf.writeUtf(packet.trackName, BrinMusicFormats.MAX_NAME_LENGTH);
        buf.writeUtf(packet.format, 8);
        buf.writeVarInt(packet.trackSize);
        buf.writeUtf(packet.messageKey, 128);
        buf.writeUtf(packet.messageArg, 256);
    }

    private static BrinMusicBoxStatusS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinMusicBoxStatusS2CPacket(
            buf.readBoolean(),
            buf.readVarInt(),
            buf.readUtf(64),
            buf.readUtf(BrinMusicFormats.MAX_NAME_LENGTH),
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
