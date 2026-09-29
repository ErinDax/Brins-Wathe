package cn.erindax.brinswathe.network;

import cn.erindax.brinswathe.musicbox.BrinMusicFormats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinMusicPlayS2CPacket(
    String trackId,
    String format,
    int size,
    String ownerName,
    String trackName
) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_play");
    public static final Type<BrinMusicPlayS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinMusicPlayS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinMusicPlayS2CPacket::write, BrinMusicPlayS2CPacket::read);

    public static BrinMusicPlayS2CPacket stop() {
        return new BrinMusicPlayS2CPacket("", "", 0, "", "");
    }

    public boolean isStop() {
        return this.trackId.isEmpty();
    }

    private static void write(RegistryFriendlyByteBuf buf, BrinMusicPlayS2CPacket packet) {
        buf.writeUtf(packet.trackId, 64);
        buf.writeUtf(packet.format, 8);
        buf.writeVarInt(packet.size);
        buf.writeUtf(packet.ownerName, 64);
        buf.writeUtf(packet.trackName, BrinMusicFormats.MAX_NAME_LENGTH);
    }

    private static BrinMusicPlayS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinMusicPlayS2CPacket(
            buf.readUtf(64),
            buf.readUtf(8),
            buf.readVarInt(),
            buf.readUtf(64),
            buf.readUtf(BrinMusicFormats.MAX_NAME_LENGTH)
        );
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
