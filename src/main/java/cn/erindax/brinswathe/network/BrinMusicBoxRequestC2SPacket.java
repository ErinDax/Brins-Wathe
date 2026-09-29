package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinMusicBoxRequestC2SPacket(int action) implements CustomPacketPayload {
    public static final int ACTION_QUERY = 0;
    public static final int ACTION_DELETE = 1;
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "music_box_request");
    public static final Type<BrinMusicBoxRequestC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinMusicBoxRequestC2SPacket> STREAM_CODEC =
        StreamCodec.of(BrinMusicBoxRequestC2SPacket::write, BrinMusicBoxRequestC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinMusicBoxRequestC2SPacket packet) {
        buf.writeVarInt(packet.action);
    }

    private static BrinMusicBoxRequestC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinMusicBoxRequestC2SPacket(buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
