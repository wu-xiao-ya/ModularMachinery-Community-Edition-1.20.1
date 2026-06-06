package hellfirepvp.modularmachinery.port.recipe;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.blockentity.UpgradeBusBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceIoType;
import hellfirepvp.modularmachinery.port.data.MmceMachineModifierDefinition;
import hellfirepvp.modularmachinery.port.data.MmceParsedRequirement;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeHelper;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

final class MmceRecipeModifiers {
    private static final ResourceLocation ITEM = target("item");
    private static final ResourceLocation FLUID = target("fluid");
    private static final ResourceLocation GAS = target("gas");
    private static final ResourceLocation CHEMICAL = target("chemical");
    private static final ResourceLocation ENERGY = target("energy");
    private static final ResourceLocation DURATION = target("duration");

    private final List<MmceMachineModifierDefinition> modifiers;

    private MmceRecipeModifiers(List<MmceMachineModifierDefinition> modifiers) {
        this.modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
    }

    static MmceRecipeModifiers of(List<MmceMachineModifierDefinition> modifiers) {
        return new MmceRecipeModifiers(modifiers);
    }

    static MmceRecipeModifiers of(List<MmceMachineModifierDefinition> structureModifiers, List<UpgradeBusBlockEntity> upgradeBuses) {
        return of(structureModifiers, upgradeBuses, null);
    }

    static MmceRecipeModifiers of(List<MmceMachineModifierDefinition> structureModifiers, List<UpgradeBusBlockEntity> upgradeBuses, ResourceLocation machineId) {
        List<MmceMachineModifierDefinition> combined = new ArrayList<>();
        if (structureModifiers != null) {
            combined.addAll(structureModifiers);
        }
        addUpgradeModifiers(combined, upgradeBuses, machineId);
        return new MmceRecipeModifiers(combined);
    }

    MmceRecipeModifiers withAdditional(List<MmceMachineModifierDefinition> additional) {
        if (additional == null || additional.isEmpty()) {
            return this;
        }
        List<MmceMachineModifierDefinition> combined = new ArrayList<>(modifiers.size() + additional.size());
        combined.addAll(modifiers);
        combined.addAll(additional);
        return new MmceRecipeModifiers(combined);
    }

    int recipeTime(int recipeTime) {
        return applyInt(DURATION, Optional.of(MmceIoType.INPUT), recipeTime, false);
    }

    int itemAmount(MmceParsedRequirement requirement, int amount) {
        return applyInt(ITEM, Optional.of(requirement.ioType()), amount, false);
    }

    int fluidAmount(MmceParsedRequirement requirement, int amount) {
        return applyInt(FLUID, Optional.of(requirement.ioType()), amount, false);
    }

    int gasAmount(MmceParsedRequirement requirement, int amount) {
        return applyInt(List.of(GAS, CHEMICAL), Optional.of(requirement.ioType()), amount, false);
    }

    long energyAmount(MmceParsedRequirement requirement, long amount) {
        return applyLong(ENERGY, Optional.of(requirement.ioType()), amount, false);
    }

    float chance(MmceParsedRequirement requirement, float chance) {
        double applied = requirement instanceof hellfirepvp.modularmachinery.port.data.MmceChemicalRequirement
                ? apply(List.of(GAS, CHEMICAL), Optional.of(requirement.ioType()), chance, true)
                : apply(targetFor(requirement), Optional.of(requirement.ioType()), chance, true);
        return (float) Math.max(0.0D, Math.min(1.0D, applied));
    }

    private int applyInt(ResourceLocation target, Optional<MmceIoType> ioType, int value, boolean affectChance) {
        return Math.max(0, clampToInt(Math.round(apply(target, ioType, value, affectChance))));
    }

    private int applyInt(List<ResourceLocation> targets, Optional<MmceIoType> ioType, int value, boolean affectChance) {
        return Math.max(0, clampToInt(Math.round(apply(targets, ioType, value, affectChance))));
    }

