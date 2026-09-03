package cn.erindax.brinswathe.network;

import cn.erindax.brinswathe.BrinIcFlags;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record BrinIcNightVisionS2CPacket(boolean enabled, boolean hudNamesThroughWalls) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "instinct_night_vision");
    public static final Type<BrinIcNightVisionS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinIcNightVisionS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinIcNightVisionS2CPacket::write, BrinIcNightVisionS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, BrinIcNightVisionS2CPacket packet) {
        buf.writeBoolean(packet.enabled);
        buf.writeBoolean(packet.hudNamesThroughWalls);
    }

    private static BrinIcNightVisionS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new BrinIcNightVisionS2CPacket(buf.readBoolean(), buf.readBoolean());
    }

    public static void sendTo(ServerPlayer player) {
        ServerPlayNetworking.send(player, new BrinIcNightVisionS2CPacket(
            BrinIcFlags.instinctNightVision,
            BrinIcFlags.instinctHudNamesThroughWalls
        ));
    }

    public static void sendToAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendTo(player);
        }
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
