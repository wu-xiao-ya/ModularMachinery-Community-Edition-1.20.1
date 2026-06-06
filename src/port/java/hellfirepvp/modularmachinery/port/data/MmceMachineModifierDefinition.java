package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record MmceMachineModifierDefinition(
        List<Integer> x,
        List<Integer> y,
        List<Integer> z,
        List<String> elements,
        Optional<String> description,
        Optional<String> checkerId,
        JsonObject matchNbt,
        JsonObject previewNbt,
        ResourceLocation target,
        Optional<MmceIoType> ioType,
        int operation,
        double multiplier,
        boolean affectChance,
        JsonObject rawJson
) {
    public static MmceMachineModifierDefinition parse(JsonObject object) {
        return parse(object, GsonHelper.getAsJsonObject(object, "modifier"));
    }

    public static List<MmceMachineModifierDefinition> parseMany(JsonObject object) {
        if (object.has("modifier")) {
            return List.of(parse(object, GsonHelper.getAsJsonObject(object, "modifier")));
        }
        if (!object.has("modifiers")) {
            throw new IllegalArgumentException("Machine modifier entry requires either 'modifier' or 'modifiers'.");
        }

        JsonElement modifiers = object.get("modifiers");
        if (modifiers.isJsonObject()) {
            return List.of(parse(object, modifiers.getAsJsonObject()));
        }
        JsonArray array = GsonHelper.convertToJsonArray(modifiers, "modifiers");
        List<MmceMachineModifierDefinition> parsed = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            parsed.add(parse(object, GsonHelper.convertToJsonObject(element, "modifiers[]")));
        }
        return List.copyOf(parsed);
    }

    private static MmceMachineModifierDefinition parse(JsonObject object, JsonObject modifier) {
        return new MmceMachineModifierDefinition(
                MmceJsonUtil.coordinates(object, "x"),
                MmceJsonUtil.coordinates(object, "y"),
                MmceJsonUtil.coordinates(object, "z"),
                MmceJsonUtil.stringOrArray(object, "elements"),
                MmceJsonUtil.optionalString(object, "description"),
                MmceJsonUtil.optionalString(object, "checker-id", "checkerId", "block-checker", "blockChecker").filter(id -> !id.isBlank()),
                MmceJsonUtil.rawObject(object, "nbt", "match-nbt", "matchNbt", "match_nbt"),
                MmceJsonUtil.rawObject(object, "preview-nbt", "previewNbt", "preview_nbt", "nbt-display", "nbtDisplay", "nbt_display"),
                MmceJsonUtil.modId(readTarget(modifier)),
                readIoType(modifier),
                readOperation(modifier),
                readMultiplier(modifier),
                readAffectChance(modifier),
                object.deepCopy()
        );
    }

    private static String readTarget(JsonObject modifier) {
        return MmceJsonUtil.optionalString(modifier,
                        "target", "requirement", "requirementType", "requirement-type", "requirement_type", "type")
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("Machine modifier requires target/requirement type."));
    }

    private static Optional<MmceIoType> readIoType(JsonObject modifier) {
        return MmceIoType.byName(MmceJsonUtil.optionalString(modifier, "io", "io-type", "ioType", "io_type")
                .orElse(""));
    }

    private static int readOperation(JsonObject modifier) {
        JsonElement element = firstPresent(modifier, "operation", "op");
        if (element == null || element.isJsonNull()) {
            return 0;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt();
        }
        String value = GsonHelper.convertToString(element, "operation").trim();
        if (value.equalsIgnoreCase("add") || value.equals("+")) {
            return 0;
        }
        if (value.equalsIgnoreCase("multiply") || value.equalsIgnoreCase("mul") || value.equals("*")) {
            return 1;
        }
        return Integer.parseInt(value);
    }

    private static double readMultiplier(JsonObject modifier) {
        for (String key : List.of("multiplier", "value", "modifier", "amount")) {
            if (modifier.has(key)) {
                return GsonHelper.getAsDouble(modifier, key, 1.0D);
            }
        }
        return 1.0D;
    }

    private static boolean readAffectChance(JsonObject modifier) {
        for (String key : List.of("affectChance", "affectsChance", "affect-chance", "affects-chance",
                "affect_chance", "affects_chance")) {
            if (modifier.has(key)) {
                return GsonHelper.getAsBoolean(modifier, key, false);
            }
        }
        return false;
    }

    private static JsonElement firstPresent(JsonObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key)) {
                return object.get(key);
            }
        }
        return null;
    }
}
