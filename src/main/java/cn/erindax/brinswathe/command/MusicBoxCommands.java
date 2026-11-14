package cn.erindax.brinswathe.command;

import cn.erindax.brinswathe.musicbox.MusicBox;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;

public final class MusicBoxCommands {
    private MusicBoxCommands() {
    }

    static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        root
            .then(Commands.literal("musicbox")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("clear")
                    .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(context -> clearMusic(
                            context.getSource(),
                            GameProfileArgument.getGameProfiles(context, "player")
                        ))))
                .then(Commands.literal("stop")
                    .executes(context -> stopMusic(context.getSource()))));
    }

    private static int clearMusic(CommandSourceStack source, Collection<GameProfile> profiles) {
        List<String> cleared = new ArrayList<>();
        for (GameProfile profile : profiles) {
            if (MusicBox.adminClear(source.getServer(), profile.getId())) cleared.add(profile.getName());
        }
        if (cleared.isEmpty()) {
            source.sendFailure(Component.literal("该玩家没有上传过音乐盒音乐"));
            return 0;
        }
        String names = String.join(", ", cleared);
        source.sendSuccess(() -> Component.literal("已清除音乐盒音乐: " + names), true);
        return cleared.size();
    }

    private static int stopMusic(CommandSourceStack source) {
        if (!MusicBox.stopBroadcast(source.getServer())) {
            source.sendFailure(Component.literal("当前没有正在播放的结算音乐"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("已停止结算音乐"), true);
        return 1;
    }
}
