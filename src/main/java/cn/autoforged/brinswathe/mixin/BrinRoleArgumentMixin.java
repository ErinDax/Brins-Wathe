package cn.autoforged.brinswathe.mixin;

import com.mojang.brigadier.StringReader;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import net.minecraft.resources.ResourceLocation;
import org.agmas.harpymodloader.commands.argument.RoleArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RoleArgumentType.class)
public abstract class BrinRoleArgumentMixin {
    @Inject(method = "parse", at = @At("HEAD"), cancellable = true, remap = false)
    private void brinParseNamespacedRole(StringReader reader, CallbackInfoReturnable<Role> cir) {
        int start = reader.getCursor();
        String remaining = reader.getRemaining();
        int tokenLength = 0;
        while (tokenLength < remaining.length() && !Character.isWhitespace(remaining.charAt(tokenLength))) {
            tokenLength++;
        }
        if (tokenLength == 0) return;

        ResourceLocation identifier = ResourceLocation.tryParse(remaining.substring(0, tokenLength));
        if (identifier == null || !"brin".equals(identifier.getNamespace())) return;
        if ("trapper".equals(identifier.getPath()) || "beast_trapper".equals(identifier.getPath())) {
            identifier = ResourceLocation.fromNamespaceAndPath("brin", "beastcatcher");
        }

        for (Role role : WatheRoles.ROLES) {
            if (!identifier.equals(role.identifier())) continue;
            reader.setCursor(start + tokenLength);
            cir.setReturnValue(role);
            return;
        }
    }
}
