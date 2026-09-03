package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.LanguageVariables;
import java.util.Map;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLanguage.class)
public class LanguageMixin {
    @Shadow
    @Final
    private Map<String, String> storage;

    @Inject(method = "getOrDefault", at = @At("RETURN"), cancellable = true)
    private void brinReplaceConfigVariables(String key, String fallback, CallbackInfoReturnable<String> callback) {
        String translation = callback.getReturnValue();
        if (!this.storage.containsKey(key)) {
            for (String alias : LanguageVariables.aliases(key)) {
                String aliasTranslation = this.storage.get(alias);
                if (aliasTranslation == null) continue;
                translation = aliasTranslation;
                break;
            }
        }
        callback.setReturnValue(LanguageVariables.replace(key, translation));
    }

    @Inject(method = "has", at = @At("RETURN"), cancellable = true)
    private void brinFindLanguageAlias(String key, CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValue()) return;
        for (String alias : LanguageVariables.aliases(key)) {
            if (!this.storage.containsKey(alias)) continue;
            callback.setReturnValue(true);
            return;
        }
    }
}
