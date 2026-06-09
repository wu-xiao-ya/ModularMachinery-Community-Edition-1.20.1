package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.resources.ResourceLocation;

public final class MmceFactoryRecipeStartEvent extends MmceRecipeEvent {
    public MmceFactoryRecipeStartEvent(
            MachineControllerBlockEntity controller,
            ResourceLocation machineId,
            ResourceLocation recipeId,
            int maxProgress,
            int parallelism,
            boolean coreThread,
            String threadName
    ) {
        super(controller, machineId, recipeId, MmceRecipeEventType.START, MmceEventPhase.START, 0, maxProgress,
                parallelism, true, coreThread, threadName, hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus.RUNNING, "", false);
    }
}
