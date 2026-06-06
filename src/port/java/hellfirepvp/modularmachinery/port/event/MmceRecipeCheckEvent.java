package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.RecipeCheckEvent")
public final class MmceRecipeCheckEvent extends MmceRecipeEvent {
    public MmceRecipeCheckEvent(
            MachineControllerBlockEntity controller,
            ResourceLocation machineId,
            ResourceLocation recipeId,
            MmceEventPhase phase,
            int parallelism,
            boolean factoryRun,
            boolean coreThread,
            String threadName,
            MmceRecipeStatus status,
            String cause
    ) {
        super(controller, machineId, recipeId, MmceRecipeEventType.CHECK, phase, 0, 1, parallelism,
                factoryRun, coreThread, threadName, status, cause, false);
    }
}
