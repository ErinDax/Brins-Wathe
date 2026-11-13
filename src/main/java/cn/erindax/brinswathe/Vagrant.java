package cn.erindax.brinswathe;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class Vagrant {
    public static final int COLOR = 0xB08D57;
    private static final String IMMERSIVE_FURNITURE = "immersive_furniture";
    private static final double GROUND_TOLERANCE = 0.1D;
    private static final double FLOOR_PROBE = 0.05D;
    private static final double LEG_REACH = 1.5D;
    private static final double BODY_HALF_WIDTH = 0.3D;
    private static final double BODY_BOTTOM = 0.1D;
    private static final double BODY_TOP = 0.4D;

    private Vagrant() {
    }

    public static boolean canLieDown(Player player, BlockHitResult hitResult) {
        return player.isShiftKeyDown()
            && player.getMainHandItem().isEmpty()
            && hitResult.getDirection() == Direction.UP
            && hitResult.getLocation().y <= player.getY() + GROUND_TOLERANCE
            && !player.level().getBlockState(hitResult.getBlockPos()).hasBlockEntity()
            && player.onGround()
            && !player.isSleeping()
            && !player.isPassenger()
            && !CowboyDuel.isActive()
            && !Funeral.isCarrying(player)
            && GameFunctions.isPlayerAliveAndSurvival(player)
            && GameWorldComponent.KEY.get(player.level()).getGameStatus() == GameWorldComponent.GameStatus.ACTIVE
            && isGroundSleep(player, player.blockPosition())
            && hasFloor(player);
    }

    public static boolean lieDown(ServerPlayer player, BlockHitResult hitResult) {
        if (!canLieDown(player, hitResult)) return false;
        float yaw = legsDirection(player).toYRot();
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.setYBodyRot(yaw);
        player.startSleeping(player.blockPosition());
        player.serverLevel().updateSleepingPlayerList();
        player.connection.teleport(player.getX(), player.getY(), player.getZ(), yaw, 0.0F);
        return true;
    }

    public static boolean isGroundSleep(Player player, BlockPos pos) {
        BlockState state = player.level().getBlockState(pos);
        return ModModifiers.hasModifier(player, ModModifiers.VAGRANT)
            && !state.hasBlockEntity()
            && !IMMERSIVE_FURNITURE.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace());
    }

    public static boolean keepsGroundSleep(Player player) {
        return GameFunctions.isPlayerAliveAndSurvival(player)
            && !player.isPassenger()
            && player.getSleepingPos().filter(pos -> isGroundSleep(player, pos)).isPresent();
    }

    private static boolean hasFloor(Player player) {
        AABB body = player.getDimensions(Pose.SLEEPING).makeBoundingBox(player.position());
        return !player.level().noBlockCollision(player, body.move(0.0D, -FLOOR_PROBE, 0.0D));
    }

    private static Direction legsDirection(Player player) {
        Direction facing = player.getDirection();
        for (Direction legs : List.of(facing, facing.getClockWise(), facing.getCounterClockWise(), facing.getOpposite())) {
            if (hasRoomForLegs(player, legs)) return legs;
        }
        return facing;
    }

    private static boolean hasRoomForLegs(Player player, Direction legs) {
        Vec3 pos = player.position();
        double sideX = legs.getAxis() == Direction.Axis.X ? 0.0D : BODY_HALF_WIDTH;
        double sideZ = legs.getAxis() == Direction.Axis.Z ? 0.0D : BODY_HALF_WIDTH;
        AABB body = new AABB(
            pos.x, pos.y + BODY_BOTTOM, pos.z,
            pos.x + legs.getStepX() * LEG_REACH, pos.y + BODY_TOP, pos.z + legs.getStepZ() * LEG_REACH
        ).inflate(sideX, 0.0D, sideZ);
        return player.level().noBlockCollision(player, body);
    }
}
