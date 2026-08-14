# harpymodloader 公开 API 参考

> 来自 `maven.modrinth:harpymodloader:nMW4YJds`。
> **某个类有哪些方法、方法的 JVM 描述符**:用下面的 `lookup`。
> **某个方法/包实际做了什么**:`./base-mod-api/decompile <类或包全限定名>` 按需反编译到 `./base-mod-src/` 再读。签名看不出行为,尤其 mixin —— 不要从名字推断。

## 查任意类的签名与 JVM 描述符

```sh
./base-mod-api/lookup <全限定类名>
```

输出里每个方法下的 `descriptor:` 就是它的 **JVM 方法描述符**。写 mixin 的 `@At(target = "L包/路径/类;方法名描述符")` 时**原样复制那一行**,**绝不要**自己把 `(int, int, double)` 心算成 `(IID)` —— 参数顺序数错一位,mixin 注入就会失败,而**编译器不报错、datagen 也不报错**(`@At` 的 target 只是字符串;客户端类在 `runData` 里根本不加载),玩家一进游戏就崩。

