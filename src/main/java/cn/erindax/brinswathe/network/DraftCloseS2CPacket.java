package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record DraftCloseS2CPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "draft_close");
    public static final Type<DraftCloseS2CPacket> TYPE = new Type<>(ID);
    public static final DraftCloseS2CPacket INSTANCE = new DraftCloseS2CPacket();
    public static final StreamCodec<RegistryFriendlyByteBuf, DraftCloseS2CPacket> STREAM_CODEC =
        StreamCodec.unit(INSTANCE);

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
