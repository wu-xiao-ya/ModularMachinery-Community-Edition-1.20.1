package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record MmceMachineDefinition(
        ResourceLocation id,
        ResourceLocation sourceId,
        String localizedName,
        String prefix,
        String geoModel,
        List<MmceStructurePart> parts,
        List<MmceMachineModifierDefinition> modifiers,
        boolean requiresBlueprint,
        boolean hasFactory,
        boolean factoryOnly,
        MmceRecipeFailureAction failureAction,
        boolean parallelizable,
        int maxParallelism,
        int internalParallelism,
        int maxThreads,
        List<CoreThreadDefinition> coreThreads,
        List<SmartInterfaceTypeDefinition> smartInterfaceTypes,
        boolean hideComponentsWhenFormed,
        Optional<JsonArray> controllerBoundingBox,
        List<MmceDynamicPatternDefinition> dynamicPatterns,
        JsonObject rawJson
) {
    public static final int DEFAULT_MAX_THREADS = 20;

    static MmceMachineDefinition parse(ResourceLocation sourceId, JsonElement json) {
        JsonObject root = GsonHelper.convertToJsonObject(json, "machine definition");
        String registryName = GsonHelper.getAsString(root, "registryname", GsonHelper.getAsString(root, "registryName", ""));
        if (registryName.isBlank()) {
            throw new IllegalArgumentException("Missing registryname/registryName");
        }

        JsonArray partArray = GsonHelper.getAsJsonArray(root, "parts");
        List<MmceStructurePart> parsedParts = new ArrayList<>(partArray.size());
        for (JsonElement element : partArray) {
            parsedParts.add(MmceStructurePart.parse(GsonHelper.convertToJsonObject(element, "parts[]")));
        }

        List<MmceMachineModifierDefinition> parsedModifiers = new ArrayList<>();
        if (root.has("modifiers")) {
            JsonArray modifierArray = GsonHelper.getAsJsonArray(root, "modifiers");
            parsedModifiers = new ArrayList<>(modifierArray.size());
            for (JsonElement element : modifierArray) {
                parsedModifiers.addAll(MmceMachineModifierDefinition.parseMany(GsonHelper.convertToJsonObject(element, "modifiers[]")));
            }
        }

        Optional<JsonArray> boundingBox = readFirstArray(root,
                "controller-bounding-box", "controllerBoundingBox", "controller_bounding_box");
        Optional<JsonArray> dynamicPatternArray = readFirstArray(root,
                "dynamic-patterns", "dynamicPatterns", "dynamic_patterns");
        List<MmceDynamicPatternDefinition> dynamicPatterns = dynamicPatternArray
                .map(MmceDynamicPatternDefinition::parseMany)
                .orElse(List.of());

        return new MmceMachineDefinition(
                MmceJsonUtil.modId(registryName),
                sourceId,
                readFirstString(root, "localizedname", "localizedName", "localized-name", "localized_name"),
                readFirstString(root, "prefix"),
                readFirstString(root, "geoModel", "geo-model", "geo_model"),
                List.copyOf(parsedParts),
                List.copyOf(parsedModifiers),
                readFirstBoolean(root, false, "requires-blueprint", "requiresBlueprint", "requires_blueprint"),
                readFirstBoolean(root, false, "has-factory", "hasFactory", "has_factory"),
                readFirstBoolean(root, false, "factory-only", "factoryOnly", "factory_only"),
                readFailureAction(root),
                readFirstBoolean(root, true, "parallelizable"),
                readMachineMaxParallelism(root),
                readInternalParallelism(root),
                readMaxThreads(root),
                readCoreThreads(root),
                readSmartInterfaceTypes(root),
                readFirstBoolean(root, false, "hide-components-when-formed", "hideComponentsWhenFormed", "hide_components_when_formed"),
                boundingBox,
                dynamicPatterns,
                root.deepCopy()
        );
    }

    public int structureBlockCount() {
        int count = 0;
        for (MmceStructurePart part : parts) {
            count += part.permutationCount();
        }
        return count;
    }

    private static int readMaxThreads(JsonObject root) {
        if (root.has("maxThreads")) {
            return Math.max(0, GsonHelper.getAsInt(root, "maxThreads", DEFAULT_MAX_THREADS));
        }
        if (root.has("max-threads")) {
            return Math.max(0, GsonHelper.getAsInt(root, "max-threads", DEFAULT_MAX_THREADS));
        }
        if (root.has("max_threads")) {
            return Math.max(0, GsonHelper.getAsInt(root, "max_threads", DEFAULT_MAX_THREADS));
        }
        return DEFAULT_MAX_THREADS;
    }

    private static int readMachineMaxParallelism(JsonObject root) {
        if (root.has("maxParallelism")) {
            return Math.max(1, GsonHelper.getAsInt(root, "maxParallelism", Integer.MAX_VALUE));
        }
        if (root.has("max-parallelism")) {
            return Math.max(1, GsonHelper.getAsInt(root, "max-parallelism", Integer.MAX_VALUE));
        }
        if (root.has("max_parallelism")) {
            return Math.max(1, GsonHelper.getAsInt(root, "max_parallelism", Integer.MAX_VALUE));
        }
        return Integer.MAX_VALUE;
    }

    private static int readInternalParallelism(JsonObject root) {
        if (root.has("internalParallelism")) {
            return Math.max(0, GsonHelper.getAsInt(root, "internalParallelism", 0));
        }
        if (root.has("internal-parallelism")) {
            return Math.max(0, GsonHelper.getAsInt(root, "internal-parallelism", 0));
        }
        if (root.has("internal_parallelism")) {
            return Math.max(0, GsonHelper.getAsInt(root, "internal_parallelism", 0));
        }
        return 0;
    }

    private static MmceRecipeFailureAction readFailureAction(JsonObject root) {
        JsonElement element = firstPresent(root, "failure-action", "failureAction", "failure_action");
        return element == null ? MmceRecipeFailureAction.STILL : MmceRecipeFailureAction.byName(GsonHelper.convertToString(element, "failure-action"));
    }

    private static List<CoreThreadDefinition> readCoreThreads(JsonObject root) {
        JsonElement element = firstPresent(root, "coreThreads", "core-threads", "core_threads");
        if (element == null) {
            return List.of();
        }

        List<CoreThreadDefinition> threads = new ArrayList<>();
        if (element.isJsonArray()) {
            for (JsonElement entry : element.getAsJsonArray()) {
                readCoreThread(entry).ifPresent(threads::add);
            }
        } else {
            readCoreThread(element).ifPresent(threads::add);
        }
        return List.copyOf(threads);
    }

    private static List<SmartInterfaceTypeDefinition> readSmartInterfaceTypes(JsonObject root) {
        JsonElement element = firstPresent(root, "smartInterfaceTypes", "smart-interface-types", "smart_interface_types", "smartInterfaces");
        if (element == null) {
            return List.of();
        }

        List<SmartInterfaceTypeDefinition> types = new ArrayList<>();
        if (element.isJsonArray()) {
            for (JsonElement entry : element.getAsJsonArray()) {
                readSmartInterfaceType(entry).ifPresent(types::add);
            }
        } else {
            readSmartInterfaceType(element).ifPresent(types::add);
        }
        types.sort((left, right) -> Integer.compare(right.priority(), left.priority()));
        return List.copyOf(types);
    }

    private static Optional<SmartInterfaceTypeDefinition> readSmartInterfaceType(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Optional.empty();
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String type = element.getAsString().trim();
            return type.isBlank() ? Optional.empty() : Optional.of(new SmartInterfaceTypeDefinition(type, 0.0F, 0, "", "", "", "", "", 2));
        }
        JsonObject object = GsonHelper.convertToJsonObject(element, "smart interface type");
        String type = readFirstString(object, "type", "name", "interfaceType", "interface-type", "interface_type").trim();
        if (type.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new SmartInterfaceTypeDefinition(
                type,
                readFirstFloat(object, 0.0F, "defaultValue", "default-value", "default_value", "default"),
                GsonHelper.getAsInt(object, "priority", 0),
                readFirstString(object, "headerInfo", "header-info", "header_info"),
                readFirstString(object, "valueInfo", "value-info", "value_info"),
                readFirstString(object, "footerInfo", "footer-info", "footer_info"),
                readFirstString(object, "notEqualMessage", "not-equal-message", "not_equal_message"),
                readFirstString(object, "jeiTooltip", "jei-tooltip", "jei_tooltip"),
                GsonHelper.getAsInt(object, "jeiTooltipArgsCount", GsonHelper.getAsInt(object, "jei-tooltip-args-count", 2))
        ));
    }

    private static Optional<CoreThreadDefinition> readCoreThread(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Optional.empty();
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String value = element.getAsString().trim();
            return value.isBlank() ? Optional.empty() : Optional.of(new CoreThreadDefinition(value, List.of(), Map.of()));
        }
        JsonObject object = GsonHelper.convertToJsonObject(element, "core thread");
        JsonElement name = firstPresent(object, "threadName", "name", "thread-name", "thread_name");
        if (name == null) {
            return Optional.empty();
        }
        String value = GsonHelper.convertToString(name, "core thread name").trim();
        if (value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new CoreThreadDefinition(value, readCoreThreadRecipes(object), readCoreThreadModifiers(object)));
    }

    private static List<ResourceLocation> readCoreThreadRecipes(JsonObject object) {
        JsonElement recipes = firstPresent(object, "recipes", "recipeIds", "recipe-ids", "recipe_ids");
        if (recipes == null) {
            recipes = firstPresent(object, "recipe", "recipeId", "recipe-id", "recipe_id");
        }
        if (recipes == null) {
            return List.of();
        }

        List<String> rawRecipes = new ArrayList<>();
        if (recipes.isJsonArray()) {
            for (JsonElement entry : recipes.getAsJsonArray()) {
                String value = GsonHelper.convertToString(entry, "core thread recipe").trim();
                if (!value.isBlank()) {
                    rawRecipes.add(value);
                }
            }
        } else {
            String value = GsonHelper.convertToString(recipes, "core thread recipe").trim();
            if (!value.isBlank()) {
                rawRecipes.add(value);
            }
        }

        List<ResourceLocation> parsed = new ArrayList<>(rawRecipes.size());
        for (String recipe : rawRecipes) {
            parsed.add(MmceJsonUtil.modId(recipe));
        }
        return List.copyOf(parsed);
    }

    private static Map<String, MmceMachineModifierDefinition> readCoreThreadModifiers(JsonObject object) {
        JsonElement modifiers = firstPresent(object, "permanentModifiers", "permanent-modifiers", "permanent_modifiers");
        if (modifiers == null || modifiers.isJsonNull()) {
            return Map.of();
        }

        Map<String, MmceMachineModifierDefinition> parsed = new LinkedHashMap<>();
        if (modifiers.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : modifiers.getAsJsonObject().entrySet()) {
                parseCoreThreadModifier(entry.getKey(), entry.getValue()).ifPresent(modifier -> parsed.put(entry.getKey(), modifier));
            }
        } else if (modifiers.isJsonArray()) {
            int index = 0;
            for (JsonElement entry : modifiers.getAsJsonArray()) {
                String fallbackKey = "modifier_" + index++;
                parseCoreThreadModifier(fallbackKey, entry).ifPresent(modifier -> parsed.put(fallbackKey, modifier));
            }
        }
        return Map.copyOf(parsed);
    }

    private static Optional<MmceMachineModifierDefinition> parseCoreThreadModifier(String key, JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Optional.empty();
        }
        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("x", 0);
        wrapper.addProperty("y", 0);
        wrapper.addProperty("z", 0);
        JsonObject modifier = element.isJsonObject()
                ? element.getAsJsonObject().deepCopy()
                : JsonParser.parseString(element.getAsString()).getAsJsonObject();
        wrapper.add("modifier", modifier);
        return Optional.of(MmceMachineModifierDefinition.parse(wrapper));
    }

    private static JsonElement firstPresent(JsonObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key)) {
                return object.get(key);
            }
        }
        return null;
    }

    private static String readFirstString(JsonObject object, String... keys) {
        JsonElement element = firstPresent(object, keys);
        return element == null ? "" : GsonHelper.convertToString(element, keys[0]);
    }

    private static float readFirstFloat(JsonObject object, float fallback, String... keys) {
        JsonElement element = firstPresent(object, keys);
        return element == null ? fallback : GsonHelper.convertToFloat(element, keys[0]);
    }

    private static boolean readFirstBoolean(JsonObject object, boolean fallback, String... keys) {
        JsonElement element = firstPresent(object, keys);
        return element == null ? fallback : GsonHelper.convertToBoolean(element, keys[0]);
    }

    private static Optional<JsonArray> readFirstArray(JsonObject object, String... keys) {
        JsonElement element = firstPresent(object, keys);
        return element == null ? Optional.empty() : Optional.of(GsonHelper.convertToJsonArray(element, keys[0]).deepCopy());
    }

    public record CoreThreadDefinition(
            String threadName,
            List<ResourceLocation> recipes,
            Map<String, MmceMachineModifierDefinition> permanentModifiers
    ) {
        public CoreThreadDefinition {
            threadName = threadName == null ? "" : threadName.trim();
            recipes = List.copyOf(recipes == null ? List.of() : recipes);
            permanentModifiers = Map.copyOf(permanentModifiers == null ? Map.of() : permanentModifiers);
        }
    }

    public record SmartInterfaceTypeDefinition(
            String type,
            float defaultValue,
            int priority,
            String headerInfo,
            String valueInfo,
            String footerInfo,
            String notEqualMessage,
            String jeiTooltip,
            int jeiTooltipArgsCount
    ) {
        public SmartInterfaceTypeDefinition {
            type = type == null ? "" : type.trim();
            headerInfo = headerInfo == null ? "" : headerInfo;
            valueInfo = valueInfo == null ? "" : valueInfo;
            footerInfo = footerInfo == null ? "" : footerInfo;
            notEqualMessage = notEqualMessage == null ? "" : notEqualMessage;
            jeiTooltip = jeiTooltip == null ? "" : jeiTooltip;
            jeiTooltipArgsCount = Math.max(0, jeiTooltipArgsCount);
        }
    }
}
