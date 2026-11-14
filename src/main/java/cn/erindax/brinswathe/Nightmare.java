package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.NightmareComponent;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.config.Tunable;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;

public final class Nightmare {
    private Nightmare() {
    }

    public static void handlePlant(ServerPlayer player, GameWorldComponent gameWorld,
                                   AbilityPlayerComponent ability,
                                   AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, ModRoles.NIGHTMARE)) return;
        if (ability.cooldown > 0) return;
        if (payload.targetId() == null) return;

        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null || !target.isAlive()) return;
        if (player.equals(target)) return;
        if (!target.isSleeping()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
        NightmareComponent comp = NightmareComponent.KEY.get(player);
        if (comp == null) return;
        if (comp.isMarked(target.getUUID())) return;
        if (!AbilityCost.spend(player, "nightmare")) return;
        comp.markPlayer(target.getUUID());
        comp.addShieldLayer();
        PlayerMoodComponent moodComponent = PlayerMoodComponent.KEY.get(target);
        moodComponent.setMood(0.19F);
        ability.setAbilityCooldown(ModConfig.skillCooldownSeconds("nightmare"));
        player.playNotifySound(SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 1.0f, 0.5f);
        player.displayClientMessage(
            Component.literal("已播种 " + target.getName().getString() + "  护盾 " + comp.getShieldLayers() + "/" + Tunable.NIGHTMARE_MAX_SHIELD_LAYERS.intValue()),
            true);
    }

    public static void handleForceSleep(ServerPlayer player, GameWorldComponent gameWorld,
                                        AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, ModRoles.NIGHTMARE)) return;
        if (payload.targetId() == null) return;
        NightmareComponent nightmare = NightmareComponent.KEY.get(player);
        if (nightmare == null || nightmare.forcedSleepTaskCooldown > 0) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null || target.equals(player) || target.level() != player.level()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
        PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(target);
        if (mood == null) return;
        mood.tasks.clear();
        mood.tasks.put(
            PlayerMoodComponent.Task.SLEEP,
            new PlayerMoodComponent.SleepTask(GameConstants.SLEEP_TASK_DURATION)
        );
        mood.sync();
        nightmare.startForcedSleepTaskCooldown();
    }
}
