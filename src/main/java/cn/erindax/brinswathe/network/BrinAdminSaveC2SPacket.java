package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinAdminSaveC2SPacket(String json) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "admin_save");
    public static final Type<BrinAdminSaveC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinAdminSaveC2SPacket> STREAM_CODEC =
        StreamCodec.of(BrinAdminSaveC2SPacket::write, BrinAdminSaveC2SPacket::read);
    public static final int MAX_LENGTH = 30000;

    private static void write(RegistryFriendlyByteBuf buf, BrinAdminSaveC2SPacket packet) {
        buf.writeUtf(packet.json, MAX_LENGTH);
    }

    private static BrinAdminSaveC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinAdminSaveC2SPacket(buf.readUtf(MAX_LENGTH));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
