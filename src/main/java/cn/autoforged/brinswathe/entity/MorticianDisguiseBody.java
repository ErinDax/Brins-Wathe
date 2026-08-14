package cn.autoforged.brinswathe.entity;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public interface MorticianDisguiseBody {
    boolean brin$isMorticianDisguise();

    void brin$setMorticianDisguise(boolean disguise);

    /**
     * The mortician hiding under this corpse. The body wears somebody else's uuid as its skin, so the
     * owner cannot be derived from {@code getPlayerUuid}.
     */
    @Nullable
    UUID brin$getMortician();

    void brin$setMortician(@Nullable UUID mortician);
}
