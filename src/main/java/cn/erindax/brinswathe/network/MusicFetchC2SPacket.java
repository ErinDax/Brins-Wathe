package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MusicFetchC2SPacket(String trackId, int index) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_fetch");
    public static final Type<MusicFetchC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MusicFetchC2SPacket> STREAM_CODEC =
        StreamCodec.of(MusicFetchC2SPacket::write, MusicFetchC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, MusicFetchC2SPacket packet) {
        buf.writeUtf(packet.trackId, 64);
        buf.writeVarInt(packet.index);
    }

    private static MusicFetchC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new MusicFetchC2SPacket(buf.readUtf(64), buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
