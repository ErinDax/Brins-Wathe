package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.BrinIcFlags;
import dev.doctor4t.wathe.client.WatheClient;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InstinctHudNameMixin {
    @Shadow
    public abstract Font getFont();

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
    private void brinDrawInstinctHudNames(GuiGraphics graphics, net.minecraft.client.DeltaTracker delta, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer viewer = client.player;
        if (viewer == null || client.level == null) return;
        if (!WatheClient.isInstinctEnabled()) return;

        float tickDelta = delta.getGameTimeDeltaPartialTick(true);
        Vec3 eye = viewer.getEyePosition(tickDelta);
        Vec3 look = viewer.getViewVector(tickDelta).normalize();
        Vec3 end = eye.add(look.scale(2048.0));
        boolean throughWalls = BrinIcFlags.instinctHudNamesThroughWalls;
        Player best = null;
        double bestDist = Double.MAX_VALUE;
        Entity camera = client.getCameraEntity();
        if (camera == null) return;

        for (Player target : client.level.players()) {
            if (target == viewer || target.isRemoved()) continue;
            if (target.distanceTo(viewer) <= 2.1F) continue;
            if (WatheClient.getInstinctHighlight(target) == -1) continue;
            if (target.distanceToSqr(camera) > 4194304.0) continue;
            Optional<Vec3> hit = target.getBoundingBox().inflate(0.15).clip(eye, end);
            if (hit.isEmpty()) continue;
            if (!throughWalls && brinBlockedByTerrain(viewer, eye, hit.get())) continue;
            double dist = eye.distanceToSqr(hit.get());
            if (dist < bestDist) {
                bestDist = dist;
                best = target;
            }
        }
        Component name;
        if (best == null) {
            if (!throughWalls) return;
            var snapshot = cn.erindax.brinswathe.client.BrinInstinctClient.lookedAt(eye, end, viewer);
            if (snapshot == null) return;
            name = Component.literal(snapshot.name());
        } else {
            name = best.getDisplayName();
        }

        Font font = this.getFont();
        graphics.pose().pushPose();
        graphics.pose().translate(graphics.guiWidth() / 2.0F, graphics.guiHeight() / 2.0F + 6.0F, 0.0F);
        graphics.pose().scale(0.6F, 0.6F, 1.0F);
        int width = font.width(name);
        graphics.drawString(font, name, -width / 2, 16, Mth.color(1.0F, 1.0F, 1.0F) | 0xFF000000, true);
        graphics.pose().popPose();
    }

    @Unique
    private static boolean brinBlockedByTerrain(Player viewer, Vec3 eye, Vec3 point) {
        HitResult hit = viewer.level().clip(new ClipContext(
            eye,
            point,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            viewer
        ));
        return hit instanceof BlockHitResult && hit.getType() == HitResult.Type.BLOCK;
    }
}
