package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import net.minecraft.resources.ResourceLocation;

public final class MmceRecipeFinishEvent extends MmceRecipeEvent {
    public MmceRecipeFinishEvent(
            MachineControllerBlockEntity controller,
            ResourceLocation machineId,
            ResourceLocation recipeId,
            int progress,
            int maxProgress,
            int parallelism,
            boolean factoryRun,
            boolean coreThread,
            String threadName
    ) {
        super(controller, machineId, recipeId, MmceRecipeEventType.FINISH, MmceEventPhase.END, progress, maxProgress,
                parallelism, factoryRun, coreThread, threadName, MmceRecipeStatus.FINISHED, "", false);
    }
}
