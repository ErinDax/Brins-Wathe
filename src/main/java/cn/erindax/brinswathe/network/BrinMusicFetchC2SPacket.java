package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinMusicFetchC2SPacket(String trackId, int index) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_fetch");
    public static final Type<BrinMusicFetchC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinMusicFetchC2SPacket> STREAM_CODEC =
        StreamCodec.of(BrinMusicFetchC2SPacket::write, BrinMusicFetchC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinMusicFetchC2SPacket packet) {
        buf.writeUtf(packet.trackId, 64);
        buf.writeVarInt(packet.index);
    }

    private static BrinMusicFetchC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinMusicFetchC2SPacket(buf.readUtf(64), buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
