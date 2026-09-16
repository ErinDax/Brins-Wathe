package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinRoleLabels;
import cn.erindax.brinswathe.component.BrinRoundRecapComponent;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.client.gui.RoundTextRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(RoundTextRenderer.class)
public abstract class BrinRoundRecapHudMixin {
    @Inject(
        method = "renderHud",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameRoundEndComponent$RoundEndData;player()Lcom/mojang/authlib/GameProfile;"
        )
    )
    private static void brinLabelOfficialHead(
        Font font,
        LocalPlayer player,
        GuiGraphics graphics,
        CallbackInfo ci,
        @Local GameRoundEndComponent.RoundEndData row
    ) {
        if (player == null || row == null) return;
        BrinRoundRecapComponent recap = BrinRoundRecapComponent.KEY.get(player.level());
        String name = row.player().getName();
        String roleId = "";
        if (recap != null) {
            BrinRoundRecapComponent.Identity identity = recap.identityOf(row.player().getId());
            if (identity != null) {
                if (!identity.name().isEmpty()) name = identity.name();
                roleId = identity.roleId();
            }
        }
        MutableComponent role = roleId.isEmpty() ? null : BrinRoleLabels.of(roleId);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(8.0F, 8.0F, 0.0F);
        pose.scale(0.28F, 0.28F, 1.0F);
        int center = 14;
        graphics.drawString(font, name, center - font.width(name) / 2, 0, 0xFFFFFF, true);
        if (role != null) {
            graphics.drawString(font, role, center - font.width(role) / 2, 10, 0xFFFFFF, true);
        }
        pose.popPose();
    }
}
