package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinSkinSoundS2CPacket(
    String kind,
    String skin,
    double x,
    double y,
    double z,
    float volume,
    float pitch
) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "skin_sound");
    public static final Type<BrinSkinSoundS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinSkinSoundS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinSkinSoundS2CPacket::write, BrinSkinSoundS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinSkinSoundS2CPacket packet) {
        buf.writeUtf(packet.kind);
        buf.writeUtf(packet.skin);
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
        buf.writeFloat(packet.volume);
        buf.writeFloat(packet.pitch);
    }

    private static BrinSkinSoundS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinSkinSoundS2CPacket(
            buf.readUtf(),
            buf.readUtf(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readFloat(),
            buf.readFloat()
        );
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
