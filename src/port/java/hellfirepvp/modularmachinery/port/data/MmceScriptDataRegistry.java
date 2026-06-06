package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

public final class MmceScriptDataRegistry {
    private static final Object LOCK = new Object();
    private static final Map<ResourceLocation, JsonObject> SCRIPT_MACHINES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, JsonObject> SCRIPT_RECIPES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, JsonObject> SCRIPT_ADAPTERS = new LinkedHashMap<>();
    private static final List<MachinePatch> SCRIPT_MACHINE_PATCHES = new ArrayList<>();
    private static MmceDataRegistry.Snapshot datapackSnapshot = MmceDataRegistry.Snapshot.EMPTY;
    private static RecipeManager recipeManager;
    private static HolderLookup.Provider registries;

    public static void clear() {
        synchronized (LOCK) {
            SCRIPT_MACHINES.clear();
            SCRIPT_RECIPES.clear();
            SCRIPT_ADAPTERS.clear();
            SCRIPT_MACHINE_PATCHES.clear();
            MmceDataRegistry.replace(mergeLocked(datapackSnapshot));
        }
    }

    public static void registerMachine(ResourceLocation sourceId, JsonObject json) {
        synchronized (LOCK) {
            SCRIPT_MACHINES.put(sourceId, json.deepCopy());
            MmceDataRegistry.replace(mergeLocked(datapackSnapshot));
        }
    }

    public static void registerRecipe(ResourceLocation sourceId, JsonObject json) {
        synchronized (LOCK) {
            SCRIPT_RECIPES.put(sourceId, json.deepCopy());
            MmceDataRegistry.replace(mergeLocked(datapackSnapshot));
        }
    }

    public static void registerAdapter(ResourceLocation sourceId, JsonObject json) {
        synchronized (LOCK) {
            SCRIPT_ADAPTERS.put(sourceId, json.deepCopy());
            MmceDataRegistry.replace(mergeLocked(datapackSnapshot));
        }
    }

    public static void registerMachinePatch(ResourceLocation machineId, ResourceLocation sourceId, Consumer<JsonObject> patcher) {
        if (machineId == null || sourceId == null || patcher == null) {
            return;
        }
        synchronized (LOCK) {
            SCRIPT_MACHINE_PATCHES.add(new MachinePatch(machineId, sourceId, patcher));
            MmceDataRegistry.replace(mergeLocked(datapackSnapshot));
        }
    }

    static MmceDataRegistry.Snapshot replaceDatapackSnapshot(
            MmceDataRegistry.Snapshot snapshot,
            RecipeManager nextRecipeManager,
            HolderLookup.Provider nextRegistries
    ) {
        synchronized (LOCK) {
            datapackSnapshot = snapshot;
            recipeManager = nextRecipeManager;
            registries = nextRegistries;
            return mergeLocked(snapshot);
        }
    }

    public static int scriptMachineCount() {
        synchronized (LOCK) {
            return SCRIPT_MACHINES.size();
        }
    }

    public static int scriptRecipeCount() {
        synchronized (LOCK) {
            return SCRIPT_RECIPES.size();
        }
    }

    public static int scriptAdapterCount() {
        synchronized (LOCK) {
            return SCRIPT_ADAPTERS.size();
        }
    }

    public static int scriptMachinePatchCount() {
        synchronized (LOCK) {
            return SCRIPT_MACHINE_PATCHES.size();
        }
    }

