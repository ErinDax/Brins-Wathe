package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.ArchivistComponent;
import cn.erindax.brinswathe.entity.ArchivistSealedCorpse;
import cn.erindax.brinswathe.network.ArchivistWitnessS2CPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.UUID;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;

public final class BrinArchivistWitness {
    public static final int HIGHLIGHT_TICKS = 10 * 20;
    private static final ResourceLocation INTERACT_PHASE =
        ResourceLocation.fromNamespaceAndPath("brinswathe", "archivist_witness");

    private BrinArchivistWitness() {
    }

    public static void init() {
        UseEntityCallback.EVENT.addPhaseOrdering(INTERACT_PHASE, Event.DEFAULT_PHASE);
        UseEntityCallback.EVENT.register(INTERACT_PHASE, (player, level, hand, entity, hitResult) -> {
            if (!level.isClientSide && hand == InteractionHand.MAIN_HAND
                && player instanceof ServerPlayer serverPlayer) {
                onCorpseInteract(serverPlayer, entity);
            }
            return InteractionResult.PASS;
        });
    }

    public static void onCorpseInteract(ServerPlayer player, Entity entity) {
        if (!(entity instanceof PlayerBodyEntity body)) return;
        if (!((ArchivistSealedCorpse) body).brin$isArchivistSealed()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        if (game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
        UUID bodyId = body.getUUID();
        for (ServerPlayer archivist : player.server.getPlayerList().getPlayers()) {
            if (archivist == player || archivist.level() != player.level()) continue;
            if (!game.isRole(archivist, BrinRoles.ARCHIVIST)) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(archivist)) continue;
            ArchivistComponent component = ArchivistComponent.KEY.get(archivist);
            if (component == null || !component.sealedCorpses.contains(bodyId)) continue;
            archivist.displayClientMessage(
                Component.translatable("message.brinswathe.archivist.witness")
                    .withColor(BrinRoles.ARCHIVIST.color()),
                true
            );
            ServerPlayNetworking.send(archivist, new ArchivistWitnessS2CPacket(player.getUUID(), HIGHLIGHT_TICKS));
        }
    }
}
