package cn.erindax.brinswathe.client.compat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import org.spongepowered.asm.util.Annotations;

@Environment(EnvType.CLIENT)
public final class MergedMethods {
    private MergedMethods() {
    }

    public static boolean isFrom(MethodNode method, String mixinClassName) {
        AnnotationNode merged = Annotations.getVisible(method, MixinMerged.class);
        return merged != null && mixinClassName.equals(Annotations.<String>getValue(merged, "mixin"));
    }
}
