package cn.erindax.brinswathe.entity;

import cn.erindax.brinswathe.Funeral;
import cn.erindax.brinswathe.ModItems;
import cn.erindax.brinswathe.Zhuimu;
import cn.erindax.brinswathe.CowboyDuel;
import cn.erindax.brinswathe.config.ModConfig;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ZhuimuBoat extends Boat {
    private static final EntityDataAccessor<Integer> DATA_LEFT_PRESSES =
        SynchedEntityData.defineId(ZhuimuBoat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RIGHT_PRESSES =
        SynchedEntityData.defineId(ZhuimuBoat.class, EntityDataSerializers.INT);

    @Nullable
    private UUID ownerId;
    @Nullable
    private UUID victimId;
    private int hits;

    public ZhuimuBoat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_LEFT_PRESSES, 0);
        builder.define(DATA_RIGHT_PRESSES, 0);
    }

    public void setOwner(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isOwnedBy(Player player) {
        return player.getUUID().equals(this.ownerId);
    }

    @Nullable
    public ServerPlayer trappedPlayer() {
        return this.victimId != null
            && this.getFirstPassenger() instanceof ServerPlayer player
            && player.getUUID().equals(this.victimId)
            && GameFunctions.isPlayerAliveAndSurvival(player) ? player : null;
    }

    public int leftPresses() {
        return this.entityData.get(DATA_LEFT_PRESSES);
    }

    public int rightPresses() {
        return this.entityData.get(DATA_RIGHT_PRESSES);
    }

    public int addHit() {
        return ++this.hits;
    }

    public void resetHits() {
        this.hits = 0;
    }

    public void struggle(ServerPlayer player, boolean right) {
        if (player != this.trappedPlayer()) return;
        int required = ModConfig.zhuimuStrugglePresses();
        EntityDataAccessor<Integer> presses = right ? DATA_RIGHT_PRESSES : DATA_LEFT_PRESSES;
        this.entityData.set(presses, Math.min(required, this.entityData.get(presses) + 1));
        if (this.leftPresses() >= required && this.rightPresses() >= required) this.release(player);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel && !this.isRemoved()) this.serverTick();
    }

    private void serverTick() {
        if (CowboyDuel.isActive()) return;
        ServerPlayer owner = this.owner();
        if (owner == null || !Zhuimu.ownsBoat(owner, this)) {
            this.discard();
            return;
        }
        if (this.victimId != null) {
            if (this.trappedPlayer() == null) this.discard();
            return;
        }
        ServerPlayer target = Zhuimu.findCaptureTarget(this, owner);
        if (target != null) this.capture(target, owner);
    }

    private void capture(ServerPlayer victim, ServerPlayer owner) {
        Funeral.putDown(victim);
        victim.stopUsingItem();
        if (!victim.startRiding(this, true)) return;
        this.victimId = victim.getUUID();
        Zhuimu.onCaptured(owner);
    }

    private void release(ServerPlayer victim) {
        ServerPlayer owner = this.owner();
        victim.stopRiding();
        this.discard();
        if (owner != null) Zhuimu.onEscaped(owner);
    }

    @Nullable
    private ServerPlayer owner() {
        MinecraftServer server = this.getServer();
        return server == null || this.ownerId == null ? null : server.getPlayerList().getPlayer(this.ownerId);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        return null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public Item getDropItem() {
        return ModItems.ZHUIMU_BOAT;
    }
}
