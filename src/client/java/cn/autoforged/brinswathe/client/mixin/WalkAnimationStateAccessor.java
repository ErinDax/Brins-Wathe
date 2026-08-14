package cn.autoforged.brinswathe.client.mixin;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WalkAnimationState.class)
public interface WalkAnimationStateAccessor {
    @Accessor("speedOld")
    float brinGetSpeedOld();

    @Accessor("speedOld")
    void brinSetSpeedOld(float speedOld);

    @Accessor("speed")
    float brinGetSpeed();

    @Accessor("speed")
    void brinSetSpeed(float speed);

    @Accessor("position")
    float brinGetPosition();

    @Accessor("position")
    void brinSetPosition(float position);
}
