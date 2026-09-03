package cn.erindax.brinswathe.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.block.SmallDoorBlock;
import dev.doctor4t.wathe.block_entity.SmallDoorBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmallDoorBlock.class)
public abstract class BrinKeyLoreDoorMixin {
    @WrapOperation(
        method = {"useWithoutItem", "onUse"},
        at = @At(value = "INVOKE", target = "Ljava/util/List;getFirst()Ljava/lang/Object;"),
        require = 0
    )
    private Object brinMatchAnyKeyLore(
        List<?> lines,
        Operation<Object> original,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hit
    ) {
        BlockPos lowerPos = state.getValue(SmallDoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockEntity blockEntity = level.getBlockEntity(lowerPos);
        if (blockEntity instanceof SmallDoorBlockEntity door) {
            String keyName = door.getKeyName();
            for (Object line : lines) {
                if (line instanceof Component text && text.getString().equals(keyName)) {
                    return line;
                }
            }
        }
        return original.call(lines);
    }
}
