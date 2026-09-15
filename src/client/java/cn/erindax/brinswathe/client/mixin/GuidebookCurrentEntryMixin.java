package cn.erindax.brinswathe.client.mixin;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.aussiebox.starexpress.util.RoleInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(targets = "org.aussiebox.starexpress.client.gui.screen.GuidebookScreen", remap = false)
public abstract class GuidebookCurrentEntryMixin {
    @Shadow
    public Map<String, RoleInfo> roleInfo;

    @Shadow
    public abstract void openToEntry(String id);

    @Inject(method = "build(Lio/wispforest/owo/ui/container/FlowLayout;)V", at = @At("RETURN"))
    private void brinJumpToCurrent(CallbackInfo ci) {
        String jumpId = brinCurrentJumpId();
        if (jumpId == null) return;
        brinEnsureQuickTravel();
        brinOpen(jumpId);
        brinExpandQuickTravel();
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            if (client.screen != (Object) this) return;
            brinOpen(jumpId);
            brinExpandQuickTravel();
        });
    }

    @Unique
    private String brinCurrentJumpId() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || this.roleInfo == null || this.roleInfo.isEmpty()) return null;
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        if (game == null || !game.isRunning()) return null;
        Role role = game.getRole(player);
        if (role != null) {
            String roleId = role.identifier().toString();
            if (this.roleInfo.containsKey(roleId)) return roleId;
        }
        WorldModifierComponent modifiers = WorldModifierComponent.KEY.get(player.level());
        if (modifiers == null) return null;
        List<Modifier> list = modifiers.getModifiers(player);
        if (list == null) return null;
        for (Modifier modifier : list) {
            if (modifier == null) continue;
            String modifierId = modifier.identifier().toString();
            if (this.roleInfo.containsKey(modifierId)) return modifierId;
        }
        return null;
    }

    @Unique
    private void brinOpen(String jumpId) {
        try {
            this.openToEntry(jumpId);
        } catch (RuntimeException ignored) {
        }
    }

    @Unique
    private void brinEnsureQuickTravel() {
        try {
            Class<?> screenClass = this.getClass();
            if (brinInvoke(screenClass, "getQuickTravelList") != null) return;
            Field field = screenClass.getDeclaredField("quickTravelList");
            field.setAccessible(true);
            Object template = field.get(this);
            if (template == null) return;
            for (Method method : screenClass.getMethods()) {
                if (!"setQuickTravelList".equals(method.getName()) || method.getParameterCount() != 1) continue;
                method.invoke(this, template);
                return;
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @Unique
    private void brinExpandQuickTravel() {
        try {
            Object list = brinInvoke(this.getClass(), "getQuickTravelList");
            if (list == null) return;
            Class<?> collapsible = Class.forName("io.wispforest.owo.ui.container.CollapsibleContainer");
            Method childById = list.getClass().getMethod("childById", Class.class, String.class);
            Object travel = childById.invoke(list, collapsible, "quick_travel_container");
            if (travel == null) return;
            if (Boolean.TRUE.equals(collapsible.getMethod("expanded").invoke(travel))) return;
            collapsible.getMethod("toggleExpansion").invoke(travel);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @Unique
    private Object brinInvoke(Class<?> screenClass, String name) throws ReflectiveOperationException {
        return screenClass.getMethod(name).invoke(this);
    }
}
