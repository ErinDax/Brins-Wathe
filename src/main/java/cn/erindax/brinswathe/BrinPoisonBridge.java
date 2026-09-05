package cn.erindax.brinswathe;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.roles.dreamer.DreamerKillerComponent;

public final class BrinPoisonBridge {
    public static final UUID UNKNOWN_POISONER = UUID.fromString("00000000-0000-4000-a000-00000000b0b0");
    public static final UUID DELUSION_MARKER = UUID.fromString("00000000-0000-0000-dead-c0de00000000");

    private static boolean setPoisonBodyReached;
    private static boolean resetBodyReached;
    private static boolean delusionLookupAttempted;
    private static Field delusionField;

    private BrinPoisonBridge() {
    }

    public static void markSetPoisonBodyReached() {
        setPoisonBodyReached = true;
    }

    public static void markResetBodyReached() {
        resetBodyReached = true;
    }

    public static void beginSetPoison() {
        setPoisonBodyReached = false;
    }

    public static void beginReset() {
        resetBodyReached = false;
    }

    public static boolean setPoisonBodyReached() {
        return setPoisonBodyReached;
    }

    public static boolean resetBodyReached() {
        return resetBodyReached;
    }

    public static Role bartenderRole() {
        return BrinNoelleAccess.findRole(BrinNoelleAccess.BARTENDER_ID);
    }

    public static boolean isOnlineBartender(GameWorldComponent game, Player victim, UUID poisoner) {
        Role bartender = bartenderRole();
        if (bartender == null || poisoner == null || !game.isRole(poisoner, bartender)) return false;
        return victim.level().getPlayerByUUID(poisoner) != null;
    }

    public static void setDelusionFlag(PlayerPoisonComponent component, boolean delusion) {
        Field field = delusionField(component);
        if (field == null) return;
        try {
            field.setBoolean(component, delusion);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    public static void applyKinsPoisonSideEffects(GameWorldComponent game, Player victim, int ticks, UUID poisoner) {
        if (ticks <= 0 || GameFunctions.isPlayerSpectatingOrCreative(victim)) return;
        if (!(victim instanceof ServerPlayer)) return;
        MinecraftServer server = victim.getServer();
        if (server == null) return;
        boolean victimRobot = game.isRole(victim, KinsWatheRoles.ROBOT);
        boolean delusion = DELUSION_MARKER.equals(poisoner);
        Role bartender = bartenderRole();
        boolean fromBartender = bartender != null && poisoner != null && game.isRole(poisoner, bartender);

        if (!victimRobot && !fromBartender) {
            for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                if (!GameFunctions.isPlayerAliveAndSurvival(other)) continue;
                if (!delusion && game.isRole(other, KinsWatheRoles.DRUGMAKER)) {
                    PlayerShopComponent shop = PlayerShopComponent.KEY.get(other);
                    other.displayClientMessage(
                        Component.translatable("tip.kinswathe.drugmaker.poisoned").withColor(KinsWatheRoles.DRUGMAKER.color()),
                        true
                    );
                    shop.balance += KinsWatheConfig.HANDLER.instance().DrugmakerGetCoins;
                    shop.sync();
                }
                if (game.isRole(other, KinsWatheRoles.PHYSICIAN)) {
                    other.displayClientMessage(
                        Component.translatable("tip.kinswathe.physician.poisoned").withColor(Color.RED.getRGB()),
                        true
                    );
                }
            }
        }

        if (delusion
            && !game.canUseKillerFeatures(victim)
            && !victimRobot
            && !game.isRole(victim, KinsWatheRoles.DREAMER)) {
            for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                if (!game.isRole(other, KinsWatheRoles.DREAMER) || !GameFunctions.isPlayerAliveAndSurvival(other)) continue;
                DreamerKillerComponent dreamer = DreamerKillerComponent.KEY.get(other);
                other.displayClientMessage(
                    Component.translatable("tip.kinswathe.dreamer.fake_poisoned").withColor(KinsWatheRoles.DREAMER.color()),
                    true
                );
                if (!dreamer.hasBecomeKiller) {
                    dreamer.dreamerCounts++;
                    dreamer.sync();
                }
            }
        }
    }

    private static Field delusionField(PlayerPoisonComponent component) {
        if (delusionLookupAttempted) return delusionField;
        delusionLookupAttempted = true;
        for (Field field : component.getClass().getDeclaredFields()) {
            if (field.getType() == boolean.class && field.getName().contains("isDelusionPoison")) {
                field.trySetAccessible();
                delusionField = field;
                break;
            }
        }
        if (delusionField == null) {
            BrinsWathe.LOGGER.warn("[poison] kins isDelusionPoison field not found on PlayerPoisonComponent; delusion vials will act as real poison");
        }
        return delusionField;
    }
}
