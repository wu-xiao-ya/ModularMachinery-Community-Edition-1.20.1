package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceFunction;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.integration.MmceUpgradeEventHandler;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineUpgradeBuilder")
public final class MmceCTMachineUpgradeBuilder {
    private final MmceMachineUpgradeBuilder builder;

    private MmceCTMachineUpgradeBuilder(String name, String localizedName, float level, int maxStack) {
        this.builder = MmceMachineUpgradeBuilder.newBuilder(name, localizedName, level, maxStack);
    }

    @ZenCodeType.Method
    public static MmceCTMachineUpgradeBuilder newBuilder(String name, String localizedName, float level, int maxStack) {
        return new MmceCTMachineUpgradeBuilder(name, localizedName, level, maxStack);
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addModifier(boolean stackAble, String modifierKey, MmceRecipeModifier modifier) {
        builder.addModifier(stackAble, modifierKey, modifier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addModifier(MmceRecipeModifier modifier) {
        builder.addModifier(modifier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addModifier(MmceRecipeModifier modifier, boolean stackAble) {
        builder.addModifier(modifier, stackAble);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder itemInputModifier(int operation, double multiplier) {
        builder.itemInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder itemInputModifier(int operation, double multiplier, boolean affectChance) {
        builder.itemInputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder itemOutputModifier(int operation, double multiplier) {
        builder.itemOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder itemOutputModifier(int operation, double multiplier, boolean affectChance) {
        builder.itemOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder fluidInputModifier(int operation, double multiplier) {
        builder.fluidInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder fluidInputModifier(int operation, double multiplier, boolean affectChance) {
        builder.fluidInputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder fluidOutputModifier(int operation, double multiplier) {
        builder.fluidOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder fluidOutputModifier(int operation, double multiplier, boolean affectChance) {
        builder.fluidOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder gasInputModifier(int operation, double multiplier) {
        builder.gasInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder gasInputModifier(int operation, double multiplier, boolean affectChance) {
        builder.gasInputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder gasOutputModifier(int operation, double multiplier) {
        builder.gasOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder gasOutputModifier(int operation, double multiplier, boolean affectChance) {
        builder.gasOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder chemicalInputModifier(int operation, double multiplier) {
        builder.chemicalInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder chemicalInputModifier(int operation, double multiplier, boolean affectChance) {
        builder.chemicalInputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder chemicalOutputModifier(int operation, double multiplier) {
        builder.chemicalOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder chemicalOutputModifier(int operation, double multiplier, boolean affectChance) {
        builder.chemicalOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder energyInputModifier(int operation, double multiplier) {
        builder.energyInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder energyInputModifier(int operation, double multiplier, boolean affectChance) {
        builder.energyInputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder energyOutputModifier(int operation, double multiplier) {
        builder.energyOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder energyOutputModifier(int operation, double multiplier, boolean affectChance) {
        builder.energyOutputModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder durationModifier(int operation, double multiplier) {
        builder.durationModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder durationModifier(int operation, double multiplier, boolean affectChance) {
        builder.durationModifier(operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addDescriptions(String... descriptions) {
        builder.addDescriptions(descriptions);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder setBusGUIDescriptionHandler(MmceFunction<MmceCTMachineUpgrade, String[]> handler) {
        builder.setBusGUIDescriptionHandler(upgrade -> handler.apply(MmceCTMachineUpgrade.of(upgrade)));
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addCompatibleMachines(String... machineNames) {
        builder.addCompatibleMachines(machineNames);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addIncompatibleMachines(String... machineNames) {
        builder.addIncompatibleMachines(machineNames);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addRecipeCheckHandler(MmceUpgradeEventHandler handler) {
        builder.addRecipeCheckHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addPreRecipeCheckHandler(MmceUpgradeEventHandler handler) {
        builder.addPreRecipeCheckHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addPostRecipeCheckHandler(MmceUpgradeEventHandler handler) {
        builder.addPostRecipeCheckHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addRecipeStartHandler(MmceUpgradeEventHandler handler) {
        builder.addRecipeStartHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addRecipePreTickHandler(MmceUpgradeEventHandler handler) {
        builder.addRecipePreTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addRecipePostTickHandler(MmceUpgradeEventHandler handler) {
        builder.addRecipePostTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addRecipeTickHandler(MmceUpgradeEventHandler handler) {
        builder.addRecipeTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addRecipeFailureHandler(MmceUpgradeEventHandler handler) {
        builder.addRecipeFailureHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addRecipeFinishHandler(MmceUpgradeEventHandler handler) {
        builder.addRecipeFinishHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addFactoryRecipeStartHandler(MmceUpgradeEventHandler handler) {
        builder.addFactoryRecipeStartHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addFactoryRecipePreTickHandler(MmceUpgradeEventHandler handler) {
        builder.addFactoryRecipePreTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addFactoryRecipePostTickHandler(MmceUpgradeEventHandler handler) {
        builder.addFactoryRecipePostTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addFactoryRecipeFailureHandler(MmceUpgradeEventHandler handler) {
        builder.addFactoryRecipeFailureHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addFactoryRecipeFinishHandler(MmceUpgradeEventHandler handler) {
        builder.addFactoryRecipeFinishHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addMachinePreTickHandler(MmceUpgradeEventHandler handler) {
        builder.addMachinePreTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addMachinePostTickHandler(MmceUpgradeEventHandler handler) {
        builder.addMachinePostTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addMachineTickHandler(MmceUpgradeEventHandler handler) {
        builder.addMachineTickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addStructureFormedHandler(MmceUpgradeEventHandler handler) {
        builder.addStructureFormedHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addStructureUpdateHandler(MmceUpgradeEventHandler handler) {
        builder.addStructureUpdateHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addControllerGUIRenderHandler(MmceUpgradeEventHandler handler) {
        builder.addControllerGUIRenderHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addGUIRenderHandler(MmceUpgradeEventHandler handler) {
        builder.addGUIRenderHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addControllerButtonClickHandler(MmceUpgradeEventHandler handler) {
        builder.addControllerButtonClickHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineUpgradeBuilder addSmartInterfaceUpdateHandler(MmceUpgradeEventHandler handler) {
        builder.addSmartInterfaceUpdateHandler(handler);
        return this;
    }

    @ZenCodeType.Method
    public IItemStack build() {
        return IItemStack.of(builder.build());
    }

    @ZenCodeType.Method
    public IItemStack applyTo(IItemStack stack) {
        ItemStack internal = stack.getInternal();
        return IItemStack.of(builder.applyTo(internal));
    }

    @ZenCodeType.Method
    public String customDataSnbt() {
        return builder.customDataSnbt();
    }

    @ZenCodeType.Method
    public void buildAndRegister() {
        builder.buildAndRegister();
    }

    @ZenCodeType.Method
    public String getName() {
        return builder.getName();
    }

    @ZenCodeType.Method
    public String getLocalizedName() {
        return builder.getLocalizedName();
    }

    @ZenCodeType.Method
    public float getLevel() {
        return builder.getLevel();
    }

    @ZenCodeType.Method
    public int getMaxStack() {
        return builder.getMaxStack();
    }
}
