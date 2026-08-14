package cn.autoforged.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Mouse look is refused on the two duelists until guns are drawn. The black overlay used to carry
 * that lock; the countdown walk is visible now, so the lock is a separate flag.
 */
public record CowboyDuelLookLockS2CPacket(boolean lock) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "cowboy_duel_look_lock");
    public static final Type<CowboyDuelLookLockS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, CowboyDuelLookLockS2CPacket> STREAM_CODEC =
        StreamCodec.of(CowboyDuelLookLockS2CPacket::write, CowboyDuelLookLockS2CPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, CowboyDuelLookLockS2CPacket packet) {
        buf.writeBoolean(packet.lock);
    }

    private static CowboyDuelLookLockS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new CowboyDuelLookLockS2CPacket(buf.readBoolean());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
