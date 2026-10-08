package cn.erindax.brinswathe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BrinDraftCloseS2CPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "draft_close");
    public static final Type<BrinDraftCloseS2CPacket> TYPE = new Type<>(ID);
    public static final BrinDraftCloseS2CPacket INSTANCE = new BrinDraftCloseS2CPacket();
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinDraftCloseS2CPacket> STREAM_CODEC =
        StreamCodec.unit(INSTANCE);

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
