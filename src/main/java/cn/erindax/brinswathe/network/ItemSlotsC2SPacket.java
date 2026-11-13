package cn.erindax.brinswathe.network;

import io.netty.handler.codec.DecoderException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ItemSlotsC2SPacket(Map<ResourceLocation, Integer> slots) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "item_slots");
    public static final Type<ItemSlotsC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemSlotsC2SPacket> STREAM_CODEC =
        StreamCodec.of(ItemSlotsC2SPacket::write, ItemSlotsC2SPacket::read);
    public static final int MAX_ENTRIES = 256;

    private static void write(RegistryFriendlyByteBuf buf, ItemSlotsC2SPacket packet) {
        buf.writeVarInt(packet.slots.size());
        for (Map.Entry<ResourceLocation, Integer> entry : packet.slots.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
    }

    private static ItemSlotsC2SPacket read(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size < 0 || size > MAX_ENTRIES) throw new DecoderException("Too many item slots: " + size);
        Map<ResourceLocation, Integer> slots = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            slots.put(buf.readResourceLocation(), buf.readVarInt());
        }
        return new ItemSlotsC2SPacket(slots);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
