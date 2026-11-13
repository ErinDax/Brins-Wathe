package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.NoelleAccess;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.agmas.harpymodloader.Harpymodloader;

public final class GuesserNames {
    private static final String ROLE_WIDGET = "org.agmas.noellesroles.client.ui.guesser.GuesserRoleWidget";
    private static final String PLAYER_WIDGET = "org.agmas.noellesroles.client.ui.guesser.GuesserPlayerWidget";

    private static Language language;
    private static List<Named> names = List.of();

    private GuesserNames() {
    }

    public static boolean isRoleWidget(Object widget) {
        return widget != null && ROLE_WIDGET.equals(widget.getClass().getName());
    }

    public static void submit(EditBox box) {
        String path = resolve(box.getValue());
        if (path == null || path.equals(box.getValue())) return;
        box.setValue(path);
        if (path.equals(box.getValue())) return;
        box.setMaxLength(path.length());
        box.setValue(path);
    }

    public static String completion(String text, int maxLength) {
        if (normalize(text).length() != text.length()) return "";
        for (Named entry : matches(text)) {
            if (entry.name().length() <= maxLength) return entry.name().substring(text.length());
        }
        return "";
    }

    public static List<FormattedCharSequence> lines(String text) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Named entry : matches(text)) {
            Role role = entry.role();
            lines.add(Component.literal(role.identifier().getPath() + " (")
                .append(RoleLabels.of(role))
                .append(")")
                .withColor(role.color())
                .getVisualOrderText());
        }
        return lines;
    }

    private static String resolve(String text) {
        String query = normalize(text);
        if (query.isEmpty()) return null;
        List<Role> found = new ArrayList<>();
        for (Named entry : names()) {
            if (entry.role().identifier().getPath().equalsIgnoreCase(query)) {
                found.addFirst(entry.role());
            } else if (entry.name().equals(query)) {
                found.add(entry.role());
            }
        }
        if (found.isEmpty()) return null;
        Role target = found.size() > 1 ? targetRole() : null;
        return (found.contains(target) ? target : found.getFirst()).identifier().getPath();
    }

    private static List<Named> matches(String text) {
        List<Named> matches = new ArrayList<>();
        String query = normalize(text);
        Minecraft client = Minecraft.getInstance();
        if (query.isEmpty() || client.player == null) return matches;
        boolean innocent = GameWorldComponent.KEY.get(client.player.level()).isInnocent(client.player);
        for (Named entry : names()) {
            if (entry.name().startsWith(query) && guessable(entry.role(), innocent)) matches.add(entry);
        }
        return matches;
    }

    private static boolean guessable(Role role, boolean innocent) {
        if (Harpymodloader.SPECIAL_ROLES.contains(role)) return false;
        if (innocent) return true;
        ResourceLocation id = role.identifier();
        return !role.canUseKiller()
            && !id.equals(NoelleAccess.VULTURE_ID)
            && !id.equals(NoelleAccess.JESTER_ID)
            && !id.equals(NoelleAccess.EXECUTIONER_ID);
    }

    private static Role targetRole() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return null;
        try {
            Object selected = Class.forName(PLAYER_WIDGET).getField("selectedPlayer").get(null);
            return selected instanceof UUID id ? GameWorldComponent.KEY.get(client.level).getRole(id) : null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static List<Named> names() {
        Language current = Language.getInstance();
        if (current != language || names.size() != WatheRoles.ROLES.size()) {
            List<Named> list = new ArrayList<>();
            for (Role role : WatheRoles.ROLES) {
                list.add(new Named(role, normalize(RoleLabels.of(role).getString())));
            }
            names = list;
            language = current;
        }
        return names;
    }

    private static String normalize(String text) {
        String name = text.strip();
        while (name.endsWith("!") || name.endsWith("！")) {
            name = name.substring(0, name.length() - 1).stripTrailing();
        }
        return name.toLowerCase(Locale.ROOT);
    }

    private record Named(Role role, String name) {
    }
}
