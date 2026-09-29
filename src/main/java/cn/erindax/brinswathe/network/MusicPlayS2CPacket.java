package cn.erindax.brinswathe.network;

import cn.erindax.brinswathe.musicbox.MusicFormats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MusicPlayS2CPacket(
    String trackId,
    String format,
    int size,
    String ownerName,
    String trackName
) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_play");
    public static final Type<MusicPlayS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MusicPlayS2CPacket> STREAM_CODEC =
        StreamCodec.of(MusicPlayS2CPacket::write, MusicPlayS2CPacket::read);

    public static MusicPlayS2CPacket stop() {
        return new MusicPlayS2CPacket("", "", 0, "", "");
    }

    public boolean isStop() {
        return this.trackId.isEmpty();
    }

    private static void write(RegistryFriendlyByteBuf buf, MusicPlayS2CPacket packet) {
        buf.writeUtf(packet.trackId, 64);
        buf.writeUtf(packet.format, 8);
        buf.writeVarInt(packet.size);
        buf.writeUtf(packet.ownerName, 64);
        buf.writeUtf(packet.trackName, MusicFormats.MAX_NAME_LENGTH);
    }

    private static MusicPlayS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new MusicPlayS2CPacket(
            buf.readUtf(64),
            buf.readUtf(8),
            buf.readVarInt(),
            buf.readUtf(64),
            buf.readUtf(MusicFormats.MAX_NAME_LENGTH)
        );
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
