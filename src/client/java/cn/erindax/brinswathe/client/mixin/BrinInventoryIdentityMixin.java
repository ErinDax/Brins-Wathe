package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinsWatheClient;
import cn.erindax.brinswathe.client.gui.BrinStoreItemWidget;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LimitedInventoryScreen.class)
public abstract class BrinInventoryIdentityMixin {
    @Shadow
    @Final
    public LocalPlayer player;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("TAIL"))
    private void brinRenderIdentity(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.player == null || BrinsWatheClient.isCowboyDuelHidingIdentities()) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
        if (game == null || !game.isRunning()) return;
        Role role = game.getRole(this.player);
        if (role == null) return;

        Screen screen = (Screen) (Object) this;
        Font font = Minecraft.getInstance().font;
        int centerX = screen.width / 2;
        int panelTop = (screen.height - 166) / 2;
        int shopTop = brinShopTop(screen);
        boolean hasShop = shopTop >= 0;
        int invTop = hasShop ? Math.min(shopTop, panelTop) : panelTop;
        int roleOffset = hasShop ? 48 : 22;
        int modifierOffset = hasShop ? 37 : 11;
        MutableComponent roleLine = brinRoleLabel(role);
        WorldModifierComponent worldModifiers = WorldModifierComponent.KEY.get(this.player.level());
        List<Modifier> modifiers = worldModifiers == null ? null : worldModifiers.getModifiers(this.player);
        MutableComponent modifierLine = brinModifierLabel(modifiers);
        if (modifierLine == null) {
            brinDrawCentered(graphics, font, roleLine, centerX, Math.max(4, invTop - (hasShop ? 37 : 12)));
            return;
        }
        brinDrawCentered(graphics, font, roleLine, centerX, Math.max(4, invTop - roleOffset));
        brinDrawCentered(graphics, font, modifierLine, centerX, Math.max(14, invTop - modifierOffset));
    }

    @Unique
    private static int brinShopTop(Screen screen) {
        int shopY = Integer.MAX_VALUE;
        for (GuiEventListener child : screen.children()) {
            if (child instanceof BrinStoreItemWidget && child instanceof AbstractWidget widget) {
                shopY = Math.min(shopY, widget.getY());
            }
        }
        return shopY == Integer.MAX_VALUE ? -1 : shopY;
    }

    @Unique
    private static void brinDrawCentered(GuiGraphics graphics, Font font, Component text, int centerX, int y) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, 0xFFFFFF, true);
    }

    @Unique
    private static MutableComponent brinRoleLabel(Role role) {
        ResourceLocation id = role.identifier();
        String namespaced = "announcement.role." + id.getNamespace() + "." + id.getPath();
        String plain = "announcement.role." + id.getPath();
        Language language = Language.getInstance();
        MutableComponent text = language.has(namespaced)
            ? Component.translatable(namespaced)
            : language.has(plain)
                ? Component.translatable(plain)
                : Harpymodloader.getRoleName(role).copy();
        return text.withColor(role.color());
    }

    @Unique
    private static MutableComponent brinModifierLabel(List<Modifier> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) return null;
        MutableComponent line = null;
        for (Modifier modifier : modifiers) {
            if (modifier == null) continue;
            MutableComponent name = brinModifierName(modifier);
            if (line == null) {
                line = name;
                continue;
            }
            line.append(Component.literal(" · ").withColor(0xAAAAAA));
            line.append(name);
        }
        return line;
    }

    @Unique
    private static MutableComponent brinModifierName(Modifier modifier) {
        ResourceLocation id = modifier.identifier();
        String namespaced = "announcement.modifier." + id.getNamespace() + "." + id.getPath();
        String plain = "announcement.modifier." + id.getPath();
        Language language = Language.getInstance();
        MutableComponent text = !language.has(namespaced) && language.has(plain)
            ? Component.translatable(plain)
            : Component.translatable(namespaced);
        return text.withColor(modifier.color());
    }
}
