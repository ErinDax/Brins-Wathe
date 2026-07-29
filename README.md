# Brin's Wathe
## 🎤 Required versions and mod loaders
- Minecraft 1.21.1
- Fabric 0.19.3+
- This mod requires the following prerequisites to run:
  - Wathe
  - HarpyModLoader
  - KinsWathe
  - Starry Express

## ✨Some optimizations made by this mod: 

Added public-range chat with a range of 12 blocks. Players beyond 12 blocks cannot see your public chat messages.

## ✨New Roles
The following are the newly added roles:
### New Civilians
**Archivist**
- Archivist can identify corpses forged by the Necro-Corpser.
- When aiming at a corpse, it will show whether the corpse's file is forged.
- Costs 100 gold to prevent the Cleaner from dissolving the corpse.
- Skill cooldown: 120 seconds.

**Eavesdropper**
- Eavesdropper can eavesdrop on all chat messages, ignoring any distance limit.
- No active skill.

**Medium**
- Medium is permanently connected to the dead, which also prevents them from communicating with the living.
- Only the dead can hear your voice.

**Stunt Double**
- Stunt Double is used to being pushed onto the stage.
- Using the skill removes all control effects and teleports you to a safe nearby location.
- Alternatively, you can choose to approach another player and mimic them.
- Skill cooldown: 90 seconds.
- Mimic duration: 30 seconds.

**Watchman**
- Watchman can see clearly in the dark, making him someone everyone trusts during the night.
- No active skill.

### New Neutrals
**Berserker**
- Berserker is the embodiment of destruction.
- After purchasing Madness, you enter an invincible state that lasts until the end of the game.
- You can use Instinct while under the effects of Madness.
- Civilians cannot kill you.
- Kill them all!

**Compensator**
- Compensator gives blood for blood.
- When a civilzan tries to kill you, you will survive by taking their place as the new civilian.
- When a killer tries to kill you, you will survive by taking their place as the new killer.
- When someone from an unknown or unclear faction tries to kill you, you will perish together with them.
- No active skill.

**Gambler**
- Gambler stakes their life on a bet.
- For 100 gold, you can place a bet on a player of your choice. While your bet target is alive, you may switch your bet to another player.
- Your gaze will forever be fixed upon your bet target.
- If your bet target dies after you've placed your bet, you will bite down on the poison capsule in your mouth.
- If your bet target survives for 6 minutes, you win—whether you are alive or not.
- Bet everything you have.

**Nightmare**
- Nightmare sows fear in the hearts of the sleeping.
- Aim at a highlighted player and use the skill to plant fear within them.
- Those who are marked by fear will be too terrified to kill you.
- Spread the fear until only you and the fearful remain.

**Penitent**
- Penitent seeks to cleanse themselves of their sins.
- Win Conditions (choose one):
- ① Kill all killers with your own hands.
- ② Kill three civilians with your own hands.
- If you mix kills between the two lines during your mission, you will die.
- Choose your path to atonement wisely.

### New Killers
**Boneharvester**
- Boneharvester buries the traces of sin.
- If you kill a player while your skill is active, their corpse will not appear until 20 seconds later. The skill lasts 15 seconds.
- You refuse to cooperate with the Cleaner.
- Skill cooldown: 120 seconds.

**Illusionist**
- Illusionist can create phantom clones of themselves.
- Pressing the skill key once creates two clones.
- While clones exist, pressing the skill key again cycles through: East Clone → West Clone → Self.
- While controlling a clone, your real body stays behind. If your real body dies, all clones vanish instantly.
- You can use your clones to kill for you.
- Skill cooldown: 120 seconds.

**Puppeteer**
- Puppeteer is a cunning remote controller.
- Select a player to initiate remote control.
- The controlled player cannot move on their own and is forcibly pulled toward you.
- The target is briefly stunned while being controlled.
- Skill cooldown: 200 seconds.

**Trapper**
- Trapper sets deadly traps.
- Press the skill key to deploy a bear trap at a cost of 125 gold.
- Only you and your allies can see the trap.
- A player caught in the trap will be immobilized until death.
- You can only have one trap active at a time.
- Skill cooldown: 120 seconds.

**Sniper**
- The Sniper possesses excellent long-range elimination skills.
- Press the skill key to enter aiming mode. While in this mode, the Sniper is fully focused. Press the skill key again to take the shot.
- Your bullets are penetrating and lethal.
- Skill cooldown: 150 seconds.

## ⚙️Config Settings
The skill cost for all newly added roles, the shop item prices for all newly added roles (only Knife, Revolver, and Madness are needed), the skill cooldowns for all newly added roles, and the starting gold for all newly added Neutral and Killer roles.

## 😋Commands
**The following commands are available to OP players:**

Use /brinswathe reload to apply configuration changes directly in-game without restarting the server.

Use /setbrinspeed <player> <type> <value> to adjust stamina and run speed:

- Parameter	Description
- maxStamina	Maximum stamina (range: 1–1000, default: 150)
- runSpeed	Run speed multiplier (range: 0.0–10.0, default: 0.1)
- regenRate	Stamina regeneration rate (range: 0–100, default: 2 per second)
