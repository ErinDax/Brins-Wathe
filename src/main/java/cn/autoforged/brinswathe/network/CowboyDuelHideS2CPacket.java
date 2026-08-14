package cn.autoforged.brinswathe.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Tells spectator clients to skip rendering the two duelists. Used only while screens are still
 * black after the warp; the countdown walk is meant to be seen.
 */
public record CowboyDuelHideS2CPacket(boolean hide, UUID first, UUID second) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "cowboy_duel_hide");
    public static final Type<CowboyDuelHideS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, CowboyDuelHideS2CPacket> STREAM_CODEC =
        StreamCodec.of(CowboyDuelHideS2CPacket::write, CowboyDuelHideS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, CowboyDuelHideS2CPacket packet) {
        buf.writeBoolean(packet.hide);
        buf.writeUUID(packet.first);
        buf.writeUUID(packet.second);
    }

    private static CowboyDuelHideS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new CowboyDuelHideS2CPacket(buf.readBoolean(), buf.readUUID(), buf.readUUID());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
