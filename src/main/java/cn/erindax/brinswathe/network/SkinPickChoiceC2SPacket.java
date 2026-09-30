package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SkinPickChoiceC2SPacket(int token, int action) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "skin_pick_choice");
    public static final Type<SkinPickChoiceC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SkinPickChoiceC2SPacket> STREAM_CODEC =
        StreamCodec.of(SkinPickChoiceC2SPacket::write, SkinPickChoiceC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, SkinPickChoiceC2SPacket packet) {
        buf.writeVarInt(packet.token);
        buf.writeVarInt(packet.action);
    }

    private static SkinPickChoiceC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new SkinPickChoiceC2SPacket(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
