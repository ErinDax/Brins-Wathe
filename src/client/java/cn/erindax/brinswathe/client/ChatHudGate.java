package cn.erindax.brinswathe.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ChatHudGate {
    private static boolean rendering;
    private ChatHudGate() {
    }
    public static void enter() {
        rendering = true;
    }
    public static void exit() {
        rendering = false;
    }
    public static boolean isRendering() {
        return rendering;
    }
}
