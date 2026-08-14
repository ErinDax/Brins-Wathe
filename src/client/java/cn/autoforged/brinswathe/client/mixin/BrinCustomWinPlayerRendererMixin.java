package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.component.BrinCustomWinnerComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts.RoleAnnouncementText;
import dev.doctor4t.wathe.client.gui.RoundTextRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(RoundTextRenderer.class)
public abstract class BrinCustomWinPlayerRendererMixin {
    @Unique
    private static final int OTHER_COLOR = 0x808080;

    @Redirect(
        method = "renderHud",
        at = @At(
            value = "FIELD",
            target = "Ldev/doctor4t/wathe/client/gui/RoleAnnouncementTexts$RoleAnnouncementText;titleText:Lnet/minecraft/network/chat/Component;",
            opcode = Opcodes.GETFIELD
        ),
        remap = false
    )
    private static Component brinModifyCategoryTitle(RoleAnnouncementText role) {
        if (!brinIsCustomWin()) return role.titleText;

        BrinCustomWinnerComponent winnerComponent = brinGetCustomWinnerComponent();
        if (role == RoleAnnouncementTexts.CIVILIAN) {
            return Component.translatable("category.custom.brinswathe.other")
                .withColor(OTHER_COLOR);
        }
        if (role == RoleAnnouncementTexts.VIGILANTE) {
            return Component.translatable(
                "announcement.role.brinswathe." + winnerComponent.getWinningTextId()
            ).withColor(winnerComponent.getColor());
        }
        if (role == RoleAnnouncementTexts.KILLER) return Component.empty();
        return role.titleText;
    }

    @Unique
    private static BrinCustomWinnerComponent brinGetCustomWinnerComponent() {
        Minecraft client = Minecraft.getInstance();
        return client.level == null ? null : BrinCustomWinnerComponent.KEY.get(client.level);
    }

    @Unique
    private static boolean brinIsCustomWin() {
        BrinCustomWinnerComponent winnerComponent = brinGetCustomWinnerComponent();
        return winnerComponent != null && winnerComponent.hasCustomWinner();
    }
}
