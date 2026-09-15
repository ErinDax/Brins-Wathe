package cn.erindax.brinswathe.voice;

import cn.erindax.brinswathe.CowboyDuel;
import cn.erindax.brinswathe.component.EavesdropperComponent;
import cn.erindax.brinswathe.component.MediumComponent;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.compat.TrainVoicePlugin;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.UUID;

public final class BrinVoiceChatPlugin implements VoicechatPlugin {
    private static volatile VoicechatServerApi serverApi;
    private static final String TRAIN_GROUP_NAME = "Train Spectators";

    @Override
    public String getPluginId() {
        return "brinswathe";
    }

    @Override
    public void initialize(VoicechatApi api) {
        if (api instanceof VoicechatServerApi voicechatServerApi) {
            serverApi = voicechatServerApi;
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, event -> serverApi = event.getVoicechat());
        registration.registerEvent(VoicechatServerStoppedEvent.class, event -> {
            serverApi = null;
            TrainVoicePlugin.GROUP = null;
        });
    }

    public static boolean addPlayer(UUID playerId) {
        if (serverApi == null) return false;
        VoicechatConnection connection = serverApi.getConnectionOf(playerId);
        if (connection == null) return false;
        Group group = canonicalTrainGroup();
        if (group == null) {
            TrainVoicePlugin.addPlayer(playerId);
            group = canonicalTrainGroup();
        }
        if (group == null) return false;
        connection.setGroup(group);
        return isInCanonicalTrainGroup(connection);
    }

    public static void resetPlayer(UUID playerId) {
        if (serverApi == null) return;
        VoicechatConnection connection = serverApi.getConnectionOf(playerId);
        if (connection != null) connection.setGroup(null);
    }

    public static boolean canStartTemporaryChannel(ServerPlayer first, ServerPlayer second) {
        return serverApi != null
            && serverApi.getConnectionOf(first.getUUID()) != null
            && serverApi.getConnectionOf(second.getUUID()) != null;
    }

    public static boolean hasVoicechatConnection(UUID playerId) {
        if (serverApi == null) return false;
        VoicechatConnection connection = serverApi.getConnectionOf(playerId);
        return connection != null && connection.isConnected();
    }

    public static boolean isInTrainChannel(UUID playerId) {
        if (serverApi == null) return false;
        return isInCanonicalTrainGroup(serverApi.getConnectionOf(playerId));
    }

    public static boolean isInSameTemporaryChannel(ServerPlayer first, ServerPlayer second) {
        EavesdropperComponent firstComponent = EavesdropperComponent.KEY.get(first);
        EavesdropperComponent secondComponent = EavesdropperComponent.KEY.get(second);
        return firstComponent != null && secondComponent != null
            && firstComponent.channelId != null
            && firstComponent.channelId.equals(secondComponent.channelId);
    }

    public static boolean startTemporaryChannel(ServerPlayer first, ServerPlayer second, int durationTicks) {
        if (!canStartTemporaryChannel(first, second)) return false;
        EavesdropperComponent firstComponent = EavesdropperComponent.KEY.get(first);
        EavesdropperComponent secondComponent = EavesdropperComponent.KEY.get(second);
        if (firstComponent == null || secondComponent == null) return false;
        endTemporaryChannel(first);
        endTemporaryChannel(second);

        Group group = serverApi.groupBuilder()
            .setName("Brin Temporary Channel")
            .setHidden(true)
            .setPersistent(false)
            .setType(Group.Type.ISOLATED)
            .build();
        VoicechatConnection firstConnection = serverApi.getConnectionOf(first.getUUID());
        VoicechatConnection secondConnection = serverApi.getConnectionOf(second.getUUID());
        firstConnection.setGroup(group);
        secondConnection.setGroup(group);
        firstComponent.setTemporaryChannel(second.getUUID(), group.getId(), durationTicks, true);
        secondComponent.setTemporaryChannel(first.getUUID(), group.getId(), durationTicks, false);
        invalidateTrainState(first);
        invalidateTrainState(second);
        return true;
    }

