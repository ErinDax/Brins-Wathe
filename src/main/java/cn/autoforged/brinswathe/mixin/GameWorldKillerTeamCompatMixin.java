package cn.autoforged.brinswathe.mixin;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GameWorldComponent.class)
public abstract class GameWorldKillerTeamCompatMixin {
    @Unique
    public boolean isKillerTeam(Player player) {
        return ((GameWorldComponent) (Object) this).canUseKillerFeatures(player);
    }
}
