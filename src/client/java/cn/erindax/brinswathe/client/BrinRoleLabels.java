package cn.erindax.brinswathe.client;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.agmas.harpymodloader.Harpymodloader;

public final class BrinRoleLabels {
    private BrinRoleLabels() {
    }

    public static MutableComponent of(String roleId) {
        Role role = find(roleId);
        if (role == null) {
            ResourceLocation id = ResourceLocation.tryParse(roleId);
            return Component.literal(id == null || roleId == null || roleId.isEmpty() ? "?" : id.getPath())
                .withColor(0xFFFFFF);
        }
        return of(role);
    }

    public static MutableComponent of(Role role) {
        ResourceLocation id = role.identifier();
        String namespaced = "announcement.role." + id.getNamespace() + "." + id.getPath();
        String plain = "announcement.role." + id.getPath();
        Language language = Language.getInstance();
        MutableComponent text = language.has(namespaced)
            ? Component.translatable(namespaced)
            : language.has(plain)
                ? Component.translatable(plain)
                : Harpymodloader.getRoleName(role).copy();
        return text.withColor(role.color());
    }

    public static Role find(String roleId) {
        if (roleId == null || roleId.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(roleId);
        if (id == null) return null;
        for (Role role : WatheRoles.ROLES) {
            if (id.equals(role.identifier())) return role;
        }
        return null;
    }
}
