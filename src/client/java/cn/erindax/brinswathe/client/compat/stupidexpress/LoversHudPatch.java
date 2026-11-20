package cn.erindax.brinswathe.client.compat.stupidexpress;

import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import org.spongepowered.asm.util.Annotations;

@Environment(EnvType.CLIENT)
public final class LoversHudPatch implements IMixinConfigPlugin {
    private static final String TARGET_CLASS = "dev.doctor4t.wathe.client.gui.RoleNameRenderer";
    private static final String HANDLER_MIXIN = "pro.fazeclan.river.stupid_express.client.mixin.modifier.lovers.LoversHudMixin";
    private static final String HANDLER_NAME_SUFFIX = "$renderLovers";
    private static final String LOOKUP_OWNER = "org/agmas/harpymodloader/component/WorldModifierComponent";
    private static final String LOOKUP_NAME = "getAllWithModifier";
    private static final String LOOKUP_DESC = "(Lorg/agmas/harpymodloader/modifiers/Modifier;)Ljava/util/List;";
    private static final String GUARD_OWNER = "cn/erindax/brinswathe/client/compat/stupidexpress/LoversHudGuard";
    private static final String GUARD_NAME = "loadedOnly";
    private static final String GUARD_DESC = "(Ljava/util/List;)Ljava/util/List;";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (!TARGET_CLASS.equals(targetClassName)) return;
        for (MethodNode method : targetClass.methods) {
            if (isLoversHandler(method)) guardLoverLookups(method);
        }
    }

    private static boolean isLoversHandler(MethodNode method) {
        if (!method.name.endsWith(HANDLER_NAME_SUFFIX)) return false;
        AnnotationNode merged = Annotations.getVisible(method, MixinMerged.class);
        return merged != null && HANDLER_MIXIN.equals(Annotations.<String>getValue(merged, "mixin"));
    }

    private static void guardLoverLookups(MethodNode method) {
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (!isCall(insn, LOOKUP_OWNER, LOOKUP_NAME, LOOKUP_DESC)) continue;
            if (isCall(insn.getNext(), GUARD_OWNER, GUARD_NAME, GUARD_DESC)) continue;
            method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, GUARD_OWNER, GUARD_NAME, GUARD_DESC, false));
        }
    }

    private static boolean isCall(AbstractInsnNode insn, String owner, String name, String desc) {
        return insn instanceof MethodInsnNode call
            && call.owner.equals(owner)
            && call.name.equals(name)
            && call.desc.equals(desc);
    }
}
