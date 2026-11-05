package cn.erindax.brinswathe.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ArchivistWitnessS2CPacket(UUID target, int durationTicks) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "archivist_witness");
    public static final Type<ArchivistWitnessS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ArchivistWitnessS2CPacket> STREAM_CODEC =
        StreamCodec.of(ArchivistWitnessS2CPacket::write, ArchivistWitnessS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, ArchivistWitnessS2CPacket packet) {
        buf.writeUUID(packet.target);
        buf.writeVarInt(packet.durationTicks);
    }

    private static ArchivistWitnessS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new ArchivistWitnessS2CPacket(buf.readUUID(), buf.readVarInt());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
