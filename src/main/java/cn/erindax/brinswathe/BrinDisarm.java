package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.DisarmComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;

public final class BrinDisarm {
    public static final int COLOR = 0xFF5555;
    private static final ResourceLocation RESTRICTION_PHASE =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "disarm");

    private BrinDisarm() {
    }

    public static void init() {
        UseItemCallback.EVENT.addPhaseOrdering(RESTRICTION_PHASE, Event.DEFAULT_PHASE);
        UseItemCallback.EVENT.register(RESTRICTION_PHASE, (player, level, hand) -> isDisarmed(player)
            ? InteractionResultHolder.fail(player.getItemInHand(hand))
            : InteractionResultHolder.pass(player.getItemInHand(hand)));
        UseEntityCallback.EVENT.addPhaseOrdering(RESTRICTION_PHASE, Event.DEFAULT_PHASE);
        UseEntityCallback.EVENT.register(RESTRICTION_PHASE, (player, level, hand, entity, hitResult) -> restrictHeldItem(player, hand));
        AttackEntityCallback.EVENT.addPhaseOrdering(RESTRICTION_PHASE, Event.DEFAULT_PHASE);
        AttackEntityCallback.EVENT.register(RESTRICTION_PHASE, (player, level, hand, entity, hitResult) -> restrictHeldItem(player, hand));
    }

    public static boolean isDisarmed(Player player) {
        return DisarmComponent.KEY.get(player.level()).isActive()
            && !CowboyDuel.isActive()
            && GameFunctions.isPlayerAliveAndSurvival(player);
    }

    public static void start(ServerLevel level, int seconds) {
        DisarmComponent.KEY.get(level).start(Math.max(0, seconds) * 20);
        Component message = Component.translatable("message.brinswathe.inspector.disarm", seconds)
            .withStyle(style -> style.withColor(BrinRoles.INSPECTOR.color()));
        for (ServerPlayer player : level.players()) {
            if (GameFunctions.isPlayerAliveAndSurvival(player)) player.stopUsingItem();
            player.sendSystemMessage(message);
            player.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    private static InteractionResult restrictHeldItem(Player player, InteractionHand hand) {
        return isDisarmed(player) && !player.getItemInHand(hand).isEmpty()
            ? InteractionResult.FAIL
            : InteractionResult.PASS;
    }
}
