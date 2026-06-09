package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.resources.ResourceLocation;

public final class MmceFactoryRecipeTickEvent extends MmceRecipeEvent {
    public MmceFactoryRecipeTickEvent(
            MachineControllerBlockEntity controller,
            ResourceLocation machineId,
            ResourceLocation recipeId,
            MmceEventPhase phase,
            int progress,
            int maxProgress,
            int parallelism,
            boolean coreThread,
            String threadName
    ) {
        super(controller, machineId, recipeId, MmceRecipeEventType.TICK, phase, progress, maxProgress,
                parallelism, true, coreThread, threadName, hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus.RUNNING, "", false);
    }
}
