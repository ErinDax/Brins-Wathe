package cn.autoforged.brinswathe.item;

import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.config.BrinConfig;
import cn.autoforged.brinswathe.component.BoneharvesterComponent;
import cn.autoforged.brinswathe.entity.BoneharvestedCorpse;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BoneKnifeItem extends Item {
    public BoneKnifeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof PlayerBodyEntity body)) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(serverPlayer.level());
        if (!gameWorld.isRole(serverPlayer, BrinRoles.BONEHARVESTER)) return InteractionResult.FAIL;
        if (serverPlayer.getCooldowns().isOnCooldown(this)) return InteractionResult.FAIL;

        BoneharvesterComponent component = BoneharvesterComponent.KEY.get(serverPlayer);
        if (component == null) return InteractionResult.FAIL;

        BoneharvestedCorpse processed = (BoneharvestedCorpse) body;
        if (processed.brin$isBoneharvested()) return InteractionResult.FAIL;

        if (!component.applyBoneShield()) return InteractionResult.FAIL;
        processed.brin$setBoneharvested(true);
        serverPlayer.getCooldowns().addCooldown(this, BrinConfig.boneKnifeCooldownSeconds() * 20);
        serverPlayer.playNotifySound(SoundEvents.ARMOR_EQUIP_CHAIN.value(), SoundSource.PLAYERS, 1.0F, 1.15F);
        serverPlayer.displayClientMessage(
            Component.translatable("item.brinswathe.bone_knife.used").withStyle(ChatFormatting.GRAY),
            true
        );
        return InteractionResult.SUCCESS;
    }
}
