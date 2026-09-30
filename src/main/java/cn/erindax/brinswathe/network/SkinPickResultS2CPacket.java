package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SkinPickResultS2CPacket(
    int token,
    int status,
    String kind,
    String skin,
    int applied
) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "skin_pick_result");
    public static final Type<SkinPickResultS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SkinPickResultS2CPacket> STREAM_CODEC =
        StreamCodec.of(SkinPickResultS2CPacket::write, SkinPickResultS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, SkinPickResultS2CPacket packet) {
        buf.writeVarInt(packet.token);
        buf.writeVarInt(packet.status);
        buf.writeUtf(packet.kind, 8);
        buf.writeUtf(packet.skin, 256);
        buf.writeVarInt(packet.applied);
    }

    private static SkinPickResultS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new SkinPickResultS2CPacket(
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
