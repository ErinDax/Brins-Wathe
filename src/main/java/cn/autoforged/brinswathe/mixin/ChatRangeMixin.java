package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.BrinRoles;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerList.class)
public abstract class ChatRangeMixin {

    @Shadow
    public abstract List<ServerPlayer> getPlayers();

    @Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
            at = @At("HEAD"), cancellable = true)
    private void brinFilterChatRange(PlayerChatMessage msg, ServerPlayer sender, ChatType.Bound bound, CallbackInfo ci) {
        ci.cancel();

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(sender.level());
        boolean isMedium = gameWorld.isRole(sender, BrinRoles.MEDIUM);
        boolean senderSpectating = GameFunctions.isPlayerSpectatingOrCreative(sender);

        sender.getServer().logChatMessage(msg.decoratedContent(), bound, null);
        OutgoingChatMessage outgoing = OutgoingChatMessage.create(msg);
        boolean fullyFiltered = false;

        for (ServerPlayer recipient : this.getPlayers()) {
            GameWorldComponent recipientGame = GameWorldComponent.KEY.get(recipient.level());
            boolean recipientSpectating = GameFunctions.isPlayerSpectatingOrCreative(recipient);
            if (senderSpectating != recipientSpectating) continue;

            boolean isEavesdropper = !senderSpectating
                && recipientGame.isRole(recipient, BrinRoles.EAVESDROPPER)
                && GameFunctions.isPlayerAliveAndSurvival(recipient);
            boolean isSender = recipient.equals(sender);

            if (isMedium) {
                if (isSender) {
                    boolean shouldFilter = sender.shouldFilterMessageTo(recipient);
                    recipient.sendChatMessage(outgoing, shouldFilter, bound);
                }
            } else {
                boolean inRange = sender.distanceToSqr(recipient) <= 144.0;
                if (isSender || inRange || isEavesdropper) {
                    boolean shouldFilter = sender.shouldFilterMessageTo(recipient);
                    recipient.sendChatMessage(outgoing, shouldFilter, bound);
                    if (shouldFilter && msg.isFullyFiltered()) {
                        fullyFiltered = true;
                    }
                }
            }
        }

        if (fullyFiltered) {
            sender.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
        }
    }
}
