package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.FactoryRecipeFailureEvent")
public final class MmceFactoryRecipeFailureEvent extends MmceRecipeEvent {
    public MmceFactoryRecipeFailureEvent(
            MachineControllerBlockEntity controller,
            ResourceLocation machineId,
            ResourceLocation recipeId,
            int progress,
            int maxProgress,
            int parallelism,
            boolean coreThread,
            String threadName,
            MmceRecipeStatus status,
            String cause,
            boolean destructRecipe
    ) {
        super(controller, machineId, recipeId, MmceRecipeEventType.FAILURE, MmceEventPhase.END, progress, maxProgress,
                parallelism, true, coreThread, threadName, status, cause, destructRecipe);
    }
}
