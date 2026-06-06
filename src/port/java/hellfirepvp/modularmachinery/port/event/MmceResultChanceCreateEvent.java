package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceParsedRequirement;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.ResultChanceCreateEvent")
public final class MmceResultChanceCreateEvent extends MmceRecipeEvent {
    private final String requirementType;
    private final String ioType;
    private float chance;

    public MmceResultChanceCreateEvent(
            MachineControllerBlockEntity controller,
            ResourceLocation machineId,
            ResourceLocation recipeId,
            int progress,
            int maxProgress,
            int parallelism,
            boolean factoryRun,
            boolean coreThread,
            String threadName,
            MmceParsedRequirement requirement,
            float chance
    ) {
        super(controller, machineId, recipeId, MmceRecipeEventType.RESULT_CHANCE, MmceEventPhase.END, progress, maxProgress,
                parallelism, factoryRun, coreThread, threadName, MmceRecipeStatus.RUNNING, "", false);
        this.requirementType = requirement == null ? "" : requirement.getClass().getSimpleName();
        this.ioType = requirement == null ? "" : requirement.ioType().name().toLowerCase(Locale.ROOT);
        this.chance = clamp(chance);
    }

    @ZenCodeType.Getter("requirementType")
    public String getRequirementType() {
        return requirementType;
    }

    @ZenCodeType.Getter("ioType")
    public String getIoType() {
        return ioType;
    }

    @ZenCodeType.Getter("chance")
    public float getChance() {
        return chance;
    }

    @ZenCodeType.Setter("chance")
    public void setChance(float chance) {
        this.chance = clamp(chance);
    }

    @ZenCodeType.Method
    public MmceResultChanceCreateEvent chance(float chance) {
        setChance(chance);
        return this;
    }

    @ZenCodeType.Method
    public MmceResultChanceCreateEvent setResultChance(float chance) {
        return chance(chance);
    }

    private static float clamp(float chance) {
        return Math.max(0.0F, Math.min(1.0F, chance));
    }
}
