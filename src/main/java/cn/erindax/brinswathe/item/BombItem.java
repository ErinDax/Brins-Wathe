package cn.erindax.brinswathe.item;

import cn.erindax.brinswathe.BomberBombs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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

    private static void consume(ServerPlayer bomber, ItemStack stack) {
        if (!bomber.getAbilities().instabuild) stack.shrink(1);
    }
}
