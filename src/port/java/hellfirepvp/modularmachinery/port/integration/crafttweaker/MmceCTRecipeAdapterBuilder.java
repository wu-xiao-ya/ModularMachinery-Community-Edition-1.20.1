package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.visitor.DataToJsonStringVisitor;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEventHandler;
import hellfirepvp.modularmachinery.port.integration.MmceIngredientArrayPrimer;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeAdapterBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.RecipeAdapterBuilder")
public final class MmceCTRecipeAdapterBuilder {
    private final MmceRecipeAdapterBuilder builder;

    private MmceCTRecipeAdapterBuilder(String sourceName, String machine, String adapter) {
        this.builder = new MmceRecipeAdapterBuilder(
                MmceCTMachineBuilder.sourceId("crafttweaker/adapters", sourceName),
                machine,
                adapter
        );
    }

    @ZenCodeType.Method
    public static MmceCTRecipeAdapterBuilder newBuilder(String sourceName, String machine, String adapter) {
        return new MmceCTRecipeAdapterBuilder(sourceName, machine, adapter);
    }

    @ZenCodeType.Method
    public static MmceCTRecipeAdapterBuilder create(String machine, String adapter) {
        return new MmceCTRecipeAdapterBuilder(machine + "_" + adapter.replace(':', '_'), machine, adapter);
    }

    @ZenCodeType.Method
    public static MmceCTRecipeAdapterBuilder createFromParentMachine(String machine, String parentMachine) {
        return create(machine, parentMachine);
    }

