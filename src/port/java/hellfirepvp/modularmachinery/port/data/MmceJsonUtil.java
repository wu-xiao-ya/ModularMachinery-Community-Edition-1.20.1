package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

final class MmceJsonUtil {
    static ResourceLocation modId(String value) {
        return value.indexOf(':') >= 0
                ? ResourceLocation.parse(value)
                : ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, value);
    }

    static ResourceLocation id(String value) {
        return value.indexOf(':') >= 0
                ? ResourceLocation.parse(value)
                : ResourceLocation.withDefaultNamespace(value);
    }

    static ResourceLocation defaultId(String value) {
        return id(value);
    }

    static Optional<String> optionalString(JsonObject object, String key) {
        return object.has(key) ? Optional.of(GsonHelper.getAsString(object, key)) : Optional.empty();
    }

    static Optional<String> optionalString(JsonObject object, String firstKey, String... otherKeys) {
        Optional<String> first = optionalString(object, firstKey);
        if (first.isPresent()) {
            return first;
        }
        if (otherKeys != null) {
            for (String key : otherKeys) {
                Optional<String> value = optionalString(object, key);
                if (value.isPresent()) {
                    return value;
                }
            }
        }
        return Optional.empty();
    }

    static List<Integer> coordinates(JsonObject object, String key) {
        if (!object.has(key)) {
            return List.of(0);
        }

        JsonElement element = object.get(key);
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return List.of(element.getAsInt());
        }

        JsonArray array = GsonHelper.convertToJsonArray(element, key);
        List<Integer> coordinates = new ArrayList<>(array.size());
        for (JsonElement entry : array) {
            coordinates.add(GsonHelper.convertToInt(entry, key));
        }
        return List.copyOf(coordinates);
    }

    static List<String> stringOrArray(JsonObject object, String key) {
        if (!object.has(key)) {
            return List.of();
        }

        JsonElement element = object.get(key);
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return List.of(element.getAsString());
        }

        JsonArray array = GsonHelper.convertToJsonArray(element, key);
        List<String> out = new ArrayList<>(array.size());
        for (JsonElement entry : array) {
            out.add(GsonHelper.convertToString(entry, key));
        }
        return List.copyOf(out);
    }

    static JsonObject rawObject(JsonObject object, String firstKey, String... otherKeys) {
        if (object.has(firstKey)) {
            return GsonHelper.getAsJsonObject(object, firstKey).deepCopy();
        }
        if (otherKeys != null) {
            for (String key : otherKeys) {
                if (object.has(key)) {
                    return GsonHelper.getAsJsonObject(object, key).deepCopy();
                }
            }
        }
        return new JsonObject();
    }

    private MmceJsonUtil() {
    }
}
