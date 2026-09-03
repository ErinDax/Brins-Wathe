package cn.erindax.brinswathe.command;

import cn.erindax.brinswathe.BrinIcFlags;
import cn.erindax.brinswathe.BrinNoelleAccess;
import cn.erindax.brinswathe.BrinRoleWeights;
import cn.erindax.brinswathe.component.StaminaComponent;
import cn.erindax.brinswathe.network.BrinIcNightVisionS2CPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameConstants;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.agmas.harpymodloader.commands.argument.RoleArgumentType;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModdedRoleRemoved;

public final class BrinAdminCommands {
    private static final SuggestionProvider<CommandSourceStack> CONFIG_FIELDS = (context, builder) ->
        SharedSuggestionProvider.suggest(configFieldNames(), builder);

    private BrinAdminCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("setnow")
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
                            )))))
        );

        dispatcher.register(
            Commands.literal("setplayer")
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
                                ))))))
        );

        dispatcher.register(
            Commands.literal("setRecover")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("value", FloatArgumentType.floatArg(1.0F, 1000.0F))
                    .executes(context -> setMaxStamina(
                        context.getSource(),
                        (int) FloatArgumentType.getFloat(context, "value")
                    )))
        );
        dispatcher.register(
            Commands.literal("setMaxSprintingTicks")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0F, 100.0F))
                    .executes(context -> setRegenRate(
                        context.getSource(),
                        (int) FloatArgumentType.getFloat(context, "value")
                    )))
        );
        dispatcher.register(
            Commands.literal("setMinSprintingTicks")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0F, 10.0F))
                    .executes(context -> setRunSpeed(
                        context.getSource(),
                        FloatArgumentType.getFloat(context, "value")
                    )))
        );
        dispatcher.register(
            Commands.literal("roleRoundsclear")
                .requires(source -> source.hasPermission(2))
                .executes(context -> clearRoleRounds(context.getSource()))
        );
        dispatcher.register(
            Commands.literal("PrintRounds")
                .requires(source -> source.hasPermission(2))
                .executes(context -> printRoleRounds(context.getSource()))
        );
        dispatcher.register(
            Commands.literal("setconfig")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showConfig(context.getSource()))
                .then(Commands.argument("field", StringArgumentType.word())
                    .suggests(CONFIG_FIELDS)
                    .executes(context -> showConfigField(
                        context.getSource(),
                        StringArgumentType.getString(context, "field")
                    ))
                    .then(Commands.argument("value", StringArgumentType.greedyString())
                        .executes(context -> setConfigField(
                            context.getSource(),
                            StringArgumentType.getString(context, "field"),
                            StringArgumentType.getString(context, "value")
                        ))))
        );
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
            source.sendFailure(Component.literal(player.getName().getString() + " 已处于疯魔中。"));
            return 0;
        }
        if (!psycho.startPsycho()) {
            source.sendFailure(Component.literal("无法触发 " + player.getName().getString() + " 的精神错乱"));
            return 0;
        }
        psycho.setPsychoTicks(GameConstants.getInTicks(0, seconds));
        psycho.setArmour(armour);
        source.sendSuccess(
            () -> Component.literal("已触发 " + player.getName().getString() + " 的精神错乱: 持续～" + seconds + "秒, 护甲=" + armour)
                .withStyle(ChatFormatting.RED),
            true
        );
        player.sendSystemMessage(Component.literal("你进入了疯魔状态！").withStyle(ChatFormatting.DARK_RED));
        return 1;
    }

    private static int setAbilityCooldown(CommandSourceStack source, ServerPlayer player, int ticks) {
        BrinNoelleAccess.setNoelleAbilityCooldown(player, ticks);
        boolean stupid = BrinNoelleAccess.setStupidAbilityCooldown(player, ticks);
        source.sendSuccess(
            () -> Component.literal(
                "已为 " + player.getName().getString() + " 设置技能冷却 " + ticks + " ticks"
                    + (stupid ? "" : "（Stupid 冷却组件不可用）")
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
        if (!(rounds instanceof java.util.Map<?, ?> map)) {
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

    private static List<String> configFieldNames() {
        List<String> names = new ArrayList<>();
        for (Field field : BrinIcFlags.class.getDeclaredFields()) {
            if (field.isSynthetic() || !Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            names.add(field.getName());
        }
        names.add("resetItemsList");
        return names;
    }

    private static int showConfig(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("=== Brin IC 配置 ===").withStyle(ChatFormatting.GOLD), false);
        for (String name : configFieldNames()) {
            Object value = readConfig(name);
            source.sendSuccess(
                () -> Component.literal("- " + name + ": " + formatConfig(value)),
                false
            );
        }
        source.sendSuccess(() -> Component.literal("使用 /setconfig <字段> <值> 修改"), false);
        return 1;
    }

    private static int showConfigField(CommandSourceStack source, String field) {
        if (!configFieldNames().contains(field)) {
            source.sendFailure(Component.literal("未知字段: " + field));
            return 0;
        }
        source.sendSuccess(
            () -> Component.literal(field + " = " + formatConfig(readConfig(field))),
            false
        );
        return 1;
    }

    private static int setConfigField(CommandSourceStack source, String field, String raw) {
        if (!configFieldNames().contains(field)) {
            source.sendFailure(Component.literal("未知字段: " + field));
            return 0;
        }
        try {
            Object oldValue = readConfig(field);
            writeConfig(field, raw);
            BrinIcFlags.save();
            if ("instinctNightVision".equals(field) && source.getServer() != null) {
                BrinIcNightVisionS2CPacket.sendToAll(source.getServer());
            }
            if ("roleWeights".equals(field) && source.getServer() != null) {
                BrinRoleWeights.apply(source.getServer(), BrinIcFlags.roleWeights);
            }
            source.sendSuccess(
                () -> Component.literal("已修改 " + field + ": " + formatConfig(oldValue) + " -> " + formatConfig(readConfig(field))),
                true
            );
            return 1;
        } catch (Exception exception) {
            source.sendFailure(Component.literal("设置失败: " + exception.getMessage()));
            return 0;
        }
    }

    private static Object readConfig(String name) {
        if ("resetItemsList".equals(name)) return new ArrayList<>(BrinIcFlags.resetItemsList);
        try {
            Field field = BrinIcFlags.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(null);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private static void writeConfig(String name, String raw) throws Exception {
        if ("resetItemsList".equals(name)) {
            BrinIcFlags.resetItemsList.clear();
            String trimmed = raw.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }
            for (String part : trimmed.split(",")) {
                String id = part.trim().replace("\"", "");
                if (!id.isEmpty()) BrinIcFlags.resetItemsList.add(id);
            }
            return;
        }
        Field field = BrinIcFlags.class.getDeclaredField(name);
        field.setAccessible(true);
        Class<?> type = field.getType();
        if (type == boolean.class || type == Boolean.class) {
            field.set(null, Boolean.parseBoolean(raw));
        } else if (type == int.class || type == Integer.class) {
            field.set(null, Integer.parseInt(raw));
        } else if (type == float.class || type == Float.class) {
            field.set(null, Float.parseFloat(raw));
        } else {
            throw new IllegalArgumentException("不支持的类型: " + type.getSimpleName());
        }
    }

    private static String formatConfig(Object value) {
        if (value instanceof List<?> list) return Arrays.toString(list.toArray());
        return String.valueOf(value);
    }
}
