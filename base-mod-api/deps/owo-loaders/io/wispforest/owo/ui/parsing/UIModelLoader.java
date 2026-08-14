package io.wispforest.owo.ui.parsing;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_124;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3300;
import net.minecraft.class_4013;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.xml.sax.SAXException;

public class UIModelLoader implements class_4013, IdentifiableResourceReloadListener {
   private static final Map<class_2960, UIModel> LOADED_MODELS = new HashMap<>();
   private static final Jankson JANKSON = Jankson.builder()
      .registerSerializer(Path.class, (path, marshaller) -> JsonPrimitive.of(path.toString()))
      .registerSerializer(class_2960.class, (identifier, marshaller) -> new JsonPrimitive(identifier.toString()))
      .build();
   private static final Path HOT_RELOAD_LOCATIONS_PATH = FabricLoader.getInstance().getConfigDir().resolve("owo_ui_hot_reload_locations.json5");
   private static final Map<class_2960, Path> HOT_RELOAD_LOCATIONS = new HashMap<>();
   private static boolean loadedOnce = false;

   @Nullable
   public static UIModel get(class_2960 id) {
      if (Owo.DEBUG && HOT_RELOAD_LOCATIONS.containsKey(id)) {
         try {
            UIModel var2;
            try (InputStream stream = Files.newInputStream(HOT_RELOAD_LOCATIONS.get(id))) {
               var2 = UIModel.load(stream);
            }

            return var2;
         } catch (IOException | SAXException | ParserConfigurationException var6) {
            class_310.method_1551()
               .field_1724
               .method_7353(
                  TextOps.concat(Owo.PREFIX, TextOps.withFormatting("hot ui model reload failed, check the log for details", class_124.field_1061)), false
               );
            Owo.LOGGER.error("Hot UI model reload failed", var6);
         }
      }

      return getPreloaded(id);
   }

   @Nullable
   public static UIModel getPreloaded(class_2960 id) {
      return LOADED_MODELS.getOrDefault(id, null);
   }

   public static void setHotReloadPath(class_2960 modelId, @Nullable Path reloadPath) {
      if (reloadPath != null) {
         HOT_RELOAD_LOCATIONS.put(modelId, reloadPath);
      } else {
         HOT_RELOAD_LOCATIONS.remove(modelId);
      }

      try {
         Files.writeString(HOT_RELOAD_LOCATIONS_PATH, JANKSON.toJson(HOT_RELOAD_LOCATIONS).toJson(JsonGrammar.JSON5));
      } catch (IOException var3) {
         Owo.LOGGER.warn("Could not save hot reload locations", var3);
      }
   }

   @Nullable
   public static Path getHotReloadPath(class_2960 modelId) {
      return HOT_RELOAD_LOCATIONS.get(modelId);
   }

   public static Set<class_2960> allLoadedModels() {
      return Collections.unmodifiableSet(LOADED_MODELS.keySet());
   }

   public class_2960 getFabricId() {
      return Owo.id("ui-model-loader");
   }

   public void method_14491(class_3300 manager) {
      LOADED_MODELS.clear();
      manager.method_14488("owo_ui", identifier -> identifier.method_12832().endsWith(".xml"))
         .forEach(
            (resourceId, resource) -> {
               try {
                  class_2960 modelId = class_2960.method_60655(
                     resourceId.method_12836(), resourceId.method_12832().substring(7, resourceId.method_12832().length() - 4)
                  );
                  LOADED_MODELS.put(modelId, UIModel.load(resource.method_14482()));
               } catch (IOException | SAXException | ParserConfigurationException var3) {
                  Owo.LOGGER.error("Couldn't parse UI model {}", resourceId, var3);
               }
            }
         );
      loadedOnce = true;
   }

   @Internal
   public static boolean hasCompletedInitialLoad() {
      return loadedOnce;
   }

   static {
      if (Owo.DEBUG && Files.exists(HOT_RELOAD_LOCATIONS_PATH)) {
         try (InputStream stream = Files.newInputStream(HOT_RELOAD_LOCATIONS_PATH)) {
            JsonObject associations = JANKSON.load(stream);
            associations.forEach((key, value) -> {
               if (value instanceof JsonPrimitive primitive) {
                  HOT_RELOAD_LOCATIONS.put(class_2960.method_60654(key), Path.of(primitive.asString()));
               }
            });
         } catch (SyntaxError | IOException var5) {
         }
      }
   }
}
