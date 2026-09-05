package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinModifiers;
import cn.erindax.brinswathe.BrinNoelleAccess;
import cn.erindax.brinswathe.BrinPoisonBridge;
import cn.erindax.brinswathe.BrinsWathe;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = PlayerPoisonComponent.class, priority = 2000)
public abstract class BrinPoisonApplyMixin {
    @Shadow
    @Final
    private Player player;

    @Shadow
    public int poisonTicks;

    @Shadow
    private int initialPoisonTicks;

    @Shadow
    private int poisonPulseCooldown;

    @Shadow
    public float pulseProgress;

    @Shadow
    public boolean pulsing;

    @Shadow
    public UUID poisoner;

    @Shadow
    public abstract void sync();

    @Shadow
    public abstract void reset();

    @WrapMethod(method = "setPoisonTicks")
    private void brinSetPoisonTicks(int ticks, UUID poisoner, Operation<Void> original) {
        UUID resolved = poisoner != null ? poisoner : BrinPoisonBridge.UNKNOWN_POISONER;
        BrinPoisonBridge.beginSetPoison();
        original.call(ticks, resolved);
        if (BrinPoisonBridge.setPoisonBodyReached()) return;

        if (this.player.level().isClientSide) {
            this.brinApplyOfficialBody(ticks, resolved);
            return;
        }

        GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
        if (game.isRole(this.player, KinsWatheRoles.ROBOT)) return;

        if (BrinPoisonBridge.isOnlineBartender(game, this.player, resolved)) {
            if (BrinModifiers.hasModifier(this.player, BrinModifiers.FAST2FAST)) {
                BrinNoelleAccess.addBartenderArmor(this.player);
            } else {
                BrinNoelleAccess.giveBartenderArmor(this.player);
            }
            this.reset();
            return;
        }

        PlayerPoisonComponent self = (PlayerPoisonComponent) (Object) this;
        BrinPoisonBridge.setDelusionFlag(self, BrinPoisonBridge.DELUSION_MARKER.equals(resolved));
        BrinPoisonBridge.applyKinsPoisonSideEffects(game, this.player, ticks, resolved);
        this.brinApplyOfficialBody(ticks, resolved);
        BrinsWathe.LOGGER.info(
            "[poison] inner setPoisonTicks chain was swallowed; applied directly target={} ticks={} poisoner={}",
            this.player.getName().getString(), ticks, resolved
        );
    }

    @WrapMethod(method = "reset")
    private void brinReset(Operation<Void> original) {
        BrinPoisonBridge.beginReset();
        original.call();
        if (BrinPoisonBridge.resetBodyReached()) return;

        BrinPoisonBridge.setDelusionFlag((PlayerPoisonComponent) (Object) this, false);
        this.poisonTicks = -1;
        this.poisonPulseCooldown = 0;
        this.initialPoisonTicks = 0;
        this.pulseProgress = 0.0F;
        this.pulsing = false;
        this.sync();
    }

    @Unique
    private void brinApplyOfficialBody(int ticks, UUID resolved) {
        this.poisoner = resolved;
        this.poisonTicks = ticks;
        if (this.initialPoisonTicks == 0) {
            this.initialPoisonTicks = ticks;
        }
        this.sync();
    }
}
