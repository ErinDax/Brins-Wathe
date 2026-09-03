package cn.erindax.brinswathe.command;

import cn.erindax.brinswathe.BrinHarpyRoles;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.doctor4t.wathe.api.Role;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.commands.argument.RoleArgumentType;

public final class ForceRefreshRoleCommand {
    private ForceRefreshRoleCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("forceRefreshRole")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add")
                    .then(Commands.argument("roles", StringArgumentType.greedyString())
                        .executes(ForceRefreshRoleCommand::add)))
                .then(Commands.literal("remove")
                    .then(Commands.argument("roles", StringArgumentType.greedyString())
                        .executes(ForceRefreshRoleCommand::remove)))
                .then(Commands.literal("clear").executes(ForceRefreshRoleCommand::clear))
                .then(Commands.literal("list").executes(ForceRefreshRoleCommand::list))
                .then(Commands.argument("roles", StringArgumentType.greedyString())
                    .executes(ForceRefreshRoleCommand::add))
        );
    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Role> roles = parseRoles(StringArgumentType.getString(context, "roles"));
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

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Role> roles = parseRoles(StringArgumentType.getString(context, "roles"));
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

    private static int clear(CommandContext<CommandSourceStack> context) {
        int cleared = BrinHarpyRoles.FORCED_REFRESH_ROLES.size();
        BrinHarpyRoles.FORCED_REFRESH_ROLES.clear();
        context.getSource().sendSuccess(
            () -> Component.translatable("commands.forcerefreshrole.clear.success", cleared),
            true
        );
        return cleared;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        List<Role> roles = BrinHarpyRoles.FORCED_REFRESH_ROLES.stream()
            .sorted(Comparator.comparing(role -> role.identifier().toString()))
            .toList();
        MutableComponent message = Component.translatable("commands.forcerefreshrole.list.title").append("\n");
        if (roles.isEmpty()) {
            context.getSource().sendSystemMessage(message.append(Component.translatable("commands.forcerefreshrole.list.empty")));
            return 1;
        }
        context.getSource().sendSystemMessage(message.append(joinLines(roles)));
        return 1;
    }

    private static List<Role> parseRoles(String input) throws CommandSyntaxException {
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

    private static MutableComponent joinLines(List<Role> roles) {
        MutableComponent result = Component.empty();
        for (int index = 0; index < roles.size(); index++) {
            if (index > 0) result.append(Component.literal("\n"));
            Role role = roles.get(index);
            result.append(roleText(role)).append(Component.literal(" (" + role.identifier() + ")"));
        }
        return result;
    }

    private static MutableComponent roleText(Role role) {
        return Harpymodloader.getRoleName(role)
            .withColor(role.color())
            .withStyle(style -> style.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(role.identifier().toString()))
            ));
    }
}
