package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinInstinctC2SPacket(boolean enabled) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "instinct_c2s");
    public static final Type<BrinInstinctC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinInstinctC2SPacket> STREAM_CODEC =
        StreamCodec.of(BrinInstinctC2SPacket::write, BrinInstinctC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinInstinctC2SPacket packet) {
        buf.writeBoolean(packet.enabled);
    }

    private static BrinInstinctC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinInstinctC2SPacket(buf.readBoolean());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
