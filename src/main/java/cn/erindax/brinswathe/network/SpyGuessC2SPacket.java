package cn.erindax.brinswathe.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SpyGuessC2SPacket(UUID targetId, String roleId) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "spy_guess");
    public static final Type<SpyGuessC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SpyGuessC2SPacket> STREAM_CODEC =
        StreamCodec.of(SpyGuessC2SPacket::write, SpyGuessC2SPacket::read);

    private static void write(RegistryFriendlyByteBuf buf, SpyGuessC2SPacket packet) {
        buf.writeUUID(packet.targetId);
        buf.writeUtf(packet.roleId, 256);
    }

    private static SpyGuessC2SPacket read(RegistryFriendlyByteBuf buf) {
        return new SpyGuessC2SPacket(buf.readUUID(), buf.readUtf(256));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
