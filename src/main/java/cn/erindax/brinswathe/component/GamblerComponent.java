package cn.erindax.brinswathe.component;

import java.util.UUID;
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
    private UUID betTarget;
    private String betName = "";

    public GamblerComponent(Player player) {
        this.player = player;
    }

    @Nullable
    public UUID betTarget() {
        return this.betTarget;
    }

    public String betName() {
        return this.betName;
    }

    public boolean placeBet(UUID targetId, String targetName) {
        if (this.betTarget != null) return false;
        this.betTarget = targetId;
        this.betName = targetName;
        this.sync();
        return true;
    }

    public void reset() {
        if (this.betTarget == null) return;
        this.betTarget = null;
        this.betName = "";
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.betTarget = tag.hasUUID("betTarget") ? tag.getUUID("betTarget") : null;
        this.betName = tag.getString("betName");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        if (this.betTarget == null) return;
        tag.putUUID("betTarget", this.betTarget);
        tag.putString("betName", this.betName);
    }
}
