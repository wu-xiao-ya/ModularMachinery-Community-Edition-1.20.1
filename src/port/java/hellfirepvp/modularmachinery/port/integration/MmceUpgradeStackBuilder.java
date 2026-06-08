package hellfirepvp.modularmachinery.port.integration;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public final class MmceUpgradeStackBuilder {
    private final ResourceLocation itemId;
    private final int amount;
    private final ListTag modifiers = new ListTag();
    private final ListTag compatibleMachines = new ListTag();
    private final ListTag incompatibleMachines = new ListTag();
    private final ListTag descriptions = new ListTag();
    private ResourceLocation upgradeId;
    private String localizedName = "";
    private float level;
    private int maxStack = 1;

    public MmceUpgradeStackBuilder(String itemId) {
        this(itemId, 1);
    }

    public MmceUpgradeStackBuilder(String itemId, int amount) {
        this.itemId = itemId(itemId);
        this.amount = Math.max(1, amount);
    }

    public static MmceUpgradeStackBuilder of(String itemId) {
        return new MmceUpgradeStackBuilder(itemId);
    }

    public static MmceUpgradeStackBuilder of(String itemId, int amount) {
        return new MmceUpgradeStackBuilder(itemId, amount);
    }

    public static MmceUpgradeStackBuilder newBuilder(String itemId) {
        return of(itemId);
    }

    public static MmceUpgradeStackBuilder newBuilder(String itemId, int amount) {
        return of(itemId, amount);
    }

    public MmceUpgradeStackBuilder modifier(String target, String io, int operation, double multiplier) {
        return modifier(target, io, operation, multiplier, false, true);
    }

    public MmceUpgradeStackBuilder modifier(String target, String io, int operation, double multiplier, boolean affectChance) {
        return modifier(target, io, operation, multiplier, affectChance, true);
    }

    public MmceUpgradeStackBuilder modifier(String target, String io, int operation, double multiplier, boolean affectChance, boolean stackable) {
        return modifier(new MmceRecipeModifier(target, io, multiplier, operation, affectChance), stackable);
    }

    public MmceUpgradeStackBuilder modifier(MmceRecipeModifier modifier) {
        return modifier(modifier, true);
    }

    public MmceUpgradeStackBuilder modifier(MmceRecipeModifier modifier, boolean stackable) {
        if (modifier != null) {
            modifiers.add(modifier.tag(stackable));
        }
        return this;
    }

    public MmceUpgradeStackBuilder addModifier(String target, String io, int operation, double multiplier) {
        return modifier(target, io, operation, multiplier);
    }

    public MmceUpgradeStackBuilder addModifier(String target, String io, int operation, double multiplier, boolean affectChance) {
        return modifier(target, io, operation, multiplier, affectChance);
    }

    public MmceUpgradeStackBuilder addModifier(String target, String io, int operation, double multiplier, boolean affectChance, boolean stackable) {
        return modifier(target, io, operation, multiplier, affectChance, stackable);
    }

    public MmceUpgradeStackBuilder addModifier(String target, String io, double multiplier, int operation) {
        return modifier(target, io, operation, multiplier);
    }

    public MmceUpgradeStackBuilder addModifier(String target, String io, double multiplier, int operation, boolean affectChance) {
        return modifier(target, io, operation, multiplier, affectChance);
    }

    public MmceUpgradeStackBuilder addModifier(String target, String io, double multiplier, int operation, boolean affectChance, boolean stackable) {
        return modifier(target, io, operation, multiplier, affectChance, stackable);
    }

    public MmceUpgradeStackBuilder addModifier(MmceRecipeModifier modifier) {
        return modifier(modifier);
    }

    public MmceUpgradeStackBuilder addModifier(MmceRecipeModifier modifier, boolean stackable) {
        return modifier(modifier, stackable);
    }

    public MmceUpgradeStackBuilder addModifier(boolean stackable, String modifierKey, MmceRecipeModifier modifier) {
        if (modifier != null) {
            CompoundTag tag = modifier.tag(stackable);
            if (modifierKey != null && !modifierKey.isBlank()) {
                tag.putString("key", modifierKey.trim());
            }
            modifiers.add(tag);
        }
        return this;
    }

    public MmceUpgradeStackBuilder itemInputModifier(int operation, double multiplier) {
        return modifier("item", "input", operation, multiplier);
    }

    public MmceUpgradeStackBuilder addItemInputModifier(int operation, double multiplier) {
        return itemInputModifier(operation, multiplier);
    }

    public MmceUpgradeStackBuilder itemOutputModifier(int operation, double multiplier) {
        return modifier("item", "output", operation, multiplier);
    }

    public MmceUpgradeStackBuilder addItemOutputModifier(int operation, double multiplier) {
        return itemOutputModifier(operation, multiplier);
    }

    public MmceUpgradeStackBuilder fluidInputModifier(int operation, double multiplier) {
        return modifier("fluid", "input", operation, multiplier);
    }

    public MmceUpgradeStackBuilder addFluidInputModifier(int operation, double multiplier) {
        return fluidInputModifier(operation, multiplier);
    }

    public MmceUpgradeStackBuilder fluidOutputModifier(int operation, double multiplier) {
        return modifier("fluid", "output", operation, multiplier);
    }

    public MmceUpgradeStackBuilder addFluidOutputModifier(int operation, double multiplier) {
        return fluidOutputModifier(operation, multiplier);
    }

    public MmceUpgradeStackBuilder gasInputModifier(int operation, double multiplier) {
        return modifier("gas", "input", operation, multiplier);
    }

    public MmceUpgradeStackBuilder addGasInputModifier(int operation, double multiplier) {
        return gasInputModifier(operation, multiplier);
    }

    public MmceUpgradeStackBuilder gasOutputModifier(int operation, double multiplier) {
        return modifier("gas", "output", operation, multiplier);
    }

    public MmceUpgradeStackBuilder addGasOutputModifier(int operation, double multiplier) {
        return gasOutputModifier(operation, multiplier);
    }

    public MmceUpgradeStackBuilder chemicalInputModifier(int operation, double multiplier) {
        return modifier("chemical", "input", operation, multiplier);
    }

    public MmceUpgradeStackBuilder addChemicalInputModifier(int operation, double multiplier) {
        return chemicalInputModifier(operation, multiplier);
    }

    public MmceUpgradeStackBuilder chemicalOutputModifier(int operation, double multiplier) {
        return modifier("chemical", "output", operation, multiplier);
    }

    public MmceUpgradeStackBuilder addChemicalOutputModifier(int operation, double multiplier) {
        return chemicalOutputModifier(operation, multiplier);
    }

    public MmceUpgradeStackBuilder energyInputModifier(int operation, double multiplier) {
        return modifier("energy", "input", operation, multiplier);
    }

    public MmceUpgradeStackBuilder addEnergyInputModifier(int operation, double multiplier) {
        return energyInputModifier(operation, multiplier);
    }

    public MmceUpgradeStackBuilder energyOutputModifier(int operation, double multiplier) {
        return modifier("energy", "output", operation, multiplier);
    }

    public MmceUpgradeStackBuilder addEnergyOutputModifier(int operation, double multiplier) {
        return energyOutputModifier(operation, multiplier);
    }

    public MmceUpgradeStackBuilder durationModifier(int operation, double multiplier) {
        return modifier("duration", "", operation, multiplier);
    }

    public MmceUpgradeStackBuilder addDurationModifier(int operation, double multiplier) {
        return durationModifier(operation, multiplier);
    }

    public MmceUpgradeStackBuilder compatibleMachines(String... machines) {
        addMachines(compatibleMachines, machines);
        return this;
    }

    public MmceUpgradeStackBuilder addCompatibleMachines(String... machines) {
        return compatibleMachines(machines);
    }

    public MmceUpgradeStackBuilder incompatibleMachines(String... machines) {
        addMachines(incompatibleMachines, machines);
        return this;
    }

    public MmceUpgradeStackBuilder addIncompatibleMachines(String... machines) {
        return incompatibleMachines(machines);
    }

    public MmceUpgradeStackBuilder upgrade(String name, String localizedName, float level, int maxStack) {
        this.upgradeId = MmceMachineUpgradeRegistry.upgradeId(name);
        this.localizedName = localizedName == null ? "" : localizedName;
        this.level = level;
        this.maxStack = Math.max(1, maxStack);
        return this;
    }

    public MmceUpgradeStackBuilder setUpgrade(String name, String localizedName, float level, int maxStack) {
        return upgrade(name, localizedName, level, maxStack);
    }

    public MmceUpgradeStackBuilder addUpgrade(String name, String localizedName, float level, int maxStack) {
        return upgrade(name, localizedName, level, maxStack);
    }

    public MmceUpgradeStackBuilder descriptions(String... lines) {
        if (lines == null) {
            return this;
        }
        for (String line : lines) {
            if (line != null && !line.isBlank()) {
                descriptions.add(StringTag.valueOf(line));
            }
        }
        return this;
    }

    public MmceUpgradeStackBuilder addDescriptions(String... lines) {
        return descriptions(lines);
    }

    public MmceUpgradeStackBuilder addDescription(String line) {
        return descriptions(line);
    }

    public CompoundTag customData() {
        return customData(true);
    }

    public CompoundTag registeredData() {
        return customData(false);
    }

    private CompoundTag customData(boolean includeRegisteredReference) {
        CompoundTag tag = new CompoundTag();
        tag.put("mmce_modifiers", modifiers.copy());
        if (!compatibleMachines.isEmpty()) {
            tag.put("compatibleMachines", compatibleMachines.copy());
        }
        if (!incompatibleMachines.isEmpty()) {
            tag.put("incompatibleMachines", incompatibleMachines.copy());
        }
        if (!descriptions.isEmpty()) {
            tag.put("descriptions", descriptions.copy());
        }
        if (upgradeId != null) {
            CompoundTag upgrade = new CompoundTag();
            upgrade.putString("name", upgradeId.toString());
            if (!localizedName.isBlank()) {
                upgrade.putString("localizedName", localizedName);
            }
            upgrade.putFloat("level", level);
            upgrade.putInt("maxStack", maxStack);
            if (includeRegisteredReference) {
                upgrade.putBoolean("inline", true);
            }
            tag.put("mmce_upgrade", upgrade);
        }
        return tag;
    }

    public String customDataSnbt() {
        return customData().toString();
    }

    public ItemStack build() {
        return applyTo(new ItemStack(item(), amount));
    }

    public ItemStack applyTo(ItemStack stack) {
        ItemStack copy = stack.copy();
        CompoundTag tag = customData();
        copy.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        if (upgradeId != null && !localizedName.isBlank()) {
            copy.set(DataComponents.CUSTOM_NAME, Component.literal(localizedName));
        }
        return copy;
    }

    private Item item() {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == Items.AIR && !itemId.equals(BuiltInRegistries.ITEM.getKey(Items.AIR))) {
            throw new IllegalArgumentException("Unknown item id for MMCE upgrade stack: " + itemId);
        }
        return item;
    }

    private static ResourceLocation itemId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("MMCE upgrade stack item id cannot be blank.");
        }
        return id.indexOf(':') >= 0 ? ResourceLocation.parse(id) : ResourceLocation.withDefaultNamespace(id);
    }

    private static void addMachines(ListTag target, String... machines) {
        if (machines == null) {
            return;
        }
        for (String machine : machines) {
            if (machine != null && !machine.isBlank()) {
                target.add(StringTag.valueOf(normalizeMachineId(machine)));
            }
        }
    }

    private static String normalizeMachineId(String machine) {
        return machine.indexOf(':') >= 0
                ? ResourceLocation.parse(machine).toString()
                : ResourceLocation.fromNamespaceAndPath(hellfirepvp.modularmachinery.port.ModularMachineryNeoForge.MODID, machine).toString();
    }
}
