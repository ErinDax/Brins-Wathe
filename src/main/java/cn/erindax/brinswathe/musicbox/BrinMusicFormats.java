package cn.erindax.brinswathe.musicbox;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;

public final class BrinMusicFormats {
    public static final String MP3 = "mp3";
    public static final String OGG = "ogg";
    public static final int MAX_NAME_LENGTH = 64;
    private static final int OGG_PROBE_BYTES = 512;
    private static final int MP3_SYNC_WINDOW = 8192;
    private static final int MP3_PROBE_FRAMES = 3;

    private BrinMusicFormats() {
    }

    public static boolean isKnown(String format) {
        return MP3.equals(format) || OGG.equals(format);
    }

    public static String detect(byte[] data) {
        if (data == null || data.length < 64) return null;
        if (isOgg(data)) return OGG;
        if (isMp3(data)) return MP3;
        return null;
    }

    private static boolean isOgg(byte[] data) {
        if (data[0] != 'O' || data[1] != 'g' || data[2] != 'g' || data[3] != 'S') return false;
        int limit = Math.min(data.length - 7, OGG_PROBE_BYTES);
        for (int index = 4; index < limit; index++) {
            if (data[index] == 1
                && data[index + 1] == 'v'
                && data[index + 2] == 'o'
                && data[index + 3] == 'r'
                && data[index + 4] == 'b'
                && data[index + 5] == 'i'
                && data[index + 6] == 's') {
                return true;
            }
        }
        return false;
    }

    private static boolean isMp3(byte[] data) {
        int start = id3Length(data);
        if (start < 0 || start >= data.length) return false;
        int limit = Math.min(data.length - 1, start + MP3_SYNC_WINDOW);
        boolean synced = false;
        for (int index = start; index < limit; index++) {
            if ((data[index] & 0xFF) == 0xFF && (data[index + 1] & 0xE0) == 0xE0) {
                synced = true;
                break;
            }
        }
        if (!synced) return false;
        Bitstream bitstream = new Bitstream(new ByteArrayInputStream(data));
        try {
            Decoder decoder = new Decoder();
            int frames = 0;
            int frequency = -1;
            int layer = -1;
            while (frames < MP3_PROBE_FRAMES) {
                Header header = bitstream.readFrame();
                if (header == null) break;
                if (frequency < 0) {
                    frequency = header.frequency();
                    layer = header.layer();
                } else if (header.frequency() != frequency || header.layer() != layer) {
                    return false;
                }
                decoder.decodeFrame(header, bitstream);
                bitstream.closeFrame();
                frames++;
            }
            return frames >= 2 && frequency > 0;
        } catch (Exception | LinkageError exception) {
            return false;
        } finally {
            try {
                bitstream.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static int id3Length(byte[] data) {
        if (data.length < 10 || data[0] != 'I' || data[1] != 'D' || data[2] != '3') return 0;
        for (int index = 6; index < 10; index++) {
            if ((data[index] & 0x80) != 0) return -1;
        }
        int size = ((data[6] & 0x7F) << 21) | ((data[7] & 0x7F) << 14) | ((data[8] & 0x7F) << 7) | (data[9] & 0x7F);
        boolean footer = (data[5] & 0x10) != 0;
        return 10 + size + (footer ? 10 : 0);
    }

    public static String sha1(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(data));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static String cleanName(String raw) {
        if (raw == null) return "music";
        String name = raw.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".mp3") || lower.endsWith(".ogg")) {
            name = name.substring(0, name.length() - 4);
        }
        StringBuilder builder = new StringBuilder();
        name.codePoints()
            .filter(codePoint -> !Character.isISOControl(codePoint) && codePoint != '\u00A7')
            .forEach(builder::appendCodePoint);
        String cleaned = builder.toString().strip();
        if (cleaned.length() > MAX_NAME_LENGTH) {
            int end = MAX_NAME_LENGTH;
            if (Character.isHighSurrogate(cleaned.charAt(end - 1))) end--;
            cleaned = cleaned.substring(0, end).strip();
        }
        return cleaned.isEmpty() ? "music" : cleaned;
    }

    public static String describeSize(long bytes) {
        if (bytes >= 1024L * 1024L) return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
        if (bytes >= 1024L) return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        return bytes + " B";
    }
}
