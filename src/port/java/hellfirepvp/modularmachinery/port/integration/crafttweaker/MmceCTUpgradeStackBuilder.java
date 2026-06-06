package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.integration.MmceUpgradeStackBuilder;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.UpgradeStackBuilder")
public final class MmceCTUpgradeStackBuilder {
    private final MmceUpgradeStackBuilder builder;

    private MmceCTUpgradeStackBuilder(String itemId, int amount) {
        this.builder = MmceUpgradeStackBuilder.of(itemId, amount);
    }

    @ZenCodeType.Method
    public static MmceCTUpgradeStackBuilder newBuilder(String itemId) {
        return newBuilder(itemId, 1);
    }

    @ZenCodeType.Method
    public static MmceCTUpgradeStackBuilder newBuilder(String itemId, int amount) {
        return new MmceCTUpgradeStackBuilder(itemId, amount);
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addModifier(String target, String io, int operation, double multiplier) {
        builder.modifier(target, io, operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addModifier(String target, String io, int operation, double multiplier, boolean affectChance) {
        builder.modifier(target, io, operation, multiplier, affectChance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addModifier(String target, String io, int operation, double multiplier, boolean affectChance, boolean stackable) {
        builder.modifier(target, io, operation, multiplier, affectChance, stackable);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addModifier(MmceRecipeModifier modifier) {
        builder.modifier(modifier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addModifier(MmceRecipeModifier modifier, boolean stackable) {
        builder.modifier(modifier, stackable);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addModifier(boolean stackable, String modifierKey, MmceRecipeModifier modifier) {
        builder.addModifier(stackable, modifierKey, modifier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addItemInputModifier(int operation, double multiplier) {
        builder.itemInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder itemInputModifier(int operation, double multiplier) {
        return addItemInputModifier(operation, multiplier);
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addItemOutputModifier(int operation, double multiplier) {
        builder.itemOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder itemOutputModifier(int operation, double multiplier) {
        return addItemOutputModifier(operation, multiplier);
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addFluidInputModifier(int operation, double multiplier) {
        builder.fluidInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder fluidInputModifier(int operation, double multiplier) {
        return addFluidInputModifier(operation, multiplier);
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addFluidOutputModifier(int operation, double multiplier) {
        builder.fluidOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder fluidOutputModifier(int operation, double multiplier) {
        return addFluidOutputModifier(operation, multiplier);
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addGasInputModifier(int operation, double multiplier) {
        builder.gasInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder gasInputModifier(int operation, double multiplier) {
        return addGasInputModifier(operation, multiplier);
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addGasOutputModifier(int operation, double multiplier) {
        builder.gasOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder gasOutputModifier(int operation, double multiplier) {
        return addGasOutputModifier(operation, multiplier);
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addChemicalInputModifier(int operation, double multiplier) {
        builder.chemicalInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder chemicalInputModifier(int operation, double multiplier) {
        return addChemicalInputModifier(operation, multiplier);
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addChemicalOutputModifier(int operation, double multiplier) {
        builder.chemicalOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder chemicalOutputModifier(int operation, double multiplier) {
        return addChemicalOutputModifier(operation, multiplier);
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addEnergyInputModifier(int operation, double multiplier) {
        builder.energyInputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder energyInputModifier(int operation, double multiplier) {
        return addEnergyInputModifier(operation, multiplier);
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addEnergyOutputModifier(int operation, double multiplier) {
        builder.energyOutputModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder energyOutputModifier(int operation, double multiplier) {
        return addEnergyOutputModifier(operation, multiplier);
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addDurationModifier(int operation, double multiplier) {
        builder.durationModifier(operation, multiplier);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder durationModifier(int operation, double multiplier) {
        return addDurationModifier(operation, multiplier);
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addCompatibleMachines(String... machineNames) {
        builder.compatibleMachines(machineNames);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTUpgradeStackBuilder addIncompatibleMachines(String... machineNames) {
        builder.incompatibleMachines(machineNames);
        return this;
    }

    @ZenCodeType.Method
    public String customDataSnbt() {
        return builder.customDataSnbt();
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
}
