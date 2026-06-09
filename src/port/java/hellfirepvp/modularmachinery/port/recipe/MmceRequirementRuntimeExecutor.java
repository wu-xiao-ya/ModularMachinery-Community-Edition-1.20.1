package hellfirepvp.modularmachinery.port.recipe;

import hellfirepvp.modularmachinery.port.data.MmceParsedRequirement;

@FunctionalInterface
public interface MmceRequirementRuntimeExecutor {
    MmceRequirementRuntimeResult execute(MmceRequirementRuntimeContext context, MmceRequirementRuntimePhase phase);

    default boolean shouldRunPerTick(MmceParsedRequirement requirement, int tick) {
        return false;
    }

    default boolean requiresPerTickChance(MmceParsedRequirement requirement) {
        return false;
    }

    default boolean shouldTriggerInput(MmceParsedRequirement requirement, int tick) {
        return false;
    }
}
