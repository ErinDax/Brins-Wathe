package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinSkinPickChoiceC2SPacket(int token, int action) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "skin_pick_choice");
    public static final Type<BrinSkinPickChoiceC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinSkinPickChoiceC2SPacket> STREAM_CODEC =
        StreamCodec.of(BrinSkinPickChoiceC2SPacket::write, BrinSkinPickChoiceC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinSkinPickChoiceC2SPacket packet) {
        buf.writeVarInt(packet.token);
        buf.writeVarInt(packet.action);
    }

    private static BrinSkinPickChoiceC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinSkinPickChoiceC2SPacket(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
