package cn.autoforged.brinswathe;

import cn.autoforged.brinswathe.component.BoneharvesterComponent;
import cn.autoforged.brinswathe.component.IllusionistComponent;
import cn.autoforged.brinswathe.component.MorticianComponent;
import cn.autoforged.brinswathe.component.NightmareComponent;
import cn.autoforged.brinswathe.component.PenitentComponent;
import cn.autoforged.brinswathe.component.PuppeteerControlComponent;
import cn.autoforged.brinswathe.component.SniperComponent;
import cn.autoforged.brinswathe.component.StuntDoubleComponent;
import cn.autoforged.brinswathe.component.ZhangshiComponent;
import cn.autoforged.brinswathe.config.BrinConfig;
import cn.autoforged.brinswathe.network.BlindFlashS2CPacket;
import cn.autoforged.brinswathe.network.CowboyShowdownMusicS2CPacket;
import cn.autoforged.brinswathe.network.CowboyDuelHideS2CPacket;
import cn.autoforged.brinswathe.network.CowboyDuelIdentityS2CPacket;
import cn.autoforged.brinswathe.network.CowboyDuelLookLockS2CPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.ladysnake.cca.api.v3.component.ComponentKey;

/**
 * The cowboy's showdown, run as a single global state machine: only one duel may exist at a time, so
 * every frozen system can ask one static question. Nothing here is persisted - a server restart simply
 * forgets the duel - but a round that stops being ACTIVE aborts it and hands the players to wathe's own
 * reset flow.
 */
public final class CowboyDuel {
    private enum Phase {
        IDLE,
        ANNOUNCE,
        REVEAL,
        COUNTDOWN,
        FIGHT
    }

    private record PlayerSnapshot(Vec3 position, float yaw, float pitch, GameType gameMode) {
    }

    private record DuelAnchor(double x, double y, double z, float yaw) {
    }

    private record ShieldSnapshot(int penitent, int nightmare, int boneharvester, int zhangshi, int bartender) {
        private boolean isEmpty() {
            return this.penitent == 0
                && this.nightmare == 0
                && this.boneharvester == 0
                && this.zhangshi == 0
                && this.bartender == 0;
        }
    }

    /** Must match AbilityHudMixin so the warp happens only after the overlay is fully black. */
    private static final int BLIND_FADE_IN_TICKS = 10;
    /** Must match AbilityHudMixin: everyone finishes fading out before the 3-2-1 walk. */
    private static final int BLIND_FADE_OUT_TICKS = 15;
    /** Extra ticks of solid black after fade-in, covering a tick or two of packet latency. */
    private static final int TELEPORT_DELAY_TICKS = BLIND_FADE_IN_TICKS + 5;
    /** Black title hold after the warp; spectators begin fading out when this elapses. */
    private static final int ANNOUNCE_HOLD_TICKS = 5 * 20;
    /** Marks the loaned revolver so the end-of-duel sweep confiscates it and nothing else. */
    private static final String DUEL_GUN_TAG = "brin_cowboy_duel_gun";
    private static final double ARENA_HORIZONTAL_MARGIN = 64.0D;
    private static final double ARENA_VERTICAL_MARGIN = 32.0D;

    private static Phase phase = Phase.IDLE;
    private static ServerLevel level;
    private static UUID cowboyId;
    private static UUID targetId;
    private static int announceTicksLeft;
    private static int revealTicksLeft;
    private static int countdownTicksLeft;
    private static int fightTicks;
    private static boolean arenaPlaced;
    private static boolean duelistsHiddenFromCrowd;
    private static Vec3 spectatorPos;
    private static float spectatorYaw;
    private static final Map<UUID, PlayerSnapshot> SNAPSHOTS = new LinkedHashMap<>();
    private static final Map<UUID, ShieldSnapshot> SHIELD_SNAPSHOTS = new LinkedHashMap<>();
    private static final Map<UUID, Integer> LOCKED_SLOTS = new LinkedHashMap<>();
    private static final Map<UUID, DuelAnchor> DUEL_ANCHORS = new LinkedHashMap<>();
    private static final Set<UUID> MODE_CHANGED = new HashSet<>();
    private static AABB originalPlayArea;
    private static AABB arenaBounds;

    private static ComponentKey<?> bartenderShieldKey;
    private static Field bartenderArmorField;
    private static boolean bartenderLookupAttempted;

    private CowboyDuel() {
    }

    public static boolean isActive() {
        return phase != Phase.IDLE;
    }

