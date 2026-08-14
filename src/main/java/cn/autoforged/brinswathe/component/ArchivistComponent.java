package cn.autoforged.brinswathe.component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class ArchivistComponent implements AutoSyncedComponent {
    public static final ComponentKey<ArchivistComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "archivist"),
        ArchivistComponent.class
    );

    private final Player player;
    public UUID sealTarget = null;
    public Set<UUID> sealedCorpses = new HashSet<>();

    public ArchivistComponent(Player player) {
        this.player = player;
    }

    public void reset() {
        this.sealTarget = null;
        this.sealedCorpses.clear();
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        if (tag.hasUUID("sealTarget")) {
            this.sealTarget = tag.getUUID("sealTarget");
        } else {
            this.sealTarget = null;
        }
        this.sealedCorpses.clear();
        ListTag list = tag.getList("sealedCorpses", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            this.sealedCorpses.add(list.getCompound(i).getUUID("uuid"));
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        if (this.sealTarget != null) {
            tag.putUUID("sealTarget", this.sealTarget);
        }
        ListTag list = new ListTag();
        for (UUID id : this.sealedCorpses) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", id);
            list.add(entry);
        }
        tag.put("sealedCorpses", list);
    }
}
