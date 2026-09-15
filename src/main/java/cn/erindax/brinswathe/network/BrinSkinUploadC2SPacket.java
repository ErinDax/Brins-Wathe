package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinSkinUploadC2SPacket(
    String kind,
    String name,
    String tooltipName,
    byte[] texture,
    byte[] sound
) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "skin_upload");
    public static final Type<BrinSkinUploadC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinSkinUploadC2SPacket> STREAM_CODEC =
        StreamCodec.of(BrinSkinUploadC2SPacket::write, BrinSkinUploadC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinSkinUploadC2SPacket packet) {
        buf.writeUtf(packet.kind, 16);
        buf.writeUtf(packet.name, 64);
        buf.writeUtf(packet.tooltipName, 64);
        buf.writeByteArray(packet.texture);
        buf.writeByteArray(packet.sound);
    }

    private static BrinSkinUploadC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinSkinUploadC2SPacket(
            buf.readUtf(16),
            buf.readUtf(64),
            buf.readUtf(64),
            buf.readByteArray(512_000),
            buf.readByteArray(512_000)
        );
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
