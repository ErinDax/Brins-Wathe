package cn.autoforged.brinswathe.component;

import cn.autoforged.brinswathe.CowboyDuel;
import cn.autoforged.brinswathe.config.BrinConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.autoforged.brinswathe.entity.IllusionistCloneEntity;
import cn.autoforged.brinswathe.network.BlindFlashS2CPacket;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheEntities;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class IllusionistComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<IllusionistComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "illusionist"),
        IllusionistComponent.class
    );

    public static final int CLONE_COUNT = 2;
    public static final int BLINDNESS_DURATION_TICKS = 100;
    public static final double CLONE_SPEED = 0.22;

    public static final EntityDimensions ILLUSION_MODEL_DIMENSIONS = EntityDimensions.scalable(0.6F, 1.8F);

    private final Player player;
    public int cloneTicks = 0;
    public List<UUID> cloneEntityIds = new ArrayList<>();
    public Map<UUID, Vec3> cloneDirections = new HashMap<>();
    public Map<UUID, Double> cloneSpeeds = new HashMap<>();
    @Nullable
    public UUID controlledCloneId;
    @Nullable
    public UUID bodyProxyEntityId;
    @Nullable
    private Vec3 controlAnchor;

    public IllusionistComponent(Player player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        // Frozen while a cowboy duel benches the crowd as spectators; nothing may reset or tick down.
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer sp)) return;

        if (!GameFunctions.isPlayerAliveAndSurvival(sp)) {
            if (!this.cloneEntityIds.isEmpty() || this.controlledCloneId != null || this.bodyProxyEntityId != null) {
                this.clearClones();
                this.sync();
            }
            return;
        }

        this.syncControlledCloneWithOwner(sp);

        if (this.cloneTicks > 0) {
            this.moveClones(sp);
            this.cloneTicks--;
            if (this.cloneTicks <= 0) {
                this.clearClones();
                this.sync();
            }
        } else if (!this.cloneEntityIds.isEmpty()) {
            this.clearClones();
            this.sync();
        }
    }

    public void spawnClones() {
        this.clearClones();

        for (int i = 0; i < CLONE_COUNT; i++) {
            boolean east = i == 0;
            float yaw = east ? -90.0F : 90.0F;
            Vec3 direction = east ? new Vec3(1, 0, 0) : new Vec3(-1, 0, 0);
            double speed = CLONE_SPEED;

            PlayerBodyEntity clone = new PlayerBodyEntity(WatheEntities.PLAYER_BODY, this.player.level());
            ((IllusionistCloneEntity) clone).brin$setIllusionBodyProxy(false);
            ((IllusionistCloneEntity) clone).brin$setIllusionClone(true);
            clone.refreshDimensions();
            clone.setPlayerUuid(this.player.getUUID());
            clone.setNoGravity(false);
            clone.noPhysics = false;
            clone.setSilent(true);
            clone.setInvulnerable(true);
            clone.setPos(this.player.getX(), this.player.getY(), this.player.getZ());
            clone.setYRot(yaw);
            clone.setYBodyRot(yaw);
            clone.setYHeadRot(yaw);
            clone.setDeltaMovement(direction.scale(speed));

            if (this.player.level().addFreshEntity(clone)) {
                UUID cloneId = clone.getUUID();
                this.cloneEntityIds.add(cloneId);
                this.cloneDirections.put(cloneId, direction);
                this.cloneSpeeds.put(cloneId, speed);
            }
        }
        this.cloneTicks = this.cloneEntityIds.isEmpty()
            ? 0
            : BrinConfig.skillDurationSeconds("illusionist") * 20;
        this.sync();
    }

    private void moveClones(ServerPlayer serverPlayer) {
        for (UUID cloneId : this.cloneEntityIds) {
            if (!(serverPlayer.serverLevel().getEntity(cloneId) instanceof PlayerBodyEntity clone)) continue;
            if (cloneId.equals(this.controlledCloneId)) continue;
            Vec3 direction = this.cloneDirections.get(cloneId);
            if (direction == null) continue;

            double verticalSpeed = clone.getDeltaMovement().y;
            double speed = this.cloneSpeeds.getOrDefault(cloneId, CLONE_SPEED);
            Vec3 movement = direction.scale(speed);
            boolean openedDoor = openDoorAhead(clone, movement);
            if (clone.horizontalCollision && !openedDoor) {
                clone.setDeltaMovement(0, verticalSpeed, 0);
                this.cloneDirections.remove(cloneId);
                this.cloneSpeeds.remove(cloneId);
                clone.hasImpulse = true;
                continue;
            }
            clone.setDeltaMovement(movement.x, verticalSpeed, movement.z);
            clone.hasImpulse = true;
        }
    }

    public void setControlledClone(@Nullable UUID cloneId) {
        if (cloneId != null && !this.cloneEntityIds.contains(cloneId)) return;
        if (cloneId != null
            && (!(this.player instanceof ServerPlayer serverPlayer)
            || !(serverPlayer.serverLevel().getEntity(cloneId) instanceof PlayerBodyEntity))) return;
        if (cloneId != null && cloneId.equals(this.controlledCloneId)) return;

        this.releaseControlledClone();

        if (cloneId != null && this.controlAnchor == null) {
            this.controlAnchor = this.player.position();
        }
        if (cloneId == null) {
            this.restoreOwnerToAnchor();
            this.controlledCloneId = null;
            this.removeBodyProxy();
            this.controlAnchor = null;
        } else {
            this.controlledCloneId = cloneId;
            this.spawnBodyProxy();
            this.teleportOwnerToClone(cloneId);
        }
        this.sync();
    }

    public void applyControlInput(UUID cloneId, float forward, float strafe, float yaw, float pitch) {
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;
        if (!cloneId.equals(this.controlledCloneId)) return;
        if (!(serverPlayer.serverLevel().getEntity(cloneId) instanceof PlayerBodyEntity clone)) return;

        float safeForward = Mth.clamp(forward, -1.0F, 1.0F);
        float safeStrafe = Mth.clamp(strafe, -1.0F, 1.0F);
        float safeYaw = Float.isFinite(yaw) ? Mth.wrapDegrees(yaw) : clone.getYRot();
        float safePitch = Float.isFinite(pitch) ? Mth.clamp(pitch, -90.0F, 90.0F) : clone.getXRot();

        double yawRadians = Math.toRadians(safeYaw);
        Vec3 forwardDirection = new Vec3(-Math.sin(yawRadians), 0, Math.cos(yawRadians));
        double leftYawRadians = Math.toRadians(safeYaw - 90.0F);
        Vec3 leftDirection = new Vec3(-Math.sin(leftYawRadians), 0, Math.cos(leftYawRadians));
        Vec3 movement = forwardDirection.scale(safeForward).add(leftDirection.scale(safeStrafe));
        if (movement.lengthSqr() > 1.0) movement = movement.normalize();
        movement = movement.scale(CLONE_SPEED);

        openDoorAhead(clone, movement);
        clone.setYRot(safeYaw);
        clone.setYBodyRot(safeYaw);
        clone.setYHeadRot(safeYaw);
        clone.setXRot(safePitch);
        clone.setDeltaMovement(movement.x, clone.getDeltaMovement().y, movement.z);
        clone.hasImpulse = true;
    }

    private static boolean openDoorAhead(PlayerBodyEntity clone, Vec3 movement) {
        if (movement.horizontalDistanceSqr() < 1.0E-6) return false;
        Vec3 direction = movement.normalize();
        BlockPos base = BlockPos.containing(
            clone.getX() + direction.x * 0.75,
            clone.getY() + 0.8,
            clone.getZ() + direction.z * 0.75
        );
        for (int y = -1; y <= 1; y++) {
            BlockPos pos = base.offset(0, y, 0);
            BlockState state = clone.level().getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock door && !state.getValue(DoorBlock.OPEN)) {
                door.setOpen(clone, clone.level(), state, pos, true);
                return true;
            }
        }
        return false;
    }

    private void syncControlledCloneWithOwner(ServerPlayer serverPlayer) {
        if (this.controlledCloneId == null || this.controlAnchor == null) return;

        if (!(serverPlayer.serverLevel().getEntity(this.controlledCloneId) instanceof PlayerBodyEntity clone)) {
            this.endSkill();
            return;
        }

        this.spawnBodyProxy();
        clone.setNoGravity(true);
        clone.noPhysics = true;
        clone.setPos(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
        clone.setYRot(serverPlayer.getYRot());
        clone.setYBodyRot(serverPlayer.yBodyRot);
        clone.setYHeadRot(serverPlayer.getYHeadRot());
        clone.setXRot(serverPlayer.getXRot());
        clone.setDeltaMovement(Vec3.ZERO);
        clone.hasImpulse = true;
    }

    private void teleportOwnerToClone(UUID cloneId) {
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;
        if (!(serverPlayer.serverLevel().getEntity(cloneId) instanceof PlayerBodyEntity clone)) return;

        clone.setNoGravity(true);
        clone.noPhysics = true;
        clone.setDeltaMovement(Vec3.ZERO);
        serverPlayer.connection.teleport(
            clone.getX(),
            clone.getY(),
            clone.getZ(),
            clone.getYRot(),
            clone.getXRot()
        );
        serverPlayer.setDeltaMovement(Vec3.ZERO);
        serverPlayer.hurtMarked = true;
    }

    private void releaseControlledClone() {
        if (!(this.player instanceof ServerPlayer serverPlayer) || this.controlledCloneId == null) return;
        if (!(serverPlayer.serverLevel().getEntity(this.controlledCloneId) instanceof PlayerBodyEntity clone)) return;

        clone.setPos(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
        clone.setYRot(serverPlayer.getYRot());
        clone.setYBodyRot(serverPlayer.yBodyRot);
        clone.setYHeadRot(serverPlayer.getYHeadRot());
        clone.setXRot(serverPlayer.getXRot());
        clone.setNoGravity(false);
        clone.noPhysics = false;
        Vec3 direction = this.cloneDirections.get(this.controlledCloneId);
        double speed = this.cloneSpeeds.getOrDefault(this.controlledCloneId, CLONE_SPEED);
        clone.setDeltaMovement(direction == null ? Vec3.ZERO : direction.scale(speed));
        clone.hasImpulse = true;
    }

    private void restoreOwnerToAnchor() {
        if (!(this.player instanceof ServerPlayer serverPlayer) || this.controlAnchor == null) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) return;

        serverPlayer.connection.teleport(
            this.controlAnchor.x,
            this.controlAnchor.y,
            this.controlAnchor.z,
            serverPlayer.getYRot(),
            serverPlayer.getXRot()
        );
        serverPlayer.setDeltaMovement(Vec3.ZERO);
        serverPlayer.hurtMarked = true;
    }

    public static boolean isClone(PlayerBodyEntity entity) {
        return ((IllusionistCloneEntity) entity).brin$isIllusionClone();
    }

    public static boolean isBodyProxy(PlayerBodyEntity entity) {
        IllusionistCloneEntity illusionEntity = (IllusionistCloneEntity) entity;
        return illusionEntity.brin$isIllusionBodyProxy() && !illusionEntity.brin$isIllusionClone();
    }

    public static boolean isIllusionModel(PlayerBodyEntity entity) {
        return isClone(entity) || isBodyProxy(entity);
    }

    public static boolean isControlledClone(PlayerBodyEntity entity) {
        if (!isClone(entity)) return false;
        Player owner = entity.level().getPlayerByUUID(entity.getPlayerUuid());
        if (owner == null) return false;
        IllusionistComponent component = KEY.get(owner);
        return entity.getUUID().equals(component.controlledCloneId);
    }

    public void handleCloneKilled(PlayerBodyEntity clone) {
        UUID cloneId = clone.getUUID();
        if (cloneId.equals(this.controlledCloneId)) {
            this.endSkill();
            return;
        }
        this.cloneEntityIds.remove(cloneId);
        this.cloneDirections.remove(cloneId);
        this.cloneSpeeds.remove(cloneId);
        clone.discard();
        if (this.cloneEntityIds.isEmpty()) {
            this.endSkill();
            return;
        }
        this.sync();
    }

    public static void applyCloneKillBlindness(@Nullable Entity attacker) {
        if (!(attacker instanceof ServerPlayer serverPlayer)) return;
        ServerPlayNetworking.send(serverPlayer, new BlindFlashS2CPacket(BLINDNESS_DURATION_TICKS));
    }

    private void spawnBodyProxy() {
        if (!(this.player instanceof ServerPlayer serverPlayer) || this.controlAnchor == null) return;
        if (this.bodyProxyEntityId != null
            && serverPlayer.serverLevel().getEntity(this.bodyProxyEntityId) instanceof PlayerBodyEntity) return;

        PlayerBodyEntity bodyProxy = new PlayerBodyEntity(WatheEntities.PLAYER_BODY, this.player.level());
        ((IllusionistCloneEntity) bodyProxy).brin$setIllusionClone(false);
        ((IllusionistCloneEntity) bodyProxy).brin$setIllusionBodyProxy(true);
        bodyProxy.refreshDimensions();
        bodyProxy.setPlayerUuid(this.player.getUUID());
        bodyProxy.setNoGravity(true);
        bodyProxy.noPhysics = false;
        bodyProxy.setSilent(true);
        bodyProxy.setPos(this.controlAnchor.x, this.controlAnchor.y, this.controlAnchor.z);
        bodyProxy.setYRot(this.player.getYRot());
        bodyProxy.setYBodyRot(this.player.yBodyRot);
        bodyProxy.setYHeadRot(this.player.getYHeadRot());
        bodyProxy.setXRot(this.player.getXRot());
        bodyProxy.setDeltaMovement(Vec3.ZERO);
        if (this.player.level().addFreshEntity(bodyProxy)) {
            this.bodyProxyEntityId = bodyProxy.getUUID();
        }
    }

    private void removeBodyProxy() {
        if (this.bodyProxyEntityId == null) return;
        this.discardEntity(this.bodyProxyEntityId);
        this.bodyProxyEntityId = null;
    }

    private void discardEntity(UUID id) {
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;
        for (ServerLevel level : serverPlayer.server.getAllLevels()) {
            var entity = level.getEntity(id);
            if (entity == null) continue;
            entity.discard();
            break;
        }
    }

    private void clearClones() {
        this.restoreOwnerToAnchor();
        if (this.player instanceof ServerPlayer sp) {
            for (UUID id : this.cloneEntityIds) {
                this.discardEntity(id);
            }
        }
        this.removeBodyProxy();
        this.cloneEntityIds.clear();
        this.cloneDirections.clear();
        this.cloneSpeeds.clear();
        this.controlledCloneId = null;
        this.bodyProxyEntityId = null;
        this.controlAnchor = null;
        this.cloneTicks = 0;
    }

    public void reset() {
        this.clearClones();
        this.sync();
    }

    public void endSkill() {
        this.clearClones();
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.cloneTicks = tag.getInt("cloneTicks");
        this.cloneEntityIds.clear();
        this.cloneDirections.clear();
        this.cloneSpeeds.clear();
        this.controlledCloneId = tag.hasUUID("controlledCloneId")
            ? tag.getUUID("controlledCloneId")
            : null;
        this.bodyProxyEntityId = tag.hasUUID("bodyProxyEntityId")
            ? tag.getUUID("bodyProxyEntityId")
            : null;
        this.controlAnchor = tag.contains("controlAnchorX")
            ? new Vec3(
                tag.getDouble("controlAnchorX"),
                tag.getDouble("controlAnchorY"),
                tag.getDouble("controlAnchorZ")
            )
            : null;
        ListTag list = tag.getList("cloneEntityIds", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID cloneId = entry.getUUID("uuid");
            this.cloneEntityIds.add(cloneId);
            if (entry.contains("directionX") && entry.contains("directionZ")) {
                this.cloneDirections.put(cloneId, new Vec3(
                    entry.getDouble("directionX"), 0, entry.getDouble("directionZ")
                ).normalize());
                this.cloneSpeeds.put(cloneId, entry.contains("speed")
                    ? entry.getDouble("speed")
                    : CLONE_SPEED);
            }
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("cloneTicks", this.cloneTicks);
        if (this.controlledCloneId != null) {
            tag.putUUID("controlledCloneId", this.controlledCloneId);
        }
        if (this.bodyProxyEntityId != null) {
            tag.putUUID("bodyProxyEntityId", this.bodyProxyEntityId);
        }
        if (this.controlAnchor != null) {
            tag.putDouble("controlAnchorX", this.controlAnchor.x);
            tag.putDouble("controlAnchorY", this.controlAnchor.y);
            tag.putDouble("controlAnchorZ", this.controlAnchor.z);
        }
        ListTag list = new ListTag();
        for (UUID id : this.cloneEntityIds) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", id);
            Vec3 direction = this.cloneDirections.get(id);
            if (direction != null) {
                entry.putDouble("directionX", direction.x);
                entry.putDouble("directionZ", direction.z);
                entry.putDouble("speed", this.cloneSpeeds.getOrDefault(id, CLONE_SPEED));
            }
            list.add(entry);
        }
        tag.put("cloneEntityIds", list);
    }
}
