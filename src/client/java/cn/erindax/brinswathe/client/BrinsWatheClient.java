package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.BrinItems;
import cn.erindax.brinswathe.BrinNoelleAccess;
import cn.erindax.brinswathe.BrinSounds;
import cn.erindax.brinswathe.BrinsWathe;
import cn.erindax.brinswathe.RpsManager;
import cn.erindax.brinswathe.BrinKnifeSkins;
import cn.erindax.brinswathe.component.CowboyComponent;
import cn.erindax.brinswathe.component.EavesdropperComponent;
import cn.erindax.brinswathe.component.IllusionistComponent;
import cn.erindax.brinswathe.component.MorticianComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import cn.erindax.brinswathe.component.SniperComponent;
import cn.erindax.brinswathe.component.StalkerComponent;
import cn.erindax.brinswathe.config.BrinConfig;
import cn.erindax.brinswathe.client.gui.BrinEventLogScreen;
import cn.erindax.brinswathe.entity.PuppetEntity;
import cn.erindax.brinswathe.network.BlindFlashS2CPacket;
import cn.erindax.brinswathe.network.BrinAbilityC2SPacket;
import cn.erindax.brinswathe.network.BrinConfigS2CPacket;
import cn.erindax.brinswathe.BrinIcFlags;
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
import cn.erindax.brinswathe.network.RpsActionC2SPacket;
import cn.erindax.brinswathe.network.RpsStateS2CPacket;

