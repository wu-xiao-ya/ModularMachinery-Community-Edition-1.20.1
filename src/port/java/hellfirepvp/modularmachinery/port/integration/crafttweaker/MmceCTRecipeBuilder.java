package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.visitor.DataToJsonStringVisitor;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.RecipeBuilder")
public class MmceCTRecipeBuilder {
    protected final String registryName;
    protected final JsonObject root = new JsonObject();
    protected final JsonArray requirements = new JsonArray();
    protected JsonObject lastRequirement;

    protected MmceCTRecipeBuilder(String registryName, String machine, int recipeTime, int priority, boolean cancelIfPerTickFails) {
        this.registryName = registryName;
        root.addProperty("registryName", registryName);
        root.addProperty("machine", machine);
        root.addProperty("recipeTime", Math.max(1, recipeTime));
        root.addProperty("priority", priority);
        root.addProperty("cancelIfPerTickFails", cancelIfPerTickFails);
        root.add("requirements", requirements);
    }

    @ZenCodeType.Method
    public static MmceCTRecipePrimer newBuilder(String recipeRegistryName, String machineRegistryName, int processingTickTime) {
        return newBuilder(recipeRegistryName, machineRegistryName, processingTickTime, 0, false);
    }

    @ZenCodeType.Method
    public static MmceCTRecipePrimer newBuilder(String recipeRegistryName, String machineRegistryName, int processingTickTime, int sortingPriority) {
        return newBuilder(recipeRegistryName, machineRegistryName, processingTickTime, sortingPriority, false);
    }

    @ZenCodeType.Method
    public static MmceCTRecipePrimer newBuilder(String recipeRegistryName, String machineRegistryName, int processingTickTime, int sortingPriority, boolean cancelIfPerTickFails) {
        return new MmceCTRecipePrimer(recipeRegistryName, machineRegistryName, processingTickTime, sortingPriority, cancelIfPerTickFails);
    }

    @ZenCodeType.Method
    public static void registerRecipeJson(String sourceName, String json) {
        JsonObject object = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
        MmceScriptDataRegistry.registerRecipe(MmceCTMachineBuilder.sourceId("crafttweaker/recipes", sourceName), object);
    }

    @ZenCodeType.Method
    public static void registerMachineJson(String sourceName, String json) {
        JsonObject object = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
        MmceScriptDataRegistry.registerMachine(MmceCTMachineBuilder.sourceId("crafttweaker/machines", sourceName), object);
    }

