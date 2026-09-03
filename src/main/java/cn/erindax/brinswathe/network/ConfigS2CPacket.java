package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ConfigS2CPacket(String json) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "config_sync");
    public static final Type<ConfigS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigS2CPacket> STREAM_CODEC = StreamCodec.of(
        ConfigS2CPacket::write,
        ConfigS2CPacket::read
    );

    private static void write(RegistryFriendlyByteBuf buf, ConfigS2CPacket packet) {
        buf.writeUtf(packet.json, 65535);
    }

    private static ConfigS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new ConfigS2CPacket(buf.readUtf(65535));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
