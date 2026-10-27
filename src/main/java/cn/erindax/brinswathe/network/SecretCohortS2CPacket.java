package cn.erindax.brinswathe.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SecretCohortS2CPacket(List<UUID> cohorts) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "secret_cohort");
    public static final Type<SecretCohortS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SecretCohortS2CPacket> STREAM_CODEC =
        StreamCodec.of(SecretCohortS2CPacket::write, SecretCohortS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, SecretCohortS2CPacket packet) {
        buf.writeVarInt(packet.cohorts.size());
        for (UUID id : packet.cohorts) buf.writeUUID(id);
    }

    private static SecretCohortS2CPacket read(RegistryFriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), 256);
        List<UUID> cohorts = new ArrayList<>(size);
        for (int i = 0; i < size; i++) cohorts.add(buf.readUUID());
        return new SecretCohortS2CPacket(cohorts);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
