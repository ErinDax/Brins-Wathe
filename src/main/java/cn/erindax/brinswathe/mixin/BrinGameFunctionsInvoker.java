package cn.erindax.brinswathe.mixin;

import dev.doctor4t.wathe.game.GameFunctions;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameFunctions.class)
public interface BrinGameFunctionsInvoker {
    @Invoker(value = "getReadyPlayerList", remap = false)
    static List<ServerPlayer> brinGetReadyPlayerList(ServerLevel world) {
        throw new AssertionError();
    }
}
