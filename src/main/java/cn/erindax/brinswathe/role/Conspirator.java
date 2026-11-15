package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.AbilityCost;
import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.ConspiratorComponent;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import cn.erindax.brinswathe.voice.ModVoiceChatPlugin;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;

public final class Conspirator {
    private Conspirator() {
    }

    public static boolean isConspirator(GameWorldComponent game, Player player) {
        return game != null && player != null && game.isRole(player, ModRoles.CONSPIRATOR);
    }

    public static boolean isConspirator(GameWorldComponent game, UUID playerId) {
        return game != null && playerId != null && game.isRole(playerId, ModRoles.CONSPIRATOR);
    }

    public static void announce(Level world, GameWorldComponent game) {
        List<ServerPlayer> conspirators = new ArrayList<>();
        for (Player player : world.players()) {
            if (player instanceof ServerPlayer serverPlayer && isConspirator(game, serverPlayer)) {
                conspirators.add(serverPlayer);
            }
        }
        if (conspirators.isEmpty()) return;

        List<ServerPlayer> killers = new ArrayList<>();
        for (UUID killerId : game.getAllKillerTeamPlayers()) {
            if (world.getPlayerByUUID(killerId) instanceof ServerPlayer killer) killers.add(killer);
        }

        int killerColor = WatheRoles.KILLER.color();
        for (ServerPlayer killer : killers) {
            killer.sendSystemMessage(
                Component.translatable("message.brinswathe.conspirator.present").withColor(killerColor)
            );
        }

        Component names = killerNames(killers);
        for (ServerPlayer conspirator : conspirators) {
            conspirator.sendSystemMessage(
                Component.translatable("message.brinswathe.conspirator.killers", names).withColor(killerColor)
            );
        }
    }

    public static void handleChannel(ServerPlayer player, GameWorldComponent gameWorld,
                                     AbilityPlayerComponent ability,
                                     AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, ModRoles.CONSPIRATOR)) return;
        ConspiratorComponent ownerChannel = ConspiratorComponent.KEY.get(player);
        if (ownerChannel != null && ownerChannel.isInTemporaryChannel()) return;
        if (ability.cooldown > 0 || payload.targetId() == null) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null || target.equals(player) || target.level() != player.level()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
        if (!gameWorld.canUseKillerFeatures(target)) return;
        if (!ModVoiceChatPlugin.canStartTemporaryChannel(player, target)) return;
        if (!AbilityCost.spend(player, "conspirator")) return;
        int durationSeconds = Math.min(
            ModConfig.skillDurationSeconds("conspirator"),
            Integer.MAX_VALUE / 20
        );
        if (!ModVoiceChatPlugin.startTemporaryChannel(player, target, durationSeconds * 20)) return;
        ability.setAbilityCooldown(ModConfig.skillCooldownSeconds("conspirator"));
        player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, 1.0F);
    }

    private static Component killerNames(List<ServerPlayer> killers) {
        if (killers.isEmpty()) return Component.translatable("message.brinswathe.conspirator.no_killers");
        MutableComponent joined = Component.empty();
        for (int index = 0; index < killers.size(); index++) {
            if (index > 0) joined.append(Component.translatable("message.brinswathe.conspirator.separator"));
            joined.append(killers.get(index).getDisplayName());
        }
        return joined;
    }
}
