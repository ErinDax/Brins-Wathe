package cn.erindax.brinswathe.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinDraftOpenS2CPacket(List<String> roleIds, int seconds) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "draft_open");
    public static final Type<BrinDraftOpenS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinDraftOpenS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinDraftOpenS2CPacket::write, BrinDraftOpenS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinDraftOpenS2CPacket packet) {
        buf.writeVarInt(packet.roleIds.size());
        for (String roleId : packet.roleIds) buf.writeUtf(roleId, 256);
        buf.writeVarInt(packet.seconds);
    }

    private static BrinDraftOpenS2CPacket read(RegistryFriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), 8);
        List<String> roleIds = new ArrayList<>(size);
        for (int i = 0; i < size; i++) roleIds.add(buf.readUtf(256));
        return new BrinDraftOpenS2CPacket(roleIds, buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
