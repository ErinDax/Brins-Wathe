package cn.erindax.brinswathe.command;

import cn.erindax.brinswathe.admin.Remark;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class RemarkCommands {
    private RemarkCommands() {
    }

    static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        root
            .then(Commands.literal("remark")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("players", EntityArgument.players())
                    .then(Commands.literal("clear")
                        .executes(context -> clearRemark(
                            context.getSource(),
                            EntityArgument.getPlayers(context, "players")
                        )))
                    .then(Commands.argument("color", ColorArgument.color())
                        .then(Commands.argument("remark", StringArgumentType.greedyString())
                            .executes(context -> setRemark(
                                context.getSource(),
                                EntityArgument.getPlayers(context, "players"),
                                ColorArgument.getColor(context, "color"),
                                StringArgumentType.getString(context, "remark")
                            ))))));
    }

    private static int setRemark(
        CommandSourceStack source,
        Collection<ServerPlayer> players,
        ChatFormatting color,
        String remark
    ) {
        if (!Remark.valid(remark)) {
            source.sendFailure(Component.literal("备注不能为空, 最多 " + Remark.MAX_LENGTH + " 个字"));
            return 0;
        }
        List<String> names = new ArrayList<>();
        for (ServerPlayer player : players) {
            Remark.set(source.getServer().getScoreboard(), player.getScoreboardName(), remark, color);
            names.add(player.getGameProfile().getName());
        }
        String joined = String.join(", ", names);
        source.sendSuccess(
            () -> Component.literal("已设置备注 ").append(Remark.prefix(remark, color)).append(": " + joined),
            true
        );
        return names.size();
    }

    private static int clearRemark(CommandSourceStack source, Collection<ServerPlayer> players) {
        List<String> cleared = new ArrayList<>();
        for (ServerPlayer player : players) {
            if (Remark.clear(source.getServer().getScoreboard(), player.getScoreboardName())) {
                cleared.add(player.getGameProfile().getName());
            }
        }
        if (cleared.isEmpty()) {
            source.sendFailure(Component.literal("该玩家没有备注"));
            return 0;
        }
        String names = String.join(", ", cleared);
        source.sendSuccess(() -> Component.literal("已清除备注: " + names), true);
        return cleared.size();
    }
}
