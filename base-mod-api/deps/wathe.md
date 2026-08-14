# wathe(自动附带的依赖)

> `wathe` 是「HarpyModLoader」自身需要的运行前置,构建环境自动带上,**一般无需直接调用**。来自 `maven.modrinth:wathe:3Kw6IUoN`。

## 查任意类的签名与 JVM 描述符

```sh
./base-mod-api/lookup <全限定类名>
```

输出里每个方法下的 `descriptor:` 就是它的 **JVM 方法描述符**。写 mixin 的 `@At(target = "L包/路径/类;方法名描述符")` 时**原样复制那一行**,**绝不要**自己把 `(int, int, double)` 心算成 `(IID)` —— 参数顺序数错一位,mixin 注入就会失败,而**编译器不报错、datagen 也不报错**(`@At` 的 target 只是字符串;客户端类在 `runData` 里根本不加载),玩家一进游戏就崩。

## ⚠️ 数据契约(前置在 `data/` 里规定的东西 —— 决定你的东西能否被它识别)

本前置的 jar 里带着这些**数据文件**。它们是游戏真正读的那份数据:

| 命名空间 / 类型 | 文件数 | 样例 |
|---|---|---|
| `wathe/loot_table` | 158 | `data/wathe/loot_table/blocks/acacia_branch.json` |
| `minecraft/tags` | 19 | `data/minecraft/tags/block/beds.json` |
| `wathe/tags` | 7 | `data/wathe/tags/block/branches.json` |

**这些不是给你参考的,是给你遵守的。** 如果你做的物品/方块要被前置的系统识别
(放进它的槽位、进它的配方、被它的机制处理),它极可能在这里规定了**硬性要求**
——比如「你的物品必须挂在某个 item tag 上」「必须注册成某个类型」。

**这类要求在 API 签名里完全看不出来**:你实现了它的接口、编译通过、datagen 通过、游戏也不报错,但东西就是不工作 —— 因为前置压根没认出你。

```sh
./base-mod-api/data                     # 看全部命名空间与类型
./base-mod-api/data <上表的路径>         # 列出该类下的文件
./base-mod-api/data <完整文件路径>       # 打印文件内容
```

看到不认识的字段(如 `validator_predicates`)就 `./base-mod-api/decompile` 去查
前置怎么用它的 —— **别猜**。

## `api` 包的公开签名(作者明确留给外部调用的接口)

这里只预导了 `api` 包;其它类用上面的 `lookup` 查。

