package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;

public record MmceDynamicPatternDefinition(
        String name,
        Set<Direction> faces,
        int minSize,
        int maxSize,
        List<MmceStructurePart> parts,
        List<MmceStructurePart> partsEnd,
        BlockPos structureSizeOffsetStart,
        BlockPos structureSizeOffset,
        JsonObject rawJson
) {
    public static List<MmceDynamicPatternDefinition> parseMany(JsonArray array) {
        if (array.isEmpty()) {
            throw new IllegalArgumentException("Empty 'dynamic-patterns'.");
        }
        List<MmceDynamicPatternDefinition> parsed = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            parsed.add(parse(GsonHelper.convertToJsonObject(element, "dynamic-patterns[]")));
        }
        return List.copyOf(parsed);
    }

    private static MmceDynamicPatternDefinition parse(JsonObject object) {
        String name = GsonHelper.getAsString(object, "name", "").trim();
        if (name.isBlank()) {
            throw new IllegalArgumentException("Dynamic pattern requires a non-empty 'name'.");
        }
        int minSize = readInt(object, 0, "minSize", "min-size", "min_size");
        int maxSize = readInt(object, 1, "maxSize", "max-size", "max_size");
        if (minSize > maxSize) {
            throw new IllegalArgumentException("Dynamic pattern '" + name + "' has minSize greater than maxSize.");
        }

        return new MmceDynamicPatternDefinition(
                name,
                readFaces(object),
                Math.max(0, minSize),
                Math.max(0, maxSize),
                readParts(object, "parts"),
                hasAny(object, "parts-end", "partsEnd", "parts_end") ? readParts(object, "parts-end", "partsEnd", "parts_end") : List.of(),
                readOffset(object, "structure-size-offset-start", "structureSizeOffsetStart", "structure_size_offset_start"),
                readOffset(object, "structure-size-offset", "structureSizeOffset", "structure_size_offset"),
                object.deepCopy()
        );
    }

    private static Set<Direction> readFaces(JsonObject object) {
        JsonArray array = GsonHelper.getAsJsonArray(object, "faces");
        if (array.isEmpty()) {
            throw new IllegalArgumentException("Dynamic pattern 'faces' cannot be empty.");
        }
        Set<Direction> faces = new LinkedHashSet<>();
        for (JsonElement element : array) {
            String value = GsonHelper.convertToString(element, "faces[]").trim().toUpperCase(Locale.ROOT);
            try {
                faces.add(Direction.valueOf(value));
            } catch (IllegalArgumentException ignored) {
                throw new IllegalArgumentException("Invalid dynamic pattern face: " + value.toLowerCase(Locale.ROOT));
            }
        }
        return Set.copyOf(faces);
    }

    private static List<MmceStructurePart> readParts(JsonObject object, String firstKey, String... otherKeys) {
        String key = firstPresentKey(object, firstKey, otherKeys);
        JsonArray array = GsonHelper.getAsJsonArray(object, key);
        if (array.isEmpty()) {
            throw new IllegalArgumentException("Dynamic pattern '" + key + "' cannot be empty.");
        }
        List<MmceStructurePart> parts = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            parts.add(MmceStructurePart.parse(GsonHelper.convertToJsonObject(element, key + "[]")));
        }
        return List.copyOf(parts);
    }

    private static BlockPos readOffset(JsonObject object, String firstKey, String... otherKeys) {
        String key = firstPresentKey(object, firstKey, otherKeys);
        if (key == null) {
            return BlockPos.ZERO;
        }
        JsonObject offset = GsonHelper.getAsJsonObject(object, key);
        return new BlockPos(
                firstCoordinate(offset, "x"),
                firstCoordinate(offset, "y"),
                firstCoordinate(offset, "z")
        );
    }

    private static int firstCoordinate(JsonObject object, String key) {
        return MmceJsonUtil.coordinates(object, key).getFirst();
    }

    private static int readInt(JsonObject object, int fallback, String... keys) {
        for (String key : keys) {
            if (object.has(key)) {
                return GsonHelper.getAsInt(object, key, fallback);
            }
        }
        return fallback;
    }

    private static boolean hasAny(JsonObject object, String firstKey, String... otherKeys) {
        return firstPresentKey(object, firstKey, otherKeys) != null;
    }

    private static String firstPresentKey(JsonObject object, String firstKey, String... otherKeys) {
        if (object.has(firstKey)) {
            return firstKey;
        }
        if (otherKeys != null) {
            for (String key : otherKeys) {
                if (object.has(key)) {
                    return key;
                }
            }
        }
        return null;
    }
}
