package cn.erindax.brinswathe.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record KnifeSkinListS2CPacket(List<SkinEntry> skins) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "knife_skins");
    public static final Type<KnifeSkinListS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, KnifeSkinListS2CPacket> STREAM_CODEC =
        StreamCodec.of(KnifeSkinListS2CPacket::write, KnifeSkinListS2CPacket::read);

    public record SkinEntry(String name, String tooltipName, byte[] texture) {
    }

    private static void write(RegistryFriendlyByteBuf buf, KnifeSkinListS2CPacket packet) {
        buf.writeVarInt(packet.skins.size());
        for (SkinEntry entry : packet.skins) {
            buf.writeUtf(entry.name);
            buf.writeUtf(entry.tooltipName);
            buf.writeByteArray(entry.texture);
        }
    }

    private static KnifeSkinListS2CPacket read(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<SkinEntry> skins = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            skins.add(new SkinEntry(buf.readUtf(), buf.readUtf(), buf.readByteArray()));
        }
        return new KnifeSkinListS2CPacket(skins);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
