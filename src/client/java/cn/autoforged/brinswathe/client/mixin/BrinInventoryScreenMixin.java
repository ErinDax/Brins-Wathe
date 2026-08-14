package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.component.CowboyComponent;
import cn.autoforged.brinswathe.component.MorticianComponent;
import cn.autoforged.brinswathe.component.NightmareComponent;
import cn.autoforged.brinswathe.component.PuppeteerControlComponent;
import cn.autoforged.brinswathe.network.BrinAbilityC2SPacket;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.entity.player.Player;

import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.GameSafeComponent;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LimitedInventoryScreen.class)
public abstract class BrinInventoryScreenMixin extends LimitedHandledScreen<InventoryMenu> {

    @Shadow
    @Final
    public LocalPlayer player;

    public BrinInventoryScreenMixin(@NotNull InventoryMenu handler, @NotNull net.minecraft.world.entity.player.Inventory inventory, @NotNull Component title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init()V", at = @At("HEAD"))
    void brinRenderPlayerWidgets(CallbackInfo ci) {
        if (GameSafeComponent.KEY.get(this.player.level()).isGameSafe) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.level());
        AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(this.player);

        boolean isPuppeteer = gameWorld.isRole(this.player, BrinRoles.PUPPETEER);
        boolean isStuntDouble = gameWorld.isRole(this.player, BrinRoles.STUNT_DOUBLE);
        boolean isEavesdropper = gameWorld.isRole(this.player, BrinRoles.EAVESDROPPER);
        boolean isNightmare = gameWorld.isRole(this.player, BrinRoles.NIGHTMARE);
        boolean isMortician = gameWorld.isRole(this.player, BrinRoles.MORTICIAN);
        boolean isCowboy = gameWorld.isRole(this.player, BrinRoles.COWBOY);
        NightmareComponent nightmare = NightmareComponent.KEY.get(this.player);

        if (!isPuppeteer && !isStuntDouble && !isEavesdropper && !isNightmare && !isMortician && !isCowboy) return;
        if (isNightmare && nightmare == null) return;
        if (isMortician) {
            MorticianComponent mortician = MorticianComponent.KEY.get(this.player);
            if (mortician == null || mortician.isDisguised()) return;
        }
        if (isCowboy) {
            CowboyComponent cowboy = CowboyComponent.KEY.get(this.player);
            if (cowboy == null || cowboy.duelUsed()) return;
        }

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;

        List<UUID> targets = new ArrayList<>();
        if (isPuppeteer) {
            PuppeteerControlComponent puppeteer = PuppeteerControlComponent.KEY.get(this.player);
            if (puppeteer == null || puppeteer.isControlling()) return;
            targets.addAll(puppeteer.storedPuppets);
        } else {
            for (PlayerInfo info : connection.getListedOnlinePlayers()) {
                UUID targetId = info.getProfile().getId();
                if (targetId.equals(this.player.getUUID())) continue;
                // The dead are fair game for the mortician, and their entities are never broadcast to
                // living clients - so his picker works off the tab list plus the round's role table
                // instead of looking the player up in the world. The cowboy's picker shows the same full
                // roster on purpose: a living-only list would double as a free "who is still alive" scan.
                if (isMortician || isCowboy) {
                    if (gameWorld.getRole(targetId) != null) targets.add(targetId);
                    continue;
                }
                Player target = this.player.level().getPlayerByUUID(targetId);
                if (target == null || !GameFunctions.isPlayerAliveAndSurvival(target)) continue;
                if (isEavesdropper && gameWorld.isRole(target, BrinRoles.MEDIUM)) continue;
                targets.add(targetId);
            }
        }

        if (targets.isEmpty()) return;

        int margin = 8;
        int frameWidth = 30;
        int preferredSpacing = 36;
        int minimumSpacing = 30;
        int verticalSpacing = 30;
        int availableWidth = Math.max(frameWidth, this.width - margin * 2);
        int columns = Math.min(
            targets.size(),
            Math.max(1, 1 + (availableWidth - frameWidth) / minimumSpacing)
        );
        int rows = (targets.size() + columns - 1) / columns;
        int preferredY = (this.height - 32) / 2 + 80;
        int firstY = Math.max(
            7,
            Math.min(preferredY, this.height - 23 - (rows - 1) * verticalSpacing)
        );

        for (int i = 0; i < targets.size(); i++) {
            UUID targetUUID = targets.get(i);
            PlayerInfo targetInfo = connection.getPlayerInfo(targetUUID);
            PlayerSkin skin = targetInfo == null
                ? DefaultPlayerSkin.get(targetUUID)
                : targetInfo.getSkin();
            String targetName = targetInfo == null
                ? targetUUID.toString().substring(0, 8)
                : targetInfo.getProfile().getName();

            int row = i / columns;
            int column = i % columns;
            int firstIndex = row * columns;
            int rowSize = Math.min(columns, targets.size() - firstIndex);
            int spacing = rowSize == 1
                ? 0
                : Math.min(preferredSpacing, (availableWidth - frameWidth) / (rowSize - 1));
            int rowWidth = frameWidth + spacing * (rowSize - 1);
            int btnX = (this.width - rowWidth) / 2 + 7 + column * spacing;
            int btnY = firstY + row * verticalSpacing;
            int abilityType = isPuppeteer
                ? BrinAbilityC2SPacket.ABILITY_PUPPETEER_SUMMON
                : isStuntDouble
                    ? BrinAbilityC2SPacket.ABILITY_STUNT_DOUBLE_MIMIC
                    : isEavesdropper
                        ? BrinAbilityC2SPacket.ABILITY_EAVESDROPPER_CHANNEL
                        : isMortician
                            ? BrinAbilityC2SPacket.ABILITY_MORTICIAN_DISGUISE
                            : isCowboy
                                ? BrinAbilityC2SPacket.ABILITY_COWBOY_DUEL
                                : BrinAbilityC2SPacket.ABILITY_NIGHTMARE_FORCE_SLEEP;

            this.addRenderableWidget(new net.minecraft.client.gui.components.Button(
                btnX, btnY, 16, 16,
                Component.empty(),
                button -> {
                    int currentCooldown = isNightmare
                        ? nightmare.forcedSleepTaskCooldown
                        : ability.cooldown;
                    if (currentCooldown > 0) return;
                    ClientPlayNetworking.send(new BrinAbilityC2SPacket(abilityType, targetUUID));
                    this.onClose();
                },
                s -> s.get()
            ) {
                @Override
                public void renderWidget(@NotNull net.minecraft.client.gui.GuiGraphics context, int mouseX, int mouseY, float delta) {
                    int cooldown = isNightmare
                        ? nightmare.forcedSleepTaskCooldown
                        : ability.cooldown;
                    boolean onCooldown = cooldown > 0;
                    super.renderWidget(context, mouseX, mouseY, delta);
                    if (onCooldown) {
                        context.setColor(0.25F, 0.25F, 0.25F, 0.5F);
                    }
                    context.blitSprite(
                        ShopEntry.Type.TOOL.getTexture(),
                        this.getX() - 7,
                        this.getY() - 7,
                        30,
                        30
                    );
                    PlayerFaceRenderer.draw(context, skin, this.getX(), this.getY(), 16);
                    context.setColor(1.0F, 1.0F, 1.0F, 1.0F);

                    if (onCooldown) {
                        context.drawString(Minecraft.getInstance().font,
                            String.valueOf((cooldown + 19) / 20),
                            this.getX(), this.getY(), 0xFFFF5555, true);
                    }

                    if (this.isHovered()) {
                        this.drawSlotHighlight(context);
                        Component tooltip = onCooldown
                            ? Component.translatable("tip.kinswathe.cooldown", (cooldown + 19) / 20)
                            : Component.literal(targetName);
                        context.renderTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
                    }
                }

                private void drawSlotHighlight(net.minecraft.client.gui.GuiGraphics context) {
                    int color = -1862287543;
                    context.fillGradient(net.minecraft.client.renderer.RenderType.guiOverlay(),
                        this.getX(), this.getY(), this.getX() + 16, this.getY() + 14,
                        color, color, 0);
                    context.fillGradient(net.minecraft.client.renderer.RenderType.guiOverlay(),
                        this.getX(), this.getY() + 14, this.getX() + 15, this.getY() + 15,
                        color, color, 0);
                    context.fillGradient(net.minecraft.client.renderer.RenderType.guiOverlay(),
                        this.getX(), this.getY() + 15, this.getX() + 14, this.getY() + 16,
                        color, color, 0);
                }
            });
        }

    }
}
