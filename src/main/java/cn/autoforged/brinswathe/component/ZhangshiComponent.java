package cn.autoforged.brinswathe.component;

import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.CowboyDuel;
import cn.autoforged.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public final class ZhangshiComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<ZhangshiComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "zhangshi"),
        ZhangshiComponent.class
    );
    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
        "brinswathe", "zhangshi_speed"
    );

    private final Player player;
    private int normalTicks;
    private boolean xueziActive;
    private int endingTicks;
    private int shieldLayers;

    public ZhangshiComponent(Player player) {
        this.player = player;
    }

    public boolean isXueziActive() {
        return this.xueziActive;
    }

    public boolean isSpeedActive() {
        return this.xueziActive || this.normalTicks > 0;
    }

    public boolean activateNormal() {
        if (this.xueziActive || this.normalTicks > 0) return false;
        this.normalTicks = Math.max(0, BrinConfig.skillDurationSeconds("zhangshi") * 20);
        if (this.normalTicks <= 0) return false;
        sync();
        return true;
    }

    public boolean activateXuezi() {
        if (this.xueziActive) return false;
        this.xueziActive = true;
        this.normalTicks = 0;
        this.endingTicks = 0;
        this.shieldLayers = BrinConfig.xueziShieldLayers();
        PlayerPoisonComponent poison = PlayerPoisonComponent.KEY.get(this.player);
        if (poison != null) {
            poison.setPoisonTicks(
                this.player.getRandom().nextIntBetweenInclusive(
                    PlayerPoisonComponent.clampTime.getA(),
                    PlayerPoisonComponent.clampTime.getB()
                ),
                this.player.getUUID()
            );
        }
        sync();
        return true;
    }

    public void recordKill() {
        if (!this.xueziActive || this.endingTicks > 0) return;
        PlayerPoisonComponent poison = PlayerPoisonComponent.KEY.get(this.player);
        if (poison != null) poison.reset();
        this.endingTicks = 100;
        sync();
        if (this.player instanceof ServerPlayer serverPlayer) {
            serverPlayer.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
    }

    public boolean consumeShield() {
        if (this.shieldLayers <= 0) return false;
        this.shieldLayers--;
        sync();
        return true;
    }

    public int getShieldLayers() {
        return this.shieldLayers;
    }

    /** Write access for the cowboy duel, which strips the layers up front and owes them back after. */
    public void setShieldLayers(int layers) {
        int clamped = Math.max(0, layers);
        if (this.shieldLayers == clamped) return;
        this.shieldLayers = clamped;
        sync();
    }

    public void reset() {
        this.normalTicks = 0;
        this.xueziActive = false;
        this.endingTicks = 0;
        this.shieldLayers = 0;
        removeSpeedModifier();
        PlayerPoisonComponent poison = PlayerPoisonComponent.KEY.get(this.player);
        if (poison != null && poison.poisonTicks > 0) poison.reset();
        sync();
    }

    @Override
    public void serverTick() {
        // Frozen while a cowboy duel benches the crowd as spectators; nothing may reset or tick down.
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(serverPlayer.level());
        if (gameWorld == null
            || gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE
            || !gameWorld.isRole(serverPlayer, BrinRoles.ZHANGSHI)
            || !GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
            if (this.isSpeedActive() || this.xueziActive || this.shieldLayers > 0) reset();
            return;
        }

        if (this.xueziActive) {
            if (this.endingTicks > 0) {
                this.endingTicks--;
                if (this.endingTicks == 0) endXuezi();
                else if (this.endingTicks % 20 == 0) sync();
            }
            return;
        }

        if (this.normalTicks > 0) {
            this.normalTicks--;
            if (this.normalTicks == 0) {
                removeSpeedModifier();
                sync();
            } else if (this.normalTicks % 20 == 0) {
                sync();
            }
        } else {
            removeSpeedModifier();
        }
    }

    private void endXuezi() {
        this.xueziActive = false;
        this.endingTicks = 0;
        this.shieldLayers = 0;
        removeSpeedModifier();
        sync();
    }

    private void removeSpeedModifier() {
        AttributeInstance attribute = this.player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) attribute.removeModifier(SPEED_MODIFIER_ID);
    }

    private void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.normalTicks = tag.getInt("normalTicks");
        this.xueziActive = tag.getBoolean("xueziActive");
        this.endingTicks = tag.getInt("endingTicks");
        this.shieldLayers = tag.getInt("shieldLayers");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("normalTicks", this.normalTicks);
        tag.putBoolean("xueziActive", this.xueziActive);
        tag.putInt("endingTicks", this.endingTicks);
        tag.putInt("shieldLayers", this.shieldLayers);
    }
}
