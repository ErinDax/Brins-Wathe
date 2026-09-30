package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinSkinPickResultS2CPacket(
    int token,
    int status,
    String kind,
    String skin,
    int applied
) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "skin_pick_result");
    public static final Type<BrinSkinPickResultS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinSkinPickResultS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinSkinPickResultS2CPacket::write, BrinSkinPickResultS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinSkinPickResultS2CPacket packet) {
        buf.writeVarInt(packet.token);
        buf.writeVarInt(packet.status);
        buf.writeUtf(packet.kind, 8);
        buf.writeUtf(packet.skin, 256);
        buf.writeVarInt(packet.applied);
    }

    private static BrinSkinPickResultS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinSkinPickResultS2CPacket(
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readUtf(8),
            buf.readUtf(256),
            buf.readVarInt()
        );
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
