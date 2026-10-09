package cn.erindax.brinswathe.admin;

import cn.erindax.brinswathe.BrinHarpyRoles;
import cn.erindax.brinswathe.BrinNoelleAccess;
import cn.erindax.brinswathe.BrinPunishment;
import cn.erindax.brinswathe.config.BrinConfig;
import cn.erindax.brinswathe.musicbox.BrinMusicBox;
import cn.erindax.brinswathe.network.BrinAdminActionC2SPacket;
import cn.erindax.brinswathe.network.BrinAdminSaveC2SPacket;
import cn.erindax.brinswathe.network.BrinAdminSnapshotS2CPacket;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.aussiebox.starexpress.cca.AllergicComponent;
import org.jetbrains.annotations.Nullable;

public final class BrinAdminPanel {
    public static final int PERMISSION = 2;
    public static final String OP_SETTING = "setting";
    public static final String OP_ROLE_ENABLED = "role_enabled";
    public static final String OP_ROLE_FORCED = "role_forced";
    public static final String OP_ROLE_BLOCKED = "role_blocked";
    public static final String OP_MODIFIER_ENABLED = "modifier_enabled";
    public static final String OP_PLAYER_ROLE_SET = "player_role_set";
    public static final String OP_PLAYER_ROLE_REMOVE = "player_role_remove";
    public static final String OP_PLAYER_PUNISH = "player_punish";
    public static final String OP_PUNISH_CANCEL = "punish_cancel";
    public static final String OP_PLAYER_ALLERGY = "player_allergy";
    public static final String OP_PLAYER_ARMOR = "player_armor";
    public static final String OP_PLAYER_PSYCHO = "player_psycho";
    public static final String OP_PLAYER_COOLDOWN = "player_cooldown";
    public static final String OP_PLAYER_ITEM_COOLDOWN = "player_item_cooldown";
    public static final String OP_PLAYER_MUSIC_CLEAR = "player_music_clear";
    public static final String ACTION_RELOAD = "reload";
    public static final String ACTION_MUSIC_STOP = "music_stop";
    public static final String ACTION_ROUNDS_CLEAR = "rounds_clear";
    public static final String ACTION_STAMINA_RESET = "stamina_reset";
    public static final String ALLERGY_FOOD = "food";
    public static final String ALLERGY_DRINK = "drink";
    public static final int ARMOR_MAX = 99;
    public static final int PSYCHO_SECONDS_MIN = 1;
    public static final int PSYCHO_SECONDS_MAX = 600;
    public static final int PSYCHO_ARMOUR_MAX = 99;
    public static final int COOLDOWN_TICKS_MAX = 72000;
    private static final int MAX_ECHO_LENGTH = 80;
    private static final Pattern QUEUED_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
    private static final Map<String, String> ACTIONS = Map.of(
        ACTION_RELOAD, "brinswathe reload",
        ACTION_MUSIC_STOP, "brinswathe musicbox stop",
        ACTION_ROUNDS_CLEAR, "brinswathe roleRoundsclear",
        ACTION_STAMINA_RESET, "brinswathe setbrinspeed reload"
    );
    private static final Map<UUID, int[]> TALLY = new HashMap<>();

