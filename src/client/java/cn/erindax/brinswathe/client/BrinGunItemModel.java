package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinKnifeSkins;
import dev.doctor4t.wathe.index.WatheCosmetics;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class BrinGunItemModel implements BakedModel, FabricBakedModel {
    private final BakedModel wrapped;

    public BrinGunItemModel(BakedModel wrapped) {
        this.wrapped = wrapped instanceof BrinGunItemModel inner ? inner.wrapped : wrapped;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        String skin = BrinKnifeSkins.resolveGunSkinName(WatheCosmetics.getSkin(stack));
        if (BrinKnifeSkins.shouldBrinRenderGun(skin)) {
            BakedModel extra = BrinGunExtraBakes.get(skin);
            if (extra == null) extra = lookup(BrinKnifeSkinClient.gunModelId(skin));
            if (extra instanceof FabricBakedModel fabricModel) {
                fabricModel.emitItemQuads(stack, randomSupplier, context);
                return;
            }
        }
        if (wrapped instanceof FabricBakedModel fabricModel) {
            fabricModel.emitItemQuads(stack, randomSupplier, context);
        }
    }

    private static BakedModel lookup(ResourceLocation modelId) {
        ModelManager manager = Minecraft.getInstance().getModelManager();
        BakedModel missing = manager.getMissingModel();
        for (String variant : new String[] {"standalone", "inventory"}) {
            BakedModel model = manager.getModel(new ModelResourceLocation(modelId, variant));
            if (model != null && model != missing) return model;
        }
        return null;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        return wrapped.getQuads(state, face, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return wrapped.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return wrapped.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return wrapped.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return wrapped.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return wrapped.getOverrides();
    }
}
