package cn.autoforged.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Lasts the whole duel, unlike the duelist-model hide which only covers the black warp.
 * Spectator clients use this to stop drawing roles, cohort tags and instinct colours.
 */
public record CowboyDuelIdentityS2CPacket(boolean hide) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "cowboy_duel_identity");
    public static final Type<CowboyDuelIdentityS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, CowboyDuelIdentityS2CPacket> STREAM_CODEC =
        StreamCodec.of(CowboyDuelIdentityS2CPacket::write, CowboyDuelIdentityS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, CowboyDuelIdentityS2CPacket packet) {
        buf.writeBoolean(packet.hide);
    }

    private static CowboyDuelIdentityS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new CowboyDuelIdentityS2CPacket(buf.readBoolean());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
