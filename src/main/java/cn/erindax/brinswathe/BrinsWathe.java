package cn.erindax.brinswathe;

import cn.erindax.brinswathe.command.SetBrinSpeedCommand;
import cn.erindax.brinswathe.command.BrinConfigCommand;
import cn.erindax.brinswathe.command.BrinIcCommands;
import cn.erindax.brinswathe.command.ForceRefreshRoleCommand;
import cn.erindax.brinswathe.command.RoleModifierBlacklistCommand;
import cn.erindax.brinswathe.command.SetRoleCountCommand;
import cn.erindax.brinswathe.component.ArchivistComponent;
import cn.erindax.brinswathe.component.BerserkerComponent;
import cn.erindax.brinswathe.component.BombComponent;
import cn.erindax.brinswathe.component.BoneharvesterComponent;
import cn.erindax.brinswathe.component.BrinCustomWinnerComponent;
import cn.erindax.brinswathe.component.CowboyComponent;
import cn.erindax.brinswathe.component.GamblerComponent;
import cn.erindax.brinswathe.component.EavesdropperComponent;
import cn.erindax.brinswathe.component.IllusionistComponent;
import cn.erindax.brinswathe.component.MediumComponent;
import cn.erindax.brinswathe.component.MorticianComponent;
import cn.erindax.brinswathe.component.MuzzlerAbilityComponent;
import cn.erindax.brinswathe.component.NightmareComponent;
import cn.erindax.brinswathe.component.PenitentComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import cn.erindax.brinswathe.component.ResurrectedComponent;
import cn.erindax.brinswathe.component.SniperComponent;
import cn.erindax.brinswathe.component.StalkerComponent;
import cn.erindax.brinswathe.component.StaminaComponent;
import cn.erindax.brinswathe.component.StuntDoubleComponent;
import cn.erindax.brinswathe.component.TrapperComponent;
import cn.erindax.brinswathe.component.ZhangshiComponent;
import cn.erindax.brinswathe.config.BrinConfig;
import cn.erindax.brinswathe.entity.ArchivistSealedCorpse;
import cn.erindax.brinswathe.network.BlindFlashS2CPacket;
import cn.erindax.brinswathe.network.BrinAbilityC2SPacket;
import cn.erindax.brinswathe.network.BrinConfigS2CPacket;
import cn.erindax.brinswathe.network.BrinIcNightVisionS2CPacket;
import cn.erindax.brinswathe.network.BrinInstinctC2SPacket;
import cn.erindax.brinswathe.network.BrinInstinctSnapshotS2CPacket;
import cn.erindax.brinswathe.network.BrinKnifeSkinApplyS2CPacket;
import cn.erindax.brinswathe.network.BrinKnifeSkinListS2CPacket;
import cn.erindax.brinswathe.network.BrinResourceReloadS2CPacket;
import cn.erindax.brinswathe.network.CowboyShowdownMusicS2CPacket;
import cn.erindax.brinswathe.network.CowboyDuelHideS2CPacket;
import cn.erindax.brinswathe.network.CowboyDuelIdentityS2CPacket;
import cn.erindax.brinswathe.network.CowboyDuelLookLockS2CPacket;
import cn.erindax.brinswathe.network.IllusionistControlC2SPacket;
import cn.erindax.brinswathe.network.RpsActionC2SPacket;
import cn.erindax.brinswathe.network.RpsStateS2CPacket;
import cn.erindax.brinswathe.voice.BrinVoiceChatPlugin;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.api.event.AllowPlayerDeath;
import dev.doctor4t.wathe.api.event.GameEvents;
import dev.doctor4t.wathe.api.event.ShouldDropOnDeath;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.index.WatheItems;

