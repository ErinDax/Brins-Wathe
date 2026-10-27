package cn.erindax.brinswathe;

import cn.erindax.brinswathe.network.BrinSecretCohortS2CPacket;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.events.ResetPlayerEvent;

public class BrinSecretMurderGameMode extends GameMode {
    public BrinSecretMurderGameMode(ResourceLocation identifier) {
        super(identifier, 10, 6);
    }

    @Override
    public void initializeGame(ServerLevel world, GameWorldComponent game, List<ServerPlayer> players) {
        game.clearRoleMap();
        WorldModifierComponent modifiers = WorldModifierComponent.KEY.get(world);
        modifiers.getModifiers().clear();
        modifiers.sync();
        for (ServerPlayer player : players) {
            ResetPlayerEvent.EVENT.invoker().resetPlayer(player);
            game.addRole(player, BrinSecretMurder.ROLE);
            PlayerShopComponent.KEY.get(player).setBalance(GameConstants.MONEY_START);
        }
        int killers = Math.max(1, Math.min(players.size(), BrinHarpyRoles.resolveKillerCount(players, game)));
        int targets = players.size() - killers;
        RandomSource random = world.getRandom();
        for (ServerPlayer player : players) {
            if (!BrinWelcome.send(player, WatheRoles.KILLER, BrinWelcome.KIND_KILLER, killers, targets)) {
                ServerPlayNetworking.send(player, new AnnounceWelcomePayload(
                    RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(RoleAnnouncementTexts.KILLER),
                    killers,
                    targets
                ));
            }
            if (ServerPlayNetworking.canSend(player, BrinSecretCohortS2CPacket.TYPE)) {
                ServerPlayNetworking.send(player, new BrinSecretCohortS2CPacket(fakeCohorts(player, players, killers - 1, random)));
            }
        }
    }

    @Override
    public void tickServerGameLoop(ServerLevel world, GameWorldComponent game) {
        GameFunctions.WinStatus winStatus = GameFunctions.WinStatus.NONE;
        if (!GameTimeComponent.KEY.get(world).hasTime()) winStatus = GameFunctions.WinStatus.TIME;
        int playersLeft = 0;
        ServerPlayer lastPlayer = null;
        for (ServerPlayer player : world.players()) {
            if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                playersLeft++;
                lastPlayer = player;
            }
        }
        if (playersLeft <= 0) {
            GameFunctions.stopGame(world);
            return;
        }
        if (playersLeft == 1) {
            game.setLooseEndWinner(lastPlayer.getUUID());
            winStatus = GameFunctions.WinStatus.LOOSE_END;
        }
        if (winStatus != GameFunctions.WinStatus.NONE && game.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE) {
            GameRoundEndComponent.KEY.get(world).setRoundEndData(world.players(), winStatus);
            GameFunctions.stopGame(world);
        }
    }

    private static List<UUID> fakeCohorts(ServerPlayer viewer, List<ServerPlayer> players, int count, RandomSource random) {
        List<UUID> others = new ArrayList<>(players.size());
        for (ServerPlayer player : players) {
            if (!player.getUUID().equals(viewer.getUUID())) others.add(player.getUUID());
        }
        List<UUID> picked = new ArrayList<>();
        while (picked.size() < count && !others.isEmpty()) {
            picked.add(others.remove(random.nextInt(others.size())));
        }
        return picked;
    }
}
