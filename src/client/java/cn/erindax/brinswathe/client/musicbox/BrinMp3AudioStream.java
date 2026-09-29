package cn.erindax.brinswathe.client.musicbox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.AudioStream;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

@Environment(EnvType.CLIENT)
public final class BrinMp3AudioStream implements AudioStream {
    private static final int MAX_FRAME_BYTES = 1152 * 2 * 2;
    private static final int MAX_BAD_FRAMES = 32;

    private final byte[] data;
    private final boolean loop;
    private final int channels;
    private final AudioFormat format;
    private Bitstream bitstream;
    private Decoder decoder;
    @Nullable
    private short[] carry;
    private int carryLength;
    private int framesThisPass;
    private boolean ended;

    public BrinMp3AudioStream(byte[] data, boolean loop) throws IOException {
        this.data = data;
        this.loop = loop;
        this.open();
        SampleBuffer first = this.nextFrame();
        if (first == null) {
            this.closeBitstream();
            throw new IOException("No decodable MP3 frame");
        }
        this.channels = first.getChannelCount() == 1 ? 1 : 2;
        this.format = new AudioFormat(
            first.getSampleFrequency(),
            16,
            this.channels,
            true,
            ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN
        );
        this.stash(first);
        this.framesThisPass = 1;
    }

    @Override
    public AudioFormat getFormat() {
        return this.format;
    }

    @Override
    public ByteBuffer read(int size) {
        int target = Math.max(0, size);
        ByteBuffer out = BufferUtils.createByteBuffer(target + MAX_FRAME_BYTES * 2);
        if (this.carry != null) {
            this.put(out, this.carry, this.carryLength);
            this.carry = null;
            this.carryLength = 0;
        }
        while (out.position() < target && !this.ended) {
            SampleBuffer frame = this.nextFrame();
            if (frame == null) {
                if (!this.loop || this.framesThisPass == 0) {
                    this.ended = true;
                    break;
                }
                this.closeBitstream();
                this.open();
                continue;
            }
            this.framesThisPass++;
            if (frame.getChannelCount() != this.channels) continue;
            int length = Math.min(frame.getBufferLength(), frame.getBuffer().length);
            if (out.remaining() < length * 2) {
                this.stash(frame);
                break;
            }
            this.put(out, frame.getBuffer(), length);
        }
        out.flip();
        return out;
    }

    @Override
    public void close() {
        this.ended = true;
        this.closeBitstream();
    }

    private void open() {
        this.bitstream = new Bitstream(new ByteArrayInputStream(this.data));
        this.decoder = new Decoder();
        this.framesThisPass = 0;
    }

    @Nullable
    private SampleBuffer nextFrame() {
        int bad = 0;
        while (true) {
            Header header;
            try {
                header = this.bitstream.readFrame();
            } catch (Exception exception) {
                return null;
            }
            if (header == null) return null;
            try {
                if (this.decoder.decodeFrame(header, this.bitstream) instanceof SampleBuffer output) return output;
            } catch (Exception exception) {
                if (++bad > MAX_BAD_FRAMES) return null;
            } finally {
                this.bitstream.closeFrame();
            }
        }
    }

    private void stash(SampleBuffer frame) {
        int length = Math.min(frame.getBufferLength(), frame.getBuffer().length);
        this.carry = Arrays.copyOf(frame.getBuffer(), length);
        this.carryLength = length;
    }

    private void put(ByteBuffer out, short[] samples, int length) {
        out.asShortBuffer().put(samples, 0, length);
        out.position(out.position() + length * 2);
    }

    private void closeBitstream() {
        try {
            this.bitstream.close();
        } catch (Exception ignored) {
        }
    }
}
