package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinKnifeSkinApplyS2CPacket(String itemName, String skinName) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "knife_skin_apply");
    public static final Type<BrinKnifeSkinApplyS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinKnifeSkinApplyS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinKnifeSkinApplyS2CPacket::write, BrinKnifeSkinApplyS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinKnifeSkinApplyS2CPacket packet) {
        buf.writeUtf(packet.itemName);
        buf.writeUtf(packet.skinName);
    }

    private static BrinKnifeSkinApplyS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinKnifeSkinApplyS2CPacket(buf.readUtf(), buf.readUtf());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
