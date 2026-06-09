package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.resources.ResourceLocation;

public final class MmceFactoryRecipeFinishEvent extends MmceRecipeEvent {
    public MmceFactoryRecipeFinishEvent(
            MachineControllerBlockEntity controller,
            ResourceLocation machineId,
            ResourceLocation recipeId,
            int progress,
            int maxProgress,
            int parallelism,
            boolean coreThread,
            String threadName
    ) {
        super(controller, machineId, recipeId, MmceRecipeEventType.FINISH, MmceEventPhase.END, progress, maxProgress,
                parallelism, true, coreThread, threadName, hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus.FINISHED, "", false);
    }
}
