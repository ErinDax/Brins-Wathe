package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ResourceReloadS2CPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "resource_reload");
    public static final Type<ResourceReloadS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceReloadS2CPacket> STREAM_CODEC = StreamCodec.of(
        ResourceReloadS2CPacket::write,
        ResourceReloadS2CPacket::read
    );

    private static void write(RegistryFriendlyByteBuf buf, ResourceReloadS2CPacket packet) {
    }

    private static ResourceReloadS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new ResourceReloadS2CPacket();
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
