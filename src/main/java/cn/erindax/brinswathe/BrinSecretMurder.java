package cn.erindax.brinswathe;

import cn.erindax.brinswathe.mixin.BrinGameFunctionsInvoker;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.MapEffect;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.agmas.harpymodloader.Harpymodloader;

public final class BrinSecretMurder {
    public static final ResourceLocation MODE_ID = ResourceLocation.fromNamespaceAndPath("brinswathe", "secret_murder");
    public static final Role ROLE = WatheRoles.registerRole(new Role(
        ResourceLocation.fromNamespaceAndPath("brin", "secret_killer"),
        0xC13838,
        false,
        true,
        Role.MoodType.FAKE,
        -1,
        true
    ));
    public static final GameMode MODE = WatheGameModes.registerGameMode(MODE_ID, new BrinSecretMurderGameMode(MODE_ID));
    private static boolean starting;

    private BrinSecretMurder() {
    }

    public static void init() {
        if (!Harpymodloader.NON_MURDER_ROLES.contains(ROLE)) Harpymodloader.NON_MURDER_ROLES.add(ROLE);
    }

    public static boolean isActive(GameWorldComponent game) {
        return game != null && game.getGameMode() == MODE;
    }

    public static boolean interceptStart(ServerLevel world, GameMode mode, MapEffect mapEffect, int time) {
        if (starting || BrinDraft.isBusy()) return false;
        if (mode != WatheGameModes.MURDER && mode != Harpymodloader.MODDED_GAMEMODE) return false;
        float chance = BrinIcFlags.secretMurderChance;
        if (chance <= 0.0F) return false;
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        if (game.getGameStatus() != GameWorldComponent.GameStatus.INACTIVE) return false;
        if (BrinGameFunctionsInvoker.brinGetReadyPlayerList(world).size() < MODE.minPlayerCount) return false;
        if (world.getRandom().nextFloat() > chance) return false;
        starting = true;
        try {
            GameFunctions.startGame(world, MODE, mapEffect, time);
        } finally {
            starting = false;
        }
        return true;
    }
}
