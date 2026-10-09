package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinAdminSnapshotS2CPacket(boolean open, String json) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "admin_snapshot");
    public static final Type<BrinAdminSnapshotS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinAdminSnapshotS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinAdminSnapshotS2CPacket::write, BrinAdminSnapshotS2CPacket::read);
    private static final int MAX_LENGTH = 262144;

    private static void write(RegistryFriendlyByteBuf buf, BrinAdminSnapshotS2CPacket packet) {
        buf.writeBoolean(packet.open);
        buf.writeUtf(packet.json, MAX_LENGTH);
    }

    private static BrinAdminSnapshotS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinAdminSnapshotS2CPacket(buf.readBoolean(), buf.readUtf(MAX_LENGTH));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
