package cn.erindax.brinswathe.command;

import cn.erindax.brinswathe.BrinHarpyRoles;
import cn.erindax.brinswathe.BrinIcFlags;
import cn.erindax.brinswathe.BrinNoelleAccess;
import cn.erindax.brinswathe.BrinRoleWeights;
import cn.erindax.brinswathe.component.StaminaComponent;
import cn.erindax.brinswathe.config.BrinConfig;
import cn.erindax.brinswathe.network.BrinIcNightVisionS2CPacket;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameConstants;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.commands.argument.ModifierArgumentType;
import org.agmas.harpymodloader.commands.argument.RoleArgumentType;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModdedRoleRemoved;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.aussiebox.starexpress.cca.AllergicComponent;

public final class BrinAdminCommands {
    private static final SimpleCommandExceptionType ROLE_COUNT_UNCHANGED = new SimpleCommandExceptionType(
        Component.translatable("commands.setrolecount.unchanged")
    );
    private static final SimpleCommandExceptionType MODIFIER_ALREADY = new SimpleCommandExceptionType(
        Component.translatable("commands.rolemodifierblacklist.add.unchanged")
    );
    private static final SimpleCommandExceptionType MODIFIER_MISSING = new SimpleCommandExceptionType(
        Component.translatable("commands.rolemodifierblacklist.delete.unchanged")
    );

    private BrinAdminCommands() {
    }

