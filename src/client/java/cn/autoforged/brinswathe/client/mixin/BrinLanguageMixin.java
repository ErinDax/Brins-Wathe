package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.client.BrinLanguageVariables;
import java.util.Map;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLanguage.class)
public class BrinLanguageMixin {
    @Shadow
    @Final
    private Map<String, String> storage;

    @Inject(method = "getOrDefault", at = @At("RETURN"), cancellable = true)
    private void brinReplaceConfigVariables(String key, String fallback, CallbackInfoReturnable<String> callback) {
        String translation = callback.getReturnValue();
        if (!this.storage.containsKey(key)) {
            for (String alias : BrinLanguageVariables.aliases(key)) {
                String aliasTranslation = this.storage.get(alias);
                if (aliasTranslation == null) continue;
                translation = aliasTranslation;
                break;
            }
        }
        callback.setReturnValue(BrinLanguageVariables.replace(key, translation));
    }

    @Inject(method = "has", at = @At("RETURN"), cancellable = true)
    private void brinFindLanguageAlias(String key, CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValue()) return;
        for (String alias : BrinLanguageVariables.aliases(key)) {
            if (!this.storage.containsKey(alias)) continue;
            callback.setReturnValue(true);
            return;
        }
    }
}
