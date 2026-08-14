package cn.autoforged.brinswathe;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class BrinSounds {
    public static final SoundEvent COWBOY_SHOWDOWN = register("cowboy_showdown");

    private BrinSounds() {
    }

    public static void init() {
    }

    private static SoundEvent register(String id) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(BrinsWathe.MOD_ID, id);
        return Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            location,
            SoundEvent.createVariableRangeEvent(location)
        );
    }
}
