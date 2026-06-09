package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import net.minecraft.resources.ResourceLocation;

public final class MmceRecipeTickEvent extends MmceRecipeEvent {
    public MmceRecipeTickEvent(
            MachineControllerBlockEntity controller,
            ResourceLocation machineId,
            ResourceLocation recipeId,
            MmceEventPhase phase,
            int progress,
            int maxProgress,
            int parallelism,
            boolean factoryRun,
            boolean coreThread,
            String threadName
    ) {
        super(controller, machineId, recipeId, MmceRecipeEventType.TICK, phase, progress, maxProgress,
                parallelism, factoryRun, coreThread, threadName, MmceRecipeStatus.RUNNING, "", false);
    }
}
