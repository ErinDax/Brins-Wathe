package cn.erindax.brinswathe.command;

import cn.erindax.brinswathe.config.ModConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class SetRoleCountCommand {
    private static final SimpleCommandExceptionType UNCHANGED = new SimpleCommandExceptionType(
        Component.translatable("commands.setrolecount.unchanged")
    );

    private SetRoleCountCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("setRoleCount")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("neutral")
                    .then(Commands.argument("count", IntegerArgumentType.integer())
                        .executes(context -> execute(context, ModConfig.HarpyCountKind.NEUTRAL))))
                .then(Commands.literal("killer")
                    .then(Commands.argument("count", IntegerArgumentType.integer())
                        .executes(context -> execute(context, ModConfig.HarpyCountKind.KILLER))))
                .then(Commands.literal("vigilante")
                    .then(Commands.argument("count", IntegerArgumentType.integer())
                        .executes(context -> execute(context, ModConfig.HarpyCountKind.VIGILANTE))))
        );
    }

    private static int execute(
        CommandContext<CommandSourceStack> context,
        ModConfig.HarpyCountKind kind
    ) throws CommandSyntaxException {
        int newValue = IntegerArgumentType.getInteger(context, "count");
        int oldValue = ModConfig.harpyRoleCount(kind);
        if (oldValue == newValue) throw UNCHANGED.create();
        try {
            ModConfig.setHarpyRoleCount(kind, newValue);
        } catch (Exception exception) {
            context.getSource().sendFailure(Component.literal("角色数量保存失败: " + exception.getMessage()));
            return 0;
        }
        context.getSource().sendSuccess(
            () -> Component.translatable(kind.successKey(), formatValue(newValue)),
            true
        );
        return 1;
    }

    private static MutableComponent formatValue(int value) {
        if (value == 0) {
            return Component.literal(String.valueOf(value))
                .append(" ")
                .append(Component.translatable("commands.setrolecount.mode.vanilla").withStyle(ChatFormatting.GRAY));
        }
        if (value > 0) {
            return Component.literal(String.valueOf(value))
                .append(" ")
                .append(Component.translatable("commands.setrolecount.mode.fixed").withStyle(ChatFormatting.GREEN));
        }
        return Component.literal(String.valueOf(value))
            .append(" ")
            .append(Component.translatable("commands.setrolecount.mode.dynamic", Math.abs(value)).withStyle(ChatFormatting.AQUA));
    }
}
