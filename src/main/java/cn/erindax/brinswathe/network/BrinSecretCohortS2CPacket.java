package cn.erindax.brinswathe.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinSecretCohortS2CPacket(List<UUID> cohorts) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "secret_cohort");
    public static final Type<BrinSecretCohortS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinSecretCohortS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinSecretCohortS2CPacket::write, BrinSecretCohortS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinSecretCohortS2CPacket packet) {
        buf.writeVarInt(packet.cohorts.size());
        for (UUID id : packet.cohorts) buf.writeUUID(id);
    }

    private static BrinSecretCohortS2CPacket read(RegistryFriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), 256);
        List<UUID> cohorts = new ArrayList<>(size);
        for (int i = 0; i < size; i++) cohorts.add(buf.readUUID());
        return new BrinSecretCohortS2CPacket(cohorts);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
