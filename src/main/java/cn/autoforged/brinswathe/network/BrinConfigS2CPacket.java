package cn.autoforged.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinConfigS2CPacket(String json) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "config_sync");
    public static final Type<BrinConfigS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinConfigS2CPacket> STREAM_CODEC = StreamCodec.of(
        BrinConfigS2CPacket::write,
        BrinConfigS2CPacket::read
    );

    private static void write(RegistryFriendlyByteBuf buf, BrinConfigS2CPacket packet) {
        buf.writeUtf(packet.json, 65535);
    }

    private static BrinConfigS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinConfigS2CPacket(buf.readUtf(65535));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
