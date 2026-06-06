package hellfirepvp.modularmachinery.port.integration;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.DynamicMachineUpgradeBuilder")
public final class MmceDynamicMachineUpgradeBuilder {
    private final MmceMachineUpgradeBuilder delegate;

    private MmceDynamicMachineUpgradeBuilder(String name, String localizedName, float level, int maxStack) {
        this.delegate = MmceMachineUpgradeBuilder.newBuilder(name, localizedName, level, maxStack);
    }

    public static MmceDynamicMachineUpgradeBuilder newBuilder(String name, String localizedName, float level, int maxStack) {
        return new MmceDynamicMachineUpgradeBuilder(name, localizedName, level, maxStack);
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addDescriptions(String... descriptions) {
        delegate.addDescriptions(descriptions);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder setDescriptionHandler(MmceFunction<MmceMachineUpgrade, String[]> handler) {
        MmceMachineUpgradeRegistry.registerDescriptionHandler(getName(), handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder setBusGUIDescriptionHandler(MmceFunction<MmceMachineUpgrade, String[]> handler) {
        MmceMachineUpgradeRegistry.registerBusGuiDescriptionHandler(getName(), handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addCompatibleMachines(String... machineNames) {
        delegate.addCompatibleMachines(machineNames);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addIncompatibleMachines(String... machineNames) {
        delegate.addIncompatibleMachines(machineNames);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addModifier(boolean stackable, String modifierKey, MmceRecipeModifier modifier) {
        delegate.addModifier(stackable, modifierKey, modifier);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addModifier(MmceRecipeModifier modifier) {
        delegate.addModifier(modifier);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addModifier(MmceRecipeModifier modifier, boolean stackable) {
        delegate.addModifier(modifier, stackable);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder itemInputModifier(int operation, double multiplier) {
        delegate.itemInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder itemInputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.itemInputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder itemOutputModifier(int operation, double multiplier) {
        delegate.itemOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder itemOutputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.itemOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder fluidInputModifier(int operation, double multiplier) {
        delegate.fluidInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder fluidInputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.fluidInputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder fluidOutputModifier(int operation, double multiplier) {
        delegate.fluidOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder fluidOutputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.fluidOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder gasInputModifier(int operation, double multiplier) {
        delegate.gasInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder gasInputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.gasInputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder gasOutputModifier(int operation, double multiplier) {
        delegate.gasOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder gasOutputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.gasOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder chemicalInputModifier(int operation, double multiplier) {
        delegate.chemicalInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder chemicalInputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.chemicalInputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder chemicalOutputModifier(int operation, double multiplier) {
        delegate.chemicalOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder chemicalOutputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.chemicalOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder energyInputModifier(int operation, double multiplier) {
        delegate.energyInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder energyInputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.energyInputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder energyOutputModifier(int operation, double multiplier) {
        delegate.energyOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder energyOutputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.energyOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder durationModifier(int operation, double multiplier) {
        delegate.durationModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder durationModifier(int operation, double multiplier, boolean affectChance) {
        delegate.durationModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addRecipeCheckHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipeCheckHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addPreRecipeCheckHandler(MmceUpgradeEventHandler handler) {
        delegate.addPreRecipeCheckHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addPostRecipeCheckHandler(MmceUpgradeEventHandler handler) {
        delegate.addPostRecipeCheckHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addRecipeStartHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipeStartHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addRecipePreTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipePreTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addRecipePostTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipePostTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addRecipeTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipeTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addRecipeFailureHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipeFailureHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addRecipeFinishHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipeFinishHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addFactoryRecipeStartHandler(MmceUpgradeEventHandler handler) {
        delegate.addFactoryRecipeStartHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addFactoryRecipePreTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addFactoryRecipePreTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addFactoryRecipePostTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addFactoryRecipePostTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addFactoryRecipeFailureHandler(MmceUpgradeEventHandler handler) {
        delegate.addFactoryRecipeFailureHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addFactoryRecipeFinishHandler(MmceUpgradeEventHandler handler) {
        delegate.addFactoryRecipeFinishHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addMachinePreTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addMachinePreTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addMachinePostTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addMachinePostTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addMachineTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addMachineTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addStructureFormedHandler(MmceUpgradeEventHandler handler) {
        delegate.addStructureFormedHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addStructureUpdateHandler(MmceUpgradeEventHandler handler) {
        delegate.addStructureUpdateHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addControllerGUIRenderHandler(MmceUpgradeEventHandler handler) {
        delegate.addControllerGUIRenderHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addGUIRenderHandler(MmceUpgradeEventHandler handler) {
        delegate.addGUIRenderHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addControllerButtonClickHandler(MmceUpgradeEventHandler handler) {
        delegate.addControllerButtonClickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceDynamicMachineUpgradeBuilder addSmartInterfaceUpdateHandler(MmceUpgradeEventHandler handler) {
        delegate.addSmartInterfaceUpdateHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public ItemStack build() {
        return delegate.build();
    }

    @ZenCodeType.Method
    public ItemStack applyTo(ItemStack stack) {
        return delegate.applyTo(stack);
    }

    @ZenCodeType.Method
    public String customDataSnbt() {
        return delegate.customDataSnbt();
    }

    @ZenCodeType.Method
    public void buildAndRegister() {
        delegate.buildAndRegister();
    }

    @ZenCodeType.Method
    public String getName() {
        return delegate.getName();
    }

    @ZenCodeType.Method
    public String getLocalizedName() {
        return delegate.getLocalizedName();
    }

    @ZenCodeType.Method
    public float getLevel() {
        return delegate.getLevel();
    }

    @ZenCodeType.Method
    public int getMaxStack() {
        return delegate.getMaxStack();
    }

}
