package cn.erindax.brinswathe;

import cn.erindax.brinswathe.admin.AdminPanel;
import cn.erindax.brinswathe.command.ConfigCommands;
import cn.erindax.brinswathe.component.BerserkerComponent;
import cn.erindax.brinswathe.component.BoneharvesterComponent;
import cn.erindax.brinswathe.component.ModCustomWinnerComponent;
import cn.erindax.brinswathe.component.RoundRecapComponent;
import cn.erindax.brinswathe.component.ConspiratorComponent;
import cn.erindax.brinswathe.component.IllusionistComponent;
import cn.erindax.brinswathe.component.MuzzlerAbilityComponent;
import cn.erindax.brinswathe.component.NightmareComponent;
import cn.erindax.brinswathe.component.PenitentComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import cn.erindax.brinswathe.component.StaminaComponent;
import cn.erindax.brinswathe.component.TrapperComponent;
import cn.erindax.brinswathe.component.ValentinaComponent;
import cn.erindax.brinswathe.component.ZhangshiComponent;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.config.Tunable;
import cn.erindax.brinswathe.musicbox.MusicBox;
import cn.erindax.brinswathe.network.ArchivistWitnessS2CPacket;
import cn.erindax.brinswathe.network.BlindFlashS2CPacket;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import cn.erindax.brinswathe.network.AdminActionC2SPacket;
import cn.erindax.brinswathe.network.AdminSaveC2SPacket;
import cn.erindax.brinswathe.network.AdminSnapshotS2CPacket;
import cn.erindax.brinswathe.network.BuySlotC2SPacket;
import cn.erindax.brinswathe.network.ConfigS2CPacket;
import cn.erindax.brinswathe.network.DraftChoiceC2SPacket;
import cn.erindax.brinswathe.network.DraftCloseS2CPacket;
import cn.erindax.brinswathe.network.DraftStateS2CPacket;
import cn.erindax.brinswathe.network.SecretCohortS2CPacket;
import cn.erindax.brinswathe.network.WelcomeS2CPacket;
import cn.erindax.brinswathe.network.IcNightVisionS2CPacket;
import cn.erindax.brinswathe.network.InstinctC2SPacket;
import cn.erindax.brinswathe.network.InstinctSnapshotS2CPacket;
import cn.erindax.brinswathe.network.ItemSlotsC2SPacket;
import cn.erindax.brinswathe.network.KnifeSkinListS2CPacket;
import cn.erindax.brinswathe.network.MusicBoxRequestC2SPacket;
import cn.erindax.brinswathe.network.MusicBoxStatusS2CPacket;
import cn.erindax.brinswathe.network.MusicChunkS2CPacket;
import cn.erindax.brinswathe.network.MusicFetchC2SPacket;
import cn.erindax.brinswathe.network.MusicPlayS2CPacket;
import cn.erindax.brinswathe.network.MusicUploadAckS2CPacket;
import cn.erindax.brinswathe.network.MusicUploadChunkC2SPacket;
import cn.erindax.brinswathe.network.MusicUploadStartC2SPacket;
import cn.erindax.brinswathe.network.ResourceReloadS2CPacket;
import cn.erindax.brinswathe.network.SkinPickChoiceC2SPacket;
import cn.erindax.brinswathe.network.SkinPickOpenS2CPacket;
import cn.erindax.brinswathe.network.SkinPickResultS2CPacket;
import cn.erindax.brinswathe.network.SkinSoundS2CPacket;
import cn.erindax.brinswathe.network.SkinUploadC2SPacket;
import cn.erindax.brinswathe.network.SkinUploadPromptS2CPacket;
import cn.erindax.brinswathe.network.SpyGuessC2SPacket;
import cn.erindax.brinswathe.network.CowboyShowdownMusicS2CPacket;
import cn.erindax.brinswathe.network.CowboyDuelHideS2CPacket;
import cn.erindax.brinswathe.network.CowboyDuelIdentityS2CPacket;
import cn.erindax.brinswathe.network.CowboyDuelLookLockS2CPacket;
import cn.erindax.brinswathe.network.IllusionistControlC2SPacket;
import cn.erindax.brinswathe.network.RpsActionC2SPacket;
import cn.erindax.brinswathe.network.RpsStateS2CPacket;
import cn.erindax.brinswathe.voice.ModVoiceChatPlugin;

