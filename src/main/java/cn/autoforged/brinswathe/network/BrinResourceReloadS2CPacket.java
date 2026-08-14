package cn.autoforged.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinResourceReloadS2CPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "resource_reload");
    public static final Type<BrinResourceReloadS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinResourceReloadS2CPacket> STREAM_CODEC = StreamCodec.of(
        BrinResourceReloadS2CPacket::write,
        BrinResourceReloadS2CPacket::read
    );

    private static void write(RegistryFriendlyByteBuf buf, BrinResourceReloadS2CPacket packet) {
    }

    private static BrinResourceReloadS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinResourceReloadS2CPacket();
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
