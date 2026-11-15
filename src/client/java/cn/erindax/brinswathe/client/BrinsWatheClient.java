package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.ModItems;
import cn.erindax.brinswathe.ModSounds;
import cn.erindax.brinswathe.KnifeSkins;
import cn.erindax.brinswathe.client.role.DisarmClient;
import cn.erindax.brinswathe.client.role.IllusionistClient;
import cn.erindax.brinswathe.client.role.PuppetClient;
import cn.erindax.brinswathe.client.role.SniperClient;
import cn.erindax.brinswathe.client.role.StalkerTrackerHud;
import cn.erindax.brinswathe.client.role.ZhuimuClient;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.client.gui.EventLogScreen;
import cn.erindax.brinswathe.client.musicbox.MusicBoxClient;
import cn.erindax.brinswathe.network.ArchivistWitnessS2CPacket;
import cn.erindax.brinswathe.network.BlindFlashS2CPacket;
import cn.erindax.brinswathe.network.ConfigS2CPacket;
import cn.erindax.brinswathe.IcFlags;
import cn.erindax.brinswathe.network.IcNightVisionS2CPacket;
import cn.erindax.brinswathe.network.InstinctC2SPacket;
import cn.erindax.brinswathe.network.InstinctSnapshotS2CPacket;
import cn.erindax.brinswathe.network.KnifeSkinListS2CPacket;
import cn.erindax.brinswathe.network.ResourceReloadS2CPacket;
import cn.erindax.brinswathe.network.CowboyShowdownMusicS2CPacket;
import cn.erindax.brinswathe.network.CowboyDuelHideS2CPacket;
import cn.erindax.brinswathe.network.CowboyDuelIdentityS2CPacket;
import cn.erindax.brinswathe.network.CowboyDuelLookLockS2CPacket;
import cn.erindax.brinswathe.network.RpsStateS2CPacket;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
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

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import org.BsXinQin.kinswathe.client.KinsWatheInitializeClient;
import org.aussiebox.starexpress.client.StarryExpressClient;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BrinsWatheClient implements ClientModInitializer {
    private static boolean abilityKeyWasDown;
    private static int blindFlashTotal;
    private static int blindFlashRemaining;
    private static final Set<UUID> hiddenDuelists = new HashSet<>();
    private static final Map<UUID, Integer> archivistWitnesses = new HashMap<>();
    private static boolean cowboyDuelHidesIdentities;
    private static boolean cowboyDuelLookLocked;
    private static boolean suppressSpectatorRoleHud;
    private static KeyMapping rpsBind;
    private static boolean appliedInstinctNightVision;
    private static final int INSTINCT_NIGHT_VISION_DURATION = 220;
    private static boolean lastInstinctReported;

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (stack.is(ModItems.XUEZI)) {
                int seconds = ModConfig.xueziCooldownSeconds();
                if (Minecraft.getInstance().player != null
                    && Minecraft.getInstance().player.getCooldowns().isOnCooldown(ModItems.XUEZI)) {
                    float percent = Minecraft.getInstance().player.getCooldowns()
                        .getCooldownPercent(ModItems.XUEZI, 0.0F);
                    seconds = Math.max(1, Mth.ceil(percent * ModConfig.xueziCooldownSeconds()));
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
                        String resolved = KnifeSkins.resolveKnifeSkinName(skin);
                        if (KnifeSkins.shouldBrinRender(resolved)) {
                            lines.add(Component.translatable("tip.skin")
                                .append(Component.literal(KnifeSkins.tooltipName(resolved))));
                        }
                    }
                }
            } else if (stack.getItem() instanceof RevolverItem) {
                CustomData data = stack.get(DataComponents.CUSTOM_DATA);
                if (data != null) {
                    var nbt = data.copyTag();
                    if (nbt.contains("wathe_skin")) {
                        String skin = nbt.getString("wathe_skin");
                        String resolved = KnifeSkins.resolveGunSkinName(skin);
                        if (KnifeSkins.shouldBrinRenderGun(resolved)) {
                            lines.add(Component.translatable("tip.skin")
                                .append(Component.literal(KnifeSkins.tooltipGunName(resolved))));
                        }
                    }
                }
            }
        });

        KnifeSkinClient.init();
        SkinUploadClient.init();
        MusicBoxClient.init();
        PersonalSettingsClient.init();
        SkinPickClient.init();
        DraftClient.init();
        SecretMurderClient.init();
        WelcomeClient.init();
        AdminClient.init();
        InstinctClient.init();
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (overlay || !EventLogScreen.isEventLog(message)) return true;
            Minecraft.getInstance().execute(() ->
                Minecraft.getInstance().setScreen(new EventLogScreen(message.copy())));
            return false;
        });

        ClientPlayNetworking.registerGlobalReceiver(BlindFlashS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> startBlindFlash(payload.durationTicks())));
        ClientPlayNetworking.registerGlobalReceiver(ConfigS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> ModConfig.applyServerJson(payload.json())));
        ClientPlayNetworking.registerGlobalReceiver(ResourceReloadS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> {
                if (context.client().player == null || context.client().level == null) return;
                ResourceReloadClient.reloadClientResources(context.client());
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
                InstinctClient.clear();
                lastInstinctReported = false;
                IcFlags.instinctNightVision = true;
            }));

        StalkerTrackerHud.init();
        RpsHud.init();
        FuneralClient.init();
        DisarmClient.init();
        ZhuimuClient.init();
        VagrantClient.init();
        rpsBind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.brinswathe.rps",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.brinswathe"
        ));
        ClientPlayNetworking.registerGlobalReceiver(RpsStateS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> RpsHud.apply(payload)));
        ClientPlayNetworking.registerGlobalReceiver(InstinctSnapshotS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> InstinctClient.apply(payload.entries())));
        ClientPlayNetworking.registerGlobalReceiver(KnifeSkinListS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> KnifeSkinClient.applyRemoteSkins(payload.skins())));
        ClientPlayNetworking.registerGlobalReceiver(IcNightVisionS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> {
                IcFlags.instinctNightVision = payload.enabled();
                IcFlags.instinctHudNamesThroughWalls = payload.hudNamesThroughWalls();
            }));
        ClientPlayNetworking.registerGlobalReceiver(ArchivistWitnessS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> archivistWitnesses.put(payload.target(), Math.max(1, payload.durationTicks()))));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
            client.execute(archivistWitnesses::clear));
        StaminaDiagnosticClient.registerClientStaminaDiagnostic();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (blindFlashRemaining > 0) {
                blindFlashRemaining--;
            }
            if (!archivistWitnesses.isEmpty()) {
                archivistWitnesses.replaceAll((id, ticks) -> ticks - 1);
                archivistWitnesses.values().removeIf(ticks -> ticks <= 0);
            }
            if (rpsBind != null && rpsBind.consumeClick() && client.screen == null) {
                RpsClient.tryInviteRps(client);
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
                        AbilityKeyClient.handleAbilityKeyPress();
                    }
                });
            }
            abilityKeyWasDown = abilityKeyDown;
            IllusionistClient.tickIllusionistControl(client);
            PuppetClient.tickPuppetControl(client);
            SniperClient.tickSniperAiming(client);
            DisguiseClient.tickMorticianDisguiseSkins(client);
            tickInstinctNightVision(client);
            tickInstinctSnapshot(client);
        });
    }

    public static void startBlindFlash(int durationTicks) {
        blindFlashTotal = Math.max(durationTicks, 1);
        blindFlashRemaining = blindFlashTotal;
    }
    private static void handleShowdownMusic(boolean play, float volume) {
        Minecraft client = Minecraft.getInstance();
        var id = ModSounds.COWBOY_SHOWDOWN.getLocation();

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
    public static boolean isArchivistWitness(Entity entity) {
        if (!(entity instanceof Player target) || archivistWitnesses.isEmpty()) return false;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return false;
        if (!GameWorldComponent.KEY.get(client.level).isRole(client.player, ModRoles.ARCHIVIST)) return false;
        return archivistWitnesses.containsKey(target.getUUID());
    }
    @Nullable
    public static KeyMapping getAbilityBind() {
        return KinsWatheInitializeClient.abilityBind;
    }
    public static KeyMapping rpsBind() {
        return rpsBind;
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

    private static void tickInstinctSnapshot(Minecraft client) {
        if (client.player == null || client.getConnection() == null) {
            lastInstinctReported = false;
            InstinctClient.clear();
            return;
        }
        boolean enabled = WatheClient.isInstinctEnabled();
        if (enabled == lastInstinctReported) return;
        lastInstinctReported = enabled;
        if (ClientPlayNetworking.canSend(InstinctC2SPacket.TYPE)) {
            ClientPlayNetworking.send(new InstinctC2SPacket(enabled));
        }
        if (!enabled) InstinctClient.clear();
    }
    private static void tickInstinctNightVision(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            appliedInstinctNightVision = false;
            return;
        }
        boolean shouldApply = WatheClient.isInstinctEnabled() && IcFlags.instinctNightVision;
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
}