    @ZenCodeType.Method
    public static MmceCTRecipeAdapterBuilder fromParentMachine(String machine, String parentMachine) {
        return createFromParentMachine(machine, parentMachine);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setRecipeTime(int ticks) {
        builder.recipeTime(ticks);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder recipeTime(int ticks) {
        return setRecipeTime(ticks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setDuration(int ticks) {
        builder.duration(ticks);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder duration(int ticks) {
        return setDuration(ticks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setPriority(int priority) {
        builder.priority(priority);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder priority(int priority) {
        return setPriority(priority);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setCancelIfPerTickFails(boolean value) {
        builder.cancelIfPerTickFails(value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder cancelIfPerTickFails(boolean value) {
        return setCancelIfPerTickFails(value);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setParallelized(boolean value) {
        builder.parallelized(value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder parallelized(boolean value) {
        return setParallelized(value);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder parallelize(boolean value) {
        return setParallelized(value);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setParallelize(boolean value) {
        return setParallelized(value);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setMaxThreads(int value) {
        builder.maxThreads(value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder maxThreads(int value) {
        return setMaxThreads(value);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setThreadName(String value) {
        builder.threadName(value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder threadName(String value) {
        return setThreadName(value);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addRecipeTooltip(String... tooltips) {
        builder.addRecipeTooltip(tooltips);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder recipeTooltip(String... tooltips) {
        builder.recipeTooltip(tooltips);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setRecipeTooltip(String... tooltips) {
        builder.setRecipeTooltip(tooltips);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setLoadJEI(boolean load) {
        builder.setLoadJEI(load);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder loadJEI(boolean load) {
        return setLoadJEI(load);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder loadJei(boolean load) {
        return setLoadJEI(load);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setLoadJei(boolean load) {
        return setLoadJEI(load);
    }

    @ZenCodeType.Method
    public boolean getLoadJEI() {
        return builder.getLoadJEI();
    }

    @ZenCodeType.Method
    public boolean getLoadJei() {
        return builder.getLoadJei();
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addPreCheckHandler(MmceRecipeEventHandler handler) {
        builder.addPreCheckHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addPostCheckHandler(MmceRecipeEventHandler handler) {
        builder.addPostCheckHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addCheckHandler(MmceRecipeEventHandler handler) {
        builder.addCheckHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addStartHandler(MmceRecipeEventHandler handler) {
        builder.addStartHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addPreTickHandler(MmceRecipeEventHandler handler) {
        builder.addPreTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addPostTickHandler(MmceRecipeEventHandler handler) {
        builder.addPostTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addTickHandler(MmceRecipeEventHandler handler) {
        builder.addTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFailureHandler(MmceRecipeEventHandler handler) {
        builder.addFailureHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFinishHandler(MmceRecipeEventHandler handler) {
        builder.addFinishHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addResultChanceHandler(MmceRecipeEventHandler handler) {
        builder.addResultChanceHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFactoryStartHandler(MmceRecipeEventHandler handler) {
        builder.addFactoryStartHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFactoryPreTickHandler(MmceRecipeEventHandler handler) {
        builder.addFactoryPreTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFactoryPostTickHandler(MmceRecipeEventHandler handler) {
        builder.addFactoryPostTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFactoryFailureHandler(MmceRecipeEventHandler handler) {
        builder.addFactoryFailureHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFactoryFinishHandler(MmceRecipeEventHandler handler) {
        builder.addFactoryFinishHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addModifier(String target, String io, int operation, double multiplier) {
        builder.modifier(target, io, operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addModifier(String target, String io, int operation, double multiplier, boolean affectChance) {
        builder.modifier(target, io, operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addModifier(String target, String io, double multiplier, int operation) {
        builder.modifier(target, io, multiplier, operation);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addModifier(String target, String io, double multiplier, int operation, boolean affectChance) {
        builder.modifier(target, io, multiplier, operation, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addModifier(MmceRecipeModifier modifier) {
        builder.modifier(modifier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemInputModifier(int operation, double multiplier) {
        builder.itemInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemOutputModifier(int operation, double multiplier) {
        builder.itemOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidInputModifier(int operation, double multiplier) {
        builder.fluidInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidOutputModifier(int operation, double multiplier) {
        builder.fluidOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasInputModifier(int operation, double multiplier) {
        builder.gasInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasOutputModifier(int operation, double multiplier) {
        builder.gasOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalInputModifier(int operation, double multiplier) {
        builder.chemicalInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalOutputModifier(int operation, double multiplier) {
        builder.chemicalOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addEnergyInputModifier(int operation, double multiplier) {
        builder.energyInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addEnergyOutputModifier(int operation, double multiplier) {
        builder.energyOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addDurationModifier(int operation, double multiplier) {
        builder.durationModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addEnergyPerTickInput(long energyPerTick) {
        builder.energyInput(energyPerTick);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addEnergyPerTickOutput(long energyPerTick) {
        builder.energyOutput(energyPerTick);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemInput(String item, int amount) {
        builder.itemInput(item, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemInput(IItemStack stack) {
        return item("input", stack);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemInput(IIngredient ingredient) {
        return ingredient("input", ingredient, 0);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemInput(IIngredientWithAmount ingredient) {
        return ingredient("input", ingredient);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addInput(IItemStack stack) {
        return addItemInput(stack);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addInput(IIngredient ingredient) {
        return addItemInput(ingredient);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addInput(IIngredientWithAmount ingredient) {
        return addItemInput(ingredient);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemInputs(IItemStack... stacks) {
        return items("input", stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemInputs(IIngredient... ingredients) {
        return ingredients("input", ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemInputs(IIngredientWithAmount... ingredients) {
        return ingredients("input", ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addInputs(IItemStack... stacks) {
        return addItemInputs(stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addInputs(IIngredient... ingredients) {
        return addItemInputs(ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addInputs(IIngredientWithAmount... ingredients) {
        return addItemInputs(ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemOutput(String item, int amount) {
        builder.itemOutput(item, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemOutput(IItemStack stack) {
        return item("output", stack);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemOutput(IIngredient ingredient) {
        return ingredient("output", ingredient, 0);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemOutput(IIngredientWithAmount ingredient) {
        return ingredient("output", ingredient);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFuelItemInput(int requiredTotalBurnTime) {
        builder.fuelItemInput(requiredTotalBurnTime);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addOutput(IItemStack stack) {
        return addItemOutput(stack);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addOutput(IIngredient ingredient) {
        return addItemOutput(ingredient);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addOutput(IIngredientWithAmount ingredient) {
        return addItemOutput(ingredient);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemOutputs(IItemStack... stacks) {
        return items("output", stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemOutputs(IIngredient... ingredients) {
        return ingredients("output", ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addItemOutputs(IIngredientWithAmount... ingredients) {
        return ingredients("output", ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addOutputs(IItemStack... stacks) {
        return addItemOutputs(stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addOutputs(IIngredient... ingredients) {
        return addItemOutputs(ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addOutputs(IIngredientWithAmount... ingredients) {
        return addItemOutputs(ingredients);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidInput(String fluid, int amount) {
        builder.fluidInput(fluid, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidInput(IFluidStack stack) {
        return fluid("input", stack, false);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addInput(IFluidStack stack) {
        return addFluidInput(stack);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidInputs(IFluidStack... stacks) {
        return fluids("input", false, stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addInputs(IFluidStack... stacks) {
        return addFluidInputs(stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidOutput(String fluid, int amount) {
        builder.fluidOutput(fluid, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidOutput(IFluidStack stack) {
        return fluid("output", stack, false);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addOutput(IFluidStack stack) {
        return addFluidOutput(stack);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidOutputs(IFluidStack... stacks) {
        return fluids("output", false, stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addOutputs(IFluidStack... stacks) {
        return addFluidOutputs(stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidPerTickInput(String fluid, int amount) {
        builder.fluidPerTickInput(fluid, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidPerTickInput(IFluidStack stack) {
        return fluid("input", stack, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidPerTickInputs(IFluidStack... stacks) {
        return fluids("input", true, stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidPerTickOutput(String fluid, int amount) {
        builder.fluidPerTickOutput(fluid, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidPerTickOutput(IFluidStack stack) {
        return fluid("output", stack, true);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addFluidPerTickOutputs(IFluidStack... stacks) {
        return fluids("output", true, stacks);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasInput(String gas, int amount) {
        builder.gasInput(gas, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasInput(Object gasStack) {
        builder.gasInput(gasStack);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasOutput(String gas, int amount) {
        builder.gasOutput(gas, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasOutput(Object gasStack) {
        builder.gasOutput(gasStack);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasInputs(Object... gasStacks) {
        builder.gasInputs(gasStacks);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasOutputs(Object... gasStacks) {
        builder.gasOutputs(gasStacks);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasPerTickInput(String gas, int amount) {
        builder.gasPerTickInput(gas, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasPerTickInput(Object gasStack) {
        builder.gasPerTickInput(gasStack);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasPerTickOutput(String gas, int amount) {
        builder.gasPerTickOutput(gas, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasPerTickOutput(Object gasStack) {
        builder.gasPerTickOutput(gasStack);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasPerTickInputs(Object... gasStacks) {
        builder.gasPerTickInputs(gasStacks);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addGasPerTickOutputs(Object... gasStacks) {
        builder.gasPerTickOutputs(gasStacks);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalInput(String chemical, int amount) {
        builder.chemicalInput(chemical, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalInput(Object chemicalStack) {
        builder.chemicalInput(chemicalStack);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalOutput(String chemical, int amount) {
        builder.chemicalOutput(chemical, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalOutput(Object chemicalStack) {
        builder.chemicalOutput(chemicalStack);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalInputs(Object... chemicalStacks) {
        builder.chemicalInputs(chemicalStacks);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalOutputs(Object... chemicalStacks) {
        builder.chemicalOutputs(chemicalStacks);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalPerTickInput(String chemical, int amount) {
        builder.chemicalPerTickInput(chemical, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalPerTickInput(Object chemicalStack) {
        builder.chemicalPerTickInput(chemicalStack);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalPerTickOutput(String chemical, int amount) {
        builder.chemicalPerTickOutput(chemical, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalPerTickOutput(Object chemicalStack) {
        builder.chemicalPerTickOutput(chemicalStack);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalPerTickInputs(Object... chemicalStacks) {
        builder.chemicalPerTickInputs(chemicalStacks);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addChemicalPerTickOutputs(Object... chemicalStacks) {
        builder.chemicalPerTickOutputs(chemicalStacks);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addIngredientArrayInput(String[] items, int amount) {
        builder.ingredientArrayInput(items, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addIngredientArrayInput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        builder.ingredientArrayInput(ingredientArrayPrimer);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addRandomItemOutput(String[] items, int amount) {
        builder.randomItemOutput(items, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addRandomItemOutput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        builder.randomItemOutput(ingredientArrayPrimer);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addCatalystInput(String item, int amount) {
        builder.catalystInput(item, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addCatalystInput(IItemStack stack) {
        MmceCTIngredientArrayPrimer primer = new MmceCTIngredientArrayPrimer();
        primer.addIngredient(stack);
        builder.catalystInput(primer);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addCatalystInput(IIngredient ingredient) {
        MmceCTIngredientArrayPrimer primer = new MmceCTIngredientArrayPrimer();
        primer.addIngredient(ingredient);
        builder.catalystInput(primer);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addCatalystInput(IIngredientWithAmount ingredient) {
        MmceCTIngredientArrayPrimer primer = new MmceCTIngredientArrayPrimer();
        primer.addIngredient(ingredient);
        builder.catalystInput(primer);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addCatalystInput(String[] items, int amount) {
        builder.catalystInput(items, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addCatalystInput(MmceIngredientArrayPrimer ingredientArrayPrimer) {
        builder.catalystInput(ingredientArrayPrimer);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addCatalystInput(String item, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        builder.catalystInput(item, amount, tooltips, modifiers);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addCatalystInput(IItemStack stack, String[] tooltips, MmceRecipeModifier[] modifiers) {
        MmceCTIngredientArrayPrimer primer = new MmceCTIngredientArrayPrimer();
        primer.addIngredient(stack);
        builder.catalystInput(primer, tooltips, modifiers);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addCatalystInput(IIngredient ingredient, String[] tooltips, MmceRecipeModifier[] modifiers) {
        MmceCTIngredientArrayPrimer primer = new MmceCTIngredientArrayPrimer();
        primer.addIngredient(ingredient);
        builder.catalystInput(primer, tooltips, modifiers);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addCatalystInput(IIngredientWithAmount ingredient, String[] tooltips, MmceRecipeModifier[] modifiers) {
        MmceCTIngredientArrayPrimer primer = new MmceCTIngredientArrayPrimer();
        primer.addIngredient(ingredient);
        builder.catalystInput(primer, tooltips, modifiers);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addCatalystInput(String[] items, int amount, String[] tooltips, MmceRecipeModifier[] modifiers) {
        builder.catalystInput(items, amount, tooltips, modifiers);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addCatalystInput(MmceIngredientArrayPrimer ingredientArrayPrimer, String[] tooltips, MmceRecipeModifier[] modifiers) {
        builder.catalystInput(ingredientArrayPrimer, tooltips, modifiers);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addSmartInterfaceDataInput(String type, float value) {
        builder.addSmartInterfaceDataInput(type, value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder addSmartInterfaceDataInput(String type, float minValue, float maxValue) {
        builder.addSmartInterfaceDataInput(type, minValue, maxValue);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setChance(float chance) {
        builder.chance(chance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder chance(float chance) {
        return setChance(chance);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setChance(double chance) {
        builder.setChance(chance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setTag(String tag) {
        builder.selectorTag(tag);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setSelectorTag(String tag) {
        builder.selectorTag(tag);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder selectorTag(String tag) {
        return setSelectorTag(tag);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder selector_tag(String tag) {
        return setSelectorTag(tag);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder consumeDurability(int durability) {
        builder.consumeDurability(durability);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setMinMaxAmount(int min, int max) {
        builder.setMinMaxAmount(min, max);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder minMaxAmount(int min, int max) {
        return setMinMaxAmount(min, max);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setMinMaxOutputAmount(int min, int max) {
        builder.setMinMaxOutputAmount(min, max);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder minMaxOutputAmount(int min, int max) {
        return setMinMaxOutputAmount(min, max);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setNbt(String json) {
        builder.nbt(json);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder nbt(String json) {
        return setNbt(json);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setNBT(String json) {
        builder.nbt(json);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setDisplayNbt(String json) {
        builder.displayNbt(json);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder displayNbt(String json) {
        return setDisplayNbt(json);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setDisplayNBT(String json) {
        builder.displayNbt(json);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setPreViewNBT(String json) {
        builder.previewNbt(json);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setPreViewNBT(IData data) {
        if (data != null) {
            builder.previewNbt(data.accept(DataToJsonStringVisitor.INSTANCE));
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder previewNbt(String json) {
        builder.previewNbt(json);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setPreviewNbt(String json) {
        builder.previewNbt(json);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setPreviewNBT(String json) {
        builder.previewNbt(json);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setTriggerTime(int tickTime) {
        builder.triggerTime(tickTime);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder triggerTime(int tickTime) {
        return setTriggerTime(tickTime);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setTriggerRepeatable(boolean repeatable) {
        builder.triggerRepeatable(repeatable);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder triggerRepeatable(boolean repeatable) {
        return setTriggerRepeatable(repeatable);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setIgnoreOutputCheck(boolean ignore) {
        builder.ignoreOutputCheck(ignore);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder ignoreOutputCheck(boolean ignore) {
        return setIgnoreOutputCheck(ignore);
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder setParallelizeUnaffected(boolean unaffected) {
        builder.parallelizeUnaffected(unaffected);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeAdapterBuilder parallelizeUnaffected(boolean unaffected) {
        return setParallelizeUnaffected(unaffected);
    }

    @ZenCodeType.Method
    public void build() {
        builder.build();
    }

    private MmceCTRecipeAdapterBuilder item(String ioType, IItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return this;
        }
        if ("input".equals(ioType)) {
            builder.itemInput(MmceCTRecipeBuilder.itemId(stack), MmceCTRecipeBuilder.itemAmount(stack));
        } else {
            builder.itemOutput(MmceCTRecipeBuilder.itemId(stack), MmceCTRecipeBuilder.itemAmount(stack));
        }
        var nbt = MmceCTRecipeBuilder.itemStackNbt(stack.getInternal());
        if (!nbt.isEmpty()) {
            builder.nbt(nbt.toString());
        }
        return this;
    }

    private MmceCTRecipeAdapterBuilder items(String ioType, IItemStack... stacks) {
        if (stacks != null) {
            for (IItemStack stack : stacks) {
                item(ioType, stack);
            }
        }
        return this;
    }

    private MmceCTRecipeAdapterBuilder ingredient(String ioType, IIngredient ingredient, int amountOverride) {
        if (ingredient == null || ingredient.isEmpty()) {
            return this;
        }
        if (ingredient instanceof IItemStack stack) {
            return item(ioType, amountOverride > 0 ? stack.withAmount(amountOverride) : stack);
        }
        MmceCTIngredientArrayPrimer primer = new MmceCTIngredientArrayPrimer();
        primer.addIngredient(ingredient, amountOverride > 0 ? amountOverride : 1);
        if ("input".equals(ioType)) {
            builder.ingredientArrayInput(primer);
        } else {
            builder.randomItemOutput(primer);
        }
        return this;
    }

    private MmceCTRecipeAdapterBuilder ingredient(String ioType, IIngredientWithAmount ingredient) {
        if (ingredient == null || ingredient.ingredient() == null) {
            return this;
        }
        return ingredient(ioType, ingredient.ingredient(), ingredient.amount());
    }

    private MmceCTRecipeAdapterBuilder ingredients(String ioType, IIngredient... ingredients) {
        if (ingredients != null) {
            for (IIngredient ingredient : ingredients) {
                ingredient(ioType, ingredient, 0);
            }
        }
        return this;
    }

    private MmceCTRecipeAdapterBuilder ingredients(String ioType, IIngredientWithAmount... ingredients) {
        if (ingredients != null) {
            for (IIngredientWithAmount ingredient : ingredients) {
                ingredient(ioType, ingredient);
            }
        }
        return this;
    }

    private MmceCTRecipeAdapterBuilder fluid(String ioType, IFluidStack stack, boolean perTick) {
        if (stack == null || stack.isEmpty()) {
            return this;
        }
        int amount = MmceCTRecipeBuilder.safeFluidAmount(stack);
        String fluid = stack.getRegistryName().toString();
        if ("input".equals(ioType)) {
            if (perTick) {
                builder.fluidPerTickInput(fluid, amount);
            } else {
                builder.fluidInput(fluid, amount);
            }
        } else if (perTick) {
            builder.fluidPerTickOutput(fluid, amount);
        } else {
            builder.fluidOutput(fluid, amount);
        }
        var nbt = MmceCTRecipeBuilder.fluidStackNbt(stack.getInternal());
        if (!nbt.isEmpty()) {
            builder.nbt(nbt.toString());
        }
        return this;
    }

    private MmceCTRecipeAdapterBuilder fluids(String ioType, boolean perTick, IFluidStack... stacks) {
        if (stacks != null) {
            for (IFluidStack stack : stacks) {
                fluid(ioType, stack, perTick);
            }
        }
        return this;
    }
}
