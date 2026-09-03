package cn.erindax.brinswathe;

import cn.erindax.brinswathe.config.BrinConfig;
import cn.erindax.brinswathe.mixin.ModdedMurderGameModeInvoker;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.modifiers.Modifier;

public final class BrinHarpyRoles {
    public static final List<Role> FORCED_REFRESH_ROLES = new ArrayList<>();
    private BrinHarpyRoles() {
    }

    public static boolean isForced(Role role) {
        return FORCED_REFRESH_ROLES.contains(role)
            || Harpymodloader.FORCED_MODDED_ROLE.containsKey(role);
    }
    public static boolean isEnabled(Role role) {
        Integer maximum = Harpymodloader.ROLE_MAX.get(role.identifier());
        return !isDisabled(role) && (maximum == null || maximum > 0);
    }
    public static boolean isDisabled(Role role) {
        return HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().toString());
    }
    public static boolean isNonMurder(Role role) {
        return Harpymodloader.NON_MURDER_ROLES.contains(role);
    }
    public static boolean isModifierBlacklisted(Role role, Modifier modifier) {
        if (role == null || modifier == null) return false;
        List<String> blocked = BrinConfig.harpyModifierBlacklist().get(role.identifier().toString());
        return blocked != null && blocked.contains(modifier.identifier().toString());
    }
    public static int resolveCount(int configured, int players, int vanillaDividend) {
        if (configured > 0) return configured;
        if (configured < 0) return (int) (players / Math.abs((long) configured));
        return (int) Math.floor((float) players / Math.max(1, vanillaDividend));
    }
    public static int resolveNeutralCount(List<ServerPlayer> players, GameWorldComponent gameWorld) {
        return Math.max(0, resolveCount(
            BrinConfig.harpyNeutralRoleCount(),
            players.size(),
            gameWorld.getKillerDividend()
        ));
    }
    public static int resolveKillerCount(List<ServerPlayer> players, GameWorldComponent gameWorld) {
        return Math.max(0, resolveCount(
            BrinConfig.harpyKillerRoleCount(),
            players.size(),
            gameWorld.getKillerDividend()
        ));
    }
    public static int resolveVigilanteCount(List<ServerPlayer> players, GameWorldComponent gameWorld) {
        return Math.max(0, resolveCount(
            BrinConfig.harpyVigilanteRoleCount(),
            players.size(),
            gameWorld.getVigilanteDividend()
        ));
    }
    public static int assignForcedRefresh(
        List<Role> roles,
        List<ServerPlayer> players,
        GameWorldComponent gameWorld,
        Level world,
        Predicate<Role> predicate
    ) {
        int assigned = 0;
        for (Role role : roles) {
            if (!FORCED_REFRESH_ROLES.contains(role)) continue;
            if (!predicate.test(role)) continue;
            if (isDisabled(role)) continue;
            if (isNonMurder(role) && role.canUseKiller()) continue;
            assigned += ModdedMurderGameModeInvoker.brinFindAndAssignPlayers(
                1,
                role,
                players,
                gameWorld,
                world
            );
            players.removeIf(player -> !Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(player)));
        }
        return assigned;
    }
}
