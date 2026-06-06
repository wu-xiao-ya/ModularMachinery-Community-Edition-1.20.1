package hellfirepvp.modularmachinery.port.integration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public final class MmceMachineUpgradeHelper {
    private static final Map<ResourceLocation, List<UpgradeItemRegistration>> SUPPORTED_ITEMS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, List<FixedUpgradeRegistration>> FIXED_UPGRADES = new LinkedHashMap<>();

    public static void registerSupportedItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            SUPPORTED_ITEMS.computeIfAbsent(itemId(stack), ignored -> new ArrayList<>())
                    .add(new UpgradeItemRegistration(stackCustomData(stack)));
        }
    }

    public static void registerSupportedItem(String itemId) {
        registerSupportedItem(stackForItem(itemId));
    }

    public static boolean supportsUpgrade(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = stackCustomData(stack);
        if (isUpgradeStack(tag)) {
            return true;
        }
        List<UpgradeItemRegistration> supported = SUPPORTED_ITEMS.get(itemId(stack));
        return supported != null && supported.stream().anyMatch(entry -> entry.matches(tag));
    }

    public static boolean supportsUpgrade(String itemId) {
        return supportsUpgrade(stackForItem(itemId));
    }

    public static void addFixedUpgrade(ItemStack stack, String upgradeName) {
        if (stack.isEmpty() || upgradeName == null || upgradeName.isBlank()) {
            return;
        }
        registerSupportedItem(stack);
        FIXED_UPGRADES.computeIfAbsent(itemId(stack), ignored -> new ArrayList<>())
                .add(new FixedUpgradeRegistration(stackCustomData(stack), fixedUpgradeTag(upgradeName)));
    }

    public static void addFixedUpgrade(String itemId, String upgradeName) {
        addFixedUpgrade(stackForItem(itemId), upgradeName);
    }

    public static ItemStack addUpgradeToStack(ItemStack stack, String upgradeName) {
        if (stack.isEmpty() || upgradeName == null || upgradeName.isBlank() || !supportsUpgrade(stack)) {
            return stack;
        }
        ItemStack copy = stack.copy();
        CompoundTag tag = stackCustomData(copy);
        tag.put("mmce_upgrade", fixedUpgradeTag(upgradeName));
        copy.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return copy;
    }

    public static ItemStack addUpgradeToStack(String itemId, String upgradeName) {
        return addUpgradeToStack(stackForItem(itemId), upgradeName);
    }

    public static Optional<CompoundTag> fixedUpgrade(ItemStack stack) {
        return fixedUpgrades(stack).stream().findFirst();
    }

    public static List<CompoundTag> fixedUpgrades(ItemStack stack) {
        if (stack.isEmpty()) {
            return List.of();
        }
        List<FixedUpgradeRegistration> upgrades = FIXED_UPGRADES.get(itemId(stack));
        if (upgrades == null || upgrades.isEmpty()) {
            return List.of();
        }
        CompoundTag tag = stackCustomData(stack);
        List<CompoundTag> matches = new ArrayList<>();
        for (FixedUpgradeRegistration upgrade : upgrades) {
            if (upgrade.matches(tag)) {
                matches.add(upgrade.upgradeData());
            }
        }
        return List.copyOf(matches);
    }

    public static boolean hasRegisteredUpgrade(String upgradeName) {
        return MmceMachineUpgradeRegistry.get(upgradeName).isPresent();
    }

    public static void clear() {
        SUPPORTED_ITEMS.clear();
        FIXED_UPGRADES.clear();
    }

    public static CompoundTag stackCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static boolean isUpgradeStack(CompoundTag tag) {
        return tag.contains("mmce_modifiers", Tag.TAG_LIST) || tag.contains("mmce_upgrade", Tag.TAG_COMPOUND);
    }

    private static boolean matchesSupportedData(CompoundTag supported, CompoundTag actual) {
        return supported.isEmpty() || supported.equals(actual);
    }

    private record UpgradeItemRegistration(CompoundTag matchData) {
        private UpgradeItemRegistration {
            matchData = matchData == null ? new CompoundTag() : matchData.copy();
        }

        boolean matches(CompoundTag actual) {
            return matchesSupportedData(matchData, actual);
        }
    }

    private record FixedUpgradeRegistration(CompoundTag matchData, CompoundTag upgradeData) {
        private FixedUpgradeRegistration {
            matchData = matchData == null ? new CompoundTag() : matchData.copy();
            upgradeData = upgradeData == null ? new CompoundTag() : upgradeData.copy();
        }

        boolean matches(CompoundTag actual) {
            return matchesSupportedData(matchData, actual);
        }

        public CompoundTag upgradeData() {
            return upgradeData.copy();
        }
    }

    private static CompoundTag fixedUpgradeTag(String upgradeName) {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", MmceMachineUpgradeRegistry.upgradeId(upgradeName).toString());
        return tag;
    }

    private static ResourceLocation itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private static ItemStack stackForItem(String itemId) {
        ResourceLocation id = parseItemId(itemId);
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR && !id.equals(BuiltInRegistries.ITEM.getKey(Items.AIR))) {
            throw new IllegalArgumentException("Unknown item id for MMCE upgrade item: " + itemId);
        }
        return new ItemStack(item);
    }

    private static ResourceLocation parseItemId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("MMCE upgrade item id cannot be blank.");
        }
        return id.indexOf(':') >= 0 ? ResourceLocation.parse(id) : ResourceLocation.withDefaultNamespace(id);
    }

    private MmceMachineUpgradeHelper() {
    }
}
