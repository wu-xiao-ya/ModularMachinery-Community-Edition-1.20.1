package hellfirepvp.modularmachinery.port.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.data.MmceScriptDataRegistry;
import hellfirepvp.modularmachinery.port.event.MmceEventPhase;
import hellfirepvp.modularmachinery.port.event.MmceEventRegistry;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEventHandler;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEventType;
import net.minecraft.resources.ResourceLocation;

public final class MmceRecipeAdapterBuilder {
    private final ResourceLocation sourceId;
    private final JsonObject root = new JsonObject();
    private final JsonArray requirements = new JsonArray();
    private final JsonArray modifiers = new JsonArray();
    private JsonObject lastRequirement;

    public MmceRecipeAdapterBuilder(ResourceLocation sourceId, String machine, String adapter) {
        this.sourceId = sourceId;
        root.addProperty("machine", machine);
        root.addProperty("adapter", adapter);
        root.add("requirements", requirements);
        root.add("modifiers", modifiers);
    }

    public MmceRecipeAdapterBuilder recipeTime(int ticks) {
        root.addProperty("recipeTime", Math.max(1, ticks));
        return this;
    }

    public MmceRecipeAdapterBuilder setRecipeTime(int ticks) {
        return recipeTime(ticks);
    }

    public MmceRecipeAdapterBuilder duration(int ticks) {
        return recipeTime(ticks);
    }

    public MmceRecipeAdapterBuilder setDuration(int ticks) {
        return duration(ticks);
    }

    public MmceRecipeAdapterBuilder priority(int priority) {
        root.addProperty("priority", priority);
        return this;
    }

    public MmceRecipeAdapterBuilder setPriority(int priority) {
        return priority(priority);
    }

    public MmceRecipeAdapterBuilder cancelIfPerTickFails(boolean value) {
        root.addProperty("cancelIfPerTickFails", value);
        return this;
    }

    public MmceRecipeAdapterBuilder setCancelIfPerTickFails(boolean value) {
        return cancelIfPerTickFails(value);
    }

    public MmceRecipeAdapterBuilder parallelized(boolean value) {
        root.addProperty("parallelized", value);
        return this;
    }

    public MmceRecipeAdapterBuilder setParallelized(boolean value) {
        return parallelized(value);
    }

    public MmceRecipeAdapterBuilder maxThreads(int value) {
        root.addProperty("maxThreads", Math.max(-1, value));
        return this;
    }

    public MmceRecipeAdapterBuilder setMaxThreads(int value) {
        return maxThreads(value);
    }

    public MmceRecipeAdapterBuilder threadName(String value) {
        root.addProperty("threadName", value == null ? "" : value.trim());
        return this;
    }

    public MmceRecipeAdapterBuilder setThreadName(String value) {
        return threadName(value);
    }

    public MmceRecipeAdapterBuilder recipeTooltip(String... tooltips) {
        if (tooltips == null) {
            return this;
        }
        JsonArray array = root.has("recipeTooltips") && root.get("recipeTooltips").isJsonArray()
                ? root.getAsJsonArray("recipeTooltips")
                : new JsonArray();
        for (String tooltip : tooltips) {
            if (tooltip != null && !tooltip.isBlank()) {
                array.add(tooltip);
            }
        }
        root.add("recipeTooltips", array);
        return this;
    }

    public MmceRecipeAdapterBuilder addRecipeTooltip(String... tooltips) {
        return recipeTooltip(tooltips);
    }

    public MmceRecipeAdapterBuilder setRecipeTooltip(String... tooltips) {
        return recipeTooltip(tooltips);
    }

    public MmceRecipeAdapterBuilder loadJEI(boolean load) {
        root.addProperty("loadJEI", load);
        return this;
    }

    public MmceRecipeAdapterBuilder loadJei(boolean load) {
        return loadJEI(load);
    }

    public MmceRecipeAdapterBuilder setLoadJEI(boolean load) {
        return loadJEI(load);
    }

    public MmceRecipeAdapterBuilder setLoadJei(boolean load) {
        return loadJEI(load);
    }

    public boolean getLoadJEI() {
        return !root.has("loadJEI") || root.get("loadJEI").getAsBoolean();
    }

    public boolean getLoadJei() {
        return getLoadJEI();
    }

