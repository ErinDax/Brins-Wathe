package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record WelcomeS2CPacket(String roleId, int kind, int killers, int targets) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "welcome");
    public static final Type<WelcomeS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, WelcomeS2CPacket> STREAM_CODEC =
        StreamCodec.of(WelcomeS2CPacket::write, WelcomeS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, WelcomeS2CPacket packet) {
        buf.writeUtf(packet.roleId, 256);
        buf.writeVarInt(packet.kind);
        buf.writeVarInt(packet.killers);
        buf.writeVarInt(packet.targets);
    }

    private static WelcomeS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new WelcomeS2CPacket(buf.readUtf(256), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
