package cn.erindax.brinswathe.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record BrinInstinctSnapshotS2CPacket(List<Entry> entries) implements CustomPacketPayload {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "instinct_snapshot");
    public static final Type<BrinInstinctSnapshotS2CPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinInstinctSnapshotS2CPacket> STREAM_CODEC =
        StreamCodec.of(BrinInstinctSnapshotS2CPacket::write, BrinInstinctSnapshotS2CPacket::read);

    public record Entry(
        UUID uuid,
        String name,
        double x,
        double y,
        double z,
        float yaw,
        float mood,
        boolean spectatorOrCreative,
        boolean canUseKillerFeatures,
        boolean innocent,
        boolean psycho
    ) {
        public Vec3 position() {
            return new Vec3(x, y, z);
        }
    }

    private static void write(RegistryFriendlyByteBuf buf, BrinInstinctSnapshotS2CPacket packet) {
        buf.writeVarInt(packet.entries.size());
        for (Entry entry : packet.entries) {
            buf.writeUUID(entry.uuid);
            buf.writeUtf(entry.name, 64);
            buf.writeDouble(entry.x);
            buf.writeDouble(entry.y);
            buf.writeDouble(entry.z);
            buf.writeFloat(entry.yaw);
            buf.writeFloat(entry.mood);
            int flags = 0;
            if (entry.spectatorOrCreative) flags |= 1;
            if (entry.canUseKillerFeatures) flags |= 2;
            if (entry.innocent) flags |= 4;
            if (entry.psycho) flags |= 8;
            buf.writeByte(flags);
        }
    }

    private static BrinInstinctSnapshotS2CPacket read(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            UUID uuid = buf.readUUID();
            String name = buf.readUtf(64);
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            float yaw = buf.readFloat();
            float mood = buf.readFloat();
            int flags = buf.readByte() & 255;
            entries.add(new Entry(
                uuid,
                name,
                x,
                y,
                z,
                yaw,
                mood,
                (flags & 1) != 0,
                (flags & 2) != 0,
                (flags & 4) != 0,
                (flags & 8) != 0
            ));
        }
        return new BrinInstinctSnapshotS2CPacket(entries);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
