package cn.erindax.brinswathe.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameFunctions.class)
public abstract class BrinCorpseSpawnMixin {
    @WrapOperation(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
        )
    )
    private static boolean brinSpawnCorpseAtFeet(
        Level world,
        Entity entity,
        Operation<Boolean> original,
        Player victim,
        boolean spawnBody,
        Player killer,
        ResourceLocation deathReason
    ) {
        if (entity instanceof PlayerBodyEntity) {
            entity.setPos(victim.getX(), victim.getY(), victim.getZ());
            entity.setYRot(victim.getYHeadRot());
            entity.setYHeadRot(victim.getYHeadRot());
        }
        return original.call(world, entity);
    }
}
