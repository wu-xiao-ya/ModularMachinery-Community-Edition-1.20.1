package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceParsedRequirement;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;

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
    public String getRequirementType() {
        return requirementType;
    }
    public String getIoType() {
        return ioType;
    }
    public float getChance() {
        return chance;
    }
    public void setChance(float chance) {
        this.chance = clamp(chance);
    }
    public MmceResultChanceCreateEvent chance(float chance) {
        setChance(chance);
        return this;
    }
    public MmceResultChanceCreateEvent setResultChance(float chance) {
        return chance(chance);
    }

    private static float clamp(float chance) {
        return Math.max(0.0F, Math.min(1.0F, chance));
    }
}
