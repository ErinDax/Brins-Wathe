package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.IllusionistComponent;
import cn.erindax.brinswathe.component.MorticianComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;

public final class Puppeteer {
    private Puppeteer() {
    }

    public static void handleCraft(ServerPlayer player, GameWorldComponent gameWorld,
                                   AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, ModRoles.PUPPETEER)) return;
        if (payload.targetId() == null) return;
        PuppeteerControlComponent comp = PuppeteerControlComponent.KEY.get(player);
        if (comp == null || comp.isControlling() || comp.craftCooldownTicks > 0) return;
        Entity targetEntity = player.serverLevel().getEntity(payload.targetId());
        if (!(targetEntity instanceof PlayerBodyEntity body)) return;
        if (PuppeteerControlComponent.isPuppetModel(body)
            || IllusionistComponent.isIllusionModel(body)
            || MorticianComponent.isDisguiseBody(body)) return;
        if (player.distanceToSqr(body) > 64.0D) return;
        if (!AbilityCost.spend(player, "puppeteer")) return;
        if (!comp.craftPuppet(body)) return;
        player.playNotifySound(SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    public static void handleSummon(ServerPlayer player, GameWorldComponent gameWorld,
                                    AbilityPlayerComponent ability,
                                    AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, ModRoles.PUPPETEER)) return;
        if (ability.cooldown > 0) return;
        if (payload.targetId() == null) return;

        PuppeteerControlComponent comp = PuppeteerControlComponent.KEY.get(player);
        if (comp == null || !comp.summonPuppet(player, payload.targetId())) return;
        player.playNotifySound(SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 0.5f);
    }

    public static void handleReturn(ServerPlayer player, GameWorldComponent gameWorld) {
        if (!gameWorld.isRole(player, ModRoles.PUPPETEER)) return;
        PuppeteerControlComponent comp = PuppeteerControlComponent.KEY.get(player);
        if (comp == null) return;
        comp.returnToBody(player);
    }
}
