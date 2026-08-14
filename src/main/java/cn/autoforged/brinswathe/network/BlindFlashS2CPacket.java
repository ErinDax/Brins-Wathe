package cn.autoforged.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BlindFlashS2CPacket(int durationTicks) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "blind_flash");
    public static final Type<BlindFlashS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BlindFlashS2CPacket> STREAM_CODEC = StreamCodec.of(
        BlindFlashS2CPacket::write,
        BlindFlashS2CPacket::read
    );

    private static void write(RegistryFriendlyByteBuf buf, BlindFlashS2CPacket packet) {
        buf.writeVarInt(packet.durationTicks);
    }

    private static BlindFlashS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BlindFlashS2CPacket(buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
