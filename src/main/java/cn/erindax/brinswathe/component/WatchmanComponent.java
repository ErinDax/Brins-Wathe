package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class WatchmanComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<WatchmanComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "watchman"),
        WatchmanComponent.class
    );

    private final Player player;
    private static final int NIGHT_VISION_DURATION = 600;

    public WatchmanComponent(Player player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer)) return;
        if (!this.player.isAlive()) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.level());
        if (!gameWorld.isRole(this.player, BrinRoles.WATCHMAN)) return;

        MobEffectInstance existing = this.player.getEffect(MobEffects.NIGHT_VISION);
        if (existing == null || existing.getDuration() < 200) {
            this.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION, 0, true, false, false));
        }
    }

    public void reset() {
        this.player.removeEffect(MobEffects.NIGHT_VISION);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
    }
}
