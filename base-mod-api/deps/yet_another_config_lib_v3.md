# yet_another_config_lib_v3(自动附带的依赖)

> `yet_another_config_lib_v3` 是「HarpyModLoader」自身需要的运行前置,构建环境自动带上,**一般无需直接调用**。来自 `maven.modrinth:yacl:o3cDn8Vp`。

## 查任意类的签名与 JVM 描述符

```sh
./base-mod-api/lookup <全限定类名>
```

输出里每个方法下的 `descriptor:` 就是它的 **JVM 方法描述符**。写 mixin 的 `@At(target = "L包/路径/类;方法名描述符")` 时**原样复制那一行**,**绝不要**自己把 `(int, int, double)` 心算成 `(IID)` —— 参数顺序数错一位,mixin 注入就会失败,而**编译器不报错、datagen 也不报错**(`@At` 的 target 只是字符串;客户端类在 `runData` 里根本不加载),玩家一进游戏就崩。

## 🚨 前置的数据加载器 —— **它们决定了你必须创建哪些文件**

已把 `yet_another_config_lib_v3` 的资源加载器反编译到本地,**动手前必读**:

- `./base-mod-api/yet_another_config_lib_v3-loaders/dev/isxander/yacl3/gui/image/YACLImageReloadListener.java`
- `./base-mod-api/yet_another_config_lib_v3-loaders/dev/isxander/yacl3/platform/PlatformEntrypoint.java`

**这些类的代码里写着前置要读哪些 `data/` 目录**(通常在构造函数或 `prepare()` 里,形如 `super(GSON, "xxx/yyy")`、`findResources("zzz", …)`)。

读完它们,再对照 `./base-mod-api/data`(前置自己**带了**哪些数据文件),问一个问题:

> **加载器要读、但前置自己没有的那些目录 —— 那就是它在等我创建的。**

🚨 **这是最容易漏、漏了最致命的一类。** 不创建的下场(实测两次):你的物品打了 tag、继承了前置的类、**编译通过、datagen 通过、runServer 通过、游戏也不报错** —— 但前置的系统**根本认不出你**。典型:玩家身上压根没有那个槽位,饰品永远放不进去。

**这类目录在前置的 jar 里是不存在的**,所以 `./base-mod-api/data` 扫不出来 —— 它们是「等你去建」的。而且**每个前置不一样**:有的默认就给你开好了(那就别多此一举),有的必须你自己请求 —— **看代码,别猜**。

## `api` 包的公开签名(作者明确留给外部调用的接口)

这里只预导了 `api` 包;其它类用上面的 `lookup` 查。

