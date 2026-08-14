package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.BrinModifiers;
import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.client.BrinsWatheClient;
import cn.autoforged.brinswathe.component.AvengerComponent;
import cn.autoforged.brinswathe.component.BerserkerComponent;
import cn.autoforged.brinswathe.component.BombComponent;
import cn.autoforged.brinswathe.component.BoneharvesterComponent;
import cn.autoforged.brinswathe.component.GamblerComponent;
import cn.autoforged.brinswathe.component.NightmareComponent;
import cn.autoforged.brinswathe.component.PenitentComponent;
import cn.autoforged.brinswathe.component.SniperComponent;
import cn.autoforged.brinswathe.component.TrapperComponent;
import cn.autoforged.brinswathe.component.ZhangshiComponent;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;

import java.lang.reflect.Field;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.ladysnake.cca.api.v3.component.ComponentKey;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = WatheClient.class, priority = 500)
public abstract class HideInstinctMixin {
    @Unique
    private static ComponentKey<?> brinShieldComponentKey;
    @Unique
    private static Field brinShieldArmorField;

    @Unique
    private static final int NIGHTMARE_OUTLINE = 0x4B0082;
    @Unique
    private static final int MARKED_OUTLINE = 0x000000;
    @Unique
    private static final int LOW_MOOD_OUTLINE = 0x171DC6;
    @Unique
    private static final int MEDIUM_MOOD_OUTLINE = 0x1FAFAF;
    @Unique
    private static final int HIGH_MOOD_OUTLINE = 0x4EDD35;
    @Unique
    private static final int NEUTRAL_OUTLINE = 0x808080;
    @Unique
    private static final int KILLER_OUTLINE = 0x990000;
    @Unique
    private static final int SHIELD_OUTLINE = 0x0000FF;
    @Unique
    private static final int BOMB_OUTLINE = 0xFF8C00;
    @Unique
    private static final int AVENGER_OUTLINE = 0xFFFFFF;

