package hellfirepvp.modularmachinery.port.integration.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import hellfirepvp.modularmachinery.port.integration.MmceBlockChecker;
import hellfirepvp.modularmachinery.port.integration.MmceBlockCheckerRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceScriptValues;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class MmceKubeJSBlockArrayBuilder {
    private final JsonArray parts;
    private final Consumer<JsonObject> lastPartSink;
    private JsonObject lastPart;

    private MmceKubeJSBlockArrayBuilder(JsonArray parts, Consumer<JsonObject> lastPartSink) {
        this.parts = parts;
        this.lastPartSink = lastPartSink;
    }

    public static MmceKubeJSBlockArrayBuilder newBuilder() {
        return new MmceKubeJSBlockArrayBuilder(new JsonArray(), ignored -> {
        });
    }

    public static MmceKubeJSBlockArrayBuilder newBuilder(MmceKubeJSBlockArrayBuilder blockArray) {
        JsonArray copy = blockArray == null ? new JsonArray() : blockArray.rawBlockArray();
        return new MmceKubeJSBlockArrayBuilder(copy, ignored -> {
        });
    }

    static MmceKubeJSBlockArrayBuilder wrap(JsonArray parts, Consumer<JsonObject> lastPartSink) {
        return new MmceKubeJSBlockArrayBuilder(parts, lastPartSink);
    }

    public MmceKubeJSBlockArrayBuilder addBlock(Object x, Object y, Object z, Object elements) {
        JsonObject part = new JsonObject();
        part.add("x", scalarOrArray(x));
        part.add("y", scalarOrArray(y));
        part.add("z", scalarOrArray(z));
        part.add("elements", blockElements(elements));
        addPart(part);
        return this;
    }

    public MmceKubeJSBlockArrayBuilder addBlock(Object x, Object y, Object z, Object elements, Object nbt, Object previewNbt) {
        addBlock(x, y, z, elements);
        setNBT(nbt);
        setPreviewNBT(previewNbt);
        return this;
    }

    public MmceKubeJSBlockArrayBuilder addBlock(Object x, Object y, Object z, Object elements, Object nbt,
                                                Object previewNbt, MmceBlockChecker checker) {
        addBlock(x, y, z, elements, nbt, previewNbt);
        setBlockChecker(checker);
        return this;
    }

    public MmceKubeJSBlockArrayBuilder addBlock(Object x, Object y, Object z, Object elements, MmceBlockChecker checker) {
        addBlock(x, y, z, elements);
        setBlockChecker(checker);
        return this;
    }

    public MmceKubeJSBlockArrayBuilder addBlock(Object x, Object y, Object z, MmceBlockChecker checker, Object elements) {
        return addBlock(x, y, z, elements, checker);
    }

    public MmceKubeJSBlockArrayBuilder block(Object x, Object y, Object z, Object elements) {
        return addBlock(x, y, z, elements);
    }

    public MmceKubeJSBlockArrayBuilder part(Object x, Object y, Object z, Object elements) {
        return addBlock(x, y, z, elements);
    }

    public MmceKubeJSBlockArrayBuilder addPart(Object x, Object y, Object z, Object elements) {
        return addBlock(x, y, z, elements);
    }

    public MmceKubeJSBlockArrayBuilder setNBT(Object value) {
        setNbt("nbt", value);
        return this;
    }

    public MmceKubeJSBlockArrayBuilder setNbt(Object value) {
        return setNBT(value);
    }

    public MmceKubeJSBlockArrayBuilder nbt(Object value) {
        return setNBT(value);
    }

    public MmceKubeJSBlockArrayBuilder partNbt(Object value) {
        return setNBT(value);
    }

    public MmceKubeJSBlockArrayBuilder setPartNbt(Object value) {
        return setNBT(value);
    }

    public MmceKubeJSBlockArrayBuilder setPreviewNBT(Object value) {
        setNbt("preview-nbt", value);
        return this;
    }

    public MmceKubeJSBlockArrayBuilder setPreviewNbt(Object value) {
        return setPreviewNBT(value);
    }

    public MmceKubeJSBlockArrayBuilder setPreViewNBT(Object value) {
        return setPreviewNBT(value);
    }

    public MmceKubeJSBlockArrayBuilder previewNbt(Object value) {
        return setPreviewNBT(value);
    }

    public MmceKubeJSBlockArrayBuilder partPreviewNbt(Object value) {
        return setPreviewNBT(value);
    }

    public MmceKubeJSBlockArrayBuilder setPartPreviewNbt(Object value) {
        return setPreviewNBT(value);
    }

    public MmceKubeJSBlockArrayBuilder setTag(String tag) {
        if (lastPart != null && tag != null && !tag.isBlank()) {
            lastPart.addProperty("selector-tag", tag);
        }
        return this;
    }

    public MmceKubeJSBlockArrayBuilder partTag(String tag) {
        return setTag(tag);
    }

    public MmceKubeJSBlockArrayBuilder selectorTag(String tag) {
        return setTag(tag);
    }

    public MmceKubeJSBlockArrayBuilder selector_tag(String tag) {
        return setTag(tag);
    }

    public MmceKubeJSBlockArrayBuilder part_tag(String tag) {
        return setTag(tag);
    }

    public MmceKubeJSBlockArrayBuilder setSelectorTag(String tag) {
        return setTag(tag);
    }

    public MmceKubeJSBlockArrayBuilder setBlockChecker(MmceBlockChecker checker) {
        return setBlockChecker("", checker);
    }

    public MmceKubeJSBlockArrayBuilder partChecker(MmceBlockChecker checker) {
        return setBlockChecker(checker);
    }

    public MmceKubeJSBlockArrayBuilder setPartChecker(MmceBlockChecker checker) {
        return setBlockChecker(checker);
    }

    public MmceKubeJSBlockArrayBuilder setBlockChecker(String checkerId, MmceBlockChecker checker) {
        if (lastPart != null && checker != null) {
            lastPart.addProperty("checker-id", MmceBlockCheckerRegistry.register(checkerId, checker));
        }
        return this;
    }

    public MmceKubeJSBlockArrayBuilder partChecker(String checkerId, MmceBlockChecker checker) {
        return setBlockChecker(checkerId, checker);
    }

    public MmceKubeJSBlockArrayBuilder setPartChecker(String checkerId, MmceBlockChecker checker) {
        return setBlockChecker(checkerId, checker);
    }

    public JsonArray getBlockArray() {
        return parts.deepCopy();
    }

    JsonArray rawBlockArray() {
        return parts.deepCopy();
    }

    private void addPart(JsonObject part) {
        parts.add(part);
        lastPart = part;
        lastPartSink.accept(part);
    }

    private void setNbt(String key, Object value) {
        if (lastPart == null || value == null) {
            return;
        }
        jsonObject(value).ifPresent(object -> lastPart.add(key, object));
    }

    private static JsonElement scalarOrArray(Object value) {
        if (value instanceof Number number) {
            return new JsonPrimitive(number.intValue());
        }
        Optional<Integer> intValue = intFrom(value);
        if (intValue.isPresent()) {
            return new JsonPrimitive(intValue.get());
        }
        return arrayFrom(value, true);
    }

    private static JsonArray blockElements(Object value) {
        JsonArray array = new JsonArray();
        for (String element : MmceScriptValues.blockElements(value)) {
            array.add(element);
        }
        return array;
    }

    private static JsonArray arrayFrom(Object value, boolean numeric) {
        JsonArray array = new JsonArray();
        if (value instanceof JsonArray jsonArray) {
            for (JsonElement entry : jsonArray) {
                addValue(array, entry, numeric);
            }
        } else if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int index = 0; index < length; index++) {
                addValue(array, Array.get(value, index), numeric);
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
        if (value instanceof JsonElement element) {
            if (numeric && element.isJsonPrimitive()) {
                Optional<Integer> intValue = intFrom(element);
                if (intValue.isPresent()) {
                    array.add(intValue.get());
                    return;
                }
            }
            array.add(element.deepCopy());
            return;
        }
        if (numeric) {
            Optional<Integer> intValue = intFrom(value);
            if (intValue.isPresent()) {
                array.add(intValue.get());
                return;
            }
        }
        if (value != null) {
            array.add(value.toString());
        }
    }

    private static Optional<Integer> intFrom(Object value) {
        if (value instanceof Number number) {
            return Optional.of(number.intValue());
        }
        if (value instanceof JsonElement element && element.isJsonPrimitive()) {
            try {
                return Optional.of(element.getAsInt());
            } catch (RuntimeException ignored) {
                return Optional.empty();
            }
        }
        if (value instanceof CharSequence sequence) {
            try {
                return Optional.of(Integer.parseInt(sequence.toString().trim()));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static Optional<JsonObject> jsonObject(Object value) {
        if (value instanceof JsonObject object) {
            return Optional.of(object.deepCopy());
        }
        if (value instanceof JsonElement element && element.isJsonObject()) {
            return Optional.of(element.getAsJsonObject().deepCopy());
        }
        if (value instanceof Map<?, ?> map) {
            return Optional.of(mapToJson(map));
        }
        Optional<Object> nested = invoke(value, "toJson", "asJson", "getJson");
        if (nested.isPresent() && nested.get() != value) {
            Optional<JsonObject> object = jsonObject(nested.get());
            if (object.isPresent()) {
                return object;
            }
        }
        if (value instanceof CharSequence sequence) {
            String json = sequence.toString().trim();
            if (json.isEmpty()) {
                return Optional.empty();
            }
            JsonElement parsed = JsonParser.parseString(json);
            return parsed.isJsonObject() ? Optional.of(parsed.getAsJsonObject().deepCopy()) : Optional.empty();
        }
        String string = String.valueOf(value).trim();
        if (string.startsWith("{") && string.endsWith("}")) {
            JsonElement parsed = JsonParser.parseString(string);
            return parsed.isJsonObject() ? Optional.of(parsed.getAsJsonObject().deepCopy()) : Optional.empty();
        }
        return Optional.empty();
    }

    private static JsonObject mapToJson(Map<?, ?> map) {
        JsonObject object = new JsonObject();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                object.add(String.valueOf(entry.getKey()), jsonElement(entry.getValue()));
            }
        }
        return object;
    }

    private static JsonElement jsonElement(Object value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        if (value instanceof JsonElement element) {
            return element.deepCopy();
        }
        if (value instanceof Map<?, ?> map) {
            return mapToJson(map);
        }
        if (value instanceof Iterable<?> iterable) {
            JsonArray array = new JsonArray();
            for (Object entry : iterable) {
                array.add(jsonElement(entry));
            }
            return array;
        }
        if (value.getClass().isArray()) {
            JsonArray array = new JsonArray();
            int length = Array.getLength(value);
            for (int index = 0; index < length; index++) {
                array.add(jsonElement(Array.get(value, index)));
            }
            return array;
        }
        if (value instanceof Number number) {
            return new JsonPrimitive(number);
        }
        if (value instanceof Boolean bool) {
            return new JsonPrimitive(bool);
        }
        return new JsonPrimitive(String.valueOf(value));
    }

    private static Optional<Object> invoke(Object target, String... names) {
        if (target == null) {
            return Optional.empty();
        }
        for (String name : names) {
            try {
                Method method = target.getClass().getMethod(name);
                method.setAccessible(true);
                return Optional.ofNullable(method.invoke(target));
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return Optional.empty();
    }
}
