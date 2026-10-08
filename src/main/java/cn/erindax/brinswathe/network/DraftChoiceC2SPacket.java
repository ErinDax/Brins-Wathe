package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record DraftChoiceC2SPacket(String roleId) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "draft_choice");
    public static final Type<DraftChoiceC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, DraftChoiceC2SPacket> STREAM_CODEC =
        StreamCodec.of(DraftChoiceC2SPacket::write, DraftChoiceC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, DraftChoiceC2SPacket packet) {
        buf.writeUtf(packet.roleId, 256);
    }

    private static DraftChoiceC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new DraftChoiceC2SPacket(buf.readUtf(256));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
