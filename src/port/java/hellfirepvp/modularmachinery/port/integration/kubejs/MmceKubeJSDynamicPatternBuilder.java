package hellfirepvp.modularmachinery.port.integration.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.integration.MmceBlockChecker;
import hellfirepvp.modularmachinery.port.integration.MmceBlockCheckerRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceScriptValues;
import java.lang.reflect.Array;
import java.util.List;

public final class MmceKubeJSDynamicPatternBuilder {
    private final JsonObject root = new JsonObject();
    private final JsonArray parts = new JsonArray();
    private JsonArray partsEnd;
    private JsonObject lastPart;

    MmceKubeJSDynamicPatternBuilder(String name) {
        root.addProperty("name", name == null || name.isBlank() ? "dynamic" : name.trim());
        root.add("parts", parts);
    }

    public MmceKubeJSDynamicPatternBuilder faces(Object faces) {
        root.add("faces", stringArray(faces));
        return this;
    }

    public MmceKubeJSDynamicPatternBuilder faces(String... faces) {
        return faces((Object) faces);
    }

    public MmceKubeJSDynamicPatternBuilder setFaces(Object faces) {
        return faces(faces);
    }

    public MmceKubeJSDynamicPatternBuilder minSize(int value) {
        root.addProperty("minSize", Math.max(0, value));
        return this;
    }

    public MmceKubeJSDynamicPatternBuilder setMinSize(int value) {
        return minSize(value);
    }

    public MmceKubeJSDynamicPatternBuilder maxSize(int value) {
        root.addProperty("maxSize", Math.max(0, value));
        return this;
    }

    public MmceKubeJSDynamicPatternBuilder setMaxSize(int value) {
        return maxSize(value);
    }

    public MmceKubeJSDynamicPatternBuilder structureSizeOffsetStart(int x, int y, int z) {
        root.add("structure-size-offset-start", offset(x, y, z));
        return this;
    }

    public MmceKubeJSDynamicPatternBuilder setStructureSizeOffsetStart(int x, int y, int z) {
        return structureSizeOffsetStart(x, y, z);
    }

    public MmceKubeJSDynamicPatternBuilder structure_size_offset_start(int x, int y, int z) {
        return structureSizeOffsetStart(x, y, z);
    }

    public MmceKubeJSDynamicPatternBuilder structureSizeOffset(int x, int y, int z) {
        root.add("structure-size-offset", offset(x, y, z));
        return this;
    }

    public MmceKubeJSDynamicPatternBuilder setStructureSizeOffset(int x, int y, int z) {
        return structureSizeOffset(x, y, z);
    }

    public MmceKubeJSDynamicPatternBuilder structure_size_offset(int x, int y, int z) {
        return structureSizeOffset(x, y, z);
    }

    public MmceKubeJSDynamicPatternBuilder setParts(MmceKubeJSBlockArrayBuilder blockArray) {
        replaceArray(parts, blockArray == null ? new JsonArray() : blockArray.rawBlockArray());
        lastPart = null;
        return this;
    }

    public MmceKubeJSDynamicPatternBuilder parts(MmceKubeJSBlockArrayBuilder blockArray) {
        return setParts(blockArray);
    }

    public MmceKubeJSDynamicPatternBuilder setPartsEnd(MmceKubeJSBlockArrayBuilder blockArray) {
        partsEnd = blockArray == null ? new JsonArray() : blockArray.rawBlockArray();
        root.add("parts-end", partsEnd);
        lastPart = null;
        return this;
    }

    public MmceKubeJSDynamicPatternBuilder partsEnd(MmceKubeJSBlockArrayBuilder blockArray) {
        return setPartsEnd(blockArray);
    }

    public MmceKubeJSDynamicPatternBuilder parts_end(MmceKubeJSBlockArrayBuilder blockArray) {
        return setPartsEnd(blockArray);
    }

    public MmceKubeJSBlockArrayBuilder getPartsBuilder() {
        return MmceKubeJSBlockArrayBuilder.wrap(parts, this::setLastPart);
    }

    public MmceKubeJSBlockArrayBuilder getBlockArrayBuilder() {
        return getPartsBuilder();
    }

    public MmceKubeJSBlockArrayBuilder getBlockArray() {
        return getPartsBuilder();
    }

    public MmceKubeJSBlockArrayBuilder getPartsEndBuilder() {
        return MmceKubeJSBlockArrayBuilder.wrap(endParts(), this::setLastPart);
    }

    public MmceKubeJSBlockArrayBuilder getEndPartsBuilder() {
        return getPartsEndBuilder();
    }

    public MmceKubeJSDynamicPatternBuilder part(Object x, Object y, Object z, Object elements) {
        JsonObject part = new JsonObject();
        part.add("x", scalarOrArray(x));
        part.add("y", scalarOrArray(y));
        part.add("z", scalarOrArray(z));
        part.add("elements", blockElements(elements));
        parts.add(part);
        lastPart = part;
        return this;
    }

    public MmceKubeJSDynamicPatternBuilder addPart(Object x, Object y, Object z, Object elements) {
        return part(x, y, z, elements);
    }

