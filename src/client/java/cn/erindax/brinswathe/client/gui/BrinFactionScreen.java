package cn.erindax.brinswathe.client.gui;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.component.GamblerComponent;
import cn.erindax.brinswathe.component.SubverterComponent;
import cn.erindax.brinswathe.network.BrinAbilityC2SPacket;
import java.util.UUID;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class BrinFactionScreen extends Screen {
    private final int titleColor;
    private final Component killerLabel;
    private final Component civilianLabel;
    private final int killerAction;
    private final int civilianAction;
    @Nullable
    private final UUID targetId;
    private final Predicate<Player> available;
    private int top;

    private BrinFactionScreen(
        Component title,
        int titleColor,
        String labelPrefix,
        int killerAction,
        int civilianAction,
        @Nullable UUID targetId,
        Predicate<Player> available
    ) {
        super(title);
        this.titleColor = titleColor;
        this.killerLabel = Component.translatable(labelPrefix + ".killer");
        this.civilianLabel = Component.translatable(labelPrefix + ".civilian");
        this.killerAction = killerAction;
        this.civilianAction = civilianAction;
        this.targetId = targetId;
        this.available = available;
    }

    public static BrinFactionScreen subvert(UUID targetId, Component targetName) {
        return new BrinFactionScreen(
            Component.translatable("screen.brinswathe.subverter", targetName),
            BrinRoles.SUBVERTER.color(),
            "gui.brinswathe.subverter",
            BrinAbilityC2SPacket.ABILITY_SUBVERT_KILLER,
            BrinAbilityC2SPacket.ABILITY_SUBVERT_CIVILIAN,
            targetId,
            player -> {
                SubverterComponent subverter = SubverterComponent.KEY.get(player);
                return subverter != null && subverter.isPending();
            }
        );
    }

    public static BrinFactionScreen bet() {
        return new BrinFactionScreen(
            Component.translatable("screen.brinswathe.gambler"),
            BrinRoles.GAMBLER.color(),
            "gui.brinswathe.gambler",
            BrinAbilityC2SPacket.ABILITY_GAMBLER_BET_KILLER,
            BrinAbilityC2SPacket.ABILITY_GAMBLER_BET_CIVILIAN,
            null,
            player -> {
                GamblerComponent gambler = GamblerComponent.KEY.get(player);
                return gambler != null && gambler.bet() == null;
            }
        );
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        this.top = this.height / 2 - 34;
        this.addRenderableWidget(Button.builder(this.killerLabel, button -> this.choose(this.killerAction))
            .bounds(center - 100, this.top, 200, 20)
            .build());
        this.addRenderableWidget(Button.builder(this.civilianLabel, button -> this.choose(this.civilianAction))
            .bounds(center - 100, this.top + 24, 200, 20)
            .build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose())
            .bounds(center - 100, this.top + 56, 200, 20)
            .build());
    }

    @Override
    public void tick() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        if (!this.available.test(this.minecraft.player)) this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.top - 26, this.titleColor);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void choose(int action) {
        ClientPlayNetworking.send(new BrinAbilityC2SPacket(action, this.targetId));
        this.onClose();
    }
}
