package cn.erindax.brinswathe.item;

import cn.erindax.brinswathe.BomberBombs;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MineItem extends Item {
    public MineItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer bomber)) return InteractionResult.SUCCESS;

        Vec3 position = context.getClickedFace() == Direction.UP
            ? Vec3.atBottomCenterOf(context.getClickedPos().above())
            : bomber.position();
        if (!plant(bomber, context.getItemInHand(), position)) return InteractionResult.FAIL;
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer bomber)) return InteractionResultHolder.success(stack);
        if (!plant(bomber, stack, bomber.position())) return InteractionResultHolder.fail(stack);
        return InteractionResultHolder.success(stack);
    }

    private static boolean plant(ServerPlayer bomber, ItemStack stack, Vec3 position) {
        if (!BomberBombs.canPlantBombs(bomber)) return false;
        if (!BomberBombs.placeMine(bomber, position)) return false;
        if (!bomber.getAbilities().instabuild) stack.shrink(1);
        return true;
    }
}
