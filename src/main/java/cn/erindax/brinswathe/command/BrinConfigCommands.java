package cn.erindax.brinswathe.command;

import cn.erindax.brinswathe.AfkKickManager;
import cn.erindax.brinswathe.BrinIcFlags;
import cn.erindax.brinswathe.BrinKnifeSkins;
import cn.erindax.brinswathe.BrinRoleWeights;
import cn.erindax.brinswathe.BrinSkinEditors;
import cn.erindax.brinswathe.config.BrinConfig;
import cn.erindax.brinswathe.network.BrinConfigS2CPacket;
import cn.erindax.brinswathe.network.BrinIcNightVisionS2CPacket;
import cn.erindax.brinswathe.network.BrinKnifeSkinApplyS2CPacket;
import cn.erindax.brinswathe.network.BrinResourceReloadS2CPacket;
import cn.erindax.brinswathe.network.BrinSkinUploadPromptS2CPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class BrinConfigCommands {
    private static final SuggestionProvider<CommandSourceStack> CONFIG_FIELDS = (context, builder) ->
        SharedSuggestionProvider.suggest(configFieldNames(), builder);

    private BrinConfigCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("brinswathe");
        attach(root);
        BrinAdminCommands.attach(root);
        dispatcher.register(root);
        BrinPlayerCommands.register(dispatcher);
    }

    static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        root
            .executes(context -> showHelp(context.getSource()))
            .then(Commands.literal("reload")
                .requires(source -> source.hasPermission(2))
                .executes(context -> reload(context.getSource())))
            .then(Commands.literal("afk")
                .requires(source -> source.hasPermission(2))
                .executes(context -> status(context.getSource()))
                .then(Commands.literal("on")
                    .executes(context -> setAfkEnabled(context.getSource(), true)))
                .then(Commands.literal("off")
                    .executes(context -> setAfkEnabled(context.getSource(), false))))
            .then(Commands.literal("setconfig")
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
                        )))))
            .then(Commands.literal("skin")
                .then(Commands.literal("upload")
                    .requires(source -> BrinSkinEditors.canEdit(source.getPlayer()))
                    .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            String remaining = builder.getRemaining().toLowerCase();
                            for (String type : new String[] {"knife", "gun"}) {
                                if (type.startsWith(remaining)) builder.suggest(type);
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(context -> requestUpload(
                                context.getSource(),
                                StringArgumentType.getString(context, "type"),
                                StringArgumentType.getString(context, "name"),
                                StringArgumentType.getString(context, "name")
                            ))
                            .then(Commands.argument("tooltip", StringArgumentType.greedyString())
                                .executes(context -> requestUpload(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "type"),
                                    StringArgumentType.getString(context, "name"),
                                    StringArgumentType.getString(context, "tooltip")
                                ))))))
                .then(Commands.literal("delete")
                    .requires(source -> BrinSkinEditors.canEdit(source.getPlayer()))
                    .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            String remaining = builder.getRemaining().toLowerCase();
                            for (String type : new String[] {"knife", "gun"}) {
                                if (type.startsWith(remaining)) builder.suggest(type);
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("name", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                String type = StringArgumentType.getString(context, "type");
                                String remaining = builder.getRemaining().toLowerCase();
                                for (String name : BrinKnifeSkins.deletableNames(type)) {
                                    if (name.toLowerCase().startsWith(remaining)) builder.suggest(name);
                                }
                                return builder.buildFuture();
                            })
                            .executes(context -> deleteSkin(
                                context.getSource(),
                                StringArgumentType.getString(context, "type"),
                                StringArgumentType.getString(context, "name"),
                                false
                            ))
                            .then(Commands.literal("files")
                                .executes(context -> deleteSkin(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "type"),
                                    StringArgumentType.getString(context, "name"),
                                    true
                                ))))))
                .then(Commands.argument("player", EntityArgument.player())
                    .requires(source -> BrinSkinEditors.canEdit(source.getPlayer()))
                    .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            String remaining = builder.getRemaining().toLowerCase();
                            for (String type : new String[] {"knife", "gun"}) {
                                if (type.startsWith(remaining)) builder.suggest(type);
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("skinName", StringArgumentType.greedyString())
                            .suggests((context, builder) -> {
                                String remaining = builder.getRemaining().toLowerCase();
                                for (String name : BrinKnifeSkins.suggestionNames()) {
                                    if (name.toLowerCase().startsWith(remaining)) builder.suggest(name);
                                }
                                return builder.buildFuture();
                            })
                            .executes(context -> setSkin(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player"),
                                StringArgumentType.getString(context, "type"),
                                StringArgumentType.getString(context, "skinName")
                            ))))));
    }

    private static int showHelp(CommandSourceStack source) {
        line(source, "/killme", "自杀");
        line(source, "/iWantBe <vulture|executioner|jester>", "自选职业");
        if (!source.hasPermission(2)) return 1;
        blank(source);
        blank(source);
        line(source, "/brinswathe reload", "重载全部");
        line(source, "/brinswathe afk [on|off]", "查看或开关挂机踢出");
        line(source, "/brinswathe setconfig [字段] [值]", "查看或修改 IC 配置字段");
        blank(source);
        blank(source);
        line(source, "/brinswathe skin upload <knife|gun> <名> [tooltip]", "选文件上传贴图, 可附带音效");
        line(source, "/brinswathe skin delete <knife|gun> <名> [files]", "取消注册, 加 files 同时删源文件");
        line(source, "/brinswathe skin <玩家> <knife|gun> <皮肤名>", "给该玩家物品套上指定皮肤");
        blank(source);
        blank(source);
        line(source, "/brinswathe km <true|false>", "开关 /killme");
        line(source, "/brinswathe nv <true|false>", "开关本能夜视");
        line(source, "/brinswathe hudnames [true|false]", "查看或设置本能准星名字是否隔墙显示");
        line(source, "/brinswathe planb <true|false>", "开关 Plan B");
        line(source, "/brinswathe badguesser <true|false>", "开关禁猜者");
        line(source, "/brinswathe weights [true|false]", "查看或设置角色权重");
        line(source, "/brinswathe setcd [秒]", "查看或设置重置物品冷却");
        line(source, "/brinswathe allergic <玩家> food|drink", "改过敏类型");
        line(source, "/brinswathe setnow <玩家> set|remove <职业>", "当场设置或移除职业");
        line(source, "/brinswathe roleRoundsclear", "清空角色轮次权重记录");
        line(source, "/brinswathe PrintRounds", "打印角色轮次权重记录");
        line(source, "/brinswathe setRoleCount neutral|killer|vigilante <数量>", "设置阵营角色数量");
        line(source, "/brinswathe forceRefreshRole add|remove|clear|list [职业…]", "强制后续刷新指定职业");
        line(source, "/brinswathe roleModifierBlacklist list|add|delete …", "职业词条黑名单");
        line(source, "/brinswathe setplayer <玩家> armor|psycho|cooldown|cd …", "改护甲、疯魔或冷却");
        line(source, "/brinswathe setRecover <值>", "设置体力上限");
        line(source, "/brinswathe setMaxSprintingTicks <值>", "设置体力回复");
        line(source, "/brinswathe setMinSprintingTicks <值>", "设置奔跑速度");
        line(source, "/brinswathe setbrinspeed reload|maxStamina|runSpeed|regenRate …", "改全局体力, reload 恢复默认");
        return 1;
    }

    private static void blank(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(" "), false);
    }

    private static void line(CommandSourceStack source, String command, String description) {
        source.sendSuccess(
            () -> Component.literal(command).withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("  " + description).withStyle(ChatFormatting.GRAY)),
            false
        );
    }

    private static int reload(CommandSourceStack source) {
        try {
            BrinConfig.reload();
            BrinIcFlags.load();
            AfkKickManager.reset();
            BrinKnifeSkins.reloadFromDisk(source.getServer());
            BrinConfigS2CPacket packet = new BrinConfigS2CPacket(BrinConfig.toJson());
            for (var player : source.getServer().getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, packet);
                ServerPlayNetworking.send(player, new BrinResourceReloadS2CPacket());
            }
            source.sendSuccess(
                () -> Component.literal("已统一重载配置、语言、皮肤白名单和皮肤"),
                true
            );
            return 1;
        } catch (Exception exception) {
            source.sendFailure(Component.literal("配置重载失败: " + exception.getMessage()));
            return 0;
        }
    }
    private static int status(CommandSourceStack source) {
        source.sendSuccess(
            () -> Component.translatable(
                BrinConfig.afkKickEnabled()
                    ? "command.brinswathe.afk.enabled"
                    : "command.brinswathe.afk.disabled"
            ),
            false
        );
        return BrinConfig.afkKickEnabled() ? 1 : 0;
    }
    private static int setAfkEnabled(CommandSourceStack source, boolean enabled) {
        try {
            BrinConfig.setAfkKickEnabled(enabled);
            AfkKickManager.reset();
            BrinConfigS2CPacket packet = new BrinConfigS2CPacket(BrinConfig.toJson());
            for (var player : source.getServer().getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, packet);
            }
            source.sendSuccess(
                () -> Component.translatable(
                    enabled
                        ? "command.brinswathe.afk.enabled"
                        : "command.brinswathe.afk.disabled"
                ),
                true
            );
            return 1;
        } catch (Exception exception) {
            source.sendFailure(Component.literal("挂机踢出设置保存失败: " + exception.getMessage()));
            return 0;
        }
    }

    private static int requestUpload(CommandSourceStack source, String type, String name, String tooltip) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("此指令只能由玩家执行"));
            return 0;
        }
        if (!BrinSkinEditors.canEdit(player)) {
            source.sendFailure(Component.translatable("message.brinswathe.skin.denied"));
            return 0;
        }
        if (!BrinSkinEditors.isType(type)) {
            source.sendFailure(Component.translatable("message.brinswathe.skin.invalid_type"));
            return 0;
        }
        ServerPlayNetworking.send(player, new BrinSkinUploadPromptS2CPacket(
            BrinSkinEditors.normalizeType(type),
            name,
            tooltip
        ));
        return 1;
    }

    private static int deleteSkin(CommandSourceStack source, String type, String name, boolean deleteFiles) {
        ServerPlayer player = source.getPlayer();
        if (player == null || !BrinSkinEditors.canEdit(player)) {
            source.sendFailure(Component.translatable("message.brinswathe.skin.denied"));
            return 0;
        }
        if (!BrinSkinEditors.isType(type)) {
            source.sendFailure(Component.translatable("message.brinswathe.skin.invalid_type"));
            return 0;
        }
        String deleted = BrinKnifeSkins.deleteUploaded(type, name, deleteFiles);
        if (deleted == null) {
            source.sendFailure(Component.literal("未找到可删除的皮肤: " + name));
            return 0;
        }
        if (source.getServer() != null) {
            BrinKnifeSkins.syncToAll(source.getServer());
        }
        source.sendSuccess(
            () -> Component.literal(
                deleteFiles
                    ? "已删除注册和源文件: " + deleted
                    : "已取消注册(源文件保留): " + deleted
            ),
            true
        );
        return 1;
    }

    private static int setSkin(CommandSourceStack source, ServerPlayer target, String type, String skinName) {
        ServerPlayer sourcePlayer = source.getPlayer();
        if (sourcePlayer == null || !BrinSkinEditors.canEdit(sourcePlayer)) {
            source.sendFailure(Component.translatable("message.brinswathe.skin.denied"));
            return 0;
        }
        String itemName;
        String resolved = skinName;
        if ("knife".equalsIgnoreCase(type)) {
            itemName = "knife";
            resolved = BrinKnifeSkins.resolveKnifeSkinName(skinName);
            if (!BrinKnifeSkins.isKnownSkin(resolved)) {
                source.sendFailure(Component.literal(
                    "无效皮肤: " + skinName + "(可用: " + BrinKnifeSkins.knownNamesText() + ")"
                ));
                return 0;
            }
        } else if ("gun".equalsIgnoreCase(type)) {
            itemName = "gun";
            resolved = BrinKnifeSkins.resolveGunSkinName(skinName);
            if (!BrinKnifeSkins.isKnownGunSkin(resolved)) {
                source.sendFailure(Component.literal(
                    "无效枪皮: " + skinName + "(可用: " + BrinKnifeSkins.knownGunNamesText() + ")"
                ));
                return 0;
            }
        } else {
            source.sendFailure(Component.literal("无效物品类型: " + type + "(可用: knife / gun)"));
            return 0;
        }
        int applied = 0;
        for (ItemStack stack : target.getInventory().items) {
            if (BrinKnifeSkins.applySkin(stack, type, resolved)) applied++;
        }
        for (ItemStack stack : target.getInventory().offhand) {
            if (BrinKnifeSkins.applySkin(stack, type, resolved)) applied++;
        }
        target.getInventory().setChanged();
        target.containerMenu.broadcastChanges();
        if ("knife".equalsIgnoreCase(type)) {
            BrinKnifeSkins.setKnifeSkin(target.getUUID(), resolved);
        }
        BrinKnifeSkins.syncHeldSkins(target);
        ServerPlayNetworking.send(target, new BrinKnifeSkinApplyS2CPacket(itemName, resolved));
        int finalApplied = applied;
        String finalName = resolved;
        source.sendSuccess(
            () -> Component.literal("已设置 " + target.getName().getString() + " 的 " + itemName + " 皮肤为: " + finalName + "(" + finalApplied + " 件物品)"),
            true
        );
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
        source.sendSuccess(() -> Component.literal("使用 /brinswathe setconfig <字段> <值> 修改"), false);
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
