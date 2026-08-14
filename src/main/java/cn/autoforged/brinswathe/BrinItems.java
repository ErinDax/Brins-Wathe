package cn.autoforged.brinswathe;

import cn.autoforged.brinswathe.item.BombItem;
import cn.autoforged.brinswathe.item.BoneKnifeItem;
import cn.autoforged.brinswathe.item.XueziItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class BrinItems {
    public static final Item BONE_KNIFE = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(BrinsWathe.MOD_ID, "bone_knife"),
        new BoneKnifeItem(new Item.Properties().stacksTo(1))
    );

    public static final Item XUEZI = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(BrinsWathe.MOD_ID, "xuezi"),
        new XueziItem(new Item.Properties().stacksTo(1))
    );

    public static final Item BOMB = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(BrinsWathe.MOD_ID, "bomb"),
        new BombItem(new Item.Properties().stacksTo(1))
    );

    private BrinItems() {
    }

    public static void init() {
    }
}
