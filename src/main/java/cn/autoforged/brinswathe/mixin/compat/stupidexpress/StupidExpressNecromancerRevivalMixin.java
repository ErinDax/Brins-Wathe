package cn.autoforged.brinswathe.mixin.compat.stupidexpress;

import cn.autoforged.brinswathe.entity.ArchivistSealedCorpse;
import cn.autoforged.brinswathe.entity.BoneharvestedCorpse;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.world.InteractionResult;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "pro.fazeclan.river.stupid_express.role.necromancer.RevivalSelectionHandler")
public abstract class StupidExpressNecromancerRevivalMixin {
    @ModifyArg(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/fabricmc/fabric/api/event/Event;register(Ljava/lang/Object;)V"
        ),
        index = 0,
        require = 0
    )
    private static Object brinPreventProtectedCorpseRevival(Object listener) {
        if (!(listener instanceof UseEntityCallback callback)) return listener;
        return (UseEntityCallback) (player, level, hand, entity, hitResult) -> {
            var role = GameWorldComponent.KEY.get(level).getRole(player);
            if (role != null
                && "stupid_express".equals(role.identifier().getNamespace())
                && "necromancer".equals(role.identifier().getPath())
                && entity instanceof PlayerBodyEntity body
                && (((ArchivistSealedCorpse) body).brin$isArchivistSealed()
                    || ((BoneharvestedCorpse) body).brin$isBoneharvested())) {
                return InteractionResult.FAIL;
            }
            return callback.interact(player, level, hand, entity, hitResult);
        };
    }
}
