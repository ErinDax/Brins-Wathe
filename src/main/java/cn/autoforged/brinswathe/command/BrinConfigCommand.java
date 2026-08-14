package cn.autoforged.brinswathe.command;

import cn.autoforged.brinswathe.AfkKickManager;
import cn.autoforged.brinswathe.component.StaminaComponent;
import cn.autoforged.brinswathe.config.BrinConfig;
import cn.autoforged.brinswathe.network.BrinConfigS2CPacket;
import cn.autoforged.brinswathe.network.BrinResourceReloadS2CPacket;
import com.mojang.brigadier.CommandDispatcher;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class BrinConfigCommand {
    private BrinConfigCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("brinswathe")
                .then(Commands.literal("reload")
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> reload(context.getSource())))
                .then(Commands.literal("afk")
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> status(context.getSource()))
                    .then(Commands.literal("on")
                        .executes(context -> setAfkEnabled(context.getSource(), true)))
                    .then(Commands.literal("off")
                        .executes(context -> setAfkEnabled(context.getSource(), false))))
                // Deliberately permission-free: any tester must be able to dump his own server-side
                // stamina state when the hud bar looks wrong.
                .then(Commands.literal("stamina")
                    .executes(context -> staminaReport(context.getSource())))
        );
    }

    /**
     * One line of server-side ground truth for the kins stamina bar. If these numbers are healthy while
     * the bar on screen is empty, the fault is on the client's side (usually an outdated jar); if they
     * are broken, they point at the exact component that lost its value.
     */
    private static int staminaReport(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command reports on the executing player."));
            return 0;
        }

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        Role role = gameWorld.getRole(player);
        StaminaComponent stamina = StaminaComponent.KEY.get(player);
        float watheSprintTicks = player.saveWithoutId(new CompoundTag()).getFloat("sprintingTicks");

        String report = "role=" + (role == null ? "null" : role.identifier())
            + " maxSprint=" + (role == null ? "-" : role.getMaxSprintTime())
            + " | stamina=" + (stamina == null ? "-" : stamina.currentStamina + "/" + stamina.maxStamina)
            + " regen=" + (stamina == null ? "-" : stamina.regenRate)
            + " | usesGameStamina=" + StaminaComponent.usesGameStamina(player)
            + " | watheSprintTicks=" + String.format("%.1f", watheSprintTicks)
            + " | status=" + gameWorld.getGameStatus()
            + " running=" + gameWorld.isRunning()
            + " alive=" + GameFunctions.isPlayerAliveAndSurvival(player);
        source.sendSuccess(() -> Component.literal(report), false);
        return 1;
    }

    private static int reload(CommandSourceStack source) {
        try {
            BrinConfig.reload();
            AfkKickManager.reset();
            BrinConfigS2CPacket packet = new BrinConfigS2CPacket(BrinConfig.toJson());
            for (var player : source.getServer().getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, packet);
                ServerPlayNetworking.send(player, new BrinResourceReloadS2CPacket());
            }
            source.sendSuccess(
                () -> Component.literal("Brin's Wathe config and language resources reloaded: " + BrinConfig.path()),
                true
            );
            return 1;
        } catch (Exception exception) {
            source.sendFailure(Component.literal("Brin's Wathe config reload failed: " + exception.getMessage()));
            return 0;
        }
    }

    private static int status(CommandSourceStack source) {
        source.sendSuccess(
            () -> Component.translatable(
                BrinConfig.afkKickEnabled()
                    ? "command.brinswathe.afk.enabled"
                    : "command.brinswathe.afk.disabled"
            ),
            false
        );
        return BrinConfig.afkKickEnabled() ? 1 : 0;
    }

    private static int setAfkEnabled(CommandSourceStack source, boolean enabled) {
        try {
            BrinConfig.setAfkKickEnabled(enabled);
            AfkKickManager.reset();
            BrinConfigS2CPacket packet = new BrinConfigS2CPacket(BrinConfig.toJson());
            for (var player : source.getServer().getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, packet);
            }
            source.sendSuccess(
                () -> Component.translatable(
                    enabled
                        ? "command.brinswathe.afk.enabled"
                        : "command.brinswathe.afk.disabled"
                ),
                true
            );
            return 1;
        } catch (Exception exception) {
            source.sendFailure(Component.literal("Failed to update AFK kick setting: " + exception.getMessage()));
            return 0;
        }
    }

}