    @Inject(method = "isPlayerSpectatingOrCreative", at = @At("HEAD"), cancellable = true, remap = false)
    private static void brinHideCowboySpectatorRoleHud(CallbackInfoReturnable<Boolean> cir) {
        if (BrinsWatheClient.isSuppressingSpectatorRoleHud()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true, remap = false)
    private static void brinEnableRoleInstinct(CallbackInfoReturnable<Boolean> cir) {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null || WatheClient.instinctKeybind == null) return;
        if (BrinsWatheClient.shouldHideSpectatorIdentities()) {
            cir.setReturnValue(false);
            return;
        }

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(localPlayer.level());
        var role = gameWorld.getRole(localPlayer);
        SniperComponent sniper = SniperComponent.KEY.get(localPlayer);
        if (role != null
            && "brin".equals(role.identifier().getNamespace())
            && gameWorld.isRole(localPlayer, BrinRoles.SNIPER)
            && WatheClient.isPlayerAliveAndInSurvival()
            && sniper != null
            && sniper.isAbilityActive()) {
            cir.setReturnValue(false);
            return;
        }

        if (WatheClient.isPlayerAliveAndInSurvival()) {
            // The bartender's own sightings are HEAD injections on getInstinctHighlight and the outline
            // renderer never consults this flag, so they survive with instinct switched off. Holding the
            // key would only have added wathe's night vision and its killer highlighting on top.
            if (brinIsBartender(gameWorld, localPlayer)) {
                cir.setReturnValue(false);
                return;
            }
            // The sleep task is the one moment a shielded player is shown his own body and cannot hold a
            // key, so instinct is forced on only there. Leaving it on while awake would hand him the
            // night vision that comes with it. Checked before the avenger branch so an avenger shielded
            // by the bartender still sees his own outline while asleep.
            if (localPlayer.isSleeping() && brinShieldLayers(localPlayer) > 0) {
                cir.setReturnValue(true);
                return;
            }
            // The avenger has no instinct key of his own; the only instinct he ever gets is the automatic
            // burst that follows a witnessed kill.
            if (gameWorld.isRole(localPlayer, BrinRoles.AVENGER)) {
                AvengerComponent avenger = AvengerComponent.KEY.get(localPlayer);
                cir.setReturnValue(avenger != null && avenger.instinctTicks() > 0);
                return;
            }
        }

        if (role == null || !"brin".equals(role.identifier().getNamespace())) return;

        boolean spectatorInstinct = GameFunctions.isPlayerSpectatingOrCreative(localPlayer);
        BerserkerComponent berserker = BerserkerComponent.KEY.get(localPlayer);
        boolean livingInstinct = gameWorld.canUseKillerFeatures(localPlayer)
            || gameWorld.isRole(localPlayer, BrinRoles.PENITENT)
            || (gameWorld.isRole(localPlayer, BrinRoles.BERSERKER)
                && berserker != null && berserker.psychoActive);
        cir.setReturnValue(
            WatheClient.instinctKeybind.isDown()
                && (spectatorInstinct
                    || (livingInstinct && WatheClient.isPlayerAliveAndInSurvival()))
        );
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true, remap = false)
    private static void brinInstinctHighlight(Entity target, CallbackInfoReturnable<Integer> cir) {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(localPlayer.level());
        if (GameFunctions.isPlayerSpectatingOrCreative(localPlayer)) {
            if (BrinsWatheClient.isCowboyDuelHidingIdentities()) cir.setReturnValue(-1);
            return;
        }

        // Shields are drawn from the RETURN handler so that a role which claims the same target - the
        // bartender's drinkers, the physician's poisoned - keeps its own colour.

        // Noell's stealth ("鬼祟") hides its carrier from instinct sight inside ten blocks, but its
        // handler runs after ours - so without this hand-off a brin viewer's branches below would paint
        // the carrier anyway. A plain return keeps noell's own precedence intact.
        if (target instanceof Player stealthTarget
            && stealthTarget != localPlayer
            && stealthTarget.distanceTo(localPlayer) < 10.0F
            && BrinModifiers.hasModifier(stealthTarget, BrinModifiers.STEALTH)) {
            return;
        }

        if (gameWorld.isRole(localPlayer, BrinRoles.NIGHTMARE)) {
            if (!(target instanceof Player targetPlayer)
                || targetPlayer == localPlayer
                || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                cir.setReturnValue(-1);
                return;
            }

            NightmareComponent nightmareComponent = NightmareComponent.KEY.get(localPlayer);
            if (nightmareComponent != null && nightmareComponent.isMarked(targetPlayer.getUUID())) {
                cir.setReturnValue(MARKED_OUTLINE);
                return;
            }
            if (targetPlayer.isSleeping()) {
                cir.setReturnValue(NIGHTMARE_OUTLINE);
                return;
            }
            cir.setReturnValue(-1);
            return;
        }

        if (gameWorld.isRole(localPlayer, BrinRoles.GAMBLER)) {
            if (target instanceof Player targetPlayer
                && targetPlayer != localPlayer
                && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                GamblerComponent gamblerComponent = GamblerComponent.KEY.get(localPlayer);
                if (gamblerComponent != null
                    && targetPlayer.getUUID().equals(gamblerComponent.betTarget)) {
                    cir.setReturnValue(0xFFD700);
                    return;
                }
            }
            cir.setReturnValue(-1);
            return;
        }

        if (gameWorld.isRole(localPlayer, BrinRoles.PENITENT)) {
            if (!(target instanceof Player targetPlayer)
                || targetPlayer == localPlayer
                || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                return;
            }
            if (!WatheClient.isInstinctEnabled()) {
                cir.setReturnValue(-1);
                return;
            }
            if (gameWorld.canUseKillerFeatures(targetPlayer)) {
                cir.setReturnValue(KILLER_OUTLINE);
                return;
            }
            if (gameWorld.isRole(targetPlayer, BrinRoles.NIGHTMARE)) {
                cir.setReturnValue(NIGHTMARE_OUTLINE);
                return;
            }
            if (gameWorld.isInnocent(targetPlayer)) {
                cir.setReturnValue(brinMoodOutline(targetPlayer));
                return;
            }
            cir.setReturnValue(-1);
            return;
        }

        if (gameWorld.isRole(localPlayer, BrinRoles.BERSERKER)) {
            BerserkerComponent berserker = BerserkerComponent.KEY.get(localPlayer);
            if (berserker == null || !berserker.psychoActive) return;
            if (!(target instanceof Player targetPlayer)
                || targetPlayer == localPlayer
                || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                return;
            }
            if (!WatheClient.isInstinctEnabled()) {
                cir.setReturnValue(-1);
                return;
            }
            if (gameWorld.canUseKillerFeatures(targetPlayer)) {
                cir.setReturnValue(KILLER_OUTLINE);
                return;
            }
            if (gameWorld.isRole(targetPlayer, BrinRoles.NIGHTMARE)) {
                cir.setReturnValue(NIGHTMARE_OUTLINE);
                return;
            }
            if (gameWorld.isInnocent(targetPlayer)) {
                cir.setReturnValue(brinMoodOutline(targetPlayer));
                return;
            }
            cir.setReturnValue(-1);
            return;
        }

        if (gameWorld.isRole(localPlayer, BrinRoles.AVENGER)) {
            AvengerComponent avengerComponent = AvengerComponent.KEY.get(localPlayer);
            if (avengerComponent == null || avengerComponent.instinctTicks() <= 0
                || !(target instanceof Player targetPlayer)
                || targetPlayer == localPlayer
                || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                cir.setReturnValue(-1);
                return;
            }
            // The burst shows where everyone is, not who they are: one colour for every living player,
            // killers included.
            cir.setReturnValue(AVENGER_OUTLINE);
            return;
        }

        if (gameWorld.isRole(localPlayer, BrinRoles.WATCHMAN)) {
            if (target instanceof Player targetPlayer
                && targetPlayer != localPlayer
                && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                if (brinIsTrapped(localPlayer, targetPlayer.getUUID())) {
                    cir.setReturnValue(0x00AAFF);
                    return;
                }
                BombComponent bomb = BombComponent.KEY.get(targetPlayer);
                if (bomb != null && bomb.canBeDefused()) {
                    cir.setReturnValue(BOMB_OUTLINE);
                    return;
                }
            }
            cir.setReturnValue(-1);
            return;
        }

        if (target instanceof Player targetPlayer) {
            if (gameWorld.canUseKillerFeatures(localPlayer)) {
                for (Player killer : localPlayer.level().players()) {
                    if (!gameWorld.isRole(killer, BrinRoles.TRAPPER)) continue;
                    TrapperComponent trapperComponent = TrapperComponent.KEY.get(killer);
                    if (trapperComponent != null && trapperComponent.isTrapped(targetPlayer.getUUID())) {
                        cir.setReturnValue(0xFF0000);
                        return;
                    }
                }
            }

            if (gameWorld.canUseKillerFeatures(localPlayer)
                && gameWorld.isRole(targetPlayer, BrinRoles.PENITENT)
                && WatheClient.isInstinctEnabled()) {
                cir.setReturnValue(KILLER_OUTLINE);
                return;
            }

            if (WatheClient.isKiller()
                && gameWorld.isRole(targetPlayer, BrinRoles.NIGHTMARE)
                && WatheClient.isInstinctEnabled()) {
                cir.setReturnValue(NIGHTMARE_OUTLINE);
                return;
            }

            if (WatheClient.isKiller()
                && gameWorld.isRole(targetPlayer, BrinRoles.BERSERKER)
                && WatheClient.isInstinctEnabled()) {
                cir.setReturnValue(HIGH_MOOD_OUTLINE);
                return;
            }

            if (WatheClient.isKiller()
                && gameWorld.isRole(targetPlayer, BrinRoles.TERRORIST)
                && WatheClient.isInstinctEnabled()) {
                cir.setReturnValue(BrinRoles.TERRORIST.color());
                return;
            }
        }
    }

