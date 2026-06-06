package hellfirepvp.modularmachinery.port.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

public final class MmceDataReloadListener extends SimplePreparableReloadListener<MmceDataRegistry.Snapshot> {
    private static final Gson GSON = new Gson();
    private static final List<String> MACHINERY_DIRECTORIES = List.of(
            "modularmachinery/machinery",
            "modularmachinery/machines",
            "mmce_machinery"
    );
    private static final List<String> RECIPES_DIRECTORIES = List.of(
            "modularmachinery/recipes",
            "mmce_recipes"
    );
    private static final List<String> VARIABLES_DIRECTORIES = List.of(
            "modularmachinery/variables",
            "mmce_variables"
    );
    private final RecipeManager recipeManager;
    private final HolderLookup.Provider registries;

    private MmceDataReloadListener(RecipeManager recipeManager, HolderLookup.Provider registries) {
        this.recipeManager = recipeManager;
        this.registries = registries;
    }

    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new MmceDataReloadListener(event.getServerResources().getRecipeManager(), event.getRegistryAccess()));
    }

    @Override
    protected MmceDataRegistry.Snapshot prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> machineJson = new HashMap<>();
        Map<ResourceLocation, JsonElement> recipeJson = new HashMap<>();
        Map<ResourceLocation, JsonElement> variableJson = new HashMap<>();
        scanDirectories(resourceManager, MACHINERY_DIRECTORIES, machineJson);
        scanDirectories(resourceManager, RECIPES_DIRECTORIES, recipeJson);
        scanDirectories(resourceManager, VARIABLES_DIRECTORIES, variableJson);

        Map<ResourceLocation, MmceMachineDefinition> machines = new LinkedHashMap<>();
        Map<ResourceLocation, MmceRecipeDefinition> recipes = new LinkedHashMap<>();
        Map<ResourceLocation, MmceRecipeAdapterDefinition> adapters = new LinkedHashMap<>();
        Map<String, List<String>> variables = new LinkedHashMap<>();
        List<MmceDataLoadIssue> loadIssues = new ArrayList<>();

        variableJson.forEach((sourceId, json) -> parseVariables(sourceId, json, variables, loadIssues));
        machineJson.forEach((sourceId, json) -> parseMachine(sourceId, json, machines, loadIssues));
        recipeJson.forEach((sourceId, json) -> {
            if (sourceId.getPath().endsWith(".adapter")) {
                parseAdapter(sourceId, json, adapters, loadIssues);
            } else {
                parseRecipe(sourceId, json, recipes, loadIssues);
            }
        });

        return new MmceDataRegistry.Snapshot(
                Map.copyOf(machines),
                Map.copyOf(recipes),
                buildRecipesByMachine(recipes),
                Map.copyOf(adapters),
                Map.copyOf(variables),
                List.copyOf(loadIssues)
        );
    }

    @Override
    protected void apply(MmceDataRegistry.Snapshot snapshot, ResourceManager resourceManager, ProfilerFiller profiler) {
        MmceDataRegistry.Snapshot merged = MmceScriptDataRegistry.replaceDatapackSnapshot(snapshot, recipeManager, registries);
        MmceDataRegistry.replace(merged);
        ModularMachineryNeoForge.LOGGER.info("Loaded {} MMCE machine definitions, {} recipes, {} recipe adapters",
                merged.machines().size(), merged.recipes().size(), merged.adapters().size());
    }

    private static void parseVariables(
            ResourceLocation sourceId,
            JsonElement json,
            Map<String, List<String>> variables,
            List<MmceDataLoadIssue> loadIssues
    ) {
        try {
            JsonObject root = net.minecraft.util.GsonHelper.convertToJsonObject(json, "machine variables");
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                variables.put(entry.getKey(), MmceJsonUtil.stringOrArray(root, entry.getKey()));
            }
        } catch (RuntimeException ex) {
            loadIssues.add(MmceDataLoadIssue.of("variable", sourceId, ex));
            ModularMachineryNeoForge.LOGGER.error("Failed to load MMCE variable definition {}", sourceId, ex);
        }
    }

    private static void scanDirectories(ResourceManager resourceManager, List<String> directories, Map<ResourceLocation, JsonElement> output) {
        for (String directory : directories) {
            SimpleJsonResourceReloadListener.scanDirectory(resourceManager, directory, GSON, output);
        }
    }

    private static void parseMachine(
            ResourceLocation sourceId,
            JsonElement json,
            Map<ResourceLocation, MmceMachineDefinition> machines,
            List<MmceDataLoadIssue> loadIssues
    ) {
        try {
            MmceMachineDefinition machine = MmceMachineDefinition.parse(sourceId, json);
            machines.put(machine.id(), machine);
        } catch (RuntimeException ex) {
            loadIssues.add(MmceDataLoadIssue.of("machine", sourceId, ex));
            ModularMachineryNeoForge.LOGGER.error("Failed to load MMCE machine definition {}", sourceId, ex);
        }
    }

    private static void parseRecipe(
            ResourceLocation sourceId,
            JsonElement json,
            Map<ResourceLocation, MmceRecipeDefinition> recipes,
            List<MmceDataLoadIssue> loadIssues
    ) {
        try {
            MmceRecipeDefinition recipe = MmceRecipeDefinition.parse(sourceId, json);
            recipes.put(recipe.id(), recipe);
        } catch (RuntimeException ex) {
            loadIssues.add(MmceDataLoadIssue.of("recipe", sourceId, ex));
            ModularMachineryNeoForge.LOGGER.error("Failed to load MMCE recipe {}", sourceId, ex);
        }
    }

    private static void parseAdapter(
            ResourceLocation sourceId,
            JsonElement json,
            Map<ResourceLocation, MmceRecipeAdapterDefinition> adapters,
            List<MmceDataLoadIssue> loadIssues
    ) {
        try {
            MmceRecipeAdapterDefinition adapter = MmceRecipeAdapterDefinition.parse(sourceId, json);
            adapters.put(adapter.id(), adapter);
        } catch (RuntimeException ex) {
            loadIssues.add(MmceDataLoadIssue.of("recipe adapter", sourceId, ex));
            ModularMachineryNeoForge.LOGGER.error("Failed to load MMCE recipe adapter {}", sourceId, ex);
        }
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
        return Map.copyOf(recipesByMachine);
    }
}
