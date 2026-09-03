package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.BrinKnifeSkins;
import cn.erindax.brinswathe.client.BrinKnifeSkinClient;
import dev.doctor4t.wathe.client.model.item.KnifeModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(targets = "dev.doctor4t.wathe.client.model.item.KnifeModelLoadingPlugin", remap = false)
public abstract class BrinFakeKnifeModelMixin {
    @Inject(method = "onInitializeModelLoader", at = @At("TAIL"))
    private void brinRegisterFakeKnifeModel(ModelLoadingPlugin.Context pluginContext, CallbackInfo ci) {
        for (String name : BrinKnifeSkins.extraModelSkinNames()) {
            pluginContext.addModels(
                BrinKnifeSkinClient.modelId(name, false),
                BrinKnifeSkinClient.modelId(name, true)
            );
        }
        ModelResourceLocation fakeKnifeModelId = new ModelResourceLocation(
            ResourceLocation.fromNamespaceAndPath("noellesroles", "fake_knife"),
            "inventory"
        );
        pluginContext.modifyModelOnLoad().register((unbakedModel, context) -> {
            if (fakeKnifeModelId.equals(context.topLevelId())) {
                return new KnifeModel(unbakedModel);
            }
            return unbakedModel;
        });
    }
}
