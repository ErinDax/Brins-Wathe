package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record InstinctC2SPacket(boolean enabled) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "instinct_c2s");
    public static final Type<InstinctC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, InstinctC2SPacket> STREAM_CODEC =
        StreamCodec.of(InstinctC2SPacket::write, InstinctC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, InstinctC2SPacket packet) {
        buf.writeBoolean(packet.enabled);
    }

    private static InstinctC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new InstinctC2SPacket(buf.readBoolean());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
