package cn.autoforged.brinswathe;

import cn.autoforged.brinswathe.component.ArchivistComponent;
import cn.autoforged.brinswathe.component.AvengerComponent;
import cn.autoforged.brinswathe.component.BerserkerComponent;
import cn.autoforged.brinswathe.component.BombComponent;
import cn.autoforged.brinswathe.component.BoneharvesterComponent;
import cn.autoforged.brinswathe.component.BrinCustomWinnerComponent;
import cn.autoforged.brinswathe.component.CompensatorComponent;
import cn.autoforged.brinswathe.component.CowboyComponent;
import cn.autoforged.brinswathe.component.EavesdropperComponent;
import cn.autoforged.brinswathe.component.GamblerComponent;
import cn.autoforged.brinswathe.component.IllusionistComponent;
import cn.autoforged.brinswathe.component.MediumComponent;
import cn.autoforged.brinswathe.component.MorticianComponent;
import cn.autoforged.brinswathe.component.NightmareComponent;
import cn.autoforged.brinswathe.component.PenitentComponent;
import cn.autoforged.brinswathe.component.PuppeteerControlComponent;
import cn.autoforged.brinswathe.component.SniperComponent;
import cn.autoforged.brinswathe.component.StalkerComponent;
import cn.autoforged.brinswathe.component.StaminaComponent;
import cn.autoforged.brinswathe.component.StuntDoubleComponent;
import cn.autoforged.brinswathe.component.TrapperComponent;
import cn.autoforged.brinswathe.component.WatchmanComponent;
import cn.autoforged.brinswathe.component.ZhangshiComponent;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

public class BrinComponents implements EntityComponentInitializer, WorldComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(Player.class, StaminaComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(StaminaComponent::new);
        registry.beginRegistration(Player.class, PuppeteerControlComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(PuppeteerControlComponent::new);
        registry.beginRegistration(Player.class, StuntDoubleComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(StuntDoubleComponent::new);
        registry.beginRegistration(Player.class, MediumComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(MediumComponent::new);
        registry.beginRegistration(Player.class, EavesdropperComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(EavesdropperComponent::new);
        registry.beginRegistration(Player.class, WatchmanComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(WatchmanComponent::new);
        registry.beginRegistration(Player.class, TrapperComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(TrapperComponent::new);
        registry.beginRegistration(Player.class, NightmareComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(NightmareComponent::new);
        registry.beginRegistration(Player.class, IllusionistComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(IllusionistComponent::new);
        registry.beginRegistration(Player.class, BerserkerComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(BerserkerComponent::new);
        registry.beginRegistration(Player.class, ArchivistComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(ArchivistComponent::new);
        registry.beginRegistration(Player.class, BoneharvesterComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(BoneharvesterComponent::new);
        registry.beginRegistration(Player.class, CompensatorComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(CompensatorComponent::new);
        registry.beginRegistration(Player.class, GamblerComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(GamblerComponent::new);
        registry.beginRegistration(Player.class, PenitentComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(PenitentComponent::new);
        registry.beginRegistration(Player.class, SniperComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(SniperComponent::new);
        registry.beginRegistration(Player.class, ZhangshiComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(ZhangshiComponent::new);
        registry.beginRegistration(Player.class, BombComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(BombComponent::new);
        registry.beginRegistration(Player.class, MorticianComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(MorticianComponent::new);
        registry.beginRegistration(Player.class, AvengerComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(AvengerComponent::new);
        registry.beginRegistration(Player.class, StalkerComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(StalkerComponent::new);
        registry.beginRegistration(Player.class, CowboyComponent.KEY)
            .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
            .end(CowboyComponent::new);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(BrinCustomWinnerComponent.KEY, BrinCustomWinnerComponent::new);
    }
}
