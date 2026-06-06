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
@ZenCodeType.Name("mods.modularmachinery.RegistryUpgrade")
public final class MmceCTRegistryUpgrade {
    private MmceCTRegistryUpgrade() {
    }

    @ZenCodeType.Method
    public static void clearAll() {
        MmceMachineUpgradeRegistry.clear();
        MmceMachineUpgradeHelper.clear();
    }

    @ZenCodeType.Method
    public static boolean supportsUpgrade(IItemStack stack) {
        return stack != null && MmceMachineUpgradeHelper.supportsUpgrade(stack.getInternal());
    }

    @ZenCodeType.Method
    public static void addSupportedItem(IItemStack stack) {
        if (stack != null) {
            MmceMachineUpgradeHelper.registerSupportedItem(stack.getInternal());
        }
    }

    @ZenCodeType.Method
    public static void addFixedUpgrade(IItemStack stack, String upgradeName) {
        if (stack == null) {
            return;
        }
        if (!MmceMachineUpgradeHelper.hasRegisteredUpgrade(upgradeName)) {
            ModularMachineryNeoForge.LOGGER.warn("[ModularMachinery] Could not find MachineUpgrade {}!", upgradeName);
            return;
        }
        MmceMachineUpgradeHelper.addFixedUpgrade(stack.getInternal(), upgradeName);
    }

    @ZenCodeType.Method
    public static void addFixedUpgrade(IItemStack stack, MmceMachineUpgrade upgrade) {
        if (upgrade != null) {
            addFixedUpgrade(stack, upgrade.getName());
        }
    }

    @ZenCodeType.Method
    public static void addFixedUpgrade(IItemStack stack, MmceCTSimpleMachineUpgrade upgrade) {
        if (upgrade != null) {
            addFixedUpgrade(stack, upgrade.unwrap());
        }
    }

    @ZenCodeType.Method
    public static void addFixedUpgrade(IItemStack stack, MmceCTSimpleDynamicMachineUpgrade upgrade) {
        if (upgrade != null) {
            addFixedUpgrade(stack, upgrade.unwrap());
        }
    }

    @ZenCodeType.Method
    public static IItemStack addUpgradeToItemStack(IItemStack stack, String upgradeName) {
        if (stack == null) {
            return IItemStack.empty();
        }
        ItemStack internal = stack.getInternal();
        if (!MmceMachineUpgradeHelper.supportsUpgrade(internal)) {
            ModularMachineryNeoForge.LOGGER.warn("[ModularMachinery] {} does not support upgrade!", internal.getItem());
            return stack;
        }
        return IItemStack.of(MmceMachineUpgradeHelper.addUpgradeToStack(internal, upgradeName));
    }

    @ZenCodeType.Method
    public static void registerUpgrade(String type, MmceMachineUpgrade upgrade) {
        if (type == null || type.isBlank() || upgrade == null) {
            return;
        }
        MmceMachineUpgradeRegistry.register(type, upgrade.data());
    }

    @ZenCodeType.Method
    public static void registerUpgrade(String type, MmceCTSimpleMachineUpgrade upgrade) {
        if (upgrade != null) {
            registerUpgrade(type, upgrade.unwrap());
        }
    }

    @ZenCodeType.Method
    public static void registerUpgrade(String type, MmceCTSimpleDynamicMachineUpgrade upgrade) {
        if (upgrade != null) {
            registerUpgrade(type, upgrade.unwrap());
        }
    }

    @ZenCodeType.Method
    public static MmceMachineUpgrade getUpgrade(String type) {
        return MmceMachineUpgradeRegistry.upgrade(type).orElse(null);
    }

    @ZenCodeType.Method
    public static MmceMachineUpgrade[] getItemUpgradeList(IItemStack stack) {
        if (stack == null || !MmceMachineUpgradeHelper.supportsUpgrade(stack.getInternal())) {
            return new MmceMachineUpgrade[0];
        }
        return MmceMachineUpgradeHelper.fixedUpgrades(stack.getInternal()).stream()
                .map(tag -> tag.getString("name"))
                .map(MmceMachineUpgradeRegistry::upgrade)
                .flatMap(java.util.Optional::stream)
                .toArray(MmceMachineUpgrade[]::new);
    }
}
