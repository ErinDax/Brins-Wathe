package cn.erindax.brinswathe;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public final class RoleWeights {
    private RoleWeights() {
    }

    public static void apply(MinecraftServer server, boolean enabled) {
        IcFlags.roleWeights = enabled;
        IcFlags.save();
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
        String description = describe();
        source.sendSuccess(() -> Component.literal(description), true);
        return 1;
    }

    public static String describe() {
        if (IcFlags.roleWeights) return "原版权重: 开启（按历史担任次数降权），防连任降权: 停用";
        if (!IcFlags.roleRepeatGuard) return "原版权重: 关闭，防连任降权: 关闭，当前为纯随机";
        String memory = IcFlags.roleRepeatDecay <= 0.0F
            ? "只记上一局"
            : "记忆衰减 " + IcFlags.roleRepeatDecay;
        String streak = IcFlags.roleRepeatMaxStreak <= 0
            ? "不限连任"
            : "最多连任 " + IcFlags.roleRepeatMaxStreak + " 局";
        return "原版权重: 关闭，当前生效: 防连任降权（强度 " + IcFlags.roleRepeatStrength
            + "，" + memory + "，" + streak + "）";
    }
}
