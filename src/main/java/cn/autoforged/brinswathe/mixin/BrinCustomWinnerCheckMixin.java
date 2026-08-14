package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.component.BrinCustomWinnerComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(GameRoundEndComponent.class)
public abstract class BrinCustomWinnerCheckMixin {
    @Unique
    private static final ResourceLocation THIEF_ID =
        ResourceLocation.fromNamespaceAndPath("stupid_express", "thief");

    @Shadow
    @Final
    private Level world;

    @Inject(method = "didWin", at = @At("HEAD"), cancellable = true)
    private void brinCheckCustomWinner(UUID playerId, CallbackInfoReturnable<Boolean> cir) {
        BrinCustomWinnerComponent winnerComponent = BrinCustomWinnerComponent.KEY.get(this.world);
        if (winnerComponent != null && winnerComponent.hasCustomWinner()) {
            cir.setReturnValue(winnerComponent.isWinner(playerId));
            return;
        }

        GameRoundEndComponent roundEnd = (GameRoundEndComponent) (Object) this;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.world);
        if (roundEnd.getWinStatus() != GameFunctions.WinStatus.KILLERS) return;

        // Wathe only awards a killer win to roles announced as KILLER, so every killer sided neutral has
        // to be named here to be settled with the wolves.
        if (gameWorld.isRole(playerId, BrinRoles.PENITENT) || brinIsThief(gameWorld, playerId)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static boolean brinIsThief(GameWorldComponent gameWorld, UUID playerId) {
        Role role = gameWorld.getRole(playerId);
        return role != null && THIEF_ID.equals(role.identifier());
    }
}
