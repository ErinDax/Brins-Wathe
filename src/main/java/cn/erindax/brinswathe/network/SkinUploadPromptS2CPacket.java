package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SkinUploadPromptS2CPacket(
    String kind,
    String name,
    String tooltipName
) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "skin_upload_prompt");
    public static final Type<SkinUploadPromptS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SkinUploadPromptS2CPacket> STREAM_CODEC =
        StreamCodec.of(SkinUploadPromptS2CPacket::write, SkinUploadPromptS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, SkinUploadPromptS2CPacket packet) {
        buf.writeUtf(packet.kind, 16);
        buf.writeUtf(packet.name, 64);
        buf.writeUtf(packet.tooltipName, 64);
    }

    private static SkinUploadPromptS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new SkinUploadPromptS2CPacket(buf.readUtf(16), buf.readUtf(64), buf.readUtf(64));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
