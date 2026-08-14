package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.client.BrinsWatheClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.agmas.harpymodloader.client.HarpymodloaderClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla RoleNameRenderer only prints "cohort" when both the viewer and the target are killers.
 * A dead killer benched into the crowd would still see that, so killer-feature checks are refused
 * for spectators for the whole duel. Player names stay on the crosshair.
 *
 * Harpy's role+modifier line is gated on {@code WatheClient.isPlayerSpectatingOrCreative()}. That
 * call is lied about only while this HUD method is running; RoleNameRenderer itself uses
 * {@code GameFunctions} for the 8-block spectator range, so the name still appears.
 */
@Environment(EnvType.CLIENT)
@Mixin(value = RoleNameRenderer.class, priority = 500)
public abstract class CowboyDuelSpectatorIdentityMixin {
    @Inject(method = "renderHud", at = @At("HEAD"))
    private static void brinBeginHideCowboySpectatorRoles(Font renderer, LocalPlayer player, GuiGraphics context,
                                                          DeltaTracker tickCounter, CallbackInfo ci) {
        BrinsWatheClient.setSuppressSpectatorRoleHud(BrinsWatheClient.shouldHideSpectatorIdentities());
    }

    @Inject(method = "renderHud", at = @At("RETURN"))
    private static void brinEndHideCowboySpectatorRoles(Font renderer, LocalPlayer player, GuiGraphics context,
                                                        DeltaTracker tickCounter, CallbackInfo ci) {
        BrinsWatheClient.setSuppressSpectatorRoleHud(false);
    }

    @WrapOperation(
        method = "renderHud",
        require = 0,
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/client/WatheClient;isPlayerSpectatingOrCreative()Z"
        )
    )
    private static boolean brinNoHarpyRoleForCowboySpectators(Operation<Boolean> original) {
        if (BrinsWatheClient.shouldHideSpectatorIdentities()) return false;
        return original.call();
    }

    @WrapOperation(
        method = "renderHud",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/world/entity/player/Player;)Z"
        )
    )
    private static boolean brinHideCohortDuringCowboyDuel(GameWorldComponent instance, Player player,
                                                          Operation<Boolean> original) {
        if (BrinsWatheClient.shouldHideSpectatorIdentities()) return false;
        return original.call(instance, player);
    }

    /**
     * Harpy writes hudRole before getDisplayName returns. Clearing afterwards makes their later
     * drawString inject a no-op even if the WatheClient wrap missed their call.
     */
    @Inject(
        method = "renderHud",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getDisplayName()Lnet/minecraft/network/chat/Component;",
            shift = At.Shift.AFTER
        )
    )
    private static void brinClearHarpyRoleAfterName(Font renderer, LocalPlayer player, GuiGraphics context,
                                                    DeltaTracker tickCounter, CallbackInfo ci) {
        if (!BrinsWatheClient.shouldHideSpectatorIdentities()) return;
        HarpymodloaderClient.hudRole = null;
        HarpymodloaderClient.modifiers = null;
    }
}
