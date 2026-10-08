package cn.erindax.brinswathe;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.events.ModifierRemoved;
import org.agmas.harpymodloader.modifiers.Modifier;

public final class BrinGuesserSuspension {
    private static final Set<UUID> SUSPENDED = new HashSet<>();
    private static boolean tracking;

    private BrinGuesserSuspension() {
    }

    public static void reset() {
        SUSPENDED.clear();
        tracking = false;
    }

    public static void begin() {
        tracking = true;
    }

    public static void onRoleChanged(Level world, GameWorldComponent game, UUID playerId, Role role) {
        if (!tracking || world == null || world.isClientSide() || playerId == null || role == null) return;
        if (!game.isRunning()) return;
        Modifier guesser = BrinIcModifiers.findRegistered(BrinModifiers.GUESSER);
        if (guesser == null) return;
        WorldModifierComponent modifiers = WorldModifierComponent.KEY.get(world);
        List<Modifier> held = modifiers.getModifiers().get(playerId);
        Player player = world.getPlayerByUUID(playerId);
        if (held != null && held.contains(guesser)) {
            if (allows(guesser, role)) return;
            held.removeIf(guesser::equals);
            SUSPENDED.add(playerId);
            if (player == null) return;
            ModifierRemoved.EVENT.invoker().removeModifier(player, guesser);
            player.sendSystemMessage(Component.translatableWithFallback(
                "tip.brinswathe.guesser_suspended",
                "当前职业不能使用猜测者，猜测者已暂停"
            ).withStyle(ChatFormatting.GOLD));
        } else if (allows(guesser, role) && SUSPENDED.remove(playerId)) {
            modifiers.addModifier(playerId, guesser);
            if (player == null) return;
            ModifierAssigned.EVENT.invoker().assignModifier(player, guesser);
            player.sendSystemMessage(Component.translatableWithFallback(
                "tip.brinswathe.guesser_restored",
                "猜测者已恢复"
            ).withStyle(ChatFormatting.GOLD));
        }
    }

    private static boolean allows(Modifier modifier, Role role) {
        if (modifier.canOnlyBeAppliedTo != null && !modifier.canOnlyBeAppliedTo.contains(role)) return false;
        return modifier.cannotBeAppliedTo == null || !modifier.cannotBeAppliedTo.contains(role);
    }
}
