package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinDraftChoiceC2SPacket(String roleId) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "draft_choice");
    public static final Type<BrinDraftChoiceC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinDraftChoiceC2SPacket> STREAM_CODEC =
        StreamCodec.of(BrinDraftChoiceC2SPacket::write, BrinDraftChoiceC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinDraftChoiceC2SPacket packet) {
        buf.writeUtf(packet.roleId, 256);
    }

    private static BrinDraftChoiceC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinDraftChoiceC2SPacket(buf.readUtf(256));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
