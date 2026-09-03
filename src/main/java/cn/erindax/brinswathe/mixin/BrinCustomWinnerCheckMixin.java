package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.component.BrinCustomWinnerComponent;
import cn.erindax.brinswathe.component.NightmareComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
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

        if (gameWorld.isRole(playerId, BrinRoles.PENITENT) || brinIsThief(gameWorld, playerId)) {
            cir.setReturnValue(true);
            return;
        }
        if (gameWorld.isRole(playerId, BrinRoles.NIGHTMARE)) {
            Player nightmare = this.world.getPlayerByUUID(playerId);
            cir.setReturnValue(nightmare == null || !NightmareComponent.isNightmareHour(gameWorld, nightmare));
        }
    }

    @Unique
    private static boolean brinIsThief(GameWorldComponent gameWorld, UUID playerId) {
        Role role = gameWorld.getRole(playerId);
        return role != null && THIEF_ID.equals(role.identifier());
    }
}
