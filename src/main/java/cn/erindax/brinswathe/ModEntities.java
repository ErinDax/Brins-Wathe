package cn.erindax.brinswathe;

import cn.erindax.brinswathe.entity.ZhuimuBoat;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final EntityType<ZhuimuBoat> ZHUIMU_BOAT = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(BrinsWathe.MOD_ID, "zhuimu_boat"),
        EntityType.Builder.<ZhuimuBoat>of(ZhuimuBoat::new, MobCategory.MISC)
            .sized(1.375F, 0.5625F)
            .eyeHeight(0.5625F)
            .clientTrackingRange(10)
            .noSave()
            .noSummon()
            .build("zhuimu_boat")
    );

    private ModEntities() {
    }

    public static void init() {
    }
}
