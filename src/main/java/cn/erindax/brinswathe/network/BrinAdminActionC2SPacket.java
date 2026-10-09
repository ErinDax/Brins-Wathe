package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinAdminActionC2SPacket(String action) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "admin_action");
    public static final Type<BrinAdminActionC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinAdminActionC2SPacket> STREAM_CODEC =
        StreamCodec.of(BrinAdminActionC2SPacket::write, BrinAdminActionC2SPacket::read);
    private static final int MAX_LENGTH = 32;

    private static void write(RegistryFriendlyByteBuf buf, BrinAdminActionC2SPacket packet) {
        buf.writeUtf(packet.action, MAX_LENGTH);
    }

    private static BrinAdminActionC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinAdminActionC2SPacket(buf.readUtf(MAX_LENGTH));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
