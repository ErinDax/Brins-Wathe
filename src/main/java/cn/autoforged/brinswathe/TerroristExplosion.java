package cn.autoforged.brinswathe;

import cn.autoforged.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheParticles;
import dev.doctor4t.wathe.index.WatheSounds;
import java.util.List;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class TerroristExplosion {
    private TerroristExplosion() {
    }

    public static boolean allowGunDeath(Player victim, Player attacker, ResourceLocation deathReason) {
        if (!GameConstants.DeathReasons.GUN.equals(deathReason)
            || !(victim instanceof ServerPlayer terrorist)
            || !(attacker instanceof ServerPlayer)
            || attacker == victim
            || !GameFunctions.isPlayerAliveAndSurvival(terrorist)) {
            return true;
        }

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(terrorist.level());
        if (!gameWorld.isRole(terrorist, BrinRoles.TERRORIST)) return true;

        int killedPlayers = explode(terrorist);
        return killedPlayers < BrinConfig.terroristExplosionSurvivalKills();
    }

    /**
     * Anything that kills a terrorist outside of a gunshot still has to set off the payload they are
     * carrying, so the same blast is reused rather than duplicated per killer.
     */
    public static void triggerChainExplosion(ServerPlayer victim) {
        if (!GameWorldComponent.KEY.get(victim.level()).isRole(victim, BrinRoles.TERRORIST)) return;
        explode(victim);
    }

    private static int explode(ServerPlayer terrorist) {
        ServerLevel level = terrorist.serverLevel();
        playExplosionEffects(level, terrorist);

        AABB bounds = terrorist.getBoundingBox().inflate(BrinConfig.terroristExplosionRange());
        List<ServerPlayer> targets = level.getPlayers(target ->
            target != terrorist
                && GameFunctions.isPlayerAliveAndSurvival(target)
                && bounds.contains(target.position())
        );

        int killedPlayers = 0;
        for (ServerPlayer target : targets) {
            boolean wasEliminated = GameFunctions.isPlayerEliminated(target);
            GameFunctions.killPlayer(target, true, terrorist, GameConstants.DeathReasons.GRENADE);
            if (!wasEliminated && GameFunctions.isPlayerEliminated(target)) killedPlayers++;
        }
        return killedPlayers;
    }

    public static void playExplosionEffects(ServerLevel level, ServerPlayer terrorist) {
        float pitch = 1.0F + terrorist.getRandom().nextFloat() * 0.1F - 0.05F;
        level.playSound(
            null,
            terrorist.blockPosition(),
            WatheSounds.ITEM_GRENADE_EXPLODE,
            SoundSource.PLAYERS,
            5.0F,
            pitch
        );

        double x = terrorist.getX();
        double y = terrorist.getY() + 0.1D;
        double z = terrorist.getZ();
        level.sendParticles(WatheParticles.BIG_EXPLOSION, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.SMOKE, x, y, z, 100, 0.0D, 0.0D, 0.0D, 0.2D);
        level.sendParticles(
            new ItemParticleOption(ParticleTypes.ITEM, WatheItems.THROWN_GRENADE.getDefaultInstance()),
            x,
            y,
            z,
            100,
            0.0D,
            0.0D,
            0.0D,
            1.0D
        );
    }
}
