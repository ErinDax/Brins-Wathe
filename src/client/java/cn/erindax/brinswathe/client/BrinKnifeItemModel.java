package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinKnifeSkins;
import dev.doctor4t.wathe.index.WatheCosmetics;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class BrinKnifeItemModel implements BakedModel, FabricBakedModel {
    private final BakedModel wrapped;
    public BrinKnifeItemModel(BakedModel wrapped) {
        this.wrapped = wrapped instanceof BrinKnifeItemModel inner ? inner.wrapped : wrapped;
    }
    public static boolean isHeldDisplay(ItemDisplayContext mode) {
        return mode.firstPerson()
            || mode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
            || mode == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
            || mode == ItemDisplayContext.HEAD
            || mode == ItemDisplayContext.FIXED;
    }
    @Override
    public boolean isVanillaAdapter() {
        return false;
    }
    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        String skin = BrinKnifeSkins.resolveKnifeSkinName(WatheCosmetics.getSkin(stack));
        if (BrinKnifeSkins.shouldBrinRender(skin)) {
            BakedModel extra = BrinKnifeExtraBakes.get(skin, isHeldDisplay(context.itemTransformationMode()));
            if (extra instanceof FabricBakedModel fabricModel) {
                fabricModel.emitItemQuads(stack, randomSupplier, context);
                return;
            }
        }
        if (wrapped instanceof FabricBakedModel fabricModel) {
            fabricModel.emitItemQuads(stack, randomSupplier, context);
        }
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