import dev.doctor4t.wathe.util.ShopEntry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.agmas.harpymodloader.events.ResetPlayerEvent;
import org.aussiebox.starexpress.StarryExpress;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.AbilityComponent;
import org.aussiebox.starexpress.item.StarryExpressItems;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BrinsWathe implements ModInitializer {
	public static final String MOD_ID = "brinswathe";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final int LOW_MOOD_DEATH_DELAY_TICKS = 60 * 20;
	private static final float VIGILANTE_DETECT_RANGE = 2.5F;
	private static final Map<UUID, Integer> LOW_MOOD_DEATH_TIMERS = new HashMap<>();

	@Override
	public void onInitialize() {
		LOGGER.info("Brin's Wathe initializing");

		BrinConfig.initialize();
		BrinIcFlags.load();
		BrinKnifeSkins.load();
		BrinKnifeSkins.scanAndRegister();
		StaminaComponent.loadPersistedOverrides();
		BrinItems.init();
		BrinSounds.init();
		BrinRoles.init();
		BrinIcModifiers.register();

		applyMuzzlerConfig();

		PayloadTypeRegistry.playC2S().register(BrinAbilityC2SPacket.TYPE, BrinAbilityC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(
			IllusionistControlC2SPacket.TYPE,
			IllusionistControlC2SPacket.STREAM_CODEC
		);
		PayloadTypeRegistry.playS2C().register(BlindFlashS2CPacket.TYPE, BlindFlashS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(CowboyShowdownMusicS2CPacket.TYPE, CowboyShowdownMusicS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(CowboyDuelHideS2CPacket.TYPE, CowboyDuelHideS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(CowboyDuelIdentityS2CPacket.TYPE, CowboyDuelIdentityS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(CowboyDuelLookLockS2CPacket.TYPE, CowboyDuelLookLockS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(BrinConfigS2CPacket.TYPE, BrinConfigS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(BrinResourceReloadS2CPacket.TYPE, BrinResourceReloadS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(RpsActionC2SPacket.TYPE, RpsActionC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(RpsStateS2CPacket.TYPE, RpsStateS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(BrinInstinctC2SPacket.TYPE, BrinInstinctC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(BrinInstinctSnapshotS2CPacket.TYPE, BrinInstinctSnapshotS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(BrinKnifeSkinListS2CPacket.TYPE, BrinKnifeSkinListS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(BrinKnifeSkinApplyS2CPacket.TYPE, BrinKnifeSkinApplyS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(BrinIcNightVisionS2CPacket.TYPE, BrinIcNightVisionS2CPacket.STREAM_CODEC);
		registerPackets();
		registerConfigSync();
		registerCommands();
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			CowboyDuel.clear();
			RpsManager.clear();
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server ->
			BrinRoleWeights.apply(server, BrinIcFlags.roleWeights)
		);
		registerIllusionistCleanup();
		registerRoleDeathRules();
		registerBombPassing();
		ShouldDropOnDeath.EVENT.register((stack, player) ->
			stack.is(WatheItems.KEY)
				|| stack.is(BuiltInRegistries.ITEM.get(
					ResourceLocation.fromNamespaceAndPath("noellesroles", "master_key"))));

		GameEvents.ON_FINISH_INITIALIZE.register((world, gameWorld) -> {
			if (world.getServer() != null) {
				RpsManager.abortAll(world.getServer());
			}
			for (var player : world.players()) {
				if (!(player instanceof ServerPlayer serverPlayer)) continue;
				String roleId = BrinRoles.getRoleId(gameWorld, serverPlayer);
				if (roleId != null) {
					Integer balance = BrinConfig.initialBalance(roleId);
					if (balance != null) {
						PlayerShopComponent.KEY.get(serverPlayer).setBalance(balance);
					}
				}
				if (gameWorld.isRole(serverPlayer, StarryExpressRoles.MUZZLER)) {
					ShopEntry.insertStackInFreeSlot(serverPlayer, StarryExpressItems.TAPE.getDefaultInstance());
				}
				if (BrinNoelleAccess.isRole(gameWorld, serverPlayer, BrinNoelleAccess.JESTER_ID)) {
					ShopEntry.insertStackInFreeSlot(serverPlayer, WatheItems.CROWBAR.getDefaultInstance());
				}
				if (BrinNoelleAccess.isRole(gameWorld, serverPlayer, BrinNoelleAccess.CONDUCTOR_ID)) {
					ShopEntry.insertStackInFreeSlot(serverPlayer, WatheItems.LOCKPICK.getDefaultInstance());
				}
				if (BrinShopAccess.isThief(gameWorld, serverPlayer) || BrinShopAccess.isArsonist(gameWorld, serverPlayer)) {
					ShopEntry.insertStackInFreeSlot(serverPlayer, WatheItems.LOCKPICK.getDefaultInstance());
				}
			}
			BrinIcRoundStart.apply(world, gameWorld);
		});
		GameEvents.ON_GAME_START.register(gameMode -> BrinItemCooldowns.schedule());
		ResetPlayerEvent.EVENT.register(player -> {
			MuzzlerAbilityComponent component = MuzzlerAbilityComponent.KEY.get(player);
			if (component != null) component.reset();
			BrinIcRoundStart.resetPlayer(player);
			BrinVoiceChatPlugin.resetPlayer(player.getUUID());
		});

		ServerTickEvents.START_SERVER_TICK.register(BrinsWathe::onServerTick);
		ServerTickEvents.END_SERVER_TICK.register(RpsManager::holdSelecting);
	}

	private void registerConfigSync() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayNetworking.send(handler.player, new BrinConfigS2CPacket(BrinConfig.toJson()));
			BrinIcNightVisionS2CPacket.sendTo(handler.player);
			StaminaComponent stamina = StaminaComponent.KEY.get(handler.player);
			if (stamina != null) stamina.applyGlobalOverridesForced();
			CowboyDuel.syncJoiningClient(handler.player);
			BrinKnifeSkins.syncTo(handler.player);
			List<String> announcement = BrinConfig.announcement();
			if (!announcement.isEmpty()) {
				handler.player.sendSystemMessage(Component.literal(String.join("\n", announcement)));
			}
		});
	}

	private void registerRoleDeathRules() {
	AllowPlayerDeath.EVENT.register((victim, attacker, deathReason) -> {
		refreshJesterPsychoArmour(victim, deathReason);
		if (CowboyDuel.isActive()) return true;
		if (StuntDoubleDeathTransfer.tryTransfer(victim, attacker, deathReason)) return false;
		GameWorldComponent gameWorld = GameWorldComponent.KEY.get(victim.level());
			if (gameWorld.isRole(victim, BrinRoles.NIGHTMARE) && attacker != null) {
				NightmareComponent nightmare = NightmareComponent.KEY.get(victim);
				if (nightmare != null && nightmare.isMarked(attacker.getUUID())) return false;
			}
			if (gameWorld.isRole(victim, BrinRoles.NIGHTMARE)
				&& !GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(deathReason)) {
				NightmareComponent nightmare = NightmareComponent.KEY.get(victim);
				if (nightmare != null && nightmare.consumeShield()) {
					victim.level().playSound(null, victim.blockPosition(), SoundEvents.SHIELD_BLOCK,
						SoundSource.PLAYERS, 1.0F, 1.0F);
					return false;
				}
			}
			if (gameWorld.isRole(victim, BrinRoles.PENITENT)
				&& !GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(deathReason)) {
				PenitentComponent penitent = PenitentComponent.KEY.get(victim);
				if (penitent != null && penitent.consumeShield()) {
					victim.level().playSound(null, victim.blockPosition(), SoundEvents.SHIELD_BLOCK,
						SoundSource.PLAYERS, 1.0F, 1.0F);
					return false;
				}
			}
			if (gameWorld.isRole(victim, BrinRoles.BONEHARVESTER)
				&& !GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(deathReason)) {
				BoneharvesterComponent boneharvester = BoneharvesterComponent.KEY.get(victim);
				if (boneharvester != null && boneharvester.consumeShield()) {
					victim.level().playSound(null, victim.blockPosition(), SoundEvents.SHIELD_BLOCK,
						SoundSource.PLAYERS, 1.0F, 1.0F);
					return false;
				}
			}
            if (gameWorld.isRole(victim, BrinRoles.ZHANGSHI)
                && !GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(deathReason)
                && !GameConstants.DeathReasons.POISON.equals(deathReason)) {
                ZhangshiComponent zhangshi = ZhangshiComponent.KEY.get(victim);
                if (zhangshi != null && zhangshi.consumeShield()) {
                    victim.level().playSound(null, victim.blockPosition(), SoundEvents.SHIELD_BLOCK,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                    return false;
                }
            }
			if (gameWorld.isRole(victim, BrinRoles.BERSERKER)) {
				if (GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(deathReason)) return true;

				BerserkerComponent berserker = BerserkerComponent.KEY.get(victim);
				if (berserker != null && berserker.psychoActive) return false;
				if (attacker != null && gameWorld.isInnocent(attacker)) return false;
			}
			return TerroristExplosion.allowGunDeath(victim, attacker, deathReason);
		});
	}

	private static void refreshJesterPsychoArmour(Player victim, ResourceLocation deathReason) {
		if (GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(deathReason)) return;
		GameWorldComponent gameWorld = GameWorldComponent.KEY.get(victim.level());
		if (!BrinNoelleAccess.isRole(gameWorld, victim, BrinNoelleAccess.JESTER_ID)) return;
		PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(victim);
		if (psycho == null || psycho.getPsychoTicks() <= GameConstants.getInTicks(0, 44)) return;
		psycho.setArmour(Math.max(0, BrinPsychoArmour.calculate(victim) - 1));
	}

	private void registerBombPassing() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			if (level.isClientSide || hand != InteractionHand.MAIN_HAND || hitResult == null) {
				return InteractionResult.PASS;
			}
			return BomberBombs.tryPassBomb(player, entity);
		});
	}

	private void registerIllusionistCleanup() {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			LOW_MOOD_DEATH_TIMERS.remove(handler.player.getUUID());
			BrinInstinctSnapshots.remove(handler.player.getUUID());
			RpsManager.onDisconnect(handler.player);
			IllusionistComponent component = IllusionistComponent.KEY.get(handler.player);
			if (component != null) component.reset();
			EavesdropperComponent eavesdropper = EavesdropperComponent.KEY.get(handler.player);
			if (eavesdropper != null) eavesdropper.reset();
		});
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			IllusionistComponent component = IllusionistComponent.KEY.get(player);
			if (component != null) component.reset();
		});
	}

	private static void onServerTick(MinecraftServer server) {
		BrinPushed.beginTick();
		AfkKickManager.tick(server);
		BrinVoiceChatPlugin.tick(server);
		BrinKnifeSkins.tick(server);
		BrinItemCooldowns.tick(server);
		BrinInstinctSnapshots.tick(server);
        AvengerWitness.tick(server);
        CowboyDuel.tick(server);
        RpsManager.tick(server);
        processDelayedCorpses(server);
        boolean duelActive = CowboyDuel.isActive();
        Set<GameWorldComponent> runningGames = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<UUID> processedGamblers = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
            if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) {
				LOW_MOOD_DEATH_TIMERS.remove(player.getUUID());
				continue;
			}
            runningGames.add(gameWorld);
			if (!duelActive) tickLowMoodDeathTimer(player);

            NightmareComponent nightmareComp = NightmareComponent.KEY.get(player);
            if (nightmareComp != null && gameWorld.isRole(player, BrinRoles.NIGHTMARE)) {
                nightmareComp.tickForcedSleepTaskCooldown();
            }

            TrapperComponent trapperComp = TrapperComponent.KEY.get(player);
            if (trapperComp != null && gameWorld.isRole(player, BrinRoles.TRAPPER)) {
                checkTrapperTrap(player, trapperComp);
            }

            if (gameWorld.isRole(player, BrinRoles.WATCHMAN) && server.getTickCount() % 20 == 0) {
                stripSurvivalExpertLeftoverKits(player);
            }
        }

        if (duelActive) return;

        for (GameWorldComponent gameWorld : runningGames) {
            beginNightmareHourIfDue(server, gameWorld);
            checkNightmareWin(server, gameWorld);
            if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) continue;
            checkBerserkerWin(server, gameWorld);
            if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) continue;
            checkGamblerWin(server, gameWorld, processedGamblers);
            if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) continue;
            checkPenitentWin(server, gameWorld);
        }

    }

	private static void stripSurvivalExpertLeftoverKits(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (BrinShopAccess.isSurvivalExpertExcludedItem(inventory.getItem(slot))) {
				inventory.setItem(slot, ItemStack.EMPTY);
			}
		}
	}
	private static void tickLowMoodDeathTimer(ServerPlayer player) {
		UUID playerId = player.getUUID();
		if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
			LOW_MOOD_DEATH_TIMERS.remove(playerId);
			return;
		}
		PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
		if (mood == null || mood.getMood() > 0.0F) {
			LOW_MOOD_DEATH_TIMERS.remove(playerId);
			return;
		}
		int remainingTicks = LOW_MOOD_DEATH_TIMERS.getOrDefault(playerId, LOW_MOOD_DEATH_DELAY_TICKS);
		if (remainingTicks <= 1) {
			LOW_MOOD_DEATH_TIMERS.remove(playerId);
			GameFunctions.killPlayer(player, true, null, GameConstants.DeathReasons.GENERIC);
			return;
		}
		LOW_MOOD_DEATH_TIMERS.put(playerId, remainingTicks - 1);
	}

    private static void processDelayedCorpses(MinecraftServer server) {
        if (BoneharvesterCorpseManager.delayedCorpseAppearances.isEmpty()) return;
        boolean gameActive = false;
        for (var level : server.getAllLevels()) {
            if (GameWorldComponent.KEY.get(level).getGameStatus() == GameWorldComponent.GameStatus.ACTIVE) {
                gameActive = true;
                break;
            }
        }
        if (!gameActive) {
            for (var level : server.getAllLevels()) {
                for (var entity : level.getAllEntities()) {
                    if (entity instanceof PlayerBodyEntity body) body.setInvisible(false);
                }
            }
            BoneharvesterCorpseManager.delayedCorpseAppearances.clear();
            return;
        }
        java.util.List<UUID> toRemove = new ArrayList<>();
        for (var entry : BoneharvesterCorpseManager.delayedCorpseAppearances.entrySet()) {
            UUID playerUuid = entry.getKey();
            int remaining = entry.getValue() - 1;
            boolean found = false;
            for (var level : server.getAllLevels()) {
                for (var e : level.getAllEntities()) {
                    if (e instanceof PlayerBodyEntity body && playerUuid.equals(body.getPlayerUuid())) {
                        if (remaining > 0) {
                            body.setInvisible(true);
                            entry.setValue(remaining);
                        } else {
                            body.setInvisible(false);
                            toRemove.add(playerUuid);
                        }
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
            if (!found) {
                if (remaining > 0) entry.setValue(remaining);
                else toRemove.add(playerUuid);
            }
        }
        for (UUID uuid : toRemove) {
            BoneharvesterCorpseManager.delayedCorpseAppearances.remove(uuid);
        }
    }
	private static void checkTrapperTrap(ServerPlayer player, TrapperComponent comp) {
		for (UUID trapEntityId : comp.activeTrapEntityIds()) {
			var entity = player.serverLevel().getEntity(trapEntityId);
			if (entity == null || !entity.isAlive()) {
				comp.removeTrap(trapEntityId);
				continue;
			}
			for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
				if (other.level() != entity.level()) continue;
				if (!GameFunctions.isPlayerAliveAndSurvival(other)) continue;
				if (comp.isTrapped(other.getUUID())) continue;
				GameWorldComponent gameWorld = GameWorldComponent.KEY.get(other.level());
				if (gameWorld.canUseKillerFeatures(other)) continue;
				if (other.getBoundingBox().inflate(0.3).intersects(entity.getBoundingBox())) {
					PlayerEffectComponent effect = PlayerEffectComponent.KEY.get(other);
					if (effect == null) continue;
					effect.setStunTicks(TrapperComponent.TRAP_STUN_TICKS);
					comp.trapPlayer(trapEntityId, other.getUUID());
					brinPayTrapperCatchReward(player);
					other.level().playSound(null, other.blockPosition(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 1.0f, 1.0f);
					entity.discard();
					break;
				}
			}
		}
	}
	@Nullable
	private static ServerPlayer livingNightmare(MinecraftServer server, GameWorldComponent gameWorld) {
		for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
			if (GameWorldComponent.KEY.get(sp.level()) != gameWorld) continue;
			if (gameWorld.isRole(sp, BrinRoles.NIGHTMARE)
				&& GameFunctions.isPlayerAliveAndSurvival(sp)
				&& NightmareComponent.KEY.get(sp) != null) {
				return sp;
			}
		}
		return null;
	}
	public static boolean killerTeamWipedOut(MinecraftServer server, GameWorldComponent gameWorld) {
		List<UUID> killers = gameWorld.getAllKillerTeamPlayers();
		if (killers.isEmpty()) return false;
		for (UUID killerId : killers) {
			ServerPlayer killer = server.getPlayerList().getPlayer(killerId);
			if (killer != null && !GameFunctions.isPlayerEliminated(killer)) return false;
		}
		return true;
	}
	public static boolean beginNightmareHourIfDue(MinecraftServer server, GameWorldComponent gameWorld) {
		if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return false;
		ServerPlayer nightmare = livingNightmare(server, gameWorld);
		if (nightmare == null) return false;
		NightmareComponent component = NightmareComponent.KEY.get(nightmare);
		if (component.isNightmareHour()) return true;
		if (!killerTeamWipedOut(server, gameWorld)) return false;
		component.beginNightmareHour();
		ShopEntry.insertStackInFreeSlot(nightmare, WatheItems.KNIFE.getDefaultInstance());
		nightmare.displayClientMessage(
			Component.translatable("message.brinswathe.nightmare.hour").withColor(BrinRoles.NIGHTMARE.color()),
			false
		);
		nightmare.displayClientMessage(
			Component.translatable("message.brinswathe.nightmare.hour.overlay").withColor(BrinRoles.NIGHTMARE.color()),
			true
		);
		nightmare.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.6F, 0.7F);
		return true;
	}
	public static boolean claimNightmareHourWin(MinecraftServer server, GameWorldComponent gameWorld) {
		ServerPlayer nightmare = livingNightmare(server, gameWorld);
		if (nightmare == null) return false;
		NightmareComponent component = NightmareComponent.KEY.get(nightmare);
		if (!component.isNightmareHour()) return false;
		endWithCustomWinner(nightmare, "nightmare", BrinRoles.NIGHTMARE.color());
		return true;
	}
	private static void checkNightmareWin(MinecraftServer server, GameWorldComponent gameWorld) {
		if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
		ServerPlayer nightmarePlayer = livingNightmare(server, gameWorld);
		if (nightmarePlayer == null) return;
		NightmareComponent nightmareComp = NightmareComponent.KEY.get(nightmarePlayer);
		Set<UUID> alivePlayers = new HashSet<>();
		for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
			if (GameFunctions.isPlayerAliveAndSurvival(sp) && !sp.equals(nightmarePlayer)) {
				alivePlayers.add(sp.getUUID());
			}
		}

		if (alivePlayers.isEmpty()) {
			if (nightmareComp.isNightmareHour()) {
				endWithCustomWinner(nightmarePlayer, "nightmare", BrinRoles.NIGHTMARE.color());
			}
			return;
		}
		if (nightmareComp.markedPlayers.containsAll(alivePlayers)) {
			endWithCustomWinner(nightmarePlayer, "nightmare", BrinRoles.NIGHTMARE.color());
		}
	}
	public static boolean checkBerserkerWin(MinecraftServer server, GameWorldComponent gameWorld) {
		ServerPlayer berserkerPlayer = null;
		for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
			if (GameWorldComponent.KEY.get(sp.level()) == gameWorld
				&& gameWorld.isRole(sp, BrinRoles.BERSERKER)
				&& GameFunctions.isPlayerAliveAndSurvival(sp)) {
				berserkerPlayer = sp;
				break;
			}
		}
		if (berserkerPlayer == null) return false;
		int aliveCount = 0;
		for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
			if (GameWorldComponent.KEY.get(sp.level()) == gameWorld
				&& GameFunctions.isPlayerAliveAndSurvival(sp)) {
				aliveCount++;
			}
		}
		if (aliveCount == 1) {
			endWithCustomWinner(berserkerPlayer, "berserker", BrinRoles.BERSERKER.color());
		}
		return true;
	}
	private static void endWithCustomWinner(ServerPlayer winner, String winningTextId, int color) {
		BrinCustomWinnerComponent winnerComponent = BrinCustomWinnerComponent.KEY.get(winner.level());
		winnerComponent.setWinningTextId(winningTextId);
		winnerComponent.setWinners(List.of(winner.getUUID()));
		winnerComponent.setColor(color);
		winnerComponent.sync();
		GameRoundEndComponent roundEndComponent = GameRoundEndComponent.KEY.get(winner.level());
		roundEndComponent.setRoundEndData(
			winner.serverLevel().players(),
			GameFunctions.WinStatus.KILLERS
		);
		List<GameRoundEndComponent.RoundEndData> playerRows = roundEndComponent.getPlayers();
		for (int index = 0; index < playerRows.size(); index++) {
			GameRoundEndComponent.RoundEndData row = playerRows.get(index);
			boolean wasDead = ("gambler".equals(winningTextId) || "nightmare".equals(winningTextId))
				? !row.player().getId().equals(winner.getUUID())
				: row.wasDead();
			RoleAnnouncementTexts.RoleAnnouncementText category =
				row.player().getId().equals(winner.getUUID())
					? RoleAnnouncementTexts.VIGILANTE
					: RoleAnnouncementTexts.CIVILIAN;
			playerRows.set(index, new GameRoundEndComponent.RoundEndData(
				row.player(),
				category,
				wasDead
			));
		}
		roundEndComponent.sync();
		GameFunctions.stopGame(winner.serverLevel());
	}
	private void applyMuzzlerConfig() {
		try {
			StarryExpress.CONFIG.muzzlerConfig.tapeCooldown(60);
			LOGGER.info("Applied Muzzler skill config: tape cooldown = 60 seconds");
			dev.doctor4t.wathe.util.ShopEntry entry = org.aussiebox.starexpress.StarryExpressConstants.MUZZLER_SHOP.get(1);
			if (entry != null && entry.price() == 75) {
				dev.doctor4t.wathe.util.ShopEntry newEntry = new dev.doctor4t.wathe.util.ShopEntry(
					entry.stack(), 50, entry.type()
				);
				org.aussiebox.starexpress.StarryExpressConstants.MUZZLER_SHOP.set(1, newEntry);
				LOGGER.info("Applied Muzzler skill config: TAPE price = 50");
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to apply Muzzler config: {}", e.getMessage());
		}
	}
	private void registerPackets() {
		ServerPlayNetworking.registerGlobalReceiver(
			BrinAbilityC2SPacket.TYPE,
			(payload, context) -> {
				ServerPlayer player = context.player();
				if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
				if (org.BsXinQin.kinswathe.component.GameSafeComponent.KEY
					.get(player.level()).isGameSafe
					&& payload.abilityType() != BrinAbilityC2SPacket.ABILITY_SNIPER_CANCEL
					&& payload.abilityType() != BrinAbilityC2SPacket.ABILITY_STALKER_TRACK
					&& payload.abilityType() != BrinAbilityC2SPacket.ABILITY_MUZZLER_SILENCE) return;
				if (CowboyDuel.isActive()
					&& payload.abilityType() != BrinAbilityC2SPacket.ABILITY_SNIPER_CANCEL) return;
				player.resetLastActionTime();
				if (payload.abilityType() == BrinAbilityC2SPacket.ABILITY_MUZZLER_SILENCE) {
					handleMuzzlerSilence(player);
					return;
				}
				GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
				AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
                switch (payload.abilityType()) {
                    case BrinAbilityC2SPacket.ABILITY_PUPPETEER_CRAFT -> handlePuppeteerCraft(player, gameWorld, payload);
                    case BrinAbilityC2SPacket.ABILITY_PUPPETEER_SUMMON -> handlePuppeteerSummon(player, gameWorld, ability, payload);
                    case BrinAbilityC2SPacket.ABILITY_PUPPETEER_RETURN -> handlePuppeteerReturn(player, gameWorld);
                    case BrinAbilityC2SPacket.ABILITY_STUNT_DOUBLE_MIMIC -> handleStuntDoubleMimic(player, gameWorld, ability, payload);
                    case BrinAbilityC2SPacket.ABILITY_MEDIUM_JOIN_VOICE -> handleMediumJoinVoice(player, gameWorld, ability);
                    case BrinAbilityC2SPacket.ABILITY_TRAPPER_PLACE_TRAP -> handleTrapperPlaceTrap(player, gameWorld, ability);
                    case BrinAbilityC2SPacket.ABILITY_NIGHTMARE_PLANT -> handleNightmarePlant(player, gameWorld, ability, payload);
                    case BrinAbilityC2SPacket.ABILITY_NIGHTMARE_FORCE_SLEEP -> handleNightmareForceSleep(player, gameWorld, payload);
                    case BrinAbilityC2SPacket.ABILITY_ILLUSIONIST_CLONES -> handleIllusionistClones(player, gameWorld, ability);
                    case BrinAbilityC2SPacket.ABILITY_ILLUSIONIST_SWITCH_CONTROL -> handleIllusionistSwitchControl(player, gameWorld, payload);
                    case BrinAbilityC2SPacket.ABILITY_ARCHIVIST_SEAL -> handleArchivistSeal(player, gameWorld, ability, payload);
                    case BrinAbilityC2SPacket.ABILITY_GAMBLER_BET -> handleGamblerBet(player, gameWorld, ability, payload);
                    case BrinAbilityC2SPacket.ABILITY_EAVESDROPPER_CHANNEL -> handleEavesdropperChannel(player, gameWorld, ability, payload);
                    case BrinAbilityC2SPacket.ABILITY_SNIPER_TOGGLE_OR_FIRE -> handleSniperAbility(player, gameWorld);
                    case BrinAbilityC2SPacket.ABILITY_SNIPER_CANCEL -> handleSniperCancel(player, gameWorld);
                    case BrinAbilityC2SPacket.ABILITY_WATCHMAN_RESCUE -> handleWatchmanRescue(player, gameWorld, ability, payload);
                    case BrinAbilityC2SPacket.ABILITY_ZHANGSHI_SPEED -> handleZhangshiSpeed(player, gameWorld, ability);
                    case BrinAbilityC2SPacket.ABILITY_MORTICIAN_DISGUISE -> handleMorticianDisguise(player, gameWorld, ability, payload);
                    case BrinAbilityC2SPacket.ABILITY_COWBOY_DUEL -> handleCowboyDuel(player, gameWorld, payload);
                    case BrinAbilityC2SPacket.ABILITY_STALKER_TRACK -> handleStalkerTrack(player, gameWorld, payload);
                    case BrinAbilityC2SPacket.ABILITY_CONDUCTOR_ARMOR -> handleConductorArmor(player, gameWorld);
                    case BrinAbilityC2SPacket.ABILITY_NOISEMAKER_MARK -> handleNoisemakerMark(player, gameWorld, payload);
                    case BrinAbilityC2SPacket.ABILITY_INSANE_REVIVE -> handleInsaneRevive(player, gameWorld, payload);
                    case BrinAbilityC2SPacket.ABILITY_MORPHLING_MORPH -> handleMorphlingMorph(player, gameWorld, payload);
                    case BrinAbilityC2SPacket.ABILITY_VIGILANTE_DETECT -> handleVigilanteDetect(player, gameWorld, ability, payload);
                }
			}
		);
		ServerPlayNetworking.registerGlobalReceiver(
			RpsActionC2SPacket.TYPE,
			(payload, context) -> {
				ServerPlayer player = context.player();
				player.resetLastActionTime();
				RpsManager.handle(player, payload);
			}
		);
		ServerPlayNetworking.registerGlobalReceiver(
			IllusionistControlC2SPacket.TYPE,
			(payload, context) -> {
				ServerPlayer player = context.player();
				if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
				GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
				if (!gameWorld.isRole(player, BrinRoles.ILLUSIONIST)) return;
				IllusionistComponent component = IllusionistComponent.KEY.get(player);
				if (component != null) {
					component.applyControlInput(
						payload.cloneId(),
						payload.forward(),
						payload.strafe(),
						payload.yaw(),
						payload.pitch()
					);
				}
			}
		);
		ServerPlayNetworking.registerGlobalReceiver(
			BrinInstinctC2SPacket.TYPE,
			(payload, context) -> BrinInstinctSnapshots.setEnabled(context.player().getUUID(), payload.enabled())
		);
	}
	private void handlePuppeteerCraft(ServerPlayer player, GameWorldComponent gameWorld,
	                                  BrinAbilityC2SPacket payload) {
		if (!gameWorld.isRole(player, BrinRoles.PUPPETEER)) return;
		if (payload.targetId() == null) return;
		PuppeteerControlComponent comp = PuppeteerControlComponent.KEY.get(player);
		if (comp == null || comp.isControlling() || comp.craftCooldownTicks > 0) return;
		Entity targetEntity = player.serverLevel().getEntity(payload.targetId());
		if (!(targetEntity instanceof PlayerBodyEntity body)) return;
		if (PuppeteerControlComponent.isPuppetModel(body)
			|| IllusionistComponent.isIllusionModel(body)
			|| MorticianComponent.isDisguiseBody(body)) return;
		if (player.distanceToSqr(body) > 64.0D) return;
		if (!spendAbilityCost(player, "puppeteer")) return;
		if (!comp.craftPuppet(body)) return;
		player.playNotifySound(SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.PLAYERS, 1.0f, 0.8f);
	}
	private void handlePuppeteerSummon(ServerPlayer player, GameWorldComponent gameWorld,
	                                   AbilityPlayerComponent ability,
	                                   BrinAbilityC2SPacket payload) {
		if (!gameWorld.isRole(player, BrinRoles.PUPPETEER)) return;
		if (ability.cooldown > 0) return;
		if (payload.targetId() == null) return;

		PuppeteerControlComponent comp = PuppeteerControlComponent.KEY.get(player);
		if (comp == null || !comp.summonPuppet(player, payload.targetId())) return;
		player.playNotifySound(SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 0.5f);
	}
	private void handlePuppeteerReturn(ServerPlayer player, GameWorldComponent gameWorld) {
		if (!gameWorld.isRole(player, BrinRoles.PUPPETEER)) return;
		PuppeteerControlComponent comp = PuppeteerControlComponent.KEY.get(player);
		if (comp == null) return;
		comp.returnToBody(player);
	}
	private void handleMorticianDisguise(ServerPlayer player, GameWorldComponent gameWorld,
	                                     AbilityPlayerComponent ability,
	                                     BrinAbilityC2SPacket payload) {
		if (!gameWorld.isRole(player, BrinRoles.MORTICIAN)) return;
		if (ability.cooldown > 0) return;
		if (payload.targetId() == null) return;

		MorticianComponent comp = MorticianComponent.KEY.get(player);
		if (comp == null || comp.isDisguised()) return;
		if (!player.onGround()) return;

		ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
		if (target == null) return;
		if (!spendAbilityCost(player, "mortician")) return;
		comp.startDisguise(player, target.getUUID());
	}
    private void handleZhangshiSpeed(ServerPlayer player, GameWorldComponent gameWorld,
                                      AbilityPlayerComponent ability) {
        if (!gameWorld.isRole(player, BrinRoles.ZHANGSHI)) return;
        if (ability.cooldown > 0) return;
        ZhangshiComponent component = ZhangshiComponent.KEY.get(player);
        if (component == null || !component.activateNormal()) return;
        ability.setAbilityCooldown(BrinConfig.skillCooldownSeconds("zhangshi"));
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
	private void handleStuntDoubleMimic(ServerPlayer player, GameWorldComponent gameWorld,
	                                    AbilityPlayerComponent ability,
	                                    BrinAbilityC2SPacket payload) {
		if (!gameWorld.isRole(player, BrinRoles.STUNT_DOUBLE)) return;
		if (ability.cooldown > 0) return;
		if (payload.targetId() == null) return;

		ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
		if (target == null || !target.isAlive()) return;
		if (player.equals(target)) return;
		if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
		StuntDoubleComponent comp = StuntDoubleComponent.KEY.get(player);
		if (comp == null) return;
		if (!spendAbilityCost(player, "stunt_double")) return;
		comp.startMimic(target.getUUID());
		ability.setAbilityCooldown(BrinConfig.skillCooldownSeconds("stunt_double"));

		player.level().playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0f, 1.0f);
	}
	private void handleMediumJoinVoice(ServerPlayer player, GameWorldComponent gameWorld,
	                                   AbilityPlayerComponent ability) {
		if (!gameWorld.isRole(player, BrinRoles.MEDIUM)) return;
		if (ability.cooldown > 0) return;
		MediumComponent medium = MediumComponent.KEY.get(player);
		if (medium == null) return;
		EavesdropperComponent eavesdropper = EavesdropperComponent.KEY.get(player);
		if (eavesdropper != null && eavesdropper.isInTemporaryChannel()) return;

		if (!medium.startChannelling(BrinConfig.skillDurationSeconds("medium"))) return;
		if (!spendAbilityCost(player, "medium")) {
			medium.reset();
			return;
		}
		ability.setAbilityCooldown(BrinConfig.skillCooldownSeconds("medium"));
		player.playNotifySound(SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
	}
	private void handleStalkerTrack(ServerPlayer player, GameWorldComponent gameWorld,
	                                BrinAbilityC2SPacket payload) {
		if (!gameWorld.isRole(player, BrinRoles.STALKER)) return;
		if (payload.targetId() == null) return;
		ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
		if (target == null || target.level() != player.level()) return;
		StalkerComponent.tryTrack(player, target);
	}
	private void handleCowboyDuel(ServerPlayer player, GameWorldComponent gameWorld,
	                              BrinAbilityC2SPacket payload) {
		if (!gameWorld.isRole(player, BrinRoles.COWBOY)) return;
		if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
		if (payload.targetId() == null) return;
		ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
		if (target == null || target.equals(player) || target.level() != player.level()) return;
		if (!GameFunctions.isPlayerAliveAndSurvival(target)) {
			player.displayClientMessage(
				Component.translatable("message.brinswathe.cowboy.target_dead")
					.withStyle(net.minecraft.ChatFormatting.RED),
				true
			);
			return;
		}
		CowboyComponent cowboy = CowboyComponent.KEY.get(player);
		if (cowboy == null) return;
		if (cowboy.duelUsed()) {
			player.displayClientMessage(
				Component.translatable("message.brinswathe.cowboy.already_used")
					.withStyle(net.minecraft.ChatFormatting.RED),
				true
			);
			return;
		}
		if (CowboyDuel.isActive()) return;
		net.minecraft.core.BlockPos posA = BrinConfig.cowboyArenaA();
		net.minecraft.core.BlockPos posB = BrinConfig.cowboyArenaB();
		net.minecraft.core.BlockPos posSpectator = BrinConfig.cowboyArenaSpectator();
		if (posA == null || posB == null || posSpectator == null) {
			player.displayClientMessage(
				Component.translatable("message.brinswathe.cowboy.arena_unset")
					.withStyle(net.minecraft.ChatFormatting.RED),
				false
			);
			return;
		}
		if (!spendAbilityCost(player, "cowboy")) return;
		cowboy.markDuelUsed();
		CowboyDuel.start(player, target, posA, posB, posSpectator);
	}
	private void handleEavesdropperChannel(ServerPlayer player, GameWorldComponent gameWorld,
	                                       AbilityPlayerComponent ability,
	                                       BrinAbilityC2SPacket payload) {
		if (!gameWorld.isRole(player, BrinRoles.EAVESDROPPER)) return;
		EavesdropperComponent ownerChannel = EavesdropperComponent.KEY.get(player);
		if (ownerChannel != null && ownerChannel.isInTemporaryChannel()) return;
		if (ability.cooldown > 0 || payload.targetId() == null) return;
		ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
		if (target == null || target.equals(player) || target.level() != player.level()) return;
		if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
		if (gameWorld.isRole(target, BrinRoles.MEDIUM)) return;
		if (!BrinVoiceChatPlugin.canStartTemporaryChannel(player, target)) return;
		if (!spendAbilityCost(player, "eavesdropper")) return;
		int durationSeconds = Math.min(
			BrinConfig.skillDurationSeconds("eavesdropper"),
			Integer.MAX_VALUE / 20
		);
		if (!BrinVoiceChatPlugin.startTemporaryChannel(player, target, durationSeconds * 20)) return;
		ability.setAbilityCooldown(BrinConfig.skillCooldownSeconds("eavesdropper"));
		player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, 1.0F);
	}
	private void handleTrapperPlaceTrap(ServerPlayer player, GameWorldComponent gameWorld,
	                                   AbilityPlayerComponent ability) {
		if (!gameWorld.isRole(player, BrinRoles.TRAPPER)) return;
		if (ability.cooldown > 0) return;
		TrapperComponent comp = TrapperComponent.KEY.get(player);
		if (comp == null) return;
		if (!comp.canPlaceTrap()) return;
        if (!spendAbilityCost(player, "beast_trapper")) return;
		if (!TrapperTraps.place(player, false)) return;
        ability.setAbilityCooldown(BrinConfig.skillCooldownSeconds("beast_trapper"));
	}
	private void handleWatchmanRescue(ServerPlayer player, GameWorldComponent gameWorld,
	                                  AbilityPlayerComponent ability,
	                                  BrinAbilityC2SPacket payload) {
		if (!gameWorld.isRole(player, BrinRoles.WATCHMAN)) return;
		if (ability.cooldown > 0 || payload.targetId() == null) return;
		ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
		if (target == null || target.level() != player.level()) return;
		if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
		TrapperComponent trappedBy = null;
		for (ServerPlayer possibleTrapper : player.server.getPlayerList().getPlayers()) {
			if (possibleTrapper.level() != player.level()) continue;
			TrapperComponent trapper = TrapperComponent.KEY.get(possibleTrapper);
			if (trapper != null && trapper.isTrapped(target.getUUID())) {
				trappedBy = trapper;
				break;
			}
		}
		BombComponent bomb = BombComponent.KEY.get(target);
		boolean hasBombToDefuse = bomb != null && bomb.canBeDefused();
		PlayerEffectComponent stun = PlayerEffectComponent.KEY.get(target);
		boolean leftoverTrapStun = trappedBy == null
			&& stun != null
			&& stun.stunTicks > 20 * 60;
		if (trappedBy == null && !hasBombToDefuse && !leftoverTrapStun) return;
		if (!spendAbilityCost(player, "watchman")) return;
		boolean rescued = trappedBy != null && trappedBy.rescuePlayer(target.getUUID());
		if (hasBombToDefuse) bomb.reset();
		if (leftoverTrapStun) BomberBombs.releaseStun(target);
		if (!rescued && !hasBombToDefuse && !leftoverTrapStun) return;
		ability.setAbilityCooldown(BrinConfig.skillCooldownSeconds("watchman"));
		player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.25F);
	}
	private void handleNightmarePlant(ServerPlayer player, GameWorldComponent gameWorld,
	                                  AbilityPlayerComponent ability,
	                                  BrinAbilityC2SPacket payload) {
		if (!gameWorld.isRole(player, BrinRoles.NIGHTMARE)) return;
		if (ability.cooldown > 0) return;
		if (payload.targetId() == null) return;

		ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
		if (target == null || !target.isAlive()) return;
		if (player.equals(target)) return;
		if (!target.isSleeping()) return;
		if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
		NightmareComponent comp = NightmareComponent.KEY.get(player);
		if (comp == null) return;
		if (comp.isMarked(target.getUUID())) return;
		if (!spendAbilityCost(player, "nightmare")) return;
		comp.markPlayer(target.getUUID());
		comp.addShieldLayer();
		PlayerMoodComponent moodComponent = PlayerMoodComponent.KEY.get(target);
		moodComponent.setMood(0.19F);
		ability.setAbilityCooldown(BrinConfig.skillCooldownSeconds("nightmare"));
		player.playNotifySound(SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 1.0f, 0.5f);
		player.displayClientMessage(
			Component.literal("已播种 " + target.getName().getString() + "  护盾 " + comp.getShieldLayers() + "/" + NightmareComponent.MAX_SHIELD_LAYERS),
			true);
	}
	private void handleIllusionistClones(ServerPlayer player, GameWorldComponent gameWorld,
	                                     AbilityPlayerComponent ability) {
		if (!gameWorld.isRole(player, BrinRoles.ILLUSIONIST)) return;
		if (ability.cooldown > 0) return;
		IllusionistComponent comp = IllusionistComponent.KEY.get(player);
		if (comp == null) return;
		if (!spendAbilityCost(player, "illusionist")) return;
		comp.spawnClones();
		ability.setAbilityCooldown(BrinConfig.skillCooldownSeconds("illusionist"));
		player.level().playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0f, 1.0f);
	}
	private void handleIllusionistSwitchControl(ServerPlayer player, GameWorldComponent gameWorld,
	                                            BrinAbilityC2SPacket payload) {
		if (!gameWorld.isRole(player, BrinRoles.ILLUSIONIST)) return;
		IllusionistComponent component = IllusionistComponent.KEY.get(player);
		if (component == null || component.cloneEntityIds.isEmpty()) return;
		component.setControlledClone(payload.targetId());
	}
    private void handleSniperAbility(ServerPlayer player, GameWorldComponent gameWorld) {
        if (!gameWorld.isRole(player, BrinRoles.SNIPER)) return;
        if (GameSafeComponent.KEY.get(player.level()).isGameSafe) return;
        SniperComponent component = SniperComponent.KEY.get(player);
        if (component == null) return;

        if (component.isAiming()) {
            component.startShotCooldown(BrinConfig.skillCooldownSeconds("sniper"));
            component.fire(player.getEyePosition(), player.getLookAngle());
            player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.CROSSBOW_SHOOT,
                SoundSource.PLAYERS,
                2.0F,
                0.55F
            );
            return;
        }
        if (component.getCooldownTicks() > 0 || component.isAbilityActive()) return;
        if (!spendAbilityCost(player, "sniper")) return;
        component.startAiming();
        player.playNotifySound(SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    private void handleSniperCancel(ServerPlayer player, GameWorldComponent gameWorld) {
        if (!gameWorld.isRole(player, BrinRoles.SNIPER)) return;
        SniperComponent component = SniperComponent.KEY.get(player);
        if (component == null || !component.isAiming()) return;
        component.cancelAiming();
        player.playNotifySound(SoundEvents.SPYGLASS_STOP_USING, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    private void handleArchivistSeal(ServerPlayer player, GameWorldComponent gameWorld,
                                     AbilityPlayerComponent ability,
                                     BrinAbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, BrinRoles.ARCHIVIST)) return;
        if (ability.cooldown > 0) return;
        if (payload.targetId() == null) return;

        Entity targetEntity = player.serverLevel().getEntity(payload.targetId());
        if (!(targetEntity instanceof PlayerBodyEntity body)) return;
        if (player.distanceToSqr(body) > 64.0D) return;
        if (((ArchivistSealedCorpse) body).brin$isArchivistSealed()) return;
        if (!spendAbilityCost(player, "archivist")) return;
        ((ArchivistSealedCorpse) body).brin$setArchivistSealed(true);
        ability.setAbilityCooldown(BrinConfig.skillCooldownSeconds("archivist"));
        ArchivistComponent comp = ArchivistComponent.KEY.get(player);
        if (comp != null) {
            comp.sealedCorpses.add(body.getUUID());
            comp.sync();
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.5f);
    }
    private void handleGamblerBet(ServerPlayer player, GameWorldComponent gameWorld,
                                  AbilityPlayerComponent ability,
                                  BrinAbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, BrinRoles.GAMBLER)) return;
        if (ability.cooldown > 0) return;
        if (payload.targetId() == null) return;

        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null || !target.isAlive()) return;
        if (player.equals(target)) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
        GamblerComponent comp = GamblerComponent.KEY.get(player);
        if (comp == null) return;
        if (comp.poisoned) return;
        if (!spendAbilityCost(player, "gambler")) return;
        comp.setBetTarget(target.getUUID());
        ability.setAbilityCooldown(BrinConfig.skillCooldownSeconds("gambler"));
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
    private static void brinPayTrapperCatchReward(ServerPlayer trapper) {
        int reward = BrinConfig.trapperCatchReward();
        if (reward <= 0) return;
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(trapper);
        if (shop == null) return;
        shop.balance += reward;
        shop.sync();
        trapper.displayClientMessage(
            Component.translatable("message.brinswathe.trapper.catch_reward", reward),
            true
        );
    }
    private static void handleMuzzlerSilence(ServerPlayer player) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (!gameWorld.isRole(player, StarryExpressRoles.MUZZLER)) return;
        AbilityComponent ability = AbilityComponent.KEY.get(player);
        if (ability == null || ability.cooldown > 0) return;
        MuzzlerAbilityComponent muzzler = MuzzlerAbilityComponent.KEY.get(player);
        if (muzzler == null || muzzler.getTicks() > 0) return;
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
        if (shop == null || shop.balance < MuzzlerAbilityComponent.ABILITY_COST) return;
        shop.balance -= MuzzlerAbilityComponent.ABILITY_COST;
        shop.sync();
        ability.setCooldown(MuzzlerAbilityComponent.ABILITY_COOLDOWN_TICKS);
        muzzler.setTicks(MuzzlerAbilityComponent.ABILITY_DURATION_TICKS);
    }
    private static void handleVigilanteDetect(ServerPlayer player, GameWorldComponent gameWorld,
                                              AbilityPlayerComponent ability, BrinAbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, WatheRoles.VIGILANTE)) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
        if (ability == null || ability.cooldown > 0 || payload.targetId() == null) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null || target == player || !GameFunctions.isPlayerAliveAndSurvival(target)) return;
        if (target.distanceTo(player) > VIGILANTE_DETECT_RANGE) return;
        Role targetRole = gameWorld.getRole(target);
        if (targetRole == null) return;
        if (!spendAbilityCost(player, "vigilante")) return;
        if (targetRole.isInnocent()) {
            player.displayClientMessage(
                Component.translatable("tip.kinswathe.detective.innocent", target.getName().getString())
                    .withColor(WatheRoles.CIVILIAN.color()),
                true
            );
            player.playNotifySound(SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            player.displayClientMessage(
                Component.translatable("tip.kinswathe.detective.notinnocent", target.getName().getString())
                    .withColor(WatheRoles.KILLER.color()),
                true
            );
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        ability.setAbilityCooldown(BrinConfig.skillCooldownSeconds("vigilante"));
    }
    private static void handleConductorArmor(ServerPlayer player, GameWorldComponent gameWorld) {
        if (!BrinNoelleAccess.isRole(gameWorld, player, BrinNoelleAccess.CONDUCTOR_ID)) return;
        if (BrinNoelleAccess.noelleAbilityCooldown(player) > 0) return;
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
        if (shop == null || shop.balance < BrinNoelleAccess.CONDUCTOR_ARMOR_COST) return;
        if (!BrinNoelleAccess.addBartenderArmor(player)) return;
        shop.balance -= BrinNoelleAccess.CONDUCTOR_ARMOR_COST;
        shop.sync();
        BrinNoelleAccess.setNoelleAbilityCooldown(player, BrinNoelleAccess.CONDUCTOR_ARMOR_COOLDOWN_TICKS);
    }
    private static void handleMorphlingMorph(ServerPlayer player, GameWorldComponent gameWorld, BrinAbilityC2SPacket payload) {
        if (!BrinNoelleAccess.isRole(gameWorld, player, BrinNoelleAccess.MORPHLING_ID)) return;
        if (payload.targetId() == null) return;
        if (BrinMorphlingAccess.morphTicks(player) != 0) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target != null && GameFunctions.isPlayerAliveAndSurvival(target)) {
            player.displayClientMessage(Component.literal("你要变身的对象还没死哦"), false);
            return;
        }
        var role = gameWorld.getRole(payload.targetId());
        if (role == null || !role.canUseKiller()) return;
        BrinMorphlingAccess.startMorph(player, payload.targetId());
    }
    private static void handleNoisemakerMark(ServerPlayer player, GameWorldComponent gameWorld, BrinAbilityC2SPacket payload) {
        if (!BrinNoelleAccess.isRole(gameWorld, player, BrinNoelleAccess.NOISEMAKER_ID)) return;
        if (payload.targetId() == null) return;
        if (BrinNoelleAccess.noelleAbilityCooldown(player) > 0) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null || target.equals(player) || target.level() != player.level()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
        if (!BrinNoelleAccess.setVoodooTarget(player, target.getUUID())) return;
        BrinNoelleAccess.setNoelleAbilityCooldown(player, 30 * 20);
    }

    private static void handleInsaneRevive(ServerPlayer player, GameWorldComponent gameWorld, BrinAbilityC2SPacket payload) {
        if (!BrinNoelleAccess.isRole(gameWorld, player, BrinNoelleAccess.INSANE_KILLER_ID)) return;
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0, false, false));
        if (payload.targetId() == null) return;
        if (BrinNoelleAccess.noelleAbilityCooldown(player) > 0) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null) return;
        if (!target.isSpectator()) {
            player.displayClientMessage(Component.literal("他还没有死"), true);
            return;
        }
        ResurrectedComponent resurrected = ResurrectedComponent.KEY.get(target);
        if (resurrected == null || !resurrected.resurrect(player.getUUID())) return;
        BrinNoelleAccess.setNoelleAbilityCooldown(player, 120 * 20);
    }
    private static boolean spendAbilityCost(ServerPlayer player, String roleId) {
        int cost = BrinConfig.skillCost(roleId);
        if (cost == 0) return true;
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
        if (shop == null || shop.balance < cost) return false;
        shop.balance -= cost;
        shop.sync();
        return true;
    }
    private static void checkGamblerWin(MinecraftServer server, GameWorldComponent gameWorld,
                                        Set<UUID> processedGamblers) {
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            if (!gameWorld.isRole(sp, BrinRoles.GAMBLER)) continue;
            if (!processedGamblers.add(sp.getUUID())) continue;
            GamblerComponent comp = GamblerComponent.KEY.get(sp);
            if (comp == null) continue;
            if (comp.poisoned) continue;

            if (comp.betTarget != null) {
                ServerPlayer target = server.getPlayerList().getPlayer(comp.betTarget);
                if (target == null || !GameFunctions.isPlayerAliveAndSurvival(target)) {
                    comp.onBetTargetDied();
                    PlayerPoisonComponent poison = PlayerPoisonComponent.KEY.get(sp);
                    int poisonTicks = poison.poisonTicks > 0
                        ? Math.max(1, poison.poisonTicks
                            - sp.getRandom().nextIntBetweenInclusive(100, 300))
                        : sp.getRandom().nextIntBetweenInclusive(
                            PlayerPoisonComponent.clampTime.getA(),
                            PlayerPoisonComponent.clampTime.getB()
                        );
                    poison.setPoisonTicks(poisonTicks, sp.getUUID());
                    continue;
		}
	}
            if (comp.betTarget == null) continue;
            if (comp.betTicks > 0) {
                comp.betTicks--;
                if (comp.betTicks <= 0) {
                    endWithCustomWinner(sp, "gambler", BrinRoles.GAMBLER.color());
                    return;
                }
            }
        }
    }
	private void handleNightmareForceSleep(ServerPlayer player, GameWorldComponent gameWorld,
	                                       BrinAbilityC2SPacket payload) {
		if (!gameWorld.isRole(player, BrinRoles.NIGHTMARE)) return;
		if (payload.targetId() == null) return;
		NightmareComponent nightmare = NightmareComponent.KEY.get(player);
		if (nightmare == null || nightmare.forcedSleepTaskCooldown > 0) return;
		ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
		if (target == null || target.equals(player) || target.level() != player.level()) return;
		if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
		PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(target);
		if (mood == null) return;
		mood.tasks.clear();
		mood.tasks.put(
			PlayerMoodComponent.Task.SLEEP,
			new PlayerMoodComponent.SleepTask(GameConstants.SLEEP_TASK_DURATION)
		);
		mood.sync();
		nightmare.startForcedSleepTaskCooldown();
	}

    public static boolean checkPenitentWin(MinecraftServer server, GameWorldComponent gameWorld) {
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            if (!gameWorld.isRole(sp, BrinRoles.PENITENT)) continue;
            PenitentComponent comp = PenitentComponent.KEY.get(sp);
            if (comp == null) continue;
            if (comp.hasCompletedTargets()) {
                endWithCustomWinner(sp, "penitent", BrinRoles.PENITENT.color());
                return true;
            }
        }
        return false;
    }

    private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			BrinConfigCommand.register(dispatcher);
			SetBrinSpeedCommand.register(dispatcher);
			SetRoleCountCommand.register(dispatcher);
			ForceRefreshRoleCommand.register(dispatcher);
			RoleModifierBlacklistCommand.register(dispatcher);
			BrinIcCommands.register(dispatcher);
		});
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
