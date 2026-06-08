package hellfirepvp.modularmachinery.port.integration.kubejs;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgrade;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeHelper;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeRegistry;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class MmceKubeJSRegistryUpgrade {
    private MmceKubeJSRegistryUpgrade() {
    }

    public static void clearAll() {
        MmceMachineUpgradeRegistry.clear();
        MmceMachineUpgradeHelper.clear();
    }

    public static boolean supportsUpgrade(ItemStack stack) {
        return stack != null && MmceMachineUpgradeHelper.supportsUpgrade(stack);
    }

    public static boolean supportsUpgrade(String itemId) {
        return MmceMachineUpgradeHelper.supportsUpgrade(itemId);
    }

    public static void addSupportedItem(ItemStack stack) {
        if (stack != null) {
            MmceMachineUpgradeHelper.registerSupportedItem(stack);
        }
    }

    public static void addSupportedItem(String itemId) {
        MmceMachineUpgradeHelper.registerSupportedItem(itemId);
    }

    public static void registerSupportedItem(ItemStack stack) {
        addSupportedItem(stack);
    }

    public static void registerSupportedItem(String itemId) {
        addSupportedItem(itemId);
    }

    public static void addFixedUpgrade(ItemStack stack, String upgradeName) {
        if (stack == null) {
            return;
        }
        if (!MmceMachineUpgradeHelper.hasRegisteredUpgrade(upgradeName)) {
            ModularMachineryNeoForge.LOGGER.warn("[ModularMachinery] Could not find MachineUpgrade {}!", upgradeName);
            return;
        }
        MmceMachineUpgradeHelper.addFixedUpgrade(stack, upgradeName);
    }

    public static void addFixedUpgrade(ItemStack stack, MmceMachineUpgrade upgrade) {
        if (upgrade != null) {
            addFixedUpgrade(stack, upgrade.getName());
        }
    }

    public static void addFixedUpgrade(String itemId, String upgradeName) {
        if (!MmceMachineUpgradeHelper.hasRegisteredUpgrade(upgradeName)) {
            ModularMachineryNeoForge.LOGGER.warn("[ModularMachinery] Could not find MachineUpgrade {}!", upgradeName);
            return;
        }
        MmceMachineUpgradeHelper.addFixedUpgrade(itemId, upgradeName);
    }

    public static void addFixedUpgrade(String itemId, MmceMachineUpgrade upgrade) {
        if (upgrade != null) {
            addFixedUpgrade(itemId, upgrade.getName());
        }
    }

    public static ItemStack addUpgradeToItemStack(ItemStack stack, String upgradeName) {
        if (stack == null) {
            return ItemStack.EMPTY;
        }
        if (!MmceMachineUpgradeHelper.supportsUpgrade(stack)) {
            ModularMachineryNeoForge.LOGGER.warn("[ModularMachinery] {} does not support upgrade!", stack.getItem());
            return stack;
        }
        return MmceMachineUpgradeHelper.addUpgradeToStack(stack, upgradeName);
    }

    public static ItemStack addUpgradeToItemStack(String itemId, String upgradeName) {
        return MmceMachineUpgradeHelper.addUpgradeToStack(itemId, upgradeName);
    }

    public static void registerUpgrade(String type, MmceMachineUpgrade upgrade) {
        if (type != null && !type.isBlank() && upgrade != null) {
            MmceMachineUpgradeRegistry.register(type, upgrade.data());
        }
    }

    public static MmceMachineUpgrade getUpgrade(String type) {
        return MmceMachineUpgradeRegistry.upgrade(type).orElse(null);
    }

    public static MmceMachineUpgrade[] getItemUpgradeList(ItemStack stack) {
        if (stack == null || !MmceMachineUpgradeHelper.supportsUpgrade(stack)) {
            return new MmceMachineUpgrade[0];
        }
        return fixedItemUpgrades(stack).toArray(MmceMachineUpgrade[]::new);
    }

    public static List<MmceMachineUpgrade> getItemUpgrades(ItemStack stack) {
        return List.copyOf(fixedItemUpgrades(stack));
    }

    private static List<MmceMachineUpgrade> fixedItemUpgrades(ItemStack stack) {
        return MmceMachineUpgradeHelper.fixedUpgrades(stack).stream()
                .map(tag -> tag.getString("name"))
                .map(MmceMachineUpgradeRegistry::upgrade)
                .flatMap(java.util.Optional::stream)
                .toList();
    }
}
