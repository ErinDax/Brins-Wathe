package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record AdminActionC2SPacket(String action) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "admin_action");
    public static final Type<AdminActionC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, AdminActionC2SPacket> STREAM_CODEC =
        StreamCodec.of(AdminActionC2SPacket::write, AdminActionC2SPacket::read);
    private static final int MAX_LENGTH = 32;

    private static void write(RegistryFriendlyByteBuf buf, AdminActionC2SPacket packet) {
        buf.writeUtf(packet.action, MAX_LENGTH);
    }

    private static AdminActionC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new AdminActionC2SPacket(buf.readUtf(MAX_LENGTH));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
