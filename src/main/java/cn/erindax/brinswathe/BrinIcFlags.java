package cn.erindax.brinswathe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

public final class BrinIcFlags {
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("brinswathe-ic.json");

    public static volatile boolean allowKillme = false;
    public static volatile boolean planB = true;
    public static volatile boolean badGuesser = true;
    public static volatile boolean onlineRegister = false;
    public static volatile boolean roleWeights = false;
    public static volatile boolean instinctNightVision = true;
    public static volatile boolean instinctHudNamesThroughWalls = false;
    public static volatile boolean size2speed = true;
    public static volatile int resetItemsCooldownSeconds = 30;
    public static volatile int psychoMinPlayersForExtraArmour = 6;
    public static volatile int psychoPlayersPerExtraArmour = 6;
    public static final List<String> resetItemsList = new ArrayList<>(List.of("wathe:revolver", "wathe:knife"));

    private BrinIcFlags() {
    }

    public static void load() {
        if (!Files.isRegularFile(PATH)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(PATH, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.has("allow_killme")) allowKillme = root.get("allow_killme").getAsBoolean();
            if (root.has("plan_b")) planB = root.get("plan_b").getAsBoolean();
            if (root.has("bad_guesser")) badGuesser = root.get("bad_guesser").getAsBoolean();
            if (root.has("online_register")) onlineRegister = root.get("online_register").getAsBoolean();
            if (root.has("role_weights")) roleWeights = root.get("role_weights").getAsBoolean();
            if (root.has("instinct_night_vision")) instinctNightVision = root.get("instinct_night_vision").getAsBoolean();
            if (root.has("instinct_hud_names_through_walls")) {
                instinctHudNamesThroughWalls = root.get("instinct_hud_names_through_walls").getAsBoolean();
            }
            if (root.has("size2speed")) size2speed = root.get("size2speed").getAsBoolean();
            if (root.has("reset_items_cooldown_seconds")) {
                resetItemsCooldownSeconds = Math.max(0, root.get("reset_items_cooldown_seconds").getAsInt());
            }
            if (root.has("psycho_min_players_for_extra_armour")) {
                psychoMinPlayersForExtraArmour = Math.max(1, root.get("psycho_min_players_for_extra_armour").getAsInt());
            }
            if (root.has("psycho_players_per_extra_armour")) {
                psychoPlayersPerExtraArmour = Math.max(1, root.get("psycho_players_per_extra_armour").getAsInt());
            }
            if (root.has("reset_items_list") && root.get("reset_items_list").isJsonArray()) {
                resetItemsList.clear();
                for (JsonElement element : root.getAsJsonArray("reset_items_list")) {
                    if (element.isJsonPrimitive()) resetItemsList.add(element.getAsString());
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("allow_killme", allowKillme);
        root.addProperty("plan_b", planB);
        root.addProperty("bad_guesser", badGuesser);
        root.addProperty("online_register", onlineRegister);
        root.addProperty("role_weights", roleWeights);
        root.addProperty("instinct_night_vision", instinctNightVision);
        root.addProperty("instinct_hud_names_through_walls", instinctHudNamesThroughWalls);
        root.addProperty("size2speed", size2speed);
        root.addProperty("reset_items_cooldown_seconds", resetItemsCooldownSeconds);
        root.addProperty("psycho_min_players_for_extra_armour", psychoMinPlayersForExtraArmour);
        root.addProperty("psycho_players_per_extra_armour", psychoPlayersPerExtraArmour);
        JsonArray items = new JsonArray();
        for (String id : resetItemsList) items.add(id);
        root.add("reset_items_list", items);
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, root.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
