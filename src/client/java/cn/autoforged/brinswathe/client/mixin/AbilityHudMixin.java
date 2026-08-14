package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.client.BrinsWatheClient;
import cn.autoforged.brinswathe.component.CowboyComponent;
import cn.autoforged.brinswathe.component.IllusionistComponent;
import cn.autoforged.brinswathe.component.MediumComponent;
import cn.autoforged.brinswathe.component.MorticianComponent;
import cn.autoforged.brinswathe.component.PuppeteerControlComponent;
import cn.autoforged.brinswathe.component.SniperComponent;
import cn.autoforged.brinswathe.component.StalkerComponent;
import cn.autoforged.brinswathe.config.BrinConfig;
import cn.autoforged.brinswathe.entity.ArchivistSealedCorpse;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class AbilityHudMixin {

    @Shadow
    @Nullable
    private Component title;
    @Shadow
    @Nullable
    private Component subtitle;
    @Shadow
    private int titleTime;
    @Shadow
    private int titleFadeInTime;
    @Shadow
    private int titleStayTime;
    @Shadow
    private int titleFadeOutTime;

    @Shadow
    public abstract Font getFont();

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
    private void brinOnRenderHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
        boolean hideReadyHud = GameSafeComponent.KEY.get(player.level()).isGameSafe
            && brinAbilityCooldownTicks(player, gameWorld, ability) <= 0;
        Font font = Minecraft.getInstance().font;
        if (hideReadyHud) {
            brinRenderBlindnessOverlay(context, tickCounter);
            return;
        }

        if (gameWorld.isRole(player, BrinRoles.PUPPETEER)) {
            PuppeteerControlComponent puppeteer = PuppeteerControlComponent.KEY.get(player);
            if (puppeteer != null && puppeteer.isControlling()) {
                renderPuppeteerControlHud(context, font, BrinRoles.PUPPETEER.color(), puppeteer);
            } else if (puppeteer != null && puppeteer.craftCooldownTicks > 0) {
                renderConfiguredRoleAbilityHud(
                    context,
                    font,
                    puppeteer.craftCooldownTicks,
                    player,
                    "puppeteer",
                    BrinRoles.PUPPETEER.color()
                );
            } else {
                renderConfiguredRoleAbilityHud(context, font, ability, player, "puppeteer", BrinRoles.PUPPETEER.color());
            }
        } else if (gameWorld.isRole(player, BrinRoles.SNIPER)) {
            SniperComponent component = SniperComponent.KEY.get(player);
            if (component != null && component.isAiming()) {
                renderSniperAimHud(context, font, BrinRoles.SNIPER.color());
            } else if (component != null) {
                renderConfiguredRoleAbilityHud(
                    context,
                    font,
                    component.getCooldownTicks(),
                    player,
                    "sniper",
                    BrinRoles.SNIPER.color()
                );
            }
        } else if (gameWorld.isRole(player, BrinRoles.TRAPPER)) {
            renderConfiguredRoleAbilityHud(context, font, ability, player, "beast_trapper", BrinRoles.TRAPPER.color());
        } else if (gameWorld.isRole(player, BrinRoles.WATCHMAN)) {
            renderConfiguredRoleAbilityHud(context, font, ability, player, "watchman", BrinRoles.WATCHMAN.color());
        } else if (gameWorld.isRole(player, BrinRoles.NIGHTMARE)) {
            renderConfiguredRoleAbilityHud(context, font, ability, player, "nightmare", BrinRoles.NIGHTMARE.color());
        } else if (gameWorld.isRole(player, BrinRoles.ILLUSIONIST)) {
            IllusionistComponent component = IllusionistComponent.KEY.get(player);
            if (component != null && !component.cloneEntityIds.isEmpty()) {
                renderIllusionistControlHud(context, font, BrinRoles.ILLUSIONIST.color(), component);
            } else {
                renderConfiguredRoleAbilityHud(context, font, ability, player, "illusionist", BrinRoles.ILLUSIONIST.color());
            }
        } else if (gameWorld.isRole(player, BrinRoles.ARCHIVIST)) {
            renderConfiguredRoleAbilityHud(context, font, ability, player, "archivist", BrinRoles.ARCHIVIST.color());
            renderArchivistCorpseInfo(context, font, player);
        } else if (gameWorld.isRole(player, BrinRoles.GAMBLER)) {
            renderConfiguredRoleAbilityHud(context, font, ability, player, "gambler", BrinRoles.GAMBLER.color());
        } else if (gameWorld.isRole(player, BrinRoles.EAVESDROPPER)) {
            renderConfiguredRoleAbilityHud(
                context,
                font,
                ability,
                player,
                "eavesdropper",
                BrinRoles.EAVESDROPPER.color(),
                false
            );
        } else if (gameWorld.isRole(player, BrinRoles.ZHANGSHI)) {
            renderConfiguredRoleAbilityHud(context, font, ability, player, "zhangshi", BrinRoles.ZHANGSHI.color());
        } else if (gameWorld.isRole(player, BrinRoles.MEDIUM)) {
            MediumComponent medium = MediumComponent.KEY.get(player);
            if (medium != null && medium.channelTicks > 0) {
                renderMediumChannelHud(context, font, BrinRoles.MEDIUM.color(), medium);
            } else {
                renderConfiguredRoleAbilityHud(context, font, ability, player, "medium", BrinRoles.MEDIUM.color());
            }
        } else if (gameWorld.isRole(player, BrinRoles.STALKER)) {
            StalkerComponent component = StalkerComponent.KEY.get(player);
            if (component != null) {
                // The stalk is a right click rather than the ability key, so a "press G" prompt would lie.
                renderConfiguredRoleAbilityHud(
                    context,
                    font,
                    component.cooldownTicks(),
                    player,
                    "stalker",
                    BrinRoles.STALKER.color(),
                    false
                );
            }
        } else if (gameWorld.isRole(player, BrinRoles.MORTICIAN)) {
            MorticianComponent mortician = MorticianComponent.KEY.get(player);
            if (mortician == null || !mortician.isDisguised()) {
                renderConfiguredRoleAbilityHud(
                    context,
                    font,
                    ability,
                    player,
                    "mortician",
                    BrinRoles.MORTICIAN.color(),
                    false
                );
            }
        } else if (gameWorld.isRole(player, BrinRoles.COWBOY)) {
            CowboyComponent cowboy = CowboyComponent.KEY.get(player);
            // One shot per round: once spent there is no cooldown to advertise, so the hud goes dark.
            if (cowboy != null && !cowboy.duelUsed()) {
                renderConfiguredRoleAbilityHud(
                    context,
                    font,
                    0,
                    player,
                    "cowboy",
                    BrinRoles.COWBOY.color(),
                    true
                );
            }
        }
        brinRenderBlindnessOverlay(context, tickCounter);
    }

    /**
     * During the opening safe period nothing is usable yet, so the hud only earns its space when there is
     * an actual countdown to show; otherwise it would advertise "ready" for abilities the server refuses.
     */
    private int brinAbilityCooldownTicks(LocalPlayer player, GameWorldComponent gameWorld,
                                         AbilityPlayerComponent ability) {
        if (gameWorld.isRole(player, BrinRoles.SNIPER)) {
            SniperComponent sniper = SniperComponent.KEY.get(player);
            return sniper == null ? 0 : sniper.getCooldownTicks();
        }
        if (gameWorld.isRole(player, BrinRoles.PUPPETEER)) {
            PuppeteerControlComponent puppeteer = PuppeteerControlComponent.KEY.get(player);
            int craftCooldown = puppeteer == null ? 0 : puppeteer.craftCooldownTicks;
            return Math.max(craftCooldown, ability == null ? 0 : ability.cooldown);
        }
        if (gameWorld.isRole(player, BrinRoles.STALKER)) {
            StalkerComponent stalker = StalkerComponent.KEY.get(player);
            return stalker == null ? 0 : stalker.cooldownTicks();
        }
        return ability == null ? 0 : ability.cooldown;
    }

    private static final float BLIND_FADE_IN_TICKS = 10.0F;
    private static final float BLIND_FADE_OUT_TICKS = 15.0F;
    private static final float BLIND_MAX_ALPHA = 255.0F;

    private void brinRenderBlindnessOverlay(GuiGraphics context, DeltaTracker tickCounter) {
        int remaining = BrinsWatheClient.getBlindFlashRemaining();
        if (remaining <= 0) return;
        int total = Math.max(BrinsWatheClient.getBlindFlashTotal(), 1);

        float partial = tickCounter.getGameTimeDeltaPartialTick(true);
        float remainingF = Math.max(0.0F, remaining - partial);
        float elapsedF = Math.max(0.0F, total - remainingF);

        float fadeIn = elapsedF / BLIND_FADE_IN_TICKS;
        float fadeOut = remainingF / BLIND_FADE_OUT_TICKS;
        float alphaRatio = Math.min(1.0F, Math.min(fadeIn, fadeOut));

        int alpha = (int) (alphaRatio * BLIND_MAX_ALPHA);
        if (alpha <= 0) return;
        context.fill(0, 0, context.guiWidth(), context.guiHeight(), alpha << 24);
        // Vanilla titles are drawn before this overlay; put them back on top of the black.
        brinRenderTitleOverBlindness(context, tickCounter);
    }

    /**
     * Same fade math as vanilla's title layer, so the yellow showdown line (and the later 3-2-1) stay
     * readable while the rest of the HUD is covered.
     */
    private void brinRenderTitleOverBlindness(GuiGraphics context, DeltaTracker tickCounter) {
        if (this.title == null || this.titleTime <= 0) return;

        float remaining = (float) this.titleTime - tickCounter.getGameTimeDeltaPartialTick(false);
        int fade = 255;
        if (this.titleFadeInTime > 0 && this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
            float elapsed = (float) (this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - remaining;
            fade = (int) (elapsed * 255.0F / (float) this.titleFadeInTime);
        }
        if (this.titleFadeOutTime > 0 && this.titleTime <= this.titleFadeOutTime) {
            fade = (int) (remaining * 255.0F / (float) this.titleFadeOutTime);
        }
        fade = Mth.clamp(fade, 0, 255);
        if (fade <= 8) return;

        Font font = this.getFont();
        int color = 0xFFFFFF | (fade << 24);
        context.pose().pushPose();
        context.pose().translate(context.guiWidth() / 2.0F, context.guiHeight() / 2.0F, 0.0F);
        context.pose().pushPose();
        context.pose().scale(4.0F, 4.0F, 4.0F);
        int titleWidth = font.width(this.title);
        context.drawString(font, this.title, -titleWidth / 2, -10, color, true);
        context.pose().popPose();
        if (this.subtitle != null) {
            context.pose().pushPose();
            context.pose().scale(2.0F, 2.0F, 2.0F);
            int subtitleWidth = font.width(this.subtitle);
            context.drawString(font, this.subtitle, -subtitleWidth / 2, 5, color, true);
            context.pose().popPose();
        }
        context.pose().popPose();
    }

    private void renderPuppeteerControlHud(GuiGraphics context, Font font, int color,
                                           PuppeteerControlComponent component) {
        KeyMapping abilityBind = BrinsWatheClient.getAbilityBind();
        Component keyName = abilityBind == null
            ? Component.literal("G")
            : abilityBind.getTranslatedKeyMessage();
        Component line = Component.translatable(
            "tip.brinswathe.puppeteer.controlling",
            keyName,
            (component.puppetTicks + 19) / 20
        );
        int y = context.guiHeight() - 68;
        int textWidth = font.width(line);
        context.drawString(font, line, (context.guiWidth() - textWidth) / 2, y, color);
    }

    private void renderMediumChannelHud(GuiGraphics context, Font font, int color,
                                        MediumComponent component) {
        Component line = Component.translatable(
            "tip.brinswathe.medium.channelling",
            (component.channelTicks + 19) / 20
        );
        int y = context.guiHeight() - 68;
        int textWidth = font.width(line);
        context.drawString(font, line, (context.guiWidth() - textWidth) / 2, y, color);
    }

    private void renderIllusionistControlHud(GuiGraphics context, Font font, int color,
                                             IllusionistComponent component) {
        KeyMapping abilityBind = BrinsWatheClient.getAbilityBind();
        Component keyName = abilityBind == null
            ? Component.literal("G")
            : abilityBind.getTranslatedKeyMessage();
        String controlKey = "tip.brinswathe.illusionist.control.body";
        if (component.controlledCloneId != null) {
            var direction = component.cloneDirections.get(component.controlledCloneId);
            controlKey = direction != null && direction.x < 0.0D
                ? "tip.brinswathe.illusionist.control.west"
                : "tip.brinswathe.illusionist.control.east";
        }
        Component controlName = Component.translatable(controlKey);
        Component line = Component.translatable(
            "tip.brinswathe.illusionist.switch_control",
            keyName,
            controlName
        );
        int y = context.guiHeight() - 68;
        int textWidth = font.width(line);
        context.drawString(font, line, (context.guiWidth() - textWidth) / 2, y, color);
    }

    private void renderSniperAimHud(GuiGraphics context, Font font, int color) {
        int width = context.guiWidth();
        int height = context.guiHeight();
        int scopeWidth = Math.min(width, height);
        int scopeLeft = (width - scopeWidth) / 2;
        int scopeRight = scopeLeft + scopeWidth;
        int centerX = width / 2;
        int centerY = height / 2;

        context.fill(0, 0, scopeLeft, height, 0xB8000000);
        context.fill(scopeRight, 0, width, height, 0xB8000000);
        context.fill(centerX - 52, centerY, centerX - 8, centerY + 1, 0xCCAA2222);
        context.fill(centerX + 8, centerY, centerX + 52, centerY + 1, 0xCCAA2222);
        context.fill(centerX, centerY - 52, centerX + 1, centerY - 8, 0xCCAA2222);
        context.fill(centerX, centerY + 8, centerX + 1, centerY + 52, 0xCCAA2222);

        KeyMapping abilityBind = BrinsWatheClient.getAbilityBind();
        Component keyName = abilityBind == null
            ? Component.literal("G")
            : abilityBind.getTranslatedKeyMessage();
        Component line = Component.translatable(
            "tip.brinswathe.sniper.aiming",
            keyName
        );
        int textWidth = font.width(line);
        context.drawString(font, line, (width - textWidth) / 2, height - font.lineHeight - 2, color);
    }

    private void renderArchivistCorpseInfo(GuiGraphics context, Font font, LocalPlayer player) {
        PlayerBodyEntity body = BrinsWatheClient.getInspectableBodyInCrosshair(player);
        if (body == null) return;
        // A mortician playing dead left no death behind him either, so the missing file gives him away
        // exactly the way it gives away a forged corpse.
        if (!((ArchivistSealedCorpse) body).brin$isBodymakerForged()
            && !MorticianComponent.isDisguiseBody(body)) {
            return;
        }

        Component line = Component.translatable(
            "tip.brinswathe.archivist.no_record"
        );
        int x = (context.guiWidth() - font.width(line)) / 2;
        int y = context.guiHeight() / 2 + 12;
        context.drawString(font, line, x, y, 0xFFFF4444);
    }

    private void renderConfiguredRoleAbilityHud(GuiGraphics context, Font font, AbilityPlayerComponent ability,
                                                LocalPlayer player, String roleId, int color) {
        renderConfiguredRoleAbilityHud(context, font, ability, player, roleId, color, true);
    }

    private void renderConfiguredRoleAbilityHud(GuiGraphics context, Font font, AbilityPlayerComponent ability,
                                                LocalPlayer player, String roleId, int color,
                                                boolean showReadyPrompt) {
        renderConfiguredRoleAbilityHud(
            context,
            font,
            ability.cooldown,
            player,
            roleId,
            color,
            showReadyPrompt
        );
    }

    private void renderConfiguredRoleAbilityHud(GuiGraphics context, Font font, int cooldownTicks,
                                                LocalPlayer player, String roleId, int color) {
        renderConfiguredRoleAbilityHud(context, font, cooldownTicks, player, roleId, color, true);
    }

    private void renderConfiguredRoleAbilityHud(GuiGraphics context, Font font, int cooldownTicks,
                                                LocalPlayer player, String roleId, int color,
                                                boolean showReadyPrompt) {
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
        renderRoleAbilityHud(
            context,
            font,
            cooldownTicks,
            color,
            shop == null ? 0 : shop.balance,
            BrinConfig.skillCost(roleId),
            showReadyPrompt
        );
    }

    private void renderRoleAbilityHud(GuiGraphics context, Font font, int cooldownTicks, int color,
                                      int balance, int requiredMoney, boolean showReadyPrompt) {
        Component line;
        if (balance < requiredMoney) {
            line = Component.translatable(
                "tip.kinswathe.ability.not_enough_money",
                requiredMoney
            );
        } else if (cooldownTicks > 0) {
            line = Component.translatable("tip.kinswathe.cooldown", cooldownTicks / 20);
        } else {
            if (!showReadyPrompt) return;
            KeyMapping abilityBind = BrinsWatheClient.getAbilityBind();
            Component keyName = abilityBind == null
                ? Component.literal("G")
                : abilityBind.getTranslatedKeyMessage();
            line = Component.translatable("tip.kinswathe.ability.can_use",
                keyName);
        }

        int y = context.guiHeight() - font.lineHeight - 2;
        int textWidth = font.width(line);
        context.drawString(font, line, context.guiWidth() - textWidth - 2, y, color);
    }
}
