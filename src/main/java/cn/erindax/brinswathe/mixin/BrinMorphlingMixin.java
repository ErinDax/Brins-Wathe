package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinNoelleAccess;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "org.agmas.noellesroles.morphling.MorphlingPlayerComponent", remap = false)
public abstract class BrinMorphlingMixin {
    @Shadow
    @Final
    private Player player;

    @Shadow
    public UUID disguise;

    @Shadow
    public int morphTicks;

    @Shadow
    public abstract void sync();

    @Shadow
    public abstract void stopMorph();

    @Shadow
    public abstract void setMorphTicks(int ticks);

    @Unique
    private boolean brinCopiedRole;

    @Inject(method = "startMorph", at = @At("HEAD"), cancellable = true)
    private void brinMorphDeadTeammate(UUID id, CallbackInfoReturnable<Boolean> cir) {
        Player target = this.player.level().getPlayerByUUID(id);
        if (target != null && GameFunctions.isPlayerAliveAndSurvival(target)) {
            this.player.displayClientMessage(Component.literal("你要变身的对象还没死哦"), false);
            cir.setReturnValue(false);
            return;
        }
        this.disguise = id;
        this.setMorphTicks(GameConstants.getInTicks(0, 120));
        GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
        Role role = game.getRole(id);
        if (role != null) {
            this.brinCopiedRole = true;
            game.addRole(this.player, role);
        }
        this.sync();
        cir.setReturnValue(true);
    }

    @Inject(method = "getMorphTicks", at = @At("HEAD"), cancellable = true, require = 0)
    private void brinHideMorphFromNoelleRenderer(CallbackInfoReturnable<Integer> cir) {
        if (!this.player.level().isClientSide) return;
        if (this.disguise == null) return;
        if (this.player.level().getPlayerByUUID(this.disguise) == null) cir.setReturnValue(0);
    }
    @Inject(method = "stopMorph", at = @At("HEAD"))
    private void brinRestoreMorphling(CallbackInfo ci) {
        if (!this.brinCopiedRole) return;
        Role morphling = BrinNoelleAccess.findRole(BrinNoelleAccess.MORPHLING_ID);
        if (morphling == null) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
        game.addRole(this.player, morphling);
        this.brinCopiedRole = false;
    }
    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private void brinKeepMorphOnDeadTargets(CallbackInfo ci) {
        if (this.morphTicks > 0 && this.disguise != null) {
            if (--this.morphTicks == 0) {
                this.stopMorph();
            }
            this.sync();
        }
        if (this.morphTicks < 0) {
            this.morphTicks++;
            this.sync();
        }
        ci.cancel();
    }
}
