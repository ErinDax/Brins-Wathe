package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record AdminSaveC2SPacket(String json) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "admin_save");
    public static final Type<AdminSaveC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, AdminSaveC2SPacket> STREAM_CODEC =
        StreamCodec.of(AdminSaveC2SPacket::write, AdminSaveC2SPacket::read);
    public static final int MAX_LENGTH = 30000;

    private static void write(RegistryFriendlyByteBuf buf, AdminSaveC2SPacket packet) {
        buf.writeUtf(packet.json, MAX_LENGTH);
    }

    private static AdminSaveC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new AdminSaveC2SPacket(buf.readUtf(MAX_LENGTH));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
