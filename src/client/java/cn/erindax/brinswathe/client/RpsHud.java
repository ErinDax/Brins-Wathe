package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.network.RpsActionC2SPacket;
import cn.erindax.brinswathe.network.RpsStateS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class RpsHud {
    private static int phase = RpsStateS2CPacket.CLEAR;
    private static String opponentName = "";
    private static int secondsLeft;
    private static int yourChoice = -1;
    private static int theirChoice = -1;
    private static int outcome;
    private RpsHud() {
    }

    public static void init() {
        HudRenderCallback.EVENT.register(RpsHud::render);
    }
    public static void apply(RpsStateS2CPacket packet) {
        phase = packet.phase();
        opponentName = packet.opponentName();
        secondsLeft = packet.secondsLeft();
        yourChoice = packet.yourChoice();
        theirChoice = packet.theirChoice();
        outcome = packet.outcome();
    }
    public static void clear() {
        phase = RpsStateS2CPacket.CLEAR;
        opponentName = "";
        secondsLeft = 0;
        yourChoice = -1;
        theirChoice = -1;
        outcome = 0;
    }
    public static boolean locksMovement() {
        return phase == RpsStateS2CPacket.PICK || phase == RpsStateS2CPacket.WAIT;
    }
    public static boolean handleKey(int key, int action) {
        if (action != GLFW.GLFW_PRESS) return false;
        if (phase == RpsStateS2CPacket.CLEAR) return false;
        Minecraft client = Minecraft.getInstance();
        if (client.screen != null || client.player == null) return false;
        if (phase == RpsStateS2CPacket.INVITE) {
            if (key == GLFW.GLFW_KEY_Y) {
                ClientPlayNetworking.send(RpsActionC2SPacket.accept());
                return true;
            }
            if (key == GLFW.GLFW_KEY_N) {
                ClientPlayNetworking.send(RpsActionC2SPacket.decline());
                return true;
            }
            return false;
        }
        if (phase == RpsStateS2CPacket.PICK) {
            int choice = switch (key) {
                case GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_KP_1 -> 0;
                case GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_KP_2 -> 1;
                case GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_KP_3 -> 2;
                default -> -1;
            };
            if (choice < 0) return false;
            ClientPlayNetworking.send(RpsActionC2SPacket.choose(choice));
            return true;
        }
        return false;
    }
    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (phase == RpsStateS2CPacket.CLEAR) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;
        Font font = client.font;
        Component title = title();
        Component hint = hint();
        int width = Math.max(font.width(title), font.width(hint)) + 24;
        int height = hint.getString().isEmpty() ? 28 : 42;
        int x = (graphics.guiWidth() - width) / 2;
        int y = 18;
        graphics.flush();
        graphics.fill(x, y, x + width, y + height, 0xC0101018);
        graphics.fill(x, y, x + width, y + 1, 0xFFE8C56B);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFFE8C56B);
        graphics.drawString(font, title, x + 12, y + 8, 0xFFF8F0D8, false);
        if (!hint.getString().isEmpty()) {
            graphics.drawString(font, hint, x + 12, y + 22, 0xFFD0D0D0, false);
        }
        graphics.flush();
    }
    private static Component title() {
        return switch (phase) {
            case RpsStateS2CPacket.INVITE -> Component.translatable("hud.brinswathe.rps.invite", opponentName, secondsLeft);
            case RpsStateS2CPacket.PENDING -> Component.translatable("hud.brinswathe.rps.pending", opponentName, secondsLeft);
            case RpsStateS2CPacket.PICK -> Component.translatable("hud.brinswathe.rps.pick", opponentName, secondsLeft);
            case RpsStateS2CPacket.WAIT -> Component.translatable("hud.brinswathe.rps.wait", opponentName, secondsLeft);
            case RpsStateS2CPacket.RESULT -> Component.translatable(
                "hud.brinswathe.rps.result",
                Component.translatable(choiceKey(yourChoice)),
                Component.translatable(choiceKey(theirChoice)),
                Component.translatable(outcomeKey())
            );
            default -> Component.empty();
        };
    }
    private static Component hint() {
        return switch (phase) {
            case RpsStateS2CPacket.INVITE -> Component.translatable("hud.brinswathe.rps.invite_hint");
            case RpsStateS2CPacket.PICK -> Component.translatable("hud.brinswathe.rps.pick_hint");
            default -> Component.empty();
        };
    }
    private static String choiceKey(int choice) {
        return switch (choice) {
            case 0 -> "rps.brinswathe.rock";
            case 1 -> "rps.brinswathe.scissors";
            case 2 -> "rps.brinswathe.paper";
            default -> "rps.brinswathe.none";
        };
    }
    private static String outcomeKey() {
        return switch (outcome) {
            case RpsStateS2CPacket.OUTCOME_WIN -> "rps.brinswathe.win";
            case RpsStateS2CPacket.OUTCOME_LOSE -> "rps.brinswathe.lose";
            case RpsStateS2CPacket.OUTCOME_DRAW -> "rps.brinswathe.draw";
            default -> "rps.brinswathe.none";
        };
    }
}
