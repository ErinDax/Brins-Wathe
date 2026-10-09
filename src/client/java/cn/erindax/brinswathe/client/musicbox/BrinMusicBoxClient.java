package cn.erindax.brinswathe.client.musicbox;

import cn.erindax.brinswathe.BrinIcFlags;
import cn.erindax.brinswathe.client.BrinFilePicker;
import cn.erindax.brinswathe.client.gui.BrinMusicBoxScreen;
import cn.erindax.brinswathe.musicbox.BrinMusicBox;
import cn.erindax.brinswathe.musicbox.BrinMusicFormats;
import cn.erindax.brinswathe.network.BrinMusicBoxRequestC2SPacket;
import cn.erindax.brinswathe.network.BrinMusicBoxStatusS2CPacket;
import cn.erindax.brinswathe.network.BrinMusicChunkS2CPacket;
import cn.erindax.brinswathe.network.BrinMusicFetchC2SPacket;
import cn.erindax.brinswathe.network.BrinMusicPlayS2CPacket;
import cn.erindax.brinswathe.network.BrinMusicUploadAckS2CPacket;
import cn.erindax.brinswathe.network.BrinMusicUploadChunkC2SPacket;
import cn.erindax.brinswathe.network.BrinMusicUploadStartC2SPacket;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class BrinMusicBoxClient {
    private static final long CACHE_LIMIT_BYTES = 64L * 1024L * 1024L;
    private static final int UPLOAD_TIMEOUT_TICKS = 20 * 30;
    private static final int DOWNLOAD_TIMEOUT_TICKS = 20 * 20;
    private static final int KEEP_ALIVE_INTERVAL_TICKS = 20;
    private static final int MAX_BROADCAST_RETRIES = 3;
    private static final String KEY_UPLOADED = "message.brinswathe.music_box.uploaded";
    private static final String KEY_DELETED = "message.brinswathe.music_box.deleted";
    private static final String KEY_CLEARED = "message.brinswathe.music_box.cleared";
    private static final String KEY_FAILED = "message.brinswathe.music_box.failed";
    private static final String KEY_INVALID = "message.brinswathe.music_box.invalid";
    private static final String KEY_TOO_LARGE = "message.brinswathe.music_box.too_large";
    private static final String KEY_PLAY_FAILED = "message.brinswathe.music_box.play_failed";

    private static final LinkedHashMap<String, CachedTrack> CACHE = new LinkedHashMap<>(16, 0.75F, true);
    private static long cacheBytes;
    private static Status status = Status.UNKNOWN;
    @Nullable
    private static Component notice;
    private static boolean picking;
    @Nullable
    private static Upload upload;
    @Nullable
    private static Download download;
    @Nullable
    private static Track broadcast;
    @Nullable
    private static BrinMusicSoundInstance broadcastSound;
    @Nullable
    private static BrinMusicSoundInstance previewSound;
    @Nullable
    private static String previewPending;
    @Nullable
    private static String failedTrack;
    private static int broadcastRetries;
    private static int ticks;

    private BrinMusicBoxClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(BrinMusicBoxStatusS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> onStatus(payload)));
        ClientPlayNetworking.registerGlobalReceiver(BrinMusicUploadAckS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> onAck(payload.received())));
        ClientPlayNetworking.registerGlobalReceiver(BrinMusicPlayS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> onPlay(payload)));
        ClientPlayNetworking.registerGlobalReceiver(BrinMusicChunkS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> onChunk(payload)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(BrinMusicBoxClient::reset));
        ClientTickEvents.END_CLIENT_TICK.register(BrinMusicBoxClient::tick);
    }

    public static void openScreen(Minecraft client, @Nullable Screen parent) {
        notice = null;
        if (ClientPlayNetworking.canSend(BrinMusicBoxRequestC2SPacket.TYPE)) {
            ClientPlayNetworking.send(new BrinMusicBoxRequestC2SPacket(BrinMusicBoxRequestC2SPacket.ACTION_QUERY));
        } else {
            status = Status.UNAVAILABLE;
        }
        client.setScreen(new BrinMusicBoxScreen(parent));
    }

    public static boolean statusKnown() {
        return status.known();
    }

    public static boolean enabled() {
        return status.enabled();
    }

    public static int maxBytes() {
        return currentLimit();
    }

    public static boolean hasTrack() {
        return !status.trackId().isEmpty();
    }

    public static String trackName() {
        return status.trackName();
    }

    public static int trackSize() {
        return status.trackSize();
    }

    public static boolean isPicking() {
        return picking;
    }

    public static boolean isUploading() {
        return upload != null;
    }

    public static boolean isProcessing() {
        Upload current = upload;
        return current != null && current.awaitingResult;
    }

    public static int uploadPercent() {
        Upload current = upload;
        if (current == null || current.total == 0) return 0;
        return Math.min(100, current.acked * 100 / current.total);
    }

    public static boolean isPreviewLoading() {
        return previewPending != null;
    }

    public static int previewLoadPercent() {
        Download current = download;
        if (current == null || !current.preview || current.chunks == 0) return 0;
        return Math.min(100, current.receivedCount * 100 / current.chunks);
    }

    @Nullable
    public static Track nowPlaying() {
        return broadcast;
    }

    public static int broadcastLoadPercent() {
        Track track = broadcast;
        Download current = download;
        if (track == null || current == null || current.preview || !current.trackId.equals(track.trackId())) return -1;
        if (current.chunks == 0) return 0;
        return Math.min(100, current.receivedCount * 100 / current.chunks);
    }

    @Nullable
    public static Component notice() {
        return notice;
    }

    public static boolean canPick() {
        return status.known() && status.enabled() && !picking && upload == null;
    }

    public static boolean canDelete() {
        return hasTrack() && !picking && upload == null;
    }

    public static boolean canPreview() {
        Download current = download;
        return hasTrack()
            && upload == null
            && broadcast == null
            && (current == null || current.preview)
            && BrinMusicFormats.isKnown(status.format())
            && status.trackSize() > 0
            && status.trackSize() <= BrinMusicBox.ABSOLUTE_MAX_BYTES;
    }

    public static boolean isPreviewing() {
        if (previewPending != null) return true;
        BrinMusicSoundInstance sound = previewSound;
        if (sound == null) return false;
        if (Minecraft.getInstance().getSoundManager().isActive(sound)) return true;
        previewSound = null;
        return false;
    }

    public static void pickFile() {
        if (!canPick()) return;
        picking = true;
        notice = null;
        int limit = currentLimit();
        Thread thread = new Thread(() -> {
            Path path;
            try {
                path = BrinFilePicker.pick("MP3 / OGG", BrinMusicFormats.MP3, BrinMusicFormats.OGG);
            } catch (Exception exception) {
                path = null;
            }
            if (path == null) {
                Minecraft.getInstance().execute(() -> picking = false);
                return;
            }
            load(path, limit);
        }, "brin-music-pick");
        thread.setDaemon(true);
        thread.start();
    }

    public static void acceptDroppedFile(Path path) {
        if (!canPick()) return;
        picking = true;
        notice = null;
        int limit = currentLimit();
        Thread thread = new Thread(() -> load(path, limit), "brin-music-load");
        thread.setDaemon(true);
        thread.start();
    }

    public static void delete() {
        if (!canDelete() || !ClientPlayNetworking.canSend(BrinMusicBoxRequestC2SPacket.TYPE)) return;
        stopPreview();
        ClientPlayNetworking.send(new BrinMusicBoxRequestC2SPacket(BrinMusicBoxRequestC2SPacket.ACTION_DELETE));
    }

    public static void togglePreview() {
        if (isPreviewing()) {
            stopPreview();
            return;
        }
        if (!canPreview()) return;
        String trackId = status.trackId();
        CachedTrack cached = CACHE.get(trackId);
        if (cached != null) {
            startPreviewSound(trackId, cached);
            return;
        }
        previewPending = trackId;
        startDownload(trackId, status.format(), status.trackSize(), true);
    }

    public static void stopPreview() {
        previewPending = null;
        Download current = download;
        if (current != null && current.preview) download = null;
        BrinMusicSoundInstance sound = previewSound;
        previewSound = null;
        if (sound != null) Minecraft.getInstance().getSoundManager().stop(sound);
    }

    private static void load(Path path, int limit) {
        Path fileName = path.getFileName();
        String name = BrinMusicFormats.cleanName(fileName == null ? "" : fileName.toString());
        byte[] data = null;
        String format = null;
        Component error = null;
        try {
            long size = Files.size(path);
            if (size > limit) {
                error = message(KEY_TOO_LARGE, BrinMusicFormats.describeSize(limit));
            } else if (size <= 0) {
                error = message(KEY_INVALID, "");
            } else {
                data = Files.readAllBytes(path);
                format = BrinMusicFormats.detect(data);
                if (format == null) error = message(KEY_INVALID, "");
            }
        } catch (IOException | RuntimeException exception) {
            error = message(KEY_FAILED, "");
        }
        byte[] bytes = data;
        String detected = format;
        Component failure = error;
        Minecraft.getInstance().execute(() -> {
            picking = false;
            if (failure != null || bytes == null || detected == null) {
                showNotice(failure != null ? failure : message(KEY_FAILED, ""));
                return;
            }
            startUpload(name, detected, bytes);
        });
    }

    private static void startUpload(String name, String format, byte[] data) {
        if (!ClientPlayNetworking.canSend(BrinMusicUploadStartC2SPacket.TYPE)) {
            showNotice(message(KEY_FAILED, ""));
            return;
        }
        int limit = currentLimit();
        if (data.length > limit) {
            showNotice(message(KEY_TOO_LARGE, BrinMusicFormats.describeSize(limit)));
            return;
        }
        stopPreview();
        upload = new Upload(data, ticks);
        notice = null;
        ClientPlayNetworking.send(new BrinMusicUploadStartC2SPacket(name, format, data.length));
    }

    private static void onStatus(BrinMusicBoxStatusS2CPacket packet) {
        String previousTrack = status.trackId();
        status = new Status(
            true,
            packet.enabled(),
            packet.maxBytes(),
            packet.trackId(),
            packet.trackName(),
            packet.format(),
            packet.trackSize()
        );
        String key = packet.messageKey();
        Upload current = upload;
        if (current != null && (!key.isEmpty() || !packet.enabled())) {
            if (KEY_UPLOADED.equals(key)
                && !packet.trackId().isEmpty()
                && packet.trackSize() == current.data.length
                && BrinMusicFormats.isKnown(packet.format())) {
                cachePut(packet.trackId(), packet.format(), current.data);
            }
            upload = null;
        }
        if (!previousTrack.equals(packet.trackId())) stopPreview();
        if (!key.isEmpty()) showNotice(message(key, packet.messageArg()));
    }

    private static void onAck(int received) {
        Upload current = upload;
        if (current == null) return;
        current.acked = Math.max(current.acked, Math.min(received, current.total));
        current.lastProgressTick = ticks;
        if (current.acked >= current.total) {
            current.awaitingResult = true;
            return;
        }
        while (current.nextToSend < current.total && current.nextToSend < current.acked + BrinMusicBox.WINDOW) {
            int index = current.nextToSend++;
            int offset = index * BrinMusicBox.CHUNK_SIZE;
            int end = Math.min(current.data.length, offset + BrinMusicBox.CHUNK_SIZE);
            ClientPlayNetworking.send(new BrinMusicUploadChunkC2SPacket(index, Arrays.copyOfRange(current.data, offset, end)));
        }
    }

    private static void onPlay(BrinMusicPlayS2CPacket packet) {
        if (packet.isStop()) {
            broadcast = null;
            broadcastRetries = 0;
            stopBroadcastSound();
            Download current = download;
            if (current != null && !current.preview) download = null;
            return;
        }
        if (packet.size() <= 0
            || packet.size() > BrinMusicBox.ABSOLUTE_MAX_BYTES
            || !BrinMusicFormats.isKnown(packet.format())) {
            return;
        }
        Track track = new Track(packet.trackId(), packet.format(), packet.size(), packet.ownerName(), packet.trackName());
        broadcast = track;
        broadcastRetries = 0;
        failedTrack = null;
        stopBroadcastSound();
        stopPreview();
        CachedTrack cached = CACHE.get(track.trackId());
        if (cached != null) {
            startBroadcastSound(track, cached);
            return;
        }
        startDownload(track.trackId(), track.format(), track.size(), false);
    }

    private static void onChunk(BrinMusicChunkS2CPacket packet) {
        Download current = download;
        if (current == null || !current.trackId.equals(packet.trackId())) return;
        byte[] data = packet.data();
        int index = packet.index();
        if (data.length == 0) {
            abortDownload(current);
            return;
        }
        if (index < 0 || index >= current.chunks || current.received[index]) return;
        int offset = index * BrinMusicBox.CHUNK_SIZE;
        int expected = Math.min(BrinMusicBox.CHUNK_SIZE, current.size - offset);
        if (data.length != expected) {
            abortDownload(current);
            return;
        }
        System.arraycopy(data, 0, current.data, offset, expected);
        current.received[index] = true;
        current.receivedCount++;
        current.lastProgressTick = ticks;
        if (current.receivedCount < current.chunks) {
            pumpDownload(current);
            return;
        }
        download = null;
        byte[] bytes = current.data;
        Util.backgroundExecutor().execute(() -> {
            boolean valid = BrinMusicFormats.sha1(bytes).equals(current.trackId);
            Minecraft.getInstance().execute(() -> finishDownload(current, valid));
        });
    }

    private static void startDownload(String trackId, String format, int size, boolean preview) {
        Download created = new Download(trackId, format, size, preview, ticks);
        download = created;
        pumpDownload(created);
    }

    private static void pumpDownload(Download current) {
        if (!ClientPlayNetworking.canSend(BrinMusicFetchC2SPacket.TYPE)) return;
        while (current.nextToRequest < current.chunks
            && current.nextToRequest - current.receivedCount < BrinMusicBox.WINDOW) {
            ClientPlayNetworking.send(new BrinMusicFetchC2SPacket(current.trackId, current.nextToRequest++));
        }
    }

    private static void abortDownload(Download current) {
        if (download == current) download = null;
        if (current.preview && current.trackId.equals(previewPending)) {
            previewPending = null;
            showNotice(message(KEY_FAILED, ""));
        }
    }

    private static void finishDownload(Download finished, boolean valid) {
        if (!valid) {
            if (finished.preview && finished.trackId.equals(previewPending)) {
                previewPending = null;
                showNotice(message(KEY_FAILED, ""));
            }
            return;
        }
        CachedTrack cached = cachePut(finished.trackId, finished.format, finished.data);
        if (finished.preview) {
            if (finished.trackId.equals(previewPending)) {
                previewPending = null;
                startPreviewSound(finished.trackId, cached);
            }
            return;
        }
        Track track = broadcast;
        if (track != null && track.trackId().equals(finished.trackId)) startBroadcastSound(track, cached);
    }

    private static void startBroadcastSound(Track track, CachedTrack cached) {
        stopBroadcastSound();
        if (track.trackId().equals(failedTrack)) return;
        String trackId = track.trackId();
        BrinMusicSoundInstance sound = new BrinMusicSoundInstance(
            trackId,
            cached.data(),
            cached.format(),
            true,
            () -> onPlaybackFailed(trackId)
        );
        broadcastSound = sound;
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    private static void stopBroadcastSound() {
        BrinMusicSoundInstance sound = broadcastSound;
        broadcastSound = null;
        if (sound != null) Minecraft.getInstance().getSoundManager().stop(sound);
    }

    private static void startPreviewSound(String trackId, CachedTrack cached) {
        BrinMusicSoundInstance previous = previewSound;
        if (previous != null) Minecraft.getInstance().getSoundManager().stop(previous);
        BrinMusicSoundInstance sound = new BrinMusicSoundInstance(
            trackId,
            cached.data(),
            cached.format(),
            false,
            () -> onPlaybackFailed(trackId)
        );
        previewSound = sound;
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    private static void onPlaybackFailed(String trackId) {
        Minecraft.getInstance().execute(() -> {
            if (trackId.equals(failedTrack)) return;
            failedTrack = trackId;
            showNotice(message(KEY_PLAY_FAILED, ""));
        });
    }

    private static void tick(Minecraft client) {
        ticks++;
        Upload current = upload;
        if (current != null && ticks - current.lastProgressTick > UPLOAD_TIMEOUT_TICKS) {
            upload = null;
            showNotice(message(KEY_FAILED, ""));
        }
        Download active = download;
        if (active != null && ticks - active.lastProgressTick > DOWNLOAD_TIMEOUT_TICKS) abortDownload(active);
        if (ticks % KEEP_ALIVE_INTERVAL_TICKS == 0) keepBroadcastAlive(client);
    }

    private static void keepBroadcastAlive(Minecraft client) {
        Track track = broadcast;
        if (track == null || client.getConnection() == null || track.trackId().equals(failedTrack)) return;
        CachedTrack cached = CACHE.get(track.trackId());
        if (cached == null) {
            if (download == null && broadcastRetries < MAX_BROADCAST_RETRIES) {
                broadcastRetries++;
                startDownload(track.trackId(), track.format(), track.size(), false);
            }
            return;
        }
        BrinMusicSoundInstance sound = broadcastSound;
        if (sound != null && client.getSoundManager().isActive(sound)) return;
        if (client.options.getSoundSourceVolume(SoundSource.MASTER) <= 0.0F
            || client.options.getSoundSourceVolume(SoundSource.RECORDS) <= 0.0F) {
            return;
        }
        startBroadcastSound(track, cached);
    }

    private static void reset() {
        stopBroadcastSound();
        stopPreview();
        status = Status.UNKNOWN;
        notice = null;
        upload = null;
        download = null;
        broadcast = null;
        previewPending = null;
        failedTrack = null;
        broadcastRetries = 0;
    }

    private static int currentLimit() {
        if (status.known() && status.maxBytes() > 0) return status.maxBytes();
        return BrinIcFlags.musicBoxMaxBytes();
    }

    private static void showNotice(Component text) {
        notice = text;
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && !(client.screen instanceof BrinMusicBoxScreen)) {
            client.player.displayClientMessage(text, false);
        }
    }

    private static Component message(String key, String arg) {
        MutableComponent text = arg == null || arg.isEmpty()
            ? Component.translatable(key)
            : Component.translatable(key, arg);
        return switch (key) {
            case KEY_UPLOADED, KEY_DELETED -> text.withStyle(ChatFormatting.GREEN);
            case KEY_CLEARED -> text.withStyle(ChatFormatting.GOLD);
            default -> text.withStyle(ChatFormatting.RED);
        };
    }

    private static CachedTrack cachePut(String trackId, String format, byte[] data) {
        CachedTrack previous = CACHE.remove(trackId);
        if (previous != null) cacheBytes -= previous.data().length;
        CachedTrack cached = new CachedTrack(format, data);
        CACHE.put(trackId, cached);
        cacheBytes += data.length;
        Iterator<Map.Entry<String, CachedTrack>> iterator = CACHE.entrySet().iterator();
        while (cacheBytes > CACHE_LIMIT_BYTES && iterator.hasNext()) {
            Map.Entry<String, CachedTrack> eldest = iterator.next();
            if (eldest.getKey().equals(trackId)) continue;
            cacheBytes -= eldest.getValue().data().length;
            iterator.remove();
        }
        return cached;
    }

    public record Track(String trackId, String format, int size, String ownerName, String trackName) {
    }

    private record CachedTrack(String format, byte[] data) {
    }

    private record Status(
        boolean known,
        boolean enabled,
        int maxBytes,
        String trackId,
        String trackName,
        String format,
        int trackSize
    ) {
        private static final Status UNKNOWN = new Status(false, false, 0, "", "", "", 0);
        private static final Status UNAVAILABLE = new Status(true, false, 0, "", "", "", 0);
    }

    private static final class Upload {
        private final byte[] data;
        private final int total;
        private int nextToSend;
        private int acked;
        private boolean awaitingResult;
        private int lastProgressTick;

        private Upload(byte[] data, int tick) {
            this.data = data;
            this.total = (data.length + BrinMusicBox.CHUNK_SIZE - 1) / BrinMusicBox.CHUNK_SIZE;
            this.lastProgressTick = tick;
        }
    }

    private static final class Download {
        private final String trackId;
        private final String format;
        private final int size;
        private final boolean preview;
        private final byte[] data;
        private final boolean[] received;
        private final int chunks;
        private int receivedCount;
        private int nextToRequest;
        private int lastProgressTick;

        private Download(String trackId, String format, int size, boolean preview, int tick) {
            this.trackId = trackId;
            this.format = format;
            this.size = size;
            this.preview = preview;
            this.data = new byte[size];
            this.chunks = (size + BrinMusicBox.CHUNK_SIZE - 1) / BrinMusicBox.CHUNK_SIZE;
            this.received = new boolean[this.chunks];
            this.lastProgressTick = tick;
        }
    }
}
