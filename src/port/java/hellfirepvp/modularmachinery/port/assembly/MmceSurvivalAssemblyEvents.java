package hellfirepvp.modularmachinery.port.assembly;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class MmceSurvivalAssemblyEvents {
    private MmceSurvivalAssemblyEvents() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide()) {
            MmceSurvivalAssemblyManager.tick(player);
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MmceSurvivalAssemblyManager.remove(player);
        }
    }
}
