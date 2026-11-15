package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.AbilityCost;
import cn.erindax.brinswathe.BrinsWathe;
import cn.erindax.brinswathe.BuySlots;
import cn.erindax.brinswathe.ModEntities;
import cn.erindax.brinswathe.ModItems;
import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.ZhuimuComponent;
import cn.erindax.brinswathe.config.Tunable;
import cn.erindax.brinswathe.entity.ZhuimuBoat;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.util.ShopEntry;
import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

public final class Zhuimu {
    private static final double CAPTURE_RADIUS = 1.0D;
    private static final double CAPTURE_HEIGHT = 1.5D;
    private static final double SIGHT_HEIGHT = 1.0D;
    private static final double PLACE_TOLERANCE = 1.0E-5D;
    private static final ResourceLocation AXE_DEATH =
        ResourceLocation.fromNamespaceAndPath(BrinsWathe.MOD_ID, "zhuimu_axe");

    private Zhuimu() {
    }

    public static void init() {
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (!(player instanceof ServerPlayer zhuimu) || !zhuimu.getMainHandItem().is(ModItems.ZHUIMU_AXE)) {
                return InteractionResult.PASS;
            }
            chop(zhuimu, entity);
            return InteractionResult.FAIL;
        });
        EntitySleepEvents.ALLOW_SLEEPING.register((player, pos) ->
            player.getVehicle() instanceof ZhuimuBoat ? Player.BedSleepingProblem.OTHER_PROBLEM : null);
    }

    public static boolean allowsFallDeath(Player player) {
        if (!(player.getVehicle() instanceof ZhuimuBoat boat)) return true;
        AABB playArea = MapVariablesWorldComponent.KEY.get(player.level()).getPlayArea();
        return playArea == null || boat.getY() < playArea.minY;
    }

    public static boolean hasFreeHotbarSlot(ServerPlayer player) {
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (player.getInventory().getItem(slot).isEmpty()) return true;
        }
        return false;
    }

    public static void handleBoat(ServerPlayer player, GameWorldComponent gameWorld) {
        if (!gameWorld.isRole(player, ModRoles.ZHUIMU) || !GameFunctions.isPlayerAliveAndSurvival(player)) return;
        ZhuimuComponent zhuimu = ZhuimuComponent.KEY.get(player);
        if (zhuimu == null || zhuimu.used()) return;
        if (!Zhuimu.hasFreeHotbarSlot(player)) {
            player.displayClientMessage(Component.translatable("message.brinswathe.zhuimu.hotbar_full"), true);
            return;
        }
        if (!AbilityCost.spend(player, "zhuimu")) return;
        zhuimu.markUsed();
        BuySlots.beginPurchase(player);
        try {
            ShopEntry.insertStackInFreeSlot(player, ModItems.ZHUIMU_BOAT.getDefaultInstance());
        } finally {
            BuySlots.endPurchase();
        }
        player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static boolean placeBoat(ServerPlayer player, BlockHitResult hitResult) {
        ZhuimuComponent component = ZhuimuComponent.KEY.get(player);
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        if (CowboyDuel.isActive() || component == null || component.hasBoat()
            || game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE
            || !game.isRole(player, ModRoles.ZHUIMU)
            || !GameFunctions.isPlayerAliveAndSurvival(player)) return false;
        ServerLevel level = player.serverLevel();
        ZhuimuBoat boat = ModEntities.ZHUIMU_BOAT.create(level);
        if (boat == null) return false;
        Vec3 spot = hitResult.getDirection() == Direction.UP
            ? findBoatSpot(level, boat, boat.getType().getDimensions(), groundTarget(hitResult))
            : null;
        if (spot == null) {
            player.displayClientMessage(
                Component.translatable("message.brinswathe.zhuimu.no_space").withColor(ModRoles.ZHUIMU.color()),
                true
            );
            return false;
        }
        boat.moveTo(spot.x, spot.y, spot.z, player.getYRot(), 0.0F);
        boat.setOwner(player.getUUID());
        if (!level.addFreshEntity(boat)) return false;
        component.trackBoat(boat);
        player.playNotifySound(SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static Vec3 groundTarget(BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        return new Vec3(pos.getX() + 0.5D, hitResult.getLocation().y, pos.getZ() + 0.5D);
    }

    @Nullable
    public static Vec3 findBoatSpot(CollisionGetter level, @Nullable Entity boat, EntityDimensions size, Vec3 target) {
        if (fits(level, boat, size, target)) return target;
        double halfHeight = size.height() / 2.0D;
        Vec3 center = target.add(0.0D, halfHeight, 0.0D);
        AABB area = AABB.ofSize(center, size.width(), 0.0D, size.width()).inflate(1.0E-6D);
        Optional<Vec3> nudged = level.findFreePosition(boat, Shapes.create(area), center, size.width(), size.height(), size.width())
            .map(free -> free.subtract(0.0D, halfHeight, 0.0D))
            .filter(free -> fits(level, boat, size, free));
        if (nudged.isPresent()) return nudged.get();
        return fits(level, boat, EntityDimensions.scalable(1.0F, size.height()), target) ? target : null;
    }

    private static boolean fits(CollisionGetter level, @Nullable Entity boat, EntityDimensions size, Vec3 position) {
        return level.noBlockCollision(boat, size.makeBoundingBox(position).deflate(PLACE_TOLERANCE));
    }

    public static boolean ownsBoat(ServerPlayer owner, ZhuimuBoat boat) {
        ZhuimuComponent component = ZhuimuComponent.KEY.get(owner);
        GameWorldComponent game = GameWorldComponent.KEY.get(owner.level());
        return owner.level() == boat.level()
            && component != null
            && component.tracks(boat)
            && game.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE
            && game.isRole(owner, ModRoles.ZHUIMU)
            && GameFunctions.isPlayerAliveAndSurvival(owner);
    }

    @Nullable
    public static ServerPlayer findCaptureTarget(ZhuimuBoat boat, ServerPlayer owner) {
        ServerPlayer target = null;
        double closest = Double.MAX_VALUE;
        for (ServerPlayer player : owner.serverLevel().players()) {
            Entity vehicle = player.getVehicle();
            if (player == owner || vehicle instanceof ZhuimuBoat || vehicle instanceof LivingEntity
                || !GameFunctions.isPlayerAliveAndSurvival(player)) continue;
            double dx = player.getX() - boat.getX();
            double dz = player.getZ() - boat.getZ();
            if (Math.abs(dx) > CAPTURE_RADIUS || Math.abs(dz) > CAPTURE_RADIUS
                || Math.abs(player.getY() - boat.getY()) > CAPTURE_HEIGHT) continue;
            double distance = dx * dx + dz * dz;
            if (distance < closest && inSight(boat.level(), boat.position(), player.getEyePosition())) {
                closest = distance;
                target = player;
            }
        }
        return target;
    }

    public static boolean inSight(BlockGetter level, Vec3 boat, Vec3 eye) {
        Vec3 from = boat.add(0.0D, SIGHT_HEIGHT, 0.0D);
        ClipContext context = new ClipContext(from, eye, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
        return level.clip(context).getType() == HitResult.Type.MISS;
    }

    public static void onCaptured(ServerPlayer owner) {
        String key = giveAxe(owner)
            ? "message.brinswathe.zhuimu.captured"
            : "message.brinswathe.zhuimu.captured_hotbar_full";
        owner.displayClientMessage(
            Component.translatable(key, Tunable.ZHUIMU_AXE_HITS.intValue()).withColor(ModRoles.ZHUIMU.color()),
            true
        );
        owner.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void onEscaped(ServerPlayer owner) {
        owner.displayClientMessage(
            Component.translatable("message.brinswathe.zhuimu.escaped").withColor(ModRoles.ZHUIMU.color()),
            true
        );
        removeAxe(owner);
    }

    public static void struggle(ServerPlayer player, boolean right) {
        if (player.getVehicle() instanceof ZhuimuBoat boat) boat.struggle(player, right);
    }

    public static boolean giveAxe(ServerPlayer player) {
        if (player.getInventory().contains(Zhuimu::isAxe) || isAxe(player.containerMenu.getCarried())) return true;
        return ShopEntry.insertStackInFreeSlot(player, ModItems.ZHUIMU_AXE.getDefaultInstance());
    }

    public static void removeAxe(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isAxe(inventory.getItem(slot))) inventory.setItem(slot, ItemStack.EMPTY);
        }
        if (isAxe(player.containerMenu.getCarried())) player.containerMenu.setCarried(ItemStack.EMPTY);
    }

    private static boolean isAxe(ItemStack stack) {
        return stack.is(ModItems.ZHUIMU_AXE);
    }

    private static void chop(ServerPlayer zhuimu, Entity target) {
        if (!(target instanceof ServerPlayer victim) || !(victim.getVehicle() instanceof ZhuimuBoat boat)) return;
        if (!boat.isOwnedBy(zhuimu) || boat.trappedPlayer() != victim) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(zhuimu)
            || zhuimu.getCooldowns().isOnCooldown(ModItems.ZHUIMU_AXE)) return;
        int cooldownTicks = Tunable.ZHUIMU_AXE_COOLDOWN_SECONDS.ticks();
        if (cooldownTicks > 0) zhuimu.getCooldowns().addCooldown(ModItems.ZHUIMU_AXE, cooldownTicks);
        zhuimu.serverLevel().playSound(
            null, victim.getX(), victim.getEyeY(), victim.getZ(),
            SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 1.0F
        );
        int hits = boat.addHit();
        int hitsToKill = Tunable.ZHUIMU_AXE_HITS.intValue();
        zhuimu.displayClientMessage(
            Component.translatable("tip.brinswathe.zhuimu.chop", hits, hitsToKill)
                .withColor(ModRoles.ZHUIMU.color()),
            true
        );
        if (hits < hitsToKill) return;
        boat.resetHits();
        GameFunctions.killPlayer(victim, true, zhuimu, AXE_DEATH);
    }
}
