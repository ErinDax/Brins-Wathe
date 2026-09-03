package cn.erindax.brinswathe.entity;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public interface MorticianDisguiseBody {
    boolean brin$isMorticianDisguise();

    void brin$setMorticianDisguise(boolean disguise);

    @Nullable
    UUID brin$getMortician();
    void brin$setMortician(@Nullable UUID mortician);
}
