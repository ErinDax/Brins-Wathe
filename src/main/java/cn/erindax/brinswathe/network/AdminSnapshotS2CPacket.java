package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record AdminSnapshotS2CPacket(boolean open, String json) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "admin_snapshot");
    public static final Type<AdminSnapshotS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, AdminSnapshotS2CPacket> STREAM_CODEC =
        StreamCodec.of(AdminSnapshotS2CPacket::write, AdminSnapshotS2CPacket::read);
    private static final int MAX_LENGTH = 262144;

    private static void write(RegistryFriendlyByteBuf buf, AdminSnapshotS2CPacket packet) {
        buf.writeBoolean(packet.open);
        buf.writeUtf(packet.json, MAX_LENGTH);
    }

    private static AdminSnapshotS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new AdminSnapshotS2CPacket(buf.readBoolean(), buf.readUtf(MAX_LENGTH));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
