package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.BrinSecretMurder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.MoodRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(MoodRenderer.class)
public abstract class BrinSecretMoodMixin {
    @WrapOperation(
        method = "renderHud",
        at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;getGameMode()Ldev/doctor4t/wathe/api/GameMode;")
    )
    private static GameMode brinShowMoodInSecretMurder(GameWorldComponent game, Operation<GameMode> original) {
        GameMode mode = original.call(game);
        return mode == BrinSecretMurder.MODE ? WatheGameModes.MURDER : mode;
    }
}
