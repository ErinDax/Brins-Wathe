package cn.autoforged.brinswathe.item;

import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.component.ZhangshiComponent;
import cn.autoforged.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public final class XueziItem extends Item {
    public XueziItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide
            && (!GameWorldComponent.KEY.get(level).isRole(player, BrinRoles.ZHANGSHI)
                || player.getCooldowns().isOnCooldown(this)
                || ZhangshiComponent.KEY.get(player).isXueziActive())) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player) || level.isClientSide) return stack;
        ZhangshiComponent component = ZhangshiComponent.KEY.get(player);
        if (component == null || !GameWorldComponent.KEY.get(level).isRole(player, BrinRoles.ZHANGSHI)
            || player.getCooldowns().isOnCooldown(this) || !component.activateXuezi()) {
            return stack;
        }
        if (!player.getAbilities().instabuild) stack.shrink(1);
        player.getCooldowns().addCooldown(this, BrinConfig.xueziCooldownSeconds() * 20);
        player.displayClientMessage(Component.translatable("item.brinswathe.xuezi.activated"), true);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.playNotifySound(SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 0.7F);
        }
        return stack;
    }

}
