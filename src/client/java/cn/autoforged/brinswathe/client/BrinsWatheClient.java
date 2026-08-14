package cn.autoforged.brinswathe.client;

import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.BrinItems;
import cn.autoforged.brinswathe.BrinSounds;
import cn.autoforged.brinswathe.BrinsWathe;
import cn.autoforged.brinswathe.component.BombComponent;
import cn.autoforged.brinswathe.component.CowboyComponent;
import cn.autoforged.brinswathe.component.EavesdropperComponent;
import cn.autoforged.brinswathe.component.IllusionistComponent;
import cn.autoforged.brinswathe.component.MorticianComponent;
import cn.autoforged.brinswathe.component.PuppeteerControlComponent;
import cn.autoforged.brinswathe.component.SniperComponent;
import cn.autoforged.brinswathe.config.BrinConfig;
import cn.autoforged.brinswathe.component.TrapperComponent;
import cn.autoforged.brinswathe.entity.PuppetEntity;
import cn.autoforged.brinswathe.network.BlindFlashS2CPacket;
import cn.autoforged.brinswathe.network.BrinAbilityC2SPacket;
import cn.autoforged.brinswathe.network.BrinConfigS2CPacket;
import cn.autoforged.brinswathe.network.BrinResourceReloadS2CPacket;
import cn.autoforged.brinswathe.network.CowboyShowdownMusicS2CPacket;
import cn.autoforged.brinswathe.network.CowboyDuelHideS2CPacket;
import cn.autoforged.brinswathe.network.CowboyDuelIdentityS2CPacket;
import cn.autoforged.brinswathe.network.CowboyDuelLookLockS2CPacket;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.BsXinQin.kinswathe.client.KinsWatheInitializeClient;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.BsXinQin.kinswathe.component.GameSafeComponent;
import org.aussiebox.starexpress.client.StarryExpressClient;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BrinsWatheClient implements ClientModInitializer {
    private static boolean abilityKeyWasDown;
    @Nullable
    private static UUID controlledCloneId;
    @Nullable
    private static CameraType previousCameraType;
    @Nullable
    private static int illusionModelRenderDepth;
    @Nullable
    private static UUID controlledPuppetId;
    @Nullable
    private static CameraType previousPuppetCameraType;
    private static int puppetModelRenderDepth;
    @Nullable
    private static AbstractClientPlayer puppetSkinCarrier;
    @Nullable
    private static PlayerSkin puppetSkinValue;
    private static int blindFlashTotal;
    private static int blindFlashRemaining;
    private static final Set<UUID> hiddenDuelists = new HashSet<>();
    private static boolean cowboyDuelHidesIdentities;
    private static boolean cowboyDuelLookLocked;
    private static boolean suppressSpectatorRoleHud;
    private static boolean sniperCancelSent;

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (!stack.is(BrinItems.XUEZI)) return;
            int seconds = BrinConfig.xueziCooldownSeconds();
            if (Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.getCooldowns().isOnCooldown(BrinItems.XUEZI)) {
                float percent = Minecraft.getInstance().player.getCooldowns()
                    .getCooldownPercent(BrinItems.XUEZI, 0.0F);
                seconds = Math.max(1, Mth.ceil(percent * BrinConfig.xueziCooldownSeconds()));
            }
            lines.add(Component.translatable("item.brinswathe.xuezi.cooldown", seconds));
        });

        ClientPlayNetworking.registerGlobalReceiver(BlindFlashS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> startBlindFlash(payload.durationTicks())));
        ClientPlayNetworking.registerGlobalReceiver(BrinConfigS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> BrinConfig.applyServerJson(payload.json())));
        ClientPlayNetworking.registerGlobalReceiver(BrinResourceReloadS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> {
                if (context.client().player == null || context.client().level == null) return;
                reloadClientResources(context.client());
            }));
        ClientPlayNetworking.registerGlobalReceiver(CowboyShowdownMusicS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> handleShowdownMusic(payload.play(), payload.volume())));
        ClientPlayNetworking.registerGlobalReceiver(CowboyDuelHideS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> handleDuelistHide(payload.hide(), payload.first(), payload.second())));
        ClientPlayNetworking.registerGlobalReceiver(CowboyDuelIdentityS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> cowboyDuelHidesIdentities = payload.hide()));
        ClientPlayNetworking.registerGlobalReceiver(CowboyDuelLookLockS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> cowboyDuelLookLocked = payload.lock()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
            client.execute(() -> {
                cowboyDuelHidesIdentities = false;
                cowboyDuelLookLocked = false;
                hiddenDuelists.clear();
            }));

        StalkerTrackerHud.init();
        registerClientStaminaDiagnostic();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (blindFlashRemaining > 0) {
                blindFlashRemaining--;
            }
            KeyMapping abilityBind = getAbilityBind();
            StarryExpressClient.abilityBind = abilityBind;
            boolean abilityKeyDown = abilityBind != null && abilityBind.isDown();
            if (abilityKeyDown && !abilityKeyWasDown && client.screen == null) {
                client.execute(() -> {
                    if (Minecraft.getInstance().player != null) {
                        handleAbilityKeyPress();
                    }
                });
            }
            abilityKeyWasDown = abilityKeyDown;
            tickIllusionistControl(client);
            tickPuppetControl(client);
            tickSniperAiming(client);
            tickMorticianDisguiseSkins(client);
        });
    }

    /**
     * The client-side twin of {@code /brinswathe stamina}: prints exactly the numbers the kins stamina
     * bar renders from on this screen, so a healthy server report with a broken bar can be pinned to the
     * precise client value that went wrong.
     */
    private static void registerClientStaminaDiagnostic() {
        net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess) -> dispatcher.register(
                net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("brinstamina")
                    .executes(context -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player == null) return 0;

                        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
                        var role = gameWorld.getRole(player);
                        // Deliberately called through the Role method so StaminaRoleMixin participates,
                        // exactly like the kins bar's own call.
                        int mixedMaxSprint = role == null ? -999 : role.getMaxSprintTime();
                        cn.autoforged.brinswathe.component.StaminaComponent stamina =
                            cn.autoforged.brinswathe.component.StaminaComponent.KEY.get(player);
                        float sprintTicks = player
                            .saveWithoutId(new net.minecraft.nbt.CompoundTag())
                            .getFloat("sprintingTicks");
                        String ratio = mixedMaxSprint > 0
                            ? String.format("%.2f", Math.max(0.0F, Math.min(1.0F, sprintTicks / mixedMaxSprint)))
                            : "-";
                        context.getSource().sendFeedback(Component.literal(
                            "CLIENT role=" + (role == null ? "null" : role.identifier())
                                + " maxSprint(mixed)=" + mixedMaxSprint
                                + " | stamina=" + (stamina == null
                                    ? "-"
                                    : stamina.currentStamina + "/" + stamina.maxStamina)
                                + " | sprintTicks=" + String.format("%.1f", sprintTicks)
                                + " | barFill=" + ratio
                        ));
                        return 1;
                    })
            )
        );
    }

    private static final float ILLUSION_AIM_TOLERANCE = 0.5F;

    private static void reloadClientResources(Minecraft client) {
        if (client.player == null || client.level == null) return;
        client.getLanguageManager().onResourceManagerReload(client.getResourceManager());
        reloadStarryGuidebook(client);
    }

    private static void reloadStarryGuidebook(Minecraft client) {
        if (client.player == null || client.level == null) return;
        try {
            Class<?> collectorClass = Class.forName(
                "org.aussiebox.starexpress.client.guidebook.GuidebookEntryCollector"
            );
            Object collector = collectorClass.getField("INSTANCE").get(null);
            for (java.lang.reflect.Method method : collectorClass.getMethods()) {
                if (!method.getName().equals("reload") || method.getParameterCount() != 1) continue;
                method.invoke(collector, client.getResourceManager());
                return;
            }
            BrinsWathe.LOGGER.warn("Could not find Starry Express guidebook reload method");
        } catch (ClassNotFoundException ignored) {
        } catch (ReflectiveOperationException | IllegalArgumentException exception) {
            BrinsWathe.LOGGER.warn("Failed to reload Starry Express guidebook: {}", exception.getMessage());
        }
    }

    public static Entity weaponAimOrigin(Player attacker) {
        return attacker;
    }

    public static boolean isWeaponTarget(Player attacker, Entity entity) {
        if (entity == attacker) return false;
        if (entity instanceof Player player) {
            return GameFunctions.isPlayerAliveAndSurvival(player);
        }
        if (!(entity instanceof PlayerBodyEntity body)) return false;
        // A controlled puppet always sits on top of its puppeteer, so weapons resolve against the real
        // player there; only the stand-in left behind needs to be aimable in its own right.
        if (PuppeteerControlComponent.isPuppetBodyProxy(body)) {
            return !attacker.getUUID().equals(((PuppetEntity) body).brin$getPuppeteer());
        }
        if (attacker.getUUID().equals(body.getPlayerUuid())) return false;
        return IllusionistComponent.isIllusionModel(body);
    }

    public static HitResult findWeaponTarget(Player attacker, double range) {
        Entity origin = weaponAimOrigin(attacker);
        Vec3 eye = origin.getEyePosition();
        Vec3 end = eye.add(origin.getViewVector(1.0F).scale(range));

        HitResult blockHit = origin.level().clip(new ClipContext(
            eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, origin));
        double bestDistSqr = blockHit.getType() == HitResult.Type.MISS
            ? range * range
            : blockHit.getLocation().distanceToSqr(eye);

        Entity bestEntity = null;
        Vec3 bestPos = null;
        AABB searchBox = origin.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        for (Entity candidate : origin.level().getEntities(origin, searchBox,
                entity -> isWeaponTarget(attacker, entity))) {
            float tolerance = candidate instanceof PlayerBodyEntity body
                && (IllusionistComponent.isIllusionModel(body)
                    || PuppeteerControlComponent.isPuppetModel(body))
                ? ILLUSION_AIM_TOLERANCE
                : 0.0F;
            AABB box = candidate.getBoundingBox().inflate(tolerance);
            if (box.contains(eye)) {
                bestEntity = candidate;
                bestPos = eye;
                bestDistSqr = 0.0D;
                continue;
            }
            var clip = box.clip(eye, end);
            if (clip.isEmpty()) continue;
            double distSqr = eye.distanceToSqr(clip.get());
            if (distSqr < bestDistSqr) {
                bestEntity = candidate;
                bestPos = clip.get();
                bestDistSqr = distSqr;
            }
        }
        return bestEntity != null ? new EntityHitResult(bestEntity, bestPos) : blockHit;
    }

    public static void startBlindFlash(int durationTicks) {
        blindFlashTotal = Math.max(durationTicks, 1);
        blindFlashRemaining = blindFlashTotal;
    }

    private static void handleShowdownMusic(boolean play, float volume) {
        Minecraft client = Minecraft.getInstance();
        var id = BrinSounds.COWBOY_SHOWDOWN.getLocation();
        // Streamed, relative, no attenuation: Sound Physics must not treat this as 3D world audio
        // (8192-sample Vorbis blocks + occlusion is a known native stack smash). MASTER rather than
        // MUSIC so a muted vanilla BGM slider cannot swallow a config of 100.
        client.getSoundManager().stop(id, SoundSource.MASTER);
        client.getSoundManager().stop(id, SoundSource.MUSIC);
        float gain = Mth.clamp(volume, 0.0F, 1.0F);
        if (play && gain > 0.0F) {
            client.getSoundManager().play(new SimpleSoundInstance(
                id,
                SoundSource.MASTER,
                gain,
                1.0F,
                SoundInstance.createUnseededRandom(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0,
                0.0,
                0.0,
                true
            ));
        }
    }

    private static void handleDuelistHide(boolean hide, UUID first, UUID second) {
        hiddenDuelists.clear();
        if (hide) {
            if (first != null) hiddenDuelists.add(first);
            if (second != null) hiddenDuelists.add(second);
        }
    }

    public static boolean isDuelistHiddenFromSpectators(UUID playerId) {
        return hiddenDuelists.contains(playerId);
    }

    public static boolean isCowboyDuelHidingIdentities() {
        return cowboyDuelHidesIdentities;
    }

    /**
     * Spectators (and creative admins) lose role names, cohort tags and instinct colours for the
     * whole duel. The two living duelists keep their normal HUD.
     */
    public static boolean shouldHideSpectatorIdentities() {
        if (!cowboyDuelHidesIdentities) return false;
        Player player = Minecraft.getInstance().player;
        return player != null && GameFunctions.isPlayerSpectatingOrCreative(player);
    }

    /**
     * RoleNameRenderer is the only place Harpy consults {@code WatheClient.isPlayerSpectatingOrCreative}
     * to print role names and modifier tags. The flag is raised for that HUD call only so other
     * spectator checks (range, instinct, ability widgets) stay honest.
     */
    public static void setSuppressSpectatorRoleHud(boolean suppress) {
        suppressSpectatorRoleHud = suppress;
    }

    public static boolean isSuppressingSpectatorRoleHud() {
        return suppressSpectatorRoleHud;
    }

    /** Mouse look is refused while the overlay is up, and on duelists for the whole countdown walk. */
    public static boolean isDuelLookLocked() {
        return cowboyDuelLookLocked || blindFlashRemaining > 0;
    }

    public static int getBlindFlashTotal() {
        return blindFlashTotal;
    }

    public static int getBlindFlashRemaining() {
        return blindFlashRemaining;
    }

    @Nullable
    public static KeyMapping getAbilityBind() {
        return KinsWatheInitializeClient.abilityBind;
    }

    public static void handleAbilityKeyPress() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (GameSafeComponent.KEY.get(player.level()).isGameSafe) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(player);

        if (gameWorld.isRole(player, BrinRoles.SNIPER)) {
            SniperComponent component = SniperComponent.KEY.get(player);
            if (component != null && (component.isAiming() || component.getCooldownTicks() <= 0)) {
                ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                    BrinAbilityC2SPacket.ABILITY_SNIPER_TOGGLE_OR_FIRE,
                    null
                ));
            }
            return;
        }

        if (gameWorld.isRole(player, BrinRoles.PUPPETEER)) {
            PuppeteerControlComponent component = PuppeteerControlComponent.KEY.get(player);
            if (component == null) return;
            if (component.isControlling()) {
                ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                    BrinAbilityC2SPacket.ABILITY_PUPPETEER_RETURN,
                    null
                ));
                return;
            }
            if (component.craftCooldownTicks > 0) return;
            UUID corpseId = findPlayerBodyInCrosshair(player);
            if (corpseId != null) {
                ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                    BrinAbilityC2SPacket.ABILITY_PUPPETEER_CRAFT,
                    corpseId
                ));
            }
            return;
        }

        if (gameWorld.isRole(player, BrinRoles.ILLUSIONIST)) {
            IllusionistComponent component = IllusionistComponent.KEY.get(player);
            if (component != null && !component.cloneEntityIds.isEmpty()) {
                ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                    BrinAbilityC2SPacket.ABILITY_ILLUSIONIST_SWITCH_CONTROL,
                    getNextControlTarget(component)
                ));
            } else if (ability.cooldown <= 0) {
                ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                    BrinAbilityC2SPacket.ABILITY_ILLUSIONIST_CLONES,
                    null
                ));
            }
            return;
        }

        if (gameWorld.isRole(player, BrinRoles.EAVESDROPPER)) {
            EavesdropperComponent channel = EavesdropperComponent.KEY.get(player);
            if (GameFunctions.isPlayerAliveAndSurvival(player)
                && ability.cooldown <= 0
                && (channel == null || !channel.isInTemporaryChannel())) {
                Minecraft.getInstance().setScreen(new LimitedInventoryScreen(player));
            }
            return;
        }

        if (gameWorld.isRole(player, BrinRoles.COWBOY)) {
            CowboyComponent cowboy = CowboyComponent.KEY.get(player);
            // Balance is left to the server and the hud; the pick screen only needs the one-shot flag.
            if (GameFunctions.isPlayerAliveAndSurvival(player)
                && cowboy != null
                && !cowboy.duelUsed()) {
                Minecraft.getInstance().setScreen(new LimitedInventoryScreen(player));
            }
            return;
        }

        if (ability.cooldown > 0) return;

        if (gameWorld.isRole(player, BrinRoles.TRAPPER)) {
            ClientPlayNetworking.send(new BrinAbilityC2SPacket(BrinAbilityC2SPacket.ABILITY_TRAPPER_PLACE_TRAP, null));
        } else if (gameWorld.isRole(player, BrinRoles.WATCHMAN)) {
            UUID targetId = findPlayerInCrosshair(player);
            if (targetId != null && (isTrappedPlayer(player, targetId) || isBombedPlayer(player, targetId))) {
                ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                    BrinAbilityC2SPacket.ABILITY_WATCHMAN_RESCUE, targetId));
            }
        } else if (gameWorld.isRole(player, BrinRoles.NIGHTMARE)) {
            UUID targetId = findSleepingPlayerInCrosshair(player);
            if (targetId != null) {
                ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                    BrinAbilityC2SPacket.ABILITY_NIGHTMARE_PLANT, targetId));
            }
        } else if (gameWorld.isRole(player, BrinRoles.ARCHIVIST)) {
            UUID corpseId = findPlayerBodyInCrosshair(player);
            if (corpseId != null) {
                ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                    BrinAbilityC2SPacket.ABILITY_ARCHIVIST_SEAL, corpseId));
            }
        } else if (gameWorld.isRole(player, BrinRoles.GAMBLER)) {
            UUID targetId = findPlayerInCrosshair(player);
            if (targetId != null) {
                ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                    BrinAbilityC2SPacket.ABILITY_GAMBLER_BET, targetId));
            }
        } else if (gameWorld.isRole(player, BrinRoles.ZHANGSHI)) {
            ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                BrinAbilityC2SPacket.ABILITY_ZHANGSHI_SPEED, null));
        } else if (gameWorld.isRole(player, BrinRoles.MEDIUM)) {
            ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                BrinAbilityC2SPacket.ABILITY_MEDIUM_JOIN_VOICE, null));
        }
    }

    @Nullable
    private static UUID getNextControlTarget(IllusionistComponent component) {
        if (component.controlledCloneId == null) return component.cloneEntityIds.getFirst();

        int currentIndex = component.cloneEntityIds.indexOf(component.controlledCloneId);
        if (currentIndex < 0 || currentIndex + 1 >= component.cloneEntityIds.size()) return null;
        return component.cloneEntityIds.get(currentIndex + 1);
    }

    /**
     * Wathe only caches the skin of players this client has actually loaded, and it is that cache the
     * corpse renderer reads. A mortician can pose as somebody who was never in range, so the impersonated
     * skin is seeded here or the disguise would fall back to the default corpse texture.
     */
    private static void tickMorticianDisguiseSkins(Minecraft client) {
        if (client.level == null) return;
        ClientPacketListener connection = client.getConnection();
        if (connection == null) return;

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof PlayerBodyEntity body)) continue;
            if (!MorticianComponent.isDisguiseBody(body)) continue;

            UUID skinId = body.getPlayerUuid();
            if (skinId == null || WatheClient.PLAYER_ENTRIES_CACHE.get(skinId) != null) continue;
            PlayerInfo info = connection.getPlayerInfo(skinId);
            if (info != null) WatheClient.PLAYER_ENTRIES_CACHE.put(skinId, info);
        }
    }

    private static void tickIllusionistControl(Minecraft client) {
        if (client.player == null || client.level == null) {
            stopClientControl(client);
            return;
        }

        IllusionistComponent component = IllusionistComponent.KEY.get(client.player);
        UUID targetId = component == null ? null : component.controlledCloneId;
        Entity target = findClientEntity(client, targetId);
        if (!(target instanceof PlayerBodyEntity clone) || !IllusionistComponent.isClone(clone)) {
            stopClientControl(client);
            return;
        }

        if (controlledCloneId == null) {
            previousCameraType = client.options.getCameraType();
        }
        controlledCloneId = targetId;
        client.options.setCameraType(CameraType.FIRST_PERSON);
        if (client.getCameraEntity() == clone) {
            client.setCameraEntity(client.player);
        }
    }

    private static void tickPuppetControl(Minecraft client) {
        if (client.player == null || client.level == null) {
            stopPuppetControl(client);
            return;
        }

        PuppeteerControlComponent component = PuppeteerControlComponent.KEY.get(client.player);
        UUID targetId = component == null ? null : component.puppetEntityId;
        if (targetId == null || !(findClientEntity(client, targetId) instanceof PlayerBodyEntity puppet)
            || !PuppeteerControlComponent.isPuppet(puppet)) {
            stopPuppetControl(client);
            return;
        }

        if (controlledPuppetId == null) {
            previousPuppetCameraType = client.options.getCameraType();
        }
        controlledPuppetId = targetId;
        // The puppeteer's own model is hidden while possessing, so third person would show empty air.
        client.options.setCameraType(CameraType.FIRST_PERSON);
    }

    private static void stopPuppetControl(Minecraft client) {
        if (controlledPuppetId == null) return;

        controlledPuppetId = null;
        if (previousPuppetCameraType != null) {
            client.options.setCameraType(previousPuppetCameraType);
            previousPuppetCameraType = null;
        }
    }

    public static boolean isControllingPuppet() {
        return controlledPuppetId != null;
    }

    public static boolean isControllingPuppet(UUID puppetId) {
        return puppetId.equals(controlledPuppetId);
    }

    public static void beginPuppetModelRender(AbstractClientPlayer carrier, @Nullable PlayerSkin skin) {
        puppetModelRenderDepth++;
        puppetSkinCarrier = carrier;
        puppetSkinValue = skin;
    }

    public static void endPuppetModelRender() {
        puppetModelRenderDepth = Math.max(0, puppetModelRenderDepth - 1);
        if (puppetModelRenderDepth == 0) {
            puppetSkinCarrier = null;
            puppetSkinValue = null;
        }
    }

    public static boolean isRenderingPuppetModel() {
        return puppetModelRenderDepth > 0;
    }

    @Nullable
    public static PlayerSkin puppetSkinOverride(AbstractClientPlayer player) {
        return puppetModelRenderDepth > 0 && player == puppetSkinCarrier ? puppetSkinValue : null;
    }

    /**
     * The body a puppet model is drawn with. Spectators are unusable here because the player renderer
     * hides everything but the head on them, and the corpse's owner is dead by definition - so on any
     * client that has that spectator loaded the puppet would all but vanish. The skin is overridden
     * separately, so borrowing a living body changes nothing about how the puppet looks.
     */
    @Nullable
    public static AbstractClientPlayer puppetRenderCarrier(PlayerBodyEntity entity) {
        UUID skinId = entity.getPlayerUuid();
        if (skinId != null && entity.level().getPlayerByUUID(skinId) instanceof AbstractClientPlayer direct
            && !direct.isSpectator()) {
            return direct;
        }
        UUID puppeteerId = ((PuppetEntity) entity).brin$getPuppeteer();
        if (puppeteerId != null
            && entity.level().getPlayerByUUID(puppeteerId) instanceof AbstractClientPlayer puppeteer
            && !puppeteer.isSpectator()) {
            return puppeteer;
        }
        AbstractClientPlayer local = Minecraft.getInstance().player;
        return local == null || local.isSpectator() ? null : local;
    }

    /**
     * Wathe reads the crosshair name off whichever player it raycasts, and both disguises leave the real
     * player standing inside the model that replaced them, so the name has to be swapped alongside the
     * skin. Returns {@code null} when the player is not disguised and the vanilla name should stand.
     */
    @Nullable
    public static Component disguisedName(Player player) {
        MorticianComponent mortician = MorticianComponent.KEY.get(player);
        if (mortician != null && mortician.isDisguised()) {
            return borrowedBodyName(player, mortician.disguiseBodyId);
        }

        PuppeteerControlComponent puppeteer = PuppeteerControlComponent.KEY.get(player);
        if (puppeteer != null && puppeteer.isControlling()) {
            return borrowedBodyName(player, puppeteer.puppetEntityId);
        }
        return null;
    }

    /**
     * The name of whoever the borrowed body belongs to. Every failure path falls back to a blank name,
     * because letting the real player's name through would undo the disguise entirely.
     */
    private static Component borrowedBodyName(Player player, @Nullable UUID bodyId) {
        Entity body = findClientEntity(Minecraft.getInstance(), bodyId);
        UUID skinId = body instanceof PlayerBodyEntity playerBody ? playerBody.getPlayerUuid() : null;
        if (skinId == null) return Component.empty();
        if (player.level().getPlayerByUUID(skinId) instanceof Player skinOwner) {
            return skinOwner.getDisplayName();
        }
        // A puppet's owner is a spectator by now and may be out of tracking range, so the tab list is the
        // last resort before giving up on a name.
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo info = connection == null ? null : connection.getPlayerInfo(skinId);
        return info == null ? Component.empty() : Component.literal(info.getProfile().getName());
    }

    @Nullable
    public static PlayerSkin puppetRenderSkin(PlayerBodyEntity entity) {
        UUID skinId = entity.getPlayerUuid();
        if (skinId == null) return null;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo info = connection == null ? null : connection.getPlayerInfo(skinId);
        return info == null ? DefaultPlayerSkin.get(skinId) : info.getSkin();
    }

    private static void tickSniperAiming(Minecraft client) {
        if (client.player == null || client.level == null) {
            sniperCancelSent = false;
            return;
        }

        SniperComponent component = SniperComponent.KEY.get(client.player);
        if (component == null || !component.isAiming()) {
            sniperCancelSent = false;
            return;
        }

        client.player.setDeltaMovement(Vec3.ZERO);
        if (client.screen instanceof PauseScreen && !sniperCancelSent) {
            sniperCancelSent = true;
            ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                BrinAbilityC2SPacket.ABILITY_SNIPER_CANCEL,
                null
            ));
            client.setScreen(null);
        }
    }

    public static boolean isSniperAiming() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return false;
        SniperComponent component = SniperComponent.KEY.get(client.player);
        return component != null && component.isAiming();
    }

    @Nullable
    private static Entity findClientEntity(Minecraft client, @Nullable UUID entityId) {
        if (client.level == null || entityId == null) return null;
        for (Entity entity : client.level.entitiesForRendering()) {
            if (entityId.equals(entity.getUUID())) return entity;
        }
        return null;
    }

    private static void stopClientControl(Minecraft client) {
        if (controlledCloneId == null) return;

        controlledCloneId = null;
        if (client.player != null && client.getCameraEntity() instanceof PlayerBodyEntity body
            && IllusionistComponent.isClone(body)) {
            client.setCameraEntity(client.player);
        }
        if (previousCameraType != null) {
            client.options.setCameraType(previousCameraType);
            previousCameraType = null;
        }
    }

    public static boolean isControllingClone() {
        return controlledCloneId != null;
    }

    public static boolean isControllingClone(UUID cloneId) {
        return cloneId.equals(controlledCloneId);
    }

    public static void beginIllusionModelRender() {
        illusionModelRenderDepth++;
    }

    public static void endIllusionModelRender() {
        illusionModelRenderDepth = Math.max(0, illusionModelRenderDepth - 1);
    }

    public static boolean isRenderingIllusionModel() {
        return illusionModelRenderDepth > 0;
    }

    @Nullable
    private static UUID findSleepingPlayerInCrosshair(Player localPlayer) {
        if (Minecraft.getInstance().getConnection() == null) return null;

        Vec3 eyePos = localPlayer.getEyePosition();
        Vec3 look = localPlayer.getLookAngle();
        double bestDot = -2.0;
        UUID bestTarget = null;

        for (PlayerInfo info : Minecraft.getInstance().getConnection().getListedOnlinePlayers()) {
            if (info.getProfile().getId().equals(localPlayer.getUUID())) continue;
            Player targetPlayer = localPlayer.level().getPlayerByUUID(info.getProfile().getId());
            if (targetPlayer == null || !targetPlayer.isSleeping()) continue;

            Vec3 toTarget = targetPlayer.getEyePosition().subtract(eyePos).normalize();
            double dot = toTarget.dot(look);
            if (dot > bestDot && dot > 0.5) {
                bestDot = dot;
                bestTarget = targetPlayer.getUUID();
            }
        }
        return bestTarget;
    }

    @Nullable
    private static UUID findPlayerInCrosshair(Player localPlayer) {
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
            localPlayer,
            entity -> entity instanceof Player target
                && target != localPlayer
                && GameFunctions.isPlayerAliveAndSurvival(target),
            64.0D
        );
        if (hitResult instanceof EntityHitResult entityHit
            && entityHit.getEntity() instanceof Player targetPlayer) {
            return targetPlayer.getUUID();
        }
        return null;
    }

    private static boolean isBombedPlayer(Player localPlayer, UUID targetId) {
        Player target = localPlayer.level().getPlayerByUUID(targetId);
        if (target == null) return false;
        BombComponent bomb = BombComponent.KEY.get(target);
        return bomb != null && bomb.canBeDefused();
    }

    private static boolean isTrappedPlayer(Player localPlayer, UUID targetId) {
        for (Player player : localPlayer.level().players()) {
            TrapperComponent trapper = TrapperComponent.KEY.get(player);
            if (trapper != null && trapper.isTrapped(targetId)) return true;
        }
        return false;
    }

    @Nullable
    private static UUID findPlayerBodyInCrosshair(Player localPlayer) {
        PlayerBodyEntity body = getPlayerBodyInCrosshair(localPlayer);
        return body == null ? null : body.getUUID();
    }

    @Nullable
    public static PlayerBodyEntity getPlayerBodyInCrosshair(Player localPlayer) {
        return brinBodyInCrosshair(localPlayer, false);
    }

    /**
     * The archivist only reads a body instead of handling it, so a mortician's decoy has to answer here
     * even though every ability that touches a corpse still refuses to see it.
     */
    @Nullable
    public static PlayerBodyEntity getInspectableBodyInCrosshair(Player localPlayer) {
        return brinBodyInCrosshair(localPlayer, true);
    }

    @Nullable
    private static PlayerBodyEntity brinBodyInCrosshair(Player localPlayer, boolean includeDisguises) {
        Vec3 eye = localPlayer.getEyePosition();
        Vec3 look = localPlayer.getLookAngle();
        double range = 8.0D;
        Vec3 end = eye.add(look.scale(range));

        HitResult blockHit = localPlayer.level().clip(new ClipContext(
            eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, localPlayer));
        double bestDistanceSqr = blockHit.getType() == HitResult.Type.MISS
            ? range * range
            : eye.distanceToSqr(blockHit.getLocation());

        PlayerBodyEntity bestBody = null;
        AABB searchBox = localPlayer.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);
        for (PlayerBodyEntity body : localPlayer.level().getEntitiesOfClass(
            PlayerBodyEntity.class,
            searchBox,
            candidate -> !candidate.isInvisible()
                && !IllusionistComponent.isIllusionModel(candidate)
                && !PuppeteerControlComponent.isPuppetModel(candidate)
                && (includeDisguises || !MorticianComponent.isDisguiseBody(candidate)))) {
            var hit = body.getBoundingBox().inflate(0.25D).clip(eye, end);
            if (hit.isEmpty()) continue;

            double distanceSqr = eye.distanceToSqr(hit.get());
            if (distanceSqr <= bestDistanceSqr) {
                bestDistanceSqr = distanceSqr;
                bestBody = body;
            }
        }
        return bestBody;
    }
}
