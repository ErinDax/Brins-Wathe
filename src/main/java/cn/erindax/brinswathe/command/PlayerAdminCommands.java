package cn.erindax.brinswathe.command;

import cn.erindax.brinswathe.IcFlags;
import cn.erindax.brinswathe.NoelleAccess;
import cn.erindax.brinswathe.component.StaminaComponent;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class PlayerAdminCommands {
    private PlayerAdminCommands() {
    }

    static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        root
            .then(Commands.literal("setplayer")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.literal("armor")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                            .executes(context -> setArmor(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player"),
                                IntegerArgumentType.getInteger(context, "value")
                            ))))
                    .then(Commands.literal("psycho")
                        .then(Commands.argument("ticks_upper_limit", IntegerArgumentType.integer(0))
                            .then(Commands.argument("armour_value", IntegerArgumentType.integer())
                                .executes(context -> setPsycho(
                                    context.getSource(),
                                    EntityArgument.getPlayer(context, "player"),
                                    IntegerArgumentType.getInteger(context, "ticks_upper_limit"),
                                    IntegerArgumentType.getInteger(context, "armour_value")
                                )))))
                    .then(Commands.literal("cooldown")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(0))
                            .executes(context -> setAbilityCooldown(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player"),
                                IntegerArgumentType.getInteger(context, "ticks")
                            ))))
                    .then(Commands.literal("cd")
                        .then(Commands.argument("item", StringArgumentType.string())
                            .suggests((context, builder) ->
                                SharedSuggestionProvider.suggest(IcFlags.resetItemsList, builder))
                            .then(Commands.argument("ticks", IntegerArgumentType.integer(0))
                                .executes(context -> setItemCooldown(
                                    context.getSource(),
                                    EntityArgument.getPlayer(context, "player"),
                                    StringArgumentType.getString(context, "item"),
                                    IntegerArgumentType.getInteger(context, "ticks")
                                )))))))
            .then(Commands.literal("setRecover")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("value", FloatArgumentType.floatArg(1.0F, 1000.0F))
                    .executes(context -> setMaxStamina(
                        context.getSource(),
                        (int) FloatArgumentType.getFloat(context, "value")
                    ))))
            .then(Commands.literal("setMaxSprintingTicks")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0F, 100.0F))
                    .executes(context -> setRegenRate(
                        context.getSource(),
                        (int) FloatArgumentType.getFloat(context, "value")
                    ))))
            .then(Commands.literal("setMinSprintingTicks")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0F, 10.0F))
                    .executes(context -> setRunSpeed(
                        context.getSource(),
                        FloatArgumentType.getFloat(context, "value")
                    ))))
            .then(Commands.literal("setbrinspeed")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload")
                    .executes(ctx -> resetGlobalSettings(ctx.getSource())))
                .then(Commands.literal("maxStamina")
                    .then(Commands.argument("value", IntegerArgumentType.integer(1, 1000))
                        .executes(ctx -> setGlobalMaxStamina(
                            ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))
                .then(Commands.literal("runSpeed")
                    .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 10.0f))
                        .executes(ctx -> setGlobalRunSpeed(
                            ctx.getSource(), FloatArgumentType.getFloat(ctx, "value")))))
                .then(Commands.literal("regenRate")
                    .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                        .executes(ctx -> setGlobalRegenRate(
                            ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.literal("maxStamina")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 1000))
                            .executes(ctx -> setGlobalMaxStamina(
                                ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))
                    .then(Commands.literal("runSpeed")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 10.0f))
                            .executes(ctx -> setGlobalRunSpeed(
                                ctx.getSource(), FloatArgumentType.getFloat(ctx, "value")))))
                    .then(Commands.literal("regenRate")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                            .executes(ctx -> setGlobalRegenRate(
                                ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))));
    }

    private static int setArmor(CommandSourceStack source, ServerPlayer player, int value) {
        NoelleAccess.setBartenderArmor(player, value);
        source.sendSuccess(
            () -> Component.literal("已为 " + player.getName().getString() + " 设置护甲: " + value)
                .withStyle(ChatFormatting.GREEN),
            true
        );
        player.sendSystemMessage(Component.literal("你的护甲被管理员设置为 " + value).withStyle(ChatFormatting.GOLD));
        return 1;
    }

    private static int setPsycho(CommandSourceStack source, ServerPlayer player, int seconds, int armour) {
        PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(player);
        if (psycho == null) {
            source.sendFailure(Component.literal("无法读取精神错乱组件"));
            return 0;
        }
        if (psycho.getPsychoTicks() > 0) {
            source.sendFailure(Component.literal(player.getName().getString() + " 已处于疯魔中."));
            return 0;
        }
        if (!psycho.startPsycho()) {
            source.sendFailure(Component.literal("无法触发 " + player.getName().getString() + " 的精神错乱"));
            return 0;
        }
        psycho.setPsychoTicks(GameConstants.getInTicks(0, seconds));
        psycho.setArmour(armour);
        source.sendSuccess(
            () -> Component.literal("已触发 " + player.getName().getString() + " 的精神错乱: 持续~" + seconds + "秒, 护甲=" + armour)
                .withStyle(ChatFormatting.RED),
            true
        );
        player.sendSystemMessage(Component.literal("你进入了疯魔状态!").withStyle(ChatFormatting.DARK_RED));
        return 1;
    }

    private static int setAbilityCooldown(CommandSourceStack source, ServerPlayer player, int ticks) {
        NoelleAccess.setNoelleAbilityCooldown(player, ticks);
        boolean stupid = NoelleAccess.setStupidAbilityCooldown(player, ticks);
        source.sendSuccess(
            () -> Component.literal(
                "已为 " + player.getName().getString() + " 设置技能冷却 " + ticks + " ticks"
                    + (stupid ? "" : "(Stupid 冷却组件不可用)")
            ),
            true
        );
        return 1;
    }

    private static int setItemCooldown(CommandSourceStack source, ServerPlayer player, String itemId, int ticks) {
        ResourceLocation id;
        try {
            id = ResourceLocation.parse(itemId);
        } catch (Exception exception) {
            source.sendFailure(Component.literal("无效的物品ID: " + itemId));
            return 0;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) {
            source.sendFailure(Component.literal("物品不存在: " + itemId));
            return 0;
        }
        player.getCooldowns().addCooldown(item, ticks);
        source.sendSuccess(
            () -> Component.literal("已为 " + player.getName().getString() + " 设置 " + itemId + " 的冷却: " + ticks + " ticks"),
            true
        );
        return 1;
    }

    private static int setMaxStamina(CommandSourceStack source, int value) {
        StaminaComponent.setGlobalMaxStamina(value);
        applyStamina(source);
        source.sendSuccess(() -> Component.literal("已将体力上限设置为 " + value).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int setRegenRate(CommandSourceStack source, int value) {
        StaminaComponent.setGlobalRegenRate(value);
        applyStamina(source);
        source.sendSuccess(() -> Component.literal("已将体力回复设置为 " + value + "/s").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int setRunSpeed(CommandSourceStack source, float value) {
        StaminaComponent.setGlobalRunSpeed(value);
        applyStamina(source);
        source.sendSuccess(() -> Component.literal("已将奔跑速度设置为 " + value).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static void applyStamina(CommandSourceStack source) {
        if (source.getServer() == null) return;
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            StaminaComponent component = StaminaComponent.KEY.get(player);
            if (component != null) component.applyGlobalOverrides();
        }
    }

    private static int setGlobalMaxStamina(CommandSourceStack source, int value) {
        StaminaComponent.setGlobalMaxStamina(value);
        applyGlobalSettings(source);
        source.sendSuccess(() -> Component.literal("全局体力上限已设为 " + value), true);
        return 1;
    }

    private static int resetGlobalSettings(CommandSourceStack source) {
        StaminaComponent.clearGlobalOverrides();
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            StaminaComponent component = StaminaComponent.KEY.get(player);
            if (component != null) component.resetToInitialSettings();
        }
        source.sendSuccess(() -> Component.literal("全局体力设置已恢复默认"), true);
        return 1;
    }

    private static int setGlobalRunSpeed(CommandSourceStack source, float value) {
        StaminaComponent.setGlobalRunSpeed(value);
        applyGlobalSettings(source);
        source.sendSuccess(() -> Component.literal("全局奔跑速度已设为 " + value), true);
        return 1;
    }

    private static int setGlobalRegenRate(CommandSourceStack source, int value) {
        StaminaComponent.setGlobalRegenRate(value);
        applyGlobalSettings(source);
        source.sendSuccess(() -> Component.literal("全局体力回复已设为 " + value + "/秒"), true);
        return 1;
    }

    private static void applyGlobalSettings(CommandSourceStack source) {
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            StaminaComponent component = StaminaComponent.KEY.get(player);
            if (component != null) component.applyGlobalOverrides();
        }
    }
}
