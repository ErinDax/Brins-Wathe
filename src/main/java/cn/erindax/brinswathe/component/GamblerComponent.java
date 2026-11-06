package cn.erindax.brinswathe.component;

import cn.erindax.brinswathe.BrinFaction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class GamblerComponent implements AutoSyncedComponent {
    public static final ComponentKey<GamblerComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "gambler"),
        GamblerComponent.class
    );

    private final Player player;
    @Nullable
    private BrinFaction bet;

    public GamblerComponent(Player player) {
        this.player = player;
    }

    @Nullable
    public BrinFaction bet() {
        return this.bet;
    }

    public boolean placeBet(BrinFaction faction) {
        if (this.bet != null) return false;
        this.bet = faction;
        this.sync();
        return true;
    }

    public void reset() {
        if (this.bet == null) return;
        this.bet = null;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.bet = tag.contains("bet") ? BrinFaction.valueOf(tag.getString("bet")) : null;
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        if (this.bet != null) tag.putString("bet", this.bet.name());
    }
}
