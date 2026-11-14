package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.NoelleAccess;
import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.client.gui.FactionScreen;
import cn.erindax.brinswathe.client.gui.SpyGuessScreen;
import cn.erindax.brinswathe.component.ConspiratorComponent;
import cn.erindax.brinswathe.component.CowboyComponent;
import cn.erindax.brinswathe.component.GamblerComponent;
import cn.erindax.brinswathe.component.IllusionistComponent;
import cn.erindax.brinswathe.component.InspectorComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import cn.erindax.brinswathe.component.SniperComponent;
import cn.erindax.brinswathe.component.SpyComponent;
import cn.erindax.brinswathe.component.StalkerComponent;
import cn.erindax.brinswathe.component.SubverterComponent;
import cn.erindax.brinswathe.component.ValentinaComponent;
import cn.erindax.brinswathe.component.ZhuimuComponent;
import cn.erindax.brinswathe.config.Tunable;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.jetbrains.annotations.Nullable;

public final class AbilityKeyClient {
    private AbilityKeyClient() {
    }

    public static void handleAbilityKeyPress() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        PlayerBodyEntity touchedBody = AimClient.getInspectableBodyInCrosshair(player);
        if (touchedBody != null) {
            ClientPlayNetworking.send(new AbilityC2SPacket(
                AbilityC2SPacket.ABILITY_CORPSE_TOUCH,
                touchedBody.getUUID()
            ));
        }
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (gameWorld.isRole(player, StarryExpressRoles.MUZZLER)) {
            ClientPlayNetworking.send(new AbilityC2SPacket(
                AbilityC2SPacket.ABILITY_MUZZLER_SILENCE,
                null
            ));
            return;
        }
        if (GameSafeComponent.KEY.get(player.level()).isGameSafe
            && !ModRoles.ignoresOpeningSafeTime(gameWorld, player)) {
            return;
        }
        if (NoelleAccess.isRole(gameWorld, player, NoelleAccess.CONDUCTOR_ID)) {
            ClientPlayNetworking.send(new AbilityC2SPacket(
                AbilityC2SPacket.ABILITY_CONDUCTOR_ARMOR, null));
            return;
        }
        if (gameWorld.isRole(player, WatheRoles.VIGILANTE)) {
            AbilityPlayerComponent vigilanteAbility = AbilityPlayerComponent.KEY.get(player);
            if (vigilanteAbility != null && vigilanteAbility.cooldown > 0) return;
            UUID targetId = AimClient.findPlayerInCrosshair(player);
            if (targetId != null) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_VIGILANTE_DETECT, targetId));
            }
            return;
        }
        if (NoelleAccess.isRole(gameWorld, player, NoelleAccess.NOISEMAKER_ID)) {
            if (NoelleAccess.noelleAbilityCooldown(player) > 0) return;
            UUID targetId = AimClient.findAimedAnyPlayer(player);
            if (targetId != null) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_NOISEMAKER_MARK, targetId));
            }
            return;
        }
        if (NoelleAccess.isRole(gameWorld, player, NoelleAccess.INSANE_KILLER_ID)) {
            if (NoelleAccess.noelleAbilityCooldown(player) > 0) return;
            UUID targetId = AimClient.findAimedAnyPlayer(player);
            if (targetId != null) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_INSANE_REVIVE, targetId));
            }
            return;
        }
        AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
        if (gameWorld.isRole(player, ModRoles.SNIPER)) {
            SniperComponent component = SniperComponent.KEY.get(player);
            if (component != null && (component.isAiming() || component.getCooldownTicks() <= 0)) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_SNIPER_TOGGLE_OR_FIRE,
                    null
                ));
            }
            return;
        }
        if (gameWorld.isRole(player, ModRoles.PUPPETEER)) {
            PuppeteerControlComponent component = PuppeteerControlComponent.KEY.get(player);
            if (component == null) return;
            if (component.isControlling()) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_PUPPETEER_RETURN,
                    null
                ));
                return;
            }
            if (component.craftCooldownTicks > 0) return;
            UUID corpseId = AimClient.findPlayerBodyInCrosshair(player);
            if (corpseId != null) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_PUPPETEER_CRAFT,
                    corpseId
                ));
            }
            return;
        }
        if (gameWorld.isRole(player, ModRoles.ILLUSIONIST)) {
            IllusionistComponent component = IllusionistComponent.KEY.get(player);
            if (component != null && !component.cloneEntityIds.isEmpty()) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_ILLUSIONIST_SWITCH_CONTROL,
                    getNextControlTarget(component)
                ));
            } else if (ability.cooldown <= 0) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_ILLUSIONIST_CLONES,
                    null
                ));
            }
            return;
        }
        if (gameWorld.isRole(player, ModRoles.CONSPIRATOR)) {
            ConspiratorComponent channel = ConspiratorComponent.KEY.get(player);
            if (GameFunctions.isPlayerAliveAndSurvival(player)
                && ability.cooldown <= 0
                && (channel == null || !channel.isInTemporaryChannel())) {
                Minecraft.getInstance().setScreen(new LimitedInventoryScreen(player));
            }
            return;
        }
        if (gameWorld.isRole(player, ModRoles.COWBOY)) {
            CowboyComponent cowboy = CowboyComponent.KEY.get(player);
            if (GameFunctions.isPlayerAliveAndSurvival(player)
                && cowboy != null
                && !cowboy.duelUsed()) {
                Minecraft.getInstance().setScreen(new LimitedInventoryScreen(player));
            }
            return;
        }
        if (gameWorld.isRole(player, ModRoles.STALKER)) {
            StalkerComponent stalker = StalkerComponent.KEY.get(player);
            if (stalker == null || stalker.cooldownTicks() > 0) return;
            UUID targetId = AimClient.findStalkerAimTarget(player);
            if (targetId != null) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_STALKER_TRACK, targetId));
            }
            return;
        }
        if (gameWorld.isRole(player, ModRoles.VALENTINA)) {
            ValentinaComponent valentina = ValentinaComponent.KEY.get(player);
            if (valentina != null && valentina.canForesee()) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_VALENTINA_FORESIGHT, null));
            }
            return;
        }
        if (gameWorld.isRole(player, ModRoles.SUBVERTER)) {
            SubverterComponent subverter = SubverterComponent.KEY.get(player);
            UUID targetId = subverter != null && subverter.isPending() ? AimClient.findPlayerInCrosshair(player) : null;
            Player target = targetId == null ? null : player.level().getPlayerByUUID(targetId);
            if (target != null) {
                Minecraft.getInstance().setScreen(FactionScreen.subvert(targetId, target.getDisplayName()));
            }
            return;
        }
        if (gameWorld.isRole(player, ModRoles.INSPECTOR)) {
            InspectorComponent inspector = InspectorComponent.KEY.get(player);
            if (GameFunctions.isPlayerAliveAndSurvival(player) && inspector != null && !inspector.used()) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_INSPECTOR_DISARM, null));
            }
            return;
        }
        if (gameWorld.isRole(player, ModRoles.ZHUIMU)) {
            ZhuimuComponent zhuimu = ZhuimuComponent.KEY.get(player);
            if (GameFunctions.isPlayerAliveAndSurvival(player) && zhuimu != null && !zhuimu.used()) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_ZHUIMU_BOAT, null));
            }
            return;
        }
        if (gameWorld.isRole(player, ModRoles.SPY)) {
            SpyComponent spy = SpyComponent.KEY.get(player);
            if (GameFunctions.isPlayerAliveAndSurvival(player)
                && ability.cooldown <= 0
                && spy != null
                && spy.required() > 0
                && !spy.completed()) {
                Minecraft.getInstance().setScreen(new SpyGuessScreen());
            }
            return;
        }
        if (gameWorld.isRole(player, ModRoles.GRAPPLER)) {
            if (!GameFunctions.isPlayerAliveAndSurvival(player) || ability.cooldown > 0) return;
            UUID targetId = AimClient.findGrapplerTarget(player);
            if (targetId != null) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_GRAPPLER_HOOK, targetId));
            } else {
                player.displayClientMessage(
                    Component.translatable(
                        "message.brinswathe.grappler.no_target",
                        Tunable.GRAPPLER_HOOK_RANGE.intValue()
                    ),
                    true
                );
            }
            return;
        }
        if (ability.cooldown > 0) return;
        if (gameWorld.isRole(player, ModRoles.TRAPPER)) {
            ClientPlayNetworking.send(new AbilityC2SPacket(AbilityC2SPacket.ABILITY_TRAPPER_PLACE_TRAP, null));
        } else if (gameWorld.isRole(player, ModRoles.WATCHMAN)) {
            UUID targetId = AimClient.findWatchmanTarget(player);
            if (targetId != null) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_WATCHMAN_RESCUE, targetId));
            }
        } else if (gameWorld.isRole(player, ModRoles.NIGHTMARE)) {
            UUID targetId = AimClient.findSleepingPlayerInCrosshair(player);
            if (targetId != null) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_NIGHTMARE_PLANT, targetId));
            }
        } else if (gameWorld.isRole(player, ModRoles.ARCHIVIST)) {
            PlayerBodyEntity corpse = AimClient.getInspectableBodyInCrosshair(player);
            if (corpse != null) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_ARCHIVIST_SEAL, corpse.getUUID()));
            }
        } else if (gameWorld.isRole(player, ModRoles.GAMBLER)) {
            GamblerComponent gambler = GamblerComponent.KEY.get(player);
            UUID targetId = gambler != null && gambler.betTarget() == null ? AimClient.findPlayerInCrosshair(player) : null;
            if (targetId != null) {
                ClientPlayNetworking.send(new AbilityC2SPacket(
                    AbilityC2SPacket.ABILITY_GAMBLER_BET, targetId));
            }
        } else if (gameWorld.isRole(player, ModRoles.ZHANGSHI)) {
            ClientPlayNetworking.send(new AbilityC2SPacket(
                AbilityC2SPacket.ABILITY_ZHANGSHI_SPEED, null));
        } else if (gameWorld.isRole(player, ModRoles.MEDIUM)) {
            ClientPlayNetworking.send(new AbilityC2SPacket(
                AbilityC2SPacket.ABILITY_MEDIUM_JOIN_VOICE, null));
        } else if (gameWorld.isRole(player, ModRoles.HALF_SPIRIT)) {
            ClientPlayNetworking.send(new AbilityC2SPacket(
                AbilityC2SPacket.ABILITY_HALF_SPIRIT_PHASE, null));
        }
    }

    @Nullable
    private static UUID getNextControlTarget(IllusionistComponent component) {
        if (component.controlledCloneId == null) return component.cloneEntityIds.getFirst();
        int currentIndex = component.cloneEntityIds.indexOf(component.controlledCloneId);
        if (currentIndex < 0 || currentIndex + 1 >= component.cloneEntityIds.size()) return null;
        return component.cloneEntityIds.get(currentIndex + 1);
    }
}
