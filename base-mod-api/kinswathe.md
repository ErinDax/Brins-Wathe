# kinswathe 公开 API 参考

> 来自 `maven.modrinth:kinswathe:tr5nQegN`。
> **某个类有哪些方法、方法的 JVM 描述符**:用下面的 `lookup`。
> **某个方法/包实际做了什么**:`./base-mod-api/decompile <类或包全限定名>` 按需反编译到 `./base-mod-src/` 再读。签名看不出行为,尤其 mixin —— 不要从名字推断。

## 查任意类的签名与 JVM 描述符

```sh
./base-mod-api/lookup <全限定类名>
```

输出里每个方法下的 `descriptor:` 就是它的 **JVM 方法描述符**。写 mixin 的 `@At(target = "L包/路径/类;方法名描述符")` 时**原样复制那一行**,**绝不要**自己把 `(int, int, double)` 心算成 `(IID)` —— 参数顺序数错一位,mixin 注入就会失败,而**编译器不报错、datagen 也不报错**(`@At` 的 target 只是字符串;客户端类在 `runData` 里根本不加载),玩家一进游戏就崩。

## ⚠️ 数据契约(前置在 `data/` 里规定的东西 —— 决定你的东西能否被它识别)

本前置的 jar 里带着这些**数据文件**。它们是游戏真正读的那份数据:

| 命名空间 / 类型 | 文件数 | 样例 |
|---|---|---|
| `wathe/tags` | 1 | `data/wathe/tags/item/psychosis_items.json` |

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

