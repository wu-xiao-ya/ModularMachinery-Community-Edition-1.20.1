package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeRegistry;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;

public final class MmceEventRegistry {
    private static final Map<ResourceLocation, EnumMap<MmceMachineEventType, List<HandlerRegistration<MmceMachineEventHandler>>>> MACHINE_HANDLERS = new HashMap<>();
    private static final Map<ResourceLocation, EnumMap<MmceRecipeEventType, List<HandlerRegistration<MmceRecipeEventHandler>>>> RECIPE_HANDLERS = new HashMap<>();
    private static final Map<ResourceLocation, EnumMap<MmceRecipeEventType, List<HandlerRegistration<MmceRecipeEventHandler>>>> RECIPE_ADAPTER_HANDLERS = new HashMap<>();
    private static final Map<ResourceLocation, EnumMap<MmceMachineEventType, Map<String, HandlerRegistration<MmceMachineEventHandler>>>> KEYED_MACHINE_HANDLERS = new HashMap<>();
    private static final Map<ResourceLocation, EnumMap<MmceRecipeEventType, Map<String, HandlerRegistration<MmceRecipeEventHandler>>>> KEYED_RECIPE_HANDLERS = new HashMap<>();
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

    public static void clearSources(Predicate<ResourceLocation> sourcePredicate) {
        if (sourcePredicate == null) {
            return;
        }
        removeListRegistrations(MACHINE_HANDLERS, sourcePredicate);
        removeListRegistrations(RECIPE_HANDLERS, sourcePredicate);
        removeListRegistrations(RECIPE_ADAPTER_HANDLERS, sourcePredicate);
        removeKeyedRegistrations(KEYED_MACHINE_HANDLERS, sourcePredicate);
        removeKeyedRegistrations(KEYED_RECIPE_HANDLERS, sourcePredicate);
    }

    public static void registerMachine(String machineId, MmceMachineEventType type, MmceMachineEventHandler handler) {
        registerMachine(resolveId(machineId), type, handler);
    }

    public static void registerMachine(ResourceLocation machineId, MmceMachineEventType type, MmceMachineEventHandler handler) {
        registerMachine(null, machineId, type, handler);
    }

    public static void registerMachine(ResourceLocation sourceId, ResourceLocation machineId, MmceMachineEventType type, MmceMachineEventHandler handler) {
        if (machineId == null || type == null || handler == null) {
            return;
        }
        MACHINE_HANDLERS
                .computeIfAbsent(machineId, ignored -> new EnumMap<>(MmceMachineEventType.class))
                .computeIfAbsent(type, ignored -> new ArrayList<>())
                .add(new HandlerRegistration<>(sourceId, handler));
    }

    public static void registerMachine(String machineId, String key, MmceMachineEventType type, MmceMachineEventHandler handler) {
        registerMachine(resolveId(machineId), key, type, handler);
    }

    public static void registerMachine(ResourceLocation machineId, String key, MmceMachineEventType type, MmceMachineEventHandler handler) {
        registerMachine(null, machineId, key, type, handler);
    }

    public static void registerMachine(ResourceLocation sourceId, String machineId, String key, MmceMachineEventType type, MmceMachineEventHandler handler) {
        registerMachine(sourceId, resolveId(machineId), key, type, handler);
    }

    public static void registerMachine(ResourceLocation sourceId, ResourceLocation machineId, String key, MmceMachineEventType type, MmceMachineEventHandler handler) {
        String handlerKey = normalizeKey(key);
        if (machineId == null || handlerKey.isBlank() || type == null || handler == null) {
            return;
        }
        KEYED_MACHINE_HANDLERS
                .computeIfAbsent(machineId, ignored -> new EnumMap<>(MmceMachineEventType.class))
                .computeIfAbsent(type, ignored -> new LinkedHashMap<>())
                .put(handlerKey, new HandlerRegistration<>(sourceId, handler));
    }

    public static void registerRecipe(String recipeId, MmceRecipeEventType type, MmceRecipeEventHandler handler) {
        registerRecipe(resolveId(recipeId), type, handler);
    }

    public static void registerRecipe(ResourceLocation recipeId, MmceRecipeEventType type, MmceRecipeEventHandler handler) {
        registerRecipe(null, recipeId, type, handler);
    }

    public static void registerRecipe(ResourceLocation sourceId, ResourceLocation recipeId, MmceRecipeEventType type, MmceRecipeEventHandler handler) {
        if (recipeId == null || type == null || handler == null) {
            return;
        }
        RECIPE_HANDLERS
                .computeIfAbsent(recipeId, ignored -> new EnumMap<>(MmceRecipeEventType.class))
                .computeIfAbsent(type, ignored -> new ArrayList<>())
                .add(new HandlerRegistration<>(sourceId, handler));
    }

    public static void registerRecipeAdapter(ResourceLocation adapterId, MmceRecipeEventType type, MmceRecipeEventHandler handler) {
        registerRecipeAdapter(null, adapterId, type, handler);
    }

