package cn.erindax.brinswathe.mixin;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModdedMurderGameMode.class)
public interface ModdedMurderGameModeInvoker {
    @Invoker(value = "findAndAssignPlayers", remap = false)
    static int brinFindAndAssignPlayers(
        int desiredRoleCount,
        Role role,
        List<ServerPlayer> players,
        GameWorldComponent gameWorldComponent,
        Level world
    ) {
        throw new AssertionError();
    }
}
