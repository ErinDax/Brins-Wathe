package cn.erindax.brinswathe.client.musicbox;

import cn.erindax.brinswathe.BrinsWathe;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.AudioStream;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

@Environment(EnvType.CLIENT)
public final class BrinGuardedAudioStream implements AudioStream {
    private static final AudioFormat FALLBACK_FORMAT =
        new AudioFormat(44100.0F, 16, 2, true, ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);

    @Nullable
    private final AudioStream delegate;
    private final Runnable onFailure;
    private boolean failed;

    public BrinGuardedAudioStream(@Nullable AudioStream delegate, Runnable onFailure) {
        this.delegate = delegate;
        this.onFailure = onFailure;
        this.failed = delegate == null;
    }

    public static BrinGuardedAudioStream failed(Runnable onFailure) {
        onFailure.run();
        return new BrinGuardedAudioStream(null, onFailure);
    }

    @Override
    public AudioFormat getFormat() {
        return this.delegate == null ? FALLBACK_FORMAT : this.delegate.getFormat();
    }

    @Override
    public ByteBuffer read(int size) {
        if (this.failed || this.delegate == null) return BufferUtils.createByteBuffer(0);
        try {
            return this.delegate.read(size);
        } catch (Exception | LinkageError exception) {
            this.failed = true;
            BrinsWathe.LOGGER.warn("Music box playback failed", exception);
            this.onFailure.run();
            return BufferUtils.createByteBuffer(0);
        }
    }

    @Override
    public void close() {
        if (this.delegate == null) return;
        try {
            this.delegate.close();
        } catch (Exception ignored) {
        }
    }
}
