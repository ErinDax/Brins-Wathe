package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.BrinFuneral;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public final class FuneralComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<FuneralComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "funeral"),
        FuneralComponent.class
    );
    private static final ResourceLocation SLOWDOWN_ID = ResourceLocation.fromNamespaceAndPath(
        "brinswathe", "funeral_carry"
    );
    private static final double SLOWDOWN = -0.5D;

    private final Player player;
    @Nullable
    private UUID carriedBody;

    public FuneralComponent(Player player) {
        this.player = player;
    }

    public boolean isCarrying() {
        return this.carriedBody != null;
    }

    public boolean pickUp(Entity body) {
        if (this.carriedBody != null || !body.startRiding(this.player, true)) return false;
        this.carriedBody = body.getUUID();
        this.applySlowdown();
        this.syncPassengers();
        this.sync();
        return true;
    }

    public void putDown() {
        if (this.carriedBody == null) return;
        Entity body = this.carriedEntity();
        this.carriedBody = null;
        this.removeSlowdown();
        if (body != null && body.getVehicle() == this.player) {
            body.stopRiding();
            this.syncPassengers();
            body.teleportTo(this.player.getX(), this.player.getY(), this.player.getZ());
            body.setDeltaMovement(Vec3.ZERO);
        }
        this.sync();
    }

    @Override
    public void serverTick() {
        if (this.carriedBody == null || !(this.player instanceof ServerPlayer serverPlayer)) return;
        Entity body = this.carriedEntity();
        if (body == null || body.getVehicle() != serverPlayer) {
            this.carriedBody = null;
            this.removeSlowdown();
            this.syncPassengers();
            this.sync();
            return;
        }
        if (!BrinFuneral.canCarry(serverPlayer)) {
            this.putDown();
            return;
        }
        this.applySlowdown();
    }

    @Nullable
    private Entity carriedEntity() {
        if (this.carriedBody == null || !(this.player.level() instanceof ServerLevel level)) return null;
        return level.getEntity(this.carriedBody);
    }

    private void applySlowdown() {
        AttributeInstance attribute = this.player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null || attribute.hasModifier(SLOWDOWN_ID)) return;
        attribute.addTransientModifier(new AttributeModifier(
            SLOWDOWN_ID,
            SLOWDOWN,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        ));
    }

    private void removeSlowdown() {
        AttributeInstance attribute = this.player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) attribute.removeModifier(SLOWDOWN_ID);
    }

    private void syncPassengers() {
        if (this.player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetPassengersPacket(serverPlayer));
        }
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.carriedBody = tag.hasUUID("carriedBody") ? tag.getUUID("carriedBody") : null;
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        if (this.carriedBody != null) tag.putUUID("carriedBody", this.carriedBody);
    }
}
