package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.entity.ArchivistSealedCorpse;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.world.entity.player.Player;
import org.BsXinQin.kinswathe.packet.roles.BodymakerC2SPacket;
import org.BsXinQin.kinswathe.roles.bodymaker.BodymakerAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BodymakerAbility.class)
public abstract class ArchivistForgedCorpseMixin {
    @Inject(
        method = "register",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
                shift = At.Shift.BEFORE
        )
    )
    private static void brinMarkForgedCorpse(
        BodymakerC2SPacket payload,
        Player bodymaker,
        CallbackInfo ci,
        @Local(name = "playerBody") PlayerBodyEntity body
    ) {
        ((ArchivistSealedCorpse) body).brin$setBodymakerForged(true);
    }
}
