package cn.autoforged.brinswathe.entity;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public interface PuppetEntity {
    boolean brin$isPuppet();

    void brin$setPuppet(boolean puppet);

    boolean brin$isPuppetBodyProxy();

    void brin$setPuppetBodyProxy(boolean bodyProxy);

    /**
     * The puppeteer that owns this model. A puppet carries the dead player's uuid as its skin, so the
     * controller cannot be derived from {@code getPlayerUuid} the way the illusionist's clones do it.
     */
    @Nullable
    UUID brin$getPuppeteer();

    void brin$setPuppeteer(@Nullable UUID puppeteer);
}
