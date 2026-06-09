package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import net.minecraft.resources.ResourceLocation;

public final class MmceRecipeStartEvent extends MmceRecipeEvent {
    public MmceRecipeStartEvent(
            MachineControllerBlockEntity controller,
            ResourceLocation machineId,
            ResourceLocation recipeId,
            int maxProgress,
            int parallelism,
            boolean factoryRun,
            boolean coreThread,
            String threadName
    ) {
        super(controller, machineId, recipeId, MmceRecipeEventType.START, MmceEventPhase.START, 0, maxProgress,
                parallelism, factoryRun, coreThread, threadName, MmceRecipeStatus.RUNNING, "", false);
    }
}
