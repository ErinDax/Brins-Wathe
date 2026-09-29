package cn.erindax.brinswathe.client.musicbox;

import cn.erindax.brinswathe.BrinsWathe;
import cn.erindax.brinswathe.musicbox.BrinMusicFormats;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

@Environment(EnvType.CLIENT)
public final class BrinMusicSoundInstance extends AbstractSoundInstance {
    public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(BrinsWathe.MOD_ID, "music_box");

    private final String trackId;
    private final byte[] data;
    private final String format;
    private final boolean loop;
    private final Runnable onFailure;

    public BrinMusicSoundInstance(String trackId, byte[] data, String format, boolean loop, Runnable onFailure) {
        super(LOCATION, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.trackId = trackId;
        this.data = data;
        this.format = format;
        this.loop = loop;
        this.onFailure = onFailure;
        this.looping = loop;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.x = 0.0D;
        this.y = 0.0D;
        this.z = 0.0D;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
    }

    public String trackId() {
        return this.trackId;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, ResourceLocation id, boolean repeatInstantly) {
        return CompletableFuture.supplyAsync(this::openGuarded, Util.backgroundExecutor());
    }

    private AudioStream openGuarded() {
        try {
            return new BrinGuardedAudioStream(this.open(), this.onFailure);
        } catch (Exception | LinkageError exception) {
            BrinsWathe.LOGGER.warn("Music box track could not be opened", exception);
            return BrinGuardedAudioStream.failed(this.onFailure);
        }
    }

    private AudioStream open() throws IOException {
        if (BrinMusicFormats.MP3.equals(this.format)) return new BrinMp3AudioStream(this.data, this.loop);
        if (this.loop) return new LoopingAudioStream(JOrbisAudioStream::new, new ByteArrayInputStream(this.data));
        return new JOrbisAudioStream(new ByteArrayInputStream(this.data));
    }
}
