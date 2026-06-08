package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record MmceRecipeDefinition(
        ResourceLocation id,
        ResourceLocation sourceId,
        ResourceLocation machineId,
        int recipeTime,
        int priority,
        boolean cancelIfPerTickFails,
        boolean parallelized,
        String threadName,
        int maxThreads,
        List<String> recipeTooltips,
        boolean loadJei,
        List<MmceRecipeRequirement> requirements,
        Optional<JsonArray> startCommands,
        Optional<JsonArray> processingCommands,
        Optional<JsonArray> finishCommands,
        Optional<JsonArray> failureCommands,
        JsonObject rawJson
) {
    static MmceRecipeDefinition parse(ResourceLocation sourceId, JsonElement json) {
        JsonObject root = GsonHelper.convertToJsonObject(json, "machine recipe");
        String registryName = MmceJsonUtil.optionalString(root, "registryName", "registryname", "registry-name", "registry_name")
                .orElse("");
        if (registryName.isBlank()) {
            throw new IllegalArgumentException("Missing registryName/registryname");
        }

        JsonArray requirementArray = GsonHelper.getAsJsonArray(root, "requirements");
        List<MmceRecipeRequirement> parsedRequirements = new ArrayList<>(requirementArray.size());
        for (JsonElement element : requirementArray) {
            JsonObject requirement = GsonHelper.convertToJsonObject(element, "requirements[]");
            if (MmceRecipeRequirement.isLegacyDuration(requirement)) {
                continue;
            }
            parsedRequirements.add(MmceRecipeRequirement.parse(requirement));
        }

        return new MmceRecipeDefinition(
                MmceJsonUtil.modId(registryName),
                sourceId,
                MmceJsonUtil.modId(readPrimaryMachine(root)),
                readRecipeTime(root, requirementArray),
                GsonHelper.getAsInt(root, "priority", 0),
                readCancelIfPerTickFails(root),
                readParallelized(root),
                readThreadName(root),
                readMaxThreads(root),
                readRecipeTooltips(root),
                readLoadJei(root),
                List.copyOf(parsedRequirements),
                optionalArray(root, "startCommands", "start-commands", "start_commands", "startCommand", "start-command", "start_command"),
                optionalArray(root, "processingCommands", "processing-commands", "processing_commands", "processingCommand", "processing-command", "processing_command"),
                optionalArray(root, "finishCommands", "finish-commands", "finish_commands", "finishCommand", "finish-command", "finish_command"),
                optionalArray(root, "failureCommands", "failure-commands", "failure_commands", "failureCommand", "failure-command", "failure_command",
                        "failCommands", "fail-commands", "fail_commands", "failCommand", "fail-command", "fail_command"),
                root.deepCopy()
        );
    }

    private static String readPrimaryMachine(JsonObject root) {
        JsonElement machine = root.get("machine");
        if (machine == null) {
            throw new IllegalArgumentException("Missing machine");
        }
        if (machine.isJsonArray()) {
            JsonArray array = machine.getAsJsonArray();
            if (array.isEmpty()) {
                throw new IllegalArgumentException("Machine array is empty");
            }
            return GsonHelper.convertToString(array.get(0), "machine[0]");
        }
        return GsonHelper.convertToString(machine, "machine");
    }

    private static int readRecipeTime(JsonObject root, JsonArray requirementArray) {
        return readFirstInt(root, "recipeTime", "recipe-time", "recipe_time", "duration", "time")
                .or(() -> readLegacyDurationRequirementTime(requirementArray))
                .orElseThrow(() -> new IllegalArgumentException("Missing recipeTime"));
    }

    private static boolean readCancelIfPerTickFails(JsonObject root) {
        return readFirstBoolean(root, false,
                "cancelIfPerTickFails",
                "cancel-if-per-tick-fails",
                "cancel_if_per_tick_fails",
                "voidPerTickFailure",
                "void-per-tick-failure",
                "void_per_tick_failure");
    }

    private static boolean readParallelized(JsonObject root) {
        if (root.has("parallelized")) {
            return GsonHelper.getAsBoolean(root, "parallelized", false);
        }
        if (root.has("parallelize")) {
            return GsonHelper.getAsBoolean(root, "parallelize", false);
        }
        return root.has("parallelizable") && GsonHelper.getAsBoolean(root, "parallelizable", false);
    }

    private static String readThreadName(JsonObject root) {
        if (root.has("threadName")) {
            return GsonHelper.getAsString(root, "threadName", "").trim();
        }
        if (root.has("thread-name")) {
            return GsonHelper.getAsString(root, "thread-name", "").trim();
        }
        if (root.has("thread_name")) {
            return GsonHelper.getAsString(root, "thread_name", "").trim();
        }
        return "";
    }

    private static int readMaxThreads(JsonObject root) {
        if (root.has("maxThreads")) {
            return Math.max(-1, GsonHelper.getAsInt(root, "maxThreads", -1));
        }
        if (root.has("max-threads")) {
            return Math.max(-1, GsonHelper.getAsInt(root, "max-threads", -1));
        }
        if (root.has("max_threads")) {
            return Math.max(-1, GsonHelper.getAsInt(root, "max_threads", -1));
        }
        return -1;
    }

    private static List<String> readRecipeTooltips(JsonObject root) {
        return readStringList(root, "recipeTooltips", "recipe-tooltips", "recipe_tooltips", "tooltips");
    }

    private static boolean readLoadJei(JsonObject root) {
        if (root.has("loadJEI")) {
            return GsonHelper.getAsBoolean(root, "loadJEI", true);
        }
        if (root.has("loadJei")) {
            return GsonHelper.getAsBoolean(root, "loadJei", true);
        }
        if (root.has("load-jei")) {
            return GsonHelper.getAsBoolean(root, "load-jei", true);
        }
        if (root.has("load_jei")) {
            return GsonHelper.getAsBoolean(root, "load_jei", true);
        }
        return true;
    }

    private static List<String> readStringList(JsonObject root, String... keys) {
        for (String key : keys) {
            if (!root.has(key)) {
                continue;
            }
            JsonElement value = root.get(key);
            List<String> values = new ArrayList<>();
            if (value.isJsonArray()) {
                JsonArray array = value.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    JsonElement element = array.get(i);
                    if (!element.isJsonNull()) {
                        values.add(GsonHelper.convertToString(element, key + "[" + i + "]"));
                    }
                }
            } else if (!value.isJsonNull()) {
                values.add(GsonHelper.convertToString(value, key));
            }
            return List.copyOf(values);
        }
        return List.of();
    }

    private static Optional<Integer> readFirstInt(JsonObject root, String... keys) {
        for (String key : keys) {
            if (root.has(key)) {
                return Optional.of(GsonHelper.getAsInt(root, key));
            }
        }
        return Optional.empty();
    }

    private static Optional<Integer> readLegacyDurationRequirementTime(JsonArray requirementArray) {
        for (JsonElement element : requirementArray) {
            JsonObject requirement = GsonHelper.convertToJsonObject(element, "requirements[]");
            Optional<Integer> duration = MmceRecipeRequirement.readLegacyDuration(requirement);
            if (duration.isPresent()) {
                return duration;
            }
        }
        return Optional.empty();
    }

    private static boolean readFirstBoolean(JsonObject root, boolean fallback, String... keys) {
        for (String key : keys) {
            if (root.has(key)) {
                return GsonHelper.getAsBoolean(root, key, fallback);
            }
        }
        return fallback;
    }

    private static Optional<JsonArray> optionalArray(JsonObject root, String... keys) {
        for (String key : keys) {
            if (root.has(key)) {
                JsonElement element = root.get(key);
                if (element.isJsonArray()) {
                    return Optional.of(element.getAsJsonArray().deepCopy());
                }
                JsonArray array = new JsonArray();
                array.add(element.deepCopy());
                return Optional.of(array);
            }
        }
        return Optional.empty();
    }
}