    private long applyLong(ResourceLocation target, Optional<MmceIoType> ioType, long value, boolean affectChance) {
        double applied = apply(target, ioType, value, affectChance);
        if (applied <= 0.0D) {
            return 0L;
        }
        if (applied >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.round(applied);
    }

    private double apply(ResourceLocation target, Optional<MmceIoType> ioType, double value, boolean affectChance) {
        return apply(List.of(target), ioType, value, affectChance);
    }

    private double apply(List<ResourceLocation> targets, Optional<MmceIoType> ioType, double value, boolean affectChance) {
        double add = 0.0D;
        double multiply = 1.0D;
        for (MmceMachineModifierDefinition modifier : modifiers) {
            if (!targets.contains(modifier.target()) || modifier.affectChance() != affectChance) {
                continue;
            }
            if (modifier.ioType().isPresent() && ioType.isPresent() && modifier.ioType().get() != ioType.get()) {
                continue;
            }
            if (modifier.operation() == 0) {
                add += modifier.multiplier();
            } else if (modifier.operation() == 1) {
                multiply *= modifier.multiplier();
            }
        }
        return (value + add) * multiply;
    }

    private static ResourceLocation targetFor(MmceParsedRequirement requirement) {
        return switch (requirement) {
            case hellfirepvp.modularmachinery.port.data.MmceFluidRequirement ignored -> FLUID;
            case hellfirepvp.modularmachinery.port.data.MmceChemicalRequirement ignored -> GAS;
            case hellfirepvp.modularmachinery.port.data.MmceEnergyRequirement ignored -> ENERGY;
            default -> ITEM;
        };
    }

    private static int clampToInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    private static ResourceLocation target(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, path);
    }

    private static void addUpgradeModifiers(List<MmceMachineModifierDefinition> output, List<UpgradeBusBlockEntity> upgradeBuses, ResourceLocation machineId) {
        if (upgradeBuses == null || upgradeBuses.isEmpty()) {
            return;
        }

        Map<String, List<MmceMachineModifierDefinition>> keyedModifiers = new LinkedHashMap<>();
        for (UpgradeBusBlockEntity bus : upgradeBuses) {
            for (int slot = 0; slot < bus.getContainerSize(); slot++) {
                ItemStack stack = bus.getItem(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                addUpgradeStackModifiers(output, keyedModifiers, stack, machineId);
            }
        }
        keyedModifiers.values().forEach(output::addAll);
    }

    private static void addUpgradeStackModifiers(List<MmceMachineModifierDefinition> output, Map<String, List<MmceMachineModifierDefinition>> keyedModifiers,
                                                 ItemStack stack, ResourceLocation machineId) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.isEmpty()) {
            return;
        }
        if (!isCompatible(tag, machineId)) {
            return;
        }

