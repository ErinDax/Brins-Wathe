package cn.erindax.brinswathe.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public final class AdminCommands {
    private AdminCommands() {
    }

    static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        FlagCommands.attach(root);
        RoleCommands.attach(root);
        PlayerAdminCommands.attach(root);
        MusicBoxCommands.attach(root);
        RemarkCommands.attach(root);
    }
}
