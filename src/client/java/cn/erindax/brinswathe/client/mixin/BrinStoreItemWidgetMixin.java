package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.BrinItems;
import cn.erindax.brinswathe.BrinShopAccess;
import cn.erindax.brinswathe.component.BombComponent;
import cn.erindax.brinswathe.config.BrinConfig;
import cn.erindax.brinswathe.client.gui.BrinStoreItemWidget;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(targets = "dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen$StoreItemWidget")
public abstract class BrinStoreItemWidgetMixin implements BrinStoreItemWidget {
    @Shadow
    @Final
    public LimitedInventoryScreen screen;

    @Shadow
    @Final
    public ShopEntry entry;

    @Inject(method = "renderWidget", at = @At("TAIL"))
    private void brinRenderShopCooldown(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int seconds = this.brinCooldownSeconds(delta);
        if (seconds <= 0) return;

        AbstractWidget widget = (AbstractWidget) (Object) this;
        PoseStack pose = context.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, 400.0F);
        context.drawString(
            Minecraft.getInstance().font,
            String.valueOf(seconds),
            widget.getX(),
            widget.getY(),
            0xFFFF5555,
            true
        );
        pose.popPose();
    }

    @Unique
    private int brinCooldownSeconds(float delta) {
        LocalPlayer player = this.screen.player;
        if (this.entry.stack().is(BrinItems.XUEZI)) {
            if (!player.getCooldowns().isOnCooldown(BrinItems.XUEZI)) return 0;
            float percent = player.getCooldowns().getCooldownPercent(BrinItems.XUEZI, delta);
            return Math.max(1, Mth.ceil(percent * BrinConfig.xueziCooldownSeconds()));
        }
        BombComponent bomb = BombComponent.KEY.get(player);
        if (bomb == null) return 0;
        if (BrinShopAccess.isBomberShopBomb(this.entry.stack())) {
            return Mth.ceil(bomb.bombPurchaseCooldownTicks() / 20.0F);
        }
        if (BrinShopAccess.isBomberShopMine(this.entry.stack())) {
            return Mth.ceil(bomb.minePurchaseCooldownTicks() / 20.0F);
        }
        return 0;
    }
}
