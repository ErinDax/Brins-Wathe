package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.FuneralComponent;
import cn.erindax.brinswathe.component.IllusionistComponent;
import cn.erindax.brinswathe.component.MorticianComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.UUID;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class BrinFuneral {
    public static final int COLOR = 0x9E9E9E;
    private static final ResourceLocation RESTRICTION_PHASE =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "funeral");

    private BrinFuneral() {
    }

    public static void init() {
        UseItemCallback.EVENT.addPhaseOrdering(RESTRICTION_PHASE, Event.DEFAULT_PHASE);
        UseItemCallback.EVENT.register(RESTRICTION_PHASE, (player, level, hand) -> isCarrying(player)
            ? InteractionResultHolder.fail(player.getItemInHand(hand))
            : InteractionResultHolder.pass(player.getItemInHand(hand)));
        UseBlockCallback.EVENT.addPhaseOrdering(RESTRICTION_PHASE, Event.DEFAULT_PHASE);
        UseBlockCallback.EVENT.register(RESTRICTION_PHASE, (player, level, hand, hitResult) -> restrict(player));
        UseEntityCallback.EVENT.addPhaseOrdering(RESTRICTION_PHASE, Event.DEFAULT_PHASE);
        UseEntityCallback.EVENT.register(RESTRICTION_PHASE, (player, level, hand, entity, hitResult) -> restrict(player));
        AttackEntityCallback.EVENT.addPhaseOrdering(RESTRICTION_PHASE, Event.DEFAULT_PHASE);
        AttackEntityCallback.EVENT.register(RESTRICTION_PHASE, (player, level, hand, entity, hitResult) -> restrict(player));
        AttackBlockCallback.EVENT.addPhaseOrdering(RESTRICTION_PHASE, Event.DEFAULT_PHASE);
        AttackBlockCallback.EVENT.register(RESTRICTION_PHASE, (player, level, hand, pos, direction) -> restrict(player));
    }

    public static boolean isCarrying(Player player) {
        FuneralComponent funeral = FuneralComponent.KEY.get(player);
        return funeral != null && funeral.isCarrying();
    }

    public static boolean canCarry(ServerPlayer player) {
        if (CowboyDuel.isActive() || !GameFunctions.isPlayerAliveAndSurvival(player)) return false;
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        return game.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE
            && BrinModifiers.hasModifier(player, BrinModifiers.FUNERAL);
    }

    public static boolean isCarryable(PlayerBodyEntity body) {
        return !body.isRemoved()
            && !body.isInvisible()
            && !body.isPassenger()
            && !IllusionistComponent.isIllusionModel(body)
            && !PuppeteerControlComponent.isPuppetModel(body)
            && !MorticianComponent.isDisguiseBody(body);
    }

    public static void pickUp(ServerPlayer player, @Nullable UUID bodyId) {
        if (bodyId == null || !canCarry(player)) return;
        FuneralComponent funeral = FuneralComponent.KEY.get(player);
        if (funeral == null || funeral.isCarrying()) return;
        if (!(player.serverLevel().getEntity(bodyId) instanceof PlayerBodyEntity body)) return;
        if (!isCarryable(body) || !player.canInteractWithEntity(body, 1.0D)) return;
        BrinArchivistWitness.onCorpseInteract(player, body);
        funeral.pickUp(body);
    }

    public static void putDown(ServerPlayer player) {
        FuneralComponent funeral = FuneralComponent.KEY.get(player);
        if (funeral != null) funeral.putDown();
    }

    private static InteractionResult restrict(Player player) {
        return isCarrying(player) ? InteractionResult.FAIL : InteractionResult.PASS;
    }
}
