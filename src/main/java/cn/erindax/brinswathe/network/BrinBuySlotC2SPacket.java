package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinBuySlotC2SPacket(int slot) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "buy_slot");
    public static final Type<BrinBuySlotC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinBuySlotC2SPacket> STREAM_CODEC =
        StreamCodec.of(BrinBuySlotC2SPacket::write, BrinBuySlotC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinBuySlotC2SPacket packet) {
        buf.writeVarInt(packet.slot);
    }

    private static BrinBuySlotC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinBuySlotC2SPacket(buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
