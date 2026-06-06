package hellfirepvp.modularmachinery.port.integration.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.data.MmceScriptDataRegistry;
import hellfirepvp.modularmachinery.port.event.MmceEventPhase;
import hellfirepvp.modularmachinery.port.event.MmceEventRegistry;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEventHandler;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEventType;
import hellfirepvp.modularmachinery.port.integration.MmceIngredientArrayPrimer;
import hellfirepvp.modularmachinery.port.integration.MmceItemCallbackRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceItemChecker;
import hellfirepvp.modularmachinery.port.integration.MmceItemModifier;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.integration.MmceScriptValues;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public final class MmceKubeJSRecipeBuilder {
    private final String registryName;
    private final JsonObject root = new JsonObject();
    private final JsonArray requirements = new JsonArray();
    private JsonObject lastRequirement;

    MmceKubeJSRecipeBuilder(String registryName, String machine, int recipeTime) {
        this.registryName = registryName;
        root.addProperty("registryName", registryName);
        root.addProperty("machine", machine);
        root.addProperty("recipeTime", Math.max(1, recipeTime));
        root.add("requirements", requirements);
    }

    public MmceKubeJSRecipeBuilder priority(int priority) {
        root.addProperty("priority", priority);
        return this;
    }

    public MmceKubeJSRecipeBuilder setPriority(int priority) {
        return priority(priority);
    }

    public MmceKubeJSRecipeBuilder recipeTime(int ticks) {
        root.addProperty("recipeTime", Math.max(1, ticks));
        return this;
    }

    public MmceKubeJSRecipeBuilder setRecipeTime(int ticks) {
        return recipeTime(ticks);
    }

    public MmceKubeJSRecipeBuilder duration(int ticks) {
        return recipeTime(ticks);
    }

    public MmceKubeJSRecipeBuilder setDuration(int ticks) {
        return recipeTime(ticks);
    }

    public MmceKubeJSRecipeBuilder cancelIfPerTickFails(boolean value) {
        root.addProperty("cancelIfPerTickFails", value);
        return this;
    }

    public MmceKubeJSRecipeBuilder setCancelIfPerTickFails(boolean value) {
        return cancelIfPerTickFails(value);
    }

    public MmceKubeJSRecipeBuilder parallelized(boolean value) {
        root.addProperty("parallelized", value);
        return this;
    }

    public MmceKubeJSRecipeBuilder setParallelized(boolean value) {
        return parallelized(value);
    }

    public MmceKubeJSRecipeBuilder parallelize(boolean value) {
        return parallelized(value);
    }

    public MmceKubeJSRecipeBuilder setParallelize(boolean value) {
        return parallelized(value);
    }

    public MmceKubeJSRecipeBuilder maxThreads(int value) {
        root.addProperty("maxThreads", Math.max(-1, value));
        return this;
    }

    public MmceKubeJSRecipeBuilder setMaxThreads(int value) {
        return maxThreads(value);
    }

    public MmceKubeJSRecipeBuilder threadName(String value) {
        root.addProperty("threadName", value == null ? "" : value.trim());
        return this;
    }

    public MmceKubeJSRecipeBuilder setThreadName(String value) {
        return threadName(value);
    }

    public MmceKubeJSRecipeBuilder recipeTooltip(String... tooltips) {
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

    public MmceKubeJSRecipeBuilder addRecipeTooltip(String... tooltips) {
        return recipeTooltip(tooltips);
    }

    public MmceKubeJSRecipeBuilder setRecipeTooltip(String... tooltips) {
        return recipeTooltip(tooltips);
    }

    public MmceKubeJSRecipeBuilder loadJEI(boolean load) {
        root.addProperty("loadJEI", load);
        return this;
    }

    public MmceKubeJSRecipeBuilder loadJei(boolean load) {
        return loadJEI(load);
    }

    public MmceKubeJSRecipeBuilder setLoadJEI(boolean load) {
        return loadJEI(load);
    }

    public MmceKubeJSRecipeBuilder setLoadJei(boolean load) {
        return loadJEI(load);
    }

    public boolean getLoadJEI() {
        return !root.has("loadJEI") || root.get("loadJEI").getAsBoolean();
    }

    public boolean getLoadJei() {
        return getLoadJEI();
    }

    public ResourceLocation getRecipeRegistryName() {
        return mmceId(stringValue("registryName", registryName));
    }

    public ResourceLocation getAssociatedMachineName() {
        return mmceId(stringValue("machine", ""));
    }

    public int getTotalProcessingTickTime() {
        return intValue("recipeTime", 1);
    }

    public int getPriority() {
        return intValue("priority", 0);
    }

    public boolean voidPerTickFailure() {
        return booleanValue("cancelIfPerTickFails", false);
    }

    public boolean isParallelized() {
        return booleanValue("parallelized", false);
    }

    public int getMaxThreads() {
        return intValue("maxThreads", -1);
    }

    public String getThreadName() {
        return stringValue("threadName", "");
    }

    public List<String> getTooltipList() {
        if (!root.has("recipeTooltips") || !root.get("recipeTooltips").isJsonArray()) {
            return List.of();
        }
        JsonArray array = root.getAsJsonArray("recipeTooltips");
        List<String> tooltips = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                tooltips.add(element.getAsString());
            }
        }
        return List.copyOf(tooltips);
    }

    public MmceKubeJSRecipeBuilder energyInput(long energyPerTick) {
        return energy("input", energyPerTick);
    }

    public MmceKubeJSRecipeBuilder addEnergyPerTickInput(long energyPerTick) {
        return energyInput(energyPerTick);
    }

    public MmceKubeJSRecipeBuilder energyOutput(long energyPerTick) {
        return energy("output", energyPerTick);
    }

    public MmceKubeJSRecipeBuilder addEnergyPerTickOutput(long energyPerTick) {
        return energyOutput(energyPerTick);
    }

    public MmceKubeJSRecipeBuilder itemInput(String item, int amount) {
        return item("input", item, amount);
    }

    public MmceKubeJSRecipeBuilder itemInput(Object item) {
        return itemObject("input", item, 0);
    }

    public MmceKubeJSRecipeBuilder itemInput(Object item, int amount) {
        return itemObject("input", item, amount);
    }

    public MmceKubeJSRecipeBuilder addItemInput(String item, int amount) {
        return itemInput(item, amount);
    }

    public MmceKubeJSRecipeBuilder addItemInput(Object item) {
        return itemInput(item);
    }

    public MmceKubeJSRecipeBuilder addItemInput(Object item, int amount) {
        return itemInput(item, amount);
    }

    public MmceKubeJSRecipeBuilder addItemInputs(Object... items) {
        if (items != null) {
            for (Object item : items) {
                itemInput(item);
            }
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder addInput(Object item) {
        return itemInput(item);
    }

    public MmceKubeJSRecipeBuilder addInput(Object item, int amount) {
        return itemInput(item, amount);
    }

    public MmceKubeJSRecipeBuilder addInputs(Object... items) {
        return addItemInputs(items);
    }

    public MmceKubeJSRecipeBuilder itemOutput(String item, int amount) {
        return item("output", item, amount);
    }

    public MmceKubeJSRecipeBuilder itemOutput(Object item) {
        return itemObject("output", item, 0);
    }

    public MmceKubeJSRecipeBuilder itemOutput(Object item, int amount) {
        return itemObject("output", item, amount);
    }

    public MmceKubeJSRecipeBuilder addItemOutput(String item, int amount) {
        return itemOutput(item, amount);
    }

    public MmceKubeJSRecipeBuilder addItemOutput(Object item) {
        return itemOutput(item);
    }

    public MmceKubeJSRecipeBuilder addItemOutput(Object item, int amount) {
        return itemOutput(item, amount);
    }

    public MmceKubeJSRecipeBuilder addItemOutputs(Object... items) {
        if (items != null) {
            for (Object item : items) {
                itemOutput(item);
            }
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder addOutput(Object item) {
        return itemOutput(item);
    }

    public MmceKubeJSRecipeBuilder addOutput(Object item, int amount) {
        return itemOutput(item, amount);
    }

    public MmceKubeJSRecipeBuilder addOutputs(Object... items) {
        return addItemOutputs(items);
    }

    public MmceKubeJSRecipeBuilder fuelItemInput(int requiredTotalBurnTime) {
        JsonObject requirement = base("modularmachinery:fuel", "input");
        requirement.addProperty("item", "any:fuel");
        requirement.addProperty("time", Math.max(1, requiredTotalBurnTime));
        add(requirement);
        return this;
    }

    public MmceKubeJSRecipeBuilder addFuelItemInput(int requiredTotalBurnTime) {
        return fuelItemInput(requiredTotalBurnTime);
    }

    public MmceKubeJSRecipeBuilder fluidInput(String fluid, int amount) {
        return fluid("input", fluid, amount);
    }

    public MmceKubeJSRecipeBuilder fluidInput(Object fluid) {
        return fluidObject("input", fluid, 0, false);
    }

    public MmceKubeJSRecipeBuilder fluidInput(Object fluid, int amount) {
        return fluidObject("input", fluid, amount, false);
    }

    public MmceKubeJSRecipeBuilder addFluidInput(String fluid, int amount) {
        return fluidInput(fluid, amount);
    }

    public MmceKubeJSRecipeBuilder addFluidInput(Object fluid) {
        return fluidInput(fluid);
    }

    public MmceKubeJSRecipeBuilder addFluidInput(Object fluid, int amount) {
        return fluidInput(fluid, amount);
    }

    public MmceKubeJSRecipeBuilder addFluidInputs(Object... fluids) {
        if (fluids != null) {
            for (Object fluid : fluids) {
                fluidInput(fluid);
            }
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder fluidOutput(String fluid, int amount) {
        return fluid("output", fluid, amount);
    }

    public MmceKubeJSRecipeBuilder fluidOutput(Object fluid) {
        return fluidObject("output", fluid, 0, false);
    }

    public MmceKubeJSRecipeBuilder fluidOutput(Object fluid, int amount) {
        return fluidObject("output", fluid, amount, false);
    }

    public MmceKubeJSRecipeBuilder addFluidOutput(String fluid, int amount) {
        return fluidOutput(fluid, amount);
    }

    public MmceKubeJSRecipeBuilder addFluidOutput(Object fluid) {
        return fluidOutput(fluid);
    }

    public MmceKubeJSRecipeBuilder addFluidOutput(Object fluid, int amount) {
        return fluidOutput(fluid, amount);
    }

    public MmceKubeJSRecipeBuilder addFluidOutputs(Object... fluids) {
        if (fluids != null) {
            for (Object fluid : fluids) {
                fluidOutput(fluid);
            }
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder fluidPerTickInput(String fluid, int amount) {
        return fluid("input", fluid, amount, true);
    }

    public MmceKubeJSRecipeBuilder fluidPerTickInput(Object fluid) {
        return fluidObject("input", fluid, 0, true);
    }

    public MmceKubeJSRecipeBuilder fluidPerTickInput(Object fluid, int amount) {
        return fluidObject("input", fluid, amount, true);
    }

    public MmceKubeJSRecipeBuilder addFluidPerTickInput(String fluid, int amount) {
        return fluidPerTickInput(fluid, amount);
    }

    public MmceKubeJSRecipeBuilder addFluidPerTickInput(Object fluid) {
        return fluidPerTickInput(fluid);
    }

    public MmceKubeJSRecipeBuilder addFluidPerTickInput(Object fluid, int amount) {
        return fluidPerTickInput(fluid, amount);
    }

    public MmceKubeJSRecipeBuilder addFluidPerTickInputs(Object... fluids) {
        if (fluids != null) {
            for (Object fluid : fluids) {
                fluidPerTickInput(fluid);
            }
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder fluidPerTickOutput(String fluid, int amount) {
        return fluid("output", fluid, amount, true);
    }

    public MmceKubeJSRecipeBuilder fluidPerTickOutput(Object fluid) {
        return fluidObject("output", fluid, 0, true);
    }

    public MmceKubeJSRecipeBuilder fluidPerTickOutput(Object fluid, int amount) {
        return fluidObject("output", fluid, amount, true);
    }

    public MmceKubeJSRecipeBuilder addFluidPerTickOutput(String fluid, int amount) {
        return fluidPerTickOutput(fluid, amount);
    }

    public MmceKubeJSRecipeBuilder addFluidPerTickOutput(Object fluid) {
        return fluidPerTickOutput(fluid);
    }

    public MmceKubeJSRecipeBuilder addFluidPerTickOutput(Object fluid, int amount) {
        return fluidPerTickOutput(fluid, amount);
    }

    public MmceKubeJSRecipeBuilder addFluidPerTickOutputs(Object... fluids) {
        if (fluids != null) {
            for (Object fluid : fluids) {
                fluidPerTickOutput(fluid);
            }
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder gasInput(String gas, int amount) {
        return chemical("input", gas, amount, false, true);
    }

    public MmceKubeJSRecipeBuilder gasInput(Object gasStack) {
        return chemical("input", gasStack, 0, false, true);
    }

    public MmceKubeJSRecipeBuilder addGasInput(String gas, int amount) {
        return gasInput(gas, amount);
    }

    public MmceKubeJSRecipeBuilder addGasInput(Object gasStack) {
        return gasInput(gasStack);
    }

    public MmceKubeJSRecipeBuilder gasOutput(String gas, int amount) {
        return chemical("output", gas, amount, false, true);
    }

    public MmceKubeJSRecipeBuilder gasOutput(Object gasStack) {
        return chemical("output", gasStack, 0, false, true);
    }

    public MmceKubeJSRecipeBuilder addGasOutput(String gas, int amount) {
        return gasOutput(gas, amount);
    }

    public MmceKubeJSRecipeBuilder addGasOutput(Object gasStack) {
        return gasOutput(gasStack);
    }

    public MmceKubeJSRecipeBuilder gasInputs(Object... gasStacks) {
        return chemicals("input", false, true, gasStacks);
    }

    public MmceKubeJSRecipeBuilder addGasInputs(Object... gasStacks) {
        return gasInputs(gasStacks);
    }

    public MmceKubeJSRecipeBuilder gasOutputs(Object... gasStacks) {
        return chemicals("output", false, true, gasStacks);
    }

    public MmceKubeJSRecipeBuilder addGasOutputs(Object... gasStacks) {
        return gasOutputs(gasStacks);
    }

    public MmceKubeJSRecipeBuilder gasPerTickInput(String gas, int amount) {
        return chemical("input", gas, amount, true, true);
    }

    public MmceKubeJSRecipeBuilder gasPerTickInput(Object gasStack) {
        return chemical("input", gasStack, 0, true, true);
    }

    public MmceKubeJSRecipeBuilder addGasPerTickInput(String gas, int amount) {
        return gasPerTickInput(gas, amount);
    }

    public MmceKubeJSRecipeBuilder addGasPerTickInput(Object gasStack) {
        return gasPerTickInput(gasStack);
    }

    public MmceKubeJSRecipeBuilder gasPerTickOutput(String gas, int amount) {
        return chemical("output", gas, amount, true, true);
    }

    public MmceKubeJSRecipeBuilder gasPerTickOutput(Object gasStack) {
        return chemical("output", gasStack, 0, true, true);
    }

    public MmceKubeJSRecipeBuilder addGasPerTickOutput(String gas, int amount) {
        return gasPerTickOutput(gas, amount);
    }

    public MmceKubeJSRecipeBuilder addGasPerTickOutput(Object gasStack) {
        return gasPerTickOutput(gasStack);
    }

    public MmceKubeJSRecipeBuilder gasPerTickInputs(Object... gasStacks) {
        return chemicals("input", true, true, gasStacks);
    }

    public MmceKubeJSRecipeBuilder addGasPerTickInputs(Object... gasStacks) {
        return gasPerTickInputs(gasStacks);
    }

    public MmceKubeJSRecipeBuilder gasPerTickOutputs(Object... gasStacks) {
        return chemicals("output", true, true, gasStacks);
    }

    public MmceKubeJSRecipeBuilder addGasPerTickOutputs(Object... gasStacks) {
        return gasPerTickOutputs(gasStacks);
    }

    public MmceKubeJSRecipeBuilder chemicalInput(String chemical, int amount) {
        return chemical("input", chemical, amount, false, false);
    }

    public MmceKubeJSRecipeBuilder chemicalInput(Object chemicalStack) {
        return chemical("input", chemicalStack, 0, false, false);
    }

    public MmceKubeJSRecipeBuilder addChemicalInput(String chemical, int amount) {
        return chemicalInput(chemical, amount);
    }

    public MmceKubeJSRecipeBuilder addChemicalInput(Object chemicalStack) {
        return chemicalInput(chemicalStack);
    }

    public MmceKubeJSRecipeBuilder chemicalOutput(String chemical, int amount) {
        return chemical("output", chemical, amount, false, false);
    }

    public MmceKubeJSRecipeBuilder chemicalOutput(Object chemicalStack) {
        return chemical("output", chemicalStack, 0, false, false);
    }

    public MmceKubeJSRecipeBuilder addChemicalOutput(String chemical, int amount) {
        return chemicalOutput(chemical, amount);
    }

    public MmceKubeJSRecipeBuilder addChemicalOutput(Object chemicalStack) {
        return chemicalOutput(chemicalStack);
    }

    public MmceKubeJSRecipeBuilder chemicalInputs(Object... chemicalStacks) {
        return chemicals("input", false, false, chemicalStacks);
    }

    public MmceKubeJSRecipeBuilder addChemicalInputs(Object... chemicalStacks) {
        return chemicalInputs(chemicalStacks);
    }

    public MmceKubeJSRecipeBuilder chemicalOutputs(Object... chemicalStacks) {
        return chemicals("output", false, false, chemicalStacks);
    }

    public MmceKubeJSRecipeBuilder addChemicalOutputs(Object... chemicalStacks) {
        return chemicalOutputs(chemicalStacks);
    }

    public MmceKubeJSRecipeBuilder chemicalPerTickInput(String chemical, int amount) {
        return chemical("input", chemical, amount, true, false);
    }

    public MmceKubeJSRecipeBuilder chemicalPerTickInput(Object chemicalStack) {
        return chemical("input", chemicalStack, 0, true, false);
    }

    public MmceKubeJSRecipeBuilder addChemicalPerTickInput(String chemical, int amount) {
        return chemicalPerTickInput(chemical, amount);
    }

    public MmceKubeJSRecipeBuilder addChemicalPerTickInput(Object chemicalStack) {
        return chemicalPerTickInput(chemicalStack);
    }

    public MmceKubeJSRecipeBuilder chemicalPerTickOutput(String chemical, int amount) {
        return chemical("output", chemical, amount, true, false);
    }

    public MmceKubeJSRecipeBuilder chemicalPerTickOutput(Object chemicalStack) {
        return chemical("output", chemicalStack, 0, true, false);
    }

    public MmceKubeJSRecipeBuilder addChemicalPerTickOutput(String chemical, int amount) {
        return chemicalPerTickOutput(chemical, amount);
    }

    public MmceKubeJSRecipeBuilder addChemicalPerTickOutput(Object chemicalStack) {
        return chemicalPerTickOutput(chemicalStack);
    }

    public MmceKubeJSRecipeBuilder chemicalPerTickInputs(Object... chemicalStacks) {
        return chemicals("input", true, false, chemicalStacks);
    }

    public MmceKubeJSRecipeBuilder addChemicalPerTickInputs(Object... chemicalStacks) {
        return chemicalPerTickInputs(chemicalStacks);
    }

    public MmceKubeJSRecipeBuilder chemicalPerTickOutputs(Object... chemicalStacks) {
        return chemicals("output", true, false, chemicalStacks);
    }

    public MmceKubeJSRecipeBuilder addChemicalPerTickOutputs(Object... chemicalStacks) {
        return chemicalPerTickOutputs(chemicalStacks);
    }

    public MmceKubeJSRecipeBuilder ingredientArrayInput(String[] items, int amount) {
        return itemArray("modularmachinery:ingredient_array_input", "input", items, amount);
    }

    public MmceKubeJSRecipeBuilder ingredientArrayInput(Object items) {
        return itemArray("modularmachinery:ingredient_array_input", "input", toIngredientArray(items, 1));
    }

    public MmceKubeJSRecipeBuilder ingredientArrayInput(Object items, int amount) {
        return itemArray("modularmachinery:ingredient_array_input", "input", toIngredientArray(items, amount));
    }

    public MmceKubeJSRecipeBuilder ingredientArrayInput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return itemArray("modularmachinery:ingredient_array_input", "input", ingredientArrayPrimer);
    }

    public MmceKubeJSRecipeBuilder addIngredientArrayInput(String[] items, int amount) {
        return ingredientArrayInput(items, amount);
    }

    public MmceKubeJSRecipeBuilder addIngredientArrayInput(Object items) {
        return ingredientArrayInput(items);
    }

    public MmceKubeJSRecipeBuilder addIngredientArrayInput(Object items, int amount) {
        return ingredientArrayInput(items, amount);
    }

    public MmceKubeJSRecipeBuilder addIngredientArrayInput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return ingredientArrayInput(ingredientArrayPrimer);
    }

    public MmceKubeJSRecipeBuilder randomItemOutput(String[] items, int amount) {
        return itemArray("modularmachinery:ingredient_array_input", "output", items, amount);
    }

    public MmceKubeJSRecipeBuilder randomItemOutput(Object items) {
        return itemArray("modularmachinery:ingredient_array_input", "output", toIngredientArray(items, 1));
    }

    public MmceKubeJSRecipeBuilder randomItemOutput(Object items, int amount) {
        return itemArray("modularmachinery:ingredient_array_input", "output", toIngredientArray(items, amount));
    }

    public MmceKubeJSRecipeBuilder randomItemOutput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return itemArray("modularmachinery:ingredient_array_input", "output", ingredientArrayPrimer);
    }

    public MmceKubeJSRecipeBuilder addRandomItemOutput(String[] items, int amount) {
        return randomItemOutput(items, amount);
    }

    public MmceKubeJSRecipeBuilder addRandomItemOutput(Object items) {
        return randomItemOutput(items);
    }

    public MmceKubeJSRecipeBuilder addRandomItemOutput(Object items, int amount) {
        return randomItemOutput(items, amount);
    }

    public MmceKubeJSRecipeBuilder addRandomItemOutput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return randomItemOutput(ingredientArrayPrimer);
    }

    public MmceKubeJSRecipeBuilder catalystInput(String item, int amount) {
        return itemArray("modularmachinery:catalyst", "input", new String[] { item }, amount);
    }

    public MmceKubeJSRecipeBuilder catalystInput(Object item) {
        return itemArray("modularmachinery:catalyst", "input", toIngredientArray(item, 1));
    }

    public MmceKubeJSRecipeBuilder catalystInput(Object item, int amount) {
        return itemArray("modularmachinery:catalyst", "input", toIngredientArray(item, amount));
    }

    public MmceKubeJSRecipeBuilder catalystInput(String[] items, int amount) {
        return itemArray("modularmachinery:catalyst", "input", items, amount);
    }

    public MmceKubeJSRecipeBuilder catalystInput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return itemArray("modularmachinery:catalyst", "input", ingredientArrayPrimer);
    }

    public MmceKubeJSRecipeBuilder addCatalystInput(String item, int amount) {
        return catalystInput(item, amount);
    }

    public MmceKubeJSRecipeBuilder addCatalystInput(Object item) {
        return catalystInput(item);
    }

    public MmceKubeJSRecipeBuilder addCatalystInput(Object item, int amount) {
        return catalystInput(item, amount);
    }

    public MmceKubeJSRecipeBuilder addCatalystInput(String[] items, int amount) {
        return catalystInput(items, amount);
    }

    public MmceKubeJSRecipeBuilder addCatalystInput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return catalystInput(ingredientArrayPrimer);
    }

    public MmceKubeJSRecipeBuilder catalystInput(String item, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        itemArray("modularmachinery:catalyst", "input", new String[] { item }, amount);
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    public MmceKubeJSRecipeBuilder catalystInput(Object item, String[] tooltips, MmceRecipeModifier[] modifiers) {
        itemArray("modularmachinery:catalyst", "input", toIngredientArray(item, 1));
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    public MmceKubeJSRecipeBuilder catalystInput(Object item, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        itemArray("modularmachinery:catalyst", "input", toIngredientArray(item, amount));
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    public MmceKubeJSRecipeBuilder catalystInput(String[] items, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        itemArray("modularmachinery:catalyst", "input", items, amount);
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    public MmceKubeJSRecipeBuilder catalystInput(MmceIngredientArrayPrimer ingredientArrayPrimer, String[] tooltips, MmceRecipeModifier[] modifiers) {
        itemArray("modularmachinery:catalyst", "input", ingredientArrayPrimer);
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    public MmceKubeJSRecipeBuilder addCatalystInput(String item, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        return catalystInput(item, amount, tooltips, modifiers);
    }

    public MmceKubeJSRecipeBuilder addCatalystInput(Object item, String[] tooltips, MmceRecipeModifier[] modifiers) {
        return catalystInput(item, tooltips, modifiers);
    }

    public MmceKubeJSRecipeBuilder addCatalystInput(Object item, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        return catalystInput(item, amount, tooltips, modifiers);
    }

    public MmceKubeJSRecipeBuilder addCatalystInput(String[] items, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        return catalystInput(items, amount, tooltips, modifiers);
    }

    public MmceKubeJSRecipeBuilder addCatalystInput(MmceIngredientArrayPrimer ingredientArrayPrimer, String[] tooltips, MmceRecipeModifier[] modifiers) {
        return catalystInput(ingredientArrayPrimer, tooltips, modifiers);
    }

    public MmceKubeJSRecipeBuilder smartInterfaceDataInput(String type, float value) {
        return smartInterfaceDataInput(type, value, value);
    }

    public MmceKubeJSRecipeBuilder smartInterfaceDataInput(String type, float minValue, float maxValue) {
        JsonObject requirement = base("modularmachinery:interface_number_input", "input");
        requirement.addProperty("interfaceType", type);
        requirement.addProperty("minValue", minValue);
        requirement.addProperty("maxValue", maxValue);
        add(requirement);
        return this;
    }

    public MmceKubeJSRecipeBuilder addSmartInterfaceDataInput(String type, float value) {
        return smartInterfaceDataInput(type, value);
    }

    public MmceKubeJSRecipeBuilder addSmartInterfaceDataInput(String type, float minValue, float maxValue) {
        return smartInterfaceDataInput(type, minValue, maxValue);
    }

    public MmceKubeJSRecipeBuilder chance(float chance) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("chance", Math.max(0.0F, Math.min(1.0F, chance)));
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder setChance(float chance) {
        return chance(chance);
    }

    public MmceKubeJSRecipeBuilder selectorTag(String tag) {
        if (lastRequirement != null && tag != null && !tag.isBlank()) {
            lastRequirement.addProperty("selector-tag", tag);
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder setTag(String tag) {
        return selectorTag(tag);
    }

    public MmceKubeJSRecipeBuilder setSelectorTag(String tag) {
        return selectorTag(tag);
    }

    public MmceKubeJSRecipeBuilder consumeDurability(int durability) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("consumeDurability", Math.max(0, durability));
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder minMaxAmount(int min, int max) {
        if (lastRequirement != null) {
            int safeMin = Math.max(1, min);
            int safeMax = Math.max(safeMin, max);
            lastRequirement.addProperty("minAmount", safeMin);
            lastRequirement.addProperty("maxAmount", safeMax);
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder setMinMaxAmount(int min, int max) {
        return minMaxAmount(min, max);
    }

    public MmceKubeJSRecipeBuilder nbt(String json) {
        if (lastRequirement != null && json != null && !json.isBlank()) {
            lastRequirement.add("nbt", parseObject(json));
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder setNbt(String json) {
        return nbt(json);
    }

    public MmceKubeJSRecipeBuilder setNBT(String json) {
        return nbt(json);
    }

    public MmceKubeJSRecipeBuilder itemChecker(MmceItemChecker checker) {
        return itemChecker("", checker);
    }

    public MmceKubeJSRecipeBuilder itemChecker(String checkerId, MmceItemChecker checker) {
        if (lastRequirement != null && checker != null) {
            lastRequirement.addProperty("checker-id", MmceItemCallbackRegistry.registerChecker(checkerId, checker));
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder setNBTChecker(MmceItemChecker checker) {
        return itemChecker(checker);
    }

    public MmceKubeJSRecipeBuilder setNBTChecker(String checkerId, MmceItemChecker checker) {
        return itemChecker(checkerId, checker);
    }

    public MmceKubeJSRecipeBuilder setNbtChecker(MmceItemChecker checker) {
        return itemChecker(checker);
    }

    public MmceKubeJSRecipeBuilder setNbtChecker(String checkerId, MmceItemChecker checker) {
        return itemChecker(checkerId, checker);
    }

    public MmceKubeJSRecipeBuilder setItemChecker(MmceItemChecker checker) {
        return itemChecker(checker);
    }

    public MmceKubeJSRecipeBuilder setItemChecker(String checkerId, MmceItemChecker checker) {
        return itemChecker(checkerId, checker);
    }

    public MmceKubeJSRecipeBuilder itemModifier(MmceItemModifier modifier) {
        return itemModifier("", modifier);
    }

    public MmceKubeJSRecipeBuilder itemModifier(String modifierId, MmceItemModifier modifier) {
        if (lastRequirement != null && modifier != null) {
            lastRequirement.addProperty("item-modifier-id", MmceItemCallbackRegistry.registerModifier(modifierId, modifier));
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder addItemModifier(MmceItemModifier modifier) {
        return itemModifier(modifier);
    }

    public MmceKubeJSRecipeBuilder addItemModifier(String modifierId, MmceItemModifier modifier) {
        return itemModifier(modifierId, modifier);
    }

    public MmceKubeJSRecipeBuilder setItemModifier(MmceItemModifier modifier) {
        return itemModifier(modifier);
    }

    public MmceKubeJSRecipeBuilder setItemModifier(String modifierId, MmceItemModifier modifier) {
        return itemModifier(modifierId, modifier);
    }

    public MmceKubeJSRecipeBuilder displayNbt(String json) {
        if (lastRequirement != null && json != null && !json.isBlank()) {
            lastRequirement.add("nbt-display", parseObject(json));
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder setDisplayNbt(String json) {
        return displayNbt(json);
    }

    public MmceKubeJSRecipeBuilder setDisplayNBT(String json) {
        return displayNbt(json);
    }

    public MmceKubeJSRecipeBuilder previewNbt(String json) {
        return displayNbt(json);
    }

    public MmceKubeJSRecipeBuilder setPreviewNbt(String json) {
        return previewNbt(json);
    }

    public MmceKubeJSRecipeBuilder setPreviewNBT(String json) {
        return previewNbt(json);
    }

    public MmceKubeJSRecipeBuilder setPreViewNBT(String json) {
        return previewNbt(json);
    }

    public MmceKubeJSRecipeBuilder triggerTime(int tickTime) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("triggerTime", Math.max(0, tickTime));
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder triggerRepeatable(boolean repeatable) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("triggerRepeatable", repeatable);
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder ignoreOutputCheck(boolean ignore) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("ignoreOutputCheck", ignore);
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder parallelizeUnaffected(boolean unaffected) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("parallelizeUnaffected", unaffected);
        }
        return this;
    }

    public MmceKubeJSRecipeBuilder startCommand(String command) {
        return command("startCommands", command, -1);
    }

    public MmceKubeJSRecipeBuilder startCommand(String command, int interval) {
        return command("startCommands", command, interval);
    }

    public MmceKubeJSRecipeBuilder addStartCommand(String command) {
        return startCommand(command);
    }

    public MmceKubeJSRecipeBuilder addStartCommand(String command, int interval) {
        return startCommand(command, interval);
    }

    public MmceKubeJSRecipeBuilder processingCommand(String command) {
        return command("processingCommands", command, -1);
    }

    public MmceKubeJSRecipeBuilder processingCommand(String command, int interval) {
        return command("processingCommands", command, interval);
    }

    public MmceKubeJSRecipeBuilder addProcessingCommand(String command) {
        return processingCommand(command);
    }

    public MmceKubeJSRecipeBuilder addProcessingCommand(String command, int interval) {
        return processingCommand(command, interval);
    }

    public MmceKubeJSRecipeBuilder finishCommand(String command) {
        return command("finishCommands", command, -1);
    }

    public MmceKubeJSRecipeBuilder finishCommand(String command, int interval) {
        return command("finishCommands", command, interval);
    }

    public MmceKubeJSRecipeBuilder addFinishCommand(String command) {
        return finishCommand(command);
    }

    public MmceKubeJSRecipeBuilder addFinishCommand(String command, int interval) {
        return finishCommand(command, interval);
    }

    public MmceKubeJSRecipeBuilder onPreCheck(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.CHECK, MmceEventPhase.START, handler);
    }

    public MmceKubeJSRecipeBuilder onPostCheck(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.CHECK, MmceEventPhase.END, handler);
    }

    public MmceKubeJSRecipeBuilder onCheck(MmceRecipeEventHandler handler) {
        return onPostCheck(handler);
    }

    public MmceKubeJSRecipeBuilder onStart(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.START, null, handler);
    }

    public MmceKubeJSRecipeBuilder onPreTick(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.TICK, MmceEventPhase.START, handler);
    }

    public MmceKubeJSRecipeBuilder onPostTick(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.TICK, MmceEventPhase.END, handler);
    }

    public MmceKubeJSRecipeBuilder onTick(MmceRecipeEventHandler handler) {
        return onPostTick(handler);
    }

    public MmceKubeJSRecipeBuilder onFailure(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.FAILURE, null, handler);
    }

    public MmceKubeJSRecipeBuilder onFinish(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.FINISH, null, handler);
    }

    public MmceKubeJSRecipeBuilder onResultChance(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.RESULT_CHANCE, null, handler);
    }

    public MmceKubeJSRecipeBuilder onFactoryStart(MmceRecipeEventHandler handler) {
        return onStart(handler);
    }

    public MmceKubeJSRecipeBuilder onFactoryPreTick(MmceRecipeEventHandler handler) {
        return onPreTick(handler);
    }

    public MmceKubeJSRecipeBuilder onFactoryPostTick(MmceRecipeEventHandler handler) {
        return onPostTick(handler);
    }

    public MmceKubeJSRecipeBuilder onFactoryFailure(MmceRecipeEventHandler handler) {
        return onFailure(handler);
    }

    public MmceKubeJSRecipeBuilder onFactoryFinish(MmceRecipeEventHandler handler) {
        return onFinish(handler);
    }

    public MmceKubeJSRecipeBuilder addPreCheckHandler(MmceRecipeEventHandler handler) {
        return onPreCheck(handler);
    }

    public MmceKubeJSRecipeBuilder addPostCheckHandler(MmceRecipeEventHandler handler) {
        return onPostCheck(handler);
    }

    public MmceKubeJSRecipeBuilder addCheckHandler(MmceRecipeEventHandler handler) {
        return onCheck(handler);
    }

    public MmceKubeJSRecipeBuilder addStartHandler(MmceRecipeEventHandler handler) {
        return onStart(handler);
    }

    public MmceKubeJSRecipeBuilder addPreTickHandler(MmceRecipeEventHandler handler) {
        return onPreTick(handler);
    }

    public MmceKubeJSRecipeBuilder addPostTickHandler(MmceRecipeEventHandler handler) {
        return onPostTick(handler);
    }

    public MmceKubeJSRecipeBuilder addTickHandler(MmceRecipeEventHandler handler) {
        return onTick(handler);
    }

    public MmceKubeJSRecipeBuilder addFailureHandler(MmceRecipeEventHandler handler) {
        return onFailure(handler);
    }

    public MmceKubeJSRecipeBuilder addFinishHandler(MmceRecipeEventHandler handler) {
        return onFinish(handler);
    }

    public MmceKubeJSRecipeBuilder addResultChanceHandler(MmceRecipeEventHandler handler) {
        return onResultChance(handler);
    }

    public MmceKubeJSRecipeBuilder addFactoryStartHandler(MmceRecipeEventHandler handler) {
        return onFactoryStart(handler);
    }

    public MmceKubeJSRecipeBuilder addFactoryPreTickHandler(MmceRecipeEventHandler handler) {
        return onFactoryPreTick(handler);
    }

    public MmceKubeJSRecipeBuilder addFactoryPostTickHandler(MmceRecipeEventHandler handler) {
        return onFactoryPostTick(handler);
    }

    public MmceKubeJSRecipeBuilder addFactoryFailureHandler(MmceRecipeEventHandler handler) {
        return onFactoryFailure(handler);
    }

    public MmceKubeJSRecipeBuilder addFactoryFinishHandler(MmceRecipeEventHandler handler) {
        return onFactoryFinish(handler);
    }

    public JsonObject json() {
        return root.deepCopy();
    }

    public void build() {
        MmceScriptDataRegistry.registerRecipe(MmceKubeJSMachineBuilder.sourceId("recipes", registryName), root);
    }

    private ResourceLocation mmceId(String value) {
        String id = value == null || value.isBlank() ? "unknown" : value.trim();
        return id.indexOf(':') >= 0
                ? ResourceLocation.parse(id)
                : ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, id);
    }

    private String stringValue(String key, String fallback) {
        return root.has(key) ? root.get(key).getAsString() : fallback;
    }

    private int intValue(String key, int fallback) {
        return root.has(key) ? root.get(key).getAsInt() : fallback;
    }

    private boolean booleanValue(String key, boolean fallback) {
        return root.has(key) ? root.get(key).getAsBoolean() : fallback;
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

    private MmceKubeJSRecipeBuilder recipeHandler(MmceRecipeEventType type, MmceEventPhase phase, MmceRecipeEventHandler handler) {
        if (handler != null) {
            MmceEventRegistry.registerRecipe(registryName, type, event -> {
                if (phase == null || event.getPhase() == phase) {
                    handler.handle(event);
                }
            });
        }
        return this;
    }

    private MmceKubeJSRecipeBuilder energy(String ioType, long energyPerTick) {
        JsonObject requirement = base("modularmachinery:energy", ioType);
        requirement.addProperty("energyPerTick", Math.max(0L, energyPerTick));
        add(requirement);
        return this;
    }

    private MmceKubeJSRecipeBuilder item(String ioType, String item, int amount) {
        JsonObject requirement = base("modularmachinery:item", ioType);
        requirement.addProperty("item", item);
        requirement.addProperty("amount", Math.max(1, amount));
        add(requirement);
        return this;
    }

    private MmceKubeJSRecipeBuilder itemObject(String ioType, Object item, int amount) {
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

    private MmceKubeJSRecipeBuilder fluid(String ioType, String fluid, int amount) {
        return fluid(ioType, fluid, amount, false);
    }

    private MmceKubeJSRecipeBuilder fluid(String ioType, String fluid, int amount, boolean perTick) {
        JsonObject requirement = base(perTick ? "modularmachinery:fluid_pertick" : "modularmachinery:fluid", ioType);
        requirement.addProperty("fluid", fluid);
        requirement.addProperty("amount", Math.max(0, amount));
        add(requirement);
        return this;
    }

    private MmceKubeJSRecipeBuilder fluidObject(String ioType, Object fluid, int amount, boolean perTick) {
        return MmceScriptValues.fluidEntry(fluid, amount).map(entry -> {
            JsonObject requirement = base(perTick ? "modularmachinery:fluid_pertick" : "modularmachinery:fluid", ioType);
            requirement.addProperty("fluid", entry.id());
            requirement.addProperty("amount", entry.amount());
            entry.nbt().ifPresent(nbt -> requirement.add("nbt", nbt.deepCopy()));
            add(requirement);
            return this;
        }).orElse(this);
    }

    private MmceKubeJSRecipeBuilder chemical(String ioType, String chemical, int amount, boolean perTick, boolean legacyGasType) {
        JsonObject requirement = base(chemicalType(perTick, legacyGasType), ioType);
        requirement.addProperty(legacyGasType ? "gas" : "chemical", chemical);
        requirement.addProperty("amount", Math.max(0, amount));
        add(requirement);
        return this;
    }

    private MmceKubeJSRecipeBuilder chemical(String ioType, Object chemical, int amount, boolean perTick, boolean legacyGasType) {
        return MmceScriptValues.chemicalEntry(chemical, amount).map(entry -> {
            JsonObject requirement = base(chemicalType(perTick, legacyGasType), ioType);
            requirement.addProperty(legacyGasType ? "gas" : "chemical", entry.id());
            requirement.addProperty("amount", entry.amount());
            entry.nbt().ifPresent(nbt -> requirement.add("nbt", nbt.deepCopy()));
            add(requirement);
            return this;
        }).orElse(this);
    }

    private MmceKubeJSRecipeBuilder chemicals(String ioType, boolean perTick, boolean legacyGasType, Object... chemicals) {
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

    private MmceKubeJSRecipeBuilder itemArray(String type, String ioType, String[] items, int amount) {
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

    private MmceKubeJSRecipeBuilder itemArray(String type, String ioType, MmceIngredientArrayPrimer ingredientArrayPrimer) {
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

    private MmceKubeJSRecipeBuilder command(String key, String command, int interval) {
        if (command == null || command.isBlank()) {
            return this;
        }

        JsonArray commands;
        if (root.has(key) && root.get(key).isJsonArray()) {
            commands = root.getAsJsonArray(key);
        } else {
            commands = new JsonArray();
            root.add(key, commands);
        }

        if (interval <= 0) {
            commands.add(command);
        } else {
            JsonObject entry = new JsonObject();
            entry.addProperty("command", command);
            entry.addProperty("interval", interval);
            commands.add(entry);
        }
        return this;
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