    public static void tick(MinecraftServer server) {
        if (serverApi == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            EavesdropperComponent component = EavesdropperComponent.KEY.get(player);
            if (component == null || !component.isInTemporaryChannel()) continue;
            Group group = serverApi.getGroup(component.channelId);
            if (group == null) {
                endTemporaryChannel(player);
                continue;
            }
            ServerPlayer partner = server.getPlayerList().getPlayer(component.channelPartner);
            if (partner == null
                || !GameFunctions.isPlayerAliveAndSurvival(player)
                || !GameFunctions.isPlayerAliveAndSurvival(partner)) {
                endTemporaryChannel(player);
                continue;
            }
            VoicechatConnection connection = serverApi.getConnectionOf(player.getUUID());
            if (connection != null && (!connection.isInGroup() || !group.getId().equals(connection.getGroup().getId()))) {
                connection.setGroup(group);
            }
            if (component.channelOwner) {
                component.channelTicks--;
                if (component.channelTicks <= 0) {
                    endTemporaryChannel(player);
                }
            }
        }
        if (CowboyDuel.isActive()) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
            if (game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) continue;
            if (!player.isSpectator()) continue;
            EavesdropperComponent channel = EavesdropperComponent.KEY.get(player);
            if (channel != null && channel.isInTemporaryChannel()) continue;
            if (isInTrainChannel(player.getUUID())) continue;
            addPlayer(player.getUUID());
        }
        removeDuplicateTrainGroups(server);
    }

    public static void endTemporaryChannel(ServerPlayer player) {
        EavesdropperComponent component = EavesdropperComponent.KEY.get(player);
        if (component == null || component.channelId == null) return;
        UUID channelId = component.channelId;
        ServerPlayer partner = player.server.getPlayerList().getPlayer(component.channelPartner);
        boolean playerDead = !GameFunctions.isPlayerAliveAndSurvival(player);
        boolean partnerDead = partner != null && !GameFunctions.isPlayerAliveAndSurvival(partner);
        leaveGroup(player, channelId);
        if (partner != null) {
            leaveGroup(partner, channelId);
            EavesdropperComponent partnerComponent = EavesdropperComponent.KEY.get(partner);
            if (partnerComponent != null && channelId.equals(partnerComponent.channelId)) {
                partnerComponent.clearTemporaryChannel();
            }
        }
        component.clearTemporaryChannel();
        invalidateTrainState(player);
        if (partner != null) invalidateTrainState(partner);
        if (playerDead) MediumComponent.addToTrainChannel(player.getUUID());
        if (partnerDead) MediumComponent.addToTrainChannel(partner.getUUID());
        if (serverApi != null) serverApi.removeGroup(channelId);
    }

    private static void invalidateTrainState(ServerPlayer player) {
        MediumComponent component = MediumComponent.KEY.get(player);
        if (component != null && component.inTrainChannel) {
            component.inTrainChannel = false;
            component.sync();
        }
    }

    private static Group canonicalTrainGroup() {
        if (serverApi == null) return null;
        UUID cachedId = groupId(TrainVoicePlugin.GROUP);
        if (cachedId != null) {
            Group live = liveGroup(cachedId);
            if (live != null) return live;
            TrainVoicePlugin.GROUP = null;
        }
        Group byId = liveGroup(TrainVoicePlugin.GROUP_ID);
        if (byId != null) {
            TrainVoicePlugin.GROUP = byId;
            return byId;
        }
        return null;
    }

    private static boolean isInCanonicalTrainGroup(VoicechatConnection connection) {
        if (connection == null || !connection.isInGroup()) return false;
        UUID keepId = groupId(canonicalTrainGroup());
        UUID currentId = groupId(connection.getGroup());
        return keepId != null && keepId.equals(currentId);
    }

    private static void removeDuplicateTrainGroups(MinecraftServer server) {
        Group keep = canonicalTrainGroup();
        UUID keepId = groupId(keep);
        if (keepId == null) return;
        for (Group group : new ArrayList<>(serverApi.getGroups())) {
            UUID id = groupId(group);
            if (id == null || keepId.equals(id) || !TRAIN_GROUP_NAME.equals(groupName(group))) continue;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                VoicechatConnection connection = serverApi.getConnectionOf(player.getUUID());
                if (connection == null || !id.equals(groupId(connection.getGroup()))) continue;
                connection.setGroup(keep);
            }
            serverApi.removeGroup(id);
        }
    }

    private static Group liveGroup(UUID id) {
        if (serverApi == null || id == null) return null;
        Group group = serverApi.getGroup(id);
        return groupId(group) == null ? null : group;
    }

    private static UUID groupId(Group group) {
        if (group == null) return null;
        try {
            return group.getId();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String groupName(Group group) {
        if (group == null) return null;
        try {
            return group.getName();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static void leaveGroup(ServerPlayer player, UUID channelId) {
        if (serverApi == null) return;
        VoicechatConnection connection = serverApi.getConnectionOf(player.getUUID());
        UUID currentId = connection == null ? null : groupId(connection.getGroup());
        if (channelId.equals(currentId)) {
            connection.setGroup(null);
        }
    }

    private static ServerPlayer getPlayer(VoicechatConnection connection) {
        if (connection == null || connection.getPlayer() == null) {
            return null;
        }
        Object entity = connection.getPlayer().getEntity();
        return entity instanceof ServerPlayer player ? player : null;
    }
}
