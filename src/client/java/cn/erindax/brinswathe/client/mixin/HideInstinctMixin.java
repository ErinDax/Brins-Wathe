package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.BrinModifiers;
import cn.erindax.brinswathe.BrinNoelleAccess;
import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.client.BrinsWatheClient;
import cn.erindax.brinswathe.component.AvengerComponent;
import cn.erindax.brinswathe.component.BerserkerComponent;
import cn.erindax.brinswathe.component.BombComponent;
import cn.erindax.brinswathe.component.BoneharvesterComponent;
import cn.erindax.brinswathe.component.GamblerComponent;
import cn.erindax.brinswathe.component.IllusionistComponent;
import cn.erindax.brinswathe.component.NightmareComponent;
import cn.erindax.brinswathe.component.PenitentComponent;
import cn.erindax.brinswathe.component.SniperComponent;
import cn.erindax.brinswathe.component.TrapperComponent;
import cn.erindax.brinswathe.component.ZhangshiComponent;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;

import java.lang.reflect.Field;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
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
    @Unique
    private static final int THIEF_OUTLINE = 0x7A3002;
    @Unique
    private static final int BODY_OUTLINE = 0x606060;

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
            if (WatheClient.instinctKeybind.isDown()
                && (BrinNoelleAccess.isRole(gameWorld, localPlayer, BrinNoelleAccess.JESTER_ID)
                    || BrinModifiers.hasModifier(localPlayer, BrinModifiers.EAGLE_EYE)
                    || brinIsPreparing(gameWorld))) {
                cir.setReturnValue(true);
                return;
            }
            if (brinIsBartender(gameWorld, localPlayer)) {
                cir.setReturnValue(false);
                return;
            }
            if (localPlayer.isSleeping() && brinShieldLayers(localPlayer) > 0) {
                cir.setReturnValue(true);
                return;
            }
            if (gameWorld.isRole(localPlayer, BrinRoles.AVENGER)) {
                AvengerComponent avenger = AvengerComponent.KEY.get(localPlayer);
                cir.setReturnValue(avenger != null && avenger.instinctTicks() > 0);
                return;
            }
            if (brinIsThief(role) && WatheClient.instinctKeybind.isDown()) {
                cir.setReturnValue(true);
                return;
            }
            if (BrinModifiers.hasModifier(localPlayer, BrinModifiers.MICEYES)
                && WatheClient.instinctKeybind.isDown()) {
                cir.setReturnValue(true);
                return;
            }
        }

        if (role == null || !"brin".equals(role.identifier().getNamespace())) return;

        boolean spectatorInstinct = GameFunctions.isPlayerSpectatingOrCreative(localPlayer);
        BerserkerComponent berserker = BerserkerComponent.KEY.get(localPlayer);
        boolean livingInstinct = gameWorld.canUseKillerFeatures(localPlayer)
            || gameWorld.isRole(localPlayer, BrinRoles.PENITENT)
            || NightmareComponent.isNightmareHour(gameWorld, localPlayer)
            || (gameWorld.isRole(localPlayer, BrinRoles.BERSERKER)
                && berserker != null && berserker.psychoActive);
        cir.setReturnValue(
            WatheClient.instinctKeybind.isDown()
                && (spectatorInstinct
                    || (livingInstinct && WatheClient.isPlayerAliveAndInSurvival()))
        );
    }

    @Inject(method = "isKiller", at = @At("RETURN"), cancellable = true, remap = false)
    private static void brinMiceyesIsKiller(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (BrinModifiers.hasModifier(player, BrinModifiers.MICEYES)) {
            cir.setReturnValue(true);
            return;
        }
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (NightmareComponent.isNightmareHour(gameWorld, player)) {
            cir.setReturnValue(true);
        }
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

        var localRole = gameWorld.getRole(localPlayer);
        if (brinIsThief(localRole)) {
            if (!WatheClient.isInstinctEnabled()) {
                cir.setReturnValue(-1);
                return;
            }
            if (!(target instanceof Player) && !(target instanceof ItemEntity)) {
                cir.setReturnValue(-1);
                return;
            }
            if (target == localPlayer) {
                cir.setReturnValue(THIEF_OUTLINE);
                return;
            }
            cir.setReturnValue(NEUTRAL_OUTLINE);
            return;
        }

        if (target instanceof Player stealthTarget
            && stealthTarget != localPlayer
            && stealthTarget.distanceTo(localPlayer) < 10.0F
            && BrinModifiers.hasModifier(stealthTarget, BrinModifiers.STEALTH)) {
            return;
        }

        if (BrinNoelleAccess.isRole(gameWorld, localPlayer, BrinNoelleAccess.JESTER_ID)
            && WatheClient.isInstinctEnabled()
            && target instanceof Player jesterTarget
            && !jesterTarget.isSpectator()) {
            cir.setReturnValue(0xFFAFAF);
            return;
        }

        if (WatheClient.isKiller()
            && WatheClient.isInstinctEnabled()
            && WatheClient.isPlayerAliveAndInSurvival()
            && target instanceof Player mimicTarget
            && !mimicTarget.isSpectator()
            && BrinNoelleAccess.isRole(gameWorld, mimicTarget, BrinNoelleAccess.MIMIC_ID)) {
            cir.setReturnValue(net.minecraft.util.Mth.hsvToRgb(0.0F, 1.0F, 0.6F));
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
            if (nightmareComponent != null
                && nightmareComponent.isNightmareHour()
                && WatheClient.isInstinctEnabled()) {
                if (gameWorld.canUseKillerFeatures(targetPlayer)) {
                    cir.setReturnValue(KILLER_OUTLINE);
                    return;
                }
                if (gameWorld.isInnocent(targetPlayer)) {
                    cir.setReturnValue(brinMoodOutline(targetPlayer));
                    return;
                }
                cir.setReturnValue(NEUTRAL_OUTLINE);
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

            var targetRole = gameWorld.getRole(targetPlayer);
            if (WatheClient.isKiller()
                && WatheClient.isInstinctEnabled()
                && brinIsThief(targetRole)) {
                cir.setReturnValue(THIEF_OUTLINE);
                return;
            }
        }
    }

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
        if (target instanceof Player stealthed
            && stealthed != localPlayer
            && !gameWorld.canUseKillerFeatures(localPlayer)
            && (stealthed.isInvisible() || stealthed.hasEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY))) {
            cir.setReturnValue(-1);
            return;
        }
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

        var localRole = gameWorld.getRole(localPlayer);
        if (brinShieldLayers(localPlayer) > 0
            && !gameWorld.canUseKillerFeatures(localPlayer)
            && (localRole == null || !"brin".equals(localRole.identifier().getNamespace()))) {
            cir.setReturnValue(-1);
            return;
        }
        if (target instanceof PlayerBodyEntity body
            && WatheClient.isInstinctEnabled()
            && !body.isInvisible()
            && !IllusionistComponent.isIllusionModel(body)
            && !BrinNoelleAccess.isRole(gameWorld, localPlayer, BrinNoelleAccess.VULTURE_ID)) {
            cir.setReturnValue(BODY_OUTLINE);
        }
    }
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
    private static boolean brinIsPreparing(GameWorldComponent gameWorld) {
        GameWorldComponent.GameStatus status = gameWorld.getGameStatus();
        return status != GameWorldComponent.GameStatus.ACTIVE
            && status != GameWorldComponent.GameStatus.STOPPING;
    }
    @Unique
    private static boolean brinIsBartender(GameWorldComponent gameWorld, Player player) {
        var localRole = gameWorld.getRole(player);
        return localRole != null
            && "noellesroles".equals(localRole.identifier().getNamespace())
            && "bartender".equals(localRole.identifier().getPath());
    }
    @Unique
    private static boolean brinIsThief(dev.doctor4t.wathe.api.Role role) {
        return role != null
            && "stupid_express".equals(role.identifier().getNamespace())
            && "thief".equals(role.identifier().getPath());
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
