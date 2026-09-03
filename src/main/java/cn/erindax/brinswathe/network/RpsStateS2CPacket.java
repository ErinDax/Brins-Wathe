package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record RpsStateS2CPacket(
    int phase,
    String opponentName,
    int secondsLeft,
    int yourChoice,
    int theirChoice,
    int outcome
) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "rps_state");
    public static final Type<RpsStateS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, RpsStateS2CPacket> STREAM_CODEC = StreamCodec.of(
        RpsStateS2CPacket::write,
        RpsStateS2CPacket::read
    );

    public static final int CLEAR = 0;
    public static final int INVITE = 1;
    public static final int PENDING = 2;
    public static final int PICK = 3;
    public static final int WAIT = 4;
    public static final int RESULT = 5;

    public static final int OUTCOME_NONE = 0;
    public static final int OUTCOME_WIN = 1;
    public static final int OUTCOME_LOSE = 2;
    public static final int OUTCOME_DRAW = 3;

    public static RpsStateS2CPacket clear() {
        return new RpsStateS2CPacket(CLEAR, "", 0, -1, -1, OUTCOME_NONE);
    }

    private static void write(RegistryFriendlyByteBuf buf, RpsStateS2CPacket packet) {
        buf.writeVarInt(packet.phase);
        buf.writeUtf(packet.opponentName, 32);
        buf.writeVarInt(packet.secondsLeft);
        buf.writeVarInt(packet.yourChoice);
        buf.writeVarInt(packet.theirChoice);
        buf.writeVarInt(packet.outcome);
    }

    private static RpsStateS2CPacket read(RegistryFriendlyByteBuf buf) {
        return new RpsStateS2CPacket(
            buf.readVarInt(),
            buf.readUtf(32),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt()
        );
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
