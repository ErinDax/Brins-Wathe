package cn.erindax.brinswathe.command;

import cn.erindax.brinswathe.HarpyRoles;
import cn.erindax.brinswathe.IcFlags;
import cn.erindax.brinswathe.IcModifiers;
import cn.erindax.brinswathe.Punishment;
import cn.erindax.brinswathe.RoleRotation;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.config.Tunable;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.commands.argument.ModifierArgumentType;
import org.agmas.harpymodloader.commands.argument.RoleArgumentType;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModdedRoleRemoved;
import org.agmas.harpymodloader.modifiers.HMLModifiers;

public final class RoleCommands {
    private static final SimpleCommandExceptionType ROLE_COUNT_UNCHANGED = new SimpleCommandExceptionType(
        Component.translatable("commands.setrolecount.unchanged")
    );
    private static final SimpleCommandExceptionType MODIFIER_ALREADY = new SimpleCommandExceptionType(
        Component.translatable("commands.rolemodifierblacklist.add.unchanged")
    );
    private static final SimpleCommandExceptionType MODIFIER_MISSING = new SimpleCommandExceptionType(
        Component.translatable("commands.rolemodifierblacklist.delete.unchanged")
    );

    private RoleCommands() {
    }

    static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
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
            .then(Commands.literal("error")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showPunishments(context.getSource()))
                .then(Commands.literal("true").executes(context -> setPunishEarlyFall(context.getSource(), true)))
                .then(Commands.literal("false").executes(context -> setPunishEarlyFall(context.getSource(), false)))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                    .executes(context -> queuePunishment(
                        context.getSource(),
                        GameProfileArgument.getGameProfiles(context, "player")
                    ))
                    .then(Commands.literal("cancel")
                        .executes(context -> cancelPunishment(
                            context.getSource(),
                            GameProfileArgument.getGameProfiles(context, "player")
                        )))))
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
                        .executes(context -> setRoleCount(context, ModConfig.HarpyCountKind.NEUTRAL))))
                .then(Commands.literal("killer")
                    .then(Commands.argument("count", IntegerArgumentType.integer())
                        .executes(context -> setRoleCount(context, ModConfig.HarpyCountKind.KILLER))))
                .then(Commands.literal("vigilante")
                    .then(Commands.argument("count", IntegerArgumentType.integer())
                        .executes(context -> setRoleCount(context, ModConfig.HarpyCountKind.VIGILANTE)))))
            .then(Commands.literal("forceRefreshRole")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add")
                    .then(Commands.argument("roles", StringArgumentType.greedyString())
                        .executes(RoleCommands::forceAdd)))
                .then(Commands.literal("remove")
                    .then(Commands.argument("roles", StringArgumentType.greedyString())
                        .executes(RoleCommands::forceRemove)))
                .then(Commands.literal("clear").executes(RoleCommands::forceClear))
                .then(Commands.literal("list").executes(RoleCommands::forceList))
                .then(Commands.argument("roles", StringArgumentType.greedyString())
                    .executes(RoleCommands::forceAdd)))
            .then(Commands.literal("roleModifierBlacklist")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list")
                    .executes(RoleCommands::listAllModifiers)
                    .then(Commands.argument("role", RoleArgumentType.create())
                        .executes(RoleCommands::listRoleModifiers)))
                .then(Commands.literal("add")
                    .then(Commands.argument("role", RoleArgumentType.create())
                        .then(Commands.argument("modifier", ModifierArgumentType.create())
                            .executes(RoleCommands::addBlockedModifier))))
                .then(Commands.literal("delete")
                    .then(Commands.argument("role", RoleArgumentType.create())
                        .then(Commands.argument("modifier", ModifierArgumentType.create())
                            .executes(RoleCommands::deleteBlockedModifier)))));
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

    private static int showPunishments(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(earlyFallStatus()), false);
        List<String> names = Punishment.queuedNames();
        if (names.isEmpty()) {
            source.sendSuccess(() -> Component.literal("惩罚名单为空"), false);
            return 0;
        }
        String joined = String.join(", ", names);
        source.sendSuccess(() -> Component.literal("等待惩罚的玩家: " + joined), false);
        return names.size();
    }

    private static int setPunishEarlyFall(CommandSourceStack source, boolean enabled) {
        IcFlags.punishEarlyFall = enabled;
        IcFlags.save();
        source.sendSuccess(() -> Component.literal(earlyFallStatus()), true);
        return 1;
    }

    private static String earlyFallStatus() {
        return "开局 " + Tunable.EARLY_FALL_SECONDS.intValue() + " 秒内掉出列车, 下一局受惩罚: "
            + (IcFlags.punishEarlyFall ? "开启" : "关闭");
    }

    private static int queuePunishment(CommandSourceStack source, Collection<GameProfile> profiles) {
        if (IcModifiers.PUNISHMENT == null || KinsWatheRoles.DRUGMAKER == null) {
            source.sendFailure(Component.literal("惩罚不可用: 没有找到惩罚词条或制毒师"));
            return 0;
        }
        List<String> added = new ArrayList<>();
        for (GameProfile profile : profiles) {
            if (Punishment.queue(profile.getId(), profile.getName())) added.add(profile.getName());
        }
        if (added.isEmpty()) {
            source.sendFailure(Component.literal("该玩家已在惩罚名单中"));
            return 0;
        }
        String names = String.join(", ", added);
        source.sendSuccess(
            () -> Component.literal("已记入惩罚: " + names + ", 他参加的下一局必定为制毒师, 并全场高亮直至死亡"),
            true
        );
        if (HarpyRoles.isDisabled(KinsWatheRoles.DRUGMAKER)) {
            source.sendSuccess(
                () -> Component.literal("注意: 制毒师已被禁用, 受惩罚的玩家只会当普通杀手").withStyle(ChatFormatting.GOLD),
                false
            );
        }
        return added.size();
    }

    private static int cancelPunishment(CommandSourceStack source, Collection<GameProfile> profiles) {
        List<String> removed = new ArrayList<>();
        for (GameProfile profile : profiles) {
            if (Punishment.cancel(profile.getId())) removed.add(profile.getName());
        }
        if (removed.isEmpty()) {
            source.sendFailure(Component.literal("该玩家不在惩罚名单中"));
            return 0;
        }
        String names = String.join(", ", removed);
        source.sendSuccess(() -> Component.literal("已取消惩罚: " + names), true);
        return removed.size();
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
        RoleRotation.clear();
        Object rounds = roleRoundsMap();
        if (rounds instanceof Map<?, ?> map) map.clear();
        source.sendSuccess(() -> Component.literal("已清空角色轮次权重和防连任记录"), true);
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
        ModConfig.HarpyCountKind kind
    ) throws CommandSyntaxException {
        int newValue = IntegerArgumentType.getInteger(context, "count");
        int oldValue = ModConfig.harpyRoleCount(kind);
        if (oldValue == newValue) throw ROLE_COUNT_UNCHANGED.create();
        try {
            ModConfig.setHarpyRoleCount(kind, newValue);
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
            if (!HarpyRoles.FORCED_REFRESH_ROLES.contains(role)) {
                HarpyRoles.FORCED_REFRESH_ROLES.add(role);
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
            if (HarpyRoles.FORCED_REFRESH_ROLES.remove(role)) removed.add(role);
        }
        MutableComponent roleText = joinRoles(removed);
        context.getSource().sendSuccess(
            () -> Component.translatable("commands.forcerefreshrole.remove.success", roleText),
            true
        );
        return removed.size();
    }

    private static int forceClear(CommandContext<CommandSourceStack> context) {
        int cleared = HarpyRoles.FORCED_REFRESH_ROLES.size();
        HarpyRoles.FORCED_REFRESH_ROLES.clear();
        context.getSource().sendSuccess(
            () -> Component.translatable("commands.forcerefreshrole.clear.success", cleared),
            true
        );
        return cleared;
    }

    private static int forceList(CommandContext<CommandSourceStack> context) {
        List<Role> roles = HarpyRoles.FORCED_REFRESH_ROLES.stream()
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
        Map<String, List<String>> blacklist = ModConfig.harpyModifierBlacklist();
        MutableComponent message = Component.translatable("commands.rolemodifierblacklist.list.title").append("\n");
        if (blacklist.isEmpty()) {
            context.getSource().sendSystemMessage(message.append(Component.translatable("commands.rolemodifierblacklist.list.empty")));
            return 1;
        }
        List<Role> roles = blacklist.keySet().stream()
            .map(RoleCommands::findRole)
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
            ModConfig.harpyModifierBlacklist().getOrDefault(role.identifier().toString(), List.of())
        );
        String modifierId = modifier.identifier().toString();
        if (blocked.contains(modifierId)) throw MODIFIER_ALREADY.create();
        blocked.add(modifierId);
        try {
            ModConfig.setHarpyModifierBlacklist(role.identifier().toString(), blocked);
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
            ModConfig.harpyModifierBlacklist().getOrDefault(role.identifier().toString(), List.of())
        );
        String modifierId = modifier.identifier().toString();
        if (!blocked.remove(modifierId)) throw MODIFIER_MISSING.create();
        try {
            ModConfig.setHarpyModifierBlacklist(role.identifier().toString(), blocked);
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
        List<String> blockedIds = ModConfig.harpyModifierBlacklist().get(role.identifier().toString());
        MutableComponent header = roleText(role).append(Component.literal(" (" + role.identifier() + ")"));
        if (blockedIds == null || blockedIds.isEmpty()) {
            return header.append(": ").append(Component.translatable("commands.rolemodifierblacklist.list.none"));
        }
        List<org.agmas.harpymodloader.modifiers.Modifier> modifiers = blockedIds.stream()
            .map(RoleCommands::findModifier)
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
}
