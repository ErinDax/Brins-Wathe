package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record DraftProgressS2CPacket(int decided, int total) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "draft_progress");
    public static final Type<DraftProgressS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, DraftProgressS2CPacket> STREAM_CODEC =
        StreamCodec.of(DraftProgressS2CPacket::write, DraftProgressS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, DraftProgressS2CPacket packet) {
        buf.writeVarInt(packet.decided);
        buf.writeVarInt(packet.total);
    }

    private static DraftProgressS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new DraftProgressS2CPacket(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