    public static void registerRecipeAdapter(ResourceLocation sourceId, ResourceLocation adapterId, MmceRecipeEventType type, MmceRecipeEventHandler handler) {
        if (adapterId == null || type == null || handler == null) {
            return;
        }
        RECIPE_ADAPTER_HANDLERS
                .computeIfAbsent(adapterId, ignored -> new EnumMap<>(MmceRecipeEventType.class))
                .computeIfAbsent(type, ignored -> new ArrayList<>())
                .add(new HandlerRegistration<>(sourceId, handler));
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
        registerRecipe(null, recipeId, key, type, handler);
    }

    public static void registerRecipe(ResourceLocation sourceId, String recipeId, String key, MmceRecipeEventType type, MmceRecipeEventHandler handler) {
        registerRecipe(sourceId, resolveId(recipeId), key, type, handler);
    }

    public static void registerRecipe(ResourceLocation sourceId, ResourceLocation recipeId, String key, MmceRecipeEventType type, MmceRecipeEventHandler handler) {
        String handlerKey = normalizeKey(key);
        if (recipeId == null || handlerKey.isBlank() || type == null || handler == null) {
            return;
        }
        KEYED_RECIPE_HANDLERS
                .computeIfAbsent(recipeId, ignored -> new EnumMap<>(MmceRecipeEventType.class))
                .computeIfAbsent(type, ignored -> new LinkedHashMap<>())
                .put(handlerKey, new HandlerRegistration<>(sourceId, handler));
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
        List<HandlerRegistration<MmceMachineEventHandler>> handlers = MACHINE_HANDLERS
                .getOrDefault(machineId, new EnumMap<>(MmceMachineEventType.class))
                .getOrDefault(event.getEventType(), List.of());
        for (HandlerRegistration<MmceMachineEventHandler> registration : List.copyOf(handlers)) {
            try {
                registration.handler().handle(event);
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
        Map<String, HandlerRegistration<MmceMachineEventHandler>> keyedHandlers = KEYED_MACHINE_HANDLERS
                .getOrDefault(machineId, new EnumMap<>(MmceMachineEventType.class))
                .getOrDefault(event.getEventType(), Map.of());
        for (HandlerRegistration<MmceMachineEventHandler> registration : List.copyOf(keyedHandlers.values())) {
            try {
                registration.handler().handle(event);
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
        List<HandlerRegistration<MmceRecipeEventHandler>> handlers = RECIPE_HANDLERS
                .getOrDefault(recipeId, new EnumMap<>(MmceRecipeEventType.class))
                .getOrDefault(event.getRecipeEventType(), List.of());
        for (HandlerRegistration<MmceRecipeEventHandler> registration : List.copyOf(handlers)) {
            try {
                registration.handler().handle(event);
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
            List<HandlerRegistration<MmceRecipeEventHandler>> adapterHandlers = RECIPE_ADAPTER_HANDLERS
                    .getOrDefault(adapterId, new EnumMap<>(MmceRecipeEventType.class))
                    .getOrDefault(event.getRecipeEventType(), List.of());
            for (HandlerRegistration<MmceRecipeEventHandler> registration : List.copyOf(adapterHandlers)) {
                try {
                    registration.handler().handle(event);
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
        Map<String, HandlerRegistration<MmceRecipeEventHandler>> keyedHandlers = KEYED_RECIPE_HANDLERS
                .getOrDefault(recipeId, new EnumMap<>(MmceRecipeEventType.class))
                .getOrDefault(event.getRecipeEventType(), Map.of());
        for (HandlerRegistration<MmceRecipeEventHandler> registration : List.copyOf(keyedHandlers.values())) {
            try {
                registration.handler().handle(event);
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

    private static <K, E extends Enum<E>, H> void removeListRegistrations(
            Map<K, EnumMap<E, List<HandlerRegistration<H>>>> handlers,
            Predicate<ResourceLocation> sourcePredicate
    ) {
        handlers.values().forEach(typeHandlers -> {
            typeHandlers.values().forEach(registrations -> registrations.removeIf(registration -> matches(registration, sourcePredicate)));
            typeHandlers.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        });
        handlers.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private static <K, E extends Enum<E>, H> void removeKeyedRegistrations(
            Map<K, EnumMap<E, Map<String, HandlerRegistration<H>>>> handlers,
            Predicate<ResourceLocation> sourcePredicate
    ) {
        handlers.values().forEach(typeHandlers -> {
            typeHandlers.values().forEach(registrations -> registrations.values().removeIf(registration -> matches(registration, sourcePredicate)));
            typeHandlers.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        });
        handlers.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private static boolean matches(HandlerRegistration<?> registration, Predicate<ResourceLocation> sourcePredicate) {
        return registration.sourceId() != null && sourcePredicate.test(registration.sourceId());
    }

    private record HandlerRegistration<H>(ResourceLocation sourceId, H handler) {
    }

    private MmceEventRegistry() {
    }
}
