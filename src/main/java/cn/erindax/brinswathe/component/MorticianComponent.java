package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.CowboyDuel;
import cn.erindax.brinswathe.config.BrinConfig;
import cn.erindax.brinswathe.entity.MorticianDisguiseBody;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheEntities;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class MorticianComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<MorticianComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "mortician"),
        MorticianComponent.class
    );

    private static final double MOVE_TOLERANCE_SQR = 0.01D;
    private enum DisguiseEnd {
        MOVED,
        BODY_LOST,
        OWNER_LOST,
        RESET
    }
    private final Player player;
    @Nullable
    public UUID disguiseBodyId;
    @Nullable
    private Vec3 disguiseAnchor;
    private int ambushTicks;
    private boolean ambushKnifeRefund;
    public MorticianComponent(Player player) {
        this.player = player;
    }
    public boolean isDisguised() {
        return this.disguiseBodyId != null;
    }
    public boolean markAmbushKnifeKill() {
        if (this.ambushTicks <= 0) return false;
        this.ambushTicks = 0;
        this.ambushKnifeRefund = true;
        return true;
    }
    public boolean consumeAmbushKnifeRefund() {
        if (!this.ambushKnifeRefund) return false;
        this.ambushKnifeRefund = false;
        return true;
    }
    @Override
    public void serverTick() {

        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;
        if (this.ambushTicks > 0) {
            this.ambushTicks--;
            if (this.ambushTicks == 0) this.ambushKnifeRefund = false;
        }
        if (!this.isDisguised()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
            this.endDisguise(serverPlayer, DisguiseEnd.OWNER_LOST);
            return;
        }
        PlayerBodyEntity body = this.disguiseBody(serverPlayer);
        if (body == null) {
            this.endDisguise(serverPlayer, DisguiseEnd.BODY_LOST);
            return;
        }
        if (this.disguiseAnchor == null
            || serverPlayer.position().distanceToSqr(this.disguiseAnchor) > MOVE_TOLERANCE_SQR) {
            this.endDisguise(serverPlayer, DisguiseEnd.MOVED);
            return;
        }
        this.pinBody(body);
    }

    public boolean startDisguise(ServerPlayer serverPlayer, UUID skinId) {
        if (this.isDisguised()) return false;

        PlayerBodyEntity body = new PlayerBodyEntity(WatheEntities.PLAYER_BODY, serverPlayer.level());
        MorticianDisguiseBody disguise = (MorticianDisguiseBody) body;
        disguise.brin$setMorticianDisguise(true);
        disguise.brin$setMortician(serverPlayer.getUUID());
        body.setPlayerUuid(skinId);
        body.setSilent(true);
        body.setInvulnerable(true);
        body.setNoGravity(true);
        body.noPhysics = true;
        body.setPos(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
        body.setYRot(serverPlayer.getYRot());
        body.setYBodyRot(serverPlayer.getYRot());
        body.setYHeadRot(serverPlayer.getYRot());
        body.setXRot(0.0F);
        body.setDeltaMovement(Vec3.ZERO);
        if (!serverPlayer.level().addFreshEntity(body)) return false;
        this.disguiseBodyId = body.getUUID();
        this.disguiseAnchor = serverPlayer.position();
        serverPlayer.setDeltaMovement(Vec3.ZERO);
        serverPlayer.swinging = false;
        this.sync();
        serverPlayer.playNotifySound(SoundEvents.WOOL_FALL, SoundSource.PLAYERS, 0.8F, 0.7F);
        return true;
    }
    private void endDisguise(ServerPlayer serverPlayer, DisguiseEnd reason) {
        PlayerBodyEntity body = this.disguiseBody(serverPlayer);
        if (body != null) body.discard();
        this.disguiseBodyId = null;
        this.disguiseAnchor = null;

        if (reason != DisguiseEnd.RESET && GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
            AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(serverPlayer);
            if (ability != null) {
                ability.setAbilityCooldown(BrinConfig.skillCooldownSeconds("mortician"));
            }
            if (reason != DisguiseEnd.OWNER_LOST) {
                this.ambushTicks = Math.max(0, BrinConfig.skillDurationSeconds("mortician")) * 20;
                this.ambushKnifeRefund = false;
            }
            serverPlayer.playNotifySound(SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
        this.sync();
    }
    private void pinBody(PlayerBodyEntity body) {
        if (this.disguiseAnchor == null) return;
        body.setPos(this.disguiseAnchor.x, this.disguiseAnchor.y, this.disguiseAnchor.z);
        body.setDeltaMovement(Vec3.ZERO);
    }
    @Nullable
    private PlayerBodyEntity disguiseBody(ServerPlayer serverPlayer) {
        if (this.disguiseBodyId == null) return null;
        if (serverPlayer.serverLevel().getEntity(this.disguiseBodyId) instanceof PlayerBodyEntity body) {
            return body;
        }
        for (ServerLevel level : serverPlayer.server.getAllLevels()) {
            Entity entity = level.getEntity(this.disguiseBodyId);
            if (entity instanceof PlayerBodyEntity body) return body;
        }
        return null;
    }
    public static boolean isDisguiseBody(PlayerBodyEntity body) {
        return ((MorticianDisguiseBody) body).brin$isMorticianDisguise();
    }
    public void reset() {
        this.ambushTicks = 0;
        this.ambushKnifeRefund = false;
        if (this.player instanceof ServerPlayer serverPlayer && this.isDisguised()) {
            this.endDisguise(serverPlayer, DisguiseEnd.RESET);
            return;
        }
        this.disguiseBodyId = null;
        this.disguiseAnchor = null;
        this.sync();
    }
    public void sync() {
        KEY.sync(this.player);
    }
    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.disguiseBodyId = tag.hasUUID("disguiseBodyId") ? tag.getUUID("disguiseBodyId") : null;
        this.ambushTicks = tag.getInt("ambushTicks");
        this.disguiseAnchor = tag.contains("disguiseAnchorX")
            ? new Vec3(
                tag.getDouble("disguiseAnchorX"),
                tag.getDouble("disguiseAnchorY"),
                tag.getDouble("disguiseAnchorZ")
            )
            : null;
    }
    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        if (this.disguiseBodyId != null) tag.putUUID("disguiseBodyId", this.disguiseBodyId);
        tag.putInt("ambushTicks", this.ambushTicks);
        if (this.disguiseAnchor != null) {
            tag.putDouble("disguiseAnchorX", this.disguiseAnchor.x);
            tag.putDouble("disguiseAnchorY", this.disguiseAnchor.y);
            tag.putDouble("disguiseAnchorZ", this.disguiseAnchor.z);
        }
    }
}
