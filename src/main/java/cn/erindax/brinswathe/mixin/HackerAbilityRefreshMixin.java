package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinAbilityCooldowns;
import cn.erindax.brinswathe.BrinRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.index.WatheItems;
import java.awt.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.roles.hacker.HackerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HackerComponent.class, remap = false)
public abstract class HackerAbilityRefreshMixin {
    @Inject(method = "refreshAbilityCooldown", at = @At("RETURN"), remap = false)
    private static void brinRefreshKillerAbilityCooldowns(Player buyer, CallbackInfo ci) {
        if (buyer.getServer() == null) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(buyer.level());
        for (ServerPlayer player : buyer.getServer().getPlayerList().getPlayers()) {
            if (player == null) continue;
            boolean killerFeatures = game.canUseKillerFeatures(player);
            boolean brinKillerWithoutFeatures = game.isRole(player, BrinRoles.NIGHTMARE)
                || game.isRole(player, BrinRoles.BERSERKER);
            if (!killerFeatures && !brinKillerWithoutFeatures) continue;

            if (brinKillerWithoutFeatures) {
                AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
                if (ability != null) ability.setAbilityCooldown(0);
                player.getCooldowns().addCooldown(WatheItems.PSYCHO_MODE, 0);
                player.displayClientMessage(
                    Component.translatable("tip.kinswathe.hacker.ability_cooldown_refresh")
                        .withColor(Color.GREEN.getRGB()),
                    true
                );
                player.playNotifySound(SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            BrinAbilityCooldowns.refreshKillerAbilityCooldowns(player);
        }
    }
}