    static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        attachFlags(root);
        attachRoles(root);
        attachPlayers(root);
    }

    private static void attachFlags(LiteralArgumentBuilder<CommandSourceStack> root) {
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
                        enabled -> BrinIcFlags.planB = enabled,
                        BoolArgumentType.getBool(context, "enabled")
                    ))))
            .then(Commands.literal("badguesser")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                    .executes(context -> setFlag(
                        context.getSource(),
                        "Bad Guesser",
                        enabled -> BrinIcFlags.badGuesser = enabled,
                        BoolArgumentType.getBool(context, "enabled")
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

    private static void attachRoles(LiteralArgumentBuilder<CommandSourceStack> root) {
        root
            .then(Commands.literal("setnow")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.literal("set")
                        .then(Commands.argument("role", RoleArgumentType.create())
                            .executes(context -> setNow(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player"),
                                RoleArgumentType.getRole(context, "role"),
                                true
                            ))))
                    .then(Commands.literal("remove")
                        .then(Commands.argument("role", RoleArgumentType.create())
                            .executes(context -> setNow(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player"),
                                RoleArgumentType.getRole(context, "role"),
                                false
                            ))))))
            .then(Commands.literal("roleRoundsclear")
                .requires(source -> source.hasPermission(2))
                .executes(context -> clearRoleRounds(context.getSource())))
            .then(Commands.literal("PrintRounds")
                .requires(source -> source.hasPermission(2))
                .executes(context -> printRoleRounds(context.getSource())))
            .then(Commands.literal("setRoleCount")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("neutral")
                    .then(Commands.argument("count", IntegerArgumentType.integer())
                        .executes(context -> setRoleCount(context, BrinConfig.HarpyCountKind.NEUTRAL))))
                .then(Commands.literal("killer")
                    .then(Commands.argument("count", IntegerArgumentType.integer())
                        .executes(context -> setRoleCount(context, BrinConfig.HarpyCountKind.KILLER))))
                .then(Commands.literal("vigilante")
                    .then(Commands.argument("count", IntegerArgumentType.integer())
                        .executes(context -> setRoleCount(context, BrinConfig.HarpyCountKind.VIGILANTE)))))
            .then(Commands.literal("forceRefreshRole")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add")
                    .then(Commands.argument("roles", StringArgumentType.greedyString())
                        .executes(BrinAdminCommands::forceAdd)))
                .then(Commands.literal("remove")
                    .then(Commands.argument("roles", StringArgumentType.greedyString())
                        .executes(BrinAdminCommands::forceRemove)))
                .then(Commands.literal("clear").executes(BrinAdminCommands::forceClear))
                .then(Commands.literal("list").executes(BrinAdminCommands::forceList))
                .then(Commands.argument("roles", StringArgumentType.greedyString())
                    .executes(BrinAdminCommands::forceAdd)))
            .then(Commands.literal("roleModifierBlacklist")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list")
                    .executes(BrinAdminCommands::listAllModifiers)
                    .then(Commands.argument("role", RoleArgumentType.create())
                        .executes(BrinAdminCommands::listRoleModifiers)))
                .then(Commands.literal("add")
                    .then(Commands.argument("role", RoleArgumentType.create())
                        .then(Commands.argument("modifier", ModifierArgumentType.create())
                            .executes(BrinAdminCommands::addBlockedModifier))))
                .then(Commands.literal("delete")
                    .then(Commands.argument("role", RoleArgumentType.create())
                        .then(Commands.argument("modifier", ModifierArgumentType.create())
                            .executes(BrinAdminCommands::deleteBlockedModifier)))));
    }

    private static void attachPlayers(LiteralArgumentBuilder<CommandSourceStack> root) {
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
                                SharedSuggestionProvider.suggest(BrinIcFlags.resetItemsList, builder))
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

    private static int setKillme(CommandSourceStack source, boolean enabled) {
        BrinIcFlags.allowKillme = enabled;
        BrinIcFlags.save();
        source.sendSuccess(() -> Component.literal("killme 指令: " + (enabled ? "开启" : "关闭")), true);
        return 1;
    }

    private static int setNightVision(CommandSourceStack source, boolean enabled) {
        BrinIcFlags.instinctNightVision = enabled;
        BrinIcFlags.save();
        if (source.getServer() != null) {
            BrinIcNightVisionS2CPacket.sendToAll(source.getServer());
        }
        source.sendSuccess(
            () -> Component.literal("本能夜视: " + (enabled ? "开启" : "关闭")),
            true
        );
        return 1;
    }

    private static int showHudNamesThroughWalls(CommandSourceStack source) {
        boolean current = BrinIcFlags.instinctHudNamesThroughWalls;
        source.sendSuccess(
            () -> Component.literal("本能准星名字隔墙显示: " + (current ? "开启" : "关闭(需视线可见)")),
            false
        );
        return 1;
    }

    private static int setHudNamesThroughWalls(CommandSourceStack source, boolean throughWalls) {
        BrinIcFlags.instinctHudNamesThroughWalls = throughWalls;
        BrinIcFlags.save();
        if (source.getServer() != null) {
            BrinIcNightVisionS2CPacket.sendToAll(source.getServer());
        }
        source.sendSuccess(
            () -> Component.literal("本能准星名字隔墙显示: " + (throughWalls ? "开启" : "关闭(需视线可见)")),
            true
        );
        return 1;
    }

    private static int showWeights(CommandSourceStack source) {
        source.sendSuccess(
            () -> Component.literal("角色权重: " + (BrinIcFlags.roleWeights ? "开启" : "关闭(纯随机)")),
            false
        );
        return 1;
    }

    private static int setWeights(CommandSourceStack source, boolean enabled) {
        BrinRoleWeights.apply(source.getServer(), enabled);
        source.sendSuccess(
            () -> Component.literal("角色权重: " + (enabled ? "开启" : "关闭(纯随机)")),
            true
        );
        return 1;
    }

    private static int setFlag(CommandSourceStack source, String name, java.util.function.Consumer<Boolean> setter, boolean enabled) {
        setter.accept(enabled);
        BrinIcFlags.save();
        source.sendSuccess(() -> Component.literal(name + ": " + (enabled ? "Enabled" : "Disabled")), true);
        return 1;
    }

    private static int showCooldown(CommandSourceStack source) {
        int current = BrinIcFlags.resetItemsCooldownSeconds;
        source.sendSuccess(() -> Component.literal("当前重置物品冷却: " + current + " 秒"), false);
        return 1;
    }

    private static int setCooldown(CommandSourceStack source, int seconds) {
        BrinIcFlags.resetItemsCooldownSeconds = seconds;
        BrinIcFlags.save();
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

    private static int setNow(CommandSourceStack source, ServerPlayer player, Role role, boolean add) {
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        if (add) {
            game.addRole(player, role);
            ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, role);
            source.sendSuccess(
                () -> Component.literal("已为 " + player.getName().getString() + " 设置职业: " + role.identifier().getPath())
                    .withStyle(ChatFormatting.GREEN),
                true
            );
            player.sendSystemMessage(
                Component.literal("你的职业已被设置为: " + role.identifier().getPath()).withStyle(ChatFormatting.GOLD)
            );
        } else {
            game.addRole(player, WatheRoles.CIVILIAN);
            ModdedRoleRemoved.EVENT.invoker().removeModdedRole(player, role);
            source.sendSuccess(
                () -> Component.literal("已为 " + player.getName().getString() + " 删除职业: " + role.identifier().getPath())
                    .withStyle(ChatFormatting.RED),
                true
            );
            player.sendSystemMessage(
                Component.literal("你的职业 '" + role.identifier().getPath() + "' 已被移除").withStyle(ChatFormatting.GRAY)
            );
        }
        return 1;
    }

    private static Object roleRoundsMap() {
        try {
            Field field = Class.forName("org.agmas.harpymodloader.modded_murder.ModdedWeights")
                .getField("roleRounds");
            return field.get(null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static int clearRoleRounds(CommandSourceStack source) {
        Object rounds = roleRoundsMap();
        if (!(rounds instanceof Map<?, ?> map)) {
            source.sendFailure(Component.literal("官包 Harpy 没有 roleRounds，无法清空"));
            return 0;
        }
        map.clear();
        source.sendSuccess(() -> Component.literal("已清空角色轮次权重记录"), true);
        return 1;
    }

    private static int printRoleRounds(CommandSourceStack source) {
        Object rounds = roleRoundsMap();
        if (rounds == null) {
            source.sendFailure(Component.literal("官包 Harpy 没有 roleRounds"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(String.valueOf(rounds)), false);
        return 1;
    }

    private static int setRoleCount(
        CommandContext<CommandSourceStack> context,
        BrinConfig.HarpyCountKind kind
    ) throws CommandSyntaxException {
        int newValue = IntegerArgumentType.getInteger(context, "count");
        int oldValue = BrinConfig.harpyRoleCount(kind);
        if (oldValue == newValue) throw ROLE_COUNT_UNCHANGED.create();
        try {
            BrinConfig.setHarpyRoleCount(kind, newValue);
        } catch (Exception exception) {
            context.getSource().sendFailure(Component.literal("角色数量保存失败: " + exception.getMessage()));
            return 0;
        }
        context.getSource().sendSuccess(
            () -> Component.translatable(kind.successKey(), formatRoleCount(newValue)),
            true
        );
        return 1;
    }

    private static MutableComponent formatRoleCount(int value) {
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

    private static int forceAdd(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Role> roles = parseForcedRoles(StringArgumentType.getString(context, "roles"));
        for (Role role : roles) {
            if (!BrinHarpyRoles.FORCED_REFRESH_ROLES.contains(role)) {
                BrinHarpyRoles.FORCED_REFRESH_ROLES.add(role);
            }
        }
        MutableComponent roleText = joinRoles(roles);
        context.getSource().sendSuccess(
            () -> Component.translatable("commands.forcerefreshrole.add.success", roleText),
            true
        );
        return 1;
    }

    private static int forceRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Role> roles = parseForcedRoles(StringArgumentType.getString(context, "roles"));
        List<Role> removed = new ArrayList<>();
        for (Role role : roles) {
            if (BrinHarpyRoles.FORCED_REFRESH_ROLES.remove(role)) removed.add(role);
        }
        MutableComponent roleText = joinRoles(removed);
        context.getSource().sendSuccess(
            () -> Component.translatable("commands.forcerefreshrole.remove.success", roleText),
            true
        );
        return removed.size();
    }

    private static int forceClear(CommandContext<CommandSourceStack> context) {
        int cleared = BrinHarpyRoles.FORCED_REFRESH_ROLES.size();
        BrinHarpyRoles.FORCED_REFRESH_ROLES.clear();
        context.getSource().sendSuccess(
            () -> Component.translatable("commands.forcerefreshrole.clear.success", cleared),
            true
        );
        return cleared;
    }

    private static int forceList(CommandContext<CommandSourceStack> context) {
        List<Role> roles = BrinHarpyRoles.FORCED_REFRESH_ROLES.stream()
            .sorted(Comparator.comparing(role -> role.identifier().toString()))
            .toList();
        MutableComponent message = Component.translatable("commands.forcerefreshrole.list.title").append("\n");
        if (roles.isEmpty()) {
            context.getSource().sendSystemMessage(message.append(Component.translatable("commands.forcerefreshrole.list.empty")));
            return 1;
        }
        context.getSource().sendSystemMessage(message.append(joinRoleLines(roles)));
        return 1;
    }

    private static List<Role> parseForcedRoles(String input) throws CommandSyntaxException {
        List<Role> roles = new ArrayList<>();
        for (String token : input.split("[\\s,]+")) {
            if (token.isBlank()) continue;
            Role role = RoleArgumentType.skipVanilla().parse(new StringReader(token));
            if (!roles.contains(role)) roles.add(role);
        }
        return roles;
    }

    private static MutableComponent joinRoles(List<Role> roles) {
        MutableComponent result = Component.empty();
        for (int index = 0; index < roles.size(); index++) {
            if (index > 0) result.append(Component.literal(", "));
            result.append(roleText(roles.get(index)));
        }
        return result;
    }

    private static MutableComponent joinRoleLines(List<Role> roles) {
        MutableComponent result = Component.empty();
        for (int index = 0; index < roles.size(); index++) {
            if (index > 0) result.append(Component.literal("\n"));
            Role role = roles.get(index);
            result.append(roleText(role)).append(Component.literal(" (" + role.identifier() + ")"));
        }
        return result;
    }

    private static int listAllModifiers(CommandContext<CommandSourceStack> context) {
        Map<String, List<String>> blacklist = BrinConfig.harpyModifierBlacklist();
        MutableComponent message = Component.translatable("commands.rolemodifierblacklist.list.title").append("\n");
        if (blacklist.isEmpty()) {
            context.getSource().sendSystemMessage(message.append(Component.translatable("commands.rolemodifierblacklist.list.empty")));
            return 1;
        }
        List<Role> roles = blacklist.keySet().stream()
            .map(BrinAdminCommands::findRole)
            .filter(role -> role != null)
            .sorted(Comparator.comparing(role -> role.identifier().toString()))
            .toList();
        MutableComponent body = Component.empty();
        for (int index = 0; index < roles.size(); index++) {
            if (index > 0) body.append(Component.literal("\n"));
            body.append(buildModifierLine(roles.get(index)));
        }
        context.getSource().sendSystemMessage(message.append(body));
        return 1;
    }

    private static int listRoleModifiers(CommandContext<CommandSourceStack> context) {
        Role role = RoleArgumentType.getRole(context, "role");
        context.getSource().sendSystemMessage(buildModifierLine(role));
        return 1;
    }

    private static int addBlockedModifier(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Role role = RoleArgumentType.getRole(context, "role");
        var modifier = ModifierArgumentType.getModifier(context, "modifier");
        List<String> blocked = new ArrayList<>(
            BrinConfig.harpyModifierBlacklist().getOrDefault(role.identifier().toString(), List.of())
        );
        String modifierId = modifier.identifier().toString();
        if (blocked.contains(modifierId)) throw MODIFIER_ALREADY.create();
        blocked.add(modifierId);
        try {
            BrinConfig.setHarpyModifierBlacklist(role.identifier().toString(), blocked);
        } catch (Exception exception) {
            context.getSource().sendFailure(Component.literal("黑名单保存失败: " + exception.getMessage()));
            return 0;
        }
        context.getSource().sendSuccess(
            () -> Component.translatable(
                "commands.rolemodifierblacklist.add.success",
                roleText(role),
                modifierText(modifier)
            ),
            true
        );
        return 1;
    }

    private static int deleteBlockedModifier(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Role role = RoleArgumentType.getRole(context, "role");
        var modifier = ModifierArgumentType.getModifier(context, "modifier");
        List<String> blocked = new ArrayList<>(
            BrinConfig.harpyModifierBlacklist().getOrDefault(role.identifier().toString(), List.of())
        );
        String modifierId = modifier.identifier().toString();
        if (!blocked.remove(modifierId)) throw MODIFIER_MISSING.create();
        try {
            BrinConfig.setHarpyModifierBlacklist(role.identifier().toString(), blocked);
        } catch (Exception exception) {
            context.getSource().sendFailure(Component.literal("黑名单保存失败: " + exception.getMessage()));
            return 0;
        }
        context.getSource().sendSuccess(
            () -> Component.translatable(
                "commands.rolemodifierblacklist.delete.success",
                roleText(role),
                modifierText(modifier)
            ),
            true
        );
        return 1;
    }

    private static MutableComponent buildModifierLine(Role role) {
        List<String> blockedIds = BrinConfig.harpyModifierBlacklist().get(role.identifier().toString());
        MutableComponent header = roleText(role).append(Component.literal(" (" + role.identifier() + ")"));
        if (blockedIds == null || blockedIds.isEmpty()) {
            return header.append(": ").append(Component.translatable("commands.rolemodifierblacklist.list.none"));
        }
        List<org.agmas.harpymodloader.modifiers.Modifier> modifiers = blockedIds.stream()
            .map(BrinAdminCommands::findModifier)
            .filter(modifier -> modifier != null)
            .sorted(Comparator.comparing(modifier -> modifier.identifier().toString()))
            .toList();
        if (modifiers.isEmpty()) {
            return header.append(": ").append(Component.translatable("commands.rolemodifierblacklist.list.none"));
        }
        MutableComponent joined = Component.empty();
        for (int index = 0; index < modifiers.size(); index++) {
            if (index > 0) joined.append(Component.literal(", "));
            joined.append(modifierText(modifiers.get(index)));
        }
        return header.append(": ").append(joined);
    }

    private static Role findRole(String roleId) {
        for (Role role : WatheRoles.ROLES) {
            if (role.identifier().toString().equals(roleId)) return role;
        }
        return null;
    }

    private static org.agmas.harpymodloader.modifiers.Modifier findModifier(String modifierId) {
        for (org.agmas.harpymodloader.modifiers.Modifier modifier : HMLModifiers.MODIFIERS) {
            if (modifier.identifier().toString().equals(modifierId)) return modifier;
        }
        return null;
    }

    private static MutableComponent roleText(Role role) {
        return Harpymodloader.getRoleName(role)
            .withColor(role.color())
            .withStyle(style -> style.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(role.identifier().toString()))
            ));
    }

    private static MutableComponent modifierText(org.agmas.harpymodloader.modifiers.Modifier modifier) {
        return modifier.getName(true)
            .withStyle(style -> style.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(modifier.identifier().toString()))
            ));
    }

    private static int setArmor(CommandSourceStack source, ServerPlayer player, int value) {
        BrinNoelleAccess.setBartenderArmor(player, value);
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
        BrinNoelleAccess.setNoelleAbilityCooldown(player, ticks);
        boolean stupid = BrinNoelleAccess.setStupidAbilityCooldown(player, ticks);
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
