package cn.autoforged.brinswathe.component;

import cn.autoforged.brinswathe.config.BrinConfig;
import cn.autoforged.brinswathe.BrinModifiers;
import cn.autoforged.brinswathe.CowboyDuel;
import cn.autoforged.brinswathe.entity.PuppetEntity;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheEntities;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheParticles;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.util.ShopEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class PuppeteerControlComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<PuppeteerControlComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath("brinswathe", "puppeteer_control"),
        PuppeteerControlComponent.class
    );

    public static final EntityDimensions PUPPET_MODEL_DIMENSIONS = EntityDimensions.scalable(0.6F, 1.8F);

    private static final int GLOW_TICKS = 200;
    private static final int KILLED_SLOWNESS_TICKS = 300;
    private static final int PARTICLE_INTERVAL_TICKS = 8;
    private static final double SELF_DESTRUCT_SIZE = 3.0D;
    private static final double SUMMON_OFFSET = 1.0D;
    private static final String PUPPET_GEAR_NBT = "BrinPuppetGear";

    private enum ControlEnd {
        RETURNED,
        EXPIRED,
        KILLED,
        OWNER_LOST,
        RESET
    }

    private final Player player;

    public List<UUID> storedPuppets = new ArrayList<>();
    public int craftCooldownTicks;
    public int puppetTicks;
    public boolean selfDestructArmed;
    @Nullable
    public UUID puppetEntityId;
    @Nullable
    public UUID bodyProxyEntityId;
    @Nullable
    private Vec3 controlAnchor;
    private int glowTicks;

    public PuppeteerControlComponent(Player player) {
        this.player = player;
    }

    public boolean isControlling() {
        return this.puppetEntityId != null;
    }

    @Override
    public void serverTick() {
        // Frozen while a cowboy duel benches the crowd as spectators; nothing may reset or tick down.
        if (CowboyDuel.isActive()) return;
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;

        if (!GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
            if (this.isControlling() || this.bodyProxyEntityId != null || this.glowTicks > 0) {
                this.endControl(serverPlayer, ControlEnd.OWNER_LOST);
            }
            return;
        }

        this.tickGlow(serverPlayer);
        this.tickCraftCooldown();

        if (!this.isControlling()) return;

        PlayerBodyEntity puppet = this.puppetEntity(serverPlayer);
        if (puppet == null) {
            this.endControl(serverPlayer, ControlEnd.EXPIRED);
            return;
        }

        this.syncPuppetWithOwner(serverPlayer, puppet);
        this.spawnBodyProxy();

        this.puppetTicks--;
        this.spawnDecayParticles(serverPlayer, puppet);
        if (this.puppetTicks <= 0) {
            this.endControl(serverPlayer, ControlEnd.EXPIRED);
            return;
        }
        if (this.puppetTicks % 20 == 0) this.sync();
    }

    /**
     * Turns a corpse into a stored puppet. The corpse itself is consumed so a single body can never be
     * both evidence on the floor and a puppet in the puppeteer's pocket.
     */
    public boolean craftPuppet(PlayerBodyEntity corpse) {
        UUID skinId = corpse.getPlayerUuid();
        if (skinId == null || this.storedPuppets.contains(skinId)) return false;

        corpse.discard();
        this.storedPuppets.add(skinId);
        // Crafting has its own countdown outside kinswathe's component, so FAST2FAST is honoured here.
        this.craftCooldownTicks = BrinModifiers.hasModifier(this.player, BrinModifiers.FAST2FAST)
            ? 0
            : Math.max(0, BrinConfig.skillCooldownSeconds("puppeteer")) * 20;
        this.sync();
        return true;
    }

    public boolean summonPuppet(ServerPlayer serverPlayer, UUID skinId) {
        if (this.isControlling() || !this.storedPuppets.remove(skinId)) return false;

        Vec3 anchor = serverPlayer.position();
        Vec3 spawn = summonPosition(serverPlayer, anchor);
        PlayerBodyEntity puppet = new PlayerBodyEntity(WatheEntities.PLAYER_BODY, serverPlayer.level());
        PuppetEntity puppetData = (PuppetEntity) puppet;
        puppetData.brin$setPuppet(true);
        puppetData.brin$setPuppeteer(serverPlayer.getUUID());
        puppet.refreshDimensions();
        puppet.setPlayerUuid(skinId);
        puppet.setSilent(true);
        puppet.setInvulnerable(true);
        puppet.setNoGravity(true);
        puppet.noPhysics = true;
        puppet.setPos(spawn.x, spawn.y, spawn.z);
        puppet.setYRot(serverPlayer.getYRot());
        puppet.setYBodyRot(serverPlayer.yBodyRot);
        puppet.setYHeadRot(serverPlayer.getYHeadRot());
        puppet.setXRot(serverPlayer.getXRot());
        puppet.setDeltaMovement(Vec3.ZERO);
        if (!serverPlayer.level().addFreshEntity(puppet)) {
            this.storedPuppets.add(skinId);
            return false;
        }

        this.puppetEntityId = puppet.getUUID();
        this.controlAnchor = anchor;
        this.puppetTicks = Math.max(1, BrinConfig.skillDurationSeconds("puppeteer")) * 20;
        this.spawnBodyProxy();
        this.teleportOwnerToPuppet(serverPlayer, puppet);
        this.givePuppetGear(serverPlayer);
        this.sync();
        return true;
    }

    public void returnToBody(ServerPlayer serverPlayer) {
        if (!this.isControlling()) return;
        this.endControl(serverPlayer, ControlEnd.RETURNED);
    }

    /**
     * Only a kill dealt by somebody carries the slowdown penalty; losing the puppet to the world is treated
     * like the timer running out.
     */
    public void handlePuppetKilled(ServerPlayer serverPlayer, @Nullable Entity killer) {
        if (!this.isControlling()) return;
        boolean murdered = killer instanceof Player attacker && attacker != serverPlayer;
        this.endControl(serverPlayer, murdered ? ControlEnd.KILLED : ControlEnd.EXPIRED);
    }

    /**
     * Called when the stand-in left at the anchor is destroyed. Control has to be torn down before the
     * owner is killed, otherwise the kill interception would bounce the death back as a puppet loss.
     */
    public void handleBodyProxyDestroyed(ServerPlayer serverPlayer) {
        this.endControl(serverPlayer, ControlEnd.OWNER_LOST);
    }

    private void endControl(ServerPlayer serverPlayer, ControlEnd reason) {
        boolean hadPuppet = this.isControlling();
        PlayerBodyEntity puppet = this.puppetEntity(serverPlayer);
        Vec3 puppetPosition = puppet == null ? serverPlayer.position() : puppet.position();
        if (puppet != null) puppet.discard();

        this.puppetEntityId = null;
        this.puppetTicks = 0;
        this.removePuppetGear(serverPlayer);
        this.removeBodyProxy(serverPlayer);
        boolean survives = reason != ControlEnd.OWNER_LOST && reason != ControlEnd.RESET;
        if (survives) this.restoreOwnerToAnchor(serverPlayer);
        this.controlAnchor = null;

        if (hadPuppet && this.selfDestructArmed && reason != ControlEnd.RESET) {
            this.selfDestructArmed = false;
            selfDestruct(serverPlayer, puppetPosition);
        }

        if (hadPuppet && survives) {
            this.startGlow(serverPlayer);
            if (reason == ControlEnd.KILLED) {
                serverPlayer.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, KILLED_SLOWNESS_TICKS, 1, false, true, true));
            }
            AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(serverPlayer);
            if (ability != null) {
                ability.setAbilityCooldown(BrinConfig.puppeteerReturnCooldownSeconds());
            }
            // Only the puppeteer hears their own casting, otherwise the sound gives away where the real
            // body is standing.
            serverPlayer.playNotifySound(SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.6F);
        } else {
            this.clearGlow(serverPlayer);
        }
        this.sync();
    }

    private static Vec3 summonPosition(ServerPlayer serverPlayer, Vec3 anchor) {
        Vec3 look = serverPlayer.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0.0D, look.z);
        if (flat.lengthSqr() < 1.0E-6) return anchor;

        Vec3 candidate = anchor.add(flat.normalize().scale(SUMMON_OFFSET));
        AABB box = PUPPET_MODEL_DIMENSIONS.makeBoundingBox(candidate);
        return serverPlayer.level().noCollision(box) ? candidate : anchor;
    }

    private void syncPuppetWithOwner(ServerPlayer serverPlayer, PlayerBodyEntity puppet) {
        puppet.setNoGravity(true);
        puppet.noPhysics = true;
        puppet.setPos(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
        puppet.setYRot(serverPlayer.getYRot());
        puppet.setYBodyRot(serverPlayer.yBodyRot);
        puppet.setYHeadRot(serverPlayer.getYHeadRot());
        puppet.setXRot(serverPlayer.getXRot());
        puppet.setDeltaMovement(Vec3.ZERO);
        puppet.hasImpulse = true;
    }

    /**
     * The tell that lets survivors call out a puppet which has been walking around for too long.
     */
    private void spawnDecayParticles(ServerPlayer serverPlayer, PlayerBodyEntity puppet) {
        int delaySeconds = BrinConfig.puppeteerParticleDelaySeconds();
        if (delaySeconds < 0) return;

        int total = Math.max(1, BrinConfig.skillDurationSeconds("puppeteer")) * 20;
        if (total - this.puppetTicks < delaySeconds * 20) return;
        if (this.puppetTicks % PARTICLE_INTERVAL_TICKS != 0) return;

        serverPlayer.serverLevel().sendParticles(
            ParticleTypes.ANGRY_VILLAGER,
            puppet.getX(),
            puppet.getY() + 2.0D,
            puppet.getZ(),
            2,
            0.25D,
            0.25D,
            0.25D,
            0.0D
        );
    }

    private void teleportOwnerToPuppet(ServerPlayer serverPlayer, PlayerBodyEntity puppet) {
        serverPlayer.connection.teleport(
            puppet.getX(),
            puppet.getY(),
            puppet.getZ(),
            puppet.getYRot(),
            puppet.getXRot()
        );
        serverPlayer.setDeltaMovement(Vec3.ZERO);
        serverPlayer.hurtMarked = true;
    }

    private void restoreOwnerToAnchor(ServerPlayer serverPlayer) {
        if (this.controlAnchor == null) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) return;

        serverPlayer.connection.teleport(
            this.controlAnchor.x,
            this.controlAnchor.y,
            this.controlAnchor.z,
            serverPlayer.getYRot(),
            serverPlayer.getXRot()
        );
        serverPlayer.setDeltaMovement(Vec3.ZERO);
        serverPlayer.hurtMarked = true;
    }

    private void spawnBodyProxy() {
        if (!(this.player instanceof ServerPlayer serverPlayer) || this.controlAnchor == null) return;
        if (this.bodyProxyEntityId != null
            && serverPlayer.serverLevel().getEntity(this.bodyProxyEntityId) instanceof PlayerBodyEntity) return;

        PlayerBodyEntity bodyProxy = new PlayerBodyEntity(WatheEntities.PLAYER_BODY, serverPlayer.level());
        PuppetEntity proxyData = (PuppetEntity) bodyProxy;
        proxyData.brin$setPuppetBodyProxy(true);
        proxyData.brin$setPuppeteer(serverPlayer.getUUID());
        bodyProxy.refreshDimensions();
        bodyProxy.setPlayerUuid(serverPlayer.getUUID());
        bodyProxy.setNoGravity(true);
        bodyProxy.noPhysics = false;
        bodyProxy.setSilent(true);
        bodyProxy.setPos(this.controlAnchor.x, this.controlAnchor.y, this.controlAnchor.z);
        bodyProxy.setYRot(serverPlayer.getYRot());
        bodyProxy.setYBodyRot(serverPlayer.yBodyRot);
        bodyProxy.setYHeadRot(serverPlayer.getYHeadRot());
        bodyProxy.setXRot(serverPlayer.getXRot());
        bodyProxy.setDeltaMovement(Vec3.ZERO);
        if (serverPlayer.level().addFreshEntity(bodyProxy)) {
            this.bodyProxyEntityId = bodyProxy.getUUID();
        }
    }

    private void removeBodyProxy(ServerPlayer serverPlayer) {
        if (this.bodyProxyEntityId == null) return;
        for (ServerLevel level : serverPlayer.server.getAllLevels()) {
            Entity entity = level.getEntity(this.bodyProxyEntityId);
            if (entity == null) continue;
            entity.discard();
            break;
        }
        this.bodyProxyEntityId = null;
    }

    @Nullable
    private PlayerBodyEntity puppetEntity(ServerPlayer serverPlayer) {
        if (this.puppetEntityId == null) return null;
        return serverPlayer.serverLevel().getEntity(this.puppetEntityId) instanceof PlayerBodyEntity puppet
            ? puppet
            : null;
    }

    private void startGlow(ServerPlayer serverPlayer) {
        this.glowTicks = GLOW_TICKS;
        serverPlayer.setGlowingTag(true);
    }

    private void tickGlow(ServerPlayer serverPlayer) {
        if (this.glowTicks <= 0) return;
        this.glowTicks--;
        if (this.glowTicks <= 0) serverPlayer.setGlowingTag(false);
    }

    private void clearGlow(ServerPlayer serverPlayer) {
        if (this.glowTicks <= 0) return;
        this.glowTicks = 0;
        serverPlayer.setGlowingTag(false);
    }

    private void tickCraftCooldown() {
        if (this.craftCooldownTicks <= 0) return;
        if (BrinModifiers.hasModifier(this.player, BrinModifiers.FAST2FAST)) {
            this.craftCooldownTicks = 0;
            this.sync();
            return;
        }
        this.craftCooldownTicks--;
        if (this.craftCooldownTicks % 20 == 0) this.sync();
    }

    private void givePuppetGear(ServerPlayer serverPlayer) {
        ShopEntry.insertStackInFreeSlot(serverPlayer, puppetGear(WatheItems.KNIFE));
        ShopEntry.insertStackInFreeSlot(serverPlayer, puppetGear(WatheItems.CROWBAR));
    }

    private void removePuppetGear(ServerPlayer serverPlayer) {
        for (int slot = 0; slot < serverPlayer.getInventory().getContainerSize(); slot++) {
            if (isPuppetGear(serverPlayer.getInventory().getItem(slot))) {
                serverPlayer.getInventory().setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    private static ItemStack puppetGear(Item item) {
        ItemStack stack = item.getDefaultInstance();
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(PUPPET_GEAR_NBT, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    private static boolean isPuppetGear(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean(PUPPET_GEAR_NBT);
    }

    private static void selfDestruct(ServerPlayer owner, Vec3 center) {
        ServerLevel level = owner.serverLevel();
        level.playSound(
            null,
            center.x,
            center.y,
            center.z,
            WatheSounds.ITEM_GRENADE_EXPLODE,
            SoundSource.PLAYERS,
            5.0F,
            1.0F + owner.getRandom().nextFloat() * 0.1F - 0.05F
        );
        level.sendParticles(WatheParticles.BIG_EXPLOSION, center.x, center.y + 0.1D, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y + 0.1D, center.z, 100, 0.0D, 0.0D, 0.0D, 0.2D);
        level.sendParticles(
            new ItemParticleOption(ParticleTypes.ITEM, WatheItems.THROWN_GRENADE.getDefaultInstance()),
            center.x,
            center.y + 0.1D,
            center.z,
            100,
            0.0D,
            0.0D,
            0.0D,
            1.0D
        );

        AABB bounds = AABB.ofSize(center, SELF_DESTRUCT_SIZE, SELF_DESTRUCT_SIZE, SELF_DESTRUCT_SIZE);
        List<ServerPlayer> victims = level.getPlayers(target ->
            target != owner
                && GameFunctions.isPlayerAliveAndSurvival(target)
                && bounds.contains(target.position())
        );
        for (ServerPlayer victim : victims) {
            GameFunctions.killPlayer(victim, true, owner, GameConstants.DeathReasons.GRENADE);
        }
    }

    public static boolean isPuppet(PlayerBodyEntity entity) {
        return ((PuppetEntity) entity).brin$isPuppet();
    }

    public static boolean isPuppetBodyProxy(PlayerBodyEntity entity) {
        PuppetEntity puppetData = (PuppetEntity) entity;
        return puppetData.brin$isPuppetBodyProxy() && !puppetData.brin$isPuppet();
    }

    public static boolean isPuppetModel(PlayerBodyEntity entity) {
        return isPuppet(entity) || isPuppetBodyProxy(entity);
    }

    @Nullable
    public static Player puppeteerOf(PlayerBodyEntity entity) {
        UUID puppeteerId = ((PuppetEntity) entity).brin$getPuppeteer();
        return puppeteerId == null ? null : entity.level().getPlayerByUUID(puppeteerId);
    }

    /**
     * Shared entry point for the weapon payloads: a hit on the puppet only cancels the possession, while a
     * hit on the stand-in is a real kill on the puppeteer.
     */
    public static boolean resolveWeaponHit(PlayerBodyEntity body, @Nullable ServerPlayer attacker,
                                           ResourceLocation deathReason) {
        if (!isPuppetModel(body)) return false;
        if (!(puppeteerOf(body) instanceof ServerPlayer puppeteer)) {
            body.discard();
            return true;
        }

        PuppeteerControlComponent component = KEY.get(puppeteer);
        if (component == null) {
            body.discard();
            return true;
        }
        if (isPuppet(body)) {
            component.handlePuppetKilled(puppeteer, attacker);
        } else {
            component.handleBodyProxyDestroyed(puppeteer);
            GameFunctions.killPlayer(puppeteer, true, attacker == null ? puppeteer : attacker, deathReason);
        }
        return true;
    }

    public void reset() {
        if (this.player instanceof ServerPlayer serverPlayer) {
            this.endControl(serverPlayer, ControlEnd.RESET);
        }
        this.storedPuppets.clear();
        this.craftCooldownTicks = 0;
        this.selfDestructArmed = false;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        this.craftCooldownTicks = tag.getInt("craftCooldownTicks");
        this.puppetTicks = tag.getInt("puppetTicks");
        this.glowTicks = tag.getInt("glowTicks");
        this.selfDestructArmed = tag.getBoolean("selfDestructArmed");
        this.puppetEntityId = tag.hasUUID("puppetEntityId") ? tag.getUUID("puppetEntityId") : null;
        this.bodyProxyEntityId = tag.hasUUID("bodyProxyEntityId") ? tag.getUUID("bodyProxyEntityId") : null;
        this.controlAnchor = tag.contains("controlAnchorX")
            ? new Vec3(
                tag.getDouble("controlAnchorX"),
                tag.getDouble("controlAnchorY"),
                tag.getDouble("controlAnchorZ")
            )
            : null;
        this.storedPuppets.clear();
        ListTag stored = tag.getList("storedPuppets", Tag.TAG_COMPOUND);
        for (int index = 0; index < stored.size(); index++) {
            CompoundTag entry = stored.getCompound(index);
            if (entry.hasUUID("uuid")) this.storedPuppets.add(entry.getUUID("uuid"));
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryAccess) {
        tag.putInt("craftCooldownTicks", this.craftCooldownTicks);
        tag.putInt("puppetTicks", this.puppetTicks);
        tag.putInt("glowTicks", this.glowTicks);
        tag.putBoolean("selfDestructArmed", this.selfDestructArmed);
        if (this.puppetEntityId != null) tag.putUUID("puppetEntityId", this.puppetEntityId);
        if (this.bodyProxyEntityId != null) tag.putUUID("bodyProxyEntityId", this.bodyProxyEntityId);
        if (this.controlAnchor != null) {
            tag.putDouble("controlAnchorX", this.controlAnchor.x);
            tag.putDouble("controlAnchorY", this.controlAnchor.y);
            tag.putDouble("controlAnchorZ", this.controlAnchor.z);
        }
        ListTag stored = new ListTag();
        for (UUID puppetId : this.storedPuppets) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", puppetId);
            stored.add(entry);
        }
        tag.put("storedPuppets", stored);
    }
}
