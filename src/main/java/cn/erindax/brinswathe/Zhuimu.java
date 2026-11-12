package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.ZhuimuComponent;
import cn.erindax.brinswathe.entity.ZhuimuBoat;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class Zhuimu {
    public static final int HITS_TO_KILL = 3;
    private static final int AXE_COOLDOWN_TICKS = 10;
    private static final double CAPTURE_RADIUS = 1.0D;
    private static final double CAPTURE_HEIGHT = 1.5D;
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
    }

    public static boolean hasFreeHotbarSlot(ServerPlayer player) {
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (player.getInventory().getItem(slot).isEmpty()) return true;
        }
        return false;
    }

    public static boolean placeBoat(ServerPlayer player, Vec3 position) {
        ZhuimuComponent component = ZhuimuComponent.KEY.get(player);
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        if (CowboyDuel.isActive() || component == null || component.hasBoat()
            || game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE
            || !game.isRole(player, ModRoles.ZHUIMU)
            || !GameFunctions.isPlayerAliveAndSurvival(player)) return false;
        ServerLevel level = player.serverLevel();
        ZhuimuBoat boat = ModEntities.ZHUIMU_BOAT.create(level);
        if (boat == null) return false;
        boat.moveTo(position.x, position.y, position.z, player.getYRot(), 0.0F);
        if (!level.noBlockCollision(boat, boat.getBoundingBox())) return false;
        boat.setOwner(player.getUUID());
        if (!level.addFreshEntity(boat)) return false;
        component.trackBoat(boat);
        player.playNotifySound(SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
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
            if (player == owner || player.isPassenger() || player.isSleeping()
                || !GameFunctions.isPlayerAliveAndSurvival(player)) continue;
            double dx = player.getX() - boat.getX();
            double dz = player.getZ() - boat.getZ();
            if (Math.abs(dx) > CAPTURE_RADIUS || Math.abs(dz) > CAPTURE_RADIUS
                || Math.abs(player.getY() - boat.getY()) > CAPTURE_HEIGHT) continue;
            double distance = dx * dx + dz * dz;
            if (distance < closest) {
                closest = distance;
                target = player;
            }
        }
        return target;
    }

    public static void onCaptured(ServerPlayer owner) {
        String key = giveAxe(owner)
            ? "message.brinswathe.zhuimu.captured"
            : "message.brinswathe.zhuimu.captured_hotbar_full";
        owner.displayClientMessage(
            Component.translatable(key, HITS_TO_KILL).withColor(ModRoles.ZHUIMU.color()),
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
        zhuimu.getCooldowns().addCooldown(ModItems.ZHUIMU_AXE, AXE_COOLDOWN_TICKS);
        zhuimu.serverLevel().playSound(
            null, victim.getX(), victim.getEyeY(), victim.getZ(),
            SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 1.0F
        );
        int hits = boat.addHit();
        zhuimu.displayClientMessage(
            Component.translatable("tip.brinswathe.zhuimu.chop", hits, HITS_TO_KILL)
                .withColor(ModRoles.ZHUIMU.color()),
            true
        );
        if (hits < HITS_TO_KILL) return;
        boat.resetHits();
        GameFunctions.killPlayer(victim, true, zhuimu, AXE_DEATH);
    }
}
