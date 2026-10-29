# Brin's Wathe

*The English content of this mod is translated by AI.*
*本模组的英文内容由 AI 翻译。*

## 🎤 Required Version & Mod Loader / 所需版本与模组加载器

- Minecraft 1.21.1
- Fabric 0.19.3+
- The following prerequisite mods are required to run this mod: / 运行本模组需要以下前置模组：
  - Wathe
  - HarpyModLoader
  - KinsWathe
  - Starry Express

## ✨ Optimization & Additional Systems in this Mod / 本模组的优化与附加系统

- **Proximity Public Chat / 近距离公共聊天**: The public chat range is 12 blocks. Players beyond 12 blocks cannot see your public messages. / 公共聊天范围为 12 格，超过 12 格的玩家无法看到你的公共消息。
- **Draft Role Selection / 轮抽选角**: At the start of the game, players take turns selecting their roles in order, with a countdown display. When it's your turn, the interface pops up automatically; if you time out, a role is randomly assigned. / 开局按顺序轮流选择身份，带倒计时显示；轮到你时自动弹出界面，超时自动随机。
- **Match Review / 对局复盘**: At the end of the round, kills, deaths, and shield breaks are displayed, along with detailed causes of death (knife, gun, baseball bat, explosion, poison, run over by train, voodoo, ignition, lover's suicide, ignition failure, exam failure, virus infection, guilt suicide, suffocation, low mood, allergy, etc.). / 回合结束展示击杀、死亡、破盾记录，并标明详细死因（刀杀、枪杀、球棒、爆炸、毒杀、被列车碾死、巫毒、点燃、殉情、点火失败、考核失败、病毒感染、愧疚自杀、窒息、心情低落、过敏等）。
- **Stamina & Sprint System / 体力与奔跑系统**: A new stamina value is added. Sprinting consumes stamina, and when stamina is depleted, you cannot sprint. Stamina limit, sprint multiplier, and regeneration rate can be adjusted via commands. / 新增体力值，奔跑消耗体力，体力耗尽无法冲刺；可通过指令调整体力上限、奔跑倍率与回复速度。
- **Music Box / 音乐盒**: A music box is built into the personal settings, supporting uploading and playing custom music. / 个人设置内置音乐盒，支持上传并播放自定义音乐。
- **Rock-Paper-Scissors / 猜拳**: A built-in Rock-Paper-Scissors mini-game. / 内置石头剪刀布小游戏。
- **Admin Settings Panel / 管理设置面板**: Visually toggle instincts, match rules, role selection methods, the music box, and other features without manually editing configs. / 可视化开关本能、对局规则、选角方式、音乐盒等功能，无需手动改配置。
- **AFK Handling / 挂机处理**: An in-game AFK execution feature can be enabled. / 可开启游戏内挂机处死功能。

## ✨ New Roles / 新增角色 (22 in total / 共 22 名)

### Innocent Faction / 平民阵营 (8)

**Stunt Double / 替身演员**
- Can select a player as a stand-in target in the inventory interface. / 可在背包界面选择一名玩家作为替身目标。
- While standing in, if that player takes fatal damage, you die in their place. / 替身期间，该玩家受到致命伤时，由你代替他死亡。

**Medium / 灵媒**
- Spend money to channel the dead for a short time. / 花费金钱可短暂通灵。
- During the channel, you can talk to the dead and gather information. / 通灵期间可以与死者对话、获取信息。

**Operator / 接线员**
- Can monitor all chat messages, ignoring distance limits. / 可监听所有聊天消息，无视距离限制。
- Spend gold to select a non-Medium player and pull both of you into a temporary voice channel. / 花费金币选择一名非灵媒玩家，将你们拉入临时语音频道。
- The channel lasts for a period, during which you cannot leave voluntarily. / 频道持续一段时间，期间无法主动退出。
- Has a skill cooldown. / 有技能冷却时间。

**Survival Expert / 生存专家**
- Has permanent Night Vision. / 拥有永久夜视。
- Can see players trapped by bear traps or landmines. / 可以看到被捕兽夹、地雷困住的玩家。
- Can see bear traps (and can therefore also be caught by them); cannot see landmines, but landmines cannot trap you. / 能看到捕兽夹（因此也会被捕兽夹夹住）；看不到地雷，但地雷无法困住你。
- Spend money and aim at a trapped player, then press the skill key to rescue them; if you are trapped, press the skill key without aiming at others to free yourself. / 花费金钱对准被困玩家按下技能键即可解救；自己被困时，不对准他人按下技能键即解救自己。

**Archivist / 档案员**
- Aim at a corpse, press the skill key, and spend money to seal it. / 对准尸体按下技能键并花费金钱，可进行封存。
- Sealed corpses cannot be dissolved by the Cleaner or revived by the Necromancer. / 封存后的尸体无法被清道夫溶解、无法被死灵法师复活。
- Can see through fake corpses created by the Corpse Maker. / 能识破造尸怪制作的假尸体。
- However, cannot distinguish the disguised corpses of the Undertaker. / 但分辨不出入殓师的伪装尸体。

**Pervert / 变态**
- Aim at a player and press the skill key to track them. / 对准一名玩家按下技能键即可追踪该玩家。
- Can always see the tracked player's icon until they die. / 可以始终看到被追踪玩家的图标，直到其死亡。
- Can switch tracking targets after the cooldown ends. / 冷却结束后可更换追踪目标。

**Avenger / 复仇者**
- Upon witnessing another's death, gains a brief instinct. / 目睹他人死亡后，可获得短暂的本能。

**Cowboy / 牛仔**
- After saving enough money, can select a player to initiate a "Western Duel" (open inventory to select player avatar). / 攒够金钱后可选择一名玩家发动「西部对决」（打开背包选择玩家头像）。
- During the duel, only the Cowboy's Revolver can be used; active and passive skills are disabled. / 对决期间只能使用牛仔左轮，无法使用主动与被动技能。
- The duel ends when only one player remains alive, and all players return to the game area. / 场地仅剩一人存活时结束，所有玩家返回游戏区域。
- If neither side scores a kill within the time limit, both players die. / 限时内双方均未完成击杀，则两人都死亡。
- Duel drops are teleported back to the position held before entering the duel; shooting penalties do not apply during the duel. / 对决掉落物会传送回进入对决前的位置；对决期间不受开枪惩罚。
- ⚠️ A duel arena must be prepared (Y level 118 or above), and spawn points for both players and spectators must be set in the config file, with default orientations facing East/West. / ⚠️ 需要准备对决场地（Y 轴 118 格以上），并在配置文件设置双方与旁观者出生点，默认朝向为东/西。

### Neutral Faction / 中立阵营 (6)

**Nightmare / 梦魇**
- Starts with one shield layer and can see players who are sleeping. / 初始拥有一层护盾，可以看到正在睡觉的玩家。
- Can "sow" sleeping players; each sown player grants an additional shield layer (max two). / 可对睡觉的玩家「播种」，每播种一人多一层护盾（上限两层）。
- Before the Nightmare Hour begins, sown players cannot kill the Nightmare. / 梦魇时刻开始前，被播种的玩家无法杀死梦魇。
- Can assign sleep tasks to living players. / 可以给存活玩家发放睡觉任务。
- If the Killer faction is fully dead and the Nightmare is still alive, the "Nightmare Hour" is triggered, granting a knife, killer instinct, and access to the killer shop. / 杀手阵营全员死亡后若梦魇仍存活，将触发「梦魇时刻」，获得刀、杀手本能与杀手商店。
- Settles together with the killers before Nightmare Hour, and independently after. / 梦魇时刻前随杀手一起结算，之后独立结算。
- Win condition: Sow all living players, or kill everyone. / 胜利条件：播种所有存活玩家，或杀死所有人。

**Berserker / 狂战士**
- The Innocent faction cannot kill you (except during Cowboy time). / 好人阵营无法杀死你（牛仔时间除外）。
- After saving enough gold, can purchase "Infinite Madness" mode, during which the only way to die is by falling out of the game area. / 攒够金币可购买「无限疯魔」模式，期间仅能通过掉出游戏区域死亡。
- Automatically activates Infinite Madness when no other killers or independently-settling neutral players are alive. / 场上没有其他杀手或独立结算的中立玩家存活时，将自动开启无限疯魔。
- Win by killing everyone. / 击杀所有人获胜。

**Substitute / 代偿者**
- When an Innocent tries to kill you, you take their place and survive as a new Innocent. / 平民试图击杀你时，你代替他成为新的平民存活。
- When a Killer tries to kill you, you take their place and survive as a new Killer. / 杀手试图击杀你时，你代替他成为新的杀手存活。
- When someone of unknown faction tries to kill you, you die together with them. / 阵营不明者试图击杀你时，与对方同归于尽。

**Gambler / 赌徒**
- Spend money, aim your crosshair at a player, and place a bet. / 花费金钱，准心对准一位玩家即可下注。
- Can change the bet target while the current target is alive. / 下注对象存活期间可以更换目标。
- The bet target is permanently highlighted to you. / 下注对象对你永久高亮。
- If the bet target dies, you are poisoned (this poison cannot be cured). / 下注对象死亡则你中毒（该毒无法解除）。
- If the bet target survives for 6 minutes, you win regardless of whether you are alive. / 下注对象存活 6 分钟，无论你是否存活，你都会获胜。

**Atoner / 赎罪者**
- Must personally kill players of designated roles; reaching the required number allows independent victory. / 需亲手击杀指定身份的玩家，达到人数即可独立获胜。
- Killing the correct role grants a shield layer; killing the wrong role causes poisoning. / 击杀正确身份获得一层护盾，击杀错误身份则中毒。
- Can purchase role hints and antidotes from the shop (antidotes are used immediately upon purchase). / 可在商店购买身份提示与解毒试剂（解毒试剂即买即用）。
- If the Killers win and your condition is not met, you settle alongside the Killers. / 若杀手胜利且你的条件未达成，则跟随杀手结算。

**Terrorist / 恐怖分子**
- When killed by gunfire, triggers a 5×5×5 explosion. / 被枪击致死时会触发 5×5×5 爆炸。
- If the explosion kills 4 or more players, the Terrorist survives. / 若爆炸造成 4 名及以上玩家死亡，恐怖分子存活。

### Killer Faction / 杀手阵营 (8)

**Puppeteer / 傀儡师**
- Aim at a corpse and press the skill key to turn it into a puppet. / 对准尸体按下技能键，可将尸体制成傀儡。
- Opening the inventory displays created puppets; click an icon to start controlling it, during which your real body cannot move. / 打开背包会显示已制成的傀儡，点击图标即可开始操控，期间本体无法移动。
- The puppet comes with a knife and a crowbar, lasting 60 seconds, and is exposed at the 45-second mark. / 傀儡自带一把刀与一根撬棍，持续 60 秒，第 45 秒会露出破绽。
- If the puppet is destroyed without being self-recalled, your real body is highlighted for 10 seconds; if the puppet is killed, you additionally receive Slowness for 15 seconds. / 傀儡非自我回收地报废时，本体获得 10 秒高亮；傀儡被击杀还会额外获得 15 秒缓慢。
- Used puppets disappear directly without leaving a corpse. / 已使用的傀儡会直接消失，不留尸体。
- After purchasing "Self-Destruct," a controlled puppet's death produces a 4×4×4 explosion centered on your real body, ignoring shields. / 购买「自爆」后，操控的傀儡死亡会产生以本体为中心的 4×4×4 爆炸，无视护盾。
- If your real body dies, the puppet vanishes immediately. / 本体死亡则傀儡立刻消失。

**Trapper / 捕兽者**
- Press the skill key and spend gold to deploy a bear trap. / 按下技能键花费金币部署捕兽夹。
- Bear traps are visible only to yourself, teammates, and the Survival Expert. / 捕兽夹仅自己、同伴与生存专家可见。
- Disappears if no one steps on it within 3.5 minutes after placement. / 放下后三分半无人踩中即消失。
- Trapped players are immobilized until death. / 被夹住的玩家持续定身直至死亡。
- Earn gold each time a player is trapped. / 每夹住一名玩家可获得金币。
- Can purchase extra bear traps from the shop; purchased traps ignore quantity limits and are placed directly at your feet. / 商店可购买额外捕兽夹，购买后无视数量限制，直接在脚下放置。

**Illusionist / 幻术师**
- Press the skill key to spawn two clones, fixed facing East/West. / 按下技能键生成两个分身，分身固定东/西朝向。
- Press the key again while the skill is active to switch perspectives: East clone → West clone → real body. / 技能持续期间再次按键可切换视角：东分身 → 西分身 → 本体。
- Your real body remains in place while controlling clones; if the real body dies, all clones vanish instantly. / 控制分身时本体留在原地；本体死亡则所有分身瞬间消失。
- Clones can interact and can kill. / 分身可以交互，也可以杀人。

**Sniper / 狙击手**
- Cannot use instinct while the skill is active, and is highlighted. / 技能启用期间无法使用本能，且自身高亮。
- Press the skill key to enter aiming mode (ESC to exit); cannot move while aiming. / 按下技能键进入瞄准模式（ESC 退出），瞄准期间无法移动。
- Press the key again to fire a bullet that pierces walls, with a max range of 300 blocks. / 再次按键射出一发穿透墙壁的子弹，最远射程 300 格。
- After use, you are highlighted for a period; completing tasks restores the skill. / 使用后自身高亮一段时间，做任务可恢复技能。

**Bone Collector / 拾骨师**
- Passive skill: Corpses of players you kill do not reveal themselves for 20 seconds. / 被动技能：你击杀的玩家在 20 秒内尸体不会显形。
- Can purchase a Boning Knife from the shop; use it on a corpse with bones to remove the bones and grant yourself a shield layer. / 可在商店购买剔骨刀，对有骨头的尸体使用可剔除骨头，为自己套上一层盾。
- There is theoretically no cap on the number of shields. / 盾的数量理论上无上限。

**Zhang Shi / 张师** (Sponsor Role · Thanks to Seal for the support / 赞助角色 · 感谢海豹支持)
- Press the skill key to boost your speed for 10 seconds. / 按下技能键提升自身速度，持续 10 秒。
- Can purchase "Xuezi"; eating it immediately activates the skill (ignoring cooldown and cost). / 可购买「雪兹」，吃掉后立刻使用技能（无视冷却与花费）。
- After eating Xuezi, the skill lasts indefinitely until death, during which you are poisoned and gain one shield layer (the shield does not block poison). / 吃完雪兹后技能持续无限直到死亡，期间自身中毒并有一层护盾（护盾不挡毒）。
- Killing one player before dying from poison removes the poison and ends the skill after 5 seconds. / 中毒死亡前击杀一人可解除中毒，并在 5 秒后结束技能。

**Undertaker / 入殓师**
- Can disguise yourself as a corpse. / 可以伪装成尸体。
- Moving while disguised cancels the disguise. / 伪装期间移动会解除伪装。
- Killing a player within 5 seconds of cancelling the disguise does not put the knife on cooldown. / 解除伪装后 5 秒内击杀玩家，刀不会进入冷却。
- While the Undertaker is alive, the Vigilante's instinct to see through corpses is disabled. / 入殓师存活时，义警透视尸体的本能失效。
- The Archivist also cannot distinguish your disguised corpse. / 档案员也无法分辨你的伪装尸体。

**Bomber / 炸弹客** (Sponsor Role · Thanks to Cat Mo for the support / 赞助角色 · 感谢猫陌支持)
- Cannot buy a knife, but grenade prices are reduced. / 无法买刀，但手雷价格降低。
- Can purchase bombs and landmines. / 可以购买炸弹与地雷。
- Bomb: Right-click on another player while holding it to place; after 15 seconds it prompts "30 seconds until explosion"; can be right-clicked to pass to another player; explosion radius is 2×2×2. / 炸弹：手持对他人右键可放置，15 秒后提示「距离爆炸还有 30 秒」，可右键传递他人；爆炸范围 2×2×2。
- Landmine: Right-click on the ground while holding it to place at your feet; stepping on it prevents movement, and moving triggers the explosion (kills only the player who stepped on it); a sound plays when stepped on. / 地雷：手持右键在脚下放置，踩上后不能移动，移动即爆炸（只炸死踩中者），踩上时有声音提示。
- Bombs and landmines are invisible to others while held. / 炸弹与地雷手持时他人不可见。
- The Survival Expert cannot see landmines but can rescue players who step on them. / 生存专家看不到地雷，但可以解救踩中的玩家。
- The Killer faction does not trigger bombs. / 杀手阵营不会触发炸弹。

## ⚙️ Configuration Options / 配置项

- Skill costs for all new roles / 所有新增角色的技能花费
- Shop item prices for all new roles (only Knife, Revolver, and Madness required) / 所有新增角色的商店物品价格（仅需刀、左轮、疯魔）
- Skill cooldowns for all new roles / 所有新增角色的技能冷却时间
- Starting gold for all Neutral and Killer faction roles / 所有中立与杀手阵营角色的初始金币

## 😋 Commands / 指令

**The following commands are available to OP players: / 以下指令可供 OP 玩家使用：**

- `/brinswathe reload` — Applies configuration changes directly in-game without restarting the server. / 直接在游戏内应用配置修改，无需重启服务器
- `/brinswathe afk` — Toggles the in-game AFK execution feature. / 开关游戏内挂机处死功能
- `/setbrinspeed <player> <type> <value>` — Adjusts stamina and sprint speed: / 调整体力与奔跑速度：

| Parameter / 参数 | Description / 说明 |
|---|---|
| maxStamina | Stamina limit (range 1–1000, default 150) / 体力上限（范围 1–1000，默认 150）|
| runSpeed | Sprint speed multiplier (range 0.0–10.0, default 0.1) / 奔跑速度倍率（范围 0.0–10.0，默认 0.1）|
| regenRate | Stamina regeneration rate (range 0–100, default 2 per second) / 体力回复速度（范围 0–100，默认每秒 2）|

- `/setrolecount` — Sets the quantity of each role. / 设置各角色数量
- `/forcerefreshrole` — Forces a refresh of roles. / 强制刷新角色
- `/rolemodifierblacklist` — Configures the role modifier blacklist. / 配置角色修饰符黑名单
