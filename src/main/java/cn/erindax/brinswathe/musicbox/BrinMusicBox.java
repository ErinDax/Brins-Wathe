package cn.erindax.brinswathe.musicbox;

import cn.erindax.brinswathe.BrinIcFlags;
import cn.erindax.brinswathe.BrinsWathe;
import cn.erindax.brinswathe.component.BrinRoundRecapComponent;
import cn.erindax.brinswathe.network.BrinMusicBoxRequestC2SPacket;
import cn.erindax.brinswathe.network.BrinMusicBoxStatusS2CPacket;
import cn.erindax.brinswathe.network.BrinMusicChunkS2CPacket;
import cn.erindax.brinswathe.network.BrinMusicFetchC2SPacket;
import cn.erindax.brinswathe.network.BrinMusicPlayS2CPacket;
import cn.erindax.brinswathe.network.BrinMusicUploadAckS2CPacket;
import cn.erindax.brinswathe.network.BrinMusicUploadChunkC2SPacket;
import cn.erindax.brinswathe.network.BrinMusicUploadStartC2SPacket;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class BrinMusicBox {
    public static final int CHUNK_SIZE = 64 * 1024;
    public static final int WINDOW = 4;
    public static final int ABSOLUTE_MAX_BYTES = BrinIcFlags.MUSIC_BOX_MAX_KILOBYTES * 1024;
    private static final long SESSION_TIMEOUT_MS = 30_000L;
    private static final long PREVIEW_TIMEOUT_MS = 60_000L;
    private static final long UPLOAD_COOLDOWN_MS = 5_000L;
    private static final int FETCHES_PER_TICK = 32;
    private static final int MAX_PENDING_FETCHES = WINDOW * 2;
    private static final String INDEX_FILE = "index.json";
    private static final Path DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("brinswathe_music");
    private static final byte[] LOADING = new byte[0];
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Brin Music IO");
        thread.setDaemon(true);
        return thread;
    });

    private static final Map<UUID, Entry> INDEX = new HashMap<>();
    private static final Map<UUID, UploadSession> SESSIONS = new HashMap<>();
    private static final Set<UUID> PROCESSING = new HashSet<>();
    private static final Set<UUID> CLEARED_WHILE_PROCESSING = new HashSet<>();
    private static final Map<UUID, Long> LAST_UPLOAD = new HashMap<>();
    private static final Map<UUID, Preview> PREVIEWS = new HashMap<>();
    private static final Map<UUID, Integer> PENDING_FETCHES = new HashMap<>();
    private static final ArrayDeque<FetchRequest> FETCH_QUEUE = new ArrayDeque<>();
    @Nullable
    private static Broadcast current;
    @Nullable
    private static PendingRound pendingRound;
    private static boolean roundResolved;
    private static int generation;
    private static int tickCounter;

    private BrinMusicBox() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            resetState();
            loadIndex();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> resetState());
        ServerTickEvents.END_SERVER_TICK.register(BrinMusicBox::tick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Broadcast broadcast = current;
            if (broadcast != null) send(handler.player, broadcast.packet());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.player.getUUID();
            SESSIONS.remove(id);
            PREVIEWS.remove(id);
            PENDING_FETCHES.remove(id);
        });
        ServerPlayNetworking.registerGlobalReceiver(
            BrinMusicBoxRequestC2SPacket.TYPE,
            (payload, context) -> handleRequest(context.player(), payload)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            BrinMusicUploadStartC2SPacket.TYPE,
            (payload, context) -> handleUploadStart(context.player(), payload)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            BrinMusicUploadChunkC2SPacket.TYPE,
            (payload, context) -> handleUploadChunk(context.player(), payload)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            BrinMusicFetchC2SPacket.TYPE,
            (payload, context) -> handleFetch(context.player(), payload)
        );
    }

    public static void onRoundEnd(List<ServerPlayer> players, GameFunctions.WinStatus status) {
        if (!BrinIcFlags.musicBoxEnabled || roundResolved || players == null || players.isEmpty()) return;
        if (!(players.getFirst().level() instanceof ServerLevel level)) return;
        pendingRound = new PendingRound(level, new ArrayList<>(players), status, BrinMvp.customWinners(level));
    }

    public static void onGameStart(@Nullable MinecraftServer server) {
        BrinRoundStats.reset();
        roundResolved = false;
        pendingRound = null;
        if (server != null) stopBroadcast(server);
    }

    public static boolean isPlaying() {
        return current != null;
    }

    public static boolean stopBroadcast(MinecraftServer server) {
        generation++;
        Broadcast broadcast = current;
        current = null;
        if (broadcast == null) return false;
        BrinMusicPlayS2CPacket stop = BrinMusicPlayS2CPacket.stop();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            send(player, stop);
        }
        return true;
    }

    public static boolean adminClear(MinecraftServer server, UUID owner) {
        SESSIONS.remove(owner);
        PREVIEWS.remove(owner);
        boolean processing = PROCESSING.contains(owner);
        if (processing) CLEARED_WHILE_PROCESSING.add(owner);
        Entry removed = INDEX.remove(owner);
        deleteFiles(owner);
        if (removed != null) saveIndex();
        Broadcast broadcast = current;
        if (broadcast != null && broadcast.owner().equals(owner)) stopBroadcast(server);
        boolean cleared = removed != null || processing;
        ServerPlayer player = server.getPlayerList().getPlayer(owner);
        if (player != null && cleared) {
            send(player, status(owner, "message.brinswathe.music_box.cleared", ""));
        }
        return cleared;
    }

    private static void tick(MinecraftServer server) {
        PendingRound pending = pendingRound;
        if (pending != null) {
            pendingRound = null;
            resolveRound(server, pending);
        }
        pumpFetches(server);
        if (++tickCounter % 20 == 0) expire(server);
    }

    private static void resolveRound(MinecraftServer server, PendingRound pending) {
        if (roundResolved || !BrinIcFlags.musicBoxEnabled) return;
        ServerLevel level = pending.level();
        GameFunctions.WinStatus status = GameRoundEndComponent.KEY.get(level).getWinStatus();
        if (status == null || status == GameFunctions.WinStatus.NONE) status = pending.status();
        Set<UUID> soloWinners = BrinMvp.customWinners(level);
        if (soloWinners == null) soloWinners = pending.soloWinners();
        BrinMvp.Result mvp = BrinMvp.resolve(level, pending.players(), status, soloWinners);
        if (mvp == null && soloWinners == null && (status == null || status == GameFunctions.WinStatus.NONE)) return;
        roundResolved = true;
        if (mvp == null) return;
        Component line = Component.translatable(
            "message.brinswathe.music_box.mvp",
            BrinRoundRecapComponent.playerLabel(mvp.name(), mvp.roleId())
        ).withStyle(ChatFormatting.GOLD);
        server.getPlayerList().broadcastSystemMessage(line, false);
        Entry entry = INDEX.get(mvp.id());
        if (entry != null) startBroadcast(server, mvp, entry);
    }

    private static void startBroadcast(MinecraftServer server, BrinMvp.Result mvp, Entry entry) {
        int token = ++generation;
        Path path = trackPath(mvp.id(), entry.format());
        IO.execute(() -> {
            byte[] data = readTrack(path);
            String trackId = data == null ? "" : BrinMusicFormats.sha1(data);
            server.execute(() -> {
                if (token != generation) return;
                if (data == null) {
                    BrinsWathe.LOGGER.warn("Music box track of {} could not be read", mvp.id());
                    ServerPlayer owner = server.getPlayerList().getPlayer(mvp.id());
                    if (owner != null) {
                        owner.sendSystemMessage(Component.translatable("message.brinswathe.music_box.read_failed")
                            .withStyle(ChatFormatting.RED));
                    }
                    return;
                }
                Broadcast broadcast = new Broadcast(trackId, entry.format(), data, mvp.id(), mvp.name(), entry.name());
                current = broadcast;
                BrinMusicPlayS2CPacket play = broadcast.packet();
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    send(player, play);
                }
                server.getPlayerList().broadcastSystemMessage(
                    Component.translatable(
                        "message.brinswathe.music_box.now_playing",
                        Component.literal(mvp.name()).withStyle(ChatFormatting.YELLOW),
                        Component.literal(entry.name()).withStyle(ChatFormatting.WHITE)
                    ).withStyle(ChatFormatting.AQUA),
                    false
                );
            });
        });
    }

    private static void handleRequest(ServerPlayer player, BrinMusicBoxRequestC2SPacket packet) {
        switch (packet.action()) {
            case BrinMusicBoxRequestC2SPacket.ACTION_QUERY -> send(player, status(player.getUUID(), "", ""));
            case BrinMusicBoxRequestC2SPacket.ACTION_DELETE -> handleDelete(player);
            default -> {
            }
        }
    }

    private static void handleDelete(ServerPlayer player) {
        UUID id = player.getUUID();
        if (PROCESSING.contains(id)) {
            send(player, status(id, "message.brinswathe.music_box.busy", ""));
            return;
        }
        SESSIONS.remove(id);
        PREVIEWS.remove(id);
        Entry removed = INDEX.remove(id);
        if (removed != null) {
            deleteFiles(id);
            saveIndex();
        }
        send(player, status(id, removed != null ? "message.brinswathe.music_box.deleted" : "", ""));
    }

    private static void handleUploadStart(ServerPlayer player, BrinMusicUploadStartC2SPacket packet) {
        UUID id = player.getUUID();
        if (!BrinIcFlags.musicBoxEnabled) {
            send(player, status(id, "", ""));
            return;
        }
        if (PROCESSING.contains(id)) {
            send(player, status(id, "message.brinswathe.music_box.busy", ""));
            return;
        }
        long now = Util.getMillis();
        Long last = LAST_UPLOAD.get(id);
        if (last != null && now - last < UPLOAD_COOLDOWN_MS) {
            send(player, status(id, "message.brinswathe.music_box.cooldown", ""));
            return;
        }
        int maxBytes = BrinIcFlags.musicBoxMaxBytes();
        if (packet.size() > maxBytes) {
            send(player, status(id, "message.brinswathe.music_box.too_large", BrinMusicFormats.describeSize(maxBytes)));
            return;
        }
        if (packet.size() <= 0 || !BrinMusicFormats.isKnown(packet.format())) {
            send(player, status(id, "message.brinswathe.music_box.invalid", ""));
            return;
        }
        LAST_UPLOAD.put(id, now);
        SESSIONS.put(id, new UploadSession(BrinMusicFormats.cleanName(packet.fileName()), packet.size(), now));
        send(player, new BrinMusicUploadAckS2CPacket(0));
    }

    private static void handleUploadChunk(ServerPlayer player, BrinMusicUploadChunkC2SPacket packet) {
        UUID id = player.getUUID();
        UploadSession session = SESSIONS.get(id);
        if (session == null) return;
        int expected = Math.min(CHUNK_SIZE, session.size - session.received);
        byte[] data = packet.data();
        if (packet.index() != session.nextIndex || data == null || data.length != expected) {
            SESSIONS.remove(id);
            send(player, status(id, "message.brinswathe.music_box.failed", ""));
            return;
        }
        System.arraycopy(data, 0, session.buffer, session.received, expected);
        session.received += expected;
        session.nextIndex++;
        session.lastActivity = Util.getMillis();
        send(player, new BrinMusicUploadAckS2CPacket(session.nextIndex));
        if (session.received < session.size) return;
        SESSIONS.remove(id);
        finishUpload(player.server, id, session);
    }

    private static void finishUpload(MinecraftServer server, UUID owner, UploadSession session) {
        PROCESSING.add(owner);
        byte[] data = session.buffer;
        String name = session.name;
        IO.execute(() -> {
            String format = BrinMusicFormats.detect(data);
            if (format == null) {
                server.execute(() -> completeUpload(server, owner, null, "message.brinswathe.music_box.invalid", ""));
                return;
            }
            String trackId = BrinMusicFormats.sha1(data);
            try {
                writeAtomically(trackPath(owner, format), data);
                String other = BrinMusicFormats.MP3.equals(format) ? BrinMusicFormats.OGG : BrinMusicFormats.MP3;
                Files.deleteIfExists(trackPath(owner, other));
            } catch (IOException exception) {
                BrinsWathe.LOGGER.warn("Failed to store music box track of {}", owner, exception);
                server.execute(() -> completeUpload(server, owner, null, "message.brinswathe.music_box.failed", ""));
                return;
            }
            Entry entry = new Entry(trackId, format, data.length, name, System.currentTimeMillis());
            server.execute(() -> completeUpload(server, owner, entry, "message.brinswathe.music_box.uploaded", name));
        });
    }

    private static void completeUpload(
        MinecraftServer server,
        UUID owner,
        @Nullable Entry entry,
        String messageKey,
        String messageArg
    ) {
        PROCESSING.remove(owner);
        if (CLEARED_WHILE_PROCESSING.remove(owner)) {
            deleteFiles(owner);
            return;
        }
        if (entry != null) {
            INDEX.put(owner, entry);
            PREVIEWS.remove(owner);
            saveIndex();
        }
        ServerPlayer player = server.getPlayerList().getPlayer(owner);
        if (player != null) send(player, status(owner, messageKey, messageArg));
    }

    private static void handleFetch(ServerPlayer player, BrinMusicFetchC2SPacket packet) {
        UUID id = player.getUUID();
        int pending = PENDING_FETCHES.getOrDefault(id, 0);
        if (pending >= MAX_PENDING_FETCHES) return;
        PENDING_FETCHES.put(id, pending + 1);
        FETCH_QUEUE.add(new FetchRequest(id, packet.trackId(), packet.index()));
    }

    private static void pumpFetches(MinecraftServer server) {
        int budget = FETCHES_PER_TICK;
        int remaining = FETCH_QUEUE.size();
        while (budget > 0 && remaining-- > 0) {
            FetchRequest request = FETCH_QUEUE.poll();
            if (request == null) break;
            ServerPlayer player = server.getPlayerList().getPlayer(request.player());
            if (player == null) {
                releaseFetch(request.player());
                continue;
            }
            byte[] data = resolveData(server, request.player(), request.trackId());
            if (data == LOADING) {
                FETCH_QUEUE.add(request);
                continue;
            }
            releaseFetch(request.player());
            send(player, new BrinMusicChunkS2CPacket(request.trackId(), request.index(), slice(data, request.index())));
            budget--;
        }
    }

    private static void releaseFetch(UUID player) {
        PENDING_FETCHES.computeIfPresent(player, (id, count) -> count > 1 ? count - 1 : null);
    }

    @Nullable
    private static byte[] resolveData(MinecraftServer server, UUID requester, String trackId) {
        Broadcast broadcast = current;
        if (broadcast != null && broadcast.trackId().equals(trackId)) return broadcast.data();
        Entry entry = INDEX.get(requester);
        if (entry == null || !entry.id().equals(trackId)) return null;
        long now = Util.getMillis();
        Preview preview = PREVIEWS.get(requester);
        if (preview != null && preview.trackId.equals(trackId)) {
            preview.lastAccess = now;
            if (preview.failed) return null;
            return preview.data == null ? LOADING : preview.data;
        }
        Preview created = new Preview(trackId, now);
        PREVIEWS.put(requester, created);
        Path path = trackPath(requester, entry.format());
        IO.execute(() -> {
            byte[] data = readTrack(path);
            boolean valid = data != null && BrinMusicFormats.sha1(data).equals(trackId);
            server.execute(() -> {
                if (PREVIEWS.get(requester) != created) return;
                if (valid) {
                    created.data = data;
                } else {
                    created.failed = true;
                }
            });
        });
        return LOADING;
    }

    private static byte[] slice(@Nullable byte[] data, int index) {
        if (data == null || index < 0) return new byte[0];
        long offset = (long) index * CHUNK_SIZE;
        if (offset >= data.length) return new byte[0];
        int start = (int) offset;
        return Arrays.copyOfRange(data, start, Math.min(data.length, start + CHUNK_SIZE));
    }

    private static void expire(MinecraftServer server) {
        long now = Util.getMillis();
        Iterator<Map.Entry<UUID, UploadSession>> sessions = SESSIONS.entrySet().iterator();
        while (sessions.hasNext()) {
            Map.Entry<UUID, UploadSession> item = sessions.next();
            if (now - item.getValue().lastActivity < SESSION_TIMEOUT_MS) continue;
            sessions.remove();
            ServerPlayer player = server.getPlayerList().getPlayer(item.getKey());
            if (player != null) send(player, status(item.getKey(), "message.brinswathe.music_box.failed", ""));
        }
        PREVIEWS.values().removeIf(preview -> now - preview.lastAccess > PREVIEW_TIMEOUT_MS);
    }

    private static BrinMusicBoxStatusS2CPacket status(UUID owner, String messageKey, String messageArg) {
        Entry entry = INDEX.get(owner);
        return new BrinMusicBoxStatusS2CPacket(
            BrinIcFlags.musicBoxEnabled,
            BrinIcFlags.musicBoxMaxBytes(),
            entry == null ? "" : entry.id(),
            entry == null ? "" : entry.name(),
            entry == null ? "" : entry.format(),
            entry == null ? 0 : entry.size(),
            messageKey,
            messageArg
        );
    }

    private static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (ServerPlayNetworking.canSend(player, payload.type())) ServerPlayNetworking.send(player, payload);
    }

    private static void resetState() {
        generation++;
        current = null;
        pendingRound = null;
        roundResolved = false;
        INDEX.clear();
        SESSIONS.clear();
        PROCESSING.clear();
        CLEARED_WHILE_PROCESSING.clear();
        LAST_UPLOAD.clear();
        PREVIEWS.clear();
        PENDING_FETCHES.clear();
        FETCH_QUEUE.clear();
        BrinRoundStats.reset();
    }

    private static void loadIndex() {
        Path file = DIRECTORY.resolve(INDEX_FILE);
        if (!Files.isRegularFile(file)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> item : root.entrySet()) {
                try {
                    UUID owner = UUID.fromString(item.getKey());
                    JsonObject object = item.getValue().getAsJsonObject();
                    String id = object.get("id").getAsString();
                    String format = object.get("format").getAsString();
                    int size = object.get("size").getAsInt();
                    String name = BrinMusicFormats.cleanName(object.has("name") ? object.get("name").getAsString() : "");
                    long uploaded = object.has("uploaded") ? object.get("uploaded").getAsLong() : 0L;
                    if (!BrinMusicFormats.isKnown(format) || !isTrackId(id) || size <= 0 || size > ABSOLUTE_MAX_BYTES) continue;
                    if (!Files.isRegularFile(trackPath(owner, format))) continue;
                    INDEX.put(owner, new Entry(id, format, size, name, uploaded));
                } catch (RuntimeException ignored) {
                }
            }
        } catch (Exception exception) {
            BrinsWathe.LOGGER.warn("Failed to load music box index", exception);
        }
    }

    private static void saveIndex() {
        JsonObject root = new JsonObject();
        for (Map.Entry<UUID, Entry> item : INDEX.entrySet()) {
            Entry entry = item.getValue();
            JsonObject object = new JsonObject();
            object.addProperty("id", entry.id());
            object.addProperty("format", entry.format());
            object.addProperty("size", entry.size());
            object.addProperty("name", entry.name());
            object.addProperty("uploaded", entry.uploadedAt());
            root.add(item.getKey().toString(), object);
        }
        byte[] data = root.toString().getBytes(StandardCharsets.UTF_8);
        IO.execute(() -> {
            try {
                writeAtomically(DIRECTORY.resolve(INDEX_FILE), data);
            } catch (IOException exception) {
                BrinsWathe.LOGGER.warn("Failed to save music box index", exception);
            }
        });
    }

    private static void deleteFiles(UUID owner) {
        Path mp3 = trackPath(owner, BrinMusicFormats.MP3);
        Path ogg = trackPath(owner, BrinMusicFormats.OGG);
        IO.execute(() -> {
            try {
                Files.deleteIfExists(mp3);
                Files.deleteIfExists(ogg);
            } catch (IOException exception) {
                BrinsWathe.LOGGER.warn("Failed to delete music box track of {}", owner, exception);
            }
        });
    }

    @Nullable
    private static byte[] readTrack(Path path) {
        try {
            if (!Files.isRegularFile(path)) return null;
            long size = Files.size(path);
            if (size <= 0 || size > ABSOLUTE_MAX_BYTES) return null;
            return Files.readAllBytes(path);
        } catch (IOException exception) {
            return null;
        }
    }

    private static void writeAtomically(Path target, byte[] data) throws IOException {
        Files.createDirectories(target.getParent());
        Path temp = target.resolveSibling(target.getFileName().toString() + ".tmp");
        Files.write(temp, data);
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Path trackPath(UUID owner, String format) {
        return DIRECTORY.resolve(owner + "." + format);
    }

    private static boolean isTrackId(String id) {
        if (id.length() != 40) return false;
        for (int index = 0; index < id.length(); index++) {
            char character = id.charAt(index);
            if ((character < '0' || character > '9') && (character < 'a' || character > 'f')) return false;
        }
        return true;
    }

    private record Entry(String id, String format, int size, String name, long uploadedAt) {
    }

    private record PendingRound(
        ServerLevel level,
        List<ServerPlayer> players,
        GameFunctions.WinStatus status,
        @Nullable Set<UUID> soloWinners
    ) {
    }

    private record FetchRequest(UUID player, String trackId, int index) {
    }

    private record Broadcast(String trackId, String format, byte[] data, UUID owner, String ownerName, String trackName) {
        private BrinMusicPlayS2CPacket packet() {
            return new BrinMusicPlayS2CPacket(this.trackId, this.format, this.data.length, this.ownerName, this.trackName);
        }
    }

    private static final class UploadSession {
        private final String name;
        private final int size;
        private final byte[] buffer;
        private int received;
        private int nextIndex;
        private long lastActivity;

        private UploadSession(String name, int size, long now) {
            this.name = name;
            this.size = size;
            this.buffer = new byte[size];
            this.lastActivity = now;
        }
    }

    private static final class Preview {
        private final String trackId;
        @Nullable
        private byte[] data;
        private boolean failed;
        private long lastAccess;

        private Preview(String trackId, long now) {
            this.trackId = trackId;
            this.lastAccess = now;
        }
    }
}
