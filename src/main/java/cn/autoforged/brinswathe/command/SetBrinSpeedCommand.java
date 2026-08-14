package cn.autoforged.brinswathe.command;

import cn.autoforged.brinswathe.component.StaminaComponent;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SetBrinSpeedCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("setbrinspeed")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload")
                    .executes(ctx -> resetGlobalSettings(ctx.getSource())))
                .then(Commands.literal("maxStamina")
                    .then(Commands.argument("value", IntegerArgumentType.integer(1, 1000))
                        .executes(ctx -> setGlobalMaxStamina(
                            ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value"))))
                )
                .then(Commands.literal("runSpeed")
                    .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 10.0f))
                        .executes(ctx -> setGlobalRunSpeed(
                            ctx.getSource(), FloatArgumentType.getFloat(ctx, "value"))))
                )
                .then(Commands.literal("regenRate")
                    .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                        .executes(ctx -> setGlobalRegenRate(
                            ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value"))))
                )
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.literal("maxStamina")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 1000))
                            .executes(ctx -> setGlobalMaxStamina(
                                ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value"))))
                    )
                    .then(Commands.literal("runSpeed")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 10.0f))
                            .executes(ctx -> setGlobalRunSpeed(
                                ctx.getSource(), FloatArgumentType.getFloat(ctx, "value"))))
                    )
                    .then(Commands.literal("regenRate")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                            .executes(ctx -> setGlobalRegenRate(
                                ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value"))))
                    )
                )
        );
    }

    private static int setGlobalMaxStamina(CommandSourceStack source, int value) {
        StaminaComponent.setGlobalMaxStamina(value);
        applyGlobalSettings(source);
        source.sendSuccess(() -> Component.literal("Set global max stamina to " + value), true);
        return 1;
    }

    private static int resetGlobalSettings(CommandSourceStack source) {
        StaminaComponent.clearGlobalOverrides();
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            StaminaComponent component = StaminaComponent.KEY.get(player);
            if (component != null) component.resetToInitialSettings();
        }
        source.sendSuccess(() -> Component.literal("Reset global stamina settings to defaults"), true);
        return 1;
    }

    private static int setGlobalRunSpeed(CommandSourceStack source, float value) {
        StaminaComponent.setGlobalRunSpeed(value);
        applyGlobalSettings(source);
        source.sendSuccess(() -> Component.literal("Set global run speed to " + value), true);
        return 1;
    }

    private static int setGlobalRegenRate(CommandSourceStack source, int value) {
        StaminaComponent.setGlobalRegenRate(value);
        applyGlobalSettings(source);
        source.sendSuccess(() -> Component.literal("Set global stamina regen rate to " + value + "/s"), true);
        return 1;
    }

    private static void applyGlobalSettings(CommandSourceStack source) {
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            StaminaComponent component = StaminaComponent.KEY.get(player);
            if (component != null) component.applyGlobalOverrides();
        }
    }
}
