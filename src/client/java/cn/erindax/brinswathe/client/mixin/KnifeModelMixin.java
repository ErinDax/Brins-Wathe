package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.KnifeSkins;
import cn.erindax.brinswathe.client.KnifeExtraBakes;
import cn.erindax.brinswathe.client.KnifeItemModel;
import cn.erindax.brinswathe.client.KnifeSkinClient;
import dev.doctor4t.wathe.index.WatheCosmetics;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(targets = "dev.doctor4t.wathe.client.model.item.KnifeModel")
public abstract class KnifeModelMixin {
    @Unique
    private final Map<String, BakedModel[]> brinExtraModels = new HashMap<>();

    @Inject(method = "bake", at = @At("RETURN"), require = 0)
    private void brinBakeRegisteredSkins(
        ModelBaker baker,
        Function<Material, TextureAtlasSprite> sprites,
        ModelState state,
        CallbackInfoReturnable<BakedModel> cir
    ) {
        this.brinExtraModels.clear();
        for (String name : KnifeSkins.extraModelSkinNames()) {
            BakedModel[] variants = new BakedModel[2];
            variants[0] = baker.bake(KnifeSkinClient.modelId(name, false), state);
            variants[1] = baker.bake(KnifeSkinClient.modelId(name, true), state);
            this.brinExtraModels.put(name, variants);
        }
    }

    @Inject(method = "emitItemQuads", at = @At("HEAD"), cancellable = true, remap = false)
    private void brinEmitRegisteredKnife(
        ItemStack stack,
        Supplier<RandomSource> randomSupplier,
        RenderContext context,
        CallbackInfo ci
    ) {
        String raw = WatheCosmetics.getSkin(stack);
        if (raw == null) return;
        String skin = KnifeSkins.resolveKnifeSkinName(raw);
        if (skin == null || KnifeSkins.isOfficialSkin(skin)) return;
        if (!KnifeSkins.shouldBrinRender(skin)) return;

        boolean inHand = KnifeItemModel.isHeldDisplay(context.itemTransformationMode());
        BakedModel model = KnifeExtraBakes.get(skin, inHand);
        if (model == null) {
            BakedModel[] baked = this.brinExtraModels.get(skin);
            model = baked != null ? baked[inHand ? 1 : 0] : brinLookupModel(KnifeSkinClient.modelId(skin, inHand));
        }
        if (!(model instanceof FabricBakedModel fabricModel)) return;
        fabricModel.emitItemQuads(stack, randomSupplier, context);
        ci.cancel();
    }

    @Unique
    private static BakedModel brinLookupModel(ResourceLocation modelId) {
        ModelManager manager = Minecraft.getInstance().getModelManager();
        BakedModel missing = manager.getMissingModel();
        for (String variant : new String[] {"standalone", "inventory"}) {
            BakedModel model = manager.getModel(new ModelResourceLocation(modelId, variant));
            if (model != null && model != missing) return model;
        }
        return null;
    }
}