    public MmceRecipeAdapterBuilder addPreCheckHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.CHECK, MmceEventPhase.START, handler);
    }

    public MmceRecipeAdapterBuilder addPostCheckHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.CHECK, MmceEventPhase.END, handler);
    }

    public MmceRecipeAdapterBuilder addCheckHandler(MmceRecipeEventHandler handler) {
        return addPostCheckHandler(handler);
    }

    public MmceRecipeAdapterBuilder addStartHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.START, null, handler);
    }

    public MmceRecipeAdapterBuilder addPreTickHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.TICK, MmceEventPhase.START, handler);
    }

    public MmceRecipeAdapterBuilder addPostTickHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.TICK, MmceEventPhase.END, handler);
    }

    public MmceRecipeAdapterBuilder addTickHandler(MmceRecipeEventHandler handler) {
        return addPostTickHandler(handler);
    }

    public MmceRecipeAdapterBuilder addFailureHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.FAILURE, null, handler);
    }

    public MmceRecipeAdapterBuilder addFinishHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.FINISH, null, handler);
    }

    public MmceRecipeAdapterBuilder addResultChanceHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.RESULT_CHANCE, null, handler);
    }

    public MmceRecipeAdapterBuilder addFactoryStartHandler(MmceRecipeEventHandler handler) {
        return addStartHandler(handler);
    }

    public MmceRecipeAdapterBuilder addFactoryPreTickHandler(MmceRecipeEventHandler handler) {
        return addPreTickHandler(handler);
    }

    public MmceRecipeAdapterBuilder addFactoryPostTickHandler(MmceRecipeEventHandler handler) {
        return addPostTickHandler(handler);
    }

    public MmceRecipeAdapterBuilder addFactoryFailureHandler(MmceRecipeEventHandler handler) {
        return addFailureHandler(handler);
    }

    public MmceRecipeAdapterBuilder addFactoryFinishHandler(MmceRecipeEventHandler handler) {
        return addFinishHandler(handler);
    }

    public MmceRecipeAdapterBuilder modifier(String target, String io, int operation, double multiplier) {
        return modifier(target, io, operation, multiplier, false);
    }

    public MmceRecipeAdapterBuilder modifier(String target, String io, int operation, double multiplier, boolean affectChance) {
        return modifier(new MmceRecipeModifier(target, io, multiplier, operation, affectChance));
    }

    public MmceRecipeAdapterBuilder modifier(String target, String io, double multiplier, int operation) {
        return modifier(target, io, operation, multiplier, false);
    }

    public MmceRecipeAdapterBuilder modifier(String target, String io, double multiplier, int operation, boolean affectChance) {
        return modifier(target, io, operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder modifier(MmceRecipeModifier modifier) {
        if (modifier != null) {
            modifiers.add(modifier.json());
        }
        return this;
    }

    public MmceRecipeAdapterBuilder addModifier(String target, String io, int operation, double multiplier) {
        return modifier(target, io, operation, multiplier);
    }

    public MmceRecipeAdapterBuilder addModifier(String target, String io, int operation, double multiplier, boolean affectChance) {
        return modifier(target, io, operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder addModifier(String target, String io, double multiplier, int operation) {
        return modifier(target, io, multiplier, operation);
    }

    public MmceRecipeAdapterBuilder addModifier(String target, String io, double multiplier, int operation, boolean affectChance) {
        return modifier(target, io, multiplier, operation, affectChance);
    }

    public MmceRecipeAdapterBuilder addModifier(MmceRecipeModifier modifier) {
        return modifier(modifier);
    }

    public MmceRecipeAdapterBuilder itemInputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("modularmachinery:item", "input", operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder itemOutputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("modularmachinery:item", "output", operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder fluidInputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("modularmachinery:fluid", "input", operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder fluidOutputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("modularmachinery:fluid", "output", operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder gasInputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("modularmachinery:gas", "input", operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder gasOutputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("modularmachinery:gas", "output", operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder chemicalInputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("modularmachinery:chemical", "input", operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder chemicalOutputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("modularmachinery:chemical", "output", operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder energyInputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("modularmachinery:energy", "input", operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder energyOutputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("modularmachinery:energy", "output", operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder durationModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("modularmachinery:duration", "input", operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder addItemInputModifier(int operation, double multiplier) {
        return itemInputModifier(operation, multiplier);
    }

    public MmceRecipeAdapterBuilder addItemOutputModifier(int operation, double multiplier) {
        return itemOutputModifier(operation, multiplier);
    }

    public MmceRecipeAdapterBuilder addFluidInputModifier(int operation, double multiplier) {
        return fluidInputModifier(operation, multiplier);
    }

    public MmceRecipeAdapterBuilder addFluidOutputModifier(int operation, double multiplier) {
        return fluidOutputModifier(operation, multiplier);
    }

    public MmceRecipeAdapterBuilder addGasInputModifier(int operation, double multiplier) {
        return gasInputModifier(operation, multiplier);
    }

    public MmceRecipeAdapterBuilder addGasOutputModifier(int operation, double multiplier) {
        return gasOutputModifier(operation, multiplier);
    }

    public MmceRecipeAdapterBuilder addChemicalInputModifier(int operation, double multiplier) {
        return chemicalInputModifier(operation, multiplier);
    }

    public MmceRecipeAdapterBuilder addChemicalOutputModifier(int operation, double multiplier) {
        return chemicalOutputModifier(operation, multiplier);
    }

    public MmceRecipeAdapterBuilder addEnergyInputModifier(int operation, double multiplier) {
        return energyInputModifier(operation, multiplier);
    }

    public MmceRecipeAdapterBuilder addEnergyOutputModifier(int operation, double multiplier) {
        return energyOutputModifier(operation, multiplier);
    }

    public MmceRecipeAdapterBuilder addDurationModifier(int operation, double multiplier) {
        return durationModifier(operation, multiplier);
    }

    public MmceRecipeAdapterBuilder addItemInputModifier(int operation, double multiplier, boolean affectChance) {
        return itemInputModifier(operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder addItemOutputModifier(int operation, double multiplier, boolean affectChance) {
        return itemOutputModifier(operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder addFluidInputModifier(int operation, double multiplier, boolean affectChance) {
        return fluidInputModifier(operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder addFluidOutputModifier(int operation, double multiplier, boolean affectChance) {
        return fluidOutputModifier(operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder addGasInputModifier(int operation, double multiplier, boolean affectChance) {
        return gasInputModifier(operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder addGasOutputModifier(int operation, double multiplier, boolean affectChance) {
        return gasOutputModifier(operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder addChemicalInputModifier(int operation, double multiplier, boolean affectChance) {
        return chemicalInputModifier(operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder addChemicalOutputModifier(int operation, double multiplier, boolean affectChance) {
        return chemicalOutputModifier(operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder addEnergyInputModifier(int operation, double multiplier, boolean affectChance) {
        return energyInputModifier(operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder addEnergyOutputModifier(int operation, double multiplier, boolean affectChance) {
        return energyOutputModifier(operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder addDurationModifier(int operation, double multiplier, boolean affectChance) {
        return durationModifier(operation, multiplier, affectChance);
    }

    public MmceRecipeAdapterBuilder itemInputModifier(int operation, double multiplier) {
        return modifier("modularmachinery:item", "input", operation, multiplier);
    }

    public MmceRecipeAdapterBuilder itemOutputModifier(int operation, double multiplier) {
        return modifier("modularmachinery:item", "output", operation, multiplier);
    }

    public MmceRecipeAdapterBuilder fluidInputModifier(int operation, double multiplier) {
        return modifier("modularmachinery:fluid", "input", operation, multiplier);
    }

    public MmceRecipeAdapterBuilder fluidOutputModifier(int operation, double multiplier) {
        return modifier("modularmachinery:fluid", "output", operation, multiplier);
    }

    public MmceRecipeAdapterBuilder gasInputModifier(int operation, double multiplier) {
        return modifier("modularmachinery:gas", "input", operation, multiplier);
    }

    public MmceRecipeAdapterBuilder gasOutputModifier(int operation, double multiplier) {
        return modifier("modularmachinery:gas", "output", operation, multiplier);
    }

    public MmceRecipeAdapterBuilder chemicalInputModifier(int operation, double multiplier) {
        return modifier("modularmachinery:chemical", "input", operation, multiplier);
    }

    public MmceRecipeAdapterBuilder chemicalOutputModifier(int operation, double multiplier) {
        return modifier("modularmachinery:chemical", "output", operation, multiplier);
    }

    public MmceRecipeAdapterBuilder energyInputModifier(int operation, double multiplier) {
        return modifier("modularmachinery:energy", "input", operation, multiplier);
    }

    public MmceRecipeAdapterBuilder energyOutputModifier(int operation, double multiplier) {
        return modifier("modularmachinery:energy", "output", operation, multiplier);
    }

    public MmceRecipeAdapterBuilder durationModifier(int operation, double multiplier) {
        return modifier("modularmachinery:duration", "input", operation, multiplier);
    }

    public MmceRecipeAdapterBuilder energyInput(long energyPerTick) {
        return energy("input", energyPerTick);
    }

    public MmceRecipeAdapterBuilder addEnergyPerTickInput(long energyPerTick) {
        return energyInput(energyPerTick);
    }

    public MmceRecipeAdapterBuilder energyOutput(long energyPerTick) {
        return energy("output", energyPerTick);
    }

    public MmceRecipeAdapterBuilder addEnergyPerTickOutput(long energyPerTick) {
        return energyOutput(energyPerTick);
    }

    public MmceRecipeAdapterBuilder itemInput(String item, int amount) {
        return item("input", item, amount);
    }

    public MmceRecipeAdapterBuilder itemInput(Object item) {
        return itemObject("input", item, 0);
    }

    public MmceRecipeAdapterBuilder itemInput(Object item, int amount) {
        return itemObject("input", item, amount);
    }

    public MmceRecipeAdapterBuilder addItemInput(String item, int amount) {
        return itemInput(item, amount);
    }

    public MmceRecipeAdapterBuilder addItemInput(Object item) {
        return itemInput(item);
    }

    public MmceRecipeAdapterBuilder addItemInput(Object item, int amount) {
        return itemInput(item, amount);
    }

    public MmceRecipeAdapterBuilder addItemInputs(Object... items) {
        if (items != null) {
            for (Object item : items) {
                itemInput(item);
            }
        }
        return this;
    }

    public MmceRecipeAdapterBuilder addInput(Object item) {
        return itemInput(item);
    }

    public MmceRecipeAdapterBuilder addInput(Object item, int amount) {
        return itemInput(item, amount);
    }

    public MmceRecipeAdapterBuilder addInputs(Object... items) {
        return addItemInputs(items);
    }

    public MmceRecipeAdapterBuilder itemOutput(String item, int amount) {
        return item("output", item, amount);
    }

    public MmceRecipeAdapterBuilder itemOutput(Object item) {
        return itemObject("output", item, 0);
    }

    public MmceRecipeAdapterBuilder itemOutput(Object item, int amount) {
        return itemObject("output", item, amount);
    }

    public MmceRecipeAdapterBuilder addItemOutput(String item, int amount) {
        return itemOutput(item, amount);
    }

    public MmceRecipeAdapterBuilder addItemOutput(Object item) {
        return itemOutput(item);
    }

    public MmceRecipeAdapterBuilder addItemOutput(Object item, int amount) {
        return itemOutput(item, amount);
    }

    public MmceRecipeAdapterBuilder addItemOutputs(Object... items) {
        if (items != null) {
            for (Object item : items) {
                itemOutput(item);
            }
        }
        return this;
    }

    public MmceRecipeAdapterBuilder addOutput(Object item) {
        return itemOutput(item);
    }

    public MmceRecipeAdapterBuilder addOutput(Object item, int amount) {
        return itemOutput(item, amount);
    }

    public MmceRecipeAdapterBuilder addOutputs(Object... items) {
        return addItemOutputs(items);
    }

    public MmceRecipeAdapterBuilder fuelItemInput(int requiredTotalBurnTime) {
        JsonObject requirement = base("modularmachinery:fuel", "input");
        requirement.addProperty("item", "any:fuel");
        requirement.addProperty("time", Math.max(1, requiredTotalBurnTime));
        add(requirement);
        return this;
    }

    public MmceRecipeAdapterBuilder addFuelItemInput(int requiredTotalBurnTime) {
        return fuelItemInput(requiredTotalBurnTime);
    }

    public MmceRecipeAdapterBuilder fluidInput(String fluid, int amount) {
        return fluid("input", fluid, amount);
    }

    public MmceRecipeAdapterBuilder fluidInput(Object fluid) {
        return fluidObject("input", fluid, 0, false);
    }

    public MmceRecipeAdapterBuilder fluidInput(Object fluid, int amount) {
        return fluidObject("input", fluid, amount, false);
    }

    public MmceRecipeAdapterBuilder addFluidInput(String fluid, int amount) {
        return fluidInput(fluid, amount);
    }

    public MmceRecipeAdapterBuilder addFluidInput(Object fluid) {
        return fluidInput(fluid);
    }

    public MmceRecipeAdapterBuilder addFluidInput(Object fluid, int amount) {
        return fluidInput(fluid, amount);
    }

    public MmceRecipeAdapterBuilder addFluidInputs(Object... fluids) {
        if (fluids != null) {
            for (Object fluid : fluids) {
                fluidInput(fluid);
            }
        }
        return this;
    }

    public MmceRecipeAdapterBuilder fluidOutput(String fluid, int amount) {
        return fluid("output", fluid, amount);
    }

    public MmceRecipeAdapterBuilder fluidOutput(Object fluid) {
        return fluidObject("output", fluid, 0, false);
    }

    public MmceRecipeAdapterBuilder fluidOutput(Object fluid, int amount) {
        return fluidObject("output", fluid, amount, false);
    }

    public MmceRecipeAdapterBuilder addFluidOutput(String fluid, int amount) {
        return fluidOutput(fluid, amount);
    }

    public MmceRecipeAdapterBuilder addFluidOutput(Object fluid) {
        return fluidOutput(fluid);
    }

    public MmceRecipeAdapterBuilder addFluidOutput(Object fluid, int amount) {
        return fluidOutput(fluid, amount);
    }

    public MmceRecipeAdapterBuilder addFluidOutputs(Object... fluids) {
        if (fluids != null) {
            for (Object fluid : fluids) {
                fluidOutput(fluid);
            }
        }
        return this;
    }

    public MmceRecipeAdapterBuilder fluidPerTickInput(String fluid, int amount) {
        return fluid("input", fluid, amount, true);
    }

    public MmceRecipeAdapterBuilder fluidPerTickInput(Object fluid) {
        return fluidObject("input", fluid, 0, true);
    }

    public MmceRecipeAdapterBuilder fluidPerTickInput(Object fluid, int amount) {
        return fluidObject("input", fluid, amount, true);
    }

    public MmceRecipeAdapterBuilder addFluidPerTickInput(String fluid, int amount) {
        return fluidPerTickInput(fluid, amount);
    }

    public MmceRecipeAdapterBuilder addFluidPerTickInput(Object fluid) {
        return fluidPerTickInput(fluid);
    }

    public MmceRecipeAdapterBuilder addFluidPerTickInput(Object fluid, int amount) {
        return fluidPerTickInput(fluid, amount);
    }

    public MmceRecipeAdapterBuilder addFluidPerTickInputs(Object... fluids) {
        if (fluids != null) {
            for (Object fluid : fluids) {
                fluidPerTickInput(fluid);
            }
        }
        return this;
    }

    public MmceRecipeAdapterBuilder fluidPerTickOutput(String fluid, int amount) {
        return fluid("output", fluid, amount, true);
    }

    public MmceRecipeAdapterBuilder fluidPerTickOutput(Object fluid) {
        return fluidObject("output", fluid, 0, true);
    }

    public MmceRecipeAdapterBuilder fluidPerTickOutput(Object fluid, int amount) {
        return fluidObject("output", fluid, amount, true);
    }

    public MmceRecipeAdapterBuilder addFluidPerTickOutput(String fluid, int amount) {
        return fluidPerTickOutput(fluid, amount);
    }

    public MmceRecipeAdapterBuilder addFluidPerTickOutput(Object fluid) {
        return fluidPerTickOutput(fluid);
    }

    public MmceRecipeAdapterBuilder addFluidPerTickOutput(Object fluid, int amount) {
        return fluidPerTickOutput(fluid, amount);
    }

    public MmceRecipeAdapterBuilder addFluidPerTickOutputs(Object... fluids) {
        if (fluids != null) {
            for (Object fluid : fluids) {
                fluidPerTickOutput(fluid);
            }
        }
        return this;
    }

    public MmceRecipeAdapterBuilder gasInput(String gas, int amount) {
        return chemical("input", gas, amount, false, true);
    }

    public MmceRecipeAdapterBuilder gasInput(Object gasStack) {
        return chemical("input", gasStack, 0, false, true);
    }

    public MmceRecipeAdapterBuilder addGasInput(String gas, int amount) {
        return gasInput(gas, amount);
    }

    public MmceRecipeAdapterBuilder addGasInput(Object gasStack) {
        return gasInput(gasStack);
    }

    public MmceRecipeAdapterBuilder gasOutput(String gas, int amount) {
        return chemical("output", gas, amount, false, true);
    }

    public MmceRecipeAdapterBuilder gasOutput(Object gasStack) {
        return chemical("output", gasStack, 0, false, true);
    }

    public MmceRecipeAdapterBuilder addGasOutput(String gas, int amount) {
        return gasOutput(gas, amount);
    }

    public MmceRecipeAdapterBuilder addGasOutput(Object gasStack) {
        return gasOutput(gasStack);
    }

    public MmceRecipeAdapterBuilder gasInputs(Object... gasStacks) {
        return chemicals("input", false, true, gasStacks);
    }

    public MmceRecipeAdapterBuilder addGasInputs(Object... gasStacks) {
        return gasInputs(gasStacks);
    }

    public MmceRecipeAdapterBuilder gasOutputs(Object... gasStacks) {
        return chemicals("output", false, true, gasStacks);
    }

    public MmceRecipeAdapterBuilder addGasOutputs(Object... gasStacks) {
        return gasOutputs(gasStacks);
    }

    public MmceRecipeAdapterBuilder gasPerTickInput(String gas, int amount) {
        return chemical("input", gas, amount, true, true);
    }

    public MmceRecipeAdapterBuilder gasPerTickInput(Object gasStack) {
        return chemical("input", gasStack, 0, true, true);
    }

    public MmceRecipeAdapterBuilder addGasPerTickInput(String gas, int amount) {
        return gasPerTickInput(gas, amount);
    }

    public MmceRecipeAdapterBuilder addGasPerTickInput(Object gasStack) {
        return gasPerTickInput(gasStack);
    }

    public MmceRecipeAdapterBuilder gasPerTickOutput(String gas, int amount) {
        return chemical("output", gas, amount, true, true);
    }

    public MmceRecipeAdapterBuilder gasPerTickOutput(Object gasStack) {
        return chemical("output", gasStack, 0, true, true);
    }

    public MmceRecipeAdapterBuilder addGasPerTickOutput(String gas, int amount) {
        return gasPerTickOutput(gas, amount);
    }

    public MmceRecipeAdapterBuilder addGasPerTickOutput(Object gasStack) {
        return gasPerTickOutput(gasStack);
    }

    public MmceRecipeAdapterBuilder gasPerTickInputs(Object... gasStacks) {
        return chemicals("input", true, true, gasStacks);
    }

    public MmceRecipeAdapterBuilder addGasPerTickInputs(Object... gasStacks) {
        return gasPerTickInputs(gasStacks);
    }

    public MmceRecipeAdapterBuilder gasPerTickOutputs(Object... gasStacks) {
        return chemicals("output", true, true, gasStacks);
    }

    public MmceRecipeAdapterBuilder addGasPerTickOutputs(Object... gasStacks) {
        return gasPerTickOutputs(gasStacks);
    }

    public MmceRecipeAdapterBuilder chemicalInput(String chemical, int amount) {
        return chemical("input", chemical, amount, false, false);
    }

    public MmceRecipeAdapterBuilder chemicalInput(Object chemicalStack) {
        return chemical("input", chemicalStack, 0, false, false);
    }

    public MmceRecipeAdapterBuilder addChemicalInput(String chemical, int amount) {
        return chemicalInput(chemical, amount);
    }

    public MmceRecipeAdapterBuilder addChemicalInput(Object chemicalStack) {
        return chemicalInput(chemicalStack);
    }

    public MmceRecipeAdapterBuilder chemicalOutput(String chemical, int amount) {
        return chemical("output", chemical, amount, false, false);
    }

    public MmceRecipeAdapterBuilder chemicalOutput(Object chemicalStack) {
        return chemical("output", chemicalStack, 0, false, false);
    }

    public MmceRecipeAdapterBuilder addChemicalOutput(String chemical, int amount) {
        return chemicalOutput(chemical, amount);
    }

    public MmceRecipeAdapterBuilder addChemicalOutput(Object chemicalStack) {
        return chemicalOutput(chemicalStack);
    }

    public MmceRecipeAdapterBuilder chemicalInputs(Object... chemicalStacks) {
        return chemicals("input", false, false, chemicalStacks);
    }

    public MmceRecipeAdapterBuilder addChemicalInputs(Object... chemicalStacks) {
        return chemicalInputs(chemicalStacks);
    }

    public MmceRecipeAdapterBuilder chemicalOutputs(Object... chemicalStacks) {
        return chemicals("output", false, false, chemicalStacks);
    }

    public MmceRecipeAdapterBuilder addChemicalOutputs(Object... chemicalStacks) {
        return chemicalOutputs(chemicalStacks);
    }

    public MmceRecipeAdapterBuilder chemicalPerTickInput(String chemical, int amount) {
        return chemical("input", chemical, amount, true, false);
    }

    public MmceRecipeAdapterBuilder chemicalPerTickInput(Object chemicalStack) {
        return chemical("input", chemicalStack, 0, true, false);
    }

    public MmceRecipeAdapterBuilder addChemicalPerTickInput(String chemical, int amount) {
        return chemicalPerTickInput(chemical, amount);
    }

    public MmceRecipeAdapterBuilder addChemicalPerTickInput(Object chemicalStack) {
        return chemicalPerTickInput(chemicalStack);
    }

    public MmceRecipeAdapterBuilder chemicalPerTickOutput(String chemical, int amount) {
        return chemical("output", chemical, amount, true, false);
    }

    public MmceRecipeAdapterBuilder chemicalPerTickOutput(Object chemicalStack) {
        return chemical("output", chemicalStack, 0, true, false);
    }

    public MmceRecipeAdapterBuilder addChemicalPerTickOutput(String chemical, int amount) {
        return chemicalPerTickOutput(chemical, amount);
    }

    public MmceRecipeAdapterBuilder addChemicalPerTickOutput(Object chemicalStack) {
        return chemicalPerTickOutput(chemicalStack);
    }

    public MmceRecipeAdapterBuilder chemicalPerTickInputs(Object... chemicalStacks) {
        return chemicals("input", true, false, chemicalStacks);
    }

    public MmceRecipeAdapterBuilder addChemicalPerTickInputs(Object... chemicalStacks) {
        return chemicalPerTickInputs(chemicalStacks);
    }

    public MmceRecipeAdapterBuilder chemicalPerTickOutputs(Object... chemicalStacks) {
        return chemicals("output", true, false, chemicalStacks);
    }

    public MmceRecipeAdapterBuilder addChemicalPerTickOutputs(Object... chemicalStacks) {
        return chemicalPerTickOutputs(chemicalStacks);
    }

    public MmceRecipeAdapterBuilder ingredientArrayInput(String[] items, int amount) {
        return itemArray("modularmachinery:ingredient_array_input", "input", items, amount);
    }

    public MmceRecipeAdapterBuilder ingredientArrayInput(Object items) {
        return itemArray("modularmachinery:ingredient_array_input", "input", toIngredientArray(items, 1));
    }

    public MmceRecipeAdapterBuilder ingredientArrayInput(Object items, int amount) {
        return itemArray("modularmachinery:ingredient_array_input", "input", toIngredientArray(items, amount));
    }

    public MmceRecipeAdapterBuilder ingredientArrayInput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return itemArray("modularmachinery:ingredient_array_input", "input", ingredientArrayPrimer);
    }

    public MmceRecipeAdapterBuilder addIngredientArrayInput(String[] items, int amount) {
        return ingredientArrayInput(items, amount);
    }

    public MmceRecipeAdapterBuilder addIngredientArrayInput(Object items) {
        return ingredientArrayInput(items);
    }

    public MmceRecipeAdapterBuilder addIngredientArrayInput(Object items, int amount) {
        return ingredientArrayInput(items, amount);
    }

    public MmceRecipeAdapterBuilder addIngredientArrayInput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return ingredientArrayInput(ingredientArrayPrimer);
    }

    public MmceRecipeAdapterBuilder randomItemOutput(String[] items, int amount) {
        return itemArray("modularmachinery:ingredient_array_input", "output", items, amount);
    }

    public MmceRecipeAdapterBuilder randomItemOutput(Object items) {
        return itemArray("modularmachinery:ingredient_array_input", "output", toIngredientArray(items, 1));
    }

    public MmceRecipeAdapterBuilder randomItemOutput(Object items, int amount) {
        return itemArray("modularmachinery:ingredient_array_input", "output", toIngredientArray(items, amount));
    }

    public MmceRecipeAdapterBuilder randomItemOutput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return itemArray("modularmachinery:ingredient_array_input", "output", ingredientArrayPrimer);
    }

    public MmceRecipeAdapterBuilder addRandomItemOutput(String[] items, int amount) {
        return randomItemOutput(items, amount);
    }

    public MmceRecipeAdapterBuilder addRandomItemOutput(Object items) {
        return randomItemOutput(items);
    }

    public MmceRecipeAdapterBuilder addRandomItemOutput(Object items, int amount) {
        return randomItemOutput(items, amount);
    }

    public MmceRecipeAdapterBuilder addRandomItemOutput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return randomItemOutput(ingredientArrayPrimer);
    }

    public MmceRecipeAdapterBuilder catalystInput(String item, int amount) {
        return itemArray("modularmachinery:catalyst", "input", new String[] { item }, amount);
    }

    public MmceRecipeAdapterBuilder catalystInput(Object item) {
        return itemArray("modularmachinery:catalyst", "input", toIngredientArray(item, 1));
    }

    public MmceRecipeAdapterBuilder catalystInput(Object item, int amount) {
        return itemArray("modularmachinery:catalyst", "input", toIngredientArray(item, amount));
    }

    public MmceRecipeAdapterBuilder catalystInput(String[] items, int amount) {
        return itemArray("modularmachinery:catalyst", "input", items, amount);
    }

    public MmceRecipeAdapterBuilder catalystInput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return itemArray("modularmachinery:catalyst", "input", ingredientArrayPrimer);
    }

    public MmceRecipeAdapterBuilder addCatalystInput(String item, int amount) {
        return catalystInput(item, amount);
    }

    public MmceRecipeAdapterBuilder addCatalystInput(Object item) {
        return catalystInput(item);
    }

    public MmceRecipeAdapterBuilder addCatalystInput(Object item, int amount) {
        return catalystInput(item, amount);
    }

    public MmceRecipeAdapterBuilder addCatalystInput(String[] items, int amount) {
        return catalystInput(items, amount);
    }

    public MmceRecipeAdapterBuilder addCatalystInput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return catalystInput(ingredientArrayPrimer);
    }

    public MmceRecipeAdapterBuilder catalystInput(String item, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        itemArray("modularmachinery:catalyst", "input", new String[] { item }, amount);
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    public MmceRecipeAdapterBuilder catalystInput(Object item, String[] tooltips, MmceRecipeModifier[] modifiers) {
        itemArray("modularmachinery:catalyst", "input", toIngredientArray(item, 1));
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    public MmceRecipeAdapterBuilder catalystInput(Object item, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        itemArray("modularmachinery:catalyst", "input", toIngredientArray(item, amount));
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    public MmceRecipeAdapterBuilder catalystInput(String[] items, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        itemArray("modularmachinery:catalyst", "input", items, amount);
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    public MmceRecipeAdapterBuilder catalystInput(MmceIngredientArrayPrimer ingredientArrayPrimer, String[] tooltips, MmceRecipeModifier[] modifiers) {
        itemArray("modularmachinery:catalyst", "input", ingredientArrayPrimer);
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    public MmceRecipeAdapterBuilder addCatalystInput(String item, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        return catalystInput(item, amount, tooltips, modifiers);
    }

    public MmceRecipeAdapterBuilder addCatalystInput(Object item, String[] tooltips, MmceRecipeModifier[] modifiers) {
        return catalystInput(item, tooltips, modifiers);
    }

    public MmceRecipeAdapterBuilder addCatalystInput(Object item, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        return catalystInput(item, amount, tooltips, modifiers);
    }

    public MmceRecipeAdapterBuilder addCatalystInput(String[] items, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        return catalystInput(items, amount, tooltips, modifiers);
    }

    public MmceRecipeAdapterBuilder addCatalystInput(MmceIngredientArrayPrimer ingredientArrayPrimer, String[] tooltips, MmceRecipeModifier[] modifiers) {
        return catalystInput(ingredientArrayPrimer, tooltips, modifiers);
    }

    public MmceRecipeAdapterBuilder smartInterfaceDataInput(String type, float value) {
        return smartInterfaceDataInput(type, value, value);
    }

    public MmceRecipeAdapterBuilder addSmartInterfaceDataInput(String type, float value) {
        return smartInterfaceDataInput(type, value);
    }

    public MmceRecipeAdapterBuilder smartInterfaceDataInput(String type, float minValue, float maxValue) {
        JsonObject requirement = base("modularmachinery:interface_number_input", "input");
        requirement.addProperty("interfaceType", type == null ? "" : type.trim());
        requirement.addProperty("minValue", minValue);
        requirement.addProperty("maxValue", maxValue);
        add(requirement);
        return this;
    }

    public MmceRecipeAdapterBuilder addSmartInterfaceDataInput(String type, float minValue, float maxValue) {
        return smartInterfaceDataInput(type, minValue, maxValue);
    }

    public MmceRecipeAdapterBuilder chance(float chance) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("chance", Math.max(0.0F, Math.min(1.0F, chance)));
        }
        return this;
    }

    public MmceRecipeAdapterBuilder setChance(float chance) {
        return chance(chance);
    }

    public MmceRecipeAdapterBuilder setChance(double chance) {
        return chance((float) chance);
    }

    public MmceRecipeAdapterBuilder selectorTag(String tag) {
        if (lastRequirement != null && tag != null && !tag.isBlank()) {
            lastRequirement.addProperty("selector-tag", tag);
        }
        return this;
    }

    public MmceRecipeAdapterBuilder setTag(String tag) {
        return selectorTag(tag);
    }

    public MmceRecipeAdapterBuilder setSelectorTag(String tag) {
        return selectorTag(tag);
    }

    public MmceRecipeAdapterBuilder selector_tag(String tag) {
        return selectorTag(tag);
    }

    public MmceRecipeAdapterBuilder set_selector_tag(String tag) {
        return selectorTag(tag);
    }

    public MmceRecipeAdapterBuilder consumeDurability(int durability) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("consumeDurability", Math.max(0, durability));
        }
        return this;
    }

    public MmceRecipeAdapterBuilder minMaxAmount(int min, int max) {
        if (lastRequirement != null) {
            int safeMin = Math.max(1, min);
            int safeMax = Math.max(safeMin, max);
            lastRequirement.addProperty("minAmount", safeMin);
            lastRequirement.addProperty("maxAmount", safeMax);
        }
        return this;
    }

    public MmceRecipeAdapterBuilder setMinMaxAmount(int min, int max) {
        return minMaxAmount(min, max);
    }

    public MmceRecipeAdapterBuilder minMaxOutputAmount(int min, int max) {
        return minMaxAmount(min, max);
    }

    public MmceRecipeAdapterBuilder setMinMaxOutputAmount(int min, int max) {
        return minMaxAmount(min, max);
    }

    public MmceRecipeAdapterBuilder nbt(String json) {
        if (lastRequirement != null && json != null && !json.isBlank()) {
            lastRequirement.add("nbt", parseObject(json));
        }
        return this;
    }

    public MmceRecipeAdapterBuilder setNbt(String json) {
        return nbt(json);
    }

    public MmceRecipeAdapterBuilder setNBT(String json) {
        return nbt(json);
    }

    public MmceRecipeAdapterBuilder displayNbt(String json) {
        if (lastRequirement != null && json != null && !json.isBlank()) {
            lastRequirement.add("nbt-display", parseObject(json));
        }
        return this;
    }

    public MmceRecipeAdapterBuilder setDisplayNbt(String json) {
        return displayNbt(json);
    }

    public MmceRecipeAdapterBuilder setDisplayNBT(String json) {
        return displayNbt(json);
    }

    public MmceRecipeAdapterBuilder previewNbt(String json) {
        return displayNbt(json);
    }

    public MmceRecipeAdapterBuilder setPreviewNbt(String json) {
        return previewNbt(json);
    }

    public MmceRecipeAdapterBuilder setPreviewNBT(String json) {
        return previewNbt(json);
    }

    public MmceRecipeAdapterBuilder setPreViewNBT(String json) {
        return previewNbt(json);
    }

    public MmceRecipeAdapterBuilder triggerTime(int tickTime) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("triggerTime", Math.max(0, tickTime));
        }
        return this;
    }

    public MmceRecipeAdapterBuilder setTriggerTime(int tickTime) {
        return triggerTime(tickTime);
    }

    public MmceRecipeAdapterBuilder triggerRepeatable(boolean repeatable) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("triggerRepeatable", repeatable);
        }
        return this;
    }

    public MmceRecipeAdapterBuilder setTriggerRepeatable(boolean repeatable) {
        return triggerRepeatable(repeatable);
    }

    public MmceRecipeAdapterBuilder ignoreOutputCheck(boolean ignore) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("ignoreOutputCheck", ignore);
        }
        return this;
    }

    public MmceRecipeAdapterBuilder setIgnoreOutputCheck(boolean ignore) {
        return ignoreOutputCheck(ignore);
    }

    public MmceRecipeAdapterBuilder parallelizeUnaffected(boolean unaffected) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("parallelizeUnaffected", unaffected);
        }
        return this;
    }

    public MmceRecipeAdapterBuilder setParallelizeUnaffected(boolean unaffected) {
        return parallelizeUnaffected(unaffected);
    }

    public JsonObject json() {
        return root.deepCopy();
    }

    public void build() {
        MmceScriptDataRegistry.registerAdapter(sourceId, root);
    }

    private void applyCatalystMetadata(String[] tooltips, MmceRecipeModifier[] modifiers) {
        if (lastRequirement == null) {
            return;
        }
        if (tooltips != null && tooltips.length > 0) {
            JsonArray tooltipArray = new JsonArray();
            for (String tooltip : tooltips) {
                if (tooltip != null && !tooltip.isBlank()) {
                    tooltipArray.add(tooltip);
                }
            }
            if (!tooltipArray.isEmpty()) {
                lastRequirement.add("tooltips", tooltipArray);
            }
        }
        if (modifiers != null && modifiers.length > 0) {
            JsonArray modifierArray = new JsonArray();
            for (MmceRecipeModifier modifier : modifiers) {
                if (modifier != null) {
                    modifierArray.add(modifier.json());
                }
            }
            if (!modifierArray.isEmpty()) {
                lastRequirement.add("modifiers", modifierArray);
            }
        }
    }

    private MmceRecipeAdapterBuilder recipeHandler(MmceRecipeEventType type, MmceEventPhase phase, MmceRecipeEventHandler handler) {
        if (handler != null) {
            MmceEventRegistry.registerRecipeAdapter(sourceId, type, event -> {
                if (phase == null || event.getPhase() == phase) {
                    handler.handle(event);
                }
            });
        }
        return this;
    }

    private MmceRecipeAdapterBuilder energy(String ioType, long energyPerTick) {
        JsonObject requirement = base("modularmachinery:energy", ioType);
        requirement.addProperty("energyPerTick", Math.max(0L, energyPerTick));
        add(requirement);
        return this;
    }

    private MmceRecipeAdapterBuilder item(String ioType, String item, int amount) {
        JsonObject requirement = base("modularmachinery:item", ioType);
        requirement.addProperty("item", item);
        requirement.addProperty("amount", Math.max(1, amount));
        add(requirement);
        return this;
    }

    private MmceRecipeAdapterBuilder itemObject(String ioType, Object item, int amount) {
        var entries = MmceScriptValues.itemEntries(item, amount);
        if (entries.isEmpty()) {
            return this;
        }
        if (entries.size() == 1) {
            MmceScriptValues.ItemEntry entry = entries.getFirst();
            JsonObject requirement = base("modularmachinery:item", ioType);
            requirement.addProperty("item", entry.id());
            requirement.addProperty("amount", entry.amount());
            entry.nbt().ifPresent(nbt -> requirement.add("nbt", nbt.deepCopy()));
            entry.displayNbt().ifPresent(nbt -> requirement.add("nbt-display", nbt.deepCopy()));
            add(requirement);
            return this;
        }
        return itemArray(
                "input".equals(ioType) ? "modularmachinery:ingredient_array_input" : "modularmachinery:ingredient_array_output",
                ioType,
                toIngredientArray(entries)
        );
    }

    private MmceRecipeAdapterBuilder fluid(String ioType, String fluid, int amount) {
        return fluid(ioType, fluid, amount, false);
    }

    private MmceRecipeAdapterBuilder fluid(String ioType, String fluid, int amount, boolean perTick) {
        JsonObject requirement = base(perTick ? "modularmachinery:fluid_pertick" : "modularmachinery:fluid", ioType);
        requirement.addProperty("fluid", fluid);
        requirement.addProperty("amount", Math.max(0, amount));
        add(requirement);
        return this;
    }

    private MmceRecipeAdapterBuilder fluidObject(String ioType, Object fluid, int amount, boolean perTick) {
        return MmceScriptValues.fluidEntry(fluid, amount).map(entry -> {
            JsonObject requirement = base(perTick ? "modularmachinery:fluid_pertick" : "modularmachinery:fluid", ioType);
            requirement.addProperty("fluid", entry.id());
            requirement.addProperty("amount", entry.amount());
            entry.nbt().ifPresent(nbt -> requirement.add("nbt", nbt.deepCopy()));
            add(requirement);
            return this;
        }).orElse(this);
    }

    private MmceRecipeAdapterBuilder chemical(String ioType, String chemical, int amount, boolean perTick, boolean legacyGasType) {
        JsonObject requirement = base(chemicalType(perTick, legacyGasType), ioType);
        requirement.addProperty(legacyGasType ? "gas" : "chemical", chemical);
        requirement.addProperty("amount", Math.max(0, amount));
        add(requirement);
        return this;
    }

    private MmceRecipeAdapterBuilder chemical(String ioType, Object chemical, int amount, boolean perTick, boolean legacyGasType) {
        return MmceScriptValues.chemicalEntry(chemical, amount).map(entry -> {
            JsonObject requirement = base(chemicalType(perTick, legacyGasType), ioType);
            requirement.addProperty(legacyGasType ? "gas" : "chemical", entry.id());
            requirement.addProperty("amount", entry.amount());
            entry.nbt().ifPresent(nbt -> requirement.add("nbt", nbt.deepCopy()));
            add(requirement);
            return this;
        }).orElse(this);
    }

    private MmceRecipeAdapterBuilder chemicals(String ioType, boolean perTick, boolean legacyGasType, Object... chemicals) {
        if (chemicals != null) {
            for (Object chemical : chemicals) {
                chemical(ioType, chemical, 0, perTick, legacyGasType);
            }
        }
        return this;
    }

    private static String chemicalType(boolean perTick, boolean legacyGasType) {
        if (legacyGasType) {
            return perTick ? "modularmachinery:gas_pertick" : "modularmachinery:gas";
        }
        return perTick ? "modularmachinery:chemical_pertick" : "modularmachinery:chemical";
    }

    private MmceRecipeAdapterBuilder itemArray(String type, String ioType, String[] items, int amount) {
        JsonObject requirement = base(type, ioType);
        JsonArray itemArray = new JsonArray();
        if (items != null) {
            for (String item : items) {
                if (item == null || item.isBlank()) {
                    continue;
                }
                JsonObject entry = new JsonObject();
                entry.addProperty("item", item);
                entry.addProperty("amount", Math.max(1, amount));
                itemArray.add(entry);
            }
        }
        requirement.add("items", itemArray);
        add(requirement);
        return this;
    }

    private MmceRecipeAdapterBuilder itemArray(String type, String ioType, MmceIngredientArrayPrimer ingredientArrayPrimer) {
        JsonObject requirement = base(type, ioType);
        requirement.add("items", ingredientArrayPrimer == null ? new JsonArray() : ingredientArrayPrimer.items());
        add(requirement);
        return this;
    }

    private static MmceIngredientArrayPrimer toIngredientArray(Object items, int amount) {
        return toIngredientArray(MmceScriptValues.itemEntries(items, amount));
    }

    private static MmceIngredientArrayPrimer toIngredientArray(java.util.List<MmceScriptValues.ItemEntry> entries) {
        MmceIngredientArrayPrimer primer = new MmceIngredientArrayPrimer();
        for (MmceScriptValues.ItemEntry entry : entries) {
            primer.addIngredient(entry.id(), entry.amount());
            entry.nbt().ifPresent(nbt -> primer.nbt(nbt.toString()));
            entry.displayNbt().ifPresent(nbt -> primer.displayNbt(nbt.toString()));
        }
        return primer;
    }

    private static JsonObject parseObject(String json) {
        return com.google.gson.JsonParser.parseString(json).getAsJsonObject();
    }

    private JsonObject base(String type, String ioType) {
        JsonObject requirement = new JsonObject();
        requirement.addProperty("type", type);
        requirement.addProperty("io-type", ioType);
        return requirement;
    }

    private void add(JsonObject requirement) {
        requirements.add(requirement);
        lastRequirement = requirement;
    }
}
