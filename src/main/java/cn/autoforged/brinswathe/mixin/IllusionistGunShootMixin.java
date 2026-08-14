package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.component.IllusionistComponent;
import cn.autoforged.brinswathe.component.PuppeteerControlComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.util.GunShootPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GunShootPayload.Receiver.class)
public abstract class IllusionistGunShootMixin {
    private static final ResourceLocation FAKE_REVOLVER_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "fake_revolver");

    @Redirect(
        method = "receive(Ldev/doctor4t/wathe/util/GunShootPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getEntity(I)Lnet/minecraft/world/entity/Entity;"
        )
    )
    private Entity brinHandleIllusionModelShot(ServerLevel level, int entityId,
                                                GunShootPayload payload,
                                                ServerPlayNetworking.Context context) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(context.player().getMainHandItem().getItem());
        if (FAKE_REVOLVER_ID.equals(itemId)) return null;
        Entity target = level.getEntity(entityId);
        if (!(target instanceof PlayerBodyEntity body)) return target;
        // Only one redirect may own this lookup, so the puppeteer's models are resolved here as well.
        if (PuppeteerControlComponent.resolveWeaponHit(body, context.player(), GameConstants.DeathReasons.GUN)) {
            return null;
        }
        boolean clone = IllusionistComponent.isClone(body);
        boolean bodyProxy = IllusionistComponent.isBodyProxy(body);
        if (!clone && !bodyProxy) return target;

        Player owner = body.level().getPlayerByUUID(body.getPlayerUuid());
        if (owner == null) {
            body.discard();
            return null;
        }

        ServerPlayer shooter = context.player();
        IllusionistComponent component = IllusionistComponent.KEY.get(owner);
        if (clone) {
            IllusionistComponent.applyCloneKillBlindness(shooter);
            component.handleCloneKilled(body);
        } else {
            component.endSkill();
            GameFunctions.killPlayer(owner, true, shooter, GameConstants.DeathReasons.GUN);
        }
        return null;
    }
}
