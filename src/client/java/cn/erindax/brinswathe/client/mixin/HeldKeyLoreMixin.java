package cn.erindax.brinswathe.client.mixin;

import dev.doctor4t.wathe.index.WatheItems;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public abstract class HeldKeyLoreMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract Font getFont();

    @Inject(method = "renderSelectedItemName", at = @At("TAIL"))
    private void brinDrawHeldKeyLore(GuiGraphics graphics, CallbackInfo ci) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(WatheItems.KEY)) return;
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null || lore.styledLines().isEmpty()) return;
        Font font = this.getFont();
        Component rooms = ComponentUtils.formatList(lore.styledLines(), ComponentUtils.DEFAULT_SEPARATOR);
        List<FormattedCharSequence> rows = font.split(rooms, graphics.guiWidth() - 8);
        int y = graphics.guiHeight() - 48 - (rows.size() - 1) * (font.lineHeight + 1);
        if (!this.minecraft.gameMode.canHurtPlayer()) y += 14;
        for (FormattedCharSequence row : rows) {
            graphics.drawString(font, row, (graphics.guiWidth() - font.width(row)) / 2, y, 0xFFFFFF);
            y += font.lineHeight + 1;
        }
    }
}
