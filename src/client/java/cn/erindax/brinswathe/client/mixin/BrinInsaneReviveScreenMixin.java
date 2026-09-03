package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.BrinNoelleAccess;
import cn.erindax.brinswathe.network.BrinAbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LimitedInventoryScreen.class)
public abstract class BrinInsaneReviveScreenMixin extends LimitedHandledScreen<InventoryMenu> {
    @Shadow
    @Final
    public LocalPlayer player;
    public BrinInsaneReviveScreenMixin(
        @NotNull InventoryMenu handler,
        @NotNull net.minecraft.world.entity.player.Inventory inventory,
        @NotNull Component title
    ) {
        super(handler, inventory, title);
    }
    @Inject(method = "init()V", at = @At("TAIL"))
    private void brinRenderInsaneReviveHeads(CallbackInfo ci) {
        if (this.player == null) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
        if (!BrinNoelleAccess.isRole(game, this.player, BrinNoelleAccess.INSANE_KILLER_ID)) return;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;

        List<UUID> targets = new ArrayList<>();
        for (PlayerInfo info : connection.getListedOnlinePlayers()) {
            UUID targetId = info.getProfile().getId();
            if (targetId.equals(this.player.getUUID())) continue;
            if (info.getGameMode() != GameType.SPECTATOR) continue;
            targets.add(targetId);
        }
        if (targets.isEmpty()) return;
        int spacing = 36;
        int x = this.width / 2 - targets.size() * spacing / 2 + 9;
        int y = (this.height - 32) / 2 + 80;
        for (int i = 0; i < targets.size(); i++) {
            UUID targetId = targets.get(i);
            PlayerInfo info = connection.getPlayerInfo(targetId);
            PlayerSkin skin = info == null ? DefaultPlayerSkin.get(targetId) : info.getSkin();
            String name = info == null
                ? targetId.toString().substring(0, 8)
                : info.getProfile().getName();
            this.addRenderableWidget(new Button(
                x + i * spacing, y, 16, 16,
                Component.empty(),
                button -> {
                    if (BrinNoelleAccess.noelleAbilityCooldown(this.player) > 0) return;
                    ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                        BrinAbilityC2SPacket.ABILITY_INSANE_REVIVE, targetId));
                    this.onClose();
                },
                supplier -> supplier.get()
            ) {
                @Override
                public void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
                    int cooldown = BrinNoelleAccess.noelleAbilityCooldown(
                        BrinInsaneReviveScreenMixin.this.player);
                    boolean onCooldown = cooldown > 0;
                    super.renderWidget(context, mouseX, mouseY, delta);
                    if (onCooldown) context.setColor(0.25F, 0.25F, 0.25F, 0.5F);
                    context.blitSprite(
                        ShopEntry.Type.POISON.getTexture(),
                        this.getX() - 7,
                        this.getY() - 7,
                        30,
                        30
                    );
                    PlayerFaceRenderer.draw(context, skin, this.getX(), this.getY(), 16);
                    context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    if (onCooldown) {
                        context.drawString(
                            Minecraft.getInstance().font,
                            String.valueOf((cooldown + 19) / 20),
                            this.getX(),
                            this.getY(),
                            0xFFFF5555,
                            true
                        );
                    }
                    if (this.isHovered()) {
                        int highlight = -1862287543;
                        context.fillGradient(
                            RenderType.guiOverlay(),
                            this.getX(),
                            this.getY(),
                            this.getX() + 16,
                            this.getY() + 14,
                            highlight,
                            highlight,
                            0
                        );
                        Component tooltip = onCooldown
                            ? Component.translatable("tip.kinswathe.cooldown", (cooldown + 19) / 20)
                            : Component.literal(name);
                        context.renderTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
                    }
                }
            });
        }
    }
}
