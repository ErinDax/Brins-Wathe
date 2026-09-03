package cn.erindax.brinswathe.command;

import cn.erindax.brinswathe.BrinIcFlags;
import cn.erindax.brinswathe.BrinKnifeSkins;
import cn.erindax.brinswathe.BrinRoleWeights;
import cn.erindax.brinswathe.network.BrinKnifeSkinApplyS2CPacket;
import cn.erindax.brinswathe.BrinNoelleAccess;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.aussiebox.starexpress.cca.AllergicComponent;

public final class BrinIcCommands {
    private static final ResourceLocation VOODOO_DEATH =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "voodoo");
    private static final String[] WANT_BE_ROLES = {"vulture", "executioner", "jester"};

    private BrinIcCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("killme").executes(context -> killme(context.getSource())));

        dispatcher.register(
            Commands.literal("iWantBe")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("value", StringArgumentType.word())
                    .suggests((context, builder) -> {
                    String remaining = builder.getRemaining().toLowerCase();
                    for (String name : WANT_BE_ROLES) {
                        if (name.startsWith(remaining)) {
                            builder.suggest(name, Component.translatable("announcement.role.noellesroles." + name));
                        }
                    }
                    return builder.buildFuture();
                })
                    .executes(context -> wantBe(
                        context.getSource(),
                        StringArgumentType.getString(context, "value")
                    )))
        );

        dispatcher.register(
            Commands.literal("setcd")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showCooldown(context.getSource()))
                .then(Commands.argument("seconds", IntegerArgumentType.integer(0, 300))
                    .executes(context -> setCooldown(
                        context.getSource(),
                        IntegerArgumentType.getInteger(context, "seconds")
                    )))
        );

        dispatcher.register(
            Commands.literal("bwathe")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("km")
                    .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> setKillme(
                            context.getSource(),
                            BoolArgumentType.getBool(context, "enabled")
                        ))))
                .then(Commands.literal("nv")
                    .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> setNightVision(
                            context.getSource(),
                            BoolArgumentType.getBool(context, "enabled")
                        ))))
                .then(Commands.literal("hudnames")
                    .executes(context -> showHudNamesThroughWalls(context.getSource()))
                    .then(Commands.argument("throughWalls", BoolArgumentType.bool())
                        .executes(context -> setHudNamesThroughWalls(
                            context.getSource(),
                            BoolArgumentType.getBool(context, "throughWalls")
                        ))))
                .then(Commands.literal("planb")
                    .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> setFlag(
                            context.getSource(),
                            "Plan B",
                            enabled -> BrinIcFlags.planB = enabled,
                            BoolArgumentType.getBool(context, "enabled")
                        ))))
                .then(Commands.literal("badguesser")
                    .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> setFlag(
                            context.getSource(),
                            "Bad Guesser",
                            enabled -> BrinIcFlags.badGuesser = enabled,
                            BoolArgumentType.getBool(context, "enabled")
                        ))))
                .then(Commands.literal("os")
                    .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> setFlag(
                            context.getSource(),
                            "Online knife skins",
                            enabled -> BrinIcFlags.onlineRegister = enabled,
                            BoolArgumentType.getBool(context, "enabled")
                        ))))
                .then(Commands.literal("weights")
                    .executes(context -> showWeights(context.getSource()))
                    .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> setWeights(
                            context.getSource(),
                            BoolArgumentType.getBool(context, "enabled")
                        ))))
                .then(Commands.literal("setcd")
                    .executes(context -> showCooldown(context.getSource()))
                    .then(Commands.argument("seconds", IntegerArgumentType.integer(0, 300))
                        .executes(context -> setCooldown(
                            context.getSource(),
                            IntegerArgumentType.getInteger(context, "seconds")
                        ))))
                .then(Commands.literal("skin")
                    .then(Commands.literal("reload")
                        .executes(context -> reloadSkins(context.getSource())))
                    .then(Commands.argument("player", EntityArgument.player())
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
                                ))))))
        );

        BrinAdminCommands.register(dispatcher);

        dispatcher.register(
            Commands.literal("starexpress")
                .then(Commands.literal("allergic")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("food").executes(context -> setAllergy(context.getSource(), EntityArgument.getPlayer(context, "player"), "food")))
                        .then(Commands.literal("drink").executes(context -> setAllergy(context.getSource(), EntityArgument.getPlayer(context, "player"), "drink")))))
        );
    }

    private static int wantBe(CommandSourceStack source, String rawValue) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("此指令只能由玩家执行"));
            return 0;
        }
        String value = rawValue.toLowerCase();
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        if (BrinNoelleAccess.initiateCount(game, player) > 1) {
            source.sendFailure(Component.literal("场上 Initiate 多于 1 人，无法自选职业"));
            return 0;
        }
        Role targetRole = BrinNoelleAccess.findRole(
            ResourceLocation.fromNamespaceAndPath("noellesroles", value)
        );
        if (targetRole == null) {
            source.sendFailure(Component.literal("无效角色: " + value + "（可选 vulture/executioner/jester）"));
            return 0;
        }
        clearItem(player, WatheItems.KNIFE);
        game.addRole(player, targetRole);
        ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, targetRole);
        announceRole(player, game, game.getRole(player));
        return 1;
    }

    private static void announceRole(ServerPlayer player, GameWorldComponent game, Role role) {
        if (role == null) return;
        RoleAnnouncementTexts.RoleAnnouncementText announcement = Harpymodloader.VANNILA_ROLES.contains(role)
            ? RoleAnnouncementTexts.KILLER
            : Harpymodloader.autogeneratedAnnouncements.get(role);
        if (announcement == null) return;
        int announcementIndex = RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(announcement);
        if (announcementIndex < 0) return;
        ServerPlayNetworking.send(
            player,
            new AnnounceWelcomePayload(announcementIndex, game.getAllKillerTeamPlayers().size(), 0)
        );
    }

    private static void clearItem(Player player, net.minecraft.world.item.Item item) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.is(item)) player.getInventory().items.set(i, ItemStack.EMPTY);
        }
        if (player.getInventory().offhand.getFirst().is(item)) {
            player.getInventory().offhand.set(0, ItemStack.EMPTY);
        }
    }

    private static int killme(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("此指令只能由玩家执行"));
            return 0;
        }
        if (!BrinIcFlags.allowKillme) {
            source.sendFailure(Component.literal("killme 指令已被管理员禁用"));
            return 0;
        }
        GameFunctions.killPlayer(player, true, null, VOODOO_DEATH);
        return 1;
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
            cn.erindax.brinswathe.network.BrinIcNightVisionS2CPacket.sendToAll(source.getServer());
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
            () -> Component.literal("本能准星名字隔墙显示: " + (current ? "开启" : "关闭（需视线可见）")),
            false
        );
        return 1;
    }

    private static int setHudNamesThroughWalls(CommandSourceStack source, boolean throughWalls) {
        BrinIcFlags.instinctHudNamesThroughWalls = throughWalls;
        BrinIcFlags.save();
        if (source.getServer() != null) {
            cn.erindax.brinswathe.network.BrinIcNightVisionS2CPacket.sendToAll(source.getServer());
        }
        source.sendSuccess(
            () -> Component.literal("本能准星名字隔墙显示: " + (throughWalls ? "开启" : "关闭（需视线可见）")),
            true
        );
        return 1;
    }

    private static int showWeights(CommandSourceStack source) {
        source.sendSuccess(
            () -> Component.literal("角色权重: " + (BrinIcFlags.roleWeights ? "开启" : "关闭（纯随机）")),
            false
        );
        return 1;
    }

    private static int setWeights(CommandSourceStack source, boolean enabled) {
        BrinRoleWeights.apply(source.getServer(), enabled);
        source.sendSuccess(
            () -> Component.literal("角色权重: " + (enabled ? "开启" : "关闭（纯随机）")),
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

    private static int reloadSkins(CommandSourceStack source) {
        try {
            java.nio.file.Files.createDirectories(BrinKnifeSkins.skinsDir());
        } catch (java.io.IOException ignored) {
        }
        java.util.List<String> registered = BrinKnifeSkins.scanAndRegister();
        if (source.getServer() != null) {
            BrinKnifeSkins.syncToAll(source.getServer());
        }
        source.sendSuccess(
            () -> Component.literal(
                "已注册刀皮（新扫描 " + registered.size() + " 个）。当前可用: " + BrinKnifeSkins.knownNamesText()
            ),
            true
        );
        return 1;
    }

    private static int setSkin(CommandSourceStack source, ServerPlayer target, String type, String skinName) {
        ServerPlayer sourcePlayer = source.getPlayer();
        if (sourcePlayer == null || !"Erin_Dax".equals(sourcePlayer.getName().getString())) {
            source.sendFailure(Component.literal("此指令仅 Erin_Dax 可以使用"));
            return 0;
        }
        String itemName;
        String resolved = skinName;
        if ("knife".equalsIgnoreCase(type)) {
            itemName = "knife";
            resolved = BrinKnifeSkins.resolveKnifeSkinName(skinName);
            if (!BrinKnifeSkins.isKnownSkin(resolved)) {
                source.sendFailure(Component.literal(
                    "无效皮肤: " + skinName + "（可用: " + BrinKnifeSkins.knownNamesText() + "）"
                ));
                return 0;
            }
        } else if ("gun".equalsIgnoreCase(type)) {
            itemName = "gun";
        } else {
            source.sendFailure(Component.literal("无效物品类型: " + type + "（可用: knife / gun）"));
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
        ServerPlayNetworking.send(target, new BrinKnifeSkinApplyS2CPacket(itemName, resolved));
        int finalApplied = applied;
        String finalName = resolved;
        source.sendSuccess(
            () -> Component.literal("已设置 " + target.getName().getString() + " 的 " + itemName + " 皮肤为: " + finalName + "（" + finalApplied + " 件物品）"),
            true
        );
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
