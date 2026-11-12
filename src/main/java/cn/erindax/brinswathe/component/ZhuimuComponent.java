package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.Zhuimu;
import cn.erindax.brinswathe.CowboyDuel;
import cn.erindax.brinswathe.entity.ZhuimuBoat;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public final class ZhuimuComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<ZhuimuComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "zhuimu"),
        ZhuimuComponent.class
    );

    private final Player player;
    private boolean used;
    @Nullable
    private UUID boatId;

    public ZhuimuComponent(Player player) {
        this.player = player;
    }

    public boolean used() {
        return this.used;
    }

    public void markUsed() {
        this.used = true;
        this.sync();
    }

    public boolean hasBoat() {
        return this.boatId != null;
    }

    public boolean tracks(ZhuimuBoat boat) {
        return boat.getUUID().equals(this.boatId);
    }

    public void trackBoat(ZhuimuBoat boat) {
        this.boatId = boat.getUUID();
    }

    public void reset() {
        this.boatId = null;
        if (this.player instanceof ServerPlayer serverPlayer) Zhuimu.removeAxe(serverPlayer);
        if (!this.used) return;
        this.used = false;
        this.sync();
    }

    @Override
    public void serverTick() {
        if (this.boatId == null || CowboyDuel.isActive() || !(this.player instanceof ServerPlayer serverPlayer)) return;
        ZhuimuBoat boat = serverPlayer.serverLevel().getEntity(this.boatId) instanceof ZhuimuBoat found
            && !found.isRemoved() ? found : null;
        if (boat != null && boat.trappedPlayer() != null) {
            Zhuimu.giveAxe(serverPlayer);
            return;
        }
        Zhuimu.removeAxe(serverPlayer);
        if (boat == null) this.boatId = null;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.used = tag.getBoolean("used");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putBoolean("used", this.used);
    }
}