import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheCosmetics;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.item.RevolverItem;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.client.StarryExpressClient;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

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
    private static KeyMapping rpsBind;
    private static boolean appliedInstinctNightVision;
    private static final int INSTINCT_NIGHT_VISION_DURATION = 220;
    private static boolean lastInstinctReported;

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (stack.is(BrinItems.XUEZI)) {
                int seconds = BrinConfig.xueziCooldownSeconds();
                if (Minecraft.getInstance().player != null
                    && Minecraft.getInstance().player.getCooldowns().isOnCooldown(BrinItems.XUEZI)) {
                    float percent = Minecraft.getInstance().player.getCooldowns()
                        .getCooldownPercent(BrinItems.XUEZI, 0.0F);
                    seconds = Math.max(1, Mth.ceil(percent * BrinConfig.xueziCooldownSeconds()));
                }
                lines.add(Component.translatable("item.brinswathe.xuezi.cooldown", seconds));
                return;
            }
            if (stack.is(WatheItems.KNIFE)) {
                CustomData data = stack.get(DataComponents.CUSTOM_DATA);
                if (data != null) {
                    var nbt = data.copyTag();
                    if (nbt.contains("wathe_skin")) {
                        String skin = nbt.getString("wathe_skin");
                        String resolved = BrinKnifeSkins.resolveKnifeSkinName(skin);
                        if (BrinKnifeSkins.shouldBrinRender(resolved)) {
                            lines.add(Component.translatable("tip.skin")
                                .append(Component.literal(BrinKnifeSkins.tooltipName(resolved))));
                        }
                    }
                }
            }
        });

        BrinKnifeSkinClient.init();
        BrinInstinctClient.init();
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (overlay || !BrinEventLogScreen.isEventLog(message)) return true;
            Minecraft.getInstance().execute(() ->
                Minecraft.getInstance().setScreen(new BrinEventLogScreen(message.copy())));
            return false;
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
                RpsHud.clear();
                BrinInstinctClient.clear();
                lastInstinctReported = false;
                BrinIcFlags.instinctNightVision = true;
            }));

        StalkerTrackerHud.init();
        RpsHud.init();
        rpsBind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.brinswathe.rps",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.brinswathe"
        ));
        ClientPlayNetworking.registerGlobalReceiver(RpsStateS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> RpsHud.apply(payload)));
        ClientPlayNetworking.registerGlobalReceiver(BrinInstinctSnapshotS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> BrinInstinctClient.apply(payload.entries())));
        ClientPlayNetworking.registerGlobalReceiver(BrinKnifeSkinListS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> BrinKnifeSkinClient.applyRemoteSkins(payload.skins())));
        ClientPlayNetworking.registerGlobalReceiver(BrinKnifeSkinApplyS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> {
                var player = Minecraft.getInstance().player;
                if (player == null) return;
                boolean applied = false;
                for (ItemStack stack : player.getInventory().items) {
                    if (brinApplyOfficialSkin(player, stack, payload.itemName(), payload.skinName())) {
                        applied = true;
                    }
                }
                for (ItemStack stack : player.getInventory().offhand) {
                    if (brinApplyOfficialSkin(player, stack, payload.itemName(), payload.skinName())) {
                        applied = true;
                    }
                }
                if (!applied && "knife".equalsIgnoreCase(payload.itemName())) {
                    WatheCosmetics.setSkin(player, new ItemStack(WatheItems.KNIFE), payload.skinName());
                }
            }));
        ClientPlayNetworking.registerGlobalReceiver(BrinIcNightVisionS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> {
                BrinIcFlags.instinctNightVision = payload.enabled();
                BrinIcFlags.instinctHudNamesThroughWalls = payload.hudNamesThroughWalls();
            }));
        registerClientStaminaDiagnostic();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (blindFlashRemaining > 0) {
                blindFlashRemaining--;
            }
            if (rpsBind != null && rpsBind.consumeClick() && client.screen == null) {
                tryInviteRps(client);
            }
            if (RpsHud.locksMovement() && client.player != null) {
                client.player.setDeltaMovement(Vec3.ZERO);
                client.player.setSprinting(false);
                var input = client.player.input;
                input.forwardImpulse = 0.0F;
                input.leftImpulse = 0.0F;
                input.up = false;
                input.down = false;
                input.left = false;
                input.right = false;
                input.jumping = false;
            }
            KeyMapping abilityBind = getAbilityBind();
            if (abilityBind != null) {
                StarryExpressClient.abilityBind = abilityBind;
            }
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
            tickInstinctNightVision(client);
            tickInstinctSnapshot(client);
        });
    }

    private static void registerClientStaminaDiagnostic() {
        net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess) -> dispatcher.register(
                net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("brinstamina")
                    .executes(context -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player == null) return 0;
                        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
                        var role = gameWorld.getRole(player);

                        int mixedMaxSprint = role == null ? -999 : role.getMaxSprintTime();
                        cn.erindax.brinswathe.component.StaminaComponent stamina =
                            cn.erindax.brinswathe.component.StaminaComponent.KEY.get(player);
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

    public static boolean shouldHideSpectatorIdentities() {
        if (!cowboyDuelHidesIdentities) return false;
        Player player = Minecraft.getInstance().player;
        return player != null && GameFunctions.isPlayerSpectatingOrCreative(player);
    }
    public static void setSuppressSpectatorRoleHud(boolean suppress) {
        suppressSpectatorRoleHud = suppress;
    }
    public static boolean isSuppressingSpectatorRoleHud() {
        return suppressSpectatorRoleHud;
    }
    public static boolean isDuelLookLocked() {
        return cowboyDuelLookLocked || blindFlashRemaining > 0;
    }
    public static int getBlindFlashTotal() {
        return blindFlashTotal;
    }
    public static int getBlindFlashRemaining() {
        return blindFlashRemaining;
    }
    private static boolean brinApplyOfficialSkin(LocalPlayer player, ItemStack stack, String itemName, String skinName) {
        if (stack.isEmpty()) return false;
        if ("knife".equalsIgnoreCase(itemName) && stack.is(WatheItems.KNIFE)) {
            WatheCosmetics.setSkin(player, stack, skinName);
            return true;
        }
        if ("gun".equalsIgnoreCase(itemName) && stack.getItem() instanceof RevolverItem) {
            WatheCosmetics.setSkin(player, stack, skinName);
            return true;
        }
        return false;
    }
    @Nullable
    public static KeyMapping getAbilityBind() {
        return KinsWatheInitializeClient.abilityBind;
    }
    public static KeyMapping shareAbilityBind(KeyMapping candidate) {
        KeyMapping shared = KinsWatheInitializeClient.abilityBind;
        if (shared == null) {
            shared = KeyBindingHelper.registerKeyBinding(candidate);
            KinsWatheInitializeClient.abilityBind = shared;
        }
        StarryExpressClient.abilityBind = shared;
        return shared;
    }

    private static void tryInviteRps(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || !GameFunctions.isPlayerAliveAndSurvival(player)) return;
        HitResult hit = findWeaponTarget(player, RpsManager.RANGE);
        if (!(hit instanceof EntityHitResult entityHit)
            || !(entityHit.getEntity() instanceof Player target)
            || !GameFunctions.isPlayerAliveAndSurvival(target)) {
            player.displayClientMessage(Component.translatable("message.brinswathe.rps.no_target"), true);
            return;
        }
        ClientPlayNetworking.send(RpsActionC2SPacket.invite(target.getUUID()));
    }
    public static void handleAbilityKeyPress() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (gameWorld.isRole(player, StarryExpressRoles.MUZZLER)) {
            ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                BrinAbilityC2SPacket.ABILITY_MUZZLER_SILENCE,
                null
            ));
            return;
        }
        if (GameSafeComponent.KEY.get(player.level()).isGameSafe
            && !BrinRoles.ignoresOpeningSafeTime(gameWorld, player)) {
            return;
        }
        if (BrinNoelleAccess.isRole(gameWorld, player, BrinNoelleAccess.CONDUCTOR_ID)) {
            ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                BrinAbilityC2SPacket.ABILITY_CONDUCTOR_ARMOR, null));
            return;
        }
        if (gameWorld.isRole(player, WatheRoles.VIGILANTE)) {
            AbilityPlayerComponent vigilanteAbility = AbilityPlayerComponent.KEY.get(player);
            if (vigilanteAbility != null && vigilanteAbility.cooldown > 0) return;
            UUID targetId = findPlayerInCrosshair(player);
            if (targetId != null) {
                ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                    BrinAbilityC2SPacket.ABILITY_VIGILANTE_DETECT, targetId));
            }
            return;
        }
        if (BrinNoelleAccess.isRole(gameWorld, player, BrinNoelleAccess.NOISEMAKER_ID)) {
            if (BrinNoelleAccess.noelleAbilityCooldown(player) > 0) return;
            UUID targetId = findAimedAnyPlayer(player);
            if (targetId != null) {
                ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                    BrinAbilityC2SPacket.ABILITY_NOISEMAKER_MARK, targetId));
            }
            return;
        }
        if (BrinNoelleAccess.isRole(gameWorld, player, BrinNoelleAccess.INSANE_KILLER_ID)) {
            if (BrinNoelleAccess.noelleAbilityCooldown(player) > 0) return;
            UUID targetId = findAimedAnyPlayer(player);
            if (targetId != null) {
                ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                    BrinAbilityC2SPacket.ABILITY_INSANE_REVIVE, targetId));
            }
            return;
        }
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
            if (GameFunctions.isPlayerAliveAndSurvival(player)
                && cowboy != null
                && !cowboy.duelUsed()) {
                Minecraft.getInstance().setScreen(new LimitedInventoryScreen(player));
            }
            return;
        }
        if (gameWorld.isRole(player, BrinRoles.STALKER)) {
            StalkerComponent stalker = StalkerComponent.KEY.get(player);
            if (stalker == null || stalker.cooldownTicks() > 0) return;
            UUID targetId = findStalkerAimTarget(player);
            if (targetId != null) {
                ClientPlayNetworking.send(new BrinAbilityC2SPacket(
                    BrinAbilityC2SPacket.ABILITY_STALKER_TRACK, targetId));
            }
            return;
        }
        if (ability.cooldown > 0) return;
        if (gameWorld.isRole(player, BrinRoles.TRAPPER)) {
            ClientPlayNetworking.send(new BrinAbilityC2SPacket(BrinAbilityC2SPacket.ABILITY_TRAPPER_PLACE_TRAP, null));
        } else if (gameWorld.isRole(player, BrinRoles.WATCHMAN)) {

            UUID targetId = findAimedLivingPlayer(player);
            if (targetId != null) {
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
    private static void tickInstinctSnapshot(Minecraft client) {
        if (client.player == null || client.getConnection() == null) {
            lastInstinctReported = false;
            BrinInstinctClient.clear();
            return;
        }
        boolean enabled = WatheClient.isInstinctEnabled();
        if (enabled == lastInstinctReported) return;
        lastInstinctReported = enabled;
        if (ClientPlayNetworking.canSend(BrinInstinctC2SPacket.TYPE)) {
            ClientPlayNetworking.send(new BrinInstinctC2SPacket(enabled));
        }
        if (!enabled) BrinInstinctClient.clear();
    }
    private static void tickInstinctNightVision(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            appliedInstinctNightVision = false;
            return;
        }
        boolean shouldApply = WatheClient.isInstinctEnabled() && BrinIcFlags.instinctNightVision;
        if (shouldApply) {
            player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                INSTINCT_NIGHT_VISION_DURATION,
                0,
                false,
                false,
                false
            ));
            appliedInstinctNightVision = true;
            return;
        }
        if (!appliedInstinctNightVision) return;
        MobEffectInstance effect = player.getEffect(MobEffects.NIGHT_VISION);
        if (effect != null
            && effect.getAmplifier() == 0
            && !effect.isAmbient()
            && effect.getDuration() <= INSTINCT_NIGHT_VISION_DURATION) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
        appliedInstinctNightVision = false;
    }
    @Nullable
    private static UUID getNextControlTarget(IllusionistComponent component) {
        if (component.controlledCloneId == null) return component.cloneEntityIds.getFirst();
        int currentIndex = component.cloneEntityIds.indexOf(component.controlledCloneId);
        if (currentIndex < 0 || currentIndex + 1 >= component.cloneEntityIds.size()) return null;
        return component.cloneEntityIds.get(currentIndex + 1);
    }
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
        Component morphName = BrinMorphlingClient.disguiseName(player);
        if (morphName != null) return morphName;
        return null;
    }
    private static Component borrowedBodyName(Player player, @Nullable UUID bodyId) {
        Entity body = findClientEntity(Minecraft.getInstance(), bodyId);
        UUID skinId = body instanceof PlayerBodyEntity playerBody ? playerBody.getPlayerUuid() : null;
        if (skinId == null) return Component.empty();
        if (player.level().getPlayerByUUID(skinId) instanceof Player skinOwner) {
            return skinOwner.getDisplayName();
        }

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
    @Nullable
    private static UUID findStalkerAimTarget(Player localPlayer) {
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
            localPlayer,
            entity -> entity instanceof Player target
                && target != localPlayer
                && GameFunctions.isPlayerAliveAndSurvival(target),
            StalkerComponent.AIM_RANGE
        );
        if (hitResult instanceof EntityHitResult entityHit
            && entityHit.getEntity() instanceof Player targetPlayer
            && StalkerComponent.isAimedAt(localPlayer, targetPlayer)) {
            return targetPlayer.getUUID();
        }
        return null;
    }
    @Nullable
    private static UUID findAimedLivingPlayer(Player localPlayer) {
        UUID raycast = findPlayerInCrosshair(localPlayer);
        if (raycast != null) return raycast;
        Vec3 eyePos = localPlayer.getEyePosition();
        Vec3 look = localPlayer.getLookAngle();
        double bestDot = -2.0;
        UUID bestTarget = null;
        for (Player target : localPlayer.level().players()) {
            if (target == localPlayer || !GameFunctions.isPlayerAliveAndSurvival(target)) continue;
            if (eyePos.distanceToSqr(target.position()) > 64.0D * 64.0D) continue;
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(eyePos);
            if (toTarget.lengthSqr() < 1.0E-6) continue;
            double dot = toTarget.normalize().dot(look);
            if (dot > bestDot && dot > 0.5) {
                bestDot = dot;
                bestTarget = target.getUUID();
            }
        }
        return bestTarget;
    }
    @Nullable
    private static UUID findAimedAnyPlayer(Player localPlayer) {
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
            localPlayer,
            entity -> entity instanceof Player target && target != localPlayer,
            64.0D
        );
        if (hitResult instanceof EntityHitResult entityHit
            && entityHit.getEntity() instanceof Player targetPlayer) {
            return targetPlayer.getUUID();
        }
        Vec3 eyePos = localPlayer.getEyePosition();
        Vec3 look = localPlayer.getLookAngle();
        double bestDot = -2.0;
        UUID bestTarget = null;
        for (Player target : localPlayer.level().players()) {
            if (target == localPlayer) continue;
            if (eyePos.distanceToSqr(target.position()) > 64.0D * 64.0D) continue;
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(eyePos);
            if (toTarget.lengthSqr() < 1.0E-6) continue;
            double dot = toTarget.normalize().dot(look);
            if (dot > bestDot && dot > 0.5) {
                bestDot = dot;
                bestTarget = target.getUUID();
            }
        }
        return bestTarget;
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