        addModifierList(output, keyedModifiers, stack, tag, "mmce_modifiers");
        for (CompoundTag upgrade : MmceMachineUpgradeHelper.fixedUpgrades(stack)) {
            if (isCompatible(upgrade, machineId)) {
                addReferencedUpgradeModifiers(output, keyedModifiers, stack, upgrade, machineId);
            }
        }
        if (tag.contains("mmce_upgrade", Tag.TAG_COMPOUND)) {
            CompoundTag upgrade = tag.getCompound("mmce_upgrade");
            if (isCompatible(upgrade, machineId)) {
                addModifierList(output, keyedModifiers, stack, upgrade, "modifiers");
            }
            if (!upgrade.getBoolean("inline")
                    && upgrade.contains("name", Tag.TAG_STRING)
                    && isCompatible(upgrade, machineId)) {
                MmceMachineUpgradeRegistry.get(upgrade.getString("name"))
                        .filter(registered -> isCompatible(registered, machineId))
                        .ifPresent(registered -> addModifierList(output, keyedModifiers, stack, registered, "mmce_modifiers"));
            }
        }
    }

    private static void addReferencedUpgradeModifiers(List<MmceMachineModifierDefinition> output, Map<String, List<MmceMachineModifierDefinition>> keyedModifiers,
                                                      ItemStack stack, CompoundTag holder, ResourceLocation machineId) {
        addModifierList(output, keyedModifiers, stack, holder, "mmce_modifiers");
        CompoundTag upgrade = upgradeInfo(holder);
        if (upgrade.contains("name", Tag.TAG_STRING) && isCompatible(upgrade, machineId)) {
            MmceMachineUpgradeRegistry.get(upgrade.getString("name"))
                    .filter(registered -> isCompatible(registered, machineId))
                    .ifPresent(registered -> addModifierList(output, keyedModifiers, stack, registered, "mmce_modifiers"));
        }
    }

    private static void addModifierList(List<MmceMachineModifierDefinition> output, Map<String, List<MmceMachineModifierDefinition>> keyedModifiers,
                                        ItemStack stack, CompoundTag holder, String key) {
        if (!holder.contains(key, Tag.TAG_LIST)) {
            return;
        }

        ListTag modifiers = holder.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < modifiers.size(); i++) {
            Optional<MmceMachineModifierDefinition> modifier = readUpgradeModifier(modifiers.getCompound(i));
            if (modifier.isEmpty()) {
                continue;
            }

            int copies = isStackable(modifiers.getCompound(i)) ? stack.getCount() : 1;
            List<MmceMachineModifierDefinition> expanded = new ArrayList<>(Math.max(1, copies));
            for (int copy = 0; copy < Math.max(1, copies); copy++) {
                expanded.add(modifier.get());
            }

            String modifierKey = readKey(modifiers.getCompound(i));
            if (modifierKey.isBlank()) {
                output.addAll(expanded);
            } else {
                keyedModifiers.put(modifierKey, expanded);
            }
        }
    }

    private static Optional<MmceMachineModifierDefinition> readUpgradeModifier(CompoundTag tag) {
        if (!tag.contains("target", Tag.TAG_STRING)) {
            return Optional.empty();
        }

        ResourceLocation target = targetId(tag.getString("target"));
        Optional<MmceIoType> ioType = tag.contains("io", Tag.TAG_STRING)
                ? MmceIoType.byName(tag.getString("io"))
                : Optional.empty();
        int operation = tag.contains("operation", Tag.TAG_ANY_NUMERIC) ? tag.getInt("operation") : 0;
        double multiplier = tag.contains("multiplier", Tag.TAG_ANY_NUMERIC) ? tag.getDouble("multiplier") : 1.0D;
        boolean affectChance = tag.getBoolean("affectChance") || tag.getBoolean("affectsChance");

        return Optional.of(new MmceMachineModifierDefinition(
                List.of(0),
                List.of(0),
                List.of(0),
                List.of(),
                Optional.empty(),
                Optional.empty(),
                new JsonObject(),
                new JsonObject(),
                target,
                ioType,
                operation,
                multiplier,
                affectChance,
                new JsonObject()
        ));
    }

    private static boolean isStackable(CompoundTag tag) {
        if (tag.contains("stackable", Tag.TAG_BYTE)) {
            return tag.getBoolean("stackable");
        }
        return !tag.contains("stackAble", Tag.TAG_BYTE) || tag.getBoolean("stackAble");
    }

    private static String readKey(CompoundTag tag) {
        if (tag.contains("key", Tag.TAG_STRING)) {
            return tag.getString("key").trim();
        }
        if (tag.contains("modifierKey", Tag.TAG_STRING)) {
            return tag.getString("modifierKey").trim();
        }
        return "";
    }

    private static boolean isCompatible(CompoundTag tag, ResourceLocation machineId) {
        if (machineId == null) {
            return true;
        }
        if (tag.contains("compatibleMachines", Tag.TAG_LIST)
                && !containsMachine(tag.getList("compatibleMachines", Tag.TAG_STRING), machineId)) {
            return false;
        }
        return !tag.contains("incompatibleMachines", Tag.TAG_LIST)
                || !containsMachine(tag.getList("incompatibleMachines", Tag.TAG_STRING), machineId);
    }

    private static boolean containsMachine(ListTag machines, ResourceLocation machineId) {
        for (int i = 0; i < machines.size(); i++) {
            ResourceLocation candidate = targetId(machines.getString(i));
            if (candidate.equals(machineId)) {
                return true;
            }
        }
        return false;
    }

    private static ResourceLocation targetId(String id) {
        return id.indexOf(':') >= 0 ? ResourceLocation.parse(id) : target(id);
    }

    private static CompoundTag upgradeInfo(CompoundTag holder) {
        return holder.contains("mmce_upgrade", Tag.TAG_COMPOUND)
                ? holder.getCompound("mmce_upgrade")
                : holder;
    }
}
