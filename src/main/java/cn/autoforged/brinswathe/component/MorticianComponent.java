package cn.autoforged.brinswathe.component;

import cn.autoforged.brinswathe.CowboyDuel;
import cn.autoforged.brinswathe.config.BrinConfig;
import cn.autoforged.brinswathe.entity.MorticianDisguiseBody;
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

    /**
     * Wide enough that standing on a slab edge or being nudged by the world does not blow the act, tight
     * enough that a single deliberate step does.
     */
    private static final double MOVE_TOLERANCE_SQR = 0.01D;

    private enum DisguiseEnd {
        MOVED,
        ACTED,
        BODY_LOST,
        OWNER_LOST,
        RESET
    }

    private final Player player;

    @Nullable
    public UUID disguiseBodyId;
    @Nullable
    private Vec3 disguiseAnchor;

    public MorticianComponent(Player player) {
        this.player = player;
    }

    public boolean isDisguised() {
        return this.disguiseBodyId != null;
    }

    @Override
    public void serverTick() {
        // Frozen while a cowboy duel benches the crowd as spectators; nothing may reset or tick down.
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer serverPlayer) || !this.isDisguised()) return;

        if (!GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
            this.endDisguise(serverPlayer, DisguiseEnd.OWNER_LOST);
            return;
        }

        PlayerBodyEntity body = this.disguiseBody(serverPlayer);
        if (body == null) {
            this.endDisguise(serverPlayer, DisguiseEnd.BODY_LOST);
            return;
        }
        if (serverPlayer.swinging || serverPlayer.isUsingItem()) {
            this.endDisguise(serverPlayer, DisguiseEnd.ACTED);
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
        // A swing still running from before the inventory was opened would cancel the disguise instantly.
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

        // A mortician who died or was wiped by a round reset has nothing left to spend a cooldown on, and
        // the whole point of charging it here is that lying still costs nothing until you get up.
        if (reason != DisguiseEnd.RESET && GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
            AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(serverPlayer);
            if (ability != null) {
                ability.setAbilityCooldown(BrinConfig.skillCooldownSeconds("mortician"));
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
        if (this.disguiseAnchor != null) {
            tag.putDouble("disguiseAnchorX", this.disguiseAnchor.x);
            tag.putDouble("disguiseAnchorY", this.disguiseAnchor.y);
            tag.putDouble("disguiseAnchorZ", this.disguiseAnchor.z);
        }
    }
}
