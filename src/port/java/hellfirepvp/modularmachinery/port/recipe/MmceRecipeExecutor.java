package hellfirepvp.modularmachinery.port.recipe;

import hellfirepvp.modularmachinery.port.blockentity.EnergyHatchBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.FluidHatchBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.ItemBusBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceChemicalRequirement;
import hellfirepvp.modularmachinery.port.data.MmceEnergyRequirement;
import hellfirepvp.modularmachinery.port.data.MmceFuelRequirement;
import hellfirepvp.modularmachinery.port.data.MmceFluidRequirement;
import hellfirepvp.modularmachinery.port.data.MmceIngredientArrayRequirement;
import hellfirepvp.modularmachinery.port.data.MmceIoType;
import hellfirepvp.modularmachinery.port.data.MmceItemRequirement;
import hellfirepvp.modularmachinery.port.data.MmceMachineModifierDefinition;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.data.MmceParsedRequirement;
import hellfirepvp.modularmachinery.port.data.MmceRecipeDefinition;
import hellfirepvp.modularmachinery.port.data.MmceRecipeFailureAction;
import hellfirepvp.modularmachinery.port.data.MmceRecipeRequirement;
import hellfirepvp.modularmachinery.port.data.MmceSmartInterfaceRequirement;
import hellfirepvp.modularmachinery.port.event.MmceEventPhase;
import hellfirepvp.modularmachinery.port.event.MmceEventRegistry;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeFailureEvent;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeFinishEvent;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeStartEvent;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeTickEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeCheckEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeFailureEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeFinishEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeStartEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeTickEvent;
import hellfirepvp.modularmachinery.port.event.MmceResultChanceCreateEvent;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public final class MmceRecipeExecutor {
    public static void tick(MachineControllerBlockEntity controller) {
        Level level = controller.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        if (!controller.isStructureFormed()) {
            controller.clearActiveRecipe();
            controller.setWorking(false);
            controller.setRecipeStatus(MmceRecipeStatus.STRUCTURE_MISSING);
            return;
        }

        MmceMachineComponents components = MmceMachineComponents.collect(level, controller.getComponentPositions(), controller.getComponentTags());
        Optional<ResourceLocation> machineId = controller.getMachineId();
        if (machineId.isEmpty()) {
            controller.clearActiveRecipe();
            controller.setWorking(false);
            controller.setRecipeStatus(MmceRecipeStatus.NO_MACHINE);
            return;
        }

        ControllerRun run = new ControllerRun(controller);
        MmceRecipeModifiers modifiers = MmceRecipeModifiers.of(run.getRecipeModifiers(controller), components.upgradeBuses(), machineId.get());
        tickRun(controller, run, machineId.get(), components, modifiers, true, machineParallelism(machineId.get(), components));
    }

    public static RunTickResult tickRun(
            MachineControllerBlockEntity controller,
            RecipeRun run,
            ResourceLocation machineId,
            MmceMachineComponents components,
            boolean searchWhenIdle
    ) {
        return tickRun(controller, run, machineId, components, searchWhenIdle, components.maxParallelism());
    }

    public static RunTickResult tickRun(
            MachineControllerBlockEntity controller,
            RecipeRun run,
            ResourceLocation machineId,
            MmceMachineComponents components,
            boolean searchWhenIdle,
            int maxParallelism
    ) {
        return tickRun(controller, run, machineId, components, searchWhenIdle, maxParallelism, recipe -> true);
    }

    public static RunTickResult tickRun(
            MachineControllerBlockEntity controller,
            RecipeRun run,
            ResourceLocation machineId,
            MmceMachineComponents components,
            boolean searchWhenIdle,
            int maxParallelism,
            Predicate<MmceRecipeDefinition> recipeFilter
    ) {
        MmceRecipeModifiers modifiers = MmceRecipeModifiers.of(run.getRecipeModifiers(controller), components.upgradeBuses(), machineId);
        return tickRun(controller, run, machineId, components, modifiers, searchWhenIdle, maxParallelism, recipeFilter);
    }

    static RunTickResult tickRun(
            MachineControllerBlockEntity controller,
            RecipeRun run,
            ResourceLocation machineId,
            MmceMachineComponents components,
            MmceRecipeModifiers modifiers,
            boolean searchWhenIdle
    ) {
        return tickRun(controller, run, machineId, components, modifiers, searchWhenIdle, components.maxParallelism());
    }

    static RunTickResult tickRun(
            MachineControllerBlockEntity controller,
            RecipeRun run,
            ResourceLocation machineId,
            MmceMachineComponents components,
            MmceRecipeModifiers modifiers,
            boolean searchWhenIdle,
            int maxParallelism
    ) {
        return tickRun(controller, run, machineId, components, modifiers, searchWhenIdle, maxParallelism, recipe -> true);
    }

    static RunTickResult tickRun(
            MachineControllerBlockEntity controller,
            RecipeRun run,
            ResourceLocation machineId,
            MmceMachineComponents components,
            MmceRecipeModifiers modifiers,
            boolean searchWhenIdle,
            int maxParallelism,
            Predicate<MmceRecipeDefinition> recipeFilter
    ) {
        Level level = controller.getLevel();
        if (level == null || level.isClientSide()) {
            return currentResult(run);
        }

        Optional<MmceRecipeDefinition> activeRecipe = run.getActiveRecipeId()
                .map(MmceDataRegistry.snapshot().recipes()::get)
                .filter(recipe -> recipe.machineId().equals(machineId));
        if (activeRecipe.isEmpty()) {
            run.getActiveRecipeId().ifPresent(recipeId -> {
                run.clearActiveRecipe();
                run.setRecipeStatus(MmceRecipeStatus.NO_RECIPE, "Active recipe unavailable: " + recipeId);
            });

            if (searchWhenIdle) {
                RecipeSearchResult searchResult = findStartableRecipe(controller, run, machineId, components, modifiers,
                        maxParallelism, recipeFilter);
                searchResult.recipe().ifPresent(recipe -> {
                    MmceRecipeModifiers startModifiers = modifiers.withAdditional(searchResult.catalystModifiers());
                    MmceRecipeEvent startEvent = postRecipeEvent(startEvent(controller, run, machineId, recipe, startModifiers,
                            searchResult.parallelism()));
                    if (startEvent.isCanceled()) {
                        if (startEvent.isFailed()) {
                            run.setRecipeStatus(startEvent.getRecipeStatus(), startEvent.getCause());
                        }
                        return;
                    }
                    MmceMachineComponents inputComponents = components.forInputGroup(searchResult.inputGroupId());
                    if (!consumeStartInputs(controller, run, machineId, recipe, inputComponents, level.getRandom(),
                            searchResult.parallelism(), modifiers, startModifiers, searchResult.catalystSelection())) {
                        run.setWorking(false);
                        run.setRecipeStatus(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " catalyst input");
                        return;
                    }
                    run.startRecipe(recipe.id(), searchResult.parallelism());
                    run.setActiveInputGroupId(searchResult.inputGroupId());
                    addCatalystModifiers(controller, run, recipe, searchResult.parallelism(), searchResult.catalystModifiers());
                    recipe.startCommands().ifPresent(commands -> MmceRecipeCommands.run(controller, commands, 0));
                });
                activeRecipe = run.getActiveRecipeId().map(MmceDataRegistry.snapshot().recipes()::get);
                if (activeRecipe.isEmpty()) {
                    run.setWorking(false);
                    if (searchResult.recipe().isEmpty()) {
                        run.setRecipeStatus(searchResult.status(), searchResult.detail());
                    } else if (run.getRecipeStatus() == MmceRecipeStatus.RUNNING) {
                        run.setRecipeStatus(MmceRecipeStatus.IDLE, "Recipe start canceled: " + searchResult.recipe().get().id());
                    }
                    return currentResult(run);
                }
            }

            if (activeRecipe.isEmpty()) {
                run.setWorking(false);
                if (run.getRecipeStatus() == MmceRecipeStatus.RUNNING) {
                    run.setRecipeStatus(MmceRecipeStatus.IDLE);
                }
                return currentResult(run);
            }
        }

        MmceRecipeDefinition recipe = activeRecipe.get();
        int parallelism = clampActiveParallelism(run, recipe, maxParallelism);
        MmceMachineComponents activeComponents = components.forInputGroup(run.getActiveInputGroupId());
        StartCheck supportCheck = checkSupported(recipe);
        if (!supportCheck.ok()) {
            postRecipeEvent(failureEvent(controller, run, machineId, recipe, modifiers, parallelism,
                    supportCheck.status(), supportCheck.detail(), true));
            run.clearActiveRecipe();
            run.setWorking(false);
            run.setRecipeStatus(supportCheck.status(), supportCheck.detail());
            return currentResult(run);
        }

        if (run.getRecipeProgress() >= recipeTime(recipe, modifiers)) {
            finishOrBlock(controller, run, recipe, activeComponents, level.getRandom(), parallelism, modifiers);
            return currentResult(run);
        }

        TickCheck tickCheck = executePerTickRequirements(controller, run, machineId, recipe, activeComponents,
                run.getRecipeProgress(), level.getRandom(), parallelism, modifiers);
        if (!tickCheck.ok()) {
            run.setWorking(false);
            applyFailureAction(machineId, run);
            run.setRecipeStatus(tickCheck.status(), tickCheck.detail());
            MmceRecipeEvent failureEvent = postRecipeEvent(failureEvent(controller, run, machineId, recipe, modifiers, parallelism,
                    tickCheck.status(), tickCheck.detail(), recipe.cancelIfPerTickFails()));
            if (recipe.cancelIfPerTickFails()) {
                run.clearActiveRecipe();
            } else if (failureEvent.isDestructRecipe()) {
                run.clearActiveRecipe();
            }
            return currentResult(run);
        }

        MmceRecipeEvent preTickEvent = postRecipeEvent(tickEvent(controller, run, machineId, recipe, modifiers, parallelism,
                MmceEventPhase.START));
        if (preTickEvent.isFailed()) {
            run.setWorking(false);
            run.setRecipeStatus(preTickEvent.getRecipeStatus(), preTickEvent.getCause());
            if (preTickEvent.isDestructRecipe()) {
                run.clearActiveRecipe();
            }
            postRecipeEvent(failureEvent(controller, run, machineId, recipe, modifiers, parallelism,
                    preTickEvent.getRecipeStatus(), preTickEvent.getCause(), preTickEvent.isDestructRecipe()));
            return currentResult(run);
        }
        if (preTickEvent.isPreventProgressing()) {
            run.setWorking(true);
            run.setRecipeStatus(MmceRecipeStatus.RUNNING, preTickEvent.getCause());
            return currentResult(run);
        }

        run.setWorking(true);
        run.setRecipeStatus(MmceRecipeStatus.RUNNING, runningDetail(recipe, parallelism));
        recipe.processingCommands().ifPresent(commands -> MmceRecipeCommands.run(controller, commands, run.getRecipeProgress()));
        int progress = run.advanceRecipeProgress();
        MmceRecipeEvent postTickEvent = postRecipeEvent(tickEvent(controller, run, machineId, recipe, modifiers, parallelism,
                MmceEventPhase.END));
        if (postTickEvent.isFailed()) {
            run.setWorking(false);
            run.setRecipeStatus(postTickEvent.getRecipeStatus(), postTickEvent.getCause());
            if (postTickEvent.isDestructRecipe()) {
                run.clearActiveRecipe();
            }
            postRecipeEvent(failureEvent(controller, run, machineId, recipe, modifiers, parallelism,
                    postTickEvent.getRecipeStatus(), postTickEvent.getCause(), postTickEvent.isDestructRecipe()));
            return currentResult(run);
        }
        if (postTickEvent.isPreventProgressing() && progress > 0) {
            run.rewindRecipeProgress();
            run.setRecipeStatus(MmceRecipeStatus.RUNNING, postTickEvent.getCause());
            return currentResult(run);
        }
        if (progress >= recipeTime(recipe, modifiers)) {
            finishOrBlock(controller, run, recipe, activeComponents, level.getRandom(), parallelism, modifiers);
        }
        return currentResult(run);
    }

    private static RecipeSearchResult findStartableRecipe(
            MachineControllerBlockEntity controller,
            RecipeRun run,
            ResourceLocation machineId,
            MmceMachineComponents components,
            MmceRecipeModifiers modifiers
    ) {
        return findStartableRecipe(controller, run, machineId, components, modifiers, components.maxParallelism(), recipe -> true);
    }

    public static int machineParallelism(ResourceLocation machineId, MmceMachineComponents components) {
        int componentParallelism = components == null ? 1 : components.maxParallelism();
        Optional<MmceMachineDefinition> machine = MmceDataRegistry.getMachine(machineId);
        if (machine.isEmpty()) {
            return Math.max(1, componentParallelism);
        }
        MmceMachineDefinition definition = machine.get();
        if (!definition.parallelizable()) {
            return 1;
        }
        int available = Math.max(1, componentParallelism + definition.internalParallelism());
        return Math.max(1, Math.min(available, definition.maxParallelism()));
    }

    private static void applyFailureAction(ResourceLocation machineId, RecipeRun run) {
        MmceRecipeFailureAction action = MmceDataRegistry.getMachine(machineId)
                .map(machine -> machine.failureAction())
                .orElse(MmceRecipeFailureAction.STILL);
        switch (action) {
            case RESET -> run.setRecipeProgress(0);
            case DECREASE -> run.setRecipeProgress(Math.max(0, run.getRecipeProgress() - 1));
            case STILL -> {
            }
        }
    }

    private static RecipeSearchResult findStartableRecipe(
            MachineControllerBlockEntity controller,
            RecipeRun run,
            ResourceLocation machineId,
            MmceMachineComponents components,
            MmceRecipeModifiers modifiers,
            int maxParallelismLimit
    ) {
        return findStartableRecipe(controller, run, machineId, components, modifiers, maxParallelismLimit, recipe -> true);
    }

    private static RecipeSearchResult findStartableRecipe(
            MachineControllerBlockEntity controller,
            RecipeRun run,
            ResourceLocation machineId,
            MmceMachineComponents components,
            MmceRecipeModifiers modifiers,
            int maxParallelismLimit,
            Predicate<MmceRecipeDefinition> recipeFilter
    ) {
        List<MmceRecipeDefinition> recipes = MmceDataRegistry.getRecipesFor(machineId);
        if (recipes.isEmpty()) {
            return RecipeSearchResult.failure(MmceRecipeStatus.NO_RECIPE, "No recipe for " + machineId);
        }

        StartCheck firstFailure = StartCheck.failure(MmceRecipeStatus.NO_RECIPE, "No recipe can start");
        for (MmceRecipeDefinition recipe : recipes) {
            if (!recipeFilter.test(recipe)) {
                if (firstFailure.status() == MmceRecipeStatus.NO_RECIPE) {
                    firstFailure = StartCheck.failure(MmceRecipeStatus.NO_RECIPE, recipe.id() + " thread limit reached");
                }
                continue;
            }
            int maxParallelism = recipe.parallelized() ? Math.max(1, maxParallelismLimit) : 1;
            StartCheck lastFailure = StartCheck.failure(MmceRecipeStatus.NO_RECIPE, "No recipe can start");
            for (int parallelism = maxParallelism; parallelism >= 1; parallelism--) {
                StartCheck preCheck = postCheckEvent(controller, machineId, run, recipe, parallelism,
                        MmceRecipeStatus.RUNNING, "", MmceEventPhase.START);
                if (!preCheck.ok()) {
                    lastFailure = preCheck;
                    continue;
                }
                int checkedParallelism = Math.max(1, Math.min(parallelism, preCheck.parallelism()));
                for (int inputGroupId : candidateInputGroups(components)) {
                    MmceMachineComponents inputComponents = components.forInputGroup(inputGroupId);
                    CatalystSelection catalystSelection = selectCatalysts(controller, recipe, inputComponents, checkedParallelism, modifiers);
                    MmceRecipeModifiers effectiveModifiers = modifiers.withAdditional(catalystSelection.modifiers());
                    StartCheck check = canStart(controller, recipe, inputComponents, checkedParallelism, effectiveModifiers);
                    if (check.ok()) {
                        StartCheck postCheck = postCheckEvent(controller, machineId, run, recipe, checkedParallelism,
                                MmceRecipeStatus.RUNNING, "", MmceEventPhase.END);
                        if (postCheck.ok()) {
                            int finalParallelism = Math.max(1, Math.min(checkedParallelism, postCheck.parallelism()));
                            if (finalParallelism != checkedParallelism) {
                                catalystSelection = selectCatalysts(controller, recipe, inputComponents, finalParallelism, modifiers);
                                effectiveModifiers = modifiers.withAdditional(catalystSelection.modifiers());
                                check = canStart(controller, recipe, inputComponents, finalParallelism, effectiveModifiers);
                                if (!check.ok()) {
                                    lastFailure = check;
                                    continue;
                                }
                            }
                            return RecipeSearchResult.success(recipe, finalParallelism, catalystSelection, inputGroupId);
                        }
                        lastFailure = postCheck;
                        continue;
                    }
                    lastFailure = check;
                }
            }
            if (firstFailure.status() == MmceRecipeStatus.NO_RECIPE) {
                firstFailure = lastFailure;
            }
        }
        return RecipeSearchResult.failure(firstFailure.status(), firstFailure.detail());
    }

    private static List<Integer> candidateInputGroups(MmceMachineComponents components) {
        List<Integer> groups = components.inputGroups();
        return groups.isEmpty() ? List.of(-1) : groups;
    }

    private static CatalystSelection selectCatalysts(MachineControllerBlockEntity controller, MmceRecipeDefinition recipe,
                                                     MmceMachineComponents components, int parallelism,
                                                     MmceRecipeModifiers modifiers) {
        List<MmceMachineModifierDefinition> catalystModifiers = new ArrayList<>();
        List<CatalystPlan> plans = new ArrayList<>();
        ComponentViews views = ComponentViews.copy(components);
        for (SelectedRequirement selected : selectedRequirements(recipe)) {
            if (!(selected.parsed() instanceof MmceIngredientArrayRequirement requirement)
                    || !requirement.optional()
                    || requirement.ioType() != MmceIoType.INPUT
                    || requirement.modifiers().isEmpty()) {
                continue;
            }
            int consumed = views.itemInputs(selected.selectorTag()).consumeAnyParallel(controller, requirement, parallelism, modifiers);
            if (consumed > 0) {
                plans.add(new CatalystPlan(selected, consumed));
                addScaledCatalystModifiers(catalystModifiers, requirement.modifiers(), Math.max(1, consumed));
            }
        }
        return new CatalystSelection(List.copyOf(catalystModifiers), List.copyOf(plans));
    }

    private static void addCatalystModifiers(MachineControllerBlockEntity controller, RecipeRun run,
                                             MmceRecipeDefinition recipe, int parallelism,
                                             List<MmceMachineModifierDefinition> modifiers) {
        if (modifiers.isEmpty()) {
            return;
        }
        for (int index = 0; index < modifiers.size(); index++) {
            run.addModifier(controller, "mmce:catalyst/" + recipe.id() + "/" + parallelism + "/" + index,
                    modifierFromDefinition(modifiers.get(index)));
        }
    }

    private static void addScaledCatalystModifiers(List<MmceMachineModifierDefinition> output,
                                                   List<MmceMachineModifierDefinition> modifiers,
                                                   int parallelism) {
        for (MmceMachineModifierDefinition modifier : modifiers) {
            output.add(scaledModifier(modifier, parallelism));
        }
    }

    private static MmceMachineModifierDefinition scaledModifier(MmceMachineModifierDefinition modifier, int parallelism) {
        if (parallelism <= 1) {
            return modifier;
        }
        return new MmceMachineModifierDefinition(
                modifier.x(),
                modifier.y(),
                modifier.z(),
                modifier.elements(),
                modifier.description(),
                modifier.checkerId(),
                modifier.matchNbt(),
                modifier.previewNbt(),
                modifier.target(),
                modifier.ioType(),
                modifier.operation(),
                modifier.multiplier() * parallelism,
                modifier.affectChance(),
                modifier.rawJson()
        );
    }

    private static MmceRecipeModifier modifierFromDefinition(MmceMachineModifierDefinition modifier) {
        return new MmceRecipeModifier(
                modifier.target().toString(),
                modifier.ioType().map(type -> type.name().toLowerCase(java.util.Locale.ROOT)).orElse(""),
                modifier.multiplier(),
                modifier.operation(),
                modifier.affectChance()
        );
    }

    private static StartCheck canStart(MachineControllerBlockEntity controller, MmceRecipeDefinition recipe, MmceMachineComponents components, int parallelism, MmceRecipeModifiers modifiers) {
        StartCheck supportCheck = checkSupported(recipe);
        if (!supportCheck.ok()) {
            return supportCheck;
        }

        ComponentViews views = ComponentViews.copy(components);
        for (SelectedRequirement selected : selectedRequirements(recipe)) {
            MmceParsedRequirement requirement = selected.parsed();
            if (requirement instanceof MmceItemRequirement itemRequirement) {
                int amount = amountFor(itemRequirement, itemRequirement.amount(), parallelism, modifiers);
                if (itemRequirement.ioType() == MmceIoType.INPUT) {
                    if (!views.itemInputs(selected.selectorTag()).consume(controller, itemRequirement, amount)) {
                        return StartCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " item input");
                    }
                } else if (!itemRequirement.ignoreOutputCheck()
                        && !views.itemOutputs(selected.selectorTag()).insert(controller, itemRequirement, amount)) {
                    return StartCheck.failure(MmceRecipeStatus.OUTPUT_BLOCKED, recipe.id() + " item output");
                }
            } else if (requirement instanceof MmceFuelRequirement fuelRequirement) {
                int amount = amountFor(fuelRequirement, fuelRequirement.burnTime(), parallelism, modifiers);
                if (!views.itemInputs(selected.selectorTag()).consumeFuel(fuelRequirement, amount)) {
                    return StartCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " fuel input");
                }
            } else if (requirement instanceof MmceIngredientArrayRequirement arrayRequirement) {
                if (arrayRequirement.ioType() == MmceIoType.INPUT) {
                    if (!arrayRequirement.optional() && !views.itemInputs(selected.selectorTag()).consumeAny(controller, arrayRequirement, parallelism, modifiers)) {
                        return StartCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " item array input");
                    }
                } else if (!arrayRequirement.ignoreOutputCheck()
                        && !views.itemOutputs(selected.selectorTag()).insertAny(controller, arrayRequirement, parallelism, modifiers)) {
                    return StartCheck.failure(MmceRecipeStatus.OUTPUT_BLOCKED, recipe.id() + " item array output");
                }
            } else if (requirement instanceof MmceFluidRequirement fluidRequirement) {
                int amount = amountFor(fluidRequirement, fluidRequirement.amount(), parallelism, modifiers);
                if (fluidRequirement.ioType() == MmceIoType.INPUT) {
                    if (!views.fluidInputs(selected.selectorTag()).drain(fluidRequirement, amount)) {
                        return StartCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " fluid input");
                    }
                } else if (!fluidRequirement.ignoreOutputCheck()
                        && !views.fluidOutputs(selected.selectorTag()).fill(fluidRequirement, amount)) {
                    return StartCheck.failure(MmceRecipeStatus.OUTPUT_BLOCKED, recipe.id() + " fluid output");
                }
            } else if (requirement instanceof MmceChemicalRequirement chemicalRequirement) {
                int amount = amountFor(chemicalRequirement, chemicalRequirement.amount(), parallelism, modifiers);
                if (chemicalRequirement.ioType() == MmceIoType.INPUT) {
                    if (!views.chemicalInputs(selected.selectorTag()).drain(chemicalRequirement, amount)) {
                        return StartCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " gas input");
                    }
                } else if (!chemicalRequirement.ignoreOutputCheck()
                        && !views.chemicalOutputs(selected.selectorTag()).fill(chemicalRequirement, amount)) {
                    return StartCheck.failure(MmceRecipeStatus.OUTPUT_BLOCKED, recipe.id() + " gas output");
                }
            } else if (requirement instanceof MmceEnergyRequirement energyRequirement) {
                long energy = amountFor(energyRequirement, energyRequirement.energyPerTick(), parallelism, modifiers);
                if (energyRequirement.ioType() == MmceIoType.INPUT) {
                    if (!views.energyInputs(selected.selectorTag()).extract(energy)) {
                        return StartCheck.failure(MmceRecipeStatus.MISSING_ENERGY, recipe.id().toString());
                    }
                } else if (!energyRequirement.ignoreOutputCheck()
                        && !views.energyOutputs(selected.selectorTag()).receive(energy)) {
                    return StartCheck.failure(MmceRecipeStatus.OUTPUT_BLOCKED, recipe.id() + " energy output");
                }
            } else if (requirement instanceof MmceSmartInterfaceRequirement smartRequirement
                    && smartRequirement.ioType() == MmceIoType.INPUT
                    && !smartInterfaceInRange(components, selected.selectorTag(), smartRequirement)) {
                return StartCheck.failure(MmceRecipeStatus.MISSING_INPUT,
                        recipe.id() + " smart interface " + smartRequirement.interfaceType());
            }
        }

        return StartCheck.OK;
    }

    private static StartCheck postCheckEvent(MachineControllerBlockEntity controller, ResourceLocation machineId, RecipeRun run,
                                             MmceRecipeDefinition recipe, int parallelism, MmceRecipeStatus status,
                                             String detail, MmceEventPhase phase) {
        MmceRecipeCheckEvent event = bindRun(new MmceRecipeCheckEvent(controller, machineId, recipe.id(), phase, parallelism,
                run.isFactoryRun(), run.isCoreThread(), run.threadName(), status, detail), run);
        postRecipeEvent(event);
        if (event.isFailed()) {
            return StartCheck.failure(event.getRecipeStatus(), event.getCause());
        }
        if (event.isCanceled()) {
            return StartCheck.failure(status, event.getCause().isBlank() ? detail : event.getCause());
        }
        return StartCheck.success(status, detail, event.getParallelism());
    }

    private static StartCheck checkSupported(MmceRecipeDefinition recipe) {
        for (MmceRecipeRequirement requirement : recipe.requirements()) {
            if (requirement.parsed().isEmpty()) {
                return StartCheck.failure(MmceRecipeStatus.UNSUPPORTED_REQUIREMENT,
                        recipe.id() + " " + requirement.type());
            }
        }
        return StartCheck.OK;
    }

    private static TickCheck executePerTickRequirements(MachineControllerBlockEntity controller, RecipeRun run, ResourceLocation machineId,
                                                        MmceRecipeDefinition recipe, MmceMachineComponents components, int tick,
                                                        RandomSource random, int parallelism, MmceRecipeModifiers modifiers) {
        TickCheck simulation = simulatePerTickRequirements(controller, recipe, components, tick, parallelism, modifiers);
        if (!simulation.ok()) {
            return simulation;
        }
        return applyPerTickRequirements(controller, run, machineId, recipe, components, tick, random, parallelism, modifiers);
    }

    private static TickCheck simulatePerTickRequirements(MachineControllerBlockEntity controller, MmceRecipeDefinition recipe, MmceMachineComponents components, int tick, int parallelism, MmceRecipeModifiers modifiers) {
        ComponentViews views = ComponentViews.copy(components);
        for (SelectedRequirement selected : selectedRequirements(recipe)) {
            MmceParsedRequirement requirement = selected.parsed();
            if (requirement instanceof MmceFluidRequirement fluidRequirement && fluidRequirement.perTick()) {
                int amount = amountFor(fluidRequirement, fluidRequirement.amount(), parallelism, modifiers);
                if (fluidRequirement.ioType() == MmceIoType.INPUT) {
                    if (!views.fluidInputs(selected.selectorTag()).drain(fluidRequirement, amount)) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " fluid/t");
                    }
                } else if (!fluidRequirement.ignoreOutputCheck() && !views.fluidOutputs(selected.selectorTag()).fill(fluidRequirement, amount)) {
                    return TickCheck.failure(MmceRecipeStatus.OUTPUT_BLOCKED, recipe.id() + " fluid/t output");
                }
            } else if (requirement instanceof MmceChemicalRequirement chemicalRequirement && chemicalRequirement.perTick()) {
                int amount = amountFor(chemicalRequirement, chemicalRequirement.amount(), parallelism, modifiers);
                if (chemicalRequirement.ioType() == MmceIoType.INPUT) {
                    if (!views.chemicalInputs(selected.selectorTag()).drain(chemicalRequirement, amount)) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " gas/t");
                    }
                } else if (!chemicalRequirement.ignoreOutputCheck() && !views.chemicalOutputs(selected.selectorTag()).fill(chemicalRequirement, amount)) {
                    return TickCheck.failure(MmceRecipeStatus.OUTPUT_BLOCKED, recipe.id() + " gas/t output");
                }
            } else if (shouldTriggerInput(requirement, tick)) {
                if (requirement instanceof MmceItemRequirement itemRequirement) {
                    if (!views.itemInputs(selected.selectorTag()).consume(controller, itemRequirement, amountFor(itemRequirement, itemRequirement.amount(), parallelism, modifiers))) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " triggered item input");
                    }
                } else if (requirement instanceof MmceFuelRequirement fuelRequirement) {
                    if (!views.itemInputs(selected.selectorTag()).consumeFuel(fuelRequirement, amountFor(fuelRequirement, fuelRequirement.burnTime(), parallelism, modifiers))) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " triggered fuel input");
                    }
                } else if (requirement instanceof MmceIngredientArrayRequirement arrayRequirement) {
                    if (!arrayRequirement.optional() && !views.itemInputs(selected.selectorTag()).consumeAny(controller, arrayRequirement, parallelism, modifiers)) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " triggered item array input");
                    }
                } else if (requirement instanceof MmceFluidRequirement fluidRequirement && !fluidRequirement.perTick()) {
                    if (!views.fluidInputs(selected.selectorTag()).drain(fluidRequirement, amountFor(fluidRequirement, fluidRequirement.amount(), parallelism, modifiers))) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " triggered fluid input");
                    }
                } else if (requirement instanceof MmceChemicalRequirement chemicalRequirement && !chemicalRequirement.perTick()) {
                    if (!views.chemicalInputs(selected.selectorTag()).drain(chemicalRequirement, amountFor(chemicalRequirement, chemicalRequirement.amount(), parallelism, modifiers))) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " triggered gas input");
                    }
                }
            } else if (requirement instanceof MmceEnergyRequirement energyRequirement) {
                long energy = amountFor(energyRequirement, energyRequirement.energyPerTick(), parallelism, modifiers);
                if (energyRequirement.ioType() == MmceIoType.INPUT) {
                    if (!views.energyInputs(selected.selectorTag()).extract(energy)) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_ENERGY, recipe.id().toString());
                    }
                } else if (!energyRequirement.ignoreOutputCheck() && !views.energyOutputs(selected.selectorTag()).receive(energy)) {
                    return TickCheck.failure(MmceRecipeStatus.OUTPUT_BLOCKED, recipe.id() + " energy output");
                }
            }
        }
        return TickCheck.OK;
    }

    private static TickCheck applyPerTickRequirements(MachineControllerBlockEntity controller, RecipeRun run, ResourceLocation machineId,
                                                      MmceRecipeDefinition recipe, MmceMachineComponents components, int tick,
                                                      RandomSource random, int parallelism, MmceRecipeModifiers modifiers) {
        ComponentViews views = ComponentViews.live(components);
        for (SelectedRequirement selected : selectedRequirements(recipe)) {
            MmceParsedRequirement requirement = selected.parsed();
            if (requirement instanceof MmceFluidRequirement fluidRequirement && fluidRequirement.perTick()) {
                int amount = amountFor(fluidRequirement, fluidRequirement.amount(), parallelism, modifiers);
                if (fluidRequirement.ioType() == MmceIoType.INPUT) {
                    if (drainFluid(components.fluidInputs(selected.selectorTag()), fluidRequirement, amount) < amount) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " fluid/t");
                    }
                } else if (fillFluid(components.fluidOutputs(selected.selectorTag()), fluidRequirement, amount) < amount
                        && !fluidRequirement.ignoreOutputCheck()) {
                    return TickCheck.failure(MmceRecipeStatus.OUTPUT_BLOCKED, recipe.id() + " fluid/t output");
                }
            } else if (requirement instanceof MmceChemicalRequirement chemicalRequirement && chemicalRequirement.perTick()) {
                int amount = amountFor(chemicalRequirement, chemicalRequirement.amount(), parallelism, modifiers);
                if (chemicalRequirement.ioType() == MmceIoType.INPUT) {
                    if (drainChemical(components.fluidInputs(selected.selectorTag()), chemicalRequirement, amount) < amount) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " gas/t");
                    }
                } else if (fillChemical(components.fluidOutputs(selected.selectorTag()), chemicalRequirement, amount) < amount
                        && !chemicalRequirement.ignoreOutputCheck()) {
                    return TickCheck.failure(MmceRecipeStatus.OUTPUT_BLOCKED, recipe.id() + " gas/t output");
                }
            } else if (shouldTriggerInput(requirement, tick)) {
                if (!rollChance(controller, run, machineId, recipe, requirement, random, parallelism, modifiers)) {
                    continue;
                }
                if (requirement instanceof MmceItemRequirement itemRequirement) {
                    int amount = amountFor(itemRequirement, itemRequirement.amount(), parallelism, modifiers);
                    if (consumeItem(controller, components.itemInputs(selected.selectorTag()), itemRequirement, amount) < amount) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " triggered item input");
                    }
                } else if (requirement instanceof MmceFuelRequirement fuelRequirement) {
                    int amount = amountFor(fuelRequirement, fuelRequirement.burnTime(), parallelism, modifiers);
                    if (consumeFuel(components.itemInputs(selected.selectorTag()), fuelRequirement, amount) < amount) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " triggered fuel input");
                    }
                } else if (requirement instanceof MmceIngredientArrayRequirement arrayRequirement) {
                    int consumed = consumeIngredientArrayInput(controller, components.itemInputs(selected.selectorTag()), arrayRequirement, parallelism, modifiers);
                    if (!arrayRequirement.optional() && consumed <= 0) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " triggered item array input");
                    }
                } else if (requirement instanceof MmceFluidRequirement fluidRequirement && !fluidRequirement.perTick()) {
                    int amount = amountFor(fluidRequirement, fluidRequirement.amount(), parallelism, modifiers);
                    if (drainFluid(components.fluidInputs(selected.selectorTag()), fluidRequirement, amount) < amount) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " triggered fluid input");
                    }
                } else if (requirement instanceof MmceChemicalRequirement chemicalRequirement && !chemicalRequirement.perTick()) {
                    int amount = amountFor(chemicalRequirement, chemicalRequirement.amount(), parallelism, modifiers);
                    if (drainChemical(components.fluidInputs(selected.selectorTag()), chemicalRequirement, amount) < amount) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_INPUT, recipe.id() + " triggered gas input");
                    }
                }
            } else if (requirement instanceof MmceEnergyRequirement energyRequirement) {
                long energy = amountFor(energyRequirement, energyRequirement.energyPerTick(), parallelism, modifiers);
                if (energyRequirement.ioType() == MmceIoType.INPUT) {
                    if (!views.energyInputs(selected.selectorTag()).extract(energy)) {
                        return TickCheck.failure(MmceRecipeStatus.MISSING_ENERGY, recipe.id().toString());
                    }
                } else if (!views.energyOutputs(selected.selectorTag()).receive(energy) && !energyRequirement.ignoreOutputCheck()) {
                    return TickCheck.failure(MmceRecipeStatus.OUTPUT_BLOCKED, recipe.id() + " energy output");
                }
            }
        }
        return TickCheck.OK;
    }

    private static boolean consumeStartInputs(MachineControllerBlockEntity controller, RecipeRun run, ResourceLocation machineId,
                                              MmceRecipeDefinition recipe, MmceMachineComponents components, RandomSource random,
                                              int parallelism, MmceRecipeModifiers baseModifiers, MmceRecipeModifiers effectiveModifiers,
                                              CatalystSelection catalystSelection) {
        for (CatalystPlan plan : catalystSelection.plans()) {
            MmceIngredientArrayRequirement requirement = (MmceIngredientArrayRequirement) plan.selected().parsed();
            int consumed = consumeIngredientArrayInputPartial(controller, components.itemInputs(plan.selected().selectorTag()),
                    requirement, plan.parallelism(), baseModifiers);
            if (consumed < plan.parallelism()) {
                return false;
            }
        }
        for (SelectedRequirement selected : selectedRequirements(recipe)) {
            MmceParsedRequirement requirement = selected.parsed();
            if (catalystSelection.contains(selected)
                    || requirement.triggerTime() > 0
                    || !rollChance(controller, run, machineId, recipe, requirement, random, parallelism, effectiveModifiers)) {
                continue;
            }
            if (requirement instanceof MmceItemRequirement itemRequirement && itemRequirement.ioType() == MmceIoType.INPUT) {
                consumeItem(controller, components.itemInputs(selected.selectorTag()), itemRequirement, amountFor(itemRequirement, itemRequirement.amount(), parallelism, effectiveModifiers));
            } else if (requirement instanceof MmceFuelRequirement fuelRequirement) {
                consumeFuel(components.itemInputs(selected.selectorTag()), fuelRequirement, amountFor(fuelRequirement, fuelRequirement.burnTime(), parallelism, effectiveModifiers));
            } else if (requirement instanceof MmceIngredientArrayRequirement arrayRequirement && arrayRequirement.ioType() == MmceIoType.INPUT) {
                consumeIngredientArrayInput(controller, components.itemInputs(selected.selectorTag()), arrayRequirement, parallelism, effectiveModifiers);
            } else if (requirement instanceof MmceFluidRequirement fluidRequirement
                    && fluidRequirement.ioType() == MmceIoType.INPUT
                    && !fluidRequirement.perTick()) {
                drainFluid(components.fluidInputs(selected.selectorTag()), fluidRequirement, amountFor(fluidRequirement, fluidRequirement.amount(), parallelism, effectiveModifiers));
            } else if (requirement instanceof MmceChemicalRequirement chemicalRequirement
                    && chemicalRequirement.ioType() == MmceIoType.INPUT
                    && !chemicalRequirement.perTick()) {
                drainChemical(components.fluidInputs(selected.selectorTag()), chemicalRequirement, amountFor(chemicalRequirement, chemicalRequirement.amount(), parallelism, effectiveModifiers));
            }
        }
        return true;
    }

    private static boolean canFinish(MachineControllerBlockEntity controller, MmceRecipeDefinition recipe, MmceMachineComponents components, int parallelism, MmceRecipeModifiers modifiers) {
        ComponentViews views = ComponentViews.copy(components);

        for (SelectedRequirement selected : selectedRequirements(recipe)) {
            MmceParsedRequirement requirement = selected.parsed();
            if (requirement.ignoreOutputCheck()) {
                continue;
            }
            if (requirement instanceof MmceItemRequirement itemRequirement && itemRequirement.ioType() == MmceIoType.OUTPUT) {
                if (!views.itemOutputs(selected.selectorTag()).insert(controller, itemRequirement, maxAmountFor(itemRequirement, parallelism, modifiers))) {
                    return false;
                }
            } else if (requirement instanceof MmceIngredientArrayRequirement arrayRequirement && arrayRequirement.ioType() == MmceIoType.OUTPUT) {
                if (!views.itemOutputs(selected.selectorTag()).insertAny(controller, arrayRequirement, parallelism, modifiers)) {
                    return false;
                }
            } else if (requirement instanceof MmceFluidRequirement fluidRequirement
                    && fluidRequirement.ioType() == MmceIoType.OUTPUT
                    && !fluidRequirement.perTick()) {
                if (!views.fluidOutputs(selected.selectorTag()).fill(fluidRequirement, amountFor(fluidRequirement, fluidRequirement.amount(), parallelism, modifiers))) {
                    return false;
                }
            } else if (requirement instanceof MmceChemicalRequirement chemicalRequirement
                    && chemicalRequirement.ioType() == MmceIoType.OUTPUT
                    && !chemicalRequirement.perTick()) {
                if (!views.chemicalOutputs(selected.selectorTag()).fill(chemicalRequirement, amountFor(chemicalRequirement, chemicalRequirement.amount(), parallelism, modifiers))) {
                    return false;
                }
            }
        }

        return true;
    }

    private static void finishOrBlock(MachineControllerBlockEntity controller, RecipeRun run, MmceRecipeDefinition recipe, MmceMachineComponents components, RandomSource random, int parallelism, MmceRecipeModifiers modifiers) {
        if (canFinish(controller, recipe, components, parallelism, modifiers)) {
            MmceRecipeEvent finishEvent = postRecipeEvent(finishEvent(controller, run,
                    controller.getMachineId().orElse(recipe.machineId()), recipe, modifiers, parallelism));
            if (finishEvent.isFailed()) {
                run.setWorking(false);
                run.setRecipeStatus(finishEvent.getRecipeStatus(), finishEvent.getCause());
                if (finishEvent.isDestructRecipe()) {
                    run.clearActiveRecipe();
                }
                postRecipeEvent(failureEvent(controller, run, controller.getMachineId().orElse(recipe.machineId()),
                        recipe, modifiers, parallelism, finishEvent.getRecipeStatus(), finishEvent.getCause(),
                        finishEvent.isDestructRecipe()));
                return;
            }
            if (finishEvent.isCanceled()) {
                run.setWorking(false);
                run.setRecipeStatus(MmceRecipeStatus.OUTPUT_BLOCKED,
                        finishEvent.getCause().isBlank() ? runningDetail(recipe, parallelism) : finishEvent.getCause());
                return;
            }
            finishOutputs(controller, run, controller.getMachineId().orElse(recipe.machineId()), recipe, components,
                    random, parallelism, modifiers);
            recipe.finishCommands().ifPresent(commands -> MmceRecipeCommands.run(controller, commands, 0));
            run.setWorking(false);
            run.setRecipeStatus(MmceRecipeStatus.FINISHED, runningDetail(recipe, parallelism));
            run.clearActiveRecipe();
        } else {
            run.setWorking(false);
            run.setRecipeStatus(MmceRecipeStatus.OUTPUT_BLOCKED, runningDetail(recipe, parallelism));
        }
    }

    private static MmceRecipeEvent postRecipeEvent(MmceRecipeEvent event) {
        return MmceEventRegistry.postRecipe(event);
    }

    private static <E extends MmceRecipeEvent> E bindRun(E event, RecipeRun run) {
        event.bindRun(run);
        return event;
    }

    private static MmceRecipeEvent startEvent(MachineControllerBlockEntity controller, RecipeRun run, ResourceLocation machineId,
                                             MmceRecipeDefinition recipe, MmceRecipeModifiers modifiers, int parallelism) {
        int recipeTime = recipeTime(recipe, modifiers);
        if (run.isFactoryRun()) {
            return bindRun(new MmceFactoryRecipeStartEvent(controller, machineId, recipe.id(), recipeTime, parallelism,
                    run.isCoreThread(), run.threadName()), run);
        }
        return bindRun(new MmceRecipeStartEvent(controller, machineId, recipe.id(), recipeTime, parallelism, false, false, ""), run);
    }

    private static MmceRecipeEvent tickEvent(MachineControllerBlockEntity controller, RecipeRun run, ResourceLocation machineId,
                                            MmceRecipeDefinition recipe, MmceRecipeModifiers modifiers, int parallelism,
                                            MmceEventPhase phase) {
        int progress = run.getRecipeProgress();
        int recipeTime = recipeTime(recipe, modifiers);
        if (run.isFactoryRun()) {
            return bindRun(new MmceFactoryRecipeTickEvent(controller, machineId, recipe.id(), phase, progress, recipeTime,
                    parallelism, run.isCoreThread(), run.threadName()), run);
        }
        return bindRun(new MmceRecipeTickEvent(controller, machineId, recipe.id(), phase, progress, recipeTime, parallelism,
                false, false, ""), run);
    }

    private static MmceRecipeEvent failureEvent(MachineControllerBlockEntity controller, RecipeRun run, ResourceLocation machineId,
                                               MmceRecipeDefinition recipe, MmceRecipeModifiers modifiers, int parallelism,
                                               MmceRecipeStatus status, String cause, boolean destructRecipe) {
        int recipeTime = recipeTime(recipe, modifiers);
        if (run.isFactoryRun()) {
            return bindRun(new MmceFactoryRecipeFailureEvent(controller, machineId, recipe.id(), run.getRecipeProgress(), recipeTime,
                    parallelism, run.isCoreThread(), run.threadName(), status, cause, destructRecipe), run);
        }
        return bindRun(new MmceRecipeFailureEvent(controller, machineId, recipe.id(), run.getRecipeProgress(), recipeTime,
                parallelism, false, false, "", status, cause, destructRecipe), run);
    }

    private static MmceRecipeEvent finishEvent(MachineControllerBlockEntity controller, RecipeRun run, ResourceLocation machineId,
                                              MmceRecipeDefinition recipe, MmceRecipeModifiers modifiers, int parallelism) {
        int recipeTime = recipeTime(recipe, modifiers);
        if (run.isFactoryRun()) {
            return bindRun(new MmceFactoryRecipeFinishEvent(controller, machineId, recipe.id(), run.getRecipeProgress(), recipeTime,
                    parallelism, run.isCoreThread(), run.threadName()), run);
        }
        return bindRun(new MmceRecipeFinishEvent(controller, machineId, recipe.id(), run.getRecipeProgress(), recipeTime,
                parallelism, false, false, ""), run);
    }

    private static int recipeTime(MmceRecipeDefinition recipe, MmceRecipeModifiers modifiers) {
        return Math.max(1, modifiers.recipeTime(recipe.recipeTime()));
    }

    private static void finishOutputs(MachineControllerBlockEntity controller, RecipeRun run, ResourceLocation machineId,
                                      MmceRecipeDefinition recipe, MmceMachineComponents components, RandomSource random,
                                      int parallelism, MmceRecipeModifiers modifiers) {
        for (SelectedRequirement selected : selectedRequirements(recipe)) {
            MmceParsedRequirement requirement = selected.parsed();
            if (requirement.ioType() != MmceIoType.OUTPUT
                    || !rollChance(controller, run, machineId, recipe, requirement, random, parallelism, modifiers)) {
                continue;
            }

            if (requirement instanceof MmceItemRequirement itemRequirement) {
                insertItem(controller, components.itemOutputs(selected.selectorTag()), itemRequirement, randomAmountFor(itemRequirement, random, parallelism, modifiers));
            } else if (requirement instanceof MmceIngredientArrayRequirement arrayRequirement) {
                insertIngredientArrayOutput(controller, components.itemOutputs(selected.selectorTag()), arrayRequirement, random, parallelism, modifiers);
            } else if (requirement instanceof MmceFluidRequirement fluidRequirement && !fluidRequirement.perTick()) {
                fillFluid(components.fluidOutputs(selected.selectorTag()), fluidRequirement, amountFor(fluidRequirement, fluidRequirement.amount(), parallelism, modifiers));
            } else if (requirement instanceof MmceChemicalRequirement chemicalRequirement && !chemicalRequirement.perTick()) {
                fillChemical(components.fluidOutputs(selected.selectorTag()), chemicalRequirement, amountFor(chemicalRequirement, chemicalRequirement.amount(), parallelism, modifiers));
            }
        }
    }

    private static List<SelectedRequirement> selectedRequirements(MmceRecipeDefinition recipe) {
        return recipe.requirements().stream()
                .filter(requirement -> requirement.parsed().isPresent())
                .map(requirement -> new SelectedRequirement(requirement.parsed().get(), requirement.selectorTag()))
                .toList();
    }

    private static boolean shouldTriggerInput(MmceParsedRequirement requirement, int tick) {
        return requirement.ioType() == MmceIoType.INPUT
                && requirement.triggerTime() > 0
                && requirement.triggerTime() == tick
                && !(requirement instanceof MmceFluidRequirement fluidRequirement && fluidRequirement.perTick())
                && !(requirement instanceof MmceChemicalRequirement chemicalRequirement && chemicalRequirement.perTick())
                && !(requirement instanceof MmceEnergyRequirement);
    }

    private static int activeParallelism(RecipeRun run, MmceRecipeDefinition recipe) {
        return recipe.parallelized() ? Math.max(1, run.getActiveRecipeParallelism()) : 1;
    }

    private static int clampActiveParallelism(RecipeRun run, MmceRecipeDefinition recipe, int maxParallelism) {
        if (!recipe.parallelized()) {
            run.setActiveRecipeParallelism(1);
            return 1;
        }
        int active = Math.max(1, run.getActiveRecipeParallelism());
        int clamped = Math.max(1, Math.min(active, maxParallelism));
        if (clamped != active) {
            run.setActiveRecipeParallelism(clamped);
        }
        return clamped;
    }

    private static String runningDetail(MmceRecipeDefinition recipe, int parallelism) {
        return parallelism > 1 ? recipe.id() + " x" + parallelism : recipe.id().toString();
    }

    private static int amountFor(MmceParsedRequirement requirement, int amount, int parallelism, MmceRecipeModifiers modifiers) {
        int modifiedAmount = modifiedAmountFor(requirement, amount, modifiers);
        if (requirement.parallelizeUnaffected()) {
            return modifiedAmount;
        }
        long multiplied = (long) modifiedAmount * Math.max(1, parallelism);
        return multiplied > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) multiplied;
    }

    private static int randomAmountFor(MmceItemRequirement requirement, RandomSource random, int parallelism, MmceRecipeModifiers modifiers) {
        if (requirement.minAmount() >= requirement.maxAmount()) {
            return amountFor(requirement, requirement.maxAmount(), parallelism, modifiers);
        }
        int amount = requirement.minAmount() + random.nextInt(requirement.maxAmount() - requirement.minAmount() + 1);
        return amountFor(requirement, amount, parallelism, modifiers);
    }

    private static int maxAmountFor(MmceItemRequirement requirement, int parallelism, MmceRecipeModifiers modifiers) {
        return amountFor(requirement, requirement.maxAmount(), parallelism, modifiers);
    }

    private static long amountFor(MmceParsedRequirement requirement, long amount, int parallelism, MmceRecipeModifiers modifiers) {
        long modifiedAmount = modifiers.energyAmount(requirement, amount);
        if (requirement.parallelizeUnaffected()) {
            return modifiedAmount;
        }
        int safeParallelism = Math.max(1, parallelism);
        if (modifiedAmount > Long.MAX_VALUE / safeParallelism) {
            return Long.MAX_VALUE;
        }
        return modifiedAmount * safeParallelism;
    }

    private static int modifiedAmountFor(MmceParsedRequirement requirement, int amount, MmceRecipeModifiers modifiers) {
        if (requirement instanceof MmceFluidRequirement) {
            return modifiers.fluidAmount(requirement, amount);
        }
        if (requirement instanceof MmceChemicalRequirement) {
            return modifiers.gasAmount(requirement, amount);
        }
        return modifiers.itemAmount(requirement, amount);
    }

    private static boolean rollChance(float chance, RandomSource random) {
        if (chance >= 1.0F) {
            return true;
        }
        if (chance <= 0.0F) {
            return false;
        }
        return random.nextFloat() < chance;
    }

    private static boolean rollChance(MachineControllerBlockEntity controller, RecipeRun run, ResourceLocation machineId,
                                      MmceRecipeDefinition recipe, MmceParsedRequirement requirement, RandomSource random,
                                      int parallelism, MmceRecipeModifiers modifiers) {
        float chance = modifiers.chance(requirement, requirement.chance());
        MmceResultChanceCreateEvent event = bindRun(new MmceResultChanceCreateEvent(
                controller,
                machineId,
                recipe.id(),
                run.getRecipeProgress(),
                recipeTime(recipe, modifiers),
                parallelism,
                run.isFactoryRun(),
                run.isCoreThread(),
                run.threadName(),
                requirement,
                chance
        ), run);
        postRecipeEvent(event);
        return !event.isCanceled() && rollChance(event.getChance(), random);
    }

    private static boolean smartInterfaceInRange(MmceMachineComponents components, Optional<String> selectorTag, MmceSmartInterfaceRequirement requirement) {
        for (var smartInterface : components.smartInterfaces(selectorTag)) {
            Optional<Float> value = smartInterface.value(requirement.interfaceType());
            if (value.isPresent() && value.get() >= requirement.minValue() && value.get() <= requirement.maxValue()) {
                return true;
            }
        }
        return false;
    }

    private record SelectedRequirement(MmceParsedRequirement parsed, Optional<String> selectorTag) {
    }

    private static final class ComponentViews {
        private final MmceMachineComponents components;
        private final boolean liveEnergy;
        private final Map<Optional<String>, ItemInventory> itemInputs = new LinkedHashMap<>();
        private final Map<Optional<String>, ItemInventory> itemOutputs = new LinkedHashMap<>();
        private final Map<Optional<String>, FluidInventory> fluidInputs = new LinkedHashMap<>();
        private final Map<Optional<String>, FluidInventory> fluidOutputs = new LinkedHashMap<>();
        private final Map<Optional<String>, ChemicalInventory> chemicalInputs = new LinkedHashMap<>();
        private final Map<Optional<String>, ChemicalInventory> chemicalOutputs = new LinkedHashMap<>();
        private final Map<Optional<String>, EnergyInventory> energyInputs = new LinkedHashMap<>();
        private final Map<Optional<String>, EnergyInventory> energyOutputs = new LinkedHashMap<>();

        private ComponentViews(MmceMachineComponents components, boolean liveEnergy) {
            this.components = components;
            this.liveEnergy = liveEnergy;
        }

        static ComponentViews copy(MmceMachineComponents components) {
            return new ComponentViews(components, false);
        }

        static ComponentViews live(MmceMachineComponents components) {
            return new ComponentViews(components, true);
        }

        ItemInventory itemInputs(Optional<String> selectorTag) {
            return itemInputs.computeIfAbsent(selectorTag, tag -> ItemInventory.copyOf(components.itemInputs(tag)));
        }

        ItemInventory itemOutputs(Optional<String> selectorTag) {
            return itemOutputs.computeIfAbsent(selectorTag, tag -> ItemInventory.copyOf(components.itemOutputs(tag)));
        }

        FluidInventory fluidInputs(Optional<String> selectorTag) {
            return fluidInputs.computeIfAbsent(selectorTag, tag -> FluidInventory.copyOf(components.fluidInputs(tag)));
        }

        FluidInventory fluidOutputs(Optional<String> selectorTag) {
            return fluidOutputs.computeIfAbsent(selectorTag, tag -> FluidInventory.copyOf(components.fluidOutputs(tag)));
        }

        ChemicalInventory chemicalInputs(Optional<String> selectorTag) {
            return chemicalInputs.computeIfAbsent(selectorTag, tag -> ChemicalInventory.copyOf(components.fluidInputs(tag)));
        }

        ChemicalInventory chemicalOutputs(Optional<String> selectorTag) {
            return chemicalOutputs.computeIfAbsent(selectorTag, tag -> ChemicalInventory.copyOf(components.fluidOutputs(tag)));
        }

        EnergyInventory energyInputs(Optional<String> selectorTag) {
            return energyInputs.computeIfAbsent(selectorTag, tag -> liveEnergy
                    ? EnergyInventory.inputLive(components.energyInputs(tag))
                    : EnergyInventory.inputCopyOf(components.energyInputs(tag)));
        }

        EnergyInventory energyOutputs(Optional<String> selectorTag) {
            return energyOutputs.computeIfAbsent(selectorTag, tag -> liveEnergy
                    ? EnergyInventory.outputLive(components.energyOutputs(tag))
                    : EnergyInventory.outputCopyOf(components.energyOutputs(tag)));
        }
    }

    private static RunTickResult currentResult(RecipeRun run) {
        return new RunTickResult(
                run.getActiveRecipeId().isPresent(),
                run.isWorking(),
                run.getRecipeStatus(),
                run.getRecipeStatusDetail()
        );
    }

    public interface RecipeRun {
        Optional<ResourceLocation> getActiveRecipeId();

        int getRecipeProgress();

        int getActiveRecipeParallelism();

        default void setActiveRecipeParallelism(int parallelism) {
        }

        default int getActiveInputGroupId() {
            return -1;
        }

        default void setActiveInputGroupId(int inputGroupId) {
        }

        MmceRecipeStatus getRecipeStatus();

        String getRecipeStatusDetail();

        boolean isWorking();

        void startRecipe(ResourceLocation recipeId, int parallelism);

        int advanceRecipeProgress();

        default void rewindRecipeProgress() {
        }

        default void setRecipeProgress(int progress) {
        }

        void clearActiveRecipe();

        void setWorking(boolean working);

        void setRecipeStatus(MmceRecipeStatus status);

        void setRecipeStatus(MmceRecipeStatus status, String detail);

        default boolean isFactoryRun() {
            return false;
        }

        default boolean isCoreThread() {
            return false;
        }

        default String threadName() {
            return "";
        }

        default List<MmceMachineModifierDefinition> getRecipeModifiers(MachineControllerBlockEntity controller) {
            return controller.getRecipeModifiers();
        }

        default void addModifier(MachineControllerBlockEntity controller, String key, MmceRecipeModifier modifier) {
            controller.addModifier(key, modifier);
        }

        default void removeModifier(MachineControllerBlockEntity controller, String key) {
            controller.removeModifier(key);
        }

        default boolean hasModifier(MachineControllerBlockEntity controller, String key) {
            return controller.hasModifier(key);
        }

        default void addPermanentModifier(MachineControllerBlockEntity controller, String key, MmceRecipeModifier modifier) {
            controller.addPermanentModifier(key, modifier);
        }

        default void removePermanentModifier(MachineControllerBlockEntity controller, String key) {
            controller.removePermanentModifier(key);
        }

        default boolean hasPermanentModifier(MachineControllerBlockEntity controller, String key) {
            return controller.hasPermanentModifier(key);
        }
    }

    public record RunTickResult(boolean active, boolean working, MmceRecipeStatus status, String detail) {
    }

    private record ControllerRun(MachineControllerBlockEntity controller) implements RecipeRun {
        @Override
        public Optional<ResourceLocation> getActiveRecipeId() {
            return controller.getActiveRecipeId();
        }

        @Override
        public int getRecipeProgress() {
            return controller.getRecipeProgress();
        }

        @Override
        public int getActiveRecipeParallelism() {
            return controller.getActiveRecipeParallelism();
        }

        @Override
        public void setActiveRecipeParallelism(int parallelism) {
            controller.setActiveRecipeParallelism(parallelism);
        }

        @Override
        public int getActiveInputGroupId() {
            return controller.getActiveInputGroupId();
        }

        @Override
        public void setActiveInputGroupId(int inputGroupId) {
            controller.setActiveInputGroupId(inputGroupId);
        }

        @Override
        public MmceRecipeStatus getRecipeStatus() {
            return controller.getRecipeStatus();
        }

        @Override
        public String getRecipeStatusDetail() {
            return controller.getRecipeStatusDetail();
        }

        @Override
        public boolean isWorking() {
            return controller.isWorking();
        }

        @Override
        public void startRecipe(ResourceLocation recipeId, int parallelism) {
            controller.startRecipe(recipeId, parallelism);
        }

        @Override
        public int advanceRecipeProgress() {
            return controller.advanceRecipeProgress();
        }

        @Override
        public void setRecipeProgress(int progress) {
            controller.setRecipeProgress(progress);
        }

        @Override
        public void clearActiveRecipe() {
            controller.clearActiveRecipe();
        }

        @Override
        public void setWorking(boolean working) {
            controller.setWorking(working);
        }

        @Override
        public void setRecipeStatus(MmceRecipeStatus status) {
            controller.setRecipeStatus(status);
        }

        @Override
        public void setRecipeStatus(MmceRecipeStatus status, String detail) {
            controller.setRecipeStatus(status, detail);
        }
    }

    private record RecipeSearchResult(Optional<MmceRecipeDefinition> recipe, int parallelism, MmceRecipeStatus status,
                                      String detail, CatalystSelection catalystSelection, int inputGroupId) {
        static RecipeSearchResult success(MmceRecipeDefinition recipe, int parallelism) {
            return success(recipe, parallelism, CatalystSelection.EMPTY);
        }

        static RecipeSearchResult success(MmceRecipeDefinition recipe, int parallelism, CatalystSelection catalystSelection) {
            return success(recipe, parallelism, catalystSelection, -1);
        }

        static RecipeSearchResult success(MmceRecipeDefinition recipe, int parallelism, CatalystSelection catalystSelection, int inputGroupId) {
            return new RecipeSearchResult(Optional.of(recipe), Math.max(1, parallelism), MmceRecipeStatus.RUNNING,
                    runningDetail(recipe, parallelism), catalystSelection == null ? CatalystSelection.EMPTY : catalystSelection,
                    inputGroupId < 0 ? -1 : inputGroupId);
        }

        static RecipeSearchResult failure(MmceRecipeStatus status, String detail) {
            return new RecipeSearchResult(Optional.empty(), 1, status, detail, CatalystSelection.EMPTY, -1);
        }

        List<MmceMachineModifierDefinition> catalystModifiers() {
            return catalystSelection.modifiers();
        }
    }

    private record CatalystSelection(List<MmceMachineModifierDefinition> modifiers, List<CatalystPlan> plans) {
        private static final CatalystSelection EMPTY = new CatalystSelection(List.of(), List.of());

        boolean contains(SelectedRequirement selected) {
            return plans.stream().anyMatch(plan -> plan.selected().equals(selected));
        }
    }

    private record CatalystPlan(SelectedRequirement selected, int parallelism) {
        private CatalystPlan {
            parallelism = Math.max(1, parallelism);
        }
    }

    private record StartCheck(boolean ok, MmceRecipeStatus status, String detail, int parallelism) {
        private static final StartCheck OK = success(MmceRecipeStatus.RUNNING, "", 1);

        static StartCheck success(MmceRecipeStatus status, String detail, int parallelism) {
            return new StartCheck(true, status, detail, Math.max(1, parallelism));
        }

        static StartCheck failure(MmceRecipeStatus status, String detail) {
            return new StartCheck(false, status, detail, 1);
        }
    }

    private record TickCheck(boolean ok, MmceRecipeStatus status, String detail) {
        private static final TickCheck OK = new TickCheck(true, MmceRecipeStatus.RUNNING, "");

        static TickCheck failure(MmceRecipeStatus status, String detail) {
            return new TickCheck(false, status, detail);
        }
    }

    private static int consumeItem(MachineControllerBlockEntity controller, List<ItemBusBlockEntity> buses, MmceItemRequirement requirement, int amount) {
        if (requirement.durabilityCost() > 0) {
            return damageItem(controller, buses, requirement, amount);
        }

        int remaining = amount;
        for (ItemBusBlockEntity bus : buses) {
            for (int slot = 0; slot < bus.getContainerSize() && remaining > 0; slot++) {
                ItemStack stack = bus.getItem(slot);
                if (requirement.matches(controller, stack)) {
                    int consumed = consumeItemStack(bus, slot, stack, requirement, remaining);
                    remaining -= consumed;
                }
            }
            if (remaining <= 0) {
                break;
            }
        }
        return amount - remaining;
    }

    private static int consumeItemStack(ItemBusBlockEntity bus, int slot, ItemStack stack, MmceItemRequirement requirement, int remaining) {
        if (!requirement.returnCraftingRemainder() || !stack.hasCraftingRemainingItem()) {
            int consumed = Math.min(remaining, stack.getCount());
            bus.removeItem(slot, consumed);
            return consumed;
        }

        int consumed = 0;
        while (consumed < remaining) {
            ItemStack current = bus.getItem(slot);
            if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, stack)) {
                break;
            }
            ItemStack remainder = current.getCraftingRemainingItem();
            ItemStack consumedOne = bus.removeItem(slot, 1);
            if (consumedOne.isEmpty()) {
                break;
            }
            if (!remainder.isEmpty() && !insertCraftingRemainder(bus, slot, remainder)) {
                ItemStack restored = consumedOne.copyWithCount(1);
                if (bus.getItem(slot).isEmpty()) {
                    bus.setItem(slot, restored);
                } else {
                    insertStack(List.of(bus), restored);
                }
                break;
            }
            consumed++;
        }
        return consumed;
    }

    private static boolean insertCraftingRemainder(ItemBusBlockEntity bus, int preferredSlot, ItemStack remainder) {
        if (remainder.isEmpty()) {
            return true;
        }
        ItemStack preferred = bus.getItem(preferredSlot);
        int max = Math.min(bus.getMaxStackSize(remainder), remainder.getMaxStackSize());
        if (preferred.isEmpty()) {
            bus.setItem(preferredSlot, remainder.copyWithCount(1));
            return true;
        }
        if (ItemStack.isSameItemSameComponents(preferred, remainder) && preferred.getCount() < max) {
            ItemStack merged = preferred.copy();
            merged.grow(1);
            bus.setItem(preferredSlot, merged);
            return true;
        }
        return insertStack(List.of(bus), remainder.copyWithCount(1)) == 1;
    }

    private static int damageItem(MachineControllerBlockEntity controller, List<ItemBusBlockEntity> buses, MmceItemRequirement requirement, int amount) {
        int remaining = amount;
        for (ItemBusBlockEntity bus : buses) {
            for (int slot = 0; slot < bus.getContainerSize() && remaining > 0; slot++) {
                ItemStack stack = bus.getItem(slot);
                if (!requirement.matches(controller, stack) || !stack.isDamageableItem()) {
                    continue;
                }

                ItemStack damaged = stack.copy();
                damaged.setDamageValue(damaged.getDamageValue() + requirement.durabilityCost());
                if (damaged.getDamageValue() >= damaged.getMaxDamage()) {
                    damaged.shrink(1);
                }
                bus.setItem(slot, damaged);
                remaining--;
            }
            if (remaining <= 0) {
                break;
            }
        }
        return amount - remaining;
    }

    private static int consumeFuel(List<ItemBusBlockEntity> buses, MmceFuelRequirement requirement, int burnTime) {
        int remaining = burnTime;
        for (ItemBusBlockEntity bus : buses) {
            for (int slot = 0; slot < bus.getContainerSize() && remaining > 0; slot++) {
                ItemStack stack = bus.getItem(slot);
                int consumed = consumeFuelStack(requirement, stack, remaining);
                if (consumed <= 0) {
                    continue;
                }
                remaining -= consumed;
                bus.setItem(slot, consumedFuelResult(stack, consumed));
            }
            if (remaining <= 0) {
                break;
            }
        }
        return burnTime - remaining;
    }

    private static int consumeFuelStack(MmceFuelRequirement requirement, ItemStack stack, int remainingBurnTime) {
        if (!requirement.matches(stack)) {
            return 0;
        }
        int burnTime = stack.getBurnTime(null);
        if (burnTime <= 0) {
            return 0;
        }
        if (stack.hasCraftingRemainingItem() && stack.getCount() > 1) {
            return 0;
        }
        int consumeCount = Math.min(stack.getCount(), (remainingBurnTime + burnTime - 1) / burnTime);
        return consumeCount * burnTime;
    }

    private static ItemStack consumedFuelResult(ItemStack stack, int consumedBurnTime) {
        int burnTime = stack.getBurnTime(null);
        if (burnTime <= 0 || consumedBurnTime <= 0) {
            return stack.copy();
        }
        int consumedCount = Math.min(stack.getCount(), (consumedBurnTime + burnTime - 1) / burnTime);
        if (stack.hasCraftingRemainingItem() && consumedCount == 1 && stack.getCount() == 1) {
            return stack.getCraftingRemainingItem();
        }
        ItemStack copy = stack.copy();
        copy.shrink(consumedCount);
        return copy.isEmpty() ? ItemStack.EMPTY : copy;
    }

    private static int insertItem(MachineControllerBlockEntity controller, List<ItemBusBlockEntity> buses, MmceItemRequirement requirement, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            ItemStack chunk = requirement.createStack(controller, remaining);
            if (chunk.isEmpty()) {
                break;
            }
            int inserted = insertStack(buses, chunk);
            if (inserted <= 0) {
                break;
            }
            remaining -= inserted;
        }
        return amount - remaining;
    }

    private static int insertStack(List<ItemBusBlockEntity> buses, ItemStack stack) {
        int remaining = stack.getCount();
        for (ItemBusBlockEntity bus : buses) {
            for (int slot = 0; slot < bus.getContainerSize() && remaining > 0; slot++) {
                ItemStack existing = bus.getItem(slot);
                int max = Math.min(bus.getMaxStackSize(stack), stack.getMaxStackSize());
                if (existing.isEmpty()) {
                    int inserted = Math.min(remaining, max);
                    bus.setItem(slot, stack.copyWithCount(inserted));
                    remaining -= inserted;
                } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                    int inserted = Math.min(remaining, max - existing.getCount());
                    if (inserted > 0) {
                        ItemStack merged = existing.copy();
                        merged.grow(inserted);
                        bus.setItem(slot, merged);
                        remaining -= inserted;
                    }
                }
            }
            if (remaining <= 0) {
                break;
            }
        }
        return stack.getCount() - remaining;
    }

    private static int consumeIngredientArrayInput(MachineControllerBlockEntity controller, List<ItemBusBlockEntity> buses, MmceIngredientArrayRequirement requirement, int parallelism, MmceRecipeModifiers modifiers) {
        ItemInventory trial = ItemInventory.copyOf(buses);
        int consumedParallelism = trial.consumeAnyParallel(controller, requirement, parallelism, modifiers);
        if (consumedParallelism <= 0) {
            return 0;
        }
        return consumeIngredientArrayInputPartial(controller, buses, requirement, consumedParallelism, modifiers);
    }

    private static int consumeIngredientArrayInputPartial(MachineControllerBlockEntity controller, List<ItemBusBlockEntity> buses, MmceIngredientArrayRequirement requirement, int parallelism, MmceRecipeModifiers modifiers) {
        int consumedParallelism = 0;
        for (MmceItemRequirement candidate : requirement.candidates()) {
            int remainingParallelism = parallelism - consumedParallelism;
            if (remainingParallelism <= 0) {
                break;
            }
            int amountPerParallel = amountFor(requirement, candidate.amount(), 1, modifiers);
            if (amountPerParallel <= 0) {
                continue;
            }
            int consumed = consumeItem(controller, buses, candidate, amountPerParallel * remainingParallelism);
            consumedParallelism += consumed / amountPerParallel;
        }
        return consumedParallelism;
    }

    private static int insertIngredientArrayOutput(MachineControllerBlockEntity controller, List<ItemBusBlockEntity> buses, MmceIngredientArrayRequirement requirement, RandomSource random, int parallelism, MmceRecipeModifiers modifiers) {
        Optional<MmceItemRequirement> selected = requirement.selectCandidate(random);
        if (selected.isPresent()) {
            int selectedAmount = randomAmountFor(selected.get(), random, parallelism, modifiers);
            if (ItemInventory.copyOf(buses).insert(controller, selected.get(), selectedAmount)) {
                return insertItem(controller, buses, selected.get(), selectedAmount);
            }
        }

        for (MmceItemRequirement candidate : requirement.candidates()) {
            int amount = randomAmountFor(candidate, random, parallelism, modifiers);
            if (ItemInventory.copyOf(buses).insert(controller, candidate, amount)) {
                return insertItem(controller, buses, candidate, amount);
            }
        }
        return 0;
    }

    private static int drainFluid(List<FluidHatchBlockEntity> hatches, MmceFluidRequirement requirement, int amount) {
        int remaining = amount;
        for (FluidHatchBlockEntity hatch : hatches) {
            FluidStack stored = hatch.getStoredFluid();
            if (requirement.matches(stored)) {
                int drained = Math.min(remaining, stored.getAmount());
                int newAmount = stored.getAmount() - drained;
                hatch.setStoredFluid(newAmount <= 0 ? FluidStack.EMPTY : stored.copyWithAmount(newAmount));
                remaining -= drained;
            }
            if (remaining <= 0) {
                break;
            }
        }
        return amount - remaining;
    }

    private static int fillFluid(List<FluidHatchBlockEntity> hatches, MmceFluidRequirement requirement, int amount) {
        int remaining = amount;
        for (FluidHatchBlockEntity hatch : hatches) {
            FluidStack stored = hatch.getStoredFluid();
            if (!stored.isEmpty() && !requirement.matches(stored)) {
                continue;
            }

            int filled = Math.min(remaining, hatch.getCapacity() - stored.getAmount());
            if (filled <= 0) {
                continue;
            }

            hatch.setStoredFluid(stored.isEmpty()
                    ? requirement.createStack(filled)
                    : stored.copyWithAmount(stored.getAmount() + filled));
            remaining -= filled;
            if (remaining <= 0) {
                break;
            }
        }
        return amount - remaining;
    }

    private static int drainChemical(List<FluidHatchBlockEntity> hatches, MmceChemicalRequirement requirement, int amount) {
        int remaining = amount;
        for (FluidHatchBlockEntity hatch : hatches) {
            if (hatch.hasStoredChemical() && requirement.matches(hatch.getStoredChemicalId(), hatch.getStoredChemicalNbt())) {
                int storedAmount = hatch.getStoredChemicalAmount();
                int drained = Math.min(remaining, storedAmount);
                int newAmount = storedAmount - drained;
                if (newAmount <= 0) {
                    hatch.clearStoredChemical();
                } else {
                    hatch.setStoredChemical(hatch.getStoredChemicalId(), newAmount, hatch.getStoredChemicalNbt());
                }
                remaining -= drained;
            }
            if (remaining <= 0) {
                break;
            }
        }
        return amount - remaining;
    }

    private static int fillChemical(List<FluidHatchBlockEntity> hatches, MmceChemicalRequirement requirement, int amount) {
        int remaining = amount;
        for (FluidHatchBlockEntity hatch : hatches) {
            if (hatch.hasStoredChemical() && !requirement.matches(hatch.getStoredChemicalId(), hatch.getStoredChemicalNbt())) {
                continue;
            }

            int storedAmount = hatch.getStoredChemicalAmount();
            int filled = Math.min(remaining, hatch.getCapacity() - storedAmount);
            if (filled <= 0) {
                continue;
            }

            hatch.setStoredChemical(requirement.chemicalId(), storedAmount + filled,
                    hatch.hasStoredChemical() ? hatch.getStoredChemicalNbt() : requirement.createNbt());
            remaining -= filled;
            if (remaining <= 0) {
                break;
            }
        }
        return amount - remaining;
    }

    private static final class ItemInventory {
        private final List<List<ItemStack>> buses;

        private ItemInventory(List<List<ItemStack>> buses) {
            this.buses = buses;
        }

        static ItemInventory copyOf(List<ItemBusBlockEntity> busEntities) {
            List<List<ItemStack>> copied = new ArrayList<>();
            for (ItemBusBlockEntity bus : busEntities) {
                List<ItemStack> items = new ArrayList<>(bus.getContainerSize());
                for (int slot = 0; slot < bus.getContainerSize(); slot++) {
                    items.add(bus.getItem(slot).copy());
                }
                copied.add(items);
            }
            return new ItemInventory(copied);
        }

        boolean consume(MachineControllerBlockEntity controller, MmceItemRequirement requirement, int amount) {
            if (requirement.durabilityCost() > 0) {
                return damage(controller, requirement, amount);
            }

            int remaining = amount;
            for (List<ItemStack> bus : buses) {
                for (int slot = 0; slot < bus.size() && remaining > 0; slot++) {
                    ItemStack stack = bus.get(slot);
                    if (requirement.matches(controller, stack)) {
                        remaining -= consumeStack(bus, slot, stack, requirement, remaining);
                    }
                }
                if (remaining <= 0) {
                    break;
                }
            }
            return remaining <= 0;
        }

        private boolean damage(MachineControllerBlockEntity controller, MmceItemRequirement requirement, int amount) {
            int remaining = amount;
            for (List<ItemStack> bus : buses) {
                for (int slot = 0; slot < bus.size() && remaining > 0; slot++) {
                    ItemStack stack = bus.get(slot);
                    if (!requirement.matches(controller, stack) || !stack.isDamageableItem()) {
                        continue;
                    }

                    ItemStack damaged = stack.copy();
                    damaged.setDamageValue(damaged.getDamageValue() + requirement.durabilityCost());
                    if (damaged.getDamageValue() >= damaged.getMaxDamage()) {
                        damaged.shrink(1);
                    }
                    bus.set(slot, damaged.isEmpty() ? ItemStack.EMPTY : damaged);
                    remaining--;
                }
                if (remaining <= 0) {
                    break;
                }
            }
            return remaining <= 0;
        }

        boolean consumeAny(MachineControllerBlockEntity controller, MmceIngredientArrayRequirement requirement, int parallelism, MmceRecipeModifiers modifiers) {
            ItemInventory trial = copy();
            int consumedParallelism = trial.consumeAnyParallel(controller, requirement, parallelism, modifiers);
            if (consumedParallelism >= Math.max(1, parallelism)) {
                replaceWith(trial);
                return true;
            }
            return false;
        }

        int consumeAnyParallel(MachineControllerBlockEntity controller, MmceIngredientArrayRequirement requirement, int parallelism, MmceRecipeModifiers modifiers) {
            int targetParallelism = requirement.parallelizeUnaffected() ? 1 : Math.max(1, parallelism);
            int consumedParallelism = 0;
            for (MmceItemRequirement candidate : requirement.candidates()) {
                int remainingParallelism = targetParallelism - consumedParallelism;
                if (remainingParallelism <= 0) {
                    break;
                }
                int amountPerParallel = amountFor(requirement, candidate.amount(), 1, modifiers);
                if (amountPerParallel <= 0) {
                    continue;
                }
                int consumed = consumePartial(controller, candidate, amountPerParallel * remainingParallelism);
                consumedParallelism += consumed / amountPerParallel;
            }
            return consumedParallelism;
        }

        private int consumePartial(MachineControllerBlockEntity controller, MmceItemRequirement requirement, int amount) {
            if (requirement.durabilityCost() > 0) {
                return damagePartial(controller, requirement, amount);
            }

            int remaining = amount;
            for (List<ItemStack> bus : buses) {
                for (int slot = 0; slot < bus.size() && remaining > 0; slot++) {
                    ItemStack stack = bus.get(slot);
                    if (requirement.matches(controller, stack)) {
                        remaining -= consumeStack(bus, slot, stack, requirement, remaining);
                    }
                }
                if (remaining <= 0) {
                    break;
                }
            }
            return amount - remaining;
        }

        private int consumeStack(List<ItemStack> bus, int slot, ItemStack stack, MmceItemRequirement requirement, int remaining) {
            if (!requirement.returnCraftingRemainder() || !stack.hasCraftingRemainingItem()) {
                int consumed = Math.min(remaining, stack.getCount());
                stack.shrink(consumed);
                if (stack.isEmpty()) {
                    bus.set(slot, ItemStack.EMPTY);
                }
                return consumed;
            }

            int consumed = 0;
            while (consumed < remaining) {
                ItemStack current = bus.get(slot);
                if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, stack)) {
                    break;
                }
                ItemStack remainder = current.getCraftingRemainingItem();
                current.shrink(1);
                if (current.isEmpty()) {
                    bus.set(slot, ItemStack.EMPTY);
                }
                if (!remainder.isEmpty() && !insertCraftingRemainder(bus, slot, remainder)) {
                    ItemStack restored = stack.copyWithCount(1);
                    if (bus.get(slot).isEmpty()) {
                        bus.set(slot, restored);
                    } else {
                        insertStackIntoBus(bus, restored);
                    }
                    break;
                }
                consumed++;
            }
            return consumed;
        }

        private boolean insertCraftingRemainder(List<ItemStack> bus, int preferredSlot, ItemStack remainder) {
            if (remainder.isEmpty()) {
                return true;
            }
            ItemStack preferred = bus.get(preferredSlot);
            int max = remainder.getMaxStackSize();
            if (preferred.isEmpty()) {
                bus.set(preferredSlot, remainder.copyWithCount(1));
                return true;
            }
            if (ItemStack.isSameItemSameComponents(preferred, remainder) && preferred.getCount() < max) {
                preferred.grow(1);
                return true;
            }
            return insertStackIntoBus(bus, remainder.copyWithCount(1)) == 1;
        }

        private int insertStackIntoBus(List<ItemStack> bus, ItemStack stack) {
            int remaining = stack.getCount();
            for (int slot = 0; slot < bus.size() && remaining > 0; slot++) {
                ItemStack existing = bus.get(slot);
                int max = stack.getMaxStackSize();
                if (existing.isEmpty()) {
                    int inserted = Math.min(remaining, max);
                    bus.set(slot, stack.copyWithCount(inserted));
                    remaining -= inserted;
                } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                    int inserted = Math.min(remaining, max - existing.getCount());
                    if (inserted > 0) {
                        existing.grow(inserted);
                        remaining -= inserted;
                    }
                }
            }
            return stack.getCount() - remaining;
        }

        private int damagePartial(MachineControllerBlockEntity controller, MmceItemRequirement requirement, int amount) {
            int remaining = amount;
            for (List<ItemStack> bus : buses) {
                for (int slot = 0; slot < bus.size() && remaining > 0; slot++) {
                    ItemStack stack = bus.get(slot);
                    if (!requirement.matches(controller, stack) || !stack.isDamageableItem()) {
                        continue;
                    }

                    ItemStack damaged = stack.copy();
                    damaged.setDamageValue(damaged.getDamageValue() + requirement.durabilityCost());
                    if (damaged.getDamageValue() >= damaged.getMaxDamage()) {
                        damaged.shrink(1);
                    }
                    bus.set(slot, damaged.isEmpty() ? ItemStack.EMPTY : damaged);
                    remaining--;
                }
                if (remaining <= 0) {
                    break;
                }
            }
            return amount - remaining;
        }

        boolean consumeFuel(MmceFuelRequirement requirement, int burnTime) {
            int remaining = burnTime;
            for (List<ItemStack> bus : buses) {
                for (int slot = 0; slot < bus.size() && remaining > 0; slot++) {
                    ItemStack stack = bus.get(slot);
                    int consumed = consumeFuelStack(requirement, stack, remaining);
                    if (consumed <= 0) {
                        continue;
                    }
                    remaining -= consumed;
                    bus.set(slot, consumedFuelResult(stack, consumed));
                }
                if (remaining <= 0) {
                    break;
                }
            }
            return remaining <= 0;
        }

        boolean insert(MachineControllerBlockEntity controller, MmceItemRequirement requirement, int amount) {
            int remaining = amount;
            while (remaining > 0) {
                ItemStack chunk = requirement.createStack(controller, remaining);
                if (chunk.isEmpty()) {
                    return false;
                }

                int inserted = insertStack(chunk);
                if (inserted <= 0) {
                    return false;
                }
                remaining -= inserted;
            }
            return true;
        }

        boolean insertAny(MachineControllerBlockEntity controller, MmceIngredientArrayRequirement requirement, int parallelism, MmceRecipeModifiers modifiers) {
            for (MmceItemRequirement candidate : requirement.candidates()) {
                ItemInventory trial = copy();
                if (trial.insert(controller, candidate, maxAmountFor(candidate, parallelism, modifiers))) {
                    replaceWith(trial);
                    return true;
                }
            }
            return false;
        }

        private ItemInventory copy() {
            List<List<ItemStack>> copied = new ArrayList<>(buses.size());
            for (List<ItemStack> bus : buses) {
                List<ItemStack> items = new ArrayList<>(bus.size());
                for (ItemStack stack : bus) {
                    items.add(stack.copy());
                }
                copied.add(items);
            }
            return new ItemInventory(copied);
        }

        private void replaceWith(ItemInventory other) {
            for (int busIndex = 0; busIndex < buses.size(); busIndex++) {
                List<ItemStack> bus = buses.get(busIndex);
                List<ItemStack> otherBus = other.buses.get(busIndex);
                for (int slot = 0; slot < bus.size(); slot++) {
                    bus.set(slot, otherBus.get(slot).copy());
                }
            }
        }

        private int insertStack(ItemStack stack) {
            int remaining = stack.getCount();
            for (List<ItemStack> bus : buses) {
                for (int slot = 0; slot < bus.size() && remaining > 0; slot++) {
                    ItemStack existing = bus.get(slot);
                    int max = stack.getMaxStackSize();
                    if (existing.isEmpty()) {
                        int inserted = Math.min(remaining, max);
                        bus.set(slot, stack.copyWithCount(inserted));
                        remaining -= inserted;
                    } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                        int inserted = Math.min(remaining, max - existing.getCount());
                        if (inserted > 0) {
                            existing.grow(inserted);
                            remaining -= inserted;
                        }
                    }
                }
                if (remaining <= 0) {
                    break;
                }
            }
            return stack.getCount() - remaining;
        }
    }

    private static final class FluidInventory {
        private final List<FluidTank> tanks;

        private FluidInventory(List<FluidTank> tanks) {
            this.tanks = tanks;
        }

        static FluidInventory copyOf(List<FluidHatchBlockEntity> hatches) {
            List<FluidTank> tanks = new ArrayList<>(hatches.size());
            for (FluidHatchBlockEntity hatch : hatches) {
                tanks.add(new FluidTank(hatch.getStoredFluid(), hatch.getCapacity()));
            }
            return new FluidInventory(tanks);
        }

        boolean drain(MmceFluidRequirement requirement, int amount) {
            int remaining = amount;
            for (FluidTank tank : tanks) {
                if (requirement.matches(tank.stack())) {
                    int drained = Math.min(remaining, tank.stack().getAmount());
                    tank.setStack(tank.stack().getAmount() <= drained ? FluidStack.EMPTY : tank.stack().copyWithAmount(tank.stack().getAmount() - drained));
                    remaining -= drained;
                }
                if (remaining <= 0) {
                    break;
                }
            }
            return remaining <= 0;
        }

        boolean fill(MmceFluidRequirement requirement, int amount) {
            int remaining = amount;
            for (FluidTank tank : tanks) {
                FluidStack stored = tank.stack();
                if (!stored.isEmpty() && !requirement.matches(stored)) {
                    continue;
                }

                int filled = Math.min(remaining, tank.capacity() - stored.getAmount());
                if (filled <= 0) {
                    continue;
                }

                tank.setStack(stored.isEmpty()
                        ? requirement.createStack(filled)
                        : stored.copyWithAmount(stored.getAmount() + filled));
                remaining -= filled;
                if (remaining <= 0) {
                    break;
                }
            }
            return remaining <= 0;
        }
    }

    private static final class ChemicalInventory {
        private final List<ChemicalTank> tanks;

        private ChemicalInventory(List<ChemicalTank> tanks) {
            this.tanks = tanks;
        }

        static ChemicalInventory copyOf(List<FluidHatchBlockEntity> hatches) {
            List<ChemicalTank> tanks = new ArrayList<>(hatches.size());
            for (FluidHatchBlockEntity hatch : hatches) {
                tanks.add(new ChemicalTank(
                        hatch.getStoredChemicalId(),
                        hatch.getStoredChemicalAmount(),
                        hatch.getStoredChemicalNbt(),
                        hatch.getCapacity()
                ));
            }
            return new ChemicalInventory(tanks);
        }

        boolean drain(MmceChemicalRequirement requirement, int amount) {
            int remaining = amount;
            for (ChemicalTank tank : tanks) {
                if (tank.matches(requirement)) {
                    int drained = Math.min(remaining, tank.amount());
                    tank.setAmount(tank.amount() - drained);
                    remaining -= drained;
                }
                if (remaining <= 0) {
                    break;
                }
            }
            return remaining <= 0;
        }

        boolean fill(MmceChemicalRequirement requirement, int amount) {
            int remaining = amount;
            for (ChemicalTank tank : tanks) {
                if (!tank.isEmpty() && !tank.matches(requirement)) {
                    continue;
                }

                int filled = Math.min(remaining, tank.capacity() - tank.amount());
                if (filled <= 0) {
                    continue;
                }

                if (tank.isEmpty()) {
                    tank.set(requirement.chemicalId(), filled, requirement.createNbt());
                } else {
                    tank.setAmount(tank.amount() + filled);
                }
                remaining -= filled;
                if (remaining <= 0) {
                    break;
                }
            }
            return remaining <= 0;
        }
    }

    private static final class EnergyInventory {
        private final List<EnergyTank> tanks;

        private EnergyInventory(List<EnergyTank> tanks) {
            this.tanks = tanks;
        }

        static EnergyInventory inputCopyOf(List<EnergyHatchBlockEntity> hatches) {
            return copyOf(hatches);
        }

        static EnergyInventory outputCopyOf(List<EnergyHatchBlockEntity> hatches) {
            return copyOf(hatches);
        }

        static EnergyInventory inputLive(List<EnergyHatchBlockEntity> hatches) {
            return live(hatches);
        }

        static EnergyInventory outputLive(List<EnergyHatchBlockEntity> hatches) {
            return live(hatches);
        }

        private static EnergyInventory copyOf(List<EnergyHatchBlockEntity> hatches) {
            List<EnergyTank> tanks = new ArrayList<>(hatches.size());
            for (EnergyHatchBlockEntity hatch : hatches) {
                tanks.add(EnergyTank.copy(hatch));
            }
            return new EnergyInventory(tanks);
        }

        private static EnergyInventory live(List<EnergyHatchBlockEntity> hatches) {
            List<EnergyTank> tanks = new ArrayList<>(hatches.size());
            for (EnergyHatchBlockEntity hatch : hatches) {
                tanks.add(EnergyTank.live(hatch));
            }
            return new EnergyInventory(tanks);
        }

        boolean extract(long amount) {
            long remaining = amount;
            for (EnergyTank tank : tanks) {
                long extracted = Math.min(remaining, tank.energy());
                tank.setEnergy(tank.energy() - extracted);
                remaining -= extracted;
                if (remaining <= 0L) {
                    break;
                }
            }
            return remaining <= 0L;
        }

        boolean receive(long amount) {
            long remaining = amount;
            for (EnergyTank tank : tanks) {
                long inserted = Math.min(remaining, tank.capacity() - tank.energy());
                tank.setEnergy(tank.energy() + inserted);
                remaining -= inserted;
                if (remaining <= 0L) {
                    break;
                }
            }
            return remaining <= 0L;
        }
    }

    private static final class EnergyTank {
        private final EnergyHatchBlockEntity hatch;
        private long energy;
        private final long capacity;

        private EnergyTank(EnergyHatchBlockEntity hatch, long energy, long capacity) {
            this.hatch = hatch;
            this.energy = energy;
            this.capacity = capacity;
        }

        static EnergyTank copy(EnergyHatchBlockEntity hatch) {
            return new EnergyTank(null, hatch.getEnergy(), hatch.getCapacity());
        }

        static EnergyTank live(EnergyHatchBlockEntity hatch) {
            return new EnergyTank(hatch, hatch.getEnergy(), hatch.getCapacity());
        }

        long energy() {
            return hatch == null ? energy : hatch.getEnergy();
        }

        long capacity() {
            return capacity;
        }

        void setEnergy(long energy) {
            if (hatch == null) {
                this.energy = Math.max(0L, Math.min(energy, capacity));
            } else {
                hatch.setEnergy(energy);
            }
        }
    }

    private static final class FluidTank {
        private FluidStack stack;
        private final int capacity;

        private FluidTank(FluidStack stack, int capacity) {
            this.stack = stack;
            this.capacity = capacity;
        }

        FluidStack stack() {
            return stack;
        }

        int capacity() {
            return capacity;
        }

        void setStack(FluidStack stack) {
            this.stack = stack;
        }
    }

    private static final class ChemicalTank {
        private ResourceLocation chemicalId;
        private int amount;
        private CompoundTag nbt;
        private final int capacity;

        private ChemicalTank(ResourceLocation chemicalId, int amount, CompoundTag nbt, int capacity) {
            this.chemicalId = amount <= 0 ? null : chemicalId;
            this.amount = this.chemicalId == null ? 0 : Math.max(0, Math.min(amount, capacity));
            this.nbt = nbt == null ? new CompoundTag() : nbt.copy();
            this.capacity = capacity;
        }

        boolean isEmpty() {
            return chemicalId == null || amount <= 0;
        }

        boolean matches(MmceChemicalRequirement requirement) {
            return !isEmpty() && requirement.matches(chemicalId, nbt);
        }

        int amount() {
            return amount;
        }

        int capacity() {
            return capacity;
        }

        void set(ResourceLocation chemicalId, int amount, CompoundTag nbt) {
            this.chemicalId = amount <= 0 ? null : chemicalId;
            this.amount = this.chemicalId == null ? 0 : Math.max(0, Math.min(amount, capacity));
            this.nbt = this.chemicalId == null || nbt == null ? new CompoundTag() : nbt.copy();
        }

        void setAmount(int amount) {
            if (chemicalId == null || amount <= 0) {
                set(null, 0, new CompoundTag());
            } else {
                this.amount = Math.min(amount, capacity);
            }
        }
    }

    private MmceRecipeExecutor() {
    }
}
