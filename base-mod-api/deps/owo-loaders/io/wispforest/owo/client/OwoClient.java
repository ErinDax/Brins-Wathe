package io.wispforest.owo.client;

import io.wispforest.owo.Owo;
import io.wispforest.owo.client.screens.ScreenInternals.Client;
import io.wispforest.owo.config.OwoConfigCommand;
import io.wispforest.owo.itemgroup.json.OwoItemGroupLoader;
import io.wispforest.owo.moddata.ModDataLoader;
import io.wispforest.owo.shader.BlurProgram;
import io.wispforest.owo.shader.GlProgram;
import io.wispforest.owo.ui.parsing.UIModelLoader;
import io.wispforest.owo.ui.util.NinePatchTexture.MetadataLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.class_156;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3264;
import net.minecraft.class_156.class_158;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Environment(EnvType.CLIENT)
public class OwoClient implements ClientModInitializer {
   private static final String LINUX_RENDERDOC_WARNING = "\n========================================\nIgnored 'owo.renderdocPath' property as this Minecraft instance is not running on Windows.\nPlease populate the LD_PRELOAD environment variable instead\n========================================";
   private static final String MAC_RENDERDOC_WARNING = "\n========================================\nIgnored 'owo.renderdocPath' property as this Minecraft instance is not running on Windows.\nRenderDoc is not supported on macOS\n========================================";
   private static final String GENERIC_RENDERDOC_WARNING = "\n========================================\nIgnored 'owo.renderdocPath' property as this Minecraft instance is not running on Windows.\n========================================";
   public static final GlProgram HSV_PROGRAM = new GlProgram(class_2960.method_60655("owo", "spectrum"), class_290.field_1576);
   public static final BlurProgram BLUR_PROGRAM = new BlurProgram();

   public void onInitializeClient() {
      ModDataLoader.load(OwoItemGroupLoader.INSTANCE);
      ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new UIModelLoader());
      ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new MetadataLoader());
      String renderdocPath = System.getProperty("owo.renderdocPath");
      if (renderdocPath != null) {
         if (class_156.method_668() == class_158.field_1133) {
            System.load(renderdocPath);
         } else {
            Owo.LOGGER
               .warn(
                  switch (class_156.method_668()) {
                     case field_1135 -> "\n========================================\nIgnored 'owo.renderdocPath' property as this Minecraft instance is not running on Windows.\nPlease populate the LD_PRELOAD environment variable instead\n========================================";
                     case field_1137 -> "\n========================================\nIgnored 'owo.renderdocPath' property as this Minecraft instance is not running on Windows.\nRenderDoc is not supported on macOS\n========================================";
                     default -> "\n========================================\nIgnored 'owo.renderdocPath' property as this Minecraft instance is not running on Windows.\n========================================";
                  }
               );
         }
      }

      Client.init();
      ClientCommandRegistrationCallback.EVENT.register(OwoConfigCommand::register);
      if (Owo.DEBUG) {
         io.wispforest.owo.command.debug.OwoDebugCommands.Client.register();
      }
   }
}
