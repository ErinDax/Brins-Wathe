package cn.erindax.brinswathe.command;

import cn.erindax.brinswathe.AfkKickManager;
import cn.erindax.brinswathe.component.StaminaComponent;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.network.ConfigS2CPacket;
import cn.erindax.brinswathe.network.ResourceReloadS2CPacket;
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

public final class ConfigCommand {
    private ConfigCommand() {
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
                .then(Commands.literal("stamina")
                    .executes(context -> staminaReport(context.getSource())))
        );
    }

    private static int staminaReport(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("此指令只能由玩家执行"));
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
            ModConfig.reload();
            AfkKickManager.reset();
            ConfigS2CPacket packet = new ConfigS2CPacket(ModConfig.toJson());
            for (var player : source.getServer().getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, packet);
                ServerPlayNetworking.send(player, new ResourceReloadS2CPacket());
            }
            source.sendSuccess(
                () -> Component.literal("配置与语言资源已重载: " + ModConfig.path()),
                true
            );
            return 1;
        } catch (Exception exception) {
            source.sendFailure(Component.literal("配置重载失败: " + exception.getMessage()));
            return 0;
        }
    }
    private static int status(CommandSourceStack source) {
        source.sendSuccess(
            () -> Component.translatable(
                ModConfig.afkKickEnabled()
                    ? "command.brinswathe.afk.enabled"
                    : "command.brinswathe.afk.disabled"
            ),
            false
        );
        return ModConfig.afkKickEnabled() ? 1 : 0;
    }
    private static int setAfkEnabled(CommandSourceStack source, boolean enabled) {
        try {
            ModConfig.setAfkKickEnabled(enabled);
            AfkKickManager.reset();
            ConfigS2CPacket packet = new ConfigS2CPacket(ModConfig.toJson());
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
            source.sendFailure(Component.literal("挂机踢出设置保存失败: " + exception.getMessage()));
            return 0;
        }
    }
}
