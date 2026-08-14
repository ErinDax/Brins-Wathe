package cn.autoforged.brinswathe.item;

import cn.autoforged.brinswathe.BomberBombs;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BombItem extends Item {
    public BombItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        if (!(target instanceof Player victim)) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer bomber)) return InteractionResult.SUCCESS;
        if (!BomberBombs.canPlantBombs(bomber)) return InteractionResult.FAIL;
        if (!BomberBombs.attachBomb(bomber, victim)) return InteractionResult.FAIL;

        consume(bomber, stack);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer bomber)) return InteractionResult.SUCCESS;

        // Only a floor gives a predictable spot for the fangs; anything else drops the mine at the
        // bomber's own feet instead of burying it in a wall.
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

        consume(bomber, stack);
        return true;
    }

    private static void consume(ServerPlayer bomber, ItemStack stack) {
        if (!bomber.getAbilities().instabuild) stack.shrink(1);
    }
}
