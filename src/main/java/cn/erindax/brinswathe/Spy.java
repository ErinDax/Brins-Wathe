package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.RoundRecapComponent;
import cn.erindax.brinswathe.component.SpyComponent;
import cn.erindax.brinswathe.config.Tunable;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.agmas.harpymodloader.Harpymodloader;

public final class Spy {
    private Spy() {
    }

    public static boolean isGuessable(Role role) {
        return role != WatheRoles.LOOSE_END
            && role != WatheRoles.DISCOVERY_CIVILIAN
            && !Harpymodloader.NON_MURDER_ROLES.contains(role);
    }

    public static boolean guess(ServerPlayer player, UUID targetId, String roleId) {
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return false;
        if (GameSafeComponent.KEY.get(player.level()).isGameSafe || CowboyDuel.isActive()) return false;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE
            || !gameWorld.isRole(player, ModRoles.SPY)) {
            return false;
        }
        SpyComponent spy = SpyComponent.KEY.get(player);
        AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
        if (ability.cooldown > 0 || spy.required() <= 0 || spy.completed()) return false;
        if (targetId.equals(player.getUUID()) || spy.hasGuessed(targetId)) return false;
        Role actual = gameWorld.getRole(targetId);
        ResourceLocation guessedId = ResourceLocation.tryParse(roleId);
        Role guessed = guessedId == null ? null : NoelleAccess.findRole(guessedId);
        if (actual == null || guessed == null || !isGuessable(guessed)) return false;
        player.resetLastActionTime();
        Component label = RoundRecapComponent.playerLabel(
            targetName(player, targetId),
            guessed.identifier().toString()
        );
        if (!actual.identifier().equals(guessed.identifier())) {
            spy.recordWrong();
            int maxWrong = Tunable.SPY_MAX_WRONG_GUESSES.intValue();
            if (spy.wrong() >= maxWrong) {
                player.displayClientMessage(
                    Component.translatable("message.brinswathe.spy.dead", label, spy.wrong()),
                    false
                );
                Subversion.punish(player);
                return false;
            }
            ability.setAbilityCooldown(Tunable.SPY_WRONG_COOLDOWN_SECONDS.intValue());
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.displayClientMessage(
                Component.translatable("message.brinswathe.spy.wrong", label, maxWrong - spy.wrong()),
                false
            );
            return false;
        }
        spy.recordCorrect(targetId);
        ability.setAbilityCooldown(Tunable.SPY_CORRECT_COOLDOWN_SECONDS.intValue());
        for (ServerPlayer listener : player.serverLevel().players()) {
            listener.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.displayClientMessage(
            Component.translatable("message.brinswathe.spy.correct", label, spy.correct(), spy.required()),
            false
        );
        return spy.completed();
    }

    private static String targetName(ServerPlayer player, UUID targetId) {
        ServerPlayer target = player.server.getPlayerList().getPlayer(targetId);
        if (target != null) return target.getGameProfile().getName();
        RoundRecapComponent.Identity identity =
            RoundRecapComponent.KEY.get(player.level()).identityOf(targetId);
        return identity == null ? targetId.toString().substring(0, 8) : identity.name();
    }
}
