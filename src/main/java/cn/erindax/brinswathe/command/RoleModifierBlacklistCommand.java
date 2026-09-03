package cn.erindax.brinswathe.command;

import cn.erindax.brinswathe.config.BrinConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.commands.argument.ModifierArgumentType;
import org.agmas.harpymodloader.commands.argument.RoleArgumentType;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;

public final class RoleModifierBlacklistCommand {
    private static final SimpleCommandExceptionType ALREADY_EXISTS = new SimpleCommandExceptionType(
        Component.translatable("commands.rolemodifierblacklist.add.unchanged")
    );
    private static final SimpleCommandExceptionType NOT_FOUND = new SimpleCommandExceptionType(
        Component.translatable("commands.rolemodifierblacklist.delete.unchanged")
    );

    private RoleModifierBlacklistCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("roleModifierBlacklist")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list")
                    .executes(RoleModifierBlacklistCommand::listAll)
                    .then(Commands.argument("role", RoleArgumentType.create())
                        .executes(RoleModifierBlacklistCommand::listRole)))
                .then(Commands.literal("add")
                    .then(Commands.argument("role", RoleArgumentType.create())
                        .then(Commands.argument("modifier", ModifierArgumentType.create())
                            .executes(RoleModifierBlacklistCommand::add))))
                .then(Commands.literal("delete")
                    .then(Commands.argument("role", RoleArgumentType.create())
                        .then(Commands.argument("modifier", ModifierArgumentType.create())
                            .executes(RoleModifierBlacklistCommand::delete))))
        );
    }

    private static int listAll(CommandContext<CommandSourceStack> context) {
        Map<String, List<String>> blacklist = BrinConfig.harpyModifierBlacklist();
        MutableComponent message = Component.translatable("commands.rolemodifierblacklist.list.title").append("\n");
        if (blacklist.isEmpty()) {
            context.getSource().sendSystemMessage(message.append(Component.translatable("commands.rolemodifierblacklist.list.empty")));
            return 1;
        }
        List<Role> roles = blacklist.keySet().stream()
            .map(RoleModifierBlacklistCommand::findRole)
            .filter(role -> role != null)
            .sorted(Comparator.comparing(role -> role.identifier().toString()))
            .toList();
        MutableComponent body = Component.empty();
        for (int index = 0; index < roles.size(); index++) {
            if (index > 0) body.append(Component.literal("\n"));
            body.append(buildRoleLine(roles.get(index)));
        }
        context.getSource().sendSystemMessage(message.append(body));
        return 1;
    }

    private static int listRole(CommandContext<CommandSourceStack> context) {
        Role role = RoleArgumentType.getRole(context, "role");
        context.getSource().sendSystemMessage(buildRoleLine(role));
        return 1;
    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Role role = RoleArgumentType.getRole(context, "role");
        Modifier modifier = ModifierArgumentType.getModifier(context, "modifier");
        List<String> blocked = new ArrayList<>(
            BrinConfig.harpyModifierBlacklist().getOrDefault(role.identifier().toString(), List.of())
        );
        String modifierId = modifier.identifier().toString();
        if (blocked.contains(modifierId)) throw ALREADY_EXISTS.create();
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

    private static int delete(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Role role = RoleArgumentType.getRole(context, "role");
        Modifier modifier = ModifierArgumentType.getModifier(context, "modifier");
        List<String> blocked = new ArrayList<>(
            BrinConfig.harpyModifierBlacklist().getOrDefault(role.identifier().toString(), List.of())
        );
        String modifierId = modifier.identifier().toString();
        if (!blocked.remove(modifierId)) throw NOT_FOUND.create();
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

    private static MutableComponent buildRoleLine(Role role) {
        List<String> blockedIds = BrinConfig.harpyModifierBlacklist().get(role.identifier().toString());
        MutableComponent header = roleText(role).append(Component.literal(" (" + role.identifier() + ")"));
        if (blockedIds == null || blockedIds.isEmpty()) {
            return header.append(": ").append(Component.translatable("commands.rolemodifierblacklist.list.none"));
        }
        List<Modifier> modifiers = blockedIds.stream()
            .map(RoleModifierBlacklistCommand::findModifier)
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

    private static Modifier findModifier(String modifierId) {
        for (Modifier modifier : HMLModifiers.MODIFIERS) {
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

    private static MutableComponent modifierText(Modifier modifier) {
        return modifier.getName(true)
            .withStyle(style -> style.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(modifier.identifier().toString()))
            ));
    }
}
