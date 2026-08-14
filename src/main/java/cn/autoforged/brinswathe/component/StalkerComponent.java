package cn.autoforged.brinswathe.component;

import cn.autoforged.brinswathe.BrinModifiers;
import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.CowboyDuel;
import cn.autoforged.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

/**
 * The stalker's obsession. Tracking lasts until the target dies or is replaced; only re-targeting is
 * rate limited. The target's position is mirrored into this component once a second so the client can
 * keep pointing at him even when his entity drops out of tracking range (blackouts, far cars).
 */
public class StalkerComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<StalkerComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "stalker"),
        StalkerComponent.class
    );
    private static final int POSITION_SYNC_INTERVAL_TICKS = 10;

    private final Player player;
    @Nullable
    private UUID targetId;
    private int cooldownTicks;
    private double targetX;
    private double targetY;
    private double targetZ;

    public StalkerComponent(Player player) {
        this.player = player;
    }

    @Nullable
    public UUID targetId() {
        return this.targetId;
    }

    public int cooldownTicks() {
        return this.cooldownTicks;
    }

    public Vec3 lastKnownTargetPos() {
        return new Vec3(this.targetX, this.targetY, this.targetZ);
    }

    /** Entry point for the empty-handed right click; anything that is not a stalker's stalk passes through. */
    public static InteractionResult tryTrack(Player player, Entity entity) {
        if (!(player instanceof ServerPlayer stalker)) return InteractionResult.PASS;
        if (!stalker.getMainHandItem().isEmpty()) return InteractionResult.PASS;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(stalker.level());
        if (!gameWorld.isRole(stalker, BrinRoles.STALKER)) return InteractionResult.PASS;
        if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return InteractionResult.PASS;
        if (!GameFunctions.isPlayerAliveAndSurvival(stalker)) return InteractionResult.PASS;
        if (!(entity instanceof ServerPlayer target)
            || target == stalker
            || !GameFunctions.isPlayerAliveAndSurvival(target)) {
            return InteractionResult.PASS;
        }

        StalkerComponent component = KEY.get(stalker);
        if (component == null) return InteractionResult.PASS;

        // Clicking the player already being stalked changes nothing, so it must not restart the cooldown.
        if (target.getUUID().equals(component.targetId)) {
            stalker.displayClientMessage(
                Component.translatable("message.brinswathe.stalker.already", target.getDisplayName())
                    .withStyle(ChatFormatting.GRAY),
                true
            );
            return InteractionResult.SUCCESS;
        }

        if (component.cooldownTicks > 0) {
            stalker.displayClientMessage(
                Component.translatable("tip.kinswathe.cooldown", component.cooldownTicks / 20)
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.SUCCESS;
        }

        component.track(target);
        return InteractionResult.SUCCESS;
    }

    private void track(ServerPlayer target) {
        this.targetId = target.getUUID();
        this.rememberPosition(target);
        // The chat line names the exact source of the applied cooldown, so a zero reads as "the
        // unlimited modifier is doing its job" or "the config says zero" instead of a broken skill.
        int configuredSeconds = Math.max(0, BrinConfig.skillCooldownSeconds("stalker"));
        boolean unlimited = BrinModifiers.hasModifier(this.player, BrinModifiers.FAST2FAST);
        this.cooldownTicks = unlimited ? 0 : configuredSeconds * 20;
        if (this.player instanceof ServerPlayer stalker) {
            stalker.playNotifySound(SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            stalker.displayClientMessage(
                Component.translatable(
                    unlimited
                        ? "message.brinswathe.stalker.tracking_unlimited"
                        : "message.brinswathe.stalker.tracking",
                    target.getDisplayName(),
                    configuredSeconds
                ).withStyle(ChatFormatting.LIGHT_PURPLE),
                false
            );
        }
        this.sync();
    }

    @Override
    public void serverTick() {
        // Frozen while a cowboy duel benches the crowd as spectators; nothing may reset or tick down.
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer stalker)) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(stalker.level());
        if (!gameWorld.isRole(stalker, BrinRoles.STALKER)
            || !GameFunctions.isPlayerAliveAndSurvival(stalker)
            || gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) {
            this.reset();
            return;
        }

        if (this.cooldownTicks > 0) {
            if (BrinModifiers.hasModifier(stalker, BrinModifiers.FAST2FAST)) {
                this.cooldownTicks = 0;
            } else {
                this.cooldownTicks--;
            }
            this.sync();
        }

        if (this.targetId == null || stalker.getServer() == null) return;

        ServerPlayer target = stalker.getServer().getPlayerList().getPlayer(this.targetId);
        if (target == null
            || target.level() != stalker.level()
            || !GameFunctions.isPlayerAliveAndSurvival(target)) {
            this.targetId = null;
            stalker.playNotifySound(SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 0.7F, 0.8F);
            stalker.displayClientMessage(
                Component.translatable("message.brinswathe.stalker.lost").withStyle(ChatFormatting.GRAY),
                false
            );
            this.sync();
            return;
        }

        this.rememberPosition(target);
        if (stalker.tickCount % POSITION_SYNC_INTERVAL_TICKS == 0) this.sync();
    }

    private void rememberPosition(ServerPlayer target) {
        this.targetX = target.getX();
        this.targetY = target.getY();
        this.targetZ = target.getZ();
    }

    public void reset() {
        if (this.targetId == null && this.cooldownTicks <= 0) return;
        this.targetId = null;
        this.cooldownTicks = 0;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.targetId = tag.hasUUID("targetId") ? tag.getUUID("targetId") : null;
        this.cooldownTicks = tag.getInt("cooldownTicks");
        this.targetX = tag.getDouble("targetX");
        this.targetY = tag.getDouble("targetY");
        this.targetZ = tag.getDouble("targetZ");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        if (this.targetId != null) tag.putUUID("targetId", this.targetId);
        tag.putInt("cooldownTicks", this.cooldownTicks);
        tag.putDouble("targetX", this.targetX);
        tag.putDouble("targetY", this.targetY);
        tag.putDouble("targetZ", this.targetZ);
    }
}
