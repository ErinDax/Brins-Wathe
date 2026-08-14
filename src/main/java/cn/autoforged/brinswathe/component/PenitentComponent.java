package cn.autoforged.brinswathe.component;

import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.CowboyDuel;
import cn.autoforged.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.agmas.harpymodloader.Harpymodloader;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class PenitentComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<PenitentComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "penitent"),
        PenitentComponent.class
    );

    private final Player player;
    private boolean initialized;
    private int completedTargetKills;
    private int requiredTargetKills;
    private UUID targetPlayerId;
    private ResourceLocation targetRoleId;
    private int actionBarRefreshTicks;
    private int shieldLayers;

    public PenitentComponent(Player player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        // Frozen while a cowboy duel benches the crowd as spectators; nothing may reset or tick down.
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer owner)) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(owner.level());
        if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE
            || !gameWorld.isRole(owner, BrinRoles.PENITENT)) {
            return;
        }

        if (!this.initialized) this.initializeRound(owner, gameWorld);
        if (this.hasCompletedTargets()) return;

        this.ensureTarget(owner, gameWorld);
        if (this.targetRoleId == null || !GameFunctions.isPlayerAliveAndSurvival(owner)) return;

        if (this.actionBarRefreshTicks <= 0) {
            owner.displayClientMessage(
                Component.translatable(
                    "message.brinswathe.penitent.target",
                    roleName(this.targetRoleId),
                    this.completedTargetKills,
                    this.requiredTargetKills
                ),
                true
            );
            this.actionBarRefreshTicks = 10;
        } else {
            this.actionBarRefreshTicks--;
        }
    }

    public boolean recordTargetKill(UUID victimId) {
        if (!this.initialized
            || this.hasCompletedTargets()
            || this.targetPlayerId == null
            || !this.targetPlayerId.equals(victimId)) {
            return false;
        }

        this.completedTargetKills++;
        this.targetPlayerId = null;
        this.targetRoleId = null;
        this.actionBarRefreshTicks = 0;
        this.sync();
        return true;
    }

    /** Free layers handed out with the role; further layers must be earned with a kill. */
    public void initializeShield() {
        this.shieldLayers = BrinConfig.penitentStartingShieldLayers();
        this.sync();
    }

    public boolean addShieldLayer() {
        if (this.shieldLayers == Integer.MAX_VALUE) return false;
        this.shieldLayers++;
        this.sync();
        return true;
    }

    public boolean consumeShield() {
        if (this.shieldLayers <= 0) return false;
        this.shieldLayers--;
        this.sync();
        return true;
    }

    public int getShieldLayers() {
        return this.shieldLayers;
    }

    public void applyWrongKillPoison() {
        PlayerPoisonComponent poison = PlayerPoisonComponent.KEY.get(this.player);
        if (poison == null) return;
        int poisonTicks = poison.poisonTicks > 0
            ? Math.max(1, poison.poisonTicks - this.player.getRandom().nextIntBetweenInclusive(100, 300))
            : this.player.getRandom().nextIntBetweenInclusive(
                PlayerPoisonComponent.clampTime.getA(),
                PlayerPoisonComponent.clampTime.getB()
            );
        poison.setPoisonTicks(poisonTicks, null);
    }

    public boolean hasCompletedTargets() {
        return this.initialized
            && this.requiredTargetKills > 0
            && this.completedTargetKills >= this.requiredTargetKills;
    }

    public boolean revealTarget(ServerPlayer owner) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(owner.level());
        if (!gameWorld.isRole(owner, BrinRoles.PENITENT)) return false;

        if (!this.initialized) this.initializeRound(owner, gameWorld);
        if (this.hasCompletedTargets()) return false;
        this.ensureTarget(owner, gameWorld);
        if (this.targetPlayerId == null || this.targetRoleId == null) return false;

        ServerPlayer target = owner.server.getPlayerList().getPlayer(this.targetPlayerId);
        if (target == null || !GameFunctions.isPlayerAliveAndSurvival(target)) return false;

        owner.sendSystemMessage(Component.translatable(
            "message.brinswathe.penitent.identity_hint",
            target.getDisplayName(),
            roleName(this.targetRoleId)
        ));
        return true;
    }

    private void initializeRound(ServerPlayer owner, GameWorldComponent gameWorld) {
        int participantCount = 0;
        for (ServerPlayer candidate : owner.server.getPlayerList().getPlayers()) {
            if (candidate.level() != owner.level()) continue;
            if (gameWorld.getRole(candidate) == null) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(candidate)) continue;
            participantCount++;
        }

        this.initialized = true;
        this.completedTargetKills = 0;
        this.requiredTargetKills = participantCount > 8 ? 3 : 2;
        this.targetPlayerId = null;
        this.targetRoleId = null;
        this.actionBarRefreshTicks = 0;
        this.chooseTarget(owner, gameWorld);
        this.sync();
    }

    private void ensureTarget(ServerPlayer owner, GameWorldComponent gameWorld) {
        if (this.targetPlayerId != null) {
            ServerPlayer currentTarget = owner.server.getPlayerList().getPlayer(this.targetPlayerId);
            if (currentTarget != null
                && currentTarget.level() == owner.level()
                && GameFunctions.isPlayerAliveAndSurvival(currentTarget)
                && gameWorld.isInnocent(currentTarget)) {
                return;
            }
        }

        this.targetPlayerId = null;
        this.targetRoleId = null;
        this.actionBarRefreshTicks = 0;
        this.chooseTarget(owner, gameWorld);
        this.sync();
    }

    private void chooseTarget(ServerPlayer owner, GameWorldComponent gameWorld) {
        ArrayList<ServerPlayer> candidates = new ArrayList<>();
        for (ServerPlayer candidate : owner.server.getPlayerList().getPlayers()) {
            if (candidate == owner || candidate.level() != owner.level()) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(candidate)) continue;
            if (!gameWorld.isInnocent(candidate)) continue;
            if (gameWorld.getRole(candidate) == null) continue;
            candidates.add(candidate);
        }
        if (candidates.isEmpty()) return;

        ServerPlayer target = candidates.get(owner.getRandom().nextInt(candidates.size()));
        Role targetRole = gameWorld.getRole(target);
        this.targetPlayerId = target.getUUID();
        this.targetRoleId = targetRole.identifier();
    }

    private static Component roleName(ResourceLocation roleId) {
        Role role = null;
        for (Role candidate : WatheRoles.ROLES) {
            if (roleId.equals(candidate.identifier())) {
                role = candidate;
                break;
            }
        }
        if (role == null) return Component.literal(roleId.getPath());

        RoleAnnouncementTexts.RoleAnnouncementText announcement = Harpymodloader.autogeneratedAnnouncements.get(role);
        if (announcement != null) return announcement.roleText.copy();
        if (role.equals(WatheRoles.KILLER)) return RoleAnnouncementTexts.KILLER.roleText.copy();
        if (role.equals(WatheRoles.VIGILANTE)) return RoleAnnouncementTexts.VIGILANTE.roleText.copy();
        if (role.equals(WatheRoles.LOOSE_END)) return RoleAnnouncementTexts.LOOSE_END.roleText.copy();
        return RoleAnnouncementTexts.CIVILIAN.roleText.copy();
    }

    public void reset() {
        this.initialized = false;
        this.completedTargetKills = 0;
        this.requiredTargetKills = 0;
        this.targetPlayerId = null;
        this.targetRoleId = null;
        this.actionBarRefreshTicks = 0;
        this.shieldLayers = 0;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.initialized = tag.getBoolean("initialized");
        this.completedTargetKills = tag.getInt("completedTargetKills");
        this.requiredTargetKills = tag.getInt("requiredTargetKills");
        this.targetPlayerId = tag.hasUUID("targetPlayerId") ? tag.getUUID("targetPlayerId") : null;
        this.targetRoleId = tag.contains("targetRoleId")
            ? ResourceLocation.tryParse(tag.getString("targetRoleId"))
            : null;
        this.actionBarRefreshTicks = 0;
        this.shieldLayers = tag.getInt("shieldLayers");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putBoolean("initialized", this.initialized);
        tag.putInt("completedTargetKills", this.completedTargetKills);
        tag.putInt("requiredTargetKills", this.requiredTargetKills);
        if (this.targetPlayerId != null) tag.putUUID("targetPlayerId", this.targetPlayerId);
        if (this.targetRoleId != null) tag.putString("targetRoleId", this.targetRoleId.toString());
        tag.putInt("shieldLayers", this.shieldLayers);
    }
}
