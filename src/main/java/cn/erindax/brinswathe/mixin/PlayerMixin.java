package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinIcFlags;
import cn.erindax.brinswathe.BrinModifiers;
import cn.erindax.brinswathe.BrinsWathe;
import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.component.StaminaComponent;
import cn.erindax.brinswathe.component.ZhangshiComponent;
import cn.erindax.brinswathe.config.BrinConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.lang.reflect.Field;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, priority = 3000)
public abstract class PlayerMixin {
	@Unique
	private static Field brin$sprintingTicksField;
	@Unique
	private static boolean brin$sprintingTicksLookupFailed;

    @Inject(method = "tick", at = @At("HEAD"))
    private void brinOnPlayerTick(CallbackInfo ci) {
        Player self = (Player) (Object) this;
		StaminaComponent stamina = StaminaComponent.KEY.get(self);
		if (brin$usesGameStamina(self) && stamina != null) {
			brin$setWatheSprintTicks(self, stamina.currentStamina > 0
				? stamina.currentStamina + 1.0F
				: 0.0F);
		}
    }

	@Inject(method = "tick", at = @At("TAIL"))
	private void brinAfterPlayerTick(CallbackInfo ci) {
		Player self = (Player) (Object) this;
		StaminaComponent stamina = StaminaComponent.KEY.get(self);
		if (!brin$usesGameStamina(self) || stamina == null) return;

		if (!self.level().isClientSide) {
			stamina.tickGameStamina(self.isSprinting());
		}
		brin$setWatheSprintTicks(self, stamina.currentStamina);
	}

	@ModifyReturnValue(method = "getSpeed", at = @At("RETURN"), order = 10000)
	private float brinApplyConfiguredRunSpeed(float original) {
		Player self = (Player) (Object) this;
		float speed = original;

		if (self.isSprinting() && StaminaComponent.usesRunSpeedBonus(self)) {
			StaminaComponent stamina = StaminaComponent.KEY.get(self);
			if (stamina != null) speed *= 1.0F + stamina.runSpeed;
		}

		GameWorldComponent gameWorld = GameWorldComponent.KEY.get(self.level());
		ZhangshiComponent zhangshi = ZhangshiComponent.KEY.get(self);
		if (gameWorld != null
			&& gameWorld.isRole(self, BrinRoles.ZHANGSHI)
			&& zhangshi != null
			&& zhangshi.isSpeedActive()) {
			speed *= BrinConfig.zhangshiRunSpeed();
		}
		if (BrinModifiers.hasModifier(self, BrinModifiers.SPEED)) {
			MobEffectInstance potion = self.getEffect(MobEffects.MOVEMENT_SPEED);
			if (potion != null) {
				speed *= 1.0F + 0.2F * (potion.getAmplifier() + 1);
			}
		}
		if (BrinIcFlags.size2speed) {
			double scale = self.getAttributeValue(Attributes.SCALE);
			speed *= (float) (scale < 1.0 ? Math.pow(scale, 0.6) : Math.pow(scale, 1.44));
		}
		return speed;
	}

	@Unique
	private static boolean brin$usesGameStamina(Player player) {
		return StaminaComponent.usesGameStamina(player);
	}

	@Unique
	private static void brin$setWatheSprintTicks(Player player, float value) {
		if (brin$sprintingTicksLookupFailed) return;
		try {
			if (brin$sprintingTicksField == null) {
				brin$sprintingTicksField = Player.class.getDeclaredField("sprintingTicks");
				brin$sprintingTicksField.setAccessible(true);
			}
			brin$sprintingTicksField.setFloat(player, value);
		} catch (ReflectiveOperationException exception) {
			brin$sprintingTicksLookupFailed = true;
			BrinsWathe.LOGGER.error("Failed to access Wathe sprint state: {}", exception.getMessage());
		}
	}
}
