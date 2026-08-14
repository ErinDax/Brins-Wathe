# owo(自动附带的依赖)

> `owo` 是「Starry Express」自身需要的运行前置,构建环境自动带上,**一般无需直接调用**。来自 `maven.modrinth:owo-lib:m3XmDd8j`。

## 查任意类的签名与 JVM 描述符

```sh
./base-mod-api/lookup <全限定类名>
```

输出里每个方法下的 `descriptor:` 就是它的 **JVM 方法描述符**。写 mixin 的 `@At(target = "L包/路径/类;方法名描述符")` 时**原样复制那一行**,**绝不要**自己把 `(int, int, double)` 心算成 `(IID)` —— 参数顺序数错一位,mixin 注入就会失败,而**编译器不报错、datagen 也不报错**(`@At` 的 target 只是字符串;客户端类在 `runData` 里根本不加载),玩家一进游戏就崩。

## 🚨 前置的数据加载器 —— **它们决定了你必须创建哪些文件**

已把 `owo` 的资源加载器反编译到本地,**动手前必读**:

- `./base-mod-api/owo-loaders/io/wispforest/owo/client/OwoClient.java`
- `./base-mod-api/owo-loaders/io/wispforest/owo/ui/parsing/UIModelLoader.java`

**这些类的代码里写着前置要读哪些 `data/` 目录**(通常在构造函数或 `prepare()` 里,形如 `super(GSON, "xxx/yyy")`、`findResources("zzz", …)`)。

读完它们,再对照 `./base-mod-api/data`(前置自己**带了**哪些数据文件),问一个问题:

> **加载器要读、但前置自己没有的那些目录 —— 那就是它在等我创建的。**

🚨 **这是最容易漏、漏了最致命的一类。** 不创建的下场(实测两次):你的物品打了 tag、继承了前置的类、**编译通过、datagen 通过、runServer 通过、游戏也不报错** —— 但前置的系统**根本认不出你**。典型:玩家身上压根没有那个槽位,饰品永远放不进去。

**这类目录在前置的 jar 里是不存在的**,所以 `./base-mod-api/data` 扫不出来 —— 它们是「等你去建」的。而且**每个前置不一样**:有的默认就给你开好了(那就别多此一举),有的必须你自己请求 —— **看代码,别猜**。

