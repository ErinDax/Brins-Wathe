package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record KnifeSkinApplyS2CPacket(String itemName, String skinName) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "knife_skin_apply");
    public static final Type<KnifeSkinApplyS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, KnifeSkinApplyS2CPacket> STREAM_CODEC =
        StreamCodec.of(KnifeSkinApplyS2CPacket::write, KnifeSkinApplyS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, KnifeSkinApplyS2CPacket packet) {
        buf.writeUtf(packet.itemName);
        buf.writeUtf(packet.skinName);
    }

    private static KnifeSkinApplyS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new KnifeSkinApplyS2CPacket(buf.readUtf(), buf.readUtf());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