    private static MmceDataRegistry.Snapshot mergeLocked(MmceDataRegistry.Snapshot base) {
        Map<ResourceLocation, MmceMachineDefinition> machines = new LinkedHashMap<>(base.machines());
        Map<ResourceLocation, MmceRecipeDefinition> recipes = new LinkedHashMap<>(base.recipes());
        Map<ResourceLocation, MmceRecipeAdapterDefinition> adapters = new LinkedHashMap<>(base.adapters());
        List<MmceDataLoadIssue> loadIssues = new ArrayList<>(base.loadIssues());

        SCRIPT_MACHINES.forEach((sourceId, json) -> {
            try {
                MmceMachineDefinition machine = MmceMachineDefinition.parse(sourceId, json);
                machines.put(machine.id(), machine);
            } catch (RuntimeException ex) {
                loadIssues.add(MmceDataLoadIssue.of("script machine", sourceId, ex));
                ModularMachineryNeoForge.LOGGER.error("Failed to load scripted MMCE machine {}", sourceId, ex);
            }
        });

        for (MachinePatch patch : SCRIPT_MACHINE_PATCHES) {
            MmceMachineDefinition current = machines.get(patch.machineId());
            if (current == null) {
                ModularMachineryNeoForge.LOGGER.debug("Deferring MMCE machine patch {} because target {} is not loaded yet",
                        patch.sourceId(), patch.machineId());
                continue;
            }
            try {
                JsonObject root = current.rawJson().deepCopy();
                patch.patcher().accept(root);
                MmceMachineDefinition patched = MmceMachineDefinition.parse(patch.sourceId(), root);
                machines.remove(patch.machineId());
                machines.put(patched.id(), patched);
            } catch (RuntimeException ex) {
                loadIssues.add(MmceDataLoadIssue.of("script machine patch", patch.sourceId(), ex));
                ModularMachineryNeoForge.LOGGER.error("Failed to apply scripted MMCE machine patch {} to {}",
                        patch.sourceId(), patch.machineId(), ex);
            }
        }

        SCRIPT_RECIPES.forEach((sourceId, json) -> {
            try {
                MmceRecipeDefinition recipe = MmceRecipeDefinition.parse(sourceId, json);
                recipes.put(recipe.id(), recipe);
            } catch (RuntimeException ex) {
                loadIssues.add(MmceDataLoadIssue.of("script recipe", sourceId, ex));
                ModularMachineryNeoForge.LOGGER.error("Failed to load scripted MMCE recipe {}", sourceId, ex);
            }
        });

        SCRIPT_ADAPTERS.forEach((sourceId, json) -> {
            try {
                MmceRecipeAdapterDefinition adapter = MmceRecipeAdapterDefinition.parse(sourceId, json);
                adapters.put(adapter.id(), adapter);
            } catch (RuntimeException ex) {
                loadIssues.add(MmceDataLoadIssue.of("script recipe adapter", sourceId, ex));
                ModularMachineryNeoForge.LOGGER.error("Failed to load scripted MMCE recipe adapter {}", sourceId, ex);
            }
        });

        MmceDataRegistry.Snapshot merged = new MmceDataRegistry.Snapshot(
                unmodifiableCopy(machines),
                unmodifiableCopy(recipes),
                buildRecipesByMachine(recipes),
                unmodifiableCopy(adapters),
                base.variables(),
                List.copyOf(loadIssues)
        );
        return MmceRecipeAdapterGenerator.expandAdapters(merged, recipeManager, registries);
    }

    private static Map<ResourceLocation, List<MmceRecipeDefinition>> buildRecipesByMachine(Map<ResourceLocation, MmceRecipeDefinition> recipes) {
        Map<ResourceLocation, List<MmceRecipeDefinition>> recipesByMachine = new LinkedHashMap<>();
        for (MmceRecipeDefinition recipe : recipes.values()) {
            recipesByMachine.computeIfAbsent(recipe.machineId(), ignored -> new ArrayList<>()).add(recipe);
        }
        recipesByMachine.replaceAll((machineId, values) -> {
            values.sort(Comparator.comparingInt(MmceRecipeDefinition::priority));
            return List.copyOf(values);
        });
        return unmodifiableCopy(recipesByMachine);
    }

    private static <K, V> Map<K, V> unmodifiableCopy(Map<K, V> map) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    private record MachinePatch(ResourceLocation machineId, ResourceLocation sourceId, Consumer<JsonObject> patcher) {
    }

    private MmceScriptDataRegistry() {
    }
}
