package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.FactoryRecipeStartEvent")
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
