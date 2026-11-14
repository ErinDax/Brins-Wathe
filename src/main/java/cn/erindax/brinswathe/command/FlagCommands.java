package cn.erindax.brinswathe.command;

import cn.erindax.brinswathe.IcFlags;
import cn.erindax.brinswathe.RoleWeights;
import cn.erindax.brinswathe.network.IcNightVisionS2CPacket;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.aussiebox.starexpress.cca.AllergicComponent;

public final class FlagCommands {
    private FlagCommands() {
    }

    static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        root
            .then(Commands.literal("km")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                    .executes(context -> setKillme(
                        context.getSource(),
                        BoolArgumentType.getBool(context, "enabled")
                    ))))
            .then(Commands.literal("nv")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                    .executes(context -> setNightVision(
                        context.getSource(),
                        BoolArgumentType.getBool(context, "enabled")
                    ))))
            .then(Commands.literal("hudnames")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showHudNamesThroughWalls(context.getSource()))
                .then(Commands.argument("throughWalls", BoolArgumentType.bool())
                    .executes(context -> setHudNamesThroughWalls(
                        context.getSource(),
                        BoolArgumentType.getBool(context, "throughWalls")
                    ))))
            .then(Commands.literal("planb")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                    .executes(context -> setFlag(
                        context.getSource(),
                        "Plan B",
                        enabled -> IcFlags.planB = enabled,
                        BoolArgumentType.getBool(context, "enabled")
                    ))))
            .then(Commands.literal("badguesser")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                    .executes(context -> setFlag(
                        context.getSource(),
                        "Bad Guesser",
                        enabled -> IcFlags.badGuesser = enabled,
                        BoolArgumentType.getBool(context, "enabled")
                    ))))
            .then(Commands.literal("draft")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showDraft(context.getSource()))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                    .executes(context -> setFlag(
                        context.getSource(),
                        "Draft",
                        enabled -> IcFlags.draft = enabled,
                        BoolArgumentType.getBool(context, "enabled")
                    ))))
            .then(Commands.literal("secretmurder")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showSecretMurder(context.getSource()))
                .then(Commands.argument("chance", FloatArgumentType.floatArg(0.0F, 1.0F))
                    .executes(context -> setSecretMurder(
                        context.getSource(),
                        FloatArgumentType.getFloat(context, "chance")
                    ))))
            .then(Commands.literal("weights")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showWeights(context.getSource()))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                    .executes(context -> setWeights(
                        context.getSource(),
                        BoolArgumentType.getBool(context, "enabled")
                    ))))
            .then(Commands.literal("setcd")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showCooldown(context.getSource()))
                .then(Commands.argument("seconds", IntegerArgumentType.integer(0, 300))
                    .executes(context -> setCooldown(
                        context.getSource(),
                        IntegerArgumentType.getInteger(context, "seconds")
                    ))))
            .then(Commands.literal("allergic")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.literal("food").executes(context -> setAllergy(
                        context.getSource(),
                        EntityArgument.getPlayer(context, "player"),
                        "food"
                    )))
                    .then(Commands.literal("drink").executes(context -> setAllergy(
                        context.getSource(),
                        EntityArgument.getPlayer(context, "player"),
                        "drink"
                    )))));
    }

    private static int setKillme(CommandSourceStack source, boolean enabled) {
        IcFlags.allowKillme = enabled;
        IcFlags.save();
        source.sendSuccess(() -> Component.literal("killme 指令: " + (enabled ? "开启" : "关闭")), true);
        return 1;
    }

    private static int setNightVision(CommandSourceStack source, boolean enabled) {
        IcFlags.instinctNightVision = enabled;
        IcFlags.save();
        if (source.getServer() != null) {
            IcNightVisionS2CPacket.sendToAll(source.getServer());
        }
        source.sendSuccess(
            () -> Component.literal("本能夜视: " + (enabled ? "开启" : "关闭")),
            true
        );
        return 1;
    }

    private static int showHudNamesThroughWalls(CommandSourceStack source) {
        boolean current = IcFlags.instinctHudNamesThroughWalls;
        source.sendSuccess(
            () -> Component.literal("本能准星名字隔墙显示: " + (current ? "开启" : "关闭(需视线可见)")),
            false
        );
        return 1;
    }

    private static int setHudNamesThroughWalls(CommandSourceStack source, boolean throughWalls) {
        IcFlags.instinctHudNamesThroughWalls = throughWalls;
        IcFlags.save();
        if (source.getServer() != null) {
            IcNightVisionS2CPacket.sendToAll(source.getServer());
        }
        source.sendSuccess(
            () -> Component.literal("本能准星名字隔墙显示: " + (throughWalls ? "开启" : "关闭(需视线可见)")),
            true
        );
        return 1;
    }

    private static int showWeights(CommandSourceStack source) {
        String description = RoleWeights.describe();
        source.sendSuccess(() -> Component.literal(description), false);
        return 1;
    }

    private static int setWeights(CommandSourceStack source, boolean enabled) {
        RoleWeights.apply(source.getServer(), enabled);
        String description = RoleWeights.describe();
        source.sendSuccess(() -> Component.literal(description), true);
        return 1;
    }

    private static int setFlag(CommandSourceStack source, String name, java.util.function.Consumer<Boolean> setter, boolean enabled) {
        setter.accept(enabled);
        IcFlags.save();
        source.sendSuccess(() -> Component.literal(name + ": " + (enabled ? "Enabled" : "Disabled")), true);
        return 1;
    }

    private static int showDraft(CommandSourceStack source) {
        boolean enabled = IcFlags.draft;
        source.sendSuccess(() -> Component.literal("开局轮抽选角: " + (enabled ? "开启" : "关闭")), false);
        return 1;
    }

    private static int showSecretMurder(CommandSourceStack source) {
        float chance = IcFlags.secretMurderChance;
        source.sendSuccess(() -> Component.literal("秘密谋杀局概率: " + chance), false);
        return 1;
    }

    private static int setSecretMurder(CommandSourceStack source, float chance) {
        IcFlags.secretMurderChance = chance;
        IcFlags.normalize();
        IcFlags.save();
        float applied = IcFlags.secretMurderChance;
        source.sendSuccess(() -> Component.literal("秘密谋杀局概率已设置为 " + applied), true);
        return 1;
    }

    private static int showCooldown(CommandSourceStack source) {
        int current = IcFlags.resetItemsCooldownSeconds;
        source.sendSuccess(() -> Component.literal("当前重置物品冷却: " + current + " 秒"), false);
        return 1;
    }

    private static int setCooldown(CommandSourceStack source, int seconds) {
        IcFlags.resetItemsCooldownSeconds = seconds;
        IcFlags.save();
        source.sendSuccess(() -> Component.literal("重置物品冷却时间已设置为 " + seconds + " 秒"), true);
        return 1;
    }

    private static int setAllergy(CommandSourceStack source, ServerPlayer target, String type) {
        AllergicComponent allergic = AllergicComponent.KEY.get(target);
        if (allergic == null || !allergic.isAllergic()) {
            source.sendFailure(Component.translatable("command.starexpress.allergic.not_allergic", target.getDisplayName()));
            return 0;
        }
        try {
            try {
                allergic.getClass().getMethod("setAllergyType", String.class).invoke(allergic, type);
            } catch (NoSuchMethodException exception) {
                var field = allergic.getClass().getDeclaredField("allergyType");
                field.setAccessible(true);
                field.set(allergic, type);
                allergic.getClass().getMethod("sync").invoke(allergic);
            }
        } catch (ReflectiveOperationException exception) {
            source.sendFailure(Component.literal("无法写入过敏类型"));
            return 0;
        }
        source.sendSuccess(
            () -> Component.translatable(
                "command.starexpress.allergic.updated",
                target.getDisplayName(),
                Component.translatable("hud.allergic.type." + type)
            ),
            true
        );
        return 1;
    }
}