    private BrinAdminPanel() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> TALLY.clear());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> TALLY.remove(handler.player.getUUID()));
        ServerPlayNetworking.registerGlobalReceiver(
            BrinAdminSaveC2SPacket.TYPE,
            (payload, context) -> save(context.player(), payload.json())
        );
        ServerPlayNetworking.registerGlobalReceiver(
            BrinAdminActionC2SPacket.TYPE,
            (payload, context) -> action(context.player(), payload.action())
        );
    }

    public static boolean open(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, BrinAdminSnapshotS2CPacket.TYPE)) return false;
        send(player, true);
        return true;
    }

    private static void send(ServerPlayer player, boolean open) {
        ServerPlayNetworking.send(player, new BrinAdminSnapshotS2CPacket(open, snapshot(player.server).toJson()));
    }

    private static BrinAdminSnapshot snapshot(MinecraftServer server) {
        Map<String, String> settings = new LinkedHashMap<>();
        for (BrinAdminSetting setting : BrinAdminSetting.values()) settings.put(setting.id(), setting.read());
        HarpyModLoaderConfig config = HarpyModLoaderConfig.HANDLER.instance();
        Map<String, List<String>> blacklist = BrinConfig.harpyModifierBlacklist();
        List<BrinAdminSnapshot.RoleState> roles = new ArrayList<>();
        for (Role role : WatheRoles.ROLES) {
            String id = role.identifier().toString();
            roles.add(new BrinAdminSnapshot.RoleState(
                role.identifier(),
                Harpymodloader.VANNILA_ROLES.contains(role),
                !config.disabled.contains(id),
                BrinHarpyRoles.FORCED_REFRESH_ROLES.contains(role),
                List.copyOf(blacklist.getOrDefault(id, List.of()))
            ));
        }
        List<BrinAdminSnapshot.ModifierState> modifiers = new ArrayList<>();
        for (Modifier modifier : HMLModifiers.MODIFIERS) {
            modifiers.add(new BrinAdminSnapshot.ModifierState(
                modifier.identifier(),
                !config.disabledModifiers.contains(modifier.identifier().toString())
            ));
        }
        List<ServerPlayer> online = new ArrayList<>(server.getPlayerList().getPlayers());
        online.sort(Comparator.comparing(player -> player.getGameProfile().getName(), String.CASE_INSENSITIVE_ORDER));
        List<BrinAdminSnapshot.PlayerState> players = new ArrayList<>();
        for (ServerPlayer player : online) players.add(playerState(player));
        return new BrinAdminSnapshot(settings, roles, modifiers, players, BrinPunishment.queuedNames());
    }

    private static BrinAdminSnapshot.PlayerState playerState(ServerPlayer player) {
        Role role = GameWorldComponent.KEY.get(player.level()).getRole(player);
        AllergicComponent allergic = AllergicComponent.KEY.getNullable(player);
        String allergy = allergic == null ? null : allergic.getAllergyType();
        PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.getNullable(player);
        return new BrinAdminSnapshot.PlayerState(
            player.getUUID(),
            player.getGameProfile().getName(),
            role == null ? "" : role.identifier().toString(),
            BrinPunishment.isQueued(player.getUUID()),
            allergic != null && allergic.isAllergic(),
            allergy == null ? "" : allergy,
            BrinNoelleAccess.bartenderArmor(player),
            psycho != null && psycho.getPsychoTicks() > 0,
            BrinMusicBox.hasMusic(player.getUUID())
        );
    }

    private static void save(ServerPlayer player, String json) {
        if (!player.hasPermissions(PERMISSION)) return;
        JsonObject root;
        try {
            root = JsonParser.parseString(json).getAsJsonObject();
        } catch (RuntimeException exception) {
            return;
        }
        int[] tally = TALLY.computeIfAbsent(player.getUUID(), id -> new int[2]);
        JsonElement ops = root.get("ops");
        if (ops != null && ops.isJsonArray()) {
            for (JsonElement op : ops.getAsJsonArray()) {
                String command = command(player.server, op);
                if (command == null) {
                    player.sendSystemMessage(Component.translatable("message.brinswathe.admin.invalid", echo(op))
                        .withStyle(ChatFormatting.RED));
                    tally[1]++;
                } else if (run(player, command)) {
                    tally[0]++;
                } else {
                    tally[1]++;
                }
            }
        }
        if (!flag(root, "done")) return;
        TALLY.remove(player.getUUID());
        if (!flag(root, "quiet")) {
            player.displayClientMessage(tally[1] == 0
                ? Component.translatable("message.brinswathe.admin.saved", tally[0]).withStyle(ChatFormatting.GREEN)
                : Component.translatable("message.brinswathe.admin.saved_failed", tally[0], tally[1])
                    .withStyle(ChatFormatting.GOLD), true);
        }
        send(player, false);
    }

    private static void action(ServerPlayer player, String action) {
        if (!player.hasPermissions(PERMISSION)) return;
        String command = ACTIONS.get(action);
        if (command == null) return;
        run(player, command);
        send(player, false);
    }

    @Nullable
    private static String command(MinecraftServer server, JsonElement element) {
        try {
            JsonArray op = element.getAsJsonArray();
            switch (op.get(0).getAsString()) {
                case OP_SETTING -> {
                    BrinAdminSetting setting = BrinAdminSetting.byId(op.get(1).getAsString());
                    String value = setting == null ? null : setting.normalize(op.get(2).getAsString());
                    return value == null ? null : setting.command(value);
                }
                case OP_ROLE_ENABLED -> {
                    Role role = role(op.get(1).getAsString(), false);
                    Boolean enabled = bool(op.get(2));
                    return role == null || enabled == null ? null : "setEnabledRole " + role.identifier() + " " + enabled;
                }
                case OP_ROLE_FORCED -> {
                    Role role = role(op.get(1).getAsString(), false);
                    Boolean forced = bool(op.get(2));
                    if (role == null || forced == null) return null;
                    return "brinswathe forceRefreshRole " + (forced ? "add " : "remove ") + role.identifier();
                }
                case OP_ROLE_BLOCKED -> {
                    Role role = role(op.get(1).getAsString(), true);
                    Modifier modifier = modifier(op.get(2).getAsString());
                    Boolean blocked = bool(op.get(3));
                    if (role == null || modifier == null || blocked == null) return null;
                    return "brinswathe roleModifierBlacklist " + (blocked ? "add " : "delete ")
                        + role.identifier() + " " + modifier.identifier();
                }
                case OP_MODIFIER_ENABLED -> {
                    Modifier modifier = modifier(op.get(1).getAsString());
                    Boolean enabled = bool(op.get(2));
                    return modifier == null || enabled == null ? null
                        : "setEnabledModifier " + modifier.identifier() + " " + enabled;
                }
                case OP_PUNISH_CANCEL -> {
                    String name = op.get(1).getAsString();
                    boolean queued = QUEUED_NAME.matcher(name).matches() && BrinPunishment.queuedNames().contains(name);
                    return queued ? "brinswathe error " + name + " cancel" : null;
                }
                default -> {
                    return playerCommand(server, op);
                }
            }
        } catch (RuntimeException exception) {
            return null;
        }
    }

    @Nullable
    private static String playerCommand(MinecraftServer server, JsonArray op) {
        ServerPlayer target = server.getPlayerList().getPlayer(UUID.fromString(op.get(1).getAsString()));
        if (target == null) return null;
        String name = target.getGameProfile().getName();
        String selector = quoted(name);
        switch (op.get(0).getAsString()) {
            case OP_PLAYER_ROLE_SET -> {
                Role role = role(op.get(2).getAsString(), true);
                return role == null ? null : "brinswathe setnow " + selector + " set " + role.identifier();
            }
            case OP_PLAYER_ROLE_REMOVE -> {
                Role role = GameWorldComponent.KEY.get(target.level()).getRole(target);
                return role == null ? null : "brinswathe setnow " + selector + " remove " + role.identifier();
            }
            case OP_PLAYER_PUNISH -> {
                Boolean punish = bool(op.get(2));
                if (punish == null || name.isEmpty() || name.startsWith("@")) return null;
                return "brinswathe error " + name + (punish ? "" : " cancel");
            }
            case OP_PLAYER_ALLERGY -> {
                String type = op.get(2).getAsString();
                if (!ALLERGY_FOOD.equals(type) && !ALLERGY_DRINK.equals(type)) return null;
                return "brinswathe allergic " + selector + " " + type;
            }
            case OP_PLAYER_ARMOR -> {
                Integer armor = number(op.get(2), 0, ARMOR_MAX);
                return armor == null ? null : "brinswathe setplayer " + selector + " armor " + armor;
            }
            case OP_PLAYER_PSYCHO -> {
                Integer seconds = number(op.get(2), PSYCHO_SECONDS_MIN, PSYCHO_SECONDS_MAX);
                Integer armour = number(op.get(3), 0, PSYCHO_ARMOUR_MAX);
                if (seconds == null || armour == null) return null;
                return "brinswathe setplayer " + selector + " psycho " + seconds + " " + armour;
            }
            case OP_PLAYER_COOLDOWN -> {
                Integer ticks = number(op.get(2), 0, COOLDOWN_TICKS_MAX);
                return ticks == null ? null : "brinswathe setplayer " + selector + " cooldown " + ticks;
            }
            case OP_PLAYER_ITEM_COOLDOWN -> {
                String item = op.get(2).getAsString().trim();
                Integer ticks = number(op.get(3), 0, COOLDOWN_TICKS_MAX);
                if (!BrinAdminSetting.isItem(item) || ticks == null) return null;
                return "brinswathe setplayer " + selector + " cd \"" + ResourceLocation.parse(item) + "\" " + ticks;
            }
            case OP_PLAYER_MUSIC_CLEAR -> {
                if (name.isEmpty() || name.startsWith("@")) return null;
                return "brinswathe musicbox clear " + name;
            }
            default -> {
                return null;
            }
        }
    }

    private static boolean run(ServerPlayer player, String command) {
        boolean[] succeeded = new boolean[1];
        CommandSourceStack source = player.createCommandSourceStack()
            .withCallback((success, result) -> succeeded[0] = success && result > 0);
        player.server.getCommands().performPrefixedCommand(source, command);
        return succeeded[0];
    }

    @Nullable
    private static Role role(String id, boolean allowVanilla) {
        for (Role role : WatheRoles.ROLES) {
            if (!role.identifier().toString().equals(id)) continue;
            return allowVanilla || !Harpymodloader.VANNILA_ROLES.contains(role) ? role : null;
        }
        return null;
    }

    @Nullable
    private static Modifier modifier(String id) {
        for (Modifier modifier : HMLModifiers.MODIFIERS) {
            if (modifier.identifier().toString().equals(id)) return modifier;
        }
        return null;
    }

    @Nullable
    private static Boolean bool(JsonElement element) {
        String value = element.getAsString();
        if ("true".equals(value)) return Boolean.TRUE;
        if ("false".equals(value)) return Boolean.FALSE;
        return null;
    }

    @Nullable
    private static Integer number(JsonElement element, int min, int max) {
        try {
            int value = Integer.parseInt(element.getAsString().trim());
            return value < min || value > max ? null : value;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static boolean flag(JsonObject root, String key) {
        JsonElement element = root.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsBoolean();
    }

    private static String quoted(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String echo(JsonElement op) {
        String text = op.toString();
        return text.length() > MAX_ECHO_LENGTH ? text.substring(0, MAX_ECHO_LENGTH) + "..." : text;
    }
}
