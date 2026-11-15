package cn.erindax.brinswathe.item;

import cn.erindax.brinswathe.role.Zhuimu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ZhuimuBoatItem extends Item {
    public ZhuimuBoatItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitResult.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);
        if (!(player instanceof ServerPlayer zhuimu)) return InteractionResultHolder.success(stack);
        if (!Zhuimu.placeBoat(zhuimu, hitResult)) return InteractionResultHolder.fail(stack);
        stack.consume(1, zhuimu);
        return InteractionResultHolder.success(stack);
    }
}
