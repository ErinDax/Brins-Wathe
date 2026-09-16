package cn.erindax.brinswathe.component;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.core.HolderLookup;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BrinRoundRecapComponent implements AutoSyncedComponent {
    public static final ComponentKey<BrinRoundRecapComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "round_recap"),
        BrinRoundRecapComponent.class
    );

    public static final int TYPE_KILL = 0;
    public static final int TYPE_DEATH = 1;
    public static final int TYPE_SHIELD = 2;
    private static final int MAX_EVENTS = 64;

    private final Level level;
    private final List<Identity> identities = new ArrayList<>();
    private final List<Event> events = new ArrayList<>();

    public BrinRoundRecapComponent(Level level) {
        this.level = level;
    }

    public List<Identity> identities() {
        return this.identities;
    }

    public List<Event> events() {
        return this.events;
    }

    public Identity identityOf(UUID id) {
        for (Identity identity : this.identities) {
            if (identity.id().equals(id)) return identity;
        }
        return null;
    }

    public void remember(Player player) {
        if (player == null) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(this.level);
        Role role = game == null ? null : game.getRole(player);
        String roleId = role == null ? "" : role.identifier().toString();
        UUID id = player.getUUID();
        String name = player.getGameProfile().getName();
        Identity current = this.identityOf(id);
        if (current != null) {
            if (roleId.isEmpty()) return;
            this.identities.set(this.identities.indexOf(current), new Identity(id, name, roleId));
            return;
        }
        this.identities.add(new Identity(id, name, roleId));
    }

    public void recordKill(Player killer, Player victim, ResourceLocation reason) {
        this.remember(killer);
        this.remember(victim);
        this.addEvent(event(TYPE_KILL, killer, victim, reason));
    }

    public void recordDeath(Player victim, ResourceLocation reason) {
        this.remember(victim);
        this.addEvent(event(TYPE_DEATH, null, victim, reason));
    }

    public void recordShield(Player victim, @Nullable Player attacker, ResourceLocation reason) {
        this.remember(attacker);
        this.remember(victim);
        this.addEvent(event(TYPE_SHIELD, attacker, victim, reason));
    }

    public void snapshotIdentities(Iterable<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            this.remember(player);
        }
        this.sync();
    }

    public void reset() {
        this.identities.clear();
        this.events.clear();
        this.sync();
    }

    public void sync() {
        KEY.sync(this.level);
    }

    public static void onPlayerKilled(Player victim, @Nullable Player killer, ResourceLocation reason) {
        if (victim.level().isClientSide) return;
        if (GameFunctions.isPlayerAliveAndSurvival(victim)) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(victim.level());
        if (game == null || !game.isRunning()) return;
        BrinRoundRecapComponent recap = KEY.get(victim.level());
        if (recap == null) return;
        if (killer != null && killer != victim) {
            recap.recordKill(killer, victim, reason);
        } else {
            recap.recordDeath(victim, reason);
        }
    }

    public static void onShieldBroken(Player victim, @Nullable Player attacker, ResourceLocation reason) {
        if (victim.level().isClientSide) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(victim.level());
        if (game == null || !game.isRunning()) return;
        BrinRoundRecapComponent recap = KEY.get(victim.level());
        if (recap != null) recap.recordShield(victim, attacker, reason);
    }

    private Event event(int type, @Nullable Player actor, Player target, ResourceLocation reason) {
        return new Event(
            type,
            nameOf(actor),
            roleOf(actor),
            nameOf(target),
            roleOf(target),
            reason == null ? "" : reason.getPath()
        );
    }

    private static String nameOf(@Nullable Player player) {
        return player == null ? "" : player.getGameProfile().getName();
    }

    private String roleOf(@Nullable Player player) {
        if (player == null) return "";
        GameWorldComponent game = GameWorldComponent.KEY.get(this.level);
        Role role = game == null ? null : game.getRole(player);
        return role == null ? "" : role.identifier().toString();
    }

    private void addEvent(Event event) {
        this.events.add(event);
        this.trimEvents();
    }

    private void trimEvents() {
        while (this.events.size() > MAX_EVENTS) {
            this.events.removeFirst();
        }
    }

    public void announceAll() {
        if (!(this.level instanceof ServerLevel serverLevel)) return;
        for (Event event : this.events) {
            Component line = formatEvent(event);
            for (ServerPlayer player : serverLevel.players()) {
                player.sendSystemMessage(line);
            }
        }
    }

    public static MutableComponent formatEvent(Event event) {
        MutableComponent tag;
        if (event.type() == TYPE_KILL) {
            tag = Component.translatable("recap.brinswathe.kill").withColor(0xE05050);
        } else if (event.type() == TYPE_SHIELD) {
            tag = Component.translatable("recap.brinswathe.shield").withColor(0xE0B040);
        } else {
            tag = Component.translatable("recap.brinswathe.death").withColor(0xAAAAAA);
        }
        MutableComponent line = Component.empty().append(tag).append(" ");
        if (event.type() == TYPE_DEATH) {
            return line.append(namedRole(event.target(), event.targetRoleId()))
                .append(" ")
                .append(reasonText(event.reason()));
        }
        if (!event.actor().isEmpty()) {
            line.append(namedRole(event.actor(), event.actorRoleId())).append(" ");
        }
        line.append(reasonText(event.reason()));
        if (!event.target().isEmpty()) {
            line.append(" ").append(namedRole(event.target(), event.targetRoleId()));
        }
        return line;
    }

    private static MutableComponent namedRole(String name, String roleId) {
        return Component.translatable("recap.brinswathe.player", name, roleText(roleId));
    }

    private static MutableComponent roleText(String roleId) {
        Role role = findRole(roleId);
        if (role == null) {
            ResourceLocation id = ResourceLocation.tryParse(roleId);
            return Component.literal(id == null || roleId == null || roleId.isEmpty() ? "?" : id.getPath())
                .withColor(0xFFFFFF);
        }
        ResourceLocation id = role.identifier();
        String namespaced = "announcement.role." + id.getNamespace() + "." + id.getPath();
        String plain = "announcement.role." + id.getPath();
        Language language = Language.getInstance();
        MutableComponent text = language.has(namespaced)
            ? Component.translatable(namespaced)
            : language.has(plain)
                ? Component.translatable(plain)
                : Component.translatable(namespaced);
        return text.withColor(role.color());
    }

    private static Role findRole(String roleId) {
        if (roleId == null || roleId.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(roleId);
        if (id == null) return null;
        for (Role role : WatheRoles.ROLES) {
            if (id.equals(role.identifier())) return role;
        }
        return null;
    }

    private static MutableComponent reasonText(String reason) {
        if (reason == null || reason.isEmpty()) {
            return Component.translatable("recap.brinswathe.reason.generic");
        }
        return Component.translatable("recap.brinswathe.reason." + reason);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.identities.clear();
        this.events.clear();
        ListTag identityTags = tag.getList("identities", Tag.TAG_COMPOUND);
        for (int index = 0; index < identityTags.size(); index++) {
            CompoundTag entry = identityTags.getCompound(index);
            this.identities.add(new Identity(
                entry.getUUID("id"),
                entry.getString("name"),
                entry.getString("role")
            ));
        }
        ListTag eventTags = tag.getList("events", Tag.TAG_COMPOUND);
        for (int index = 0; index < eventTags.size(); index++) {
            CompoundTag entry = eventTags.getCompound(index);
            this.events.add(new Event(
                entry.getInt("type"),
                entry.getString("actor"),
                entry.getString("actorRole"),
                entry.getString("target"),
                entry.getString("targetRole"),
                entry.getString("reason")
            ));
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        ListTag identityTags = new ListTag();
        for (Identity identity : this.identities) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("id", identity.id());
            entry.putString("name", identity.name());
            entry.putString("role", identity.roleId());
            identityTags.add(entry);
        }
        tag.put("identities", identityTags);
        ListTag eventTags = new ListTag();
        for (Event event : this.events) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("type", event.type());
            entry.putString("actor", event.actor());
            entry.putString("actorRole", event.actorRoleId());
            entry.putString("target", event.target());
            entry.putString("targetRole", event.targetRoleId());
            entry.putString("reason", event.reason());
            eventTags.add(entry);
        }
        tag.put("events", eventTags);
    }

    public record Identity(UUID id, String name, String roleId) {
    }

    public record Event(
        int type,
        String actor,
        String actorRoleId,
        String target,
        String targetRoleId,
        String reason
    ) {
    }
}