    /**
     * Every restriction that is about what the viewer may <em>not</em> see belongs here rather than at
     * HEAD: a role mod that claims a target sets its colour at HEAD and ends the method, so its own
     * vision - the physician's poisoned players, the bartender's drinkers - never reaches this point.
     * Only targets nobody claimed fall through, and those are the ones that would leak wathe's default
     * killer highlighting.
     */
    @Inject(method = "getInstinctHighlight", at = @At("RETURN"), cancellable = true, remap = false)
    private static void brinFinalizeHighlight(Entity target, CallbackInfoReturnable<Integer> cir) {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;
        if (BrinsWatheClient.shouldHideSpectatorIdentities()) {
            cir.setReturnValue(-1);
            return;
        }
        if (GameFunctions.isPlayerSpectatingOrCreative(localPlayer)) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(localPlayer.level());
        if (target instanceof Player targetPlayer
            && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)
            && brinCanSeeShieldOf(gameWorld, localPlayer, targetPlayer)
            && brinShieldLayers(targetPlayer) > 0) {
            cir.setReturnValue(SHIELD_OUTLINE);
            return;
        }

        if (brinIsBartender(gameWorld, localPlayer)) {
            cir.setReturnValue(-1);
            return;
        }

        // Instinct is forced on for anyone wearing a shield so they can see their own outline while the
        // sleep task draws them; that must not turn into a free set of killer eyes.
        var localRole = gameWorld.getRole(localPlayer);
        if (brinShieldLayers(localPlayer) > 0
            && !gameWorld.canUseKillerFeatures(localPlayer)
            && (localRole == null || !"brin".equals(localRole.identifier().getNamespace()))) {
            cir.setReturnValue(-1);
        }
    }

    /**
     * A shield is only ever revealed to its owner and to the bartender, and the bartender is limited to
     * innocents so that neutral and killer shields stay hidden.
     */
    @Unique
    private static boolean brinCanSeeShieldOf(GameWorldComponent gameWorld, Player viewer, Player target) {
        if (target == viewer) return true;
        return brinIsBartender(gameWorld, viewer) && gameWorld.isInnocent(target);
    }

    @Unique
    private static int brinMoodOutline(Player player) {
        float mood = PlayerMoodComponent.KEY.get(player).getMood();
        if (mood < 0.2F) return LOW_MOOD_OUTLINE;
        if (mood < 0.55F) return MEDIUM_MOOD_OUTLINE;
        return HIGH_MOOD_OUTLINE;
    }

    @Unique
    private static boolean brinIsTrapped(Player localPlayer, java.util.UUID targetId) {
        for (Player player : localPlayer.level().players()) {
            TrapperComponent trapper = TrapperComponent.KEY.get(player);
            if (trapper != null && trapper.isTrapped(targetId)) return true;
        }
        return false;
    }

    @Unique
    private static boolean brinIsBartender(GameWorldComponent gameWorld, Player player) {
        var localRole = gameWorld.getRole(player);
        return localRole != null
            && "noellesroles".equals(localRole.identifier().getNamespace())
            && "bartender".equals(localRole.identifier().getPath());
    }

    @Unique
    private static int brinShieldLayers(Player player) {
        if (brinShieldComponentKey == null || brinShieldArmorField == null) {
            try {
                Class<?> componentClass = Class.forName("org.agmas.noellesroles.bartender.BartenderPlayerComponent");
                Field keyField;
                try {
                    keyField = componentClass.getField("KEY");
                } catch (NoSuchFieldException exception) {
                    keyField = componentClass.getDeclaredField("KEY");
                    keyField.trySetAccessible();
                }
                Field armorField;
                try {
                    armorField = componentClass.getField("armor");
                } catch (NoSuchFieldException exception) {
                    armorField = componentClass.getDeclaredField("armor");
                    armorField.trySetAccessible();
                }
                Object key = keyField.get(null);
                if (key instanceof ComponentKey<?> componentKey) {
                    brinShieldComponentKey = componentKey;
                    brinShieldArmorField = armorField;
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }

        int layers = 0;
        try {
            if (brinShieldComponentKey != null && brinShieldArmorField != null) {
                Object component = brinShieldComponentKey.get(player);
                if (component != null) layers = Math.max(0, brinShieldArmorField.getInt(component));
            }
        } catch (IllegalAccessException | RuntimeException ignored) {
        }
        try {
            PenitentComponent penitent = PenitentComponent.KEY.get(player);
            NightmareComponent nightmare = NightmareComponent.KEY.get(player);
            BoneharvesterComponent boneharvester = BoneharvesterComponent.KEY.get(player);
            ZhangshiComponent zhangshi = ZhangshiComponent.KEY.get(player);
            if (penitent != null) layers = Math.max(layers, penitent.getShieldLayers());
            if (nightmare != null) layers = Math.max(layers, nightmare.getShieldLayers());
            if (boneharvester != null) layers = Math.max(layers, boneharvester.getShieldLayers());
            if (zhangshi != null) layers = Math.max(layers, zhangshi.getShieldLayers());
        } catch (RuntimeException ignored) {
        }
        return layers;
    }
}
