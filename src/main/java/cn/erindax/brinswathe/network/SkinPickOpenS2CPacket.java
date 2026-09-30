package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SkinPickOpenS2CPacket(
    int token,
    String kind,
    String skin,
    String senderName,
    int poolSize
) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "skin_pick_open");
    public static final Type<SkinPickOpenS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SkinPickOpenS2CPacket> STREAM_CODEC =
        StreamCodec.of(SkinPickOpenS2CPacket::write, SkinPickOpenS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, SkinPickOpenS2CPacket packet) {
        buf.writeVarInt(packet.token);
        buf.writeUtf(packet.kind, 8);
        buf.writeUtf(packet.skin, 256);
        buf.writeUtf(packet.senderName, 64);
        buf.writeVarInt(packet.poolSize);
    }

    private static SkinPickOpenS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new SkinPickOpenS2CPacket(
            buf.readVarInt(),
            buf.readUtf(8),
            buf.readUtf(256),
            buf.readUtf(64),
            buf.readVarInt()
        );
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
