package cn.erindax.brinswathe.entity;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public interface PuppetEntity {
    boolean brin$isPuppet();

    void brin$setPuppet(boolean puppet);

    boolean brin$isPuppetBodyProxy();

    void brin$setPuppetBodyProxy(boolean bodyProxy);

    @Nullable
    UUID brin$getPuppeteer();
    void brin$setPuppeteer(@Nullable UUID puppeteer);
}
