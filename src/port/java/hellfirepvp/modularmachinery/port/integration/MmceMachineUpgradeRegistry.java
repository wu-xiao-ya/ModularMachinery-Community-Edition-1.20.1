package hellfirepvp.modularmachinery.port.integration;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.UpgradeBusBlockEntity;
import hellfirepvp.modularmachinery.port.event.MmceMachineEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineEventType;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEventType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public final class MmceMachineUpgradeRegistry {
    private static final Map<ResourceLocation, CompoundTag> UPGRADES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, EnumMap<MmceMachineEventType, List<MmceUpgradeEventHandler>>> MACHINE_HANDLERS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, EnumMap<MmceRecipeEventType, List<MmceUpgradeEventHandler>>> RECIPE_HANDLERS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, MmceFunction<MmceMachineUpgrade, String[]>> DESCRIPTION_HANDLERS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, MmceFunction<MmceMachineUpgrade, String[]>> BUS_GUI_DESCRIPTION_HANDLERS = new LinkedHashMap<>();

    public static void register(String name, CompoundTag data) {
        UPGRADES.put(upgradeId(name), data == null ? new CompoundTag() : data.copy());
    }

    public static Optional<CompoundTag> get(String name) {
        CompoundTag data = UPGRADES.get(upgradeId(name));
        return data == null ? Optional.empty() : Optional.of(data.copy());
    }

    public static int count() {
        return UPGRADES.size();
    }

    public static Optional<MmceMachineUpgrade> upgrade(String name) {
        ResourceLocation id = upgradeId(name);
        CompoundTag data = UPGRADES.get(id);
        return data == null ? Optional.empty() : Optional.of(MmceMachineUpgrade.registered(id, data));
    }

    public static void registerMachineHandler(String name, MmceMachineEventType type, MmceUpgradeEventHandler handler) {
        ResourceLocation id = upgradeId(name);
        if (type == null || handler == null) {
            return;
        }
        MACHINE_HANDLERS
                .computeIfAbsent(id, ignored -> new EnumMap<>(MmceMachineEventType.class))
                .computeIfAbsent(type, ignored -> new ArrayList<>())
                .add(handler);
    }

    public static void registerRecipeHandler(String name, MmceRecipeEventType type, MmceUpgradeEventHandler handler) {
        ResourceLocation id = upgradeId(name);
        if (type == null || handler == null) {
            return;
        }
        RECIPE_HANDLERS
                .computeIfAbsent(id, ignored -> new EnumMap<>(MmceRecipeEventType.class))
                .computeIfAbsent(type, ignored -> new ArrayList<>())
                .add(handler);
    }

    public static void registerDescriptionHandler(String name, MmceFunction<MmceMachineUpgrade, String[]> handler) {
        if (handler != null) {
            DESCRIPTION_HANDLERS.put(upgradeId(name), handler);
        }
    }

    public static void registerBusGuiDescriptionHandler(String name, MmceFunction<MmceMachineUpgrade, String[]> handler) {
        if (handler != null) {
            BUS_GUI_DESCRIPTION_HANDLERS.put(upgradeId(name), handler);
        }
    }

    static List<String> descriptions(MmceMachineUpgrade upgrade, boolean busGui, List<String> fallback) {
        MmceFunction<MmceMachineUpgrade, String[]> handler = (busGui ? BUS_GUI_DESCRIPTION_HANDLERS : DESCRIPTION_HANDLERS).get(upgrade.id());
        if (handler == null) {
            return fallback == null ? List.of() : fallback;
        }
        try {
            String[] values = handler.apply(upgrade);
            if (values == null) {
                return fallback == null ? List.of() : fallback;
            }
            List<String> out = new ArrayList<>(values.length);
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    out.add(value);
                }
            }
            return out;
        } catch (Exception exception) {
            ModularMachineryNeoForge.LOGGER.warn("Caught an exception while building MMCE upgrade description {}", upgrade.getName(), exception);
            return fallback == null ? List.of() : fallback;
        }
    }

    public static <E extends MmceMachineEvent> E postMachine(E event) {
        if (event == null || event.isCanceled()) {
            return event;
        }
        for (MmceMachineUpgrade upgrade : installedUpgrades(event.getController(), event.getMachineResourceLocation())) {
            List<MmceUpgradeEventHandler> handlers = MACHINE_HANDLERS
                    .getOrDefault(upgrade.id(), new EnumMap<>(MmceMachineEventType.class))
                    .getOrDefault(event.getEventType(), List.of());
            if (handlers.isEmpty()) {
                continue;
            }
            upgrade.loadRuntimeData();
            for (MmceUpgradeEventHandler handler : List.copyOf(handlers)) {
                handle(event, upgrade, handler);
                if (event.isCanceled()) {
                    upgrade.writeBack();
                    return event;
                }
            }
            upgrade.writeBack();
        }
        return event;
    }

    public static <E extends MmceRecipeEvent> E postRecipe(E event) {
        if (event == null || event.isCanceled()) {
            return event;
        }
        for (MmceMachineUpgrade upgrade : installedUpgrades(event.getController(), event.getMachineResourceLocation())) {
            List<MmceUpgradeEventHandler> handlers = RECIPE_HANDLERS
                    .getOrDefault(upgrade.id(), new EnumMap<>(MmceRecipeEventType.class))
                    .getOrDefault(event.getRecipeEventType(), List.of());
            if (handlers.isEmpty()) {
                continue;
            }
            upgrade.loadRuntimeData();
            for (MmceUpgradeEventHandler handler : List.copyOf(handlers)) {
                handle(event, upgrade, handler);
                if (event.isCanceled()) {
                    upgrade.writeBack();
                    return event;
                }
            }
            upgrade.writeBack();
        }
        return event;
    }

    public static List<MmceMachineUpgrade> installedUpgrades(MachineControllerBlockEntity controller) {
        ResourceLocation machineId = controller == null ? null : controller.getMachineId().orElse(null);
        return installedUpgrades(controller, machineId);
    }

    public static List<MmceMachineUpgrade> installedUpgrades(UpgradeBusBlockEntity bus) {
        return installedUpgrades(bus, null);
    }

    public static List<MmceMachineUpgrade> installedUpgrades(UpgradeBusBlockEntity bus, ResourceLocation machineId) {
        if (bus == null) {
            return List.of();
        }
        List<MmceMachineUpgrade> upgrades = new ArrayList<>();
        for (int slot = 0; slot < bus.getContainerSize(); slot++) {
            addStackUpgrades(upgrades, bus, slot, bus.getItem(slot), machineId);
        }
        upgrades.forEach(MmceMachineUpgrade::loadRuntimeData);
        return upgrades;
    }

    public static Optional<MmceMachineUpgrade> installedUpgrade(MachineControllerBlockEntity controller, String upgradeName) {
        if (upgradeName == null || upgradeName.isBlank()) {
            return Optional.empty();
        }
        ResourceLocation upgradeId = upgradeId(upgradeName);
        return installedUpgrades(controller).stream()
                .filter(upgrade -> upgrade.id().equals(upgradeId))
                .findFirst();
    }

    public static boolean hasInstalledUpgrade(MachineControllerBlockEntity controller, String upgradeName) {
        return installedUpgrade(controller, upgradeName).isPresent();
    }

    public static void clear() {
        UPGRADES.clear();
        MACHINE_HANDLERS.clear();
        RECIPE_HANDLERS.clear();
        DESCRIPTION_HANDLERS.clear();
        BUS_GUI_DESCRIPTION_HANDLERS.clear();
    }

    public static ResourceLocation upgradeId(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("MMCE machine upgrade name cannot be blank.");
        }
        String normalized = name.trim();
        return normalized.indexOf(':') >= 0
                ? ResourceLocation.parse(normalized)
                : ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, normalized);
    }

    private MmceMachineUpgradeRegistry() {
    }

    private static void handle(MmceMachineEvent event, MmceMachineUpgrade upgrade, MmceUpgradeEventHandler handler) {
        try {
            handler.handle(event, upgrade);
        } catch (Exception exception) {
            ModularMachineryNeoForge.LOGGER.warn("Caught an exception while handling MMCE machine upgrade event {}", event.getType(), exception);
        }
    }

    private static List<MmceMachineUpgrade> installedUpgrades(MachineControllerBlockEntity controller, ResourceLocation machineId) {
        if (controller == null) {
            return List.of();
        }
        Level level = controller.getLevel();
        if (level == null) {
            return List.of();
        }

        List<MmceMachineUpgrade> upgrades = new ArrayList<>();
        for (BlockPos pos : controller.getComponentPositions()) {
            if (!(level.getBlockEntity(pos) instanceof UpgradeBusBlockEntity bus)) {
                continue;
            }
            upgrades.addAll(installedUpgrades(bus, machineId));
        }
        return upgrades;
    }

    private static void addStackUpgrades(List<MmceMachineUpgrade> output, UpgradeBusBlockEntity bus, int slot, ItemStack stack, ResourceLocation machineId) {
        if (stack.isEmpty()) {
            return;
        }

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.isEmpty() && isCompatible(tag, machineId) && tag.contains("mmce_upgrade", Tag.TAG_COMPOUND)) {
            addUpgrade(output, bus, slot, stack, stack.getCount(), tag.getCompound("mmce_upgrade"), tag, machineId);
        }

        for (CompoundTag fixed : MmceMachineUpgradeHelper.fixedUpgrades(stack)) {
            addUpgrade(output, bus, slot, stack, stack.getCount(), upgradeInfo(fixed), fixed, machineId);
        }
    }

    private static void addUpgrade(List<MmceMachineUpgrade> output, UpgradeBusBlockEntity bus, int slot, ItemStack stack, int stackSize, CompoundTag upgradeInfo,
                                   CompoundTag installedData, ResourceLocation machineId) {
        if (upgradeInfo == null || !upgradeInfo.contains("name", Tag.TAG_STRING)) {
            return;
        }
        ResourceLocation id = upgradeId(upgradeInfo.getString("name"));
        CompoundTag registeredData = UPGRADES.get(id);
        CompoundTag data = mergeInstalledData(registeredData, installedData);
        if (!isCompatible(data, machineId)) {
            return;
        }
        output.add(MmceMachineUpgrade.from(id, stack, stackSize, data, bus, slot));
    }

    private static boolean isCompatible(CompoundTag tag, ResourceLocation machineId) {
        if (tag == null || machineId == null) {
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
            ResourceLocation candidate = normalizeId(machines.getString(i));
            if (candidate.equals(machineId)) {
                return true;
            }
        }
        return false;
    }

    private static ResourceLocation normalizeId(String id) {
        return id.indexOf(':') >= 0
                ? ResourceLocation.parse(id)
                : ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, id);
    }

    private static CompoundTag mergeInstalledData(CompoundTag registeredData, CompoundTag installedData) {
        CompoundTag merged = registeredData == null ? new CompoundTag() : registeredData.copy();
        if (installedData == null) {
            return merged;
        }
        for (String key : List.of("mmce_upgrade", "mmce_modifiers", "compatibleMachines", "incompatibleMachines",
                "descriptions", "itemData", "customData")) {
            if (installedData.contains(key)) {
                merged.put(key, installedData.get(key).copy());
            }
        }
        if (!merged.contains("mmce_upgrade") && installedData.contains("mmce_upgrade", Tag.TAG_COMPOUND)) {
            merged.put("mmce_upgrade", installedData.getCompound("mmce_upgrade").copy());
        }
        return merged;
    }

    private static CompoundTag upgradeInfo(CompoundTag holder) {
        return holder.contains("mmce_upgrade", Tag.TAG_COMPOUND)
                ? holder.getCompound("mmce_upgrade")
                : holder;
    }
}
