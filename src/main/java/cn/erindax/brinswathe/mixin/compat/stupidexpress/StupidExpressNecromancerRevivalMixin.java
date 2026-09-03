package cn.erindax.brinswathe.mixin.compat.stupidexpress;

import cn.erindax.brinswathe.entity.ArchivistSealedCorpse;
import cn.erindax.brinswathe.entity.BoneharvestedCorpse;
import cn.erindax.brinswathe.voice.BrinVoiceChatPlugin;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
            InteractionResult result = callback.interact(player, level, hand, entity, hitResult);
            if (result.consumesAction()
                && entity instanceof PlayerBodyEntity body
                && player instanceof ServerPlayer necromancer
                && level instanceof ServerLevel serverLevel) {
                ServerPlayer revived = (ServerPlayer) serverLevel.getPlayerByUUID(body.getPlayerUuid());
                if (revived != null) {
                    MobEffectInstance glow = new MobEffectInstance(MobEffects.GLOWING, 300, 0, false, false);
                    revived.addEffect(glow);
                    necromancer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0, false, false));
                    BrinVoiceChatPlugin.addPlayer(necromancer.getUUID());
                    BrinVoiceChatPlugin.addPlayer(revived.getUUID());
                }
            }
            return result;
        };
    }
}
