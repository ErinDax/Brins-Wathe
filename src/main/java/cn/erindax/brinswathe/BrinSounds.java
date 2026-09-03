package cn.erindax.brinswathe;

import dev.doctor4t.wathe.index.WatheCosmetics;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

public final class BrinSounds {
    public static final SoundEvent COWBOY_SHOWDOWN = register("cowboy_showdown");
    public static final SoundEvent KNIFE_PREPARE_PURUISAISSI = register("item.knife.prepare.puruisaissi");
    public static final SoundEvent KNIFE_PREPARE_MAOMOCHUI = register("item.knife.prepare.maomochui");

    private BrinSounds() {
    }

    public static void init() {
    }

    public static SoundEvent knifePrepare(ItemStack stack) {
        String skin = BrinKnifeSkins.resolveKnifeSkinName(WatheCosmetics.getSkin(stack));
        if (isPuruisaissi(skin)) return KNIFE_PREPARE_PURUISAISSI;
        if (isMaomochui(skin)) return KNIFE_PREPARE_MAOMOCHUI;
        return WatheSounds.ITEM_KNIFE_PREPARE;
    }

    public static float knifePrepareVolume(ItemStack stack) {
        String skin = BrinKnifeSkins.resolveKnifeSkinName(WatheCosmetics.getSkin(stack));
        if (isPuruisaissi(skin) || isMaomochui(skin)) return 0.2F;
        return 1.0F;
    }

    private static boolean isPuruisaissi(String skin) {
        return "puruisaissi".equalsIgnoreCase(skin) || "普瑞赛斯".equals(skin);
    }

    private static boolean isMaomochui(String skin) {
        return "maomochui".equalsIgnoreCase(skin) || "猫陌锤".equals(skin);
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
