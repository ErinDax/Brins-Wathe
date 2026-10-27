package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinWelcomeS2CPacket(String roleId, int kind, int killers, int targets) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "welcome");
    public static final Type<BrinWelcomeS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinWelcomeS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinWelcomeS2CPacket::write, BrinWelcomeS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinWelcomeS2CPacket packet) {
        buf.writeUtf(packet.roleId, 256);
        buf.writeVarInt(packet.kind);
        buf.writeVarInt(packet.killers);
        buf.writeVarInt(packet.targets);
    }

    private static BrinWelcomeS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinWelcomeS2CPacket(buf.readUtf(256), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
