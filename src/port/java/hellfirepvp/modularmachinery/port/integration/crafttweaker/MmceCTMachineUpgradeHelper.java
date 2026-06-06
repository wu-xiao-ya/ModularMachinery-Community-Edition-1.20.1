package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgrade;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeHelper;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeRegistry;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineUpgradeHelper")
public final class MmceCTMachineUpgradeHelper {

    @ZenCodeType.Method
    public static void registerSupportedItem(IItemStack itemStack) {
        if (itemStack != null) {
            MmceMachineUpgradeHelper.registerSupportedItem(itemStack.getInternal());
        }
    }

    @ZenCodeType.Method
    public static void registerSupportedUpgradeItem(IItemStack itemStack) {
        registerSupportedItem(itemStack);
    }

    @ZenCodeType.Method
    public static void addFixedUpgrade(IItemStack itemStack, String upgradeName) {
        if (itemStack == null) {
            return;
        }
        if (!MmceMachineUpgradeHelper.hasRegisteredUpgrade(upgradeName)) {
            ModularMachineryNeoForge.LOGGER.warn("[ModularMachinery] Could not find MachineUpgrade {}!", upgradeName);
            return;
        }
        MmceMachineUpgradeHelper.addFixedUpgrade(itemStack.getInternal(), upgradeName);
    }

    @ZenCodeType.Method
    public static IItemStack addUpgradeToIItemStack(IItemStack stack, String upgradeName) {
        if (stack == null) {
            return IItemStack.empty();
        }
        if (!MmceMachineUpgradeHelper.hasRegisteredUpgrade(upgradeName)) {
            ModularMachineryNeoForge.LOGGER.warn("[ModularMachinery] Could not find MachineUpgrade {}!", upgradeName);
            return stack;
        }
        ItemStack internal = stack.getInternal();
        if (!MmceMachineUpgradeHelper.supportsUpgrade(internal)) {
            ModularMachineryNeoForge.LOGGER.warn("[ModularMachinery] {} does not support upgrade!", internal.getItem());
            return stack;
        }
        return IItemStack.of(MmceMachineUpgradeHelper.addUpgradeToStack(internal, upgradeName));
    }

    @ZenCodeType.Method
    public static IItemStack addUpgradeToItemStack(IItemStack stack, String upgradeName) {
        return addUpgradeToIItemStack(stack, upgradeName);
    }

    @ZenCodeType.Method
    public static boolean supportsUpgrade(IItemStack stack) {
        return stack != null && MmceMachineUpgradeHelper.supportsUpgrade(stack.getInternal());
    }

    @ZenCodeType.Method
    public static boolean hasRegisteredUpgrade(String upgradeName) {
        return MmceMachineUpgradeHelper.hasRegisteredUpgrade(upgradeName);
    }

    @ZenCodeType.Method
    public static MmceMachineUpgrade getUpgrade(String upgradeName) {
        return MmceMachineUpgradeRegistry.upgrade(upgradeName).orElse(null);
    }

    @ZenCodeType.Method
    public static MmceCTSimpleMachineUpgrade castToSimpleMachineUpgrade(MmceMachineUpgrade upgrade) {
        return MmceCTSimpleMachineUpgrade.of(upgrade);
    }

    @ZenCodeType.Method
    public static MmceCTSimpleDynamicMachineUpgrade castToSimpleDynamicMachineUpgrade(MmceMachineUpgrade upgrade) {
        return MmceCTSimpleDynamicMachineUpgrade.of(upgrade);
    }

    private MmceCTMachineUpgradeHelper() {
    }
}
