package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.ArchivistComponent;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.entity.ArchivistSealedCorpse;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;

public final class Archivist {
    private Archivist() {
    }

    public static void handleSeal(ServerPlayer player, GameWorldComponent gameWorld,
                                  AbilityPlayerComponent ability,
                                  AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, ModRoles.ARCHIVIST)) return;
        if (ability.cooldown > 0) return;
        if (payload.targetId() == null) return;

        Entity targetEntity = player.serverLevel().getEntity(payload.targetId());
        if (!(targetEntity instanceof PlayerBodyEntity body)) return;
        if (player.distanceToSqr(body) > 64.0D) return;
        if (((ArchivistSealedCorpse) body).brin$isArchivistSealed()) return;
        if (!AbilityCost.spend(player, "archivist")) return;
        ((ArchivistSealedCorpse) body).brin$setArchivistSealed(true);
        ability.setAbilityCooldown(ModConfig.skillCooldownSeconds("archivist"));
        ArchivistComponent comp = ArchivistComponent.KEY.get(player);
        if (comp != null) {
            comp.sealedCorpses.add(body.getUUID());
            comp.sync();
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.5f);
    }
}
