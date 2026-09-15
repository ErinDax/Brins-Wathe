package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SkinUploadC2SPacket(
    String kind,
    String name,
    String tooltipName,
    byte[] texture,
    byte[] sound
) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "skin_upload");
    public static final Type<SkinUploadC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SkinUploadC2SPacket> STREAM_CODEC =
        StreamCodec.of(SkinUploadC2SPacket::write, SkinUploadC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, SkinUploadC2SPacket packet) {
        buf.writeUtf(packet.kind, 16);
        buf.writeUtf(packet.name, 64);
        buf.writeUtf(packet.tooltipName, 64);
        buf.writeByteArray(packet.texture);
        buf.writeByteArray(packet.sound);
    }

    private static SkinUploadC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new SkinUploadC2SPacket(
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
