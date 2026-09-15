package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinKnifeSkins;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.index.WatheCosmetics;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.util.GunShootPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GunShootPayload.Receiver.class)
public abstract class BrinGunShootSoundMixin {
    @WrapOperation(
        method = "receive(Ldev/doctor4t/wathe/util/GunShootPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
        )
    )
    private void brinPlaySkinShootSound(
        Level level,
        Player except,
        double x,
        double y,
        double z,
        SoundEvent sound,
        SoundSource source,
        float volume,
        float pitch,
        Operation<Void> original,
        GunShootPayload payload,
        ServerPlayNetworking.Context context
    ) {
        if (sound == WatheSounds.ITEM_REVOLVER_SHOOT) {
            ServerPlayer shooter = context.player();
            ItemStack stack = shooter.getMainHandItem();
            String skin = BrinKnifeSkins.resolveGunSkinName(WatheCosmetics.getSkin(stack));
            if (BrinKnifeSkins.hasSound("gun", skin)) {
                BrinKnifeSkins.broadcastSound(shooter, "gun", skin, volume, pitch);
                return;
            }
        }
        original.call(level, except, x, y, z, sound, source, volume, pitch);
    }
}
