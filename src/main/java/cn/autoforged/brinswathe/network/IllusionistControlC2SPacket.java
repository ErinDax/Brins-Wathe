package cn.autoforged.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record IllusionistControlC2SPacket(
    UUID cloneId,
    float forward,
    float strafe,
    float yaw,
    float pitch
) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        "brinswathe",
        "illusionist_control"
    );
    public static final Type<IllusionistControlC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, IllusionistControlC2SPacket> STREAM_CODEC =
        StreamCodec.of(IllusionistControlC2SPacket::write, IllusionistControlC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buffer, IllusionistControlC2SPacket packet) {
        buffer.writeUUID(packet.cloneId);
        buffer.writeFloat(packet.forward);
        buffer.writeFloat(packet.strafe);
        buffer.writeFloat(packet.yaw);
        buffer.writeFloat(packet.pitch);
    }

    private static IllusionistControlC2SPacket read(RegistryFriendlyByteBuf buffer) {
        return new IllusionistControlC2SPacket(
            buffer.readUUID(),
            buffer.readFloat(),
            buffer.readFloat(),
            buffer.readFloat(),
            buffer.readFloat()
        );
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
