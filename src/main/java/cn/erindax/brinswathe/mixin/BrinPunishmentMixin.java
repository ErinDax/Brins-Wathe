package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinPunishment;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModdedMurderGameMode.class)
public abstract class BrinPunishmentMixin {
    @Inject(method = "initializeGame", at = @At("HEAD"))
    private void brinApplyPunishments(
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci
    ) {
        BrinPunishment.applyQueued(players);
    }
}
