package cn.erindax.brinswathe.client.compat.kinswathe;

import cn.erindax.brinswathe.client.compat.MergedMethods;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

@Environment(EnvType.CLIENT)
public final class KnifeAttackIconPatch {
    private static final String TARGET_CLASS = "dev.doctor4t.wathe.client.gui.CrosshairRenderer";
    private static final String DRAW_MIXIN = "org.BsXinQin.kinswathe.client.mixin.gui.ItemCrosshairMixin";
    private static final String GUARD_MIXIN = "cn.erindax.brinswathe.client.mixin.KinsCrosshairMixin";
    private static final String GUARD_NAME = "brinDrawUnlessKnifeAttack";

    private KnifeAttackIconPatch() {
    }

    public static void apply(String targetClassName, ClassNode targetClass) {
        if (!TARGET_CLASS.equals(targetClassName)) return;
        MethodNode guard = findGuard(targetClass);
        if (guard == null) return;
        MethodInsnNode guarded = findGuardedCall(guard);
        if (guarded == null) return;
        for (MethodNode method : targetClass.methods) {
            if (MergedMethods.isFrom(method, DRAW_MIXIN)) redirectToGuard(method, guarded, targetClass.name, guard);
        }
    }

    private static MethodNode findGuard(ClassNode targetClass) {
        for (MethodNode method : targetClass.methods) {
            if (method.name.equals(GUARD_NAME) && MergedMethods.isFrom(method, GUARD_MIXIN)) return method;
        }
        return null;
    }

    private static MethodInsnNode findGuardedCall(MethodNode guard) {
        for (AbstractInsnNode insn : guard.instructions) {
            if (insn instanceof MethodInsnNode call
                && call.getOpcode() == Opcodes.INVOKEVIRTUAL
                && guard.desc.equals("(L" + call.owner + ";" + call.desc.substring(1))) {
                return call;
            }
        }
        return null;
    }

    private static void redirectToGuard(MethodNode method, MethodInsnNode guarded, String guardOwner, MethodNode guard) {
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (!isSameCall(insn, guarded)) continue;
            method.instructions.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, guardOwner, guard.name, guard.desc, false));
        }
    }

    private static boolean isSameCall(AbstractInsnNode insn, MethodInsnNode expected) {
        return insn instanceof MethodInsnNode call
            && call.getOpcode() == expected.getOpcode()
            && call.owner.equals(expected.owner)
            && call.name.equals(expected.name)
            && call.desc.equals(expected.desc);
    }
}
