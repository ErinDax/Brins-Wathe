package cn.erindax.brinswathe.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

public record BrinAdminSnapshot(
    Map<String, String> settings,
    List<RoleState> roles,
    List<ModifierState> modifiers,
    List<PlayerState> players,
    List<String> punishments
) {
    public String toJson() {
        JsonObject root = new JsonObject();
        JsonObject settings = new JsonObject();
        for (Map.Entry<String, String> entry : this.settings.entrySet()) {
            settings.addProperty(entry.getKey(), entry.getValue());
        }
        root.add("settings", settings);
        JsonArray roles = new JsonArray();
        for (RoleState role : this.roles) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", role.id().toString());
            entry.addProperty("vanilla", role.vanilla());
            entry.addProperty("enabled", role.enabled());
            entry.addProperty("forced", role.forced());
            JsonArray blocked = new JsonArray();
            for (String modifier : role.blocked()) blocked.add(modifier);
            entry.add("blocked", blocked);
            roles.add(entry);
        }
        root.add("roles", roles);
        JsonArray modifiers = new JsonArray();
        for (ModifierState modifier : this.modifiers) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", modifier.id().toString());
            entry.addProperty("enabled", modifier.enabled());
            modifiers.add(entry);
        }
        root.add("modifiers", modifiers);
        JsonArray players = new JsonArray();
        for (PlayerState player : this.players) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", player.id().toString());
            entry.addProperty("name", player.name());
            entry.addProperty("role", player.role());
            entry.addProperty("punished", player.punished());
            entry.addProperty("allergic", player.allergic());
            entry.addProperty("allergy", player.allergy());
            entry.addProperty("armor", player.armor());
            entry.addProperty("psycho", player.psycho());
            entry.addProperty("music", player.music());
            players.add(entry);
        }
        root.add("players", players);
        JsonArray punishments = new JsonArray();
        for (String name : this.punishments) punishments.add(name);
        root.add("punishments", punishments);
        return root.toString();
    }

    public static BrinAdminSnapshot fromJson(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        Map<String, String> settings = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("settings").entrySet()) {
            settings.put(entry.getKey(), entry.getValue().getAsString());
        }
        List<RoleState> roles = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray("roles")) {
            JsonObject entry = element.getAsJsonObject();
            ResourceLocation id = ResourceLocation.tryParse(entry.get("id").getAsString());
            if (id == null) continue;
            List<String> blocked = new ArrayList<>();
            for (JsonElement modifier : entry.getAsJsonArray("blocked")) blocked.add(modifier.getAsString());
            roles.add(new RoleState(
                id,
                entry.get("vanilla").getAsBoolean(),
                entry.get("enabled").getAsBoolean(),
                entry.get("forced").getAsBoolean(),
                List.copyOf(blocked)
            ));
        }
        List<ModifierState> modifiers = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray("modifiers")) {
            JsonObject entry = element.getAsJsonObject();
            ResourceLocation id = ResourceLocation.tryParse(entry.get("id").getAsString());
            if (id != null) modifiers.add(new ModifierState(id, entry.get("enabled").getAsBoolean()));
        }
        List<PlayerState> players = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray("players")) {
            JsonObject entry = element.getAsJsonObject();
            players.add(new PlayerState(
                UUID.fromString(entry.get("id").getAsString()),
                entry.get("name").getAsString(),
                entry.get("role").getAsString(),
                entry.get("punished").getAsBoolean(),
                entry.get("allergic").getAsBoolean(),
                entry.get("allergy").getAsString(),
                entry.get("armor").getAsInt(),
                entry.get("psycho").getAsBoolean(),
                entry.get("music").getAsBoolean()
            ));
        }
        List<String> punishments = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray("punishments")) punishments.add(element.getAsString());
        return new BrinAdminSnapshot(
            Collections.unmodifiableMap(settings),
            List.copyOf(roles),
            List.copyOf(modifiers),
            List.copyOf(players),
            List.copyOf(punishments)
        );
    }

    public record RoleState(ResourceLocation id, boolean vanilla, boolean enabled, boolean forced, List<String> blocked) {
    }

    public record ModifierState(ResourceLocation id, boolean enabled) {
    }

    public record PlayerState(
        UUID id,
        String name,
        String role,
        boolean punished,
        boolean allergic,
        String allergy,
        int armor,
        boolean psycho,
        boolean music
    ) {
    }
}
