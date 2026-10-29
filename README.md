# Brin's Wathe

*The English content of this mod is translated by AI.*

## 🎤 Required Version & Mod Loader

- Minecraft 1.21.1
- Fabric 0.19.3+
- The following prerequisite mods are required to run this mod:
  - Wathe
  - HarpyModLoader
  - KinsWathe
  - Starry Express

## ✨ Optimization & Additional Systems in this Mod

- **Proximity Public Chat**: The public chat range is 12 blocks. Players beyond 12 blocks cannot see your public messages.
- **Draft Role Selection**: At the start of the game, players take turns selecting their roles in order, with a countdown display. When it's your turn, the interface pops up automatically; if you time out, a role is randomly assigned.
- **Match Review**: At the end of the round, kills, deaths, and shield breaks are displayed, along with detailed causes of death (knife, gun, baseball bat, explosion, poison, run over by train, voodoo, ignition, lover's suicide, ignition failure, exam failure, virus infection, guilt suicide, suffocation, low mood, allergy, etc.).
- **Stamina & Sprint System**: A new stamina value is added. Sprinting consumes stamina, and when stamina is depleted, you cannot sprint. Stamina limit, sprint multiplier, and regeneration rate can be adjusted via commands.
- **Music Box**: A music box is built into the personal settings, supporting uploading and playing custom music.
- **Rock-Paper-Scissors**: A built-in Rock-Paper-Scissors mini-game.
- **Admin Settings Panel**: Visually toggle instincts, match rules, role selection methods, the music box, and other features without manually editing configs.
- **AFK Handling**: An in-game AFK execution feature can be enabled.

## ✨ New Roles (22 in total)

### Innocent Faction (8)

**Stunt Double**
- Can select a player as a stand-in target in the inventory interface.
- While standing in, if that player takes fatal damage, you die in their place.

**Medium**
- Spend money to channel the dead for a short time.
- During the channel, you can talk to the dead and gather information.

**Operator**
- Can monitor all chat messages, ignoring distance limits.
- Spend gold to select a non-Medium player and pull both of you into a temporary voice channel.
- The channel lasts for a period, during which you cannot leave voluntarily.
- Has a skill cooldown.

**Survival Expert**
- Has permanent Night Vision.
- Can see players trapped by bear traps or landmines.
- Can see bear traps (and can therefore also be caught by them); cannot see landmines, but landmines cannot trap you.
- Spend money and aim at a trapped player, then press the skill key to rescue them; if you are trapped, press the skill key without aiming at others to free yourself.

**Archivist**
- Aim at a corpse, press the skill key, and spend money to seal it.
- Sealed corpses cannot be dissolved by the Cleaner or revived by the Necromancer.
- Can see through fake corpses created by the Corpse Maker.
- However, cannot distinguish the disguised corpses of the Undertaker.

**Pervert**
- Aim at a player and press the skill key to track them.
- Can always see the tracked player's icon until they die.
- Can switch tracking targets after the cooldown ends.

**Avenger**
- Upon witnessing another's death, gains a brief instinct.

**Cowboy**
- After saving enough money, can select a player to initiate a "Western Duel" (open inventory to select player avatar).
- During the duel, only the Cowboy's Revolver can be used; active and passive skills are disabled.
- The duel ends when only one player remains alive, and all players return to the game area.
- If neither side scores a kill within the time limit, both players die.
- Duel drops are teleported back to the position held before entering the duel; shooting penalties do not apply during the duel.
- ⚠️ A duel arena must be prepared (Y level 118 or above), and spawn points for both players and spectators must be set in the config file, with default orientations facing East/West.

### Neutral Faction (6)

**Nightmare**
- Starts with one shield layer and can see players who are sleeping.
- Can "sow" sleeping players; each sown player grants an additional shield layer (max two).
- Before the Nightmare Hour begins, sown players cannot kill the Nightmare.
- Can assign sleep tasks to living players.
- If the Killer faction is fully dead and the Nightmare is still alive, the "Nightmare Hour" is triggered, granting a knife, killer instinct, and access to the killer shop.
- Settles together with the killers before Nightmare Hour, and independently after.
- Win condition: Sow all living players, or kill everyone.

**Berserker**
- The Innocent faction cannot kill you (except during Cowboy time).
- After saving enough gold, can purchase "Infinite Madness" mode, during which the only way to die is by falling out of the game area.
- Automatically activates Infinite Madness when no other killers or independently-settling neutral players are alive.
- Win by killing everyone.

**Substitute**
- When an Innocent tries to kill you, you take their place and survive as a new Innocent.
- When a Killer tries to kill you, you take their place and survive as a new Killer.
- When someone of unknown faction tries to kill you, you die together with them.

**Gambler**
- Spend money, aim your crosshair at a player, and place a bet.
- Can change the bet target while the current target is alive.
- The bet target is permanently highlighted to you.
- If the bet target dies, you are poisoned (this poison cannot be cured).
- If the bet target survives for 6 minutes, you win regardless of whether you are alive.

**Atoner**
- Must personally kill players of designated roles; reaching the required number allows independent victory.
- Killing the correct role grants a shield layer; killing the wrong role causes poisoning.
- Can purchase role hints and antidotes from the shop (antidotes are used immediately upon purchase).
- If the Killers win and your condition is not met, you settle alongside the Killers.

**Terrorist**
- When killed by gunfire, triggers a 5×5×5 explosion.
- If the explosion kills 4 or more players, the Terrorist survives.

### Killer Faction (8)

**Puppeteer**
- Aim at a corpse and press the skill key to turn it into a puppet.
- Opening the inventory displays created puppets; click an icon to start controlling it, during which your real body cannot move.
- The puppet comes with a knife and a crowbar, lasting 60 seconds, and is exposed at the 45-second mark.
- If the puppet is destroyed without being self-recalled, your real body is highlighted for 10 seconds; if the puppet is killed, you additionally receive Slowness for 15 seconds.
- Used puppets disappear directly without leaving a corpse.
- After purchasing "Self-Destruct," a controlled puppet's death produces a 4×4×4 explosion centered on your real body, ignoring shields.
- If your real body dies, the puppet vanishes immediately.

**Trapper**
- Press the skill key and spend gold to deploy a bear trap.
- Bear traps are visible only to yourself, teammates, and the Survival Expert.
- Disappears if no one steps on it within 3.5 minutes after placement.
- Trapped players are immobilized until death.
- Earn gold each time a player is trapped.
- Can purchase extra bear traps from the shop; purchased traps ignore quantity limits and are placed directly at your feet.

**Illusionist**
- Press the skill key to spawn two clones, fixed facing East/West.
- Press the key again while the skill is active to switch perspectives: East clone → West clone → real body.
- Your real body remains in place while controlling clones; if the real body dies, all clones vanish instantly.
- Clones can interact and can kill.

**Sniper**
- Cannot use instinct while the skill is active, and is highlighted.
- Press the skill key to enter aiming mode (ESC to exit); cannot move while aiming.
- Press the key again to fire a bullet that pierces walls, with a max range of 300 blocks.
- After use, you are highlighted for a period; completing tasks restores the skill.

**Bone Collector**
- Passive skill: Corpses of players you kill do not reveal themselves for 20 seconds.
- Can purchase a Boning Knife from the shop; use it on a corpse with bones to remove the bones and grant yourself a shield layer.
- There is theoretically no cap on the number of shields.

**Zhang Shi** (Sponsor Role · Thanks to Seal for the support)
- Press the skill key to boost your speed for 10 seconds.
- Can purchase "Xuezi"; eating it immediately activates the skill (ignoring cooldown and cost).
- After eating Xuezi, the skill lasts indefinitely until death, during which you are poisoned and gain one shield layer (the shield does not block poison).
- Killing one player before dying from poison removes the poison and ends the skill after 5 seconds.

**Undertaker**
- Can disguise yourself as a corpse.
- Moving while disguised cancels the disguise.
- Killing a player within 5 seconds of cancelling the disguise does not put the knife on cooldown.
- While the Undertaker is alive, the Vigilante's instinct to see through corpses is disabled.
- The Archivist also cannot distinguish your disguised corpse.

**Bomber** (Sponsor Role · Thanks to Cat Mo for the support)
- Cannot buy a knife, but grenade prices are reduced.
- Can purchase bombs and landmines.
- Bomb: Right-click on another player while holding it to place; after 15 seconds it prompts "30 seconds until explosion"; can be right-clicked to pass to another player; explosion radius is 2×2×2.
- Landmine: Right-click on the ground while holding it to place at your feet; stepping on it prevents movement, and moving triggers the explosion (kills only the player who stepped on it); a sound plays when stepped on.
- Bombs and landmines are invisible to others while held.
- The Survival Expert cannot see landmines but can rescue players who step on them.
- The Killer faction does not trigger bombs.

## ⚙️ Configuration Options

- Skill costs for all new roles
- Shop item prices for all new roles (only Knife, Revolver, and Madness required)
- Skill cooldowns for all new roles
- Starting gold for all Neutral and Killer faction roles

## 😋 Commands

**The following commands are available to OP players:**

- `/brinswathe reload` — Applies configuration changes directly in-game without restarting the server.
- `/brinswathe afk` — Toggles the in-game AFK execution feature.
- `/setbrinspeed <player> <type> <value>` — Adjusts stamina and sprint speed:

| Parameter | Description |
|---|---|
| maxStamina | Stamina limit (range 1–1000, default 150) |
| runSpeed | Sprint speed multiplier (range 0.0–10.0, default 0.1) |
| regenRate | Stamina regeneration rate (range 0–100, default 2 per second) |

- `/setrolecount` — Sets the quantity of each role.
- `/forcerefreshrole` — Forces a refresh of roles.
- `/rolemodifierblacklist` — Configures the role modifier blacklist.
