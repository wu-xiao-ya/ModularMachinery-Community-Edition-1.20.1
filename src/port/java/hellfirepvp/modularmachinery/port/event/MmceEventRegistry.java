package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeRegistry;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class MmceEventRegistry {
    private static final Map<ResourceLocation, EnumMap<MmceMachineEventType, List<MmceMachineEventHandler>>> MACHINE_HANDLERS = new HashMap<>();
    private static final Map<ResourceLocation, EnumMap<MmceRecipeEventType, List<MmceRecipeEventHandler>>> RECIPE_HANDLERS = new HashMap<>();
    private static final Map<ResourceLocation, EnumMap<MmceRecipeEventType, List<MmceRecipeEventHandler>>> RECIPE_ADAPTER_HANDLERS = new HashMap<>();
    private static final Map<ResourceLocation, EnumMap<MmceMachineEventType, Map<String, MmceMachineEventHandler>>> KEYED_MACHINE_HANDLERS = new HashMap<>();
    private static final Map<ResourceLocation, EnumMap<MmceRecipeEventType, Map<String, MmceRecipeEventHandler>>> KEYED_RECIPE_HANDLERS = new HashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> GENERATED_RECIPE_ADAPTERS = new HashMap<>();

    public static ResourceLocation resolveId(String value) {
        String id = value == null ? "" : value.trim();
        if (id.isBlank()) {
            throw new IllegalArgumentException("MMCE event id cannot be blank");
        }
        return id.indexOf(':') >= 0
                ? ResourceLocation.parse(id)
                : ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, id);
    }

    public static void clear() {
        MACHINE_HANDLERS.clear();
        RECIPE_HANDLERS.clear();
        RECIPE_ADAPTER_HANDLERS.clear();
        KEYED_MACHINE_HANDLERS.clear();
        KEYED_RECIPE_HANDLERS.clear();
        GENERATED_RECIPE_ADAPTERS.clear();
    }

    public static void clearGeneratedRecipeAdapterLinks() {
        GENERATED_RECIPE_ADAPTERS.clear();
    }

    public static void registerMachine(String machineId, MmceMachineEventType type, MmceMachineEventHandler handler) {
        registerMachine(resolveId(machineId), type, handler);
    }

    public static void registerMachine(ResourceLocation machineId, MmceMachineEventType type, MmceMachineEventHandler handler) {
        if (machineId == null || type == null || handler == null) {
            return;
        }
        MACHINE_HANDLERS
                .computeIfAbsent(machineId, ignored -> new EnumMap<>(MmceMachineEventType.class))
                .computeIfAbsent(type, ignored -> new ArrayList<>())
                .add(handler);
    }

    public static void registerMachine(String machineId, String key, MmceMachineEventType type, MmceMachineEventHandler handler) {
        registerMachine(resolveId(machineId), key, type, handler);
    }

    public static void registerMachine(ResourceLocation machineId, String key, MmceMachineEventType type, MmceMachineEventHandler handler) {
        String handlerKey = normalizeKey(key);
        if (machineId == null || handlerKey.isBlank() || type == null || handler == null) {
            return;
        }
        KEYED_MACHINE_HANDLERS
                .computeIfAbsent(machineId, ignored -> new EnumMap<>(MmceMachineEventType.class))
                .computeIfAbsent(type, ignored -> new LinkedHashMap<>())
                .put(handlerKey, handler);
    }

    public static void registerRecipe(String recipeId, MmceRecipeEventType type, MmceRecipeEventHandler handler) {
        registerRecipe(resolveId(recipeId), type, handler);
    }

    public static void registerRecipe(ResourceLocation recipeId, MmceRecipeEventType type, MmceRecipeEventHandler handler) {
        if (recipeId == null || type == null || handler == null) {
            return;
        }
        RECIPE_HANDLERS
                .computeIfAbsent(recipeId, ignored -> new EnumMap<>(MmceRecipeEventType.class))
                .computeIfAbsent(type, ignored -> new ArrayList<>())
                .add(handler);
    }

    public static void registerRecipeAdapter(ResourceLocation adapterId, MmceRecipeEventType type, MmceRecipeEventHandler handler) {
        if (adapterId == null || type == null || handler == null) {
            return;
        }
        RECIPE_ADAPTER_HANDLERS
                .computeIfAbsent(adapterId, ignored -> new EnumMap<>(MmceRecipeEventType.class))
                .computeIfAbsent(type, ignored -> new ArrayList<>())
                .add(handler);
    }

    public static void linkGeneratedRecipeToAdapter(ResourceLocation recipeId, ResourceLocation adapterId) {
        if (recipeId != null && adapterId != null) {
            GENERATED_RECIPE_ADAPTERS.put(recipeId, adapterId);
        }
    }

    public static void registerRecipe(String recipeId, String key, MmceRecipeEventType type, MmceRecipeEventHandler handler) {
        registerRecipe(resolveId(recipeId), key, type, handler);
    }

    public static void registerRecipe(ResourceLocation recipeId, String key, MmceRecipeEventType type, MmceRecipeEventHandler handler) {
        String handlerKey = normalizeKey(key);
        if (recipeId == null || handlerKey.isBlank() || type == null || handler == null) {
            return;
        }
        KEYED_RECIPE_HANDLERS
                .computeIfAbsent(recipeId, ignored -> new EnumMap<>(MmceRecipeEventType.class))
                .computeIfAbsent(type, ignored -> new LinkedHashMap<>())
                .put(handlerKey, handler);
    }

    public static <E extends MmceMachineEvent> E postMachine(E event) {
        if (event == null) {
            return null;
        }
        ResourceLocation machineId = event.getMachineResourceLocation();
        if (machineId == null) {
            return event;
        }
        MmceMachineUpgradeRegistry.postMachine(event);
        if (event.isCanceled()) {
            return event;
        }
        List<MmceMachineEventHandler> handlers = MACHINE_HANDLERS
                .getOrDefault(machineId, new EnumMap<>(MmceMachineEventType.class))
                .getOrDefault(event.getEventType(), List.of());
        for (MmceMachineEventHandler handler : List.copyOf(handlers)) {
            try {
                handler.handle(event);
            } catch (Exception exception) {
                ModularMachineryNeoForge.LOGGER.warn("Caught an exception while handling MMCE machine event {}", event.getType(), exception);
            }
            if (event.isCanceled()) {
                break;
            }
        }
        if (event.isCanceled()) {
            return event;
        }
        Map<String, MmceMachineEventHandler> keyedHandlers = KEYED_MACHINE_HANDLERS
                .getOrDefault(machineId, new EnumMap<>(MmceMachineEventType.class))
                .getOrDefault(event.getEventType(), Map.of());
        for (MmceMachineEventHandler handler : List.copyOf(keyedHandlers.values())) {
            try {
                handler.handle(event);
            } catch (Exception exception) {
                ModularMachineryNeoForge.LOGGER.warn("Caught an exception while handling keyed MMCE machine event {}", event.getType(), exception);
            }
            if (event.isCanceled()) {
                break;
            }
        }
        return event;
    }

    public static <E extends MmceRecipeEvent> E postRecipe(E event) {
        if (event == null) {
            return null;
        }
        ResourceLocation recipeId = event.getRecipeResourceLocation();
        if (recipeId == null) {
            return event;
        }
        MmceMachineUpgradeRegistry.postRecipe(event);
        if (event.isCanceled()) {
            return event;
        }
        List<MmceRecipeEventHandler> handlers = RECIPE_HANDLERS
                .getOrDefault(recipeId, new EnumMap<>(MmceRecipeEventType.class))
                .getOrDefault(event.getRecipeEventType(), List.of());
        for (MmceRecipeEventHandler handler : List.copyOf(handlers)) {
            try {
                handler.handle(event);
            } catch (Exception exception) {
                ModularMachineryNeoForge.LOGGER.warn("Caught an exception while handling MMCE recipe event {}", event.getRecipeEventTypeName(), exception);
            }
            if (event.isCanceled()) {
                break;
            }
        }
        if (event.isCanceled()) {
            return event;
        }
        ResourceLocation adapterId = GENERATED_RECIPE_ADAPTERS.get(recipeId);
        if (adapterId != null) {
            List<MmceRecipeEventHandler> adapterHandlers = RECIPE_ADAPTER_HANDLERS
                    .getOrDefault(adapterId, new EnumMap<>(MmceRecipeEventType.class))
                    .getOrDefault(event.getRecipeEventType(), List.of());
            for (MmceRecipeEventHandler handler : List.copyOf(adapterHandlers)) {
                try {
                    handler.handle(event);
                } catch (Exception exception) {
                    ModularMachineryNeoForge.LOGGER.warn("Caught an exception while handling MMCE recipe adapter event {}", event.getRecipeEventTypeName(), exception);
                }
                if (event.isCanceled()) {
                    break;
                }
            }
        }
        if (event.isCanceled()) {
            return event;
        }
        Map<String, MmceRecipeEventHandler> keyedHandlers = KEYED_RECIPE_HANDLERS
                .getOrDefault(recipeId, new EnumMap<>(MmceRecipeEventType.class))
                .getOrDefault(event.getRecipeEventType(), Map.of());
        for (MmceRecipeEventHandler handler : List.copyOf(keyedHandlers.values())) {
            try {
                handler.handle(event);
            } catch (Exception exception) {
                ModularMachineryNeoForge.LOGGER.warn("Caught an exception while handling keyed MMCE recipe event {}", event.getRecipeEventTypeName(), exception);
            }
            if (event.isCanceled()) {
                break;
            }
        }
        return event;
    }

    private static String normalizeKey(String key) {
        return key == null ? "" : key.trim();
    }

    private MmceEventRegistry() {
    }
}
