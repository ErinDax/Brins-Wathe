package cn.erindax.brinswathe.admin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public final class BrinRemark {
    public static final int MAX_LENGTH = 16;
    private static final String TEAM_PREFIX = "brin_remark_";

    private BrinRemark() {
    }

    public static boolean valid(String text) {
        String trimmed = text.trim();
        return !trimmed.isEmpty() && trimmed.length() <= MAX_LENGTH;
    }

    public static MutableComponent prefix(String text, ChatFormatting color) {
        return Component.literal("[" + text.trim() + "]").withStyle(color);
    }

    public static void set(ServerScoreboard scoreboard, String holder, String text, ChatFormatting color) {
        String name = TEAM_PREFIX + holder;
        PlayerTeam team = scoreboard.getPlayerTeam(name);
        if (team == null) team = scoreboard.addPlayerTeam(name);
        team.setPlayerPrefix(prefix(text, color));
        scoreboard.addPlayerToTeam(holder, team);
    }

    public static boolean clear(ServerScoreboard scoreboard, String holder) {
        PlayerTeam team = scoreboard.getPlayersTeam(holder);
        boolean managed = team != null && team.getName().startsWith(TEAM_PREFIX);
        if (team == null || !managed && team.getPlayerPrefix().getString().isEmpty()) return false;
        scoreboard.removePlayerFromTeam(holder, team);
        if (managed && team.getPlayers().isEmpty()) scoreboard.removePlayerTeam(team);
        return true;
    }

    public static String text(Scoreboard scoreboard, String holder) {
        PlayerTeam team = scoreboard.getPlayersTeam(holder);
        return team == null ? "" : team.getPlayerPrefix().getString();
    }

    public static String color(Scoreboard scoreboard, String holder) {
        PlayerTeam team = scoreboard.getPlayersTeam(holder);
        TextColor color = team == null ? null : team.getPlayerPrefix().getStyle().getColor();
        return color == null ? "" : color.serialize();
    }
}
