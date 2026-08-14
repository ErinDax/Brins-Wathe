package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.BrinShopAccess;
import cn.autoforged.brinswathe.client.gui.BrinStoreItemWidget;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LimitedInventoryScreen.class)
public class BrinShopScreenMixin {
    @Shadow
    @Final
    public LocalPlayer player;

    @ModifyExpressionValue(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/world/entity/player/Player;)Z"
        )
    )
    private boolean brinAllowRoleShops(boolean original) {
        GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
        if (BrinShopAccess.hasNoShop(game, this.player)) return false;
        return original || BrinShopAccess.canUseShopAndEconomy(game, this.player);
    }

    @ModifyExpressionValue(
        method = "init",
        at = @At(
            value = "FIELD",
            target = "Ldev/doctor4t/wathe/game/GameConstants;SHOP_ENTRIES:Ljava/util/List;"
        )
    )
    private List<ShopEntry> brinGetShopEntries(List<ShopEntry> original) {
        GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
        List<ShopEntry> configured = BrinShopAccess.getShopEntries(game, this.player);
        return configured == null ? original : configured;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void brinLayoutShopWidgets(CallbackInfo ci) {
        LimitedInventoryScreen screen = (LimitedInventoryScreen) (Object) this;
        List<AbstractWidget> storeWidgets = new ArrayList<>();
        for (GuiEventListener child : screen.children()) {
            if (child instanceof BrinStoreItemWidget && child instanceof AbstractWidget widget) {
                storeWidgets.add(widget);
            }
        }
        if (storeWidgets.isEmpty()) return;

        int margin = 8;
        int frameWidth = 30;
        int preferredSpacing = 38;
        int minimumSpacing = 30;
        int verticalSpacing = 32;
        int availableWidth = Math.max(frameWidth, screen.width - margin * 2);
        int columns = Math.min(
            storeWidgets.size(),
            Math.max(1, 1 + (availableWidth - frameWidth) / minimumSpacing)
        );
        int rows = (storeWidgets.size() + columns - 1) / columns;
        int lastY = Math.max(
            7 + (rows - 1) * verticalSpacing,
            Math.min(storeWidgets.getFirst().getY(), screen.height - 23)
        );
        int firstY = lastY - (rows - 1) * verticalSpacing;

        for (int row = 0; row < rows; row++) {
            int firstIndex = row * columns;
            int rowSize = Math.min(columns, storeWidgets.size() - firstIndex);
            int spacing = rowSize == 1
                ? 0
                : Math.min(preferredSpacing, (availableWidth - frameWidth) / (rowSize - 1));
            int rowWidth = frameWidth + spacing * (rowSize - 1);
            int firstX = (screen.width - rowWidth) / 2 + 7;

            for (int column = 0; column < rowSize; column++) {
                AbstractWidget widget = storeWidgets.get(firstIndex + column);
                widget.setX(firstX + column * spacing);
                widget.setY(firstY + row * verticalSpacing);
            }
        }
    }
}
