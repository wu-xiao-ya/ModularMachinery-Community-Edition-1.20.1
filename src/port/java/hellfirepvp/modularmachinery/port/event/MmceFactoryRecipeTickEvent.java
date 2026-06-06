package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.FactoryRecipeTickEvent")
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
