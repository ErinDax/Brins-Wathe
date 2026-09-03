package cn.erindax.brinswathe.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RpsActionC2SPacket(
    int action,
    @Nullable UUID targetId,
    int choice
) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "rps_action");
    public static final Type<RpsActionC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, RpsActionC2SPacket> STREAM_CODEC = StreamCodec.of(
        RpsActionC2SPacket::write,
        RpsActionC2SPacket::read
    );

    public static final int INVITE = 0;
    public static final int ACCEPT = 1;
    public static final int DECLINE = 2;
    public static final int CHOOSE = 3;

    public static RpsActionC2SPacket invite(UUID targetId) {
        return new RpsActionC2SPacket(INVITE, targetId, -1);
    }

    public static RpsActionC2SPacket accept() {
        return new RpsActionC2SPacket(ACCEPT, null, -1);
    }

    public static RpsActionC2SPacket decline() {
        return new RpsActionC2SPacket(DECLINE, null, -1);
    }

    public static RpsActionC2SPacket choose(int choice) {
        return new RpsActionC2SPacket(CHOOSE, null, choice);
    }

    private static void write(RegistryFriendlyByteBuf buf, RpsActionC2SPacket packet) {
        buf.writeVarInt(packet.action);
        buf.writeBoolean(packet.targetId != null);
        if (packet.targetId != null) buf.writeUUID(packet.targetId);
        buf.writeVarInt(packet.choice);
    }

    private static RpsActionC2SPacket read(RegistryFriendlyByteBuf buf) {
        int action = buf.readVarInt();
        UUID targetId = buf.readBoolean() ? buf.readUUID() : null;
        return new RpsActionC2SPacket(action, targetId, buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
