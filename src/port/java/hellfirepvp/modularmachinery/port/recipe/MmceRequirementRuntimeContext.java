package hellfirepvp.modularmachinery.port.recipe;

import hellfirepvp.modularmachinery.port.blockentity.EnergyHatchBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.FluidHatchBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.ItemBusBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceParsedRequirement;
import hellfirepvp.modularmachinery.port.data.MmceRecipeDefinition;
import hellfirepvp.modularmachinery.port.data.MmceRecipeRequirement;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public final class MmceRequirementRuntimeContext {
    private final MachineControllerBlockEntity controller;
    private final MmceRecipeExecutor.RecipeRun run;
    private final ResourceLocation machineId;
    private final MmceRecipeDefinition recipe;
    private final MmceRecipeRequirement requirementDefinition;
    private final MmceParsedRequirement requirement;
    private final MmceMachineComponents components;
    private final Optional<String> selectorTag;
    private final int tick;
    private final RandomSource random;
    private final int parallelism;
    private final MmceRecipeModifiers modifiers;

    MmceRequirementRuntimeContext(
            MachineControllerBlockEntity controller,
            MmceRecipeExecutor.RecipeRun run,
            ResourceLocation machineId,
            MmceRecipeDefinition recipe,
            MmceRecipeRequirement requirementDefinition,
            MmceParsedRequirement requirement,
            MmceMachineComponents components,
            Optional<String> selectorTag,
            int tick,
            RandomSource random,
            int parallelism,
            MmceRecipeModifiers modifiers
    ) {
        this.controller = controller;
        this.run = run;
        this.machineId = machineId;
        this.recipe = recipe;
        this.requirementDefinition = requirementDefinition;
        this.requirement = requirement;
        this.components = components;
        this.selectorTag = selectorTag == null ? Optional.empty() : selectorTag;
        this.tick = tick;
        this.random = random;
        this.parallelism = Math.max(1, parallelism);
        this.modifiers = modifiers;
    }

    public MachineControllerBlockEntity controller() {
        return controller;
    }

    public MmceRecipeExecutor.RecipeRun run() {
        return run;
    }

    public ResourceLocation machineId() {
        return machineId;
    }

    public MmceRecipeDefinition recipe() {
        return recipe;
    }

    public MmceRecipeRequirement requirementDefinition() {
        return requirementDefinition;
    }

    public MmceParsedRequirement requirement() {
        return requirement;
    }

    public MmceMachineComponents components() {
        return components;
    }

    public Optional<String> selectorTag() {
        return selectorTag;
    }

    public int tick() {
        return tick;
    }

    public RandomSource random() {
        return random;
    }

    public int parallelism() {
        return parallelism;
    }

    public int modifiedAmount(int amount) {
        return MmceRecipeExecutor.amountFor(requirement, amount, parallelism, modifiers);
    }

    public long modifiedAmount(long amount) {
        return MmceRecipeExecutor.amountFor(requirement, amount, parallelism, modifiers);
    }

    public float modifiedChance(float chance) {
        return modifiers.chance(requirement, chance);
    }

    public boolean rollChance() {
        return MmceRecipeExecutor.rollChance(controller, run, machineId, recipe, requirement, random, parallelism, modifiers);
    }

    public List<ItemBusBlockEntity> itemInputs() {
        return components.itemInputs(selectorTag);
    }

    public List<ItemBusBlockEntity> itemOutputs() {
        return components.itemOutputs(selectorTag);
    }

    public List<FluidHatchBlockEntity> fluidInputs() {
        return components.fluidInputs(selectorTag);
    }

    public List<FluidHatchBlockEntity> fluidOutputs() {
        return components.fluidOutputs(selectorTag);
    }

    public List<EnergyHatchBlockEntity> energyInputs() {
        return components.energyInputs(selectorTag);
    }

    public List<EnergyHatchBlockEntity> energyOutputs() {
        return components.energyOutputs(selectorTag);
    }
}
