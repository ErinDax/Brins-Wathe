package dev.isxander.yacl3.platform;

import dev.isxander.yacl3.gui.image.YACLImageReloadListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.class_3264;

public class PlatformEntrypoint implements ClientModInitializer {
   public void onInitializeClient() {
      YACLConfig.HANDLER.load();
      ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new YACLImageReloadListener());
   }
}
