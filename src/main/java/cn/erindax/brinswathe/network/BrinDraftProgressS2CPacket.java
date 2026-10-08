package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinDraftProgressS2CPacket(int decided, int total) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "draft_progress");
    public static final Type<BrinDraftProgressS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinDraftProgressS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinDraftProgressS2CPacket::write, BrinDraftProgressS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinDraftProgressS2CPacket packet) {
        buf.writeVarInt(packet.decided);
        buf.writeVarInt(packet.total);
    }

    private static BrinDraftProgressS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinDraftProgressS2CPacket(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