```
<!-- javap 范围: api 包;共 12 个类 -->
Compiled from "GameMode.java"
public abstract class dev.doctor4t.wathe.api.GameMode {
  public final net.minecraft.class_2960 identifier;
    descriptor: Lnet/minecraft/class_2960;
  public final int defaultStartTime;
    descriptor: I
  public final int minPlayerCount;
    descriptor: I
  public dev.doctor4t.wathe.api.GameMode(net.minecraft.class_2960, int, int);
    descriptor: (Lnet/minecraft/class_2960;II)V

  public void tickCommonGameLoop();
    descriptor: ()V

  public void tickClientGameLoop();
    descriptor: ()V

  public abstract void tickServerGameLoop(net.minecraft.class_3218, dev.doctor4t.wathe.cca.GameWorldComponent);
    descriptor: (Lnet/minecraft/class_3218;Ldev/doctor4t/wathe/cca/GameWorldComponent;)V

  public abstract void initializeGame(net.minecraft.class_3218, dev.doctor4t.wathe.cca.GameWorldComponent, java.util.List<net.minecraft.class_3222>);
    descriptor: (Lnet/minecraft/class_3218;Ldev/doctor4t/wathe/cca/GameWorldComponent;Ljava/util/List;)V

  public void finalizeGame(net.minecraft.class_3218, dev.doctor4t.wathe.cca.GameWorldComponent);
    descriptor: (Lnet/minecraft/class_3218;Ldev/doctor4t/wathe/cca/GameWorldComponent;)V
}
Compiled from "MapEffect.java"
public abstract class dev.doctor4t.wathe.api.MapEffect {
  public final net.minecraft.class_2960 identifier;
    descriptor: Lnet/minecraft/class_2960;
  public dev.doctor4t.wathe.api.MapEffect(net.minecraft.class_2960);
    descriptor: (Lnet/minecraft/class_2960;)V

  public abstract void initializeMapEffects(net.minecraft.class_3218, java.util.List<net.minecraft.class_3222>);
    descriptor: (Lnet/minecraft/class_3218;Ljava/util/List;)V

  public abstract void finalizeMapEffects(net.minecraft.class_3218, java.util.List<net.minecraft.class_3222>);
    descriptor: (Lnet/minecraft/class_3218;Ljava/util/List;)V
}
Compiled from "Role.java"
public final class dev.doctor4t.wathe.api.Role {
  public dev.doctor4t.wathe.api.Role(net.minecraft.class_2960, int, boolean, boolean, dev.doctor4t.wathe.api.Role$MoodType, int, boolean);
    descriptor: (Lnet/minecraft/class_2960;IZZLdev/doctor4t/wathe/api/Role$MoodType;IZ)V

  public net.minecraft.class_2960 identifier();
    descriptor: ()Lnet/minecraft/class_2960;

  public int color();
    descriptor: ()I

  public boolean isInnocent();
    descriptor: ()Z

  public boolean canUseKiller();
    descriptor: ()Z

  public dev.doctor4t.wathe.api.Role$MoodType getMoodType();
    descriptor: ()Ldev/doctor4t/wathe/api/Role$MoodType;

  public int getMaxSprintTime();
    descriptor: ()I

  public boolean canSeeTime();
    descriptor: ()Z
}
Compiled from "WatheGameModes.java"
public class dev.doctor4t.wathe.api.WatheGameModes {
  public static final java.util.HashMap<net.minecraft.class_2960, dev.doctor4t.wathe.api.GameMode> GAME_MODES;
    descriptor: Ljava/util/HashMap;
  public static final net.minecraft.class_2960 MURDER_ID;
    descriptor: Lnet/minecraft/class_2960;
  public static final net.minecraft.class_2960 DISCOVERY_ID;
    descriptor: Lnet/minecraft/class_2960;
  public static final net.minecraft.class_2960 LOOSE_ENDS_ID;
    descriptor: Lnet/minecraft/class_2960;
  public static final dev.doctor4t.wathe.api.GameMode MURDER;
    descriptor: Ldev/doctor4t/wathe/api/GameMode;
  public static final dev.doctor4t.wathe.api.GameMode DISCOVERY;
    descriptor: Ldev/doctor4t/wathe/api/GameMode;
  public static final dev.doctor4t.wathe.api.GameMode LOOSE_ENDS;
    descriptor: Ldev/doctor4t/wathe/api/GameMode;
  public dev.doctor4t.wathe.api.WatheGameModes();
    descriptor: ()V

  public static dev.doctor4t.wathe.api.GameMode registerGameMode(net.minecraft.class_2960, dev.doctor4t.wathe.api.GameMode);
    descriptor: (Lnet/minecraft/class_2960;Ldev/doctor4t/wathe/api/GameMode;)Ldev/doctor4t/wathe/api/GameMode;
}
Compiled from "WatheMapEffects.java"
public class dev.doctor4t.wathe.api.WatheMapEffects {
  public static final java.util.HashMap<net.minecraft.class_2960, dev.doctor4t.wathe.api.MapEffect> MAP_EFFECTS;
    descriptor: Ljava/util/HashMap;
  public static final net.minecraft.class_2960 HARPY_EXPRESS_LOBBY_ID;
    descriptor: Lnet/minecraft/class_2960;
  public static final net.minecraft.class_2960 HARPY_EXPRESS_NIGHT_ID;
    descriptor: Lnet/minecraft/class_2960;
  public static final net.minecraft.class_2960 HARPY_EXPRESS_DAY_ID;
    descriptor: Lnet/minecraft/class_2960;
  public static final net.minecraft.class_2960 HARPY_EXPRESS_SUNDOWN_ID;
    descriptor: Lnet/minecraft/class_2960;
  public static final net.minecraft.class_2960 GENERIC_ID;
    descriptor: Lnet/minecraft/class_2960;
  public static final dev.doctor4t.wathe.api.MapEffect HARPY_EXPRESS_LOBBY;
    descriptor: Ldev/doctor4t/wathe/api/MapEffect;
  public static final dev.doctor4t.wathe.api.MapEffect HARPY_EXPRESS_NIGHT;
    descriptor: Ldev/doctor4t/wathe/api/MapEffect;
  public static final dev.doctor4t.wathe.api.MapEffect HARPY_EXPRESS_DAY;
    descriptor: Ldev/doctor4t/wathe/api/MapEffect;
  public static final dev.doctor4t.wathe.api.MapEffect HARPY_EXPRESS_SUNDOWN;
    descriptor: Ldev/doctor4t/wathe/api/MapEffect;
  public static final dev.doctor4t.wathe.api.MapEffect GENERIC;
    descriptor: Ldev/doctor4t/wathe/api/MapEffect;
  public dev.doctor4t.wathe.api.WatheMapEffects();
    descriptor: ()V

  public static dev.doctor4t.wathe.api.MapEffect registerMapEffect(net.minecraft.class_2960, dev.doctor4t.wathe.api.MapEffect);
    descriptor: (Lnet/minecraft/class_2960;Ldev/doctor4t/wathe/api/MapEffect;)Ldev/doctor4t/wathe/api/MapEffect;
}
Compiled from "WatheRoles.java"
public class dev.doctor4t.wathe.api.WatheRoles {
  public static final java.util.ArrayList<dev.doctor4t.wathe.api.Role> ROLES;
    descriptor: Ljava/util/ArrayList;
  public static final dev.doctor4t.wathe.api.Role DISCOVERY_CIVILIAN;
    descriptor: Ldev/doctor4t/wathe/api/Role;
  public static final dev.doctor4t.wathe.api.Role CIVILIAN;
    descriptor: Ldev/doctor4t/wathe/api/Role;
  public static final dev.doctor4t.wathe.api.Role VIGILANTE;
    descriptor: Ldev/doctor4t/wathe/api/Role;
  public static final dev.doctor4t.wathe.api.Role KILLER;
    descriptor: Ldev/doctor4t/wathe/api/Role;
  public static final dev.doctor4t.wathe.api.Role LOOSE_END;
    descriptor: Ldev/doctor4t/wathe/api/Role;
  public dev.doctor4t.wathe.api.WatheRoles();
    descriptor: ()V

  public static dev.doctor4t.wathe.api.Role registerRole(dev.doctor4t.wathe.api.Role);
    descriptor: (Ldev/doctor4t/wathe/api/Role;)Ldev/doctor4t/wathe/api/Role;
}
Compiled from "AllowPlayerDeath.java"
public interface dev.doctor4t.wathe.api.event.AllowPlayerDeath {
  public static final net.fabricmc.fabric.api.event.Event<dev.doctor4t.wathe.api.event.AllowPlayerDeath> EVENT;
    descriptor: Lnet/fabricmc/fabric/api/event/Event;
  public abstract boolean allowDeath(net.minecraft.class_1657, net.minecraft.class_1657, net.minecraft.class_2960);
    descriptor: (Lnet/minecraft/class_1657;Lnet/minecraft/class_1657;Lnet/minecraft/class_2960;)Z
}
Compiled from "AllowPlayerOpenLockedDoor.java"
public interface dev.doctor4t.wathe.api.event.AllowPlayerOpenLockedDoor {
  public static final net.fabricmc.fabric.api.event.Event<dev.doctor4t.wathe.api.event.AllowPlayerOpenLockedDoor> EVENT;
    descriptor: Lnet/fabricmc/fabric/api/event/Event;
  public abstract boolean allowOpen(net.minecraft.class_1657);
    descriptor: (Lnet/minecraft/class_1657;)Z
}
Compiled from "AllowPlayerPunching.java"
public interface dev.doctor4t.wathe.api.event.AllowPlayerPunching {
  public static final net.fabricmc.fabric.api.event.Event<dev.doctor4t.wathe.api.event.AllowPlayerPunching> EVENT;
    descriptor: Lnet/fabricmc/fabric/api/event/Event;
  public abstract boolean allowPunching(net.minecraft.class_1657, net.minecraft.class_1657);
    descriptor: (Lnet/minecraft/class_1657;Lnet/minecraft/class_1657;)Z
}
Compiled from "CanSeePoison.java"
public interface dev.doctor4t.wathe.api.event.CanSeePoison {
  public static final net.fabricmc.fabric.api.event.Event<dev.doctor4t.wathe.api.event.CanSeePoison> EVENT;
    descriptor: Lnet/fabricmc/fabric/api/event/Event;
  public abstract boolean visible(net.minecraft.class_1657);
    descriptor: (Lnet/minecraft/class_1657;)Z
}
Compiled from "GameEvents.java"
public final class dev.doctor4t.wathe.api.event.GameEvents {
  public static final net.fabricmc.fabric.api.event.Event<dev.doctor4t.wathe.api.event.GameEvents$OnGameStart> ON_GAME_START;
    descriptor: Lnet/fabricmc/fabric/api/event/Event;
  public static final net.fabricmc.fabric.api.event.Event<dev.doctor4t.wathe.api.event.GameEvents$OnGameStop> ON_GAME_STOP;
    descriptor: Lnet/fabricmc/fabric/api/event/Event;
  public static final net.fabricmc.fabric.api.event.Event<dev.doctor4t.wathe.api.event.GameEvents$OnFinishInitialize> ON_FINISH_INITIALIZE;
    descriptor: Lnet/fabricmc/fabric/api/event/Event;
  public static final net.fabricmc.fabric.api.event.Event<dev.doctor4t.wathe.api.event.GameEvents$OnFinishFinalize> ON_FINISH_FINALIZE;
    descriptor: Lnet/fabricmc/fabric/api/event/Event;
}
Compiled from "ShouldDropOnDeath.java"
public interface dev.doctor4t.wathe.api.event.ShouldDropOnDeath {
  public static final net.fabricmc.fabric.api.event.Event<dev.doctor4t.wathe.api.event.ShouldDropOnDeath> EVENT;
    descriptor: Lnet/fabricmc/fabric/api/event/Event;
  public abstract boolean shouldDrop(net.minecraft.class_1799, net.minecraft.class_1657);
    descriptor: (Lnet/minecraft/class_1799;Lnet/minecraft/class_1657;)Z
}

```
