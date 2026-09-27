package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinHarpyRoles;
import cn.erindax.brinswathe.BrinIcModifiers;
import cn.erindax.brinswathe.BrinModifiers;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModdedMurderGameMode.class)
public abstract class BrinGuaranteedGuesserMixin {
    @Inject(
        method = "assignModifiers",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/HashMap;clear()V",
            ordinal = 0,
            shift = At.Shift.AFTER,
            remap = false
        )
    )
    private void brinAssignGuaranteedGuesser(
        int desiredRoleCount,
        ServerLevel world,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci
    ) {
        Modifier guesser = BrinIcModifiers.findRegistered(BrinModifiers.GUESSER);
        if (guesser == null) return;
        if (HarpyModLoaderConfig.HANDLER.instance().disabledModifiers.contains(guesser.identifier().toString())) return;
        Integer maximum = Harpymodloader.MODIFIER_MAX.get(guesser.identifier());
        if (maximum != null && maximum <= 0) return;
        List<UUID> forcedGuessers = Harpymodloader.FORCED_MODDED_MODIFIER.get(guesser);
        if (forcedGuessers != null && !forcedGuessers.isEmpty()) return;

        Set<UUID> forcedPlayers = new HashSet<>();
        for (List<UUID> ids : Harpymodloader.FORCED_MODDED_MODIFIER.values()) {
            if (ids != null) forcedPlayers.addAll(ids);
        }
        List<ServerPlayer> candidates = new ArrayList<>();
        for (ServerPlayer player : players) {
            if (forcedPlayers.contains(player.getUUID())) continue;
            if (brinCanReceive(guesser, gameWorld, player)) candidates.add(player);
        }
        if (candidates.isEmpty()) return;

        ServerPlayer selected = candidates.get(world.getRandom().nextInt(candidates.size()));
        WorldModifierComponent.KEY.get(world).addModifier(selected.getUUID(), guesser);
        ModifierAssigned.EVENT.invoker().assignModifier(selected, guesser);
    }

    @Unique
    private static boolean brinCanReceive(Modifier modifier, GameWorldComponent gameWorld, ServerPlayer player) {
        Role role = gameWorld.getRole(player);
        if (role == null) return false;
        if (modifier.canOnlyBeAppliedTo != null && !modifier.canOnlyBeAppliedTo.contains(role)) return false;
        if (modifier.cannotBeAppliedTo != null && modifier.cannotBeAppliedTo.contains(role)) return false;
        boolean killer = gameWorld.canUseKillerFeatures(player);
        if (modifier.killerOnly && !killer) return false;
        if (modifier.civilianOnly && killer) return false;
        return !BrinHarpyRoles.isModifierBlacklisted(role, modifier);
    }
}
