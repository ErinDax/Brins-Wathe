package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.BrinMorphlingAccess;
import cn.erindax.brinswathe.client.BrinMorphlingClient;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = LimitedInventoryScreen.class, priority = 2000)
public abstract class BrinMorphlingScreenMixin extends LimitedHandledScreen<InventoryMenu> {
    @Shadow
    @Final
    public LocalPlayer player;

    public BrinMorphlingScreenMixin(
        @NotNull InventoryMenu handler,
        @NotNull net.minecraft.world.entity.player.Inventory inventory,
        @NotNull Component title
    ) {
        super(handler, inventory, title);
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void brinReplaceMorphlingHeads(CallbackInfo ci) {
        if (this.player == null) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
        if (!BrinMorphlingClient.isMorphling(game, this.player)) return;

        List<net.minecraft.client.gui.components.events.GuiEventListener> official = new ArrayList<>();
        for (net.minecraft.client.gui.components.events.GuiEventListener child : this.children()) {
            if (child.getClass().getName().endsWith("MorphlingPlayerWidget")) {
                official.add(child);
            }
        }
        for (net.minecraft.client.gui.components.events.GuiEventListener child : official) {
            this.removeWidget(child);
        }

        List<UUID> targets = BrinMorphlingClient.deadTeammates(game, this.player);
        if (targets.isEmpty()) return;

        int spacing = 36;
        int x = this.width / 2 - targets.size() * spacing / 2 + 9;
        int y = (this.height - 32) / 2 + 80;
        for (int i = 0; i < targets.size(); i++) {
            UUID targetId = targets.get(i);
            PlayerSkin skin = BrinMorphlingClient.skin(targetId);
            if (skin == null) skin = DefaultPlayerSkin.get(targetId);
            String name = BrinMorphlingClient.name(targetId);
            PlayerSkin face = skin;
            this.addRenderableWidget(new net.minecraft.client.gui.components.Button(
                x + i * spacing, y, 16, 16,
                Component.empty(),
                button -> BrinMorphlingClient.sendMorph(targetId),
                supplier -> supplier.get()
            ) {
                @Override
                public void renderWidget(
                    @NotNull net.minecraft.client.gui.GuiGraphics context,
                    int mouseX,
                    int mouseY,
                    float delta
                ) {
                    int ticks = BrinMorphlingAccess.morphTicks(BrinMorphlingScreenMixin.this.player);
                    if (ticks > 0) return;
                    boolean cooling = ticks < 0;
                    super.renderWidget(context, mouseX, mouseY, delta);
                    if (cooling) context.setColor(0.25F, 0.25F, 0.25F, 0.5F);
                    context.blitSprite(
                        ShopEntry.Type.POISON.getTexture(),
                        this.getX() - 7,
                        this.getY() - 7,
                        30,
                        30
                    );
                    PlayerFaceRenderer.draw(context, face, this.getX(), this.getY(), 16);
                    context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    if (cooling) {
                        context.drawString(
                            Minecraft.getInstance().font,
                            String.valueOf((-ticks + 19) / 20),
                            this.getX(),
                            this.getY(),
                            0xFFFF5555,
                            true
                        );
                    }
                    if (this.isHovered()) {
                        context.fillGradient(
                            net.minecraft.client.renderer.RenderType.guiOverlay(),
                            this.getX(),
                            this.getY(),
                            this.getX() + 16,
                            this.getY() + 14,
                            -1862287543,
                            -1862287543,
                            0
                        );
                        context.renderTooltip(
                            Minecraft.getInstance().font,
                            Component.literal(name),
                            mouseX,
                            mouseY
                        );
                    }
                }
            });
        }
    }
}