```
<!-- javap 范围: api 包;共 79 个类 -->
Compiled from "Binding.java"
public interface dev.isxander.yacl3.api.Binding<T> {
  public abstract void setValue(T);
    descriptor: (Ljava/lang/Object;)V

  public abstract T getValue();
    descriptor: ()Ljava/lang/Object;

  public abstract T defaultValue();
    descriptor: ()Ljava/lang/Object;

  public default <U> dev.isxander.yacl3.api.Binding<U> xmap(java.util.function.Function<T, U>, java.util.function.Function<U, T>);
    descriptor: (Ljava/util/function/Function;Ljava/util/function/Function;)Ldev/isxander/yacl3/api/Binding;

  public static <T> dev.isxander.yacl3.api.Binding<T> generic(T, java.util.function.Supplier<T>, java.util.function.Consumer<T>);
    descriptor: (Ljava/lang/Object;Ljava/util/function/Supplier;Ljava/util/function/Consumer;)Ldev/isxander/yacl3/api/Binding;

  public static <T> dev.isxander.yacl3.api.Binding<T> minecraft(net.minecraft.class_7172<T>);
    descriptor: (Lnet/minecraft/class_7172;)Ldev/isxander/yacl3/api/Binding;

  public static <T> dev.isxander.yacl3.api.Binding<T> immutable(T);
    descriptor: (Ljava/lang/Object;)Ldev/isxander/yacl3/api/Binding;
}
Compiled from "ButtonOption.java"
public interface dev.isxander.yacl3.api.ButtonOption extends dev.isxander.yacl3.api.Option<java.util.function.BiConsumer<dev.isxander.yacl3.gui.YACLScreen, dev.isxander.yacl3.api.ButtonOption>> {
  public abstract java.util.function.BiConsumer<dev.isxander.yacl3.gui.YACLScreen, dev.isxander.yacl3.api.ButtonOption> action();
    descriptor: ()Ljava/util/function/BiConsumer;

  public static dev.isxander.yacl3.api.ButtonOption$Builder createBuilder();
    descriptor: ()Ldev/isxander/yacl3/api/ButtonOption$Builder;
}
Compiled from "ConfigCategory.java"
public interface dev.isxander.yacl3.api.ConfigCategory {
  public abstract net.minecraft.class_2561 name();
    descriptor: ()Lnet/minecraft/class_2561;

  public abstract com.google.common.collect.ImmutableList<dev.isxander.yacl3.api.OptionGroup> groups();
    descriptor: ()Lcom/google/common/collect/ImmutableList;

  public abstract net.minecraft.class_2561 tooltip();
    descriptor: ()Lnet/minecraft/class_2561;

  public static dev.isxander.yacl3.api.ConfigCategory$Builder createBuilder();
    descriptor: ()Ldev/isxander/yacl3/api/ConfigCategory$Builder;
}
Compiled from "Controller.java"
public interface dev.isxander.yacl3.api.Controller<T> {
  public abstract dev.isxander.yacl3.api.Option<T> option();
    descriptor: ()Ldev/isxander/yacl3/api/Option;

  public abstract net.minecraft.class_2561 formatValue();
    descriptor: ()Lnet/minecraft/class_2561;

  public abstract dev.isxander.yacl3.gui.AbstractWidget provideWidget(dev.isxander.yacl3.gui.YACLScreen, dev.isxander.yacl3.api.utils.Dimension<java.lang.Integer>);
    descriptor: (Ldev/isxander/yacl3/gui/YACLScreen;Ldev/isxander/yacl3/api/utils/Dimension;)Ldev/isxander/yacl3/gui/AbstractWidget;
}
Compiled from "CustomTabProvider.java"
public interface dev.isxander.yacl3.api.CustomTabProvider {
  public abstract net.minecraft.class_8087 createTab(dev.isxander.yacl3.gui.YACLScreen, net.minecraft.class_8030);
    descriptor: (Ldev/isxander/yacl3/gui/YACLScreen;Lnet/minecraft/class_8030;)Lnet/minecraft/class_8087;
}
Compiled from "LabelOption.java"
public interface dev.isxander.yacl3.api.LabelOption extends dev.isxander.yacl3.api.Option<net.minecraft.class_2561> {
  public abstract net.minecraft.class_2561 label();
    descriptor: ()Lnet/minecraft/class_2561;

  public static dev.isxander.yacl3.api.LabelOption create(net.minecraft.class_2561);
    descriptor: (Lnet/minecraft/class_2561;)Ldev/isxander/yacl3/api/LabelOption;

  public static dev.isxander.yacl3.api.LabelOption$Builder createBuilder();
    descriptor: ()Ldev/isxander/yacl3/api/LabelOption$Builder;
}
Compiled from "ListOption.java"
public interface dev.isxander.yacl3.api.ListOption<T> extends dev.isxander.yacl3.api.OptionGroup, dev.isxander.yacl3.api.Option<java.util.List<T>> {
  public abstract com.google.common.collect.ImmutableList<dev.isxander.yacl3.api.ListOptionEntry<T>> options();
    descriptor: ()Lcom/google/common/collect/ImmutableList;

  public abstract int numberOfEntries();
    descriptor: ()I

  public abstract int maximumNumberOfEntries();
    descriptor: ()I

  public abstract int minimumNumberOfEntries();
    descriptor: ()I

  public abstract dev.isxander.yacl3.api.ListOptionEntry<T> insertNewEntry();
    descriptor: ()Ldev/isxander/yacl3/api/ListOptionEntry;

  public abstract void insertEntry(int, dev.isxander.yacl3.api.ListOptionEntry<?>);
    descriptor: (ILdev/isxander/yacl3/api/ListOptionEntry;)V

  public abstract int indexOf(dev.isxander.yacl3.api.ListOptionEntry<?>);
    descriptor: (Ldev/isxander/yacl3/api/ListOptionEntry;)I

  public abstract void removeEntry(dev.isxander.yacl3.api.ListOptionEntry<?>);
    descriptor: (Ldev/isxander/yacl3/api/ListOptionEntry;)V

  public abstract void addRefreshListener(java.lang.Runnable);
    descriptor: (Ljava/lang/Runnable;)V

  public static <T> dev.isxander.yacl3.api.ListOption$Builder<T> createBuilder();
    descriptor: ()Ldev/isxander/yacl3/api/ListOption$Builder;

  public static <T> dev.isxander.yacl3.api.ListOption$Builder<T> createBuilder(java.lang.Class<T>);
    descriptor: (Ljava/lang/Class;)Ldev/isxander/yacl3/api/ListOption$Builder;
}
Compiled from "ListOptionEntry.java"
public interface dev.isxander.yacl3.api.ListOptionEntry<T> extends dev.isxander.yacl3.api.Option<T> {
  public abstract dev.isxander.yacl3.api.ListOption<T> parentGroup();
    descriptor: ()Ldev/isxander/yacl3/api/ListOption;

  public default com.google.common.collect.ImmutableSet<dev.isxander.yacl3.api.OptionFlag> flags();
    descriptor: ()Lcom/google/common/collect/ImmutableSet;

  public default boolean available();
    descriptor: ()Z
}
Compiled from "NameableEnum.java"
public interface dev.isxander.yacl3.api.NameableEnum {
  public abstract net.minecraft.class_2561 getDisplayName();
    descriptor: ()Lnet/minecraft/class_2561;
}
Compiled from "Option.java"
public interface dev.isxander.yacl3.api.Option<T> {
  public abstract net.minecraft.class_2561 name();
    descriptor: ()Lnet/minecraft/class_2561;

  public abstract dev.isxander.yacl3.api.OptionDescription description();
    descriptor: ()Ldev/isxander/yacl3/api/OptionDescription;

  public abstract net.minecraft.class_2561 tooltip();
    descriptor: ()Lnet/minecraft/class_2561;

  public abstract dev.isxander.yacl3.api.Controller<T> controller();
    descriptor: ()Ldev/isxander/yacl3/api/Controller;

  public abstract dev.isxander.yacl3.api.StateManager<T> stateManager();
    descriptor: ()Ldev/isxander/yacl3/api/StateManager;

  public abstract dev.isxander.yacl3.api.Binding<T> binding();
    descriptor: ()Ldev/isxander/yacl3/api/Binding;

  public abstract boolean available();
    descriptor: ()Z

  public abstract void setAvailable(boolean);
    descriptor: (Z)V

  public abstract com.google.common.collect.ImmutableSet<dev.isxander.yacl3.api.OptionFlag> flags();
    descriptor: ()Lcom/google/common/collect/ImmutableSet;

  public abstract boolean changed();
    descriptor: ()Z

  public abstract T pendingValue();
    descriptor: ()Ljava/lang/Object;

  public abstract void requestSet(T);
    descriptor: (Ljava/lang/Object;)V

  public abstract boolean applyValue();
    descriptor: ()Z

  public abstract void forgetPendingValue();
    descriptor: ()V

  public abstract void requestSetDefault();
    descriptor: ()V

  public abstract boolean isPendingValueDefault();
    descriptor: ()Z

  public default boolean canResetToDefault();
    descriptor: ()Z

  public abstract void addEventListener(dev.isxander.yacl3.api.OptionEventListener<T>);
    descriptor: (Ldev/isxander/yacl3/api/OptionEventListener;)V

  public abstract void addListener(java.util.function.BiConsumer<dev.isxander.yacl3.api.Option<T>, T>);
    descriptor: (Ljava/util/function/BiConsumer;)V

  public static <T> dev.isxander.yacl3.api.Option$Builder<T> createBuilder();
    descriptor: ()Ldev/isxander/yacl3/api/Option$Builder;

  public static <T> dev.isxander.yacl3.api.Option$Builder<T> createBuilder(java.lang.Class<T>);
    descriptor: (Ljava/lang/Class;)Ldev/isxander/yacl3/api/Option$Builder;
}
Compiled from "OptionAddable.java"
public interface dev.isxander.yacl3.api.OptionAddable {
  public abstract dev.isxander.yacl3.api.OptionAddable option(dev.isxander.yacl3.api.Option<?>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/OptionAddable;

  public default dev.isxander.yacl3.api.OptionAddable option(java.util.function.Supplier<dev.isxander.yacl3.api.Option<?>>);
    descriptor: (Ljava/util/function/Supplier;)Ldev/isxander/yacl3/api/OptionAddable;

  public default dev.isxander.yacl3.api.OptionAddable optionIf(boolean, dev.isxander.yacl3.api.Option<?>);
    descriptor: (ZLdev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/OptionAddable;

  public default dev.isxander.yacl3.api.OptionAddable optionIf(boolean, java.util.function.Supplier<dev.isxander.yacl3.api.Option<?>>);
    descriptor: (ZLjava/util/function/Supplier;)Ldev/isxander/yacl3/api/OptionAddable;

  public abstract dev.isxander.yacl3.api.OptionAddable options(java.util.Collection<? extends dev.isxander.yacl3.api.Option<?>>);
    descriptor: (Ljava/util/Collection;)Ldev/isxander/yacl3/api/OptionAddable;

  public default dev.isxander.yacl3.api.OptionAddable optionsIf(boolean, java.util.Collection<? extends dev.isxander.yacl3.api.Option<?>>);
    descriptor: (ZLjava/util/Collection;)Ldev/isxander/yacl3/api/OptionAddable;
}
Compiled from "OptionDescription.java"
public interface dev.isxander.yacl3.api.OptionDescription {
  public static final dev.isxander.yacl3.api.OptionDescription EMPTY;
    descriptor: Ldev/isxander/yacl3/api/OptionDescription;
  public abstract net.minecraft.class_2561 text();
    descriptor: ()Lnet/minecraft/class_2561;

  public abstract java.util.concurrent.CompletableFuture<java.util.Optional<dev.isxander.yacl3.gui.image.ImageRenderer>> image();
    descriptor: ()Ljava/util/concurrent/CompletableFuture;

  public static dev.isxander.yacl3.api.OptionDescription$Builder createBuilder();
    descriptor: ()Ldev/isxander/yacl3/api/OptionDescription$Builder;

  public static dev.isxander.yacl3.api.OptionDescription of(net.minecraft.class_2561...);
    descriptor: ([Lnet/minecraft/class_2561;)Ldev/isxander/yacl3/api/OptionDescription;
}
Compiled from "OptionEventListener.java"
public interface dev.isxander.yacl3.api.OptionEventListener<T> {
  public abstract void onEvent(dev.isxander.yacl3.api.Option<T>, dev.isxander.yacl3.api.OptionEventListener$Event);
    descriptor: (Ldev/isxander/yacl3/api/Option;Ldev/isxander/yacl3/api/OptionEventListener$Event;)V
}
Compiled from "OptionFlag.java"
public interface dev.isxander.yacl3.api.OptionFlag extends java.util.function.Consumer<net.minecraft.class_310> {
  public static final dev.isxander.yacl3.api.OptionFlag GAME_RESTART;
    descriptor: Ldev/isxander/yacl3/api/OptionFlag;
  public static final dev.isxander.yacl3.api.OptionFlag RELOAD_CHUNKS;
    descriptor: Ldev/isxander/yacl3/api/OptionFlag;
  public static final dev.isxander.yacl3.api.OptionFlag WORLD_RENDER_UPDATE;
    descriptor: Ldev/isxander/yacl3/api/OptionFlag;
  public static final dev.isxander.yacl3.api.OptionFlag ASSET_RELOAD;
    descriptor: Ldev/isxander/yacl3/api/OptionFlag;
}
Compiled from "OptionGroup.java"
public interface dev.isxander.yacl3.api.OptionGroup {
  public abstract net.minecraft.class_2561 name();
    descriptor: ()Lnet/minecraft/class_2561;

  public abstract dev.isxander.yacl3.api.OptionDescription description();
    descriptor: ()Ldev/isxander/yacl3/api/OptionDescription;

  public abstract net.minecraft.class_2561 tooltip();
    descriptor: ()Lnet/minecraft/class_2561;

  public abstract com.google.common.collect.ImmutableList<? extends dev.isxander.yacl3.api.Option<?>> options();
    descriptor: ()Lcom/google/common/collect/ImmutableList;

  public abstract boolean collapsed();
    descriptor: ()Z

  public abstract boolean isRoot();
    descriptor: ()Z

  public static dev.isxander.yacl3.api.OptionGroup$Builder createBuilder();
    descriptor: ()Ldev/isxander/yacl3/api/OptionGroup$Builder;
}
Compiled from "PlaceholderCategory.java"
public interface dev.isxander.yacl3.api.PlaceholderCategory extends dev.isxander.yacl3.api.ConfigCategory {
  public abstract java.util.function.BiFunction<net.minecraft.class_310, dev.isxander.yacl3.gui.YACLScreen, net.minecraft.class_437> screen();
    descriptor: ()Ljava/util/function/BiFunction;

  public static dev.isxander.yacl3.api.PlaceholderCategory$Builder createBuilder();
    descriptor: ()Ldev/isxander/yacl3/api/PlaceholderCategory$Builder;
}
Compiled from "StateManager.java"
public interface dev.isxander.yacl3.api.StateManager<T> {
  public static <T> dev.isxander.yacl3.api.StateManager<T> createSimple(dev.isxander.yacl3.api.Binding<T>);
    descriptor: (Ldev/isxander/yacl3/api/Binding;)Ldev/isxander/yacl3/api/StateManager;

  public static <T> dev.isxander.yacl3.api.StateManager<T> createSimple(T, java.util.function.Supplier<T>, java.util.function.Consumer<T>);
    descriptor: (Ljava/lang/Object;Ljava/util/function/Supplier;Ljava/util/function/Consumer;)Ldev/isxander/yacl3/api/StateManager;

  public static <T> dev.isxander.yacl3.api.StateManager<T> createInstant(dev.isxander.yacl3.api.Binding<T>);
    descriptor: (Ldev/isxander/yacl3/api/Binding;)Ldev/isxander/yacl3/api/StateManager;

  public static <T> dev.isxander.yacl3.api.StateManager<T> createInstant(T, java.util.function.Supplier<T>, java.util.function.Consumer<T>);
    descriptor: (Ljava/lang/Object;Ljava/util/function/Supplier;Ljava/util/function/Consumer;)Ldev/isxander/yacl3/api/StateManager;

  public static <T> dev.isxander.yacl3.api.StateManager<T> createImmutable(T);
    descriptor: (Ljava/lang/Object;)Ldev/isxander/yacl3/api/StateManager;

  public abstract void set(T);
    descriptor: (Ljava/lang/Object;)V

  public abstract T get();
    descriptor: ()Ljava/lang/Object;

  public abstract void apply();
    descriptor: ()V

  public abstract void resetToDefault(dev.isxander.yacl3.api.StateManager$ResetAction);
    descriptor: (Ldev/isxander/yacl3/api/StateManager$ResetAction;)V

  public abstract void sync();
    descriptor: ()V

  public abstract boolean isSynced();
    descriptor: ()Z

  public default boolean isAlwaysSynced();
    descriptor: ()Z

  public abstract boolean isDefault();
    descriptor: ()Z

  public abstract void addListener(dev.isxander.yacl3.api.StateManager$StateListener<T>);
    descriptor: (Ldev/isxander/yacl3/api/StateManager$StateListener;)V
}
Compiled from "YetAnotherConfigLib.java"
public interface dev.isxander.yacl3.api.YetAnotherConfigLib {
  public abstract net.minecraft.class_2561 title();
    descriptor: ()Lnet/minecraft/class_2561;

  public abstract com.google.common.collect.ImmutableList<dev.isxander.yacl3.api.ConfigCategory> categories();
    descriptor: ()Lcom/google/common/collect/ImmutableList;

  public abstract java.lang.Runnable saveFunction();
    descriptor: ()Ljava/lang/Runnable;

  public abstract java.util.function.Consumer<dev.isxander.yacl3.gui.YACLScreen> initConsumer();
    descriptor: ()Ljava/util/function/Consumer;

  public abstract net.minecraft.class_437 generateScreen(net.minecraft.class_437);
    descriptor: (Lnet/minecraft/class_437;)Lnet/minecraft/class_437;

  public static dev.isxander.yacl3.api.YetAnotherConfigLib$Builder createBuilder();
    descriptor: ()Ldev/isxander/yacl3/api/YetAnotherConfigLib$Builder;

  public static <T> dev.isxander.yacl3.api.YetAnotherConfigLib create(dev.isxander.yacl3.config.v2.api.ConfigClassHandler<T>, dev.isxander.yacl3.api.YetAnotherConfigLib$ConfigBackedBuilder<T>);
    descriptor: (Ldev/isxander/yacl3/config/v2/api/ConfigClassHandler;Ldev/isxander/yacl3/api/YetAnotherConfigLib$ConfigBackedBuilder;)Ldev/isxander/yacl3/api/YetAnotherConfigLib;

  public static <T> dev.isxander.yacl3.api.YetAnotherConfigLib create(dev.isxander.yacl3.config.ConfigInstance<T>, dev.isxander.yacl3.api.YetAnotherConfigLib$ConfigBackedBuilder<T>);
    descriptor: (Ldev/isxander/yacl3/config/ConfigInstance;Ldev/isxander/yacl3/api/YetAnotherConfigLib$ConfigBackedBuilder;)Ldev/isxander/yacl3/api/YetAnotherConfigLib;
}
Compiled from "BooleanControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.BooleanControllerBuilder extends dev.isxander.yacl3.api.controller.ValueFormattableController<java.lang.Boolean, dev.isxander.yacl3.api.controller.BooleanControllerBuilder> {
  public abstract dev.isxander.yacl3.api.controller.BooleanControllerBuilder coloured(boolean);
    descriptor: (Z)Ldev/isxander/yacl3/api/controller/BooleanControllerBuilder;

  public abstract dev.isxander.yacl3.api.controller.BooleanControllerBuilder onOffFormatter();
    descriptor: ()Ldev/isxander/yacl3/api/controller/BooleanControllerBuilder;

  public abstract dev.isxander.yacl3.api.controller.BooleanControllerBuilder yesNoFormatter();
    descriptor: ()Ldev/isxander/yacl3/api/controller/BooleanControllerBuilder;

  public abstract dev.isxander.yacl3.api.controller.BooleanControllerBuilder trueFalseFormatter();
    descriptor: ()Ldev/isxander/yacl3/api/controller/BooleanControllerBuilder;

  public static dev.isxander.yacl3.api.controller.BooleanControllerBuilder create(dev.isxander.yacl3.api.Option<java.lang.Boolean>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/BooleanControllerBuilder;
}
Compiled from "ColorControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.ColorControllerBuilder extends dev.isxander.yacl3.api.controller.ControllerBuilder<java.awt.Color> {
  public abstract dev.isxander.yacl3.api.controller.ColorControllerBuilder allowAlpha(boolean);
    descriptor: (Z)Ldev/isxander/yacl3/api/controller/ColorControllerBuilder;

  public static dev.isxander.yacl3.api.controller.ColorControllerBuilder create(dev.isxander.yacl3.api.Option<java.awt.Color>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/ColorControllerBuilder;
}
Compiled from "ControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.ControllerBuilder<T> {
  public abstract dev.isxander.yacl3.api.Controller<T> build();
    descriptor: ()Ldev/isxander/yacl3/api/Controller;
}
Compiled from "CyclingListControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.CyclingListControllerBuilder<T> extends dev.isxander.yacl3.api.controller.ValueFormattableController<T, dev.isxander.yacl3.api.controller.CyclingListControllerBuilder<T>> {
  public abstract dev.isxander.yacl3.api.controller.CyclingListControllerBuilder<T> values(T...);
    descriptor: ([Ljava/lang/Object;)Ldev/isxander/yacl3/api/controller/CyclingListControllerBuilder;

  public abstract dev.isxander.yacl3.api.controller.CyclingListControllerBuilder<T> values(java.lang.Iterable<? extends T>);
    descriptor: (Ljava/lang/Iterable;)Ldev/isxander/yacl3/api/controller/CyclingListControllerBuilder;

  public static <T> dev.isxander.yacl3.api.controller.CyclingListControllerBuilder<T> create(dev.isxander.yacl3.api.Option<T>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/CyclingListControllerBuilder;
}
Compiled from "DoubleFieldControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder extends dev.isxander.yacl3.api.controller.NumberFieldControllerBuilder<java.lang.Double, dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder> {
  public static dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder create(dev.isxander.yacl3.api.Option<java.lang.Double>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/DoubleFieldControllerBuilder;
}
Compiled from "DoubleSliderControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder extends dev.isxander.yacl3.api.controller.SliderControllerBuilder<java.lang.Double, dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder> {
  public static dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder create(dev.isxander.yacl3.api.Option<java.lang.Double>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/DoubleSliderControllerBuilder;
}
Compiled from "DropdownStringControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder extends dev.isxander.yacl3.api.controller.StringControllerBuilder {
  public abstract dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder values(java.util.List<java.lang.String>);
    descriptor: (Ljava/util/List;)Ldev/isxander/yacl3/api/controller/DropdownStringControllerBuilder;

  public abstract dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder values(java.lang.String...);
    descriptor: ([Ljava/lang/String;)Ldev/isxander/yacl3/api/controller/DropdownStringControllerBuilder;

  public abstract dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder allowEmptyValue(boolean);
    descriptor: (Z)Ldev/isxander/yacl3/api/controller/DropdownStringControllerBuilder;

  public abstract dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder allowAnyValue(boolean);
    descriptor: (Z)Ldev/isxander/yacl3/api/controller/DropdownStringControllerBuilder;

  public static dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder create(dev.isxander.yacl3.api.Option<java.lang.String>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/DropdownStringControllerBuilder;
}
Compiled from "EnumControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.EnumControllerBuilder<T extends java.lang.Enum<T>> extends dev.isxander.yacl3.api.controller.ValueFormattableController<T, dev.isxander.yacl3.api.controller.EnumControllerBuilder<T>> {
  public abstract dev.isxander.yacl3.api.controller.EnumControllerBuilder<T> enumClass(java.lang.Class<T>);
    descriptor: (Ljava/lang/Class;)Ldev/isxander/yacl3/api/controller/EnumControllerBuilder;

  public static <T extends java.lang.Enum<T>> dev.isxander.yacl3.api.controller.EnumControllerBuilder<T> create(dev.isxander.yacl3.api.Option<T>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/EnumControllerBuilder;
}
Compiled from "EnumDropdownControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.EnumDropdownControllerBuilder<E extends java.lang.Enum<E>> extends dev.isxander.yacl3.api.controller.ValueFormattableController<E, dev.isxander.yacl3.api.controller.EnumDropdownControllerBuilder<E>> {
  public static <E extends java.lang.Enum<E>> dev.isxander.yacl3.api.controller.EnumDropdownControllerBuilder<E> create(dev.isxander.yacl3.api.Option<E>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/EnumDropdownControllerBuilder;
}
Compiled from "FloatFieldControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder extends dev.isxander.yacl3.api.controller.NumberFieldControllerBuilder<java.lang.Float, dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder> {
  public static dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder create(dev.isxander.yacl3.api.Option<java.lang.Float>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/FloatFieldControllerBuilder;
}
Compiled from "FloatSliderControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder extends dev.isxander.yacl3.api.controller.SliderControllerBuilder<java.lang.Float, dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder> {
  public static dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder create(dev.isxander.yacl3.api.Option<java.lang.Float>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/FloatSliderControllerBuilder;
}
Compiled from "IntegerFieldControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder extends dev.isxander.yacl3.api.controller.NumberFieldControllerBuilder<java.lang.Integer, dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder> {
  public static dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder create(dev.isxander.yacl3.api.Option<java.lang.Integer>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/IntegerFieldControllerBuilder;
}
Compiled from "IntegerSliderControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder extends dev.isxander.yacl3.api.controller.SliderControllerBuilder<java.lang.Integer, dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder> {
  public static dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder create(dev.isxander.yacl3.api.Option<java.lang.Integer>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/IntegerSliderControllerBuilder;
}
Compiled from "ItemControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.ItemControllerBuilder extends dev.isxander.yacl3.api.controller.ControllerBuilder<net.minecraft.class_1792> {
  public static dev.isxander.yacl3.api.controller.ItemControllerBuilder create(dev.isxander.yacl3.api.Option<net.minecraft.class_1792>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/ItemControllerBuilder;
}
Compiled from "LongFieldControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.LongFieldControllerBuilder extends dev.isxander.yacl3.api.controller.NumberFieldControllerBuilder<java.lang.Long, dev.isxander.yacl3.api.controller.LongFieldControllerBuilder> {
  public static dev.isxander.yacl3.api.controller.LongFieldControllerBuilder create(dev.isxander.yacl3.api.Option<java.lang.Long>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/LongFieldControllerBuilder;
}
Compiled from "LongSliderControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.LongSliderControllerBuilder extends dev.isxander.yacl3.api.controller.SliderControllerBuilder<java.lang.Long, dev.isxander.yacl3.api.controller.LongSliderControllerBuilder> {
  public static dev.isxander.yacl3.api.controller.LongSliderControllerBuilder create(dev.isxander.yacl3.api.Option<java.lang.Long>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/LongSliderControllerBuilder;
}
Compiled from "NumberFieldControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.NumberFieldControllerBuilder<T extends java.lang.Number, B extends dev.isxander.yacl3.api.controller.NumberFieldControllerBuilder<T, B>> extends dev.isxander.yacl3.api.controller.ValueFormattableController<T, B> {
  public abstract B min(T);
    descriptor: (Ljava/lang/Number;)Ldev/isxander/yacl3/api/controller/NumberFieldControllerBuilder;

  public abstract B max(T);
    descriptor: (Ljava/lang/Number;)Ldev/isxander/yacl3/api/controller/NumberFieldControllerBuilder;

  public abstract B range(T, T);
    descriptor: (Ljava/lang/Number;Ljava/lang/Number;)Ldev/isxander/yacl3/api/controller/NumberFieldControllerBuilder;
}
Compiled from "SliderControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.SliderControllerBuilder<T extends java.lang.Number, B extends dev.isxander.yacl3.api.controller.SliderControllerBuilder<T, B>> extends dev.isxander.yacl3.api.controller.ValueFormattableController<T, B> {
  public abstract B range(T, T);
    descriptor: (Ljava/lang/Number;Ljava/lang/Number;)Ldev/isxander/yacl3/api/controller/SliderControllerBuilder;

  public abstract B step(T);
    descriptor: (Ljava/lang/Number;)Ldev/isxander/yacl3/api/controller/SliderControllerBuilder;
}
Compiled from "StringControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.StringControllerBuilder extends dev.isxander.yacl3.api.controller.ControllerBuilder<java.lang.String> {
  public static dev.isxander.yacl3.api.controller.StringControllerBuilder create(dev.isxander.yacl3.api.Option<java.lang.String>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/StringControllerBuilder;
}
Compiled from "TickBoxControllerBuilder.java"
public interface dev.isxander.yacl3.api.controller.TickBoxControllerBuilder extends dev.isxander.yacl3.api.controller.ControllerBuilder<java.lang.Boolean> {
  public static dev.isxander.yacl3.api.controller.TickBoxControllerBuilder create(dev.isxander.yacl3.api.Option<java.lang.Boolean>);
    descriptor: (Ldev/isxander/yacl3/api/Option;)Ldev/isxander/yacl3/api/controller/TickBoxControllerBuilder;
}
Compiled from "ValueFormattableController.java"
public interface dev.isxander.yacl3.api.controller.ValueFormattableController<T, B extends dev.isxander.yacl3.api.controller.ValueFormattableController<T, B>> extends dev.isxander.yacl3.api.controller.ControllerBuilder<T> {
  public abstract B formatValue(dev.isxander.yacl3.api.controller.ValueFormatter<T>);
    descriptor: (Ldev/isxander/yacl3/api/controller/ValueFormatter;)Ldev/isxander/yacl3/api/controller/ValueFormattableController;

  public default B valueFormatter(java.util.function.Function<T, net.minecraft.class_2561>);
    descriptor: (Ljava/util/function/Function;)Ldev/isxander/yacl3/api/controller/ValueFormattableController;
}
Compiled from "ValueFormatter.java"
public interface dev.isxander.yacl3.api.controller.ValueFormatter<T> {
  public abstract net.minecraft.class_2561 format(T);
    descriptor: (Ljava/lang/Object;)Lnet/minecraft/class_2561;
}
Compiled from "Dimension.java"
public interface dev.isxander.yacl3.api.utils.Dimension<T extends java.lang.Number> {
  public abstract T x();
    descriptor: ()Ljava/lang/Number;

  public abstract T y();
    descriptor: ()Ljava/lang/Number;

  public abstract T width();
    descriptor: ()Ljava/lang/Number;

  public abstract T height();
    descriptor: ()Ljava/lang/Number;

  public abstract T xLimit();
    descriptor: ()Ljava/lang/Number;

  public abstract T yLimit();
    descriptor: ()Ljava/lang/Number;

  public abstract T centerX();
    descriptor: ()Ljava/lang/Number;

  public abstract T centerY();
    descriptor: ()Ljava/lang/Number;

  public abstract boolean isPointInside(T, T);
    descriptor: (Ljava/lang/Number;Ljava/lang/Number;)Z

  public abstract dev.isxander.yacl3.api.utils.MutableDimension<T> clone();
    descriptor: ()Ldev/isxander/yacl3/api/utils/MutableDimension;

  public abstract dev.isxander.yacl3.api.utils.Dimension<T> withX(T);
    descriptor: (Ljava/lang/Number;)Ldev/isxander/yacl3/api/utils/Dimension;

  public abstract dev.isxander.yacl3.api.utils.Dimension<T> withY(T);
    descriptor: (Ljava/lang/Number;)Ldev/isxander/yacl3/api/utils/Dimension;

  public abstract dev.isxander.yacl3.api.utils.Dimension<T> withWidth(T);
    descriptor: (Ljava/lang/Number;)Ldev/isxander/yacl3/api/utils/Dimension;

  public abstract dev.isxander.yacl3.api.utils.Dimension<T> withHeight(T);
    descriptor: (Ljava/lang/Number;)Ldev/isxander/yacl3/api/utils/Dimension;

  public abstract dev.isxander.yacl3.api.utils.Dimension<T> moved(T, T);
    descriptor: (Ljava/lang/Number;Ljava/lang/Number;)Ldev/isxander/yacl3/api/utils/Dimension;

  public abstract dev.isxander.yacl3.api.utils.Dimension<T> expanded(T, T);
    descriptor: (Ljava/lang/Number;Ljava/lang/Number;)Ldev/isxander/yacl3/api/utils/Dimension;

  public static dev.isxander.yacl3.api.utils.MutableDimension<java.lang.Integer> ofInt(int, int, int, int);
    descriptor: (IIII)Ldev/isxander/yacl3/api/utils/MutableDimension;
}
Compiled from "MutableDimension.java"
public interface dev.isxander.yacl3.api.utils.MutableDimension<T extends java.lang.Number> extends dev.isxander.yacl3.api.utils.Dimension<T> {
  public abstract dev.isxander.yacl3.api.utils.MutableDimension<T> setX(T);
    descriptor: (Ljava/lang/Number;)Ldev/isxander/yacl3/api/utils/MutableDimension;

  public abstract dev.isxander.yacl3.api.utils.MutableDimension<T> setY(T);
    descriptor: (Ljava/lang/Number;)Ldev/isxander/yacl3/api/utils/MutableDimension;

  public abstract dev.isxander.yacl3.api.utils.MutableDimension<T> setWidth(T);
    descriptor: (Ljava/lang/Number;)Ldev/isxander/yacl3/api/utils/MutableDimension;

  public abstract dev.isxander.yacl3.api.utils.MutableDimension<T> setHeight(T);
    descriptor: (Ljava/lang/Number;)Ldev/isxander/yacl3/api/utils/MutableDimension;

  public abstract dev.isxander.yacl3.api.utils.MutableDimension<T> move(T, T);
    descriptor: (Ljava/lang/Number;Ljava/lang/Number;)Ldev/isxander/yacl3/api/utils/MutableDimension;

  public abstract dev.isxander.yacl3.api.utils.MutableDimension<T> expand(T, T);
    descriptor: (Ljava/lang/Number;Ljava/lang/Number;)Ldev/isxander/yacl3/api/utils/MutableDimension;
}
Compiled from "OptionUtils.java"
public class dev.isxander.yacl3.api.utils.OptionUtils {
  public dev.isxander.yacl3.api.utils.OptionUtils();
    descriptor: ()V

  public static java.util.stream.Stream<dev.isxander.yacl3.api.Option<?>> getFlatOptions(dev.isxander.yacl3.api.YetAnotherConfigLib);
    descriptor: (Ldev/isxander/yacl3/api/YetAnotherConfigLib;)Ljava/util/stream/Stream;

  public static void consumeOptions(dev.isxander.yacl3.api.YetAnotherConfigLib, java.util.function.Function<dev.isxander.yacl3.api.Option<?>, java.lang.Boolean>);
    descriptor: (Ldev/isxander/yacl3/api/YetAnotherConfigLib;Ljava/util/function/Function;)V

  public static void forEachOptions(dev.isxander.yacl3.api.YetAnotherConfigLib, java.util.function.Consumer<dev.isxander.yacl3.api.Option<?>>);
    descriptor: (Ldev/isxander/yacl3/api/YetAnotherConfigLib;Ljava/util/function/Consumer;)V
}
Compiled from "ConfigClassHandler.java"
public interface dev.isxander.yacl3.config.v2.api.ConfigClassHandler<T> {
  public abstract T instance();
    descriptor: ()Ljava/lang/Object;

  public abstract T defaults();
    descriptor: ()Ljava/lang/Object;

  public abstract java.lang.Class<T> configClass();
    descriptor: ()Ljava/lang/Class;

  public abstract dev.isxander.yacl3.config.v2.api.ConfigField<?>[] fields();
    descriptor: ()[Ldev/isxander/yacl3/config/v2/api/ConfigField;

  public abstract net.minecraft.class_2960 id();
    descriptor: ()Lnet/minecraft/class_2960;

  public abstract dev.isxander.yacl3.api.YetAnotherConfigLib generateGui();
    descriptor: ()Ldev/isxander/yacl3/api/YetAnotherConfigLib;

  public abstract boolean supportsAutoGen();
    descriptor: ()Z

  public abstract boolean load();
    descriptor: ()Z

  public abstract void save();
    descriptor: ()V

  public abstract dev.isxander.yacl3.config.v2.api.ConfigSerializer<T> serializer();
    descriptor: ()Ldev/isxander/yacl3/config/v2/api/ConfigSerializer;

  public static <T> dev.isxander.yacl3.config.v2.api.ConfigClassHandler$Builder<T> createBuilder(java.lang.Class<T>);
    descriptor: (Ljava/lang/Class;)Ldev/isxander/yacl3/config/v2/api/ConfigClassHandler$Builder;
}
Compiled from "ConfigField.java"
public interface dev.isxander.yacl3.config.v2.api.ConfigField<T> {
  public abstract dev.isxander.yacl3.config.v2.api.FieldAccess<T> access();
    descriptor: ()Ldev/isxander/yacl3/config/v2/api/FieldAccess;

  public abstract dev.isxander.yacl3.config.v2.api.ReadOnlyFieldAccess<T> defaultAccess();
    descriptor: ()Ldev/isxander/yacl3/config/v2/api/ReadOnlyFieldAccess;

  public abstract dev.isxander.yacl3.config.v2.api.ConfigClassHandler<?> parent();
    descriptor: ()Ldev/isxander/yacl3/config/v2/api/ConfigClassHandler;

  public abstract java.util.Optional<dev.isxander.yacl3.config.v2.api.SerialField> serial();
    descriptor: ()Ljava/util/Optional;

  public abstract java.util.Optional<dev.isxander.yacl3.config.v2.api.autogen.AutoGenField> autoGen();
    descriptor: ()Ljava/util/Optional;
}
Compiled from "ConfigSerializer.java"
public abstract class dev.isxander.yacl3.config.v2.api.ConfigSerializer<T> {
  public dev.isxander.yacl3.config.v2.api.ConfigSerializer(dev.isxander.yacl3.config.v2.api.ConfigClassHandler<T>);
    descriptor: (Ldev/isxander/yacl3/config/v2/api/ConfigClassHandler;)V

  public abstract void save();
    descriptor: ()V

  public dev.isxander.yacl3.config.v2.api.ConfigSerializer$LoadResult loadSafely(java.util.Map<dev.isxander.yacl3.config.v2.api.ConfigField<?>, dev.isxander.yacl3.config.v2.api.FieldAccess<?>>);
    descriptor: (Ljava/util/Map;)Ldev/isxander/yacl3/config/v2/api/ConfigSerializer$LoadResult;

  public void load();
    descriptor: ()V
}
Compiled from "FieldAccess.java"
public interface dev.isxander.yacl3.config.v2.api.FieldAccess<T> extends dev.isxander.yacl3.config.v2.api.ReadOnlyFieldAccess<T> {
  public abstract void set(T);
    descriptor: (Ljava/lang/Object;)V
}
Compiled from "ReadOnlyFieldAccess.java"
public interface dev.isxander.yacl3.config.v2.api.ReadOnlyFieldAccess<T> {
  public abstract T get();
    descriptor: ()Ljava/lang/Object;

  public abstract java.lang.String name();
    descriptor: ()Ljava/lang/String;

  public abstract java.lang.reflect.Type type();
    descriptor: ()Ljava/lang/reflect/Type;

  public abstract java.lang.Class<T> typeClass();
    descriptor: ()Ljava/lang/Class;

  public abstract <A extends java.lang.annotation.Annotation> java.util.Optional<A> getAnnotation(java.lang.Class<A>);
    descriptor: (Ljava/lang/Class;)Ljava/util/Optional;
}
Compiled from "SerialEntry.java"
public interface dev.isxander.yacl3.config.v2.api.SerialEntry extends java.lang.annotation.Annotation {
  public abstract java.lang.String value();
    descriptor: ()Ljava/lang/String;

  public abstract java.lang.String comment();
    descriptor: ()Ljava/lang/String;

  public abstract boolean required();
    descriptor: ()Z

  public abstract boolean nullable();
    descriptor: ()Z
}
Compiled from "SerialField.java"
public interface dev.isxander.yacl3.config.v2.api.SerialField {
  public abstract java.lang.String serialName();
    descriptor: ()Ljava/lang/String;

  public abstract java.util.Optional<java.lang.String> comment();
    descriptor: ()Ljava/util/Optional;

  public abstract boolean required();
    descriptor: ()Z

  public abstract boolean nullable();
    descriptor: ()Z
}
Compiled from "AutoGen.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.AutoGen extends java.lang.annotation.Annotation {
  public abstract java.lang.String category();
    descriptor: ()Ljava/lang/String;

  public abstract java.lang.String group();
    descriptor: ()Ljava/lang/String;
}
Compiled from "AutoGenField.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.AutoGenField {
  public abstract java.lang.String category();
    descriptor: ()Ljava/lang/String;

  public abstract java.util.Optional<java.lang.String> group();
    descriptor: ()Ljava/util/Optional;
}
Compiled from "Boolean.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.Boolean extends java.lang.annotation.Annotation {
  public abstract dev.isxander.yacl3.config.v2.api.autogen.Boolean$Formatter formatter();
    descriptor: ()Ldev/isxander/yacl3/config/v2/api/autogen/Boolean$Formatter;

  public abstract boolean colored();
    descriptor: ()Z
}
Compiled from "ColorField.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.ColorField extends java.lang.annotation.Annotation {
  public abstract boolean allowAlpha();
    descriptor: ()Z
}
Compiled from "CustomDescription.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.CustomDescription extends java.lang.annotation.Annotation {
  public abstract java.lang.String[] value();
    descriptor: ()[Ljava/lang/String;
}
Compiled from "CustomFormat.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.CustomFormat extends java.lang.annotation.Annotation {
  public abstract java.lang.Class<? extends dev.isxander.yacl3.api.controller.ValueFormatter<?>> value();
    descriptor: ()Ljava/lang/Class;
}
Compiled from "CustomImage.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.CustomImage extends java.lang.annotation.Annotation {
  public abstract java.lang.String value();
    descriptor: ()Ljava/lang/String;

  public abstract int width();
    descriptor: ()I

  public abstract int height();
    descriptor: ()I

  public abstract java.lang.Class<? extends dev.isxander.yacl3.config.v2.api.autogen.CustomImage$CustomImageFactory<?>> factory();
    descriptor: ()Ljava/lang/Class;
}
Compiled from "CustomName.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.CustomName extends java.lang.annotation.Annotation {
  public abstract java.lang.String value();
    descriptor: ()Ljava/lang/String;
}
Compiled from "DoubleField.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.DoubleField extends java.lang.annotation.Annotation {
  public abstract double min();
    descriptor: ()D

  public abstract double max();
    descriptor: ()D

  public abstract java.lang.String format();
    descriptor: ()Ljava/lang/String;
}
Compiled from "DoubleSlider.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.DoubleSlider extends java.lang.annotation.Annotation {
  public abstract double min();
    descriptor: ()D

  public abstract double max();
    descriptor: ()D

  public abstract double step();
    descriptor: ()D

  public abstract java.lang.String format();
    descriptor: ()Ljava/lang/String;
}
Compiled from "Dropdown.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.Dropdown extends java.lang.annotation.Annotation {
  public abstract java.lang.String[] values();
    descriptor: ()[Ljava/lang/String;

  public abstract boolean allowEmptyValue();
    descriptor: ()Z

  public abstract boolean allowAnyValue();
    descriptor: ()Z
}
Compiled from "EnumCycler.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.EnumCycler extends java.lang.annotation.Annotation {
  public abstract int[] allowedOrdinals();
    descriptor: ()[I
}
Compiled from "FloatField.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.FloatField extends java.lang.annotation.Annotation {
  public abstract float min();
    descriptor: ()F

  public abstract float max();
    descriptor: ()F

  public abstract java.lang.String format();
    descriptor: ()Ljava/lang/String;
}
Compiled from "FloatSlider.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.FloatSlider extends java.lang.annotation.Annotation {
  public abstract float min();
    descriptor: ()F

  public abstract float max();
    descriptor: ()F

  public abstract float step();
    descriptor: ()F

  public abstract java.lang.String format();
    descriptor: ()Ljava/lang/String;
}
Compiled from "FormatTranslation.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.FormatTranslation extends java.lang.annotation.Annotation {
  public abstract java.lang.String value();
    descriptor: ()Ljava/lang/String;
}
Compiled from "IntField.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.IntField extends java.lang.annotation.Annotation {
  public abstract int min();
    descriptor: ()I

  public abstract int max();
    descriptor: ()I

  public abstract java.lang.String format();
    descriptor: ()Ljava/lang/String;
}
Compiled from "IntSlider.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.IntSlider extends java.lang.annotation.Annotation {
  public abstract int min();
    descriptor: ()I

  public abstract int max();
    descriptor: ()I

  public abstract int step();
    descriptor: ()I
}
Compiled from "ItemField.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.ItemField extends java.lang.annotation.Annotation {
}
Compiled from "Label.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.Label extends java.lang.annotation.Annotation {
}
Compiled from "ListGroup.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.ListGroup extends java.lang.annotation.Annotation {
  public abstract java.lang.Class<? extends dev.isxander.yacl3.config.v2.api.autogen.ListGroup$ValueFactory<?>> valueFactory();
    descriptor: ()Ljava/lang/Class;

  public abstract java.lang.Class<? extends dev.isxander.yacl3.config.v2.api.autogen.ListGroup$ControllerFactory<?>> controllerFactory();
    descriptor: ()Ljava/lang/Class;

  public abstract int maxEntries();
    descriptor: ()I

  public abstract int minEntries();
    descriptor: ()I

  public abstract boolean addEntriesToBottom();
    descriptor: ()Z
}
Compiled from "LongField.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.LongField extends java.lang.annotation.Annotation {
  public abstract long min();
    descriptor: ()J

  public abstract long max();
    descriptor: ()J

  public abstract java.lang.String format();
    descriptor: ()Ljava/lang/String;
}
Compiled from "LongSlider.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.LongSlider extends java.lang.annotation.Annotation {
  public abstract long min();
    descriptor: ()J

  public abstract long max();
    descriptor: ()J

  public abstract long step();
    descriptor: ()J
}
Compiled from "MasterTickBox.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.MasterTickBox extends java.lang.annotation.Annotation {
  public abstract java.lang.String[] value();
    descriptor: ()[Ljava/lang/String;

  public abstract boolean invert();
    descriptor: ()Z
}
Compiled from "OptionAccess.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.OptionAccess {
  public abstract dev.isxander.yacl3.api.Option<?> getOption(java.lang.String);
    descriptor: (Ljava/lang/String;)Ldev/isxander/yacl3/api/Option;

  public abstract void scheduleOptionOperation(java.lang.String, java.util.function.Consumer<dev.isxander.yacl3.api.Option<?>>);
    descriptor: (Ljava/lang/String;Ljava/util/function/Consumer;)V
}
Compiled from "OptionFactory.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.OptionFactory<A extends java.lang.annotation.Annotation, T> {
  public abstract dev.isxander.yacl3.api.Option<T> createOption(A, dev.isxander.yacl3.config.v2.api.ConfigField<T>, dev.isxander.yacl3.config.v2.api.autogen.OptionAccess);
    descriptor: (Ljava/lang/annotation/Annotation;Ldev/isxander/yacl3/config/v2/api/ConfigField;Ldev/isxander/yacl3/config/v2/api/autogen/OptionAccess;)Ldev/isxander/yacl3/api/Option;

  public static <A extends java.lang.annotation.Annotation, T> void register(java.lang.Class<A>, dev.isxander.yacl3.config.v2.api.autogen.OptionFactory<A, T>);
    descriptor: (Ljava/lang/Class;Ldev/isxander/yacl3/config/v2/api/autogen/OptionFactory;)V
}
Compiled from "SimpleOptionFactory.java"
public abstract class dev.isxander.yacl3.config.v2.api.autogen.SimpleOptionFactory<A extends java.lang.annotation.Annotation, T> implements dev.isxander.yacl3.config.v2.api.autogen.OptionFactory<A, T> {
  public dev.isxander.yacl3.config.v2.api.autogen.SimpleOptionFactory();
    descriptor: ()V

  public dev.isxander.yacl3.api.Option<T> createOption(A, dev.isxander.yacl3.config.v2.api.ConfigField<T>, dev.isxander.yacl3.config.v2.api.autogen.OptionAccess);
    descriptor: (Ljava/lang/annotation/Annotation;Ldev/isxander/yacl3/config/v2/api/ConfigField;Ldev/isxander/yacl3/config/v2/api/autogen/OptionAccess;)Ldev/isxander/yacl3/api/Option;
}
Compiled from "StringField.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.StringField extends java.lang.annotation.Annotation {
}
Compiled from "TickBox.java"
public interface dev.isxander.yacl3.config.v2.api.autogen.TickBox extends java.lang.annotation.Annotation {
}
Compiled from "GsonConfigSerializerBuilder.java"
public interface dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder<T> {
  public static <T> dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder<T> create(dev.isxander.yacl3.config.v2.api.ConfigClassHandler<T>);
    descriptor: (Ldev/isxander/yacl3/config/v2/api/ConfigClassHandler;)Ldev/isxander/yacl3/config/v2/api/serializer/GsonConfigSerializerBuilder;

  public abstract dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder<T> setPath(java.nio.file.Path);
    descriptor: (Ljava/nio/file/Path;)Ldev/isxander/yacl3/config/v2/api/serializer/GsonConfigSerializerBuilder;

  public abstract dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder<T> overrideGsonBuilder(com.google.gson.GsonBuilder);
    descriptor: (Lcom/google/gson/GsonBuilder;)Ldev/isxander/yacl3/config/v2/api/serializer/GsonConfigSerializerBuilder;

  public abstract dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder<T> overrideGsonBuilder(com.google.gson.Gson);
    descriptor: (Lcom/google/gson/Gson;)Ldev/isxander/yacl3/config/v2/api/serializer/GsonConfigSerializerBuilder;

  public abstract dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder<T> appendGsonBuilder(java.util.function.UnaryOperator<com.google.gson.GsonBuilder>);
    descriptor: (Ljava/util/function/UnaryOperator;)Ldev/isxander/yacl3/config/v2/api/serializer/GsonConfigSerializerBuilder;

  public abstract dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder<T> setJson5(boolean);
    descriptor: (Z)Ldev/isxander/yacl3/config/v2/api/serializer/GsonConfigSerializerBuilder;

  public abstract dev.isxander.yacl3.config.v2.api.ConfigSerializer<T> build();
    descriptor: ()Ldev/isxander/yacl3/config/v2/api/ConfigSerializer;
}

```
