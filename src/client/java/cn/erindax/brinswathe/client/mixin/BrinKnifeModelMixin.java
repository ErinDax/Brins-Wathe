package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.BrinKnifeSkins;
import cn.erindax.brinswathe.client.BrinKnifeExtraBakes;
import cn.erindax.brinswathe.client.BrinKnifeItemModel;
import cn.erindax.brinswathe.client.BrinKnifeSkinClient;
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
public abstract class BrinKnifeModelMixin {
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
        for (String name : BrinKnifeSkins.extraModelSkinNames()) {
            BakedModel[] variants = new BakedModel[2];
            variants[0] = baker.bake(BrinKnifeSkinClient.modelId(name, false), state);
            variants[1] = baker.bake(BrinKnifeSkinClient.modelId(name, true), state);
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
        String skin = BrinKnifeSkins.resolveKnifeSkinName(raw);
        if (skin == null || BrinKnifeSkins.isOfficialSkin(skin)) return;
        if (!BrinKnifeSkins.shouldBrinRender(skin)) return;

        boolean inHand = BrinKnifeItemModel.isHeldDisplay(context.itemTransformationMode());
        BakedModel model = BrinKnifeExtraBakes.get(skin, inHand);
        if (model == null) {
            BakedModel[] baked = this.brinExtraModels.get(skin);
            model = baked != null ? baked[inHand ? 1 : 0] : brinLookupModel(BrinKnifeSkinClient.modelId(skin, inHand));
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
