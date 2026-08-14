package cn.autoforged.brinswathe.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BrinAbilityC2SPacket(
    int abilityType,
    @Nullable UUID targetId
) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "ability");
    public static final Type<BrinAbilityC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrinAbilityC2SPacket> STREAM_CODEC = StreamCodec.of(
        BrinAbilityC2SPacket::write,
        BrinAbilityC2SPacket::read
    );

    public static final int ABILITY_PUPPETEER_CRAFT = 0;
    public static final int ABILITY_PUPPETEER_SUMMON = 1;
    public static final int ABILITY_PUPPETEER_RETURN = 7;
    public static final int ABILITY_STUNT_DOUBLE_MIMIC = 2;
    public static final int ABILITY_MEDIUM_JOIN_VOICE = 3;
    public static final int ABILITY_TRAPPER_PLACE_TRAP = 4;
    public static final int ABILITY_NIGHTMARE_PLANT = 5;
    public static final int ABILITY_ILLUSIONIST_CLONES = 6;
    public static final int ABILITY_ARCHIVIST_SEAL = 8;
    public static final int ABILITY_GAMBLER_BET = 10;
    public static final int ABILITY_EAVESDROPPER_CHANNEL = 14;
    public static final int ABILITY_NIGHTMARE_FORCE_SLEEP = 15;
    public static final int ABILITY_ILLUSIONIST_SWITCH_CONTROL = 11;
    public static final int ABILITY_SNIPER_TOGGLE_OR_FIRE = 12;
    public static final int ABILITY_SNIPER_CANCEL = 13;
    public static final int ABILITY_WATCHMAN_RESCUE = 16;
    public static final int ABILITY_ZHANGSHI_SPEED = 17;
    public static final int ABILITY_MORTICIAN_DISGUISE = 18;
    public static final int ABILITY_COWBOY_DUEL = 19;

    private static void write(RegistryFriendlyByteBuf buf, BrinAbilityC2SPacket packet) {
        buf.writeVarInt(packet.abilityType);
        buf.writeBoolean(packet.targetId != null);
        if (packet.targetId != null) {
            buf.writeUUID(packet.targetId);
        }
    }

    private static BrinAbilityC2SPacket read(RegistryFriendlyByteBuf buf) {
        int abilityType = buf.readVarInt();
        boolean hasTarget = buf.readBoolean();
        UUID targetId = hasTarget ? buf.readUUID() : null;
        return new BrinAbilityC2SPacket(abilityType, targetId);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
