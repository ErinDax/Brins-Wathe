package cn.erindax.brinswathe.network;

import cn.erindax.brinswathe.IcFlags;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record IcNightVisionS2CPacket(boolean enabled, boolean hudNamesThroughWalls) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "instinct_night_vision");
    public static final Type<IcNightVisionS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, IcNightVisionS2CPacket> STREAM_CODEC =
        StreamCodec.of(IcNightVisionS2CPacket::write, IcNightVisionS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, IcNightVisionS2CPacket packet) {
        buf.writeBoolean(packet.enabled);
        buf.writeBoolean(packet.hudNamesThroughWalls);
    }

    private static IcNightVisionS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new IcNightVisionS2CPacket(buf.readBoolean(), buf.readBoolean());
    }

    public static void sendTo(ServerPlayer player) {
        ServerPlayNetworking.send(player, new IcNightVisionS2CPacket(
            IcFlags.instinctNightVision,
            IcFlags.instinctHudNamesThroughWalls
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
