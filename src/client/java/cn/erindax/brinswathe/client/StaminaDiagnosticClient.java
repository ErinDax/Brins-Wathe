package cn.erindax.brinswathe.client;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public final class StaminaDiagnosticClient {
    private StaminaDiagnosticClient() {
    }

    static void registerClientStaminaDiagnostic() {
        net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess) -> dispatcher.register(
                net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("brinstamina")
                    .executes(context -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player == null) return 0;
                        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
                        var role = gameWorld.getRole(player);

                        int mixedMaxSprint = role == null ? -999 : role.getMaxSprintTime();
                        cn.erindax.brinswathe.component.StaminaComponent stamina =
                            cn.erindax.brinswathe.component.StaminaComponent.KEY.get(player);
                        float sprintTicks = player
                            .saveWithoutId(new net.minecraft.nbt.CompoundTag())
                            .getFloat("sprintingTicks");
                        String ratio = mixedMaxSprint > 0
                            ? String.format("%.2f", Math.max(0.0F, Math.min(1.0F, sprintTicks / mixedMaxSprint)))
                            : "-";
                        context.getSource().sendFeedback(Component.literal(
                            "CLIENT role=" + (role == null ? "null" : role.identifier())
                                + " maxSprint(mixed)=" + mixedMaxSprint
                                + " | stamina=" + (stamina == null
                                    ? "-"
                                    : stamina.currentStamina + "/" + stamina.maxStamina)
                                + " | sprintTicks=" + String.format("%.1f", sprintTicks)
                                + " | barFill=" + ratio
                        ));
                        return 1;
                    })
            )
        );
    }
}
