package hellfirepvp.modularmachinery.port.integration;

import net.minecraft.world.item.ItemStack;

public final class MmceDynamicMachineUpgradeBuilder {
    private final MmceMachineUpgradeBuilder delegate;

    private MmceDynamicMachineUpgradeBuilder(String name, String localizedName, float level, int maxStack) {
        this.delegate = MmceMachineUpgradeBuilder.newBuilder(name, localizedName, level, maxStack);
    }

    public static MmceDynamicMachineUpgradeBuilder newBuilder(String name, String localizedName, float level, int maxStack) {
        return new MmceDynamicMachineUpgradeBuilder(name, localizedName, level, maxStack);
    }

    public MmceDynamicMachineUpgradeBuilder addDescriptions(String... descriptions) {
        delegate.addDescriptions(descriptions);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder setDescriptionHandler(MmceFunction<MmceMachineUpgrade, String[]> handler) {
        MmceMachineUpgradeRegistry.registerDescriptionHandler(getName(), handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder setBusGUIDescriptionHandler(MmceFunction<MmceMachineUpgrade, String[]> handler) {
        MmceMachineUpgradeRegistry.registerBusGuiDescriptionHandler(getName(), handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addCompatibleMachines(String... machineNames) {
        delegate.addCompatibleMachines(machineNames);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addIncompatibleMachines(String... machineNames) {
        delegate.addIncompatibleMachines(machineNames);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addModifier(boolean stackable, String modifierKey, MmceRecipeModifier modifier) {
        delegate.addModifier(stackable, modifierKey, modifier);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addModifier(MmceRecipeModifier modifier) {
        delegate.addModifier(modifier);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addModifier(MmceRecipeModifier modifier, boolean stackable) {
        delegate.addModifier(modifier, stackable);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder itemInputModifier(int operation, double multiplier) {
        delegate.itemInputModifier(operation, multiplier);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder itemInputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.itemInputModifier(operation, multiplier, affectChance);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder itemOutputModifier(int operation, double multiplier) {
        delegate.itemOutputModifier(operation, multiplier);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder itemOutputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.itemOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder fluidInputModifier(int operation, double multiplier) {
        delegate.fluidInputModifier(operation, multiplier);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder fluidInputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.fluidInputModifier(operation, multiplier, affectChance);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder fluidOutputModifier(int operation, double multiplier) {
        delegate.fluidOutputModifier(operation, multiplier);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder fluidOutputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.fluidOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder gasInputModifier(int operation, double multiplier) {
        delegate.gasInputModifier(operation, multiplier);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder gasInputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.gasInputModifier(operation, multiplier, affectChance);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder gasOutputModifier(int operation, double multiplier) {
        delegate.gasOutputModifier(operation, multiplier);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder gasOutputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.gasOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder chemicalInputModifier(int operation, double multiplier) {
        delegate.chemicalInputModifier(operation, multiplier);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder chemicalInputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.chemicalInputModifier(operation, multiplier, affectChance);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder chemicalOutputModifier(int operation, double multiplier) {
        delegate.chemicalOutputModifier(operation, multiplier);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder chemicalOutputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.chemicalOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder energyInputModifier(int operation, double multiplier) {
        delegate.energyInputModifier(operation, multiplier);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder energyInputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.energyInputModifier(operation, multiplier, affectChance);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder energyOutputModifier(int operation, double multiplier) {
        delegate.energyOutputModifier(operation, multiplier);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder energyOutputModifier(int operation, double multiplier, boolean affectChance) {
        delegate.energyOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder durationModifier(int operation, double multiplier) {
        delegate.durationModifier(operation, multiplier);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder durationModifier(int operation, double multiplier, boolean affectChance) {
        delegate.durationModifier(operation, multiplier, affectChance);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addRecipeCheckHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipeCheckHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addPreRecipeCheckHandler(MmceUpgradeEventHandler handler) {
        delegate.addPreRecipeCheckHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addPostRecipeCheckHandler(MmceUpgradeEventHandler handler) {
        delegate.addPostRecipeCheckHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addRecipeStartHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipeStartHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addRecipePreTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipePreTickHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addRecipePostTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipePostTickHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addRecipeTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipeTickHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addRecipeFailureHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipeFailureHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addRecipeFinishHandler(MmceUpgradeEventHandler handler) {
        delegate.addRecipeFinishHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addFactoryRecipeStartHandler(MmceUpgradeEventHandler handler) {
        delegate.addFactoryRecipeStartHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addFactoryRecipePreTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addFactoryRecipePreTickHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addFactoryRecipePostTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addFactoryRecipePostTickHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addFactoryRecipeFailureHandler(MmceUpgradeEventHandler handler) {
        delegate.addFactoryRecipeFailureHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addFactoryRecipeFinishHandler(MmceUpgradeEventHandler handler) {
        delegate.addFactoryRecipeFinishHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addMachinePreTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addMachinePreTickHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addMachinePostTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addMachinePostTickHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addMachineTickHandler(MmceUpgradeEventHandler handler) {
        delegate.addMachineTickHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addStructureFormedHandler(MmceUpgradeEventHandler handler) {
        delegate.addStructureFormedHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addStructureUpdateHandler(MmceUpgradeEventHandler handler) {
        delegate.addStructureUpdateHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addControllerGUIRenderHandler(MmceUpgradeEventHandler handler) {
        delegate.addControllerGUIRenderHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addGUIRenderHandler(MmceUpgradeEventHandler handler) {
        delegate.addGUIRenderHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addControllerButtonClickHandler(MmceUpgradeEventHandler handler) {
        delegate.addControllerButtonClickHandler(handler);
        return this;
    }

    public MmceDynamicMachineUpgradeBuilder addSmartInterfaceUpdateHandler(MmceUpgradeEventHandler handler) {
        delegate.addSmartInterfaceUpdateHandler(handler);
        return this;
    }

    public ItemStack build() {
        return delegate.build();
    }

    public ItemStack applyTo(ItemStack stack) {
        return delegate.applyTo(stack);
    }

    public String customDataSnbt() {
        return delegate.customDataSnbt();
    }

    public void buildAndRegister() {
        delegate.buildAndRegister();
    }

    public String getName() {
        return delegate.getName();
    }

    public String getLocalizedName() {
        return delegate.getLocalizedName();
    }

    public float getLevel() {
        return delegate.getLevel();
    }

    public int getMaxStack() {
        return delegate.getMaxStack();
    }

}
