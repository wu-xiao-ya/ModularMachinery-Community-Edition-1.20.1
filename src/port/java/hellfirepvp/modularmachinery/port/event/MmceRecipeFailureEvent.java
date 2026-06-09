package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import net.minecraft.resources.ResourceLocation;

public final class MmceRecipeFailureEvent extends MmceRecipeEvent {
    public MmceRecipeFailureEvent(
            MachineControllerBlockEntity controller,
            ResourceLocation machineId,
            ResourceLocation recipeId,
            int progress,
            int maxProgress,
            int parallelism,
            boolean factoryRun,
            boolean coreThread,
            String threadName,
            MmceRecipeStatus status,
            String cause,
            boolean destructRecipe
    ) {
        super(controller, machineId, recipeId, MmceRecipeEventType.FAILURE, MmceEventPhase.END, progress, maxProgress,
                parallelism, factoryRun, coreThread, threadName, status, cause, destructRecipe);
    }
}