    public MmceKubeJSDynamicPatternBuilder block(Object x, Object y, Object z, Object elements) {
        return part(x, y, z, elements);
    }

    public MmceKubeJSDynamicPatternBuilder endPart(Object x, Object y, Object z, Object elements) {
        JsonObject part = new JsonObject();
        part.add("x", scalarOrArray(x));
        part.add("y", scalarOrArray(y));
        part.add("z", scalarOrArray(z));
        part.add("elements", blockElements(elements));
        endParts().add(part);
        lastPart = part;
        return this;
    }

    public MmceKubeJSDynamicPatternBuilder addEndPart(Object x, Object y, Object z, Object elements) {
        return endPart(x, y, z, elements);
    }

    public MmceKubeJSDynamicPatternBuilder partsEnd(Object x, Object y, Object z, Object elements) {
        return endPart(x, y, z, elements);
    }

    public MmceKubeJSDynamicPatternBuilder parts_end(Object x, Object y, Object z, Object elements) {
        return endPart(x, y, z, elements);
    }

    public MmceKubeJSDynamicPatternBuilder partNbt(String json) {
        setLastPartNbt("nbt", json);
        return this;
    }

    public MmceKubeJSDynamicPatternBuilder partPreviewNbt(String json) {
        setLastPartNbt("preview-nbt", json);
        return this;
    }

    public MmceKubeJSDynamicPatternBuilder partTag(String tag) {
        if (lastPart != null && tag != null && !tag.isBlank()) {
            lastPart.addProperty("selector-tag", tag);
        }
        return this;
    }

    public MmceKubeJSDynamicPatternBuilder setPartTag(String tag) {
        return partTag(tag);
    }

    public MmceKubeJSDynamicPatternBuilder selectorTag(String tag) {
        return partTag(tag);
    }

    public MmceKubeJSDynamicPatternBuilder selector_tag(String tag) {
        return partTag(tag);
    }

    public MmceKubeJSDynamicPatternBuilder part_tag(String tag) {
        return partTag(tag);
    }

    public MmceKubeJSDynamicPatternBuilder partChecker(MmceBlockChecker checker) {
        return partChecker("", checker);
    }

    public MmceKubeJSDynamicPatternBuilder partChecker(String checkerId, MmceBlockChecker checker) {
        if (lastPart != null && checker != null) {
            lastPart.addProperty("checker-id", MmceBlockCheckerRegistry.register(checkerId, checker));
        }
        return this;
    }

    public JsonObject json() {
        return root.deepCopy();
    }

    private JsonArray endParts() {
        if (partsEnd == null) {
            partsEnd = new JsonArray();
            root.add("parts-end", partsEnd);
        }
        return partsEnd;
    }

    private void setLastPart(JsonObject part) {
        lastPart = part;
    }

    private void setLastPartNbt(String key, String json) {
        if (lastPart != null && json != null && !json.isBlank()) {
            lastPart.add(key, com.google.gson.JsonParser.parseString(json).getAsJsonObject());
        }
    }

    private static JsonObject offset(int x, int y, int z) {
        JsonObject object = new JsonObject();
        object.addProperty("x", x);
        object.addProperty("y", y);
        object.addProperty("z", z);
        return object;
    }

    private static JsonElement scalarOrArray(Object value) {
        if (value instanceof Number number) {
            return new com.google.gson.JsonPrimitive(number.intValue());
        }
        return arrayFrom(value, true);
    }

    private static JsonElement stringOrArray(Object value) {
        if (value instanceof CharSequence sequence) {
            return new com.google.gson.JsonPrimitive(sequence.toString());
        }
        return arrayFrom(value, false);
    }

    private static JsonArray stringArray(Object value) {
        JsonElement element = stringOrArray(value);
        if (element instanceof JsonArray array) {
            return array;
        }
        JsonArray array = new JsonArray();
        array.add(element);
        return array;
    }

    private static JsonElement blockElements(Object value) {
        List<String> elements = MmceScriptValues.blockElements(value);
        if (elements.size() == 1) {
            return new com.google.gson.JsonPrimitive(elements.getFirst());
        }
        JsonArray array = new JsonArray();
        for (String element : elements) {
            array.add(element);
        }
        return array;
    }

    private static JsonArray arrayFrom(Object value, boolean numeric) {
        JsonArray array = new JsonArray();
        if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                addValue(array, Array.get(value, i), numeric);
            }
        } else if (value instanceof Iterable<?> iterable) {
            for (Object entry : iterable) {
                addValue(array, entry, numeric);
            }
        } else {
            addValue(array, value, numeric);
        }
        return array;
    }

    private static void addValue(JsonArray array, Object value, boolean numeric) {
        if (numeric && value instanceof Number number) {
            array.add(number.intValue());
        } else if (value != null) {
            array.add(value.toString());
        }
    }

    private static void replaceArray(JsonArray target, JsonArray source) {
        target.asList().clear();
        for (int index = 0; index < source.size(); index++) {
            target.add(source.get(index).deepCopy());
        }
    }
}
