package cn.erindax.brinswathe.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinDraftStateS2CPacket(
    boolean open,
    int number,
    int total,
    int current,
    int seconds,
    List<String> offers,
    String picked,
    boolean random
) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "draft_state");
    public static final Type<BrinDraftStateS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinDraftStateS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinDraftStateS2CPacket::write, BrinDraftStateS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinDraftStateS2CPacket packet) {
        buf.writeBoolean(packet.open);
        buf.writeVarInt(packet.number);
        buf.writeVarInt(packet.total);
        buf.writeVarInt(packet.current);
        buf.writeVarInt(packet.seconds);
        buf.writeVarInt(packet.offers.size());
        for (String roleId : packet.offers) buf.writeUtf(roleId, 256);
        buf.writeUtf(packet.picked, 256);
        buf.writeBoolean(packet.random);
    }

    private static BrinDraftStateS2CPacket read(RegistryFriendlyByteBuf buf) {
        boolean open = buf.readBoolean();
        int number = buf.readVarInt();
        int total = buf.readVarInt();
        int current = buf.readVarInt();
        int seconds = buf.readVarInt();
        int size = Math.min(buf.readVarInt(), 8);
        List<String> offers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) offers.add(buf.readUtf(256));
        String picked = buf.readUtf(256);
        boolean random = buf.readBoolean();
        return new BrinDraftStateS2CPacket(open, number, total, current, seconds, offers, picked, random);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
