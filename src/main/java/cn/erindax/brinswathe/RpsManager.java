package cn.erindax.brinswathe;

import cn.erindax.brinswathe.network.RpsActionC2SPacket;
import cn.erindax.brinswathe.network.RpsStateS2CPacket;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class RpsManager {
    public static final int ROCK = 0;
    public static final int SCISSORS = 1;
    public static final int PAPER = 2;
    public static final double RANGE = 12.0D;
    private static final int INVITE_TICKS = 15 * 20;
    private static final int PICK_TICKS = 20 * 20;
    private static final int RESULT_TICKS = 5 * 20;
    private static final int INVITE_COOLDOWN_TICKS = 40;
    private static final Map<UUID, Invite> invitesByTarget = new HashMap<>();
    private static final Map<UUID, Invite> invitesByChallenger = new HashMap<>();
    private static final Map<UUID, Match> matches = new HashMap<>();
    private static final Map<UUID, Integer> inviteCooldowns = new HashMap<>();
    private RpsManager() {
    }

    public static void clear() {
        invitesByTarget.clear();
        invitesByChallenger.clear();
        matches.clear();
        inviteCooldowns.clear();
    }
    public static void handle(ServerPlayer player, RpsActionC2SPacket packet) {
        if (CowboyDuel.isActive()) return;
        switch (packet.action()) {
            case RpsActionC2SPacket.INVITE -> invite(player, packet.targetId());
            case RpsActionC2SPacket.ACCEPT -> accept(player);
            case RpsActionC2SPacket.DECLINE -> decline(player);
            case RpsActionC2SPacket.CHOOSE -> choose(player, packet.choice());
            default -> {
            }
        }
    }
    public static void tick(MinecraftServer server) {
        if (CowboyDuel.isActive()) {
            abortAll(server);
            return;
        }
        tickCooldowns();
        tickInvites(server);
        tickMatches(server);
    }
    public static void holdSelecting(MinecraftServer server) {
        if (CowboyDuel.isActive()) {
            abortAll(server);
            return;
        }
        if (matches.isEmpty()) return;
        HashMap<Match, Boolean> seen = new HashMap<>();
        for (Match match : matches.values()) seen.putIfAbsent(match, Boolean.TRUE);
        for (Match match : seen.keySet()) {
            if (match.resultTicks >= 0) continue;
            hold(server.getPlayerList().getPlayer(match.playerA), match.anchorA);
            hold(server.getPlayerList().getPlayer(match.playerB), match.anchorB);
        }
    }
    public static void abortAll(MinecraftServer server) {
        if (invitesByTarget.isEmpty() && matches.isEmpty()) return;
        HashSet<UUID> ids = new HashSet<>();
        ids.addAll(invitesByTarget.keySet());
        ids.addAll(invitesByChallenger.keySet());
        ids.addAll(matches.keySet());
        clear();
        for (UUID id : ids) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) send(player, RpsStateS2CPacket.clear());
        }
    }
    public static void onDisconnect(ServerPlayer player) {
        cancelFor(player, "message.brinswathe.rps.left");
        inviteCooldowns.remove(player.getUUID());
    }
    private static void invite(ServerPlayer challenger, @Nullable UUID targetId) {
        if (targetId == null || targetId.equals(challenger.getUUID())) return;
        if (!isPlayable(challenger)) return;
        if (isBusy(challenger.getUUID())) {
            challenger.displayClientMessage(Component.translatable("message.brinswathe.rps.busy_self"), true);
            return;
        }
        int cooldown = inviteCooldowns.getOrDefault(challenger.getUUID(), 0);
        if (cooldown > 0) return;
        ServerPlayer target = challenger.server.getPlayerList().getPlayer(targetId);
        if (target == null || target.level() != challenger.level() || !isPlayable(target)) {
            challenger.displayClientMessage(Component.translatable("message.brinswathe.rps.no_target"), true);
            return;
        }
        if (challenger.distanceToSqr(target) > RANGE * RANGE) {
            challenger.displayClientMessage(Component.translatable("message.brinswathe.rps.too_far"), true);
            return;
        }
        if (isBusy(target.getUUID())) {
            challenger.displayClientMessage(Component.translatable("message.brinswathe.rps.busy_other"), true);
            return;
        }
        Invite invite = new Invite(challenger.getUUID(), target.getUUID(), INVITE_TICKS);
        invitesByTarget.put(target.getUUID(), invite);
        invitesByChallenger.put(challenger.getUUID(), invite);
        inviteCooldowns.put(challenger.getUUID(), INVITE_COOLDOWN_TICKS);
        send(challenger, new RpsStateS2CPacket(
            RpsStateS2CPacket.PENDING, name(target), seconds(INVITE_TICKS), -1, -1, 0));
        send(target, new RpsStateS2CPacket(
            RpsStateS2CPacket.INVITE, name(challenger), seconds(INVITE_TICKS), -1, -1, 0));
        target.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.8F, 1.3F);
        challenger.displayClientMessage(
            Component.translatable("message.brinswathe.rps.sent", name(target)), true);
    }
    private static void accept(ServerPlayer target) {
        Invite invite = invitesByTarget.get(target.getUUID());
        if (invite == null) return;
        ServerPlayer challenger = target.server.getPlayerList().getPlayer(invite.challengerId);
        dropInvite(invite);
        if (challenger == null || !isPlayable(challenger) || !isPlayable(target)) {
            send(target, RpsStateS2CPacket.clear());
            return;
        }
        Match match = new Match(
            challenger.getUUID(),
            target.getUUID(),
            copyPos(challenger),
            copyPos(target),
            PICK_TICKS);
        matches.put(challenger.getUUID(), match);
        matches.put(target.getUUID(), match);
        hold(challenger, match.anchorA);
        hold(target, match.anchorB);
        syncMatch(challenger.server, match);
        challenger.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.2F);
        target.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.2F);
    }
    private static void decline(ServerPlayer target) {
        Invite invite = invitesByTarget.get(target.getUUID());
        if (invite == null) return;
        ServerPlayer challenger = target.server.getPlayerList().getPlayer(invite.challengerId);
        dropInvite(invite);
        send(target, RpsStateS2CPacket.clear());
        if (challenger != null) {
            send(challenger, RpsStateS2CPacket.clear());
            challenger.displayClientMessage(
                Component.translatable("message.brinswathe.rps.declined", name(target)), true);
        }
    }
    private static void choose(ServerPlayer player, int choice) {
        if (choice < ROCK || choice > PAPER) return;
        Match match = matches.get(player.getUUID());
        if (match == null || match.resultTicks >= 0) return;
        if (player.getUUID().equals(match.playerA)) {
            if (match.choiceA >= 0) return;
            match.choiceA = choice;
        } else if (player.getUUID().equals(match.playerB)) {
            if (match.choiceB >= 0) return;
            match.choiceB = choice;
        } else {
            return;
        }
        if (match.choiceA >= 0 && match.choiceB >= 0) {
            match.resultTicks = RESULT_TICKS;
            announceResult(player.server, match);
        }
        syncMatch(player.server, match);
    }
    private static void tickCooldowns() {
        Iterator<Map.Entry<UUID, Integer>> iterator = inviteCooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int next = entry.getValue() - 1;
            if (next <= 0) iterator.remove();
            else entry.setValue(next);
        }
    }
    private static void tickInvites(MinecraftServer server) {
        Iterator<Invite> iterator = invitesByTarget.values().iterator();
        while (iterator.hasNext()) {
            Invite invite = iterator.next();
            invite.ticksLeft--;
            ServerPlayer challenger = server.getPlayerList().getPlayer(invite.challengerId);
            ServerPlayer target = server.getPlayerList().getPlayer(invite.targetId);
            if (invite.ticksLeft <= 0 || !isPlayable(challenger) || !isPlayable(target)) {
                iterator.remove();
                invitesByChallenger.remove(invite.challengerId);
                if (challenger != null) {
                    send(challenger, RpsStateS2CPacket.clear());
                    challenger.displayClientMessage(Component.translatable("message.brinswathe.rps.expired"), true);
                }
                if (target != null) send(target, RpsStateS2CPacket.clear());
                continue;
            }
            if (invite.ticksLeft % 20 == 0) {
                send(challenger, new RpsStateS2CPacket(
                    RpsStateS2CPacket.PENDING, name(target), seconds(invite.ticksLeft), -1, -1, 0));
                send(target, new RpsStateS2CPacket(
                    RpsStateS2CPacket.INVITE, name(challenger), seconds(invite.ticksLeft), -1, -1, 0));
            }
        }
    }
    private static void tickMatches(MinecraftServer server) {
        HashMap<Match, Boolean> seen = new HashMap<>();
        for (Match match : matches.values()) seen.putIfAbsent(match, Boolean.TRUE);
        for (Match match : seen.keySet()) {
            if (match.resultTicks >= 0) {
                match.resultTicks--;
                if (match.resultTicks <= 0) {
                    finish(server, match);
                    continue;
                }
                if (match.resultTicks % 20 == 0) syncMatch(server, match);
                continue;
            }
            match.pickTicksLeft--;
            ServerPlayer a = server.getPlayerList().getPlayer(match.playerA);
            ServerPlayer b = server.getPlayerList().getPlayer(match.playerB);
            hold(a, match.anchorA);
            hold(b, match.anchorB);
            if (match.pickTicksLeft <= 0 || !isPlayable(a) || !isPlayable(b)) {
                finish(server, match);
                if (a != null) a.displayClientMessage(Component.translatable("message.brinswathe.rps.expired"), true);
                if (b != null) b.displayClientMessage(Component.translatable("message.brinswathe.rps.expired"), true);
                continue;
            }
            if (match.pickTicksLeft % 20 == 0) syncMatch(server, match);
        }
    }
    private static void announceResult(MinecraftServer server, Match match) {
        ServerPlayer a = server.getPlayerList().getPlayer(match.playerA);
        ServerPlayer b = server.getPlayerList().getPlayer(match.playerB);
        int outcomeA = outcome(match.choiceA, match.choiceB);
        if (a != null) {
            a.displayClientMessage(resultChat(name(b), match.choiceA, match.choiceB, outcomeA), false);
        }
        if (b != null) {
            b.displayClientMessage(resultChat(name(a), match.choiceB, match.choiceA, outcome(match.choiceB, match.choiceA)), false);
        }
    }
    private static Component resultChat(String opponent, int yours, int theirs, int outcome) {
        return Component.translatable(
            "message.brinswathe.rps.result",
            opponent,
            Component.translatable(choiceKey(yours)),
            Component.translatable(choiceKey(theirs)),
            Component.translatable(outcomeKey(outcome))
        );
    }
    private static void finish(MinecraftServer server, Match match) {
        ServerPlayer a = server.getPlayerList().getPlayer(match.playerA);
        ServerPlayer b = server.getPlayerList().getPlayer(match.playerB);
        matches.remove(match.playerA);
        matches.remove(match.playerB);
        if (a != null) send(a, RpsStateS2CPacket.clear());
        if (b != null) send(b, RpsStateS2CPacket.clear());
    }
    private static void cancelFor(ServerPlayer player, String messageKey) {
        Invite asTarget = invitesByTarget.remove(player.getUUID());
        if (asTarget != null) {
            invitesByChallenger.remove(asTarget.challengerId);
            ServerPlayer other = player.server.getPlayerList().getPlayer(asTarget.challengerId);
            send(player, RpsStateS2CPacket.clear());
            if (other != null) {
                send(other, RpsStateS2CPacket.clear());
                other.displayClientMessage(Component.translatable(messageKey, name(player)), true);
            }
        }
        Invite asChallenger = invitesByChallenger.remove(player.getUUID());
        if (asChallenger != null) {
            invitesByTarget.remove(asChallenger.targetId);
            ServerPlayer other = player.server.getPlayerList().getPlayer(asChallenger.targetId);
            send(player, RpsStateS2CPacket.clear());
            if (other != null) send(other, RpsStateS2CPacket.clear());
        }
        Match match = matches.remove(player.getUUID());
        if (match != null) {
            UUID otherId = match.playerA.equals(player.getUUID()) ? match.playerB : match.playerA;
            matches.remove(otherId);
            send(player, RpsStateS2CPacket.clear());
            ServerPlayer other = player.server.getPlayerList().getPlayer(otherId);
            if (other != null) {
                send(other, RpsStateS2CPacket.clear());
                other.displayClientMessage(Component.translatable(messageKey, name(player)), true);
            }
        }
    }
    private static void dropInvite(Invite invite) {
        invitesByTarget.remove(invite.targetId);
        invitesByChallenger.remove(invite.challengerId);
    }
    private static void syncMatch(MinecraftServer server, Match match) {
        ServerPlayer a = server.getPlayerList().getPlayer(match.playerA);
        ServerPlayer b = server.getPlayerList().getPlayer(match.playerB);
        if (a != null) send(a, stateFor(match, true, name(b)));
        if (b != null) send(b, stateFor(match, false, name(a)));
    }
    private static RpsStateS2CPacket stateFor(Match match, boolean asA, String opponent) {
        int yours = asA ? match.choiceA : match.choiceB;
        int theirs = asA ? match.choiceB : match.choiceA;
        if (match.resultTicks >= 0) {
            return new RpsStateS2CPacket(
                RpsStateS2CPacket.RESULT,
                opponent,
                seconds(match.resultTicks),
                yours,
                theirs,
                outcome(yours, theirs)
            );
        }
        if (yours >= 0) {
            return new RpsStateS2CPacket(
                RpsStateS2CPacket.WAIT, opponent, seconds(match.pickTicksLeft), yours, -1, 0);
        }
        return new RpsStateS2CPacket(
            RpsStateS2CPacket.PICK, opponent, seconds(match.pickTicksLeft), -1, -1, 0);
    }
    private static int outcome(int yours, int theirs) {
        if (yours < 0 || theirs < 0) return RpsStateS2CPacket.OUTCOME_NONE;
        if (yours == theirs) return RpsStateS2CPacket.OUTCOME_DRAW;
        boolean win = (yours == ROCK && theirs == SCISSORS)
            || (yours == SCISSORS && theirs == PAPER)
            || (yours == PAPER && theirs == ROCK);
        return win ? RpsStateS2CPacket.OUTCOME_WIN : RpsStateS2CPacket.OUTCOME_LOSE;
    }
    private static String choiceKey(int choice) {
        return switch (choice) {
            case ROCK -> "rps.brinswathe.rock";
            case SCISSORS -> "rps.brinswathe.scissors";
            case PAPER -> "rps.brinswathe.paper";
            default -> "rps.brinswathe.none";
        };
    }
    private static String outcomeKey(int outcome) {
        return switch (outcome) {
            case RpsStateS2CPacket.OUTCOME_WIN -> "rps.brinswathe.win";
            case RpsStateS2CPacket.OUTCOME_LOSE -> "rps.brinswathe.lose";
            default -> "rps.brinswathe.draw";
        };
    }
    private static boolean isBusy(UUID id) {
        return invitesByTarget.containsKey(id)
            || invitesByChallenger.containsKey(id)
            || matches.containsKey(id);
    }

    private static boolean isPlayable(@Nullable ServerPlayer player) {
        return player != null && GameFunctions.isPlayerAliveAndSurvival(player);
    }
    private static String name(@Nullable ServerPlayer player) {
        return player == null ? "?" : player.getGameProfile().getName();
    }
    private static void hold(@Nullable ServerPlayer player, Vec3 anchor) {
        if (player == null || anchor == null) return;
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        if (player.isPassenger()) return;
        if (player.position().distanceToSqr(anchor) > 1.0E-4D) {
            player.connection.teleport(anchor.x, anchor.y, anchor.z, player.getYRot(), player.getXRot());
        }
        player.hurtMarked = true;
    }
    private static Vec3 copyPos(ServerPlayer player) {
        return new Vec3(player.getX(), player.getY(), player.getZ());
    }
    private static int seconds(int ticks) {
        return Math.max(0, Mth.ceil(ticks / 20.0F));
    }
    private static void send(ServerPlayer player, RpsStateS2CPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }
    private static final class Invite {
        private final UUID challengerId;
        private final UUID targetId;
        private int ticksLeft;
        private Invite(UUID challengerId, UUID targetId, int ticksLeft) {
            this.challengerId = challengerId;
            this.targetId = targetId;
            this.ticksLeft = ticksLeft;
        }
    }
    private static final class Match {
        private final UUID playerA;
        private final UUID playerB;
        private final Vec3 anchorA;
        private final Vec3 anchorB;
        private int pickTicksLeft;
        private int choiceA = -1;
        private int choiceB = -1;
        private int resultTicks = -1;
        private Match(UUID playerA, UUID playerB, Vec3 anchorA, Vec3 anchorB, int pickTicksLeft) {
            this.playerA = playerA;
            this.playerB = playerB;
            this.anchorA = anchorA;
            this.anchorB = anchorB;
            this.pickTicksLeft = pickTicksLeft;
        }
    }
}