    public static void clear() {
        phase = Phase.IDLE;
        level = null;
        cowboyId = null;
        targetId = null;
        announceTicksLeft = 0;
        revealTicksLeft = 0;
        countdownTicksLeft = 0;
        fightTicks = 0;
        arenaPlaced = false;
        duelistsHiddenFromCrowd = false;
        spectatorPos = null;
        spectatorYaw = 0.0F;
        SNAPSHOTS.clear();
        SHIELD_SNAPSHOTS.clear();
        LOCKED_SLOTS.clear();
        DUEL_ANCHORS.clear();
        MODE_CHANGED.clear();
        originalPlayArea = null;
        arenaBounds = null;
    }

    /** Caller has already validated the role, the target, the arena config and the fee. */
    public static void start(ServerPlayer cowboy, ServerPlayer target,
                             BlockPos posA, BlockPos posB, BlockPos posSpectator) {
        ServerLevel duelLevel = cowboy.serverLevel();

        // Possessions and disguises teleport their owner back on ending, so they must be wound down
        // before the snapshots or the restore would strand players at their borrowed positions.
        finishBlockingStates(duelLevel);

        SNAPSHOTS.clear();
        SHIELD_SNAPSHOTS.clear();
        LOCKED_SLOTS.clear();
        DUEL_ANCHORS.clear();
        MODE_CHANGED.clear();
        for (ServerPlayer player : duelLevel.players()) {
            SNAPSHOTS.put(player.getUUID(), new PlayerSnapshot(
                player.position(),
                player.getYRot(),
                player.getXRot(),
                player.gameMode.getGameModeForPlayer()
            ));
        }

        captureAndClearShields(cowboy);
        captureAndClearShields(target);
        // Locked before the teleport so an overflow item dropped by the stow lands at the player's
        // original spot rather than inside the arena.
        lockHotbar(cowboy);
        lockHotbar(target);

        AABB pointBounds = new AABB(Vec3.atCenterOf(posA), Vec3.atCenterOf(posB))
            .minmax(new AABB(Vec3.atCenterOf(posSpectator), Vec3.atCenterOf(posSpectator)));
        arenaBounds = pointBounds.inflate(
            ARENA_HORIZONTAL_MARGIN,
            ARENA_VERTICAL_MARGIN,
            ARENA_HORIZONTAL_MARGIN
        );

        // The arena sits outside the map's play area; without the union wathe would reset the duelists
        // out of it and clamp the spectators back into the train every tick.
        MapVariablesWorldComponent mapVariables = MapVariablesWorldComponent.KEY.get(duelLevel);
        originalPlayArea = mapVariables.getPlayArea();
        if (originalPlayArea != null) {
            mapVariables.setPlayArea(originalPlayArea.minmax(arenaBounds));
            mapVariables.sync();
        }

        double ax = posA.getX() + 0.5D;
        double ay = posA.getY();
        double az = posA.getZ() + 0.5D;
        double bx = posB.getX() + 0.5D;
        double by = posB.getY();
        double bz = posB.getZ() + 0.5D;
        // Anchors are ready before the fade finishes; the actual warp waits until every screen is black.
        float cowboyYaw = horizontalYaw(ax - bx, az - bz);
        float targetYaw = horizontalYaw(bx - ax, bz - az);
        DUEL_ANCHORS.put(cowboy.getUUID(), new DuelAnchor(ax, ay, az, cowboyYaw));
        DUEL_ANCHORS.put(target.getUUID(), new DuelAnchor(bx, by, bz, targetYaw));

        double spectatorX = posSpectator.getX() + 0.5D;
        double spectatorY = posSpectator.getY();
        double spectatorZ = posSpectator.getZ() + 0.5D;
        spectatorPos = new Vec3(spectatorX, spectatorY, spectatorZ);
        spectatorYaw = horizontalYaw(
            (ax + bx) / 2.0D - spectatorX,
            (az + bz) / 2.0D - spectatorZ
        );

        int countdownTicks = BrinConfig.cowboyDuelCountdownSeconds() * 20;
        // Duelists and the crowd share the overlay: fade in, hold the yellow title, fade out. The
        // 3-2-1 walk is visible; mouse look stays locked on the two duelists by a separate packet.
        int overlayTicks = TELEPORT_DELAY_TICKS + ANNOUNCE_HOLD_TICKS + BLIND_FADE_OUT_TICKS;
        // Arena lighting is the builder's business, not the players': everyone pulled in can watch the
        // duel clearly. The duration is only a fallback; leaving the duel removes the effect explicitly.
        int nightVisionTicks = overlayTicks
            + countdownTicks
            + BrinConfig.cowboyDuelTimeoutSeconds() * 20
            + 100;
        for (ServerPlayer player : duelLevel.players()) {
            player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION, nightVisionTicks, 0, false, false, false));
            ServerPlayNetworking.send(player, new BlindFlashS2CPacket(overlayTicks));
        }

        level = duelLevel;
        cowboyId = cowboy.getUUID();
        targetId = target.getUUID();
        announceTicksLeft = TELEPORT_DELAY_TICKS + ANNOUNCE_HOLD_TICKS;
        revealTicksLeft = 0;
        countdownTicksLeft = countdownTicks;
        fightTicks = 0;
        arenaPlaced = false;
        duelistsHiddenFromCrowd = false;
        phase = Phase.ANNOUNCE;

        // The line is owner-written text rather than a translation key, so it can be reworded freely in
        // the config; leaving it empty silences the broadcast.
        String announcement = BrinConfig.cowboyDuelAnnounceMessage()
            .replace("{cowboy}", cowboy.getDisplayName().getString())
            .replace("{target}", target.getDisplayName().getString());
        if (!announcement.isBlank()) {
            broadcast(duelLevel.getServer(), Component.literal(announcement).withStyle(ChatFormatting.GOLD));
        }
        hideSpectatorIdentities(duelLevel.getServer(), true);
        lockDuelistLook(duelLevel.getServer(), true);
    }

    public static void tick(MinecraftServer server) {
        if (phase == Phase.IDLE) return;
        if (level == null) {
            hideSpectatorIdentities(server, false);
            hideDuelistsFromCrowd(server, false);
            lockDuelistLook(server, false);
            stopShowdownMusic(server);
            clear();
            return;
        }

        // A force-stopped or restarted round tears the duel down; wathe's own reset flow owns the
        // players from there, so only the play area is put back.
        if (GameWorldComponent.KEY.get(level).getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) {
            // The duel never resolved, so the confiscated layers were taken for nothing.
            returnShields(server);
            removeNightVision(server);
            stopShowdownMusic(server);
            hideDuelistsFromCrowd(server, false);
            hideSpectatorIdentities(server, false);
            lockDuelistLook(server, false);
            returnDuelistCorpses(server);
            returnArenaDrops();
            restorePlayArea();
            clear();
            return;
        }

        enforceHotbarLocks(server);

        if (phase == Phase.ANNOUNCE) {
            if (!arenaPlaced && announceTicksLeft <= ANNOUNCE_HOLD_TICKS) {
                placeArena(server);
                sendTitle(
                    Component.translatable("title.brinswathe.cowboy.showdown")
                        .withStyle(ChatFormatting.YELLOW),
                    10,
                    ANNOUNCE_HOLD_TICKS - 10,
                    BLIND_FADE_OUT_TICKS
                );
                playShowdownMusic();
            }
            pinDuelists(server);
            pinSpectators(server);
            announceTicksLeft--;
            if (announceTicksLeft <= 0) {
                // Crowd overlay is lifting; show the two standing back-to-back for the fade-in and walk.
                hideDuelistsFromCrowd(server, false);
                phase = Phase.REVEAL;
                revealTicksLeft = BLIND_FADE_OUT_TICKS;
            }
            return;
        }

        if (phase == Phase.REVEAL) {
            pinDuelists(server);
            pinSpectators(server);
            revealTicksLeft--;
            if (revealTicksLeft <= 0) {
                phase = Phase.COUNTDOWN;
            }
            return;
        }

        if (phase == Phase.COUNTDOWN) {
            if (countdownTicksLeft > 0 && countdownTicksLeft % 20 == 0) {
                sendTitle(Component.literal(Integer.toString(countdownTicksLeft / 20)), 0, 25, 10);
                stepDuelistsForward();
            }
            pinDuelists(server);
            countdownTicksLeft--;
            if (countdownTicksLeft <= 0) {
                lockDuelistLook(server, false);
                hideDuelistsFromCrowd(server, false);
                sendTitle(Component.translatable("title.brinswathe.cowboy.draw"), 0, 25, 10);
                ServerPlayer cowboy = server.getPlayerList().getPlayer(cowboyId);
                ServerPlayer target = server.getPlayerList().getPlayer(targetId);
                if (cowboy != null && GameFunctions.isPlayerAliveAndSurvival(cowboy)) giveRevolver(cowboy);
                if (target != null && GameFunctions.isPlayerAliveAndSurvival(target)) giveRevolver(target);
                phase = Phase.FIGHT;
                fightTicks = 0;
            }
            return;
        }

        fightTicks++;
        List<ServerPlayer> aliveDuelists = new ArrayList<>(2);
        for (UUID duelistId : new UUID[]{cowboyId, targetId}) {
            ServerPlayer duelist = server.getPlayerList().getPlayer(duelistId);
            if (duelist != null && GameFunctions.isPlayerAliveAndSurvival(duelist)) {
                aliveDuelists.add(duelist);
            }
        }

        if (fightTicks > BrinConfig.cowboyDuelTimeoutSeconds() * 20) {
            for (ServerPlayer duelist : aliveDuelists) {
                GameFunctions.killPlayer(duelist, true, null, GameConstants.DeathReasons.GENERIC);
            }
            finish(server, Component.translatable("message.brinswathe.cowboy.duel_timeout")
                .withStyle(ChatFormatting.RED));
            return;
        }

        if (aliveDuelists.size() <= 1) {
            Component result = aliveDuelists.isEmpty()
                ? Component.translatable("message.brinswathe.cowboy.duel_both_dead")
                    .withStyle(ChatFormatting.RED)
                : Component.translatable(
                    "message.brinswathe.cowboy.duel_winner",
                    aliveDuelists.getFirst().getDisplayName()
                ).withStyle(ChatFormatting.GOLD);
            finish(server, result);
        }
    }

    private static void finish(MinecraftServer server, Component result) {
        // The revolver was a loan for the duel; a surviving duelist does not get to keep it.
        for (UUID duelistId : new UUID[]{cowboyId, targetId}) {
            ServerPlayer duelist = server.getPlayerList().getPlayer(duelistId);
            if (duelist != null && GameFunctions.isPlayerAliveAndSurvival(duelist)) {
                stripDuelGuns(duelist);
            }
        }

        returnShields(server);
        returnDuelistCorpses(server);
        returnArenaDrops();
        removeNightVision(server);

        for (Map.Entry<UUID, PlayerSnapshot> entry : SNAPSHOTS.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) continue;
            PlayerSnapshot snapshot = entry.getValue();
            if (MODE_CHANGED.contains(entry.getKey())) {
                player.setGameMode(snapshot.gameMode());
            }
            if (player.serverLevel() == level) {
                teleport(
                    player,
                    snapshot.position().x,
                    snapshot.position().y,
                    snapshot.position().z,
                    snapshot.yaw(),
                    snapshot.pitch()
                );
            }
        }

        restorePlayArea();
        hideDuelistsFromCrowd(server, false);
        hideSpectatorIdentities(server, false);
        lockDuelistLook(server, false);
        stopShowdownMusic(server);
        broadcast(server, result);
        clear();
    }

    /**
     * A duelist who died in the arena left his corpse there, far outside the map; it is carried back to
     * where he stood before the duel so body-driven roles still get their evidence.
     */
    private static void returnDuelistCorpses(MinecraftServer server) {
        if (arenaBounds == null) return;
        for (UUID duelistId : new UUID[]{cowboyId, targetId}) {
            PlayerSnapshot snapshot = SNAPSHOTS.get(duelistId);
            if (snapshot == null) continue;
            ServerPlayer duelist = server.getPlayerList().getPlayer(duelistId);
            if (duelist != null && GameFunctions.isPlayerAliveAndSurvival(duelist)) continue;

            for (PlayerBodyEntity body : level.getEntitiesOfClass(
                PlayerBodyEntity.class,
                arenaBounds,
                body -> duelistId.equals(body.getPlayerUuid())
                    && !MorticianComponent.isDisguiseBody(body)
                    && !PuppeteerControlComponent.isPuppetModel(body)
                    && !IllusionistComponent.isIllusionModel(body))) {
                body.teleportTo(snapshot.position().x, snapshot.position().y, snapshot.position().z);
            }
        }
    }

    /**
     * Keys, guns and anything else that hit the floor in the arena stay there unless carried back.
     * Everything except the loaned duel revolver is dropped at the cowboy's pre-duel spot so the
     * train still has the evidence; the loaned guns are deleted rather than put into circulation.
     */
    private static void returnArenaDrops() {
        if (arenaBounds == null || level == null || cowboyId == null) return;
        PlayerSnapshot cowboySnapshot = SNAPSHOTS.get(cowboyId);
        if (cowboySnapshot == null) return;
        Vec3 dest = cowboySnapshot.position();
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, arenaBounds)) {
            if (!item.isAlive()) continue;
            ItemStack stack = item.getItem();
            if (stack.isEmpty() || isDuelGun(stack)) {
                item.discard();
                continue;
            }
            item.setDeltaMovement(Vec3.ZERO);
            item.teleportTo(dest.x, dest.y, dest.z);
            item.hurtMarked = true;
        }
    }

    /**
     * The watchman's own night vision survives this: his component reapplies it once the duel freeze
     * lifts, so a blanket removal cannot permanently take it from him.
     */
    private static void removeNightVision(MinecraftServer server) {
        for (UUID playerId : SNAPSHOTS.keySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null) player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    private static void restorePlayArea() {
        if (level == null || originalPlayArea == null) return;
        MapVariablesWorldComponent mapVariables = MapVariablesWorldComponent.KEY.get(level);
        mapVariables.setPlayArea(originalPlayArea);
        mapVariables.sync();
    }

    /**
     * States that cannot be frozen mid-flight - anything that hides, splits or relocates a player - are
     * ended cleanly for everyone before the arena swallows the round.
     */
    private static void finishBlockingStates(ServerLevel duelLevel) {
        for (ServerPlayer player : List.copyOf(duelLevel.players())) {
            PuppeteerControlComponent puppeteer = PuppeteerControlComponent.KEY.get(player);
            if (puppeteer != null && puppeteer.isControlling()) puppeteer.returnToBody(player);

            IllusionistComponent illusionist = IllusionistComponent.KEY.get(player);
            if (illusionist != null && !illusionist.cloneEntityIds.isEmpty()) illusionist.reset();

            MorticianComponent mortician = MorticianComponent.KEY.get(player);
            if (mortician != null && mortician.isDisguised()) mortician.reset();

            SniperComponent sniper = SniperComponent.KEY.get(player);
            if (sniper != null) sniper.cancelAiming();

            StuntDoubleComponent stuntDouble = StuntDoubleComponent.KEY.get(player);
            if (stuntDouble != null && stuntDouble.isMimicking()) stuntDouble.reset();
        }
    }

    /**
     * A duel is settled by aim alone: every shield layer of both duelists is torn off beforehand.
     * The counts are remembered so a duelist who lives through it gets them back.
     */
    private static void captureAndClearShields(ServerPlayer duelist) {
        int penitentLayers = 0;
        PenitentComponent penitent = PenitentComponent.KEY.get(duelist);
        if (penitent != null) {
            penitentLayers = Math.max(0, penitent.getShieldLayers());
            while (penitent.consumeShield()) {
            }
        }
        int nightmareLayers = 0;
        NightmareComponent nightmare = NightmareComponent.KEY.get(duelist);
        if (nightmare != null) {
            nightmareLayers = Math.max(0, nightmare.getShieldLayers());
            while (nightmare.consumeShield()) {
            }
        }
        int boneharvesterLayers = 0;
        BoneharvesterComponent boneharvester = BoneharvesterComponent.KEY.get(duelist);
        if (boneharvester != null) {
            boneharvesterLayers = Math.max(0, boneharvester.getShieldLayers());
            while (boneharvester.consumeShield()) {
            }
        }
        int zhangshiLayers = 0;
        ZhangshiComponent zhangshi = ZhangshiComponent.KEY.get(duelist);
        if (zhangshi != null) {
            zhangshiLayers = Math.max(0, zhangshi.getShieldLayers());
            while (zhangshi.consumeShield()) {
            }
        }
        int bartenderArmor = getBartenderArmor(duelist);
        setBartenderArmor(duelist, 0);

        SHIELD_SNAPSHOTS.put(duelist.getUUID(), new ShieldSnapshot(
            penitentLayers,
            nightmareLayers,
            boneharvesterLayers,
            zhangshiLayers,
            bartenderArmor
        ));
    }

    /**
     * The layers were only pawned for the duel: a duelist who is still alive - the winner, or both of
     * them when the duel is torn down unresolved - gets them back. Death forfeits them.
     */
    private static void returnShields(MinecraftServer server) {
        for (Map.Entry<UUID, ShieldSnapshot> entry : SHIELD_SNAPSHOTS.entrySet()) {
            ShieldSnapshot snapshot = entry.getValue();
            if (snapshot.isEmpty()) continue;
            ServerPlayer duelist = server.getPlayerList().getPlayer(entry.getKey());
            if (duelist == null || !GameFunctions.isPlayerAliveAndSurvival(duelist)) continue;

            PenitentComponent penitent = PenitentComponent.KEY.get(duelist);
            if (penitent != null) {
                for (int i = 0; i < snapshot.penitent(); i++) {
                    if (!penitent.addShieldLayer()) break;
                }
            }
            NightmareComponent nightmare = NightmareComponent.KEY.get(duelist);
            if (nightmare != null) {
                for (int i = 0; i < snapshot.nightmare(); i++) {
                    if (!nightmare.addShieldLayer()) break;
                }
            }
            BoneharvesterComponent boneharvester = BoneharvesterComponent.KEY.get(duelist);
            if (boneharvester != null) {
                for (int i = 0; i < snapshot.boneharvester(); i++) {
                    if (!boneharvester.applyBoneShield()) break;
                }
            }
            ZhangshiComponent zhangshi = ZhangshiComponent.KEY.get(duelist);
            if (zhangshi != null && snapshot.zhangshi() > 0) {
                zhangshi.setShieldLayers(snapshot.zhangshi());
            }
            if (snapshot.bartender() > 0) {
                setBartenderArmor(duelist, snapshot.bartender());
            }
        }
        SHIELD_SNAPSHOTS.clear();
    }

    /** Noell's bartender shield is not a compile-time dependency, so it is reached through reflection. */
    private static void resolveBartenderReflection() {
        if (bartenderLookupAttempted) return;
        bartenderLookupAttempted = true;
        try {
            Class<?> componentClass =
                Class.forName("org.agmas.noellesroles.bartender.BartenderPlayerComponent");
            Field keyField;
            try {
                keyField = componentClass.getField("KEY");
            } catch (NoSuchFieldException exception) {
                keyField = componentClass.getDeclaredField("KEY");
                keyField.trySetAccessible();
            }
            Field armorField;
            try {
                armorField = componentClass.getField("armor");
            } catch (NoSuchFieldException exception) {
                armorField = componentClass.getDeclaredField("armor");
                armorField.trySetAccessible();
            }
            if (keyField.get(null) instanceof ComponentKey<?> key) {
                bartenderShieldKey = key;
                bartenderArmorField = armorField;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private static int getBartenderArmor(ServerPlayer duelist) {
        resolveBartenderReflection();
        if (bartenderShieldKey == null || bartenderArmorField == null) return 0;
        try {
            Object component = bartenderShieldKey.get(duelist);
            return component == null ? 0 : Math.max(0, bartenderArmorField.getInt(component));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return 0;
        }
    }

    private static void setBartenderArmor(ServerPlayer duelist, int armor) {
        resolveBartenderReflection();
        if (bartenderShieldKey == null || bartenderArmorField == null) return;
        try {
            Object component = bartenderShieldKey.get(duelist);
            if (component == null || bartenderArmorField.getInt(component) == armor) return;
            bartenderArmorField.setInt(component, armor);
            try {
                component.getClass().getMethod("sync").invoke(component);
            } catch (ReflectiveOperationException exception) {
                bartenderShieldKey.sync(duelist);
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    /**
     * The duel is fought with the granted revolver alone. One hotbar slot is emptied - along with the
     * off hand, which could still fire a stowed gun - and the selection is pinned there for the whole
     * duel, so knives or firearms already in the inventory stay out of reach. The revolver later
     * materialises in that very slot on the draw.
     */
    private static void lockHotbar(ServerPlayer duelist) {
        Inventory inventory = duelist.getInventory();
        int slot = inventory.selected;

        ItemStack offhand = inventory.offhand.get(0);
        if (!offhand.isEmpty()) {
            inventory.offhand.set(0, ItemStack.EMPTY);
            stow(duelist, offhand, slot);
        }
        ItemStack held = inventory.getItem(slot);
        if (!held.isEmpty()) {
            inventory.setItem(slot, ItemStack.EMPTY);
            stow(duelist, held, slot);
        }

        LOCKED_SLOTS.put(duelist.getUUID(), slot);
        forceSelectedSlot(duelist, slot);
    }

    /** Displaced items stay with their owner; only a completely full inventory spills onto the floor. */
    private static void stow(ServerPlayer duelist, ItemStack stack, int reservedSlot) {
        Inventory inventory = duelist.getInventory();
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            if (slot == reservedSlot || !inventory.getItem(slot).isEmpty()) continue;
            inventory.setItem(slot, stack);
            return;
        }
        duelist.drop(stack, false);
    }

    private static void enforceHotbarLocks(MinecraftServer server) {
        for (Map.Entry<UUID, Integer> entry : LOCKED_SLOTS.entrySet()) {
            ServerPlayer duelist = server.getPlayerList().getPlayer(entry.getKey());
            if (duelist == null || !GameFunctions.isPlayerAliveAndSurvival(duelist)) continue;
            if (duelist.getInventory().selected != entry.getValue()) {
                forceSelectedSlot(duelist, entry.getValue());
            }
        }
    }

    private static void forceSelectedSlot(ServerPlayer duelist, int slot) {
        duelist.getInventory().selected = slot;
        duelist.connection.send(new ClientboundSetCarriedItemPacket(slot));
    }

    /**
     * The duel gun is always granted, even to a duelist who owns a revolver of his own - that one sits
     * stowed behind the hotbar lock and must stay untouched. Any per-item cooldown left on revolvers
     * (a shop purchase, the opening safe period) is wiped so the draw fires instantly.
     */
    private static void giveRevolver(ServerPlayer duelist) {
        Inventory inventory = duelist.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isDuelGun(inventory.getItem(slot))) return;
        }
        ItemStack revolver = WatheItems.REVOLVER.getDefaultInstance();
        CustomData.update(DataComponents.CUSTOM_DATA, revolver, tag -> tag.putBoolean(DUEL_GUN_TAG, true));
        Integer lockedSlot = LOCKED_SLOTS.get(duelist.getUUID());
        if (lockedSlot != null) {
            ItemStack occupant = inventory.getItem(lockedSlot);
            if (!occupant.isEmpty()) {
                inventory.setItem(lockedSlot, ItemStack.EMPTY);
                stow(duelist, occupant, lockedSlot);
            }
            inventory.setItem(lockedSlot, revolver);
            forceSelectedSlot(duelist, lockedSlot);
        } else if (!duelist.addItem(revolver)) {
            duelist.drop(revolver, false);
        }
        duelist.getCooldowns().removeCooldown(WatheItems.REVOLVER);
    }

    public static boolean isDuelGun(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean(DUEL_GUN_TAG);
    }

    /** Only the loaned gun is confiscated; a duelist's own firearms stay exactly where they were stowed. */
    private static void stripDuelGuns(ServerPlayer duelist) {
        Inventory inventory = duelist.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isDuelGun(inventory.getItem(slot))) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    private static void teleport(ServerPlayer player, double x, double y, double z, float yaw, float pitch) {
        // Train beds and seats hold the player in place; a rider's position is owned by the vehicle and
        // a sleeper keeps the bed pose, so both grips are broken before the move.
        if (player.isSleeping()) player.stopSleepInBed(true, true);
        player.stopRiding();
        player.connection.teleport(x, y, z, yaw, pitch);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        player.hurtMarked = true;
    }

    private static float horizontalYaw(double dx, double dz) {
        return (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
    }

    /**
     * The warp is delayed until every screen is black, so nobody sees the jump from the train to the
     * arena. Spectators are benched here rather than at skill-press for the same reason.
     */
    private static void placeArena(MinecraftServer server) {
        if (arenaPlaced || level == null || spectatorPos == null) return;
        arenaPlaced = true;

        ServerPlayer cowboy = server.getPlayerList().getPlayer(cowboyId);
        ServerPlayer target = server.getPlayerList().getPlayer(targetId);
        teleportToAnchor(cowboy);
        teleportToAnchor(target);

        for (ServerPlayer player : List.copyOf(level.players())) {
            if (player.getUUID().equals(cowboyId) || player.getUUID().equals(targetId)) continue;
            // Players who are already spectating (or admin creative) keep their mode; only the ones we
            // actively bench are restored later.
            if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                player.setGameMode(GameType.SPECTATOR);
                MODE_CHANGED.add(player.getUUID());
            }
            teleport(player, spectatorPos.x, spectatorPos.y, spectatorPos.z, spectatorYaw, 0.0F);
        }
        hideDuelistsFromCrowd(server, true);
    }

    private static void teleportToAnchor(ServerPlayer duelist) {
        if (duelist == null) return;
        DuelAnchor anchor = DUEL_ANCHORS.get(duelist.getUUID());
        if (anchor == null) return;
        teleport(duelist, anchor.x(), anchor.y(), anchor.z(), anchor.yaw(), 0.0F);
    }

    /** Held for the whole countdown: no turning around for an early peek, no walking off the line. */
    private static void pinDuelists(MinecraftServer server) {
        if (!arenaPlaced) return;
        for (Map.Entry<UUID, DuelAnchor> entry : DUEL_ANCHORS.entrySet()) {
            ServerPlayer duelist = server.getPlayerList().getPlayer(entry.getKey());
            if (duelist == null || !GameFunctions.isPlayerAliveAndSurvival(duelist)) continue;
            DuelAnchor anchor = entry.getValue();
            duelist.connection.teleport(anchor.x(), anchor.y(), anchor.z(), anchor.yaw(), 0.0F);
            duelist.setYRot(anchor.yaw());
            duelist.setXRot(0.0F);
            duelist.setYHeadRot(anchor.yaw());
            duelist.setYBodyRot(anchor.yaw());
        }
    }

    /** Spectators stay on the viewing pad facing the arena until the overlay has lifted. */
    private static void pinSpectators(MinecraftServer server) {
        if (!arenaPlaced || level == null || spectatorPos == null) return;
        for (ServerPlayer player : level.players()) {
            if (player.getUUID().equals(cowboyId) || player.getUUID().equals(targetId)) continue;
            player.connection.teleport(spectatorPos.x, spectatorPos.y, spectatorPos.z, spectatorYaw, 0.0F);
            player.setYRot(spectatorYaw);
            player.setXRot(0.0F);
        }
    }

    /** One block along the locked facing, so an east-west pit walks them apart (or together) on the X axis. */
    private static void stepDuelistsForward() {
        for (UUID id : List.copyOf(DUEL_ANCHORS.keySet())) {
            DuelAnchor anchor = DUEL_ANCHORS.get(id);
            Vec3 forward = Vec3.directionFromRotation(0.0F, anchor.yaw());
            DUEL_ANCHORS.put(id, new DuelAnchor(
                anchor.x() + forward.x,
                anchor.y(),
                anchor.z() + forward.z,
                anchor.yaw()
            ));
        }
    }

    /**
     * Late joiners miss the start/stop packets, so they get the current hide flags on login.
     */
    public static void syncJoiningClient(ServerPlayer player) {
        ServerPlayNetworking.send(player, new CowboyDuelIdentityS2CPacket(isActive()));
        boolean duelist = (cowboyId != null && player.getUUID().equals(cowboyId))
            || (targetId != null && player.getUUID().equals(targetId));
        if (duelist && phase != Phase.IDLE && phase != Phase.FIGHT) {
            ServerPlayNetworking.send(player, new CowboyDuelLookLockS2CPacket(true));
        }
        if (!isActive() || !duelistsHiddenFromCrowd) return;
        UUID first = cowboyId != null ? cowboyId : new UUID(0L, 0L);
        UUID second = targetId != null ? targetId : new UUID(0L, 0L);
        if (duelist) return;
        ServerPlayNetworking.send(player, new CowboyDuelHideS2CPacket(true, first, second));
    }

    /** Crosshair role names, cohort tags and spectator instinct stay off for the whole duel. */
    private static void hideSpectatorIdentities(MinecraftServer server, boolean hide) {
        CowboyDuelIdentityS2CPacket packet = new CowboyDuelIdentityS2CPacket(hide);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, packet);
        }
    }

    private static void lockDuelistLook(MinecraftServer server, boolean lock) {
        CowboyDuelLookLockS2CPacket packet = new CowboyDuelLookLockS2CPacket(lock);
        for (UUID id : new UUID[]{cowboyId, targetId}) {
            if (id == null) continue;
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) ServerPlayNetworking.send(player, packet);
        }
    }

    private static void hideDuelistsFromCrowd(MinecraftServer server, boolean hide) {
        if (duelistsHiddenFromCrowd == hide) return;
        duelistsHiddenFromCrowd = hide;
        UUID first = cowboyId != null ? cowboyId : new UUID(0L, 0L);
        UUID second = targetId != null ? targetId : new UUID(0L, 0L);
        CowboyDuelHideS2CPacket packet = new CowboyDuelHideS2CPacket(hide, first, second);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (hide && (player.getUUID().equals(first) || player.getUUID().equals(second))) continue;
            ServerPlayNetworking.send(player, packet);
        }
    }

    /** Local 2D playback so the theme stays at full volume after the warp. */
    private static void playShowdownMusic() {
        if (level == null) return;
        CowboyShowdownMusicS2CPacket packet =
            new CowboyShowdownMusicS2CPacket(true, BrinConfig.cowboyDuelMusicVolume());
        for (ServerPlayer player : level.players()) {
            ServerPlayNetworking.send(player, packet);
        }
    }

    private static void stopShowdownMusic(MinecraftServer server) {
        CowboyShowdownMusicS2CPacket packet = new CowboyShowdownMusicS2CPacket(false, 0.0F);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, packet);
        }
    }

    private static void sendTitle(Component text, int fadeIn, int stay, int fadeOut) {
        if (level == null) return;
        for (ServerPlayer player : level.players()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
            player.connection.send(new ClientboundSetTitleTextPacket(text));
        }
    }

    private static void broadcast(MinecraftServer server, Component message) {
        server.getPlayerList().broadcastSystemMessage(message, false);
    }
}
