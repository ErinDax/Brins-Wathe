package cn.erindax.brinswathe;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public final class BrinRoleWeights {
    private BrinRoleWeights() {
    }

    public static void apply(MinecraftServer server, boolean enabled) {
        BrinIcFlags.roleWeights = enabled;
        BrinIcFlags.save();
        if (server == null) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(server.overworld());
        game.setWeightsEnabled(enabled);
        game.sync();
        ScoreboardRoleSelectorComponent.KEY.get(server.getScoreboard()).reset();
    }

    public static int applyFromCommand(CommandContext<?> context) {
        CommandSourceStack source = (CommandSourceStack) context.getSource();
        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        apply(source.getServer(), enabled);
        source.sendSuccess(
            () -> Component.literal("角色权重: " + (enabled ? "开启" : "关闭（纯随机）")),
            true
        );
        return 1;
    }
}