    @ZenCodeType.Method
    public static void registerAdapterJson(String sourceName, String json) {
        JsonObject object = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
        MmceScriptDataRegistry.registerAdapter(MmceCTMachineBuilder.sourceId("crafttweaker/adapters", sourceName), object);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setParallelized(boolean value) {
        root.addProperty("parallelized", value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder parallelized(boolean value) {
        return setParallelized(value);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder parallelize(boolean value) {
        return setParallelized(value);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setParallelize(boolean value) {
        return setParallelized(value);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setRecipeTime(int ticks) {
        root.addProperty("recipeTime", Math.max(1, ticks));
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder recipeTime(int ticks) {
        return setRecipeTime(ticks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setDuration(int ticks) {
        return setRecipeTime(ticks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder duration(int ticks) {
        return setRecipeTime(ticks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setPriority(int priority) {
        root.addProperty("priority", priority);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder priority(int priority) {
        return setPriority(priority);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setCancelIfPerTickFails(boolean value) {
        root.addProperty("cancelIfPerTickFails", value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder cancelIfPerTickFails(boolean value) {
        return setCancelIfPerTickFails(value);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setMaxThreads(int value) {
        root.addProperty("maxThreads", Math.max(-1, value));
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder maxThreads(int value) {
        return setMaxThreads(value);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setThreadName(String value) {
        root.addProperty("threadName", value == null ? "" : value.trim());
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder threadName(String value) {
        return setThreadName(value);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addRecipeTooltip(String... tooltips) {
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

    @ZenCodeType.Method
    public MmceCTRecipeBuilder recipeTooltip(String... tooltips) {
        return addRecipeTooltip(tooltips);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setRecipeTooltip(String... tooltips) {
        return addRecipeTooltip(tooltips);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setLoadJEI(boolean load) {
        root.addProperty("loadJEI", load);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder loadJEI(boolean load) {
        return setLoadJEI(load);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder loadJei(boolean load) {
        return setLoadJEI(load);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setLoadJei(boolean load) {
        return setLoadJEI(load);
    }

    @ZenCodeType.Method
    public boolean getLoadJEI() {
        return !root.has("loadJEI") || root.get("loadJEI").getAsBoolean();
    }

    @ZenCodeType.Method
    public boolean getLoadJei() {
        return getLoadJEI();
    }

    @ZenCodeType.Method
    public ResourceLocation getRecipeRegistryName() {
        return mmceId(stringValue("registryName", registryName));
    }

    @ZenCodeType.Method
    public ResourceLocation getAssociatedMachineName() {
        return mmceId(stringValue("machine", ""));
    }

    @ZenCodeType.Method
    public int getTotalProcessingTickTime() {
        return intValue("recipeTime", 1);
    }

    @ZenCodeType.Method
    public int getPriority() {
        return intValue("priority", 0);
    }

    @ZenCodeType.Method
    public boolean voidPerTickFailure() {
        return booleanValue("cancelIfPerTickFails", false);
    }

    @ZenCodeType.Method
    public boolean isParallelized() {
        return booleanValue("parallelized", false);
    }

    @ZenCodeType.Method
    public int getMaxThreads() {
        return intValue("maxThreads", -1);
    }

    @ZenCodeType.Method
    public String getThreadName() {
        return stringValue("threadName", "");
    }

    @ZenCodeType.Method
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

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addEnergyPerTickInput(long energyPerTick) {
        return energy("input", energyPerTick);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addEnergyPerTickOutput(long energyPerTick) {
        return energy("output", energyPerTick);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemInput(String item, int amount) {
        return item("input", item, amount);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemInput(IItemStack stack) {
        return item("input", stack);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemInput(IIngredient ingredient) {
        return ingredient("input", ingredient, 0);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemInput(IIngredientWithAmount ingredient) {
        return ingredient("input", ingredient);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addInput(IItemStack stack) {
        return addItemInput(stack);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addInput(IIngredient ingredient) {
        return addItemInput(ingredient);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addInput(IIngredientWithAmount ingredient) {
        return addItemInput(ingredient);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemInputs(IItemStack... stacks) {
        return items("input", stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemInputs(IIngredient... ingredients) {
        return ingredients("input", ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemInputs(IIngredientWithAmount... ingredients) {
        return ingredients("input", ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addInputs(IItemStack... stacks) {
        return addItemInputs(stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addInputs(IIngredient... ingredients) {
        return addItemInputs(ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addInputs(IIngredientWithAmount... ingredients) {
        return addItemInputs(ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemOutput(String item, int amount) {
        return item("output", item, amount);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemOutput(IItemStack stack) {
        return item("output", stack);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemOutput(IIngredient ingredient) {
        return ingredient("output", ingredient, 0);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemOutput(IIngredientWithAmount ingredient) {
        return ingredient("output", ingredient);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFuelItemInput(int requiredTotalBurnTime) {
        JsonObject requirement = base("modularmachinery:fuel", "input");
        requirement.addProperty("item", "any:fuel");
        requirement.addProperty("time", Math.max(1, requiredTotalBurnTime));
        add(requirement);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addOutput(IItemStack stack) {
        return addItemOutput(stack);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addOutput(IIngredient ingredient) {
        return addItemOutput(ingredient);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addOutput(IIngredientWithAmount ingredient) {
        return addItemOutput(ingredient);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemOutputs(IItemStack... stacks) {
        return items("output", stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemOutputs(IIngredient... ingredients) {
        return ingredients("output", ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemOutputs(IIngredientWithAmount... ingredients) {
        return ingredients("output", ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addOutputs(IItemStack... stacks) {
        return addItemOutputs(stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addOutputs(IIngredient... ingredients) {
        return addItemOutputs(ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addOutputs(IIngredientWithAmount... ingredients) {
        return addItemOutputs(ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFluidInput(String fluid, int amount) {
        return fluid("input", fluid, amount);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFluidInput(IFluidStack stack) {
        return fluid("input", stack, false);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addInput(IFluidStack stack) {
        return addFluidInput(stack);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFluidInputs(IFluidStack... stacks) {
        return fluids("input", false, stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addInputs(IFluidStack... stacks) {
        return addFluidInputs(stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFluidOutput(String fluid, int amount) {
        return fluid("output", fluid, amount);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFluidOutput(IFluidStack stack) {
        return fluid("output", stack, false);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addOutput(IFluidStack stack) {
        return addFluidOutput(stack);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFluidOutputs(IFluidStack... stacks) {
        return fluids("output", false, stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addOutputs(IFluidStack... stacks) {
        return addFluidOutputs(stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFluidPerTickInput(String fluid, int amount) {
        return fluid("input", fluid, amount, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFluidPerTickInput(IFluidStack stack) {
        return fluid("input", stack, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFluidPerTickInputs(IFluidStack... stacks) {
        return fluids("input", true, stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFluidPerTickOutput(String fluid, int amount) {
        return fluid("output", fluid, amount, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFluidPerTickOutput(IFluidStack stack) {
        return fluid("output", stack, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFluidPerTickOutputs(IFluidStack... stacks) {
        return fluids("output", true, stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addGasInput(String gas, int amount) {
        return chemical("input", gas, amount, false, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addGasInput(Object gasStack) {
        return chemical("input", gasStack, 0, false, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addGasOutput(String gas, int amount) {
        return chemical("output", gas, amount, false, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addGasOutput(Object gasStack) {
        return chemical("output", gasStack, 0, false, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addGasInputs(Object... gasStacks) {
        return chemicals("input", false, true, gasStacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addGasOutputs(Object... gasStacks) {
        return chemicals("output", false, true, gasStacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addGasPerTickInput(String gas, int amount) {
        return chemical("input", gas, amount, true, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addGasPerTickInput(Object gasStack) {
        return chemical("input", gasStack, 0, true, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addGasPerTickOutput(String gas, int amount) {
        return chemical("output", gas, amount, true, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addGasPerTickOutput(Object gasStack) {
        return chemical("output", gasStack, 0, true, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addGasPerTickInputs(Object... gasStacks) {
        return chemicals("input", true, true, gasStacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addGasPerTickOutputs(Object... gasStacks) {
        return chemicals("output", true, true, gasStacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addChemicalInput(String chemical, int amount) {
        return chemical("input", chemical, amount, false, false);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addChemicalInput(Object chemicalStack) {
        return chemical("input", chemicalStack, 0, false, false);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addChemicalOutput(String chemical, int amount) {
        return chemical("output", chemical, amount, false, false);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addChemicalOutput(Object chemicalStack) {
        return chemical("output", chemicalStack, 0, false, false);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addChemicalInputs(Object... chemicalStacks) {
        return chemicals("input", false, false, chemicalStacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addChemicalOutputs(Object... chemicalStacks) {
        return chemicals("output", false, false, chemicalStacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addChemicalPerTickInput(String chemical, int amount) {
        return chemical("input", chemical, amount, true, false);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addChemicalPerTickInput(Object chemicalStack) {
        return chemical("input", chemicalStack, 0, true, false);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addChemicalPerTickOutput(String chemical, int amount) {
        return chemical("output", chemical, amount, true, false);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addChemicalPerTickOutput(Object chemicalStack) {
        return chemical("output", chemicalStack, 0, true, false);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addChemicalPerTickInputs(Object... chemicalStacks) {
        return chemicals("input", true, false, chemicalStacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addChemicalPerTickOutputs(Object... chemicalStacks) {
        return chemicals("output", true, false, chemicalStacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addIngredientArrayInput(String[] items, int amount) {
        return itemArray("modularmachinery:ingredient_array_input", "input", items, amount);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addIngredientArrayInput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return itemArray("modularmachinery:ingredient_array_input", "input", ingredientArrayPrimer);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addRandomItemOutput(String[] items, int amount) {
        return itemArray("modularmachinery:ingredient_array_input", "output", items, amount);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addRandomItemOutput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return itemArray("modularmachinery:ingredient_array_input", "output", ingredientArrayPrimer);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addCatalystInput(String item, int amount) {
        return itemArray("modularmachinery:catalyst", "input", new String[] { item }, amount);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addCatalystInput(IItemStack stack) {
        MmceCTIngredientArrayPrimer primer = new MmceCTIngredientArrayPrimer();
        primer.addIngredient(stack);
        return addCatalystInput(primer);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addCatalystInput(IIngredient ingredient) {
        MmceCTIngredientArrayPrimer primer = new MmceCTIngredientArrayPrimer();
        primer.addIngredient(ingredient);
        return addCatalystInput(primer);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addCatalystInput(IIngredientWithAmount ingredient) {
        MmceCTIngredientArrayPrimer primer = new MmceCTIngredientArrayPrimer();
        primer.addIngredient(ingredient);
        return addCatalystInput(primer);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addCatalystInput(String[] items, int amount) {
        return itemArray("modularmachinery:catalyst", "input", items, amount);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addCatalystInput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        return itemArray("modularmachinery:catalyst", "input", ingredientArrayPrimer);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addCatalystInput(String item, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        itemArray("modularmachinery:catalyst", "input", new String[] { item }, amount);
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addCatalystInput(IItemStack stack, String[] tooltips, MmceRecipeModifier[] modifiers) {
        addCatalystInput(stack);
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addCatalystInput(IIngredient ingredient, String[] tooltips, MmceRecipeModifier[] modifiers) {
        addCatalystInput(ingredient);
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addCatalystInput(IIngredientWithAmount ingredient, String[] tooltips, MmceRecipeModifier[] modifiers) {
        addCatalystInput(ingredient);
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addCatalystInput(String[] items, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        itemArray("modularmachinery:catalyst", "input", items, amount);
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addCatalystInput(MmceIngredientArrayPrimer ingredientArrayPrimer, String[] tooltips, MmceRecipeModifier[] modifiers) {
        itemArray("modularmachinery:catalyst", "input", ingredientArrayPrimer);
        applyCatalystMetadata(tooltips, modifiers);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addSmartInterfaceDataInput(String type, float value) {
        return addSmartInterfaceDataInput(type, value, value);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addSmartInterfaceDataInput(String type, float minValue, float maxValue) {
        JsonObject requirement = base("modularmachinery:interface_number_input", "input");
        requirement.addProperty("interfaceType", type);
        requirement.addProperty("minValue", minValue);
        requirement.addProperty("maxValue", maxValue);
        add(requirement);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setChance(float chance) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("chance", Math.max(0.0F, Math.min(1.0F, chance)));
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder chance(float chance) {
        return setChance(chance);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setChance(double chance) {
        return setChance((float) chance);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setTag(String tag) {
        if (lastRequirement != null && tag != null && !tag.isBlank()) {
            lastRequirement.addProperty("selector-tag", tag);
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setSelectorTag(String tag) {
        return setTag(tag);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder selectorTag(String tag) {
        return setTag(tag);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder selector_tag(String tag) {
        return setTag(tag);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder consumeDurability(int durability) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("consumeDurability", Math.max(0, durability));
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setMinMaxAmount(int min, int max) {
        if (lastRequirement != null) {
            int safeMin = Math.max(1, min);
            int safeMax = Math.max(safeMin, max);
            lastRequirement.addProperty("minAmount", safeMin);
            lastRequirement.addProperty("maxAmount", safeMax);
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder minMaxAmount(int min, int max) {
        return setMinMaxAmount(min, max);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setMinMaxOutputAmount(int min, int max) {
        return setMinMaxAmount(min, max);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder minMaxOutputAmount(int min, int max) {
        return setMinMaxAmount(min, max);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setNbt(String json) {
        if (lastRequirement != null && json != null && !json.isBlank()) {
            lastRequirement.add("nbt", parseObject(json));
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setNBT(String json) {
        return setNbt(json);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setNBTChecker(MmceItemChecker checker) {
        return setNBTChecker("", checker);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setNBTChecker(String checkerId, MmceItemChecker checker) {
        if (lastRequirement != null && checker != null) {
            lastRequirement.addProperty("checker-id", MmceItemCallbackRegistry.registerChecker(checkerId, checker));
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setNbtChecker(MmceItemChecker checker) {
        return setNBTChecker(checker);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setNbtChecker(String checkerId, MmceItemChecker checker) {
        return setNBTChecker(checkerId, checker);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemModifier(MmceItemModifier modifier) {
        return addItemModifier("", modifier);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addItemModifier(String modifierId, MmceItemModifier modifier) {
        if (lastRequirement != null && modifier != null) {
            lastRequirement.addProperty("item-modifier-id", MmceItemCallbackRegistry.registerModifier(modifierId, modifier));
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setDisplayNbt(String json) {
        if (lastRequirement != null && json != null && !json.isBlank()) {
            lastRequirement.add("nbt-display", parseObject(json));
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setDisplayNBT(String json) {
        return setDisplayNbt(json);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setPreViewNBT(String json) {
        return setDisplayNbt(json);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setPreViewNBT(IData nbt) {
        if (lastRequirement != null && nbt != null) {
            lastRequirement.add("nbt-display", parseObject(nbt.accept(DataToJsonStringVisitor.INSTANCE)));
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder previewNbt(String json) {
        return setDisplayNbt(json);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setPreviewNbt(String json) {
        return setDisplayNbt(json);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setPreviewNBT(String json) {
        return setDisplayNbt(json);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setPreviewNBT(IData nbt) {
        return setPreViewNBT(nbt);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setTriggerTime(int tickTime) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("triggerTime", Math.max(0, tickTime));
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder triggerTime(int tickTime) {
        return setTriggerTime(tickTime);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setTriggerRepeatable(boolean repeatable) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("triggerRepeatable", repeatable);
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder triggerRepeatable(boolean repeatable) {
        return setTriggerRepeatable(repeatable);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setIgnoreOutputCheck(boolean ignore) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("ignoreOutputCheck", ignore);
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder ignoreOutputCheck(boolean ignore) {
        return setIgnoreOutputCheck(ignore);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder setParallelizeUnaffected(boolean unaffected) {
        if (lastRequirement != null) {
            lastRequirement.addProperty("parallelizeUnaffected", unaffected);
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder parallelizeUnaffected(boolean unaffected) {
        return setParallelizeUnaffected(unaffected);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addStartCommand(String command) {
        return command("startCommands", command, -1);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder startCommand(String command) {
        return addStartCommand(command);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addStartCommand(String command, int interval) {
        return command("startCommands", command, interval);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder startCommand(String command, int interval) {
        return addStartCommand(command, interval);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addProcessingCommand(String command) {
        return command("processingCommands", command, -1);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder processingCommand(String command) {
        return addProcessingCommand(command);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addProcessingCommand(String command, int interval) {
        return command("processingCommands", command, interval);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder processingCommand(String command, int interval) {
        return addProcessingCommand(command, interval);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFinishCommand(String command) {
        return command("finishCommands", command, -1);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder finishCommand(String command) {
        return addFinishCommand(command);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFinishCommand(String command, int interval) {
        return command("finishCommands", command, interval);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder finishCommand(String command, int interval) {
        return addFinishCommand(command, interval);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addPreCheckHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.CHECK, MmceEventPhase.START, handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addPostCheckHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.CHECK, MmceEventPhase.END, handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addCheckHandler(MmceRecipeEventHandler handler) {
        return addPostCheckHandler(handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addStartHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.START, null, handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addPreTickHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.TICK, MmceEventPhase.START, handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addPostTickHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.TICK, MmceEventPhase.END, handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addTickHandler(MmceRecipeEventHandler handler) {
        return addPostTickHandler(handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFailureHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.FAILURE, null, handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFinishHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.FINISH, null, handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addResultChanceHandler(MmceRecipeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.RESULT_CHANCE, null, handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFactoryStartHandler(MmceRecipeEventHandler handler) {
        return addStartHandler(handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFactoryPreTickHandler(MmceRecipeEventHandler handler) {
        return addPreTickHandler(handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFactoryPostTickHandler(MmceRecipeEventHandler handler) {
        return addPostTickHandler(handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFactoryFailureHandler(MmceRecipeEventHandler handler) {
        return addFailureHandler(handler);
    }

    @ZenCodeType.Method
    public MmceCTRecipeBuilder addFactoryFinishHandler(MmceRecipeEventHandler handler) {
        return addFinishHandler(handler);
    }

    @ZenCodeType.Method
    public void build() {
        MmceScriptDataRegistry.registerRecipe(MmceCTMachineBuilder.sourceId("crafttweaker/recipes", registryName), root);
    }

    protected void applyCatalystMetadata(String[] tooltips, MmceRecipeModifier[] modifiers) {
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

    private MmceCTRecipeBuilder recipeHandler(MmceRecipeEventType type, MmceEventPhase phase, MmceRecipeEventHandler handler) {
        if (handler != null) {
            MmceEventRegistry.registerRecipe(registryName, type, event -> {
                if (phase == null || event.getPhase() == phase) {
                    handler.handle(event);
                }
            });
        }
        return this;
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

    private MmceCTRecipeBuilder energy(String ioType, long energyPerTick) {
        JsonObject requirement = base("modularmachinery:energy", ioType);
        requirement.addProperty("energyPerTick", Math.max(0L, energyPerTick));
        add(requirement);
        return this;
    }

    private MmceCTRecipeBuilder item(String ioType, String item, int amount) {
        JsonObject requirement = base("modularmachinery:item", ioType);
        requirement.addProperty("item", item);
        requirement.addProperty("amount", Math.max(1, amount));
        add(requirement);
        return this;
    }

    private MmceCTRecipeBuilder item(String ioType, IItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return this;
        }
        item(ioType, itemId(stack), itemAmount(stack));
        JsonObject nbt = itemStackNbt(stack.getInternal());
        if (!nbt.isEmpty() && lastRequirement != null) {
            lastRequirement.add("nbt", nbt);
        }
        return this;
    }

    private MmceCTRecipeBuilder items(String ioType, IItemStack... stacks) {
        if (stacks != null) {
            for (IItemStack stack : stacks) {
                item(ioType, stack);
            }
        }
        return this;
    }

    private MmceCTRecipeBuilder ingredient(String ioType, IIngredient ingredient, int amountOverride) {
        if (ingredient == null || ingredient.isEmpty()) {
            return this;
        }
        if (ingredient instanceof IItemStack stack) {
            return item(ioType, amountOverride > 0 ? stack.withAmount(amountOverride) : stack);
        }
        MmceCTIngredientArrayPrimer primer = new MmceCTIngredientArrayPrimer();
        primer.addIngredient(ingredient, amountOverride > 0 ? amountOverride : 1);
        return itemArray(
                "input".equals(ioType) ? "modularmachinery:ingredient_array_input" : "modularmachinery:ingredient_array_output",
                ioType,
                primer
        );
    }

    private MmceCTRecipeBuilder ingredient(String ioType, IIngredientWithAmount ingredient) {
        if (ingredient == null || ingredient.ingredient() == null) {
            return this;
        }
        return ingredient(ioType, ingredient.ingredient(), ingredient.amount());
    }

    private MmceCTRecipeBuilder ingredients(String ioType, IIngredient... ingredients) {
        if (ingredients != null) {
            for (IIngredient ingredient : ingredients) {
                ingredient(ioType, ingredient, 0);
            }
        }
        return this;
    }

    private MmceCTRecipeBuilder ingredients(String ioType, IIngredientWithAmount... ingredients) {
        if (ingredients != null) {
            for (IIngredientWithAmount ingredient : ingredients) {
                ingredient(ioType, ingredient);
            }
        }
        return this;
    }

    private MmceCTRecipeBuilder fluid(String ioType, String fluid, int amount) {
        return fluid(ioType, fluid, amount, false);
    }

    private MmceCTRecipeBuilder fluid(String ioType, String fluid, int amount, boolean perTick) {
        JsonObject requirement = base(perTick ? "modularmachinery:fluid_pertick" : "modularmachinery:fluid", ioType);
        requirement.addProperty("fluid", fluid);
        requirement.addProperty("amount", Math.max(0, amount));
        add(requirement);
        return this;
    }

    private MmceCTRecipeBuilder fluid(String ioType, IFluidStack stack, boolean perTick) {
        if (stack == null || stack.isEmpty()) {
            return this;
        }
        fluid(ioType, stack.getRegistryName().toString(), safeFluidAmount(stack), perTick);
        JsonObject nbt = fluidStackNbt(stack.getInternal());
        if (!nbt.isEmpty() && lastRequirement != null) {
            lastRequirement.add("nbt", nbt);
        }
        return this;
    }

    private MmceCTRecipeBuilder fluids(String ioType, boolean perTick, IFluidStack... stacks) {
        if (stacks != null) {
            for (IFluidStack stack : stacks) {
                fluid(ioType, stack, perTick);
            }
        }
        return this;
    }

    private MmceCTRecipeBuilder chemical(String ioType, String chemical, int amount, boolean perTick, boolean legacyGasType) {
        JsonObject requirement = base(chemicalType(perTick, legacyGasType), ioType);
        requirement.addProperty(legacyGasType ? "gas" : "chemical", chemical);
        requirement.addProperty("amount", Math.max(0, amount));
        add(requirement);
        return this;
    }

    private MmceCTRecipeBuilder chemical(String ioType, Object chemical, int amount, boolean perTick, boolean legacyGasType) {
        return MmceScriptValues.chemicalEntry(chemical, amount).map(entry -> {
            JsonObject requirement = base(chemicalType(perTick, legacyGasType), ioType);
            requirement.addProperty(legacyGasType ? "gas" : "chemical", entry.id());
            requirement.addProperty("amount", entry.amount());
            entry.nbt().ifPresent(nbt -> requirement.add("nbt", nbt.deepCopy()));
            add(requirement);
            return this;
        }).orElse(this);
    }

    private MmceCTRecipeBuilder chemicals(String ioType, boolean perTick, boolean legacyGasType, Object... chemicals) {
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

    private MmceCTRecipeBuilder itemArray(String type, String ioType, String[] items, int amount) {
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

    private MmceCTRecipeBuilder itemArray(String type, String ioType, MmceIngredientArrayPrimer ingredientArrayPrimer) {
        JsonObject requirement = base(type, ioType);
        requirement.add("items", ingredientArrayPrimer == null ? new JsonArray() : ingredientArrayPrimer.items());
        add(requirement);
        return this;
    }

    private MmceCTRecipeBuilder command(String key, String command, int interval) {
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

    protected static JsonObject parseObject(String json) {
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

    static String itemId(IItemStack stack) {
        return stack.getRegistryName().toString();
    }

    static int itemAmount(IItemStack stack) {
        return Math.max(1, stack.amount());
    }

    static int safeFluidAmount(IFluidStack stack) {
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, stack.getAmount()));
    }

    static JsonObject itemStackNbt(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (stack.has(DataComponents.DAMAGE)) {
            tag.putInt("Damage", stack.getOrDefault(DataComponents.DAMAGE, 0));
        }
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name != null) {
            CompoundTag display = tag.contains("display") ? tag.getCompound("display").copy() : new CompoundTag();
            display.putString("Name", name.getString());
            tag.put("display", display);
        }
        return nbtToJson(tag);
    }

    static JsonObject fluidStackNbt(FluidStack stack) {
        return nbtToJson(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
    }

    private static JsonObject nbtToJson(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return new JsonObject();
        }
        return NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag).getAsJsonObject();
    }
}