import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.agmas.harpymodloader.events.ResetPlayerEvent;
import org.aussiebox.starexpress.StarryExpress;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.item.StarryExpressItems;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BrinsWathe implements ModInitializer {
	public static final String MOD_ID = "brinswathe";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final Map<UUID, Integer> LOW_MOOD_DEATH_TIMERS = new HashMap<>();

	@Override
	public void onInitialize() {
		LOGGER.info("Brin's Wathe initializing");

		ModConfig.initialize();
		IcFlags.load();
		KnifeSkins.load();
		KnifeSkins.scanAndRegister();
		StaminaComponent.loadPersistedOverrides();
		ModItems.init();
		ModEntities.init();
		ModSounds.init();
		ModRoles.init();
		SecretMurder.init();
		IcModifiers.register();

		applyMuzzlerConfig();

		PayloadTypeRegistry.playC2S().register(AbilityC2SPacket.TYPE, AbilityC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(
			IllusionistControlC2SPacket.TYPE,
			IllusionistControlC2SPacket.STREAM_CODEC
		);
		PayloadTypeRegistry.playS2C().register(BlindFlashS2CPacket.TYPE, BlindFlashS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(ArchivistWitnessS2CPacket.TYPE, ArchivistWitnessS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(CowboyShowdownMusicS2CPacket.TYPE, CowboyShowdownMusicS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(CowboyDuelHideS2CPacket.TYPE, CowboyDuelHideS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(CowboyDuelIdentityS2CPacket.TYPE, CowboyDuelIdentityS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(CowboyDuelLookLockS2CPacket.TYPE, CowboyDuelLookLockS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(ConfigS2CPacket.TYPE, ConfigS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(ResourceReloadS2CPacket.TYPE, ResourceReloadS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(RpsActionC2SPacket.TYPE, RpsActionC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(RpsStateS2CPacket.TYPE, RpsStateS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(InstinctC2SPacket.TYPE, InstinctC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(InstinctSnapshotS2CPacket.TYPE, InstinctSnapshotS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(KnifeSkinListS2CPacket.TYPE, KnifeSkinListS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(SkinUploadC2SPacket.TYPE, SkinUploadC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(SkinUploadPromptS2CPacket.TYPE, SkinUploadPromptS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(SkinSoundS2CPacket.TYPE, SkinSoundS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(IcNightVisionS2CPacket.TYPE, IcNightVisionS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(MusicBoxRequestC2SPacket.TYPE, MusicBoxRequestC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(MusicUploadStartC2SPacket.TYPE, MusicUploadStartC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(MusicUploadChunkC2SPacket.TYPE, MusicUploadChunkC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(MusicFetchC2SPacket.TYPE, MusicFetchC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(MusicBoxStatusS2CPacket.TYPE, MusicBoxStatusS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(MusicUploadAckS2CPacket.TYPE, MusicUploadAckS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(MusicPlayS2CPacket.TYPE, MusicPlayS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(MusicChunkS2CPacket.TYPE, MusicChunkS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(SkinPickOpenS2CPacket.TYPE, SkinPickOpenS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(SkinPickChoiceC2SPacket.TYPE, SkinPickChoiceC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(SkinPickResultS2CPacket.TYPE, SkinPickResultS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(DraftStateS2CPacket.TYPE, DraftStateS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(SecretCohortS2CPacket.TYPE, SecretCohortS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(WelcomeS2CPacket.TYPE, WelcomeS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(DraftChoiceC2SPacket.TYPE, DraftChoiceC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(DraftCloseS2CPacket.TYPE, DraftCloseS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(BuySlotC2SPacket.TYPE, BuySlotC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(ItemSlotsC2SPacket.TYPE, ItemSlotsC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(AdminSnapshotS2CPacket.TYPE, AdminSnapshotS2CPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(AdminSaveC2SPacket.TYPE, AdminSaveC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(AdminActionC2SPacket.TYPE, AdminActionC2SPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(SpyGuessC2SPacket.TYPE, SpyGuessC2SPacket.STREAM_CODEC);
		registerPackets();
		MusicBox.init();
		SkinPicks.init();
		Draft.init();
		Punishment.init();
		BuySlots.init();
		AdminPanel.init();
		ArchivistWitness.init();
		Funeral.init();
		Disarm.init();
		Zhuimu.init();
		registerConfigSync();
		registerCommands();
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			CowboyDuel.clear();
			RpsManager.clear();
			RoleRotation.clear();
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server ->
			RoleWeights.apply(server, IcFlags.roleWeights)
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
			ArrayList<ServerPlayer> recapPlayers = new ArrayList<>();
			for (var player : world.players()) {
				if (!(player instanceof ServerPlayer serverPlayer)) continue;
				recapPlayers.add(serverPlayer);
				String roleId = ModRoles.getRoleId(gameWorld, serverPlayer);
				if (roleId != null) {
					Integer balance = ModConfig.initialBalance(roleId);
					if (balance != null) {
						PlayerShopComponent.KEY.get(serverPlayer).setBalance(balance);
					}
				}
				if (gameWorld.isRole(serverPlayer, StarryExpressRoles.MUZZLER)) {
					ShopEntry.insertStackInFreeSlot(serverPlayer, StarryExpressItems.TAPE.getDefaultInstance());
				}
				if (NoelleAccess.isRole(gameWorld, serverPlayer, NoelleAccess.JESTER_ID)) {
					ShopEntry.insertStackInFreeSlot(serverPlayer, WatheItems.CROWBAR.getDefaultInstance());
				}
				if (NoelleAccess.isRole(gameWorld, serverPlayer, NoelleAccess.CONDUCTOR_ID)) {
					ShopEntry.insertStackInFreeSlot(serverPlayer, WatheItems.LOCKPICK.getDefaultInstance());
				}
				if (ShopAccess.isThief(gameWorld, serverPlayer) || ShopAccess.isArsonist(gameWorld, serverPlayer)) {
					ShopEntry.insertStackInFreeSlot(serverPlayer, WatheItems.LOCKPICK.getDefaultInstance());
				}
			}
			GamblerSettlement.apply(gameWorld, recapPlayers);
			IcRoundStart.apply(world, gameWorld);
			Conspirator.announce(world, gameWorld);
			RoundRecapComponent recap = RoundRecapComponent.KEY.get(world);
			if (recap != null) recap.snapshotIdentities(recapPlayers);
			GuesserSuspension.begin();
			Draft.roundInitialized();
		});
		GameEvents.ON_GAME_START.register(gameMode -> ModItemCooldowns.schedule());
		GameEvents.ON_GAME_START.register(gameMode -> GuesserSuspension.reset());
		ResetPlayerEvent.EVENT.register(player -> {
			MuzzlerAbilityComponent component = MuzzlerAbilityComponent.KEY.get(player);
			if (component != null) component.reset();
			IcRoundStart.resetPlayer(player);
			ModVoiceChatPlugin.resetPlayer(player.getUUID());
		});

		ServerTickEvents.START_SERVER_TICK.register(BrinsWathe::onServerTick);
		ServerTickEvents.END_SERVER_TICK.register(RpsManager::holdSelecting);
	}

	private void registerConfigSync() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayNetworking.send(handler.player, new ConfigS2CPacket(ModConfig.toJson()));
			IcNightVisionS2CPacket.sendTo(handler.player);
			StaminaComponent stamina = StaminaComponent.KEY.get(handler.player);
			if (stamina != null) stamina.applyGlobalOverridesForced();
			CowboyDuel.syncJoiningClient(handler.player);
			KnifeSkins.syncTo(handler.player);
			List<String> announcement = ModConfig.announcement();
			if (!announcement.isEmpty()) {
				handler.player.sendSystemMessage(Component.literal(String.join("\n", announcement)));
			}
		});
	}

	private void registerRoleDeathRules() {
	AllowPlayerDeath.EVENT.register((victim, attacker, deathReason) -> {
		refreshJesterPsychoArmour(victim, deathReason);
		if (CowboyDuel.isActive()) return true;
		if (!Executioner.allowDeath(victim, deathReason)) return false;
		if (StuntDoubleDeathTransfer.tryTransfer(victim, attacker, deathReason)) return false;
		GameWorldComponent gameWorld = GameWorldComponent.KEY.get(victim.level());
		boolean shieldPierced = PuppeteerControlComponent.isSelfDestructKill(attacker, deathReason);
			if (gameWorld.isRole(victim, ModRoles.NIGHTMARE) && attacker != null) {
				NightmareComponent nightmare = NightmareComponent.KEY.get(victim);
				if (nightmare != null && !nightmare.isNightmareHour() && nightmare.isMarked(attacker.getUUID())) return false;
			}
			if (gameWorld.isRole(victim, ModRoles.NIGHTMARE)
				&& !shieldPierced
				&& !GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(deathReason)) {
				NightmareComponent nightmare = NightmareComponent.KEY.get(victim);
				if (nightmare != null && nightmare.consumeShield()) {
					victim.level().playSound(null, victim.blockPosition(), SoundEvents.SHIELD_BLOCK,
						SoundSource.PLAYERS, 1.0F, 1.0F);
					RoundRecapComponent.onShieldBroken(victim, attacker, deathReason);
					return false;
				}
			}
			if (gameWorld.isRole(victim, ModRoles.PENITENT)
				&& !shieldPierced
				&& !GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(deathReason)) {
				PenitentComponent penitent = PenitentComponent.KEY.get(victim);
				if (penitent != null && penitent.consumeShield()) {
					victim.level().playSound(null, victim.blockPosition(), SoundEvents.SHIELD_BLOCK,
						SoundSource.PLAYERS, 1.0F, 1.0F);
					RoundRecapComponent.onShieldBroken(victim, attacker, deathReason);
					return false;
				}
			}
			if (gameWorld.isRole(victim, ModRoles.BONEHARVESTER)
				&& !shieldPierced
				&& !GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(deathReason)) {
				BoneharvesterComponent boneharvester = BoneharvesterComponent.KEY.get(victim);
				if (boneharvester != null && boneharvester.consumeShield()) {
					victim.level().playSound(null, victim.blockPosition(), SoundEvents.SHIELD_BLOCK,
						SoundSource.PLAYERS, 1.0F, 1.0F);
					RoundRecapComponent.onShieldBroken(victim, attacker, deathReason);
					return false;
				}
			}
            if (gameWorld.isRole(victim, ModRoles.ZHANGSHI)
                && !shieldPierced
                && !GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(deathReason)
                && !GameConstants.DeathReasons.POISON.equals(deathReason)) {
                ZhangshiComponent zhangshi = ZhangshiComponent.KEY.get(victim);
                if (zhangshi != null && zhangshi.consumeShield()) {
                    victim.level().playSound(null, victim.blockPosition(), SoundEvents.SHIELD_BLOCK,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
					RoundRecapComponent.onShieldBroken(victim, attacker, deathReason);
                    return false;
                }
            }
            if (gameWorld.isRole(victim, ModRoles.VALENTINA)
                && !shieldPierced
                && (GameConstants.DeathReasons.GUN.equals(deathReason)
                    || GameConstants.DeathReasons.KNIFE.equals(deathReason))) {
                ValentinaComponent valentina = ValentinaComponent.KEY.get(victim);
                if (valentina != null && valentina.consumeEye()) {
                    victim.level().playSound(null, victim.blockPosition(), SoundEvents.SHIELD_BLOCK,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                    RoundRecapComponent.onShieldBroken(victim, attacker, deathReason);
                    return false;
                }
            }
			if (gameWorld.isRole(victim, ModRoles.BERSERKER)) {
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
		if (!NoelleAccess.isRole(gameWorld, victim, NoelleAccess.JESTER_ID)) return;
		PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(victim);
		if (psycho == null || psycho.getPsychoTicks() <= GameConstants.getInTicks(0, 44)) return;
		psycho.setArmour(Math.max(0, PsychoArmour.calculate(victim) - 1));
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
			InstinctSnapshots.remove(handler.player.getUUID());
			RpsManager.onDisconnect(handler.player);
			IllusionistComponent component = IllusionistComponent.KEY.get(handler.player);
			if (component != null) component.reset();
			ConspiratorComponent conspirator = ConspiratorComponent.KEY.get(handler.player);
			if (conspirator != null) conspirator.reset();
		});
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			IllusionistComponent component = IllusionistComponent.KEY.get(player);
			if (component != null) component.reset();
		});
	}

	private static void onServerTick(MinecraftServer server) {
		Pushed.beginTick();
		AfkKickManager.tick(server);
		ModVoiceChatPlugin.tick(server);
		KnifeSkins.tick(server);
		ModItemCooldowns.tick(server);
		InstinctSnapshots.tick(server);
        AvengerWitness.tick(server);
        CowboyDuel.tick(server);
        RpsManager.tick(server);
        processDelayedCorpses(server);
        boolean duelActive = CowboyDuel.isActive();
        Set<GameWorldComponent> runningGames = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
            if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) {
				LOW_MOOD_DEATH_TIMERS.remove(player.getUUID());
				continue;
			}
            runningGames.add(gameWorld);
			if (!duelActive) tickLowMoodDeathTimer(player);

            NightmareComponent nightmareComp = NightmareComponent.KEY.get(player);
            if (nightmareComp != null && gameWorld.isRole(player, ModRoles.NIGHTMARE)) {
                nightmareComp.tickForcedSleepTaskCooldown();
            }

            TrapperComponent trapperComp = TrapperComponent.KEY.get(player);
            if (trapperComp != null && gameWorld.isRole(player, ModRoles.TRAPPER)) {
                checkTrapperTrap(player, trapperComp);
            }

            if (gameWorld.isRole(player, ModRoles.WATCHMAN) && server.getTickCount() % 20 == 0) {
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
            checkPenitentWin(server, gameWorld);
        }

    }

	private static void stripSurvivalExpertLeftoverKits(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (ShopAccess.isSurvivalExpertExcludedItem(inventory.getItem(slot))) {
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
		int remainingTicks = LOW_MOOD_DEATH_TIMERS.getOrDefault(playerId, Tunable.LOW_MOOD_DEATH_SECONDS.ticks());
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
			if (gameWorld.isRole(sp, ModRoles.NIGHTMARE)
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
			Component.translatable("message.brinswathe.nightmare.hour").withColor(ModRoles.NIGHTMARE.color()),
			false
		);
		nightmare.displayClientMessage(
			Component.translatable("message.brinswathe.nightmare.hour.overlay").withColor(ModRoles.NIGHTMARE.color()),
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
		endWithCustomWinner(nightmare, "nightmare", ModRoles.NIGHTMARE.color());
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
				endWithCustomWinner(nightmarePlayer, "nightmare", ModRoles.NIGHTMARE.color());
			}
			return;
		}
		if (nightmareComp.markedPlayers.containsAll(alivePlayers)) {
			endWithCustomWinner(nightmarePlayer, "nightmare", ModRoles.NIGHTMARE.color());
		}
	}
	public static boolean checkBerserkerWin(MinecraftServer server, GameWorldComponent gameWorld) {
		ServerPlayer berserkerPlayer = null;
		for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
			if (GameWorldComponent.KEY.get(sp.level()) == gameWorld
				&& gameWorld.isRole(sp, ModRoles.BERSERKER)
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
			endWithCustomWinner(berserkerPlayer, "berserker", ModRoles.BERSERKER.color());
		}
		return true;
	}
	private static void endWithCustomWinner(ServerPlayer winner, String winningTextId, int color) {
		ModCustomWinnerComponent winnerComponent = ModCustomWinnerComponent.KEY.get(winner.level());
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
			boolean wasDead = "nightmare".equals(winningTextId)
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
			AbilityC2SPacket.TYPE,
			(payload, context) -> {
				ServerPlayer player = context.player();
				if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
				if (org.BsXinQin.kinswathe.component.GameSafeComponent.KEY
					.get(player.level()).isGameSafe
					&& payload.abilityType() != AbilityC2SPacket.ABILITY_SNIPER_CANCEL
					&& payload.abilityType() != AbilityC2SPacket.ABILITY_STALKER_TRACK
					&& payload.abilityType() != AbilityC2SPacket.ABILITY_SUBVERT_KILLER
					&& payload.abilityType() != AbilityC2SPacket.ABILITY_SUBVERT_CIVILIAN
					&& payload.abilityType() != AbilityC2SPacket.ABILITY_MUZZLER_SILENCE) return;
				if (CowboyDuel.isActive()
					&& payload.abilityType() != AbilityC2SPacket.ABILITY_SNIPER_CANCEL) return;
				player.resetLastActionTime();
				if (payload.abilityType() == AbilityC2SPacket.ABILITY_MUZZLER_SILENCE) {
					Muzzler.handleSilence(player);
					return;
				}
				GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
				AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);
                switch (payload.abilityType()) {
                    case AbilityC2SPacket.ABILITY_PUPPETEER_CRAFT -> Puppeteer.handleCraft(player, gameWorld, payload);
                    case AbilityC2SPacket.ABILITY_PUPPETEER_SUMMON -> Puppeteer.handleSummon(player, gameWorld, ability, payload);
                    case AbilityC2SPacket.ABILITY_PUPPETEER_RETURN -> Puppeteer.handleReturn(player, gameWorld);
                    case AbilityC2SPacket.ABILITY_STUNT_DOUBLE_MIMIC -> StuntDouble.handleMimic(player, gameWorld, ability, payload);
                    case AbilityC2SPacket.ABILITY_MEDIUM_JOIN_VOICE -> Medium.handleJoinVoice(player, gameWorld, ability);
                    case AbilityC2SPacket.ABILITY_TRAPPER_PLACE_TRAP -> Trapper.handlePlaceTrap(player, gameWorld, ability);
                    case AbilityC2SPacket.ABILITY_NIGHTMARE_PLANT -> Nightmare.handlePlant(player, gameWorld, ability, payload);
                    case AbilityC2SPacket.ABILITY_NIGHTMARE_FORCE_SLEEP -> Nightmare.handleForceSleep(player, gameWorld, payload);
                    case AbilityC2SPacket.ABILITY_ILLUSIONIST_CLONES -> Illusionist.handleClones(player, gameWorld, ability);
                    case AbilityC2SPacket.ABILITY_ILLUSIONIST_SWITCH_CONTROL -> Illusionist.handleSwitchControl(player, gameWorld, payload);
                    case AbilityC2SPacket.ABILITY_ARCHIVIST_SEAL -> Archivist.handleSeal(player, gameWorld, ability, payload);
                    case AbilityC2SPacket.ABILITY_CONSPIRATOR_CHANNEL -> Conspirator.handleChannel(player, gameWorld, ability, payload);
                    case AbilityC2SPacket.ABILITY_SNIPER_TOGGLE_OR_FIRE -> Sniper.handleAbility(player, gameWorld);
                    case AbilityC2SPacket.ABILITY_SNIPER_CANCEL -> Sniper.handleCancel(player, gameWorld);
                    case AbilityC2SPacket.ABILITY_WATCHMAN_RESCUE -> Watchman.handleRescue(player, gameWorld, ability, payload);
                    case AbilityC2SPacket.ABILITY_ZHANGSHI_SPEED -> Zhangshi.handleSpeed(player, gameWorld, ability);
                    case AbilityC2SPacket.ABILITY_MORTICIAN_DISGUISE -> Mortician.handleDisguise(player, gameWorld, ability, payload);
                    case AbilityC2SPacket.ABILITY_COWBOY_DUEL -> Cowboy.handleDuel(player, gameWorld, payload);
                    case AbilityC2SPacket.ABILITY_STALKER_TRACK -> Stalker.handleTrack(player, gameWorld, payload);
                    case AbilityC2SPacket.ABILITY_CONDUCTOR_ARMOR -> Conductor.handleArmor(player, gameWorld);
                    case AbilityC2SPacket.ABILITY_NOISEMAKER_MARK -> Noisemaker.handleMark(player, gameWorld, payload);
                    case AbilityC2SPacket.ABILITY_INSANE_REVIVE -> InsaneKiller.handleRevive(player, gameWorld, payload);
                    case AbilityC2SPacket.ABILITY_MORPHLING_MORPH -> Morphling.handleMorph(player, gameWorld, payload);
                    case AbilityC2SPacket.ABILITY_VIGILANTE_DETECT -> Vigilante.handleDetect(player, gameWorld, ability, payload);
                    case AbilityC2SPacket.ABILITY_CORPSE_TOUCH -> ArchivistWitness.handleCorpseTouch(player, payload);
                    case AbilityC2SPacket.ABILITY_VALENTINA_FORESIGHT -> Valentina.handleForesight(player, gameWorld);
                    case AbilityC2SPacket.ABILITY_FUNERAL_PICK_UP -> Funeral.pickUp(player, payload.targetId());
                    case AbilityC2SPacket.ABILITY_FUNERAL_PUT_DOWN -> Funeral.putDown(player);
                    case AbilityC2SPacket.ABILITY_SUBVERT_KILLER ->
                        Subversion.subvert(player, gameWorld, payload.targetId(), Faction.KILLER);
                    case AbilityC2SPacket.ABILITY_SUBVERT_CIVILIAN ->
                        Subversion.subvert(player, gameWorld, payload.targetId(), Faction.CIVILIAN);
                    case AbilityC2SPacket.ABILITY_GAMBLER_BET -> Gambler.handleBet(player, gameWorld, payload);
                    case AbilityC2SPacket.ABILITY_INSPECTOR_DISARM -> Inspector.handleDisarm(player, gameWorld);
                    case AbilityC2SPacket.ABILITY_ZHUIMU_BOAT -> Zhuimu.handleBoat(player, gameWorld);
                    case AbilityC2SPacket.ABILITY_ZHUIMU_STRUGGLE_LEFT -> Zhuimu.struggle(player, false);
                    case AbilityC2SPacket.ABILITY_ZHUIMU_STRUGGLE_RIGHT -> Zhuimu.struggle(player, true);
                    case AbilityC2SPacket.ABILITY_GRAPPLER_HOOK -> Grappler.handleHook(player, gameWorld, ability, payload);
                    case AbilityC2SPacket.ABILITY_HALF_SPIRIT_PHASE -> HalfSpirit.handlePhase(player, gameWorld, ability);
                }
			}
		);
		ServerPlayNetworking.registerGlobalReceiver(
			SpyGuessC2SPacket.TYPE,
			(payload, context) -> {
				ServerPlayer player = context.player();
				if (Spy.guess(player, payload.targetId(), payload.roleId())) {
					endWithCustomWinner(player, "spy", ModRoles.SPY.color());
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
				if (!gameWorld.isRole(player, ModRoles.ILLUSIONIST)) return;
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
			InstinctC2SPacket.TYPE,
			(payload, context) -> InstinctSnapshots.setEnabled(context.player().getUUID(), payload.enabled())
		);
		ServerPlayNetworking.registerGlobalReceiver(
			SkinUploadC2SPacket.TYPE,
			(payload, context) -> {
				ServerPlayer player = context.player();
				if (!SkinEditors.canEdit(player)) {
					player.sendSystemMessage(Component.translatable("message.brinswathe.skin.denied"));
					return;
				}
				byte[] sound = payload.sound() == null ? new byte[0] : payload.sound();
				if (payload.texture() == null || payload.texture().length + sound.length > 900_000) {
					player.sendSystemMessage(Component.translatable("message.brinswathe.skin.upload_failed"));
					return;
				}
				String accepted = KnifeSkins.acceptUpload(
					payload.kind(),
					payload.name(),
					payload.tooltipName(),
					payload.texture(),
					sound
				);
				if (accepted == null) {
					player.sendSystemMessage(Component.translatable("message.brinswathe.skin.upload_failed"));
					return;
				}
				KnifeSkins.syncToAll(player.server);
				player.sendSystemMessage(Component.translatable(
					"message.brinswathe.skin.uploaded",
					SkinEditors.normalizeType(payload.kind()),
					accepted
				));
			}
		);
	}
    private static void brinPayTrapperCatchReward(ServerPlayer trapper) {
        int reward = ModConfig.trapperCatchReward();
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

    public static boolean checkPenitentWin(MinecraftServer server, GameWorldComponent gameWorld) {
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            if (!gameWorld.isRole(sp, ModRoles.PENITENT)) continue;
            PenitentComponent comp = PenitentComponent.KEY.get(sp);
            if (comp == null) continue;
            if (comp.hasCompletedTargets()) {
                endWithCustomWinner(sp, "penitent", ModRoles.PENITENT.color());
                return true;
            }
        }
        return false;
    }

    private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			ConfigCommands.register(dispatcher);
		});
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
