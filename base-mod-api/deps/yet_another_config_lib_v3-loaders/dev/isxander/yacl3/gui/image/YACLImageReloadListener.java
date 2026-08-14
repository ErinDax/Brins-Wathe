package dev.isxander.yacl3.gui.image;

import dev.isxander.yacl3.gui.image.ImageRendererFactory.ImageSupplier;
import dev.isxander.yacl3.impl.utils.YACLConstants;
import dev.isxander.yacl3.platform.YACLPlatform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_3302;
import net.minecraft.class_3695;
import net.minecraft.class_3302.class_4045;
import org.jetbrains.annotations.NotNull;

public class YACLImageReloadListener implements class_3302, IdentifiableResourceReloadListener {
   @NotNull
   public CompletableFuture<Void> method_25931(
      class_4045 preparationBarrier,
      @NotNull class_3300 resourceManager,
      @NotNull class_3695 preparationsProfiler,
      @NotNull class_3695 reloadProfiler,
      @NotNull Executor backgroundExecutor,
      @NotNull Executor gameExecutor
   ) {
      return this.reload0(resourceManager, backgroundExecutor, preparationBarrier, gameExecutor);
   }

   @NotNull
   private CompletableFuture<Void> reload0(class_3300 resourceManager, Executor backgroundExecutor, class_4045 preparationBarrier, Executor gameExecutor) {
      return this.prepare(resourceManager, backgroundExecutor)
         .<List>thenCompose(preparationBarrier::method_18352)
         .thenCompose(suppliers -> this.apply((List<Optional<YACLImageReloadListener.SupplierPreparation>>)suppliers, gameExecutor));
   }

   private CompletableFuture<List<Optional<YACLImageReloadListener.SupplierPreparation>>> prepare(class_3300 manager, Executor executor) {
      Map<class_2960, class_3298> imageResources = manager.method_14488(
         "textures", location -> ImageRendererManager.PRELOADED_IMAGE_FACTORIES.stream().anyMatch(factory -> factory.predicate().test(location))
      );
      return imageResources.keySet()
         .stream()
         .map(
            location -> {
               ImageRendererFactory imageFactory = ImageRendererManager.PRELOADED_IMAGE_FACTORIES
                  .stream()
                  .filter(factory -> factory.predicate().test(location))
                  .map(factory -> factory.factory().apply(location))
                  .findAny()
                  .orElseThrow();
               return CompletableFuture.supplyAsync(
                  () -> ImageRendererManager.safelyPrepareFactory(location, imageFactory)
                        .map(supplier -> new YACLImageReloadListener.SupplierPreparation(location, supplier)),
                  executor
               );
            }
         )
         .collect(YACLImageReloadListener.CompletableFutureCollector.allOf());
   }

   private CompletableFuture<Void> apply(List<Optional<YACLImageReloadListener.SupplierPreparation>> suppliers, Executor executor) {
      return CompletableFuture.allOf(suppliers.stream().flatMap(Optional::stream).map(prep -> CompletableFuture.supplyAsync(() -> {
            ImageRenderer imageRenderer;
            try {
               imageRenderer = prep.supplier().completeImage();
            } catch (Exception var3) {
               YACLConstants.LOGGER.error("Failed to create image '{}'", prep.location(), var3);
               return Optional.empty();
            }

            ImageRendererManager.PRELOADED_IMAGE_CACHE.put(prep.location(), imageRenderer);
            YACLConstants.LOGGER.info("Successfully loaded image '{}'", prep.location());
            return Optional.of(imageRenderer);
         }, executor)).toArray(CompletableFuture[]::new));
   }

   public class_2960 getId() {
      return YACLPlatform.rl("image_reload_listener");
   }

   public class_2960 getFabricId() {
      return this.getId();
   }

   public static class CompletableFutureCollector<X, T extends CompletableFuture<X>> implements Collector<T, List<T>, CompletableFuture<List<X>>> {
      private CompletableFutureCollector() {
      }

      public static <X, T extends CompletableFuture<X>> Collector<T, List<T>, CompletableFuture<List<X>>> allOf() {
         return new YACLImageReloadListener.CompletableFutureCollector<>();
      }

      @Override
      public Supplier<List<T>> supplier() {
         return ArrayList::new;
      }

      @Override
      public BiConsumer<List<T>, T> accumulator() {
         return List::add;
      }

      @Override
      public BinaryOperator<List<T>> combiner() {
         return (left, right) -> {
            left.addAll(right);
            return left;
         };
      }

      @Override
      public Function<List<T>, CompletableFuture<List<X>>> finisher() {
         return ls -> CompletableFuture.allOf(ls.toArray(CompletableFuture[]::new)).thenApply(v -> ls.stream().map(CompletableFuture::join).toList());
      }

      @Override
      public Set<Characteristics> characteristics() {
         return Collections.emptySet();
      }
   }

   private static record SupplierPreparation(class_2960 location, ImageSupplier supplier) {
   }
}
