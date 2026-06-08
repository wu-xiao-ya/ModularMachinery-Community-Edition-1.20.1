package hellfirepvp.modularmachinery.port.integration.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.data.MmceScriptDataRegistry;
import hellfirepvp.modularmachinery.port.event.MmceEventPhase;
import hellfirepvp.modularmachinery.port.event.MmceEventRegistry;
import hellfirepvp.modularmachinery.port.event.MmceMachineEventHandler;
import hellfirepvp.modularmachinery.port.event.MmceMachineEventType;
import hellfirepvp.modularmachinery.port.integration.MmceBlockChecker;
import hellfirepvp.modularmachinery.port.integration.MmceBlockCheckerRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.integration.MmceScriptValues;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class MmceKubeJSMachineBuilder {
    private static final Map<ResourceLocation, MmceKubeJSMachineBuilder> PRE_LOAD_MACHINES = new LinkedHashMap<>();

    private final String registryName;
    private final JsonObject root = new JsonObject();
    private final JsonArray parts = new JsonArray();
    private JsonObject lastPart;

    MmceKubeJSMachineBuilder(String registryName, String localizedName) {
        this.registryName = registryName;
        root.addProperty("registryname", registryName);
        root.addProperty("localizedname", localizedName == null || localizedName.isBlank() ? registryName : localizedName);
        root.add("parts", parts);
    }

    public static MmceKubeJSMachineBuilder newBuilder(String registryName) {
        return new MmceKubeJSMachineBuilder(registryName, registryName);
    }

    public static MmceKubeJSMachineBuilder newBuilder(String registryName, String localizedName) {
        return new MmceKubeJSMachineBuilder(registryName, localizedName);
    }

    public static MmceKubeJSMachineBuilder registerMachine(String registryName) {
        return registerMachine(registryName, registryName);
    }

    public static MmceKubeJSMachineBuilder registerMachine(String registryName, String localizedName) {
        return registerMachineInternal(new MmceKubeJSMachineBuilder(registryName, localizedName));
    }

    public static MmceKubeJSMachineBuilder registerMachine(String registryName, String localizedName,
                                                           boolean hasFactory, boolean factoryOnly) {
        MmceKubeJSMachineBuilder builder = new MmceKubeJSMachineBuilder(registryName, localizedName)
                .hasFactory(hasFactory)
                .factoryOnly(factoryOnly);
        return registerMachineInternal(builder);
    }

    public static MmceKubeJSMachineBuilder registerMachine(String registryName, String localizedName,
                                                           boolean requiresBlueprint,
                                                           MmceKubeJSRecipeFailureActions failureAction,
                                                           int color) {
        return registerMachine(registryName, localizedName, requiresBlueprint,
                failureAction == null ? null : failureAction.getName(), color);
    }

    public static MmceKubeJSMachineBuilder registerMachine(String registryName, String localizedName,
                                                           boolean requiresBlueprint, String failureAction,
                                                           int color) {
        MmceKubeJSMachineBuilder builder = new MmceKubeJSMachineBuilder(registryName, localizedName)
                .requiresBlueprint(requiresBlueprint)
                .setFailureAction(failureAction)
                .setColor(color);
        return registerMachineInternal(builder);
    }

    public static MmceKubeJSMachineBuilder registerMachine(String registryName, String localizedName,
                                                           boolean requiresBlueprint,
                                                           MmceKubeJSRecipeFailureActions failureAction,
                                                           int color, boolean hasFactory, boolean factoryOnly) {
        return registerMachine(registryName, localizedName, requiresBlueprint,
                failureAction == null ? null : failureAction.getName(), color, hasFactory, factoryOnly);
    }

    public static MmceKubeJSMachineBuilder registerMachine(String registryName, String localizedName,
                                                           boolean requiresBlueprint, String failureAction,
                                                           int color, boolean hasFactory, boolean factoryOnly) {
        MmceKubeJSMachineBuilder builder = new MmceKubeJSMachineBuilder(registryName, localizedName)
                .requiresBlueprint(requiresBlueprint)
                .setFailureAction(failureAction)
                .setColor(color)
                .hasFactory(hasFactory)
                .factoryOnly(factoryOnly);
        return registerMachineInternal(builder);
    }

    public static MmceKubeJSMachineBuilder getBuilder(String registryName) {
        return PRE_LOAD_MACHINES.get(machineKey(registryName));
    }

    static void clearPreLoadMachines() {
        PRE_LOAD_MACHINES.clear();
    }

    public MmceKubeJSMachineBuilder part(Object x, Object y, Object z, Object elements) {
        JsonObject part = new JsonObject();
        part.add("x", scalarOrArray(x));
        part.add("y", scalarOrArray(y));
        part.add("z", scalarOrArray(z));
        part.add("elements", blockElements(elements));
        addPart(part);
        return this;
    }

    public MmceKubeJSMachineBuilder part(Object x, Object y, Object z, Object elements, String nbtJson, String previewNbtJson) {
        part(x, y, z, elements);
        partNbt(nbtJson);
        partPreviewNbt(previewNbtJson);
        return this;
    }

    public MmceKubeJSMachineBuilder partArray(Object x, Object y, Object z, Object elements) {
        return part(x, y, z, elements);
    }

    public MmceKubeJSMachineBuilder addBlock(Object x, Object y, Object z, Object elements) {
        return part(x, y, z, elements);
    }

    public MmceKubeJSMachineBuilder addPart(Object x, Object y, Object z, Object elements) {
        return part(x, y, z, elements);
    }

    public MmceKubeJSMachineBuilder block(Object x, Object y, Object z, Object elements) {
        return part(x, y, z, elements);
    }

    public MmceKubeJSMachineBuilder setParts(MmceKubeJSBlockArrayBuilder blockArray) {
        replaceArray(parts, blockArray == null ? new JsonArray() : blockArray.rawBlockArray());
        lastPart = null;
        return this;
    }

    public MmceKubeJSMachineBuilder parts(MmceKubeJSBlockArrayBuilder blockArray) {
        return setParts(blockArray);
    }

    public MmceKubeJSBlockArrayBuilder getBlockArrayBuilder() {
        return MmceKubeJSBlockArrayBuilder.wrap(parts, this::setLastPart);
    }

    public MmceKubeJSBlockArrayBuilder getBlockArray() {
        return getBlockArrayBuilder();
    }

    public MmceKubeJSMachineBuilder partNbt(String json) {
        setLastPartNbt("nbt", json);
        return this;
    }

    public MmceKubeJSMachineBuilder nbt(String json) {
        return partNbt(json);
    }

    public MmceKubeJSMachineBuilder setPartNbt(String json) {
        return partNbt(json);
    }

    public MmceKubeJSMachineBuilder setNBT(String json) {
        return partNbt(json);
    }

    public MmceKubeJSMachineBuilder setNbt(String json) {
        return partNbt(json);
    }

    public MmceKubeJSMachineBuilder partPreviewNbt(String json) {
        setLastPartNbt("preview-nbt", json);
        return this;
    }

    public MmceKubeJSMachineBuilder previewNbt(String json) {
        return partPreviewNbt(json);
    }

    public MmceKubeJSMachineBuilder setPartPreviewNbt(String json) {
        return partPreviewNbt(json);
    }

    public MmceKubeJSMachineBuilder setPreviewNBT(String json) {
        return partPreviewNbt(json);
    }

    public MmceKubeJSMachineBuilder setPreviewNbt(String json) {
        return partPreviewNbt(json);
    }

    public MmceKubeJSMachineBuilder setPreViewNBT(String json) {
        return partPreviewNbt(json);
    }

    public MmceKubeJSMachineBuilder partTag(String tag) {
        if (lastPart != null && tag != null && !tag.isBlank()) {
            lastPart.addProperty("selector-tag", tag);
        }
        return this;
    }

    public MmceKubeJSMachineBuilder setTag(String tag) {
        return partTag(tag);
    }

    public MmceKubeJSMachineBuilder setPartTag(String tag) {
        return partTag(tag);
    }

    public MmceKubeJSMachineBuilder selectorTag(String tag) {
        return partTag(tag);
    }

    public MmceKubeJSMachineBuilder selector_tag(String tag) {
        return partTag(tag);
    }

    public MmceKubeJSMachineBuilder part_tag(String tag) {
        return partTag(tag);
    }

    public MmceKubeJSMachineBuilder partChecker(MmceBlockChecker checker) {
        return partChecker("", checker);
    }

    public MmceKubeJSMachineBuilder partChecker(String checkerId, MmceBlockChecker checker) {
        if (lastPart != null && checker != null) {
            lastPart.addProperty("checker-id", MmceBlockCheckerRegistry.register(checkerId, checker));
        }
        return this;
    }

    public MmceKubeJSMachineBuilder setPartChecker(MmceBlockChecker checker) {
        return partChecker(checker);
    }

    public MmceKubeJSMachineBuilder setBlockChecker(MmceBlockChecker checker) {
        return partChecker(checker);
    }

    public MmceKubeJSMachineBuilder setBlockChecker(String checkerId, MmceBlockChecker checker) {
        return partChecker(checkerId, checker);
    }

    public MmceKubeJSMachineBuilder requiresBlueprint(boolean value) {
        root.addProperty("requires-blueprint", value);
        return this;
    }

    public MmceKubeJSMachineBuilder setRequiresBlueprint(boolean value) {
        return requiresBlueprint(value);
    }

    public MmceKubeJSMachineBuilder hasFactory(boolean value) {
        root.addProperty("has-factory", value);
        return this;
    }

    public MmceKubeJSMachineBuilder setHasFactory(boolean value) {
        return hasFactory(value);
    }

    public MmceKubeJSMachineBuilder factoryOnly(boolean value) {
        root.addProperty("factory-only", value);
        return this;
    }

    public MmceKubeJSMachineBuilder setFactoryOnly(boolean value) {
        return factoryOnly(value);
    }

    public MmceKubeJSMachineBuilder setParallelizable(boolean value) {
        root.addProperty("parallelizable", value);
        return this;
    }

    public MmceKubeJSMachineBuilder setMaxParallelism(int value) {
        root.addProperty("maxParallelism", Math.max(1, value));
        return this;
    }

    public MmceKubeJSMachineBuilder setInternalParallelism(int value) {
        root.addProperty("internalParallelism", Math.max(0, value));
        return this;
    }

    public MmceKubeJSMachineBuilder setFailureAction(String failureAction) {
        if (failureAction != null && !failureAction.isBlank()) {
            root.addProperty("failure-action", failureAction.trim());
        }
        return this;
    }

    public MmceKubeJSMachineBuilder setFailureAction(MmceKubeJSRecipeFailureActions failureAction) {
        return failureAction == null ? this : setFailureAction(failureAction.getName());
    }

    public MmceKubeJSMachineBuilder failureAction(String failureAction) {
        return setFailureAction(failureAction);
    }

    public MmceKubeJSMachineBuilder failureAction(MmceKubeJSRecipeFailureActions failureAction) {
        return setFailureAction(failureAction);
    }

    public MmceKubeJSMachineBuilder setColor(int color) {
        root.addProperty("color", color);
        return this;
    }

    public MmceKubeJSMachineBuilder prefix(String prefixName) {
        root.addProperty("prefix", prefixName == null ? "" : prefixName);
        return this;
    }

    public MmceKubeJSMachineBuilder setPrefix(String prefixName) {
        return prefix(prefixName);
    }

    public MmceKubeJSMachineBuilder machinePrefix(String prefixName) {
        return prefix(prefixName);
    }

    public MmceKubeJSMachineBuilder geoModel(String modelName) {
        root.addProperty("geoModel", modelName == null ? "" : modelName);
        return this;
    }

    public MmceKubeJSMachineBuilder setGeoModel(String modelName) {
        return geoModel(modelName);
    }

    public MmceKubeJSMachineBuilder machineGeoModel(String modelName) {
        return geoModel(modelName);
    }

    public MmceKubeJSMachineBuilder setControllerBoundingBox(double minX, double minY, double minZ,
                                                             double maxX, double maxY, double maxZ) {
        JsonArray array = new JsonArray();
        array.add(minX);
        array.add(minY);
        array.add(minZ);
        array.add(maxX);
        array.add(maxY);
        array.add(maxZ);
        root.add("controller-bounding-box", array);
        return this;
    }

    public MmceKubeJSMachineBuilder setControllerBoundingBox(Object aabb) {
        List<Double> values = boundingBoxValues(aabb);
        if (values.size() == 6) {
            return setControllerBoundingBox(values.get(0), values.get(1), values.get(2),
                    values.get(3), values.get(4), values.get(5));
        }
        return this;
    }

    public MmceKubeJSMachineBuilder maxThreads(int value) {
        root.addProperty("maxThreads", Math.max(0, value));
        return this;
    }

    public MmceKubeJSMachineBuilder setMaxThreads(int value) {
        return maxThreads(value);
    }

    public MmceKubeJSMachineBuilder coreThread(String threadName) {
        if (threadName != null && !threadName.isBlank()) {
            coreThreads().add(threadName.trim());
        }
        return this;
    }

    public MmceKubeJSMachineBuilder addCoreThread(String threadName) {
        return coreThread(threadName);
    }

    public MmceKubeJSMachineBuilder addCoreThread(MmceKubeJSFactoryRecipeThreadBuilder thread) {
        if (thread != null) {
            coreThreads().add(thread.json());
        }
        return this;
    }

    public MmceKubeJSMachineBuilder coreThread(String threadName, String[] recipes) {
        return coreThread(threadName, (Object) recipes);
    }

    public MmceKubeJSMachineBuilder coreThread(String threadName, Object recipes) {
        if (threadName == null || threadName.isBlank()) {
            return this;
        }
        JsonObject object = new JsonObject();
        object.addProperty("threadName", threadName.trim());
        JsonArray recipeArray = stringArray(recipes);
        if (!recipeArray.isEmpty()) {
            object.add("recipes", recipeArray);
        }
        coreThreads().add(object);
        return this;
    }

    public MmceKubeJSMachineBuilder smartInterfaceType(String type, float defaultValue) {
        if (type == null || type.isBlank()) {
            return this;
        }
        JsonObject object = new JsonObject();
        object.addProperty("type", type.trim());
        object.addProperty("defaultValue", defaultValue);
        smartInterfaceTypes().add(object);
        return this;
    }

    public MmceKubeJSMachineBuilder addSmartInterfaceType(String type, float defaultValue) {
        return smartInterfaceType(type, defaultValue);
    }

    public MmceKubeJSMachineBuilder hideComponentsWhenFormed(boolean value) {
        root.addProperty("hide-components-when-formed", value);
        return this;
    }

    public MmceKubeJSMachineBuilder addDynamicPattern(MmceKubeJSDynamicPatternBuilder pattern) {
        if (pattern != null) {
            dynamicPatterns().add(pattern.json());
        }
        return this;
    }

    public MmceKubeJSMachineBuilder dynamicPattern(MmceKubeJSDynamicPatternBuilder pattern) {
        return addDynamicPattern(pattern);
    }

    public MmceKubeJSMachineBuilder addDynamicPatternJson(String json) {
        if (json != null && !json.isBlank()) {
            dynamicPatterns().add(com.google.gson.JsonParser.parseString(json).getAsJsonObject());
        }
        return this;
    }

    public MmceKubeJSMachineBuilder singleBlockModifier(Object x, Object y, Object z, Object elements, String description, MmceRecipeModifier... modifiers) {
        return singleBlockModifier(x, y, z, elements, description, "", "", modifiers);
    }

    public MmceKubeJSMachineBuilder singleBlockModifier(Object x, Object y, Object z, Object elements, String description,
                                                        String nbtJson, String previewNbtJson, MmceRecipeModifier... modifiers) {
        JsonObject entry = new JsonObject();
        entry.add("x", scalarOrArray(x));
        entry.add("y", scalarOrArray(y));
        entry.add("z", scalarOrArray(z));
        entry.add("elements", blockElements(elements));
        if (description != null && !description.isBlank()) {
            entry.addProperty("description", description);
        }
        addNbt(entry, nbtJson, previewNbtJson);
        addModifiers(entry, modifiers);
        modifiers().add(entry);
        return this;
    }

    public MmceKubeJSMachineBuilder addSingleBlockModifier(Object x, Object y, Object z, Object elements, String description, MmceRecipeModifier... modifiers) {
        return singleBlockModifier(x, y, z, elements, description, modifiers);
    }

    public MmceKubeJSMachineBuilder addMultiBlockModifier(MmceKubeJSMultiBlockModifierReplacement multiBlockModifier) {
        if (multiBlockModifier == null) {
            return this;
        }
        JsonArray entries = multiBlockModifier.entries();
        JsonArray target = modifiers();
        for (int index = 0; index < entries.size(); index++) {
            target.add(entries.get(index).deepCopy());
        }
        return this;
    }

    public MmceKubeJSMachineBuilder multiBlockModifier(MmceKubeJSMultiBlockModifierReplacement multiBlockModifier) {
        return addMultiBlockModifier(multiBlockModifier);
    }

    public MmceKubeJSMachineBuilder onStructureFormed(MmceMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.STRUCTURE_FORMED, null, handler);
    }

    public MmceKubeJSMachineBuilder onStructureUpdate(MmceMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.STRUCTURE_UPDATE, null, handler);
    }

    public MmceKubeJSMachineBuilder onPreTick(MmceMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.TICK, MmceEventPhase.START, handler);
    }

    public MmceKubeJSMachineBuilder onPostTick(MmceMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.TICK, MmceEventPhase.END, handler);
    }

    public MmceKubeJSMachineBuilder onTick(MmceMachineEventHandler handler) {
        return onPostTick(handler);
    }

    public MmceKubeJSMachineBuilder onSmartInterfaceUpdate(MmceMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.SMART_INTERFACE_UPDATE, null, handler);
    }

    public MmceKubeJSMachineBuilder onControllerButtonClick(MmceMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.CONTROLLER_BUTTON_CLICK, null, handler);
    }

    public MmceKubeJSMachineBuilder onGUIRender(MmceMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.CONTROLLER_GUI_RENDER, null, handler);
    }

    public MmceKubeJSMachineBuilder onControllerGUIRender(MmceMachineEventHandler handler) {
        return onGUIRender(handler);
    }

    public MmceKubeJSMachineBuilder addStructureFormedHandler(MmceMachineEventHandler handler) {
        return onStructureFormed(handler);
    }

    public MmceKubeJSMachineBuilder addStructureUpdateHandler(MmceMachineEventHandler handler) {
        return onStructureUpdate(handler);
    }

    public MmceKubeJSMachineBuilder addMachinePreTickHandler(MmceMachineEventHandler handler) {
        return onPreTick(handler);
    }

    public MmceKubeJSMachineBuilder addMachinePostTickHandler(MmceMachineEventHandler handler) {
        return onPostTick(handler);
    }

    public MmceKubeJSMachineBuilder addTickHandler(MmceMachineEventHandler handler) {
        return onTick(handler);
    }

    public MmceKubeJSMachineBuilder addSmartInterfaceUpdateHandler(MmceMachineEventHandler handler) {
        return onSmartInterfaceUpdate(handler);
    }

    public MmceKubeJSMachineBuilder addControllerButtonClickHandler(MmceMachineEventHandler handler) {
        return onControllerButtonClick(handler);
    }

    public MmceKubeJSMachineBuilder addGUIRenderHandler(MmceMachineEventHandler handler) {
        return onGUIRender(handler);
    }

    public MmceKubeJSMachineBuilder addControllerGUIRenderHandler(MmceMachineEventHandler handler) {
        return onControllerGUIRender(handler);
    }

    public JsonObject json() {
        return root.deepCopy();
    }

    public void build() {
        PRE_LOAD_MACHINES.remove(machineKey(registryName));
        MmceScriptDataRegistry.registerMachine(sourceId("machines", registryName), root);
    }

    private static MmceKubeJSMachineBuilder registerMachineInternal(MmceKubeJSMachineBuilder builder) {
        PRE_LOAD_MACHINES.putIfAbsent(machineKey(builder.registryName), builder);
        return PRE_LOAD_MACHINES.get(machineKey(builder.registryName));
    }

    private MmceKubeJSMachineBuilder machineHandler(MmceMachineEventType type, MmceEventPhase phase, MmceMachineEventHandler handler) {
        if (handler != null) {
            MmceEventRegistry.registerMachine(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(registryName), type, event -> {
                if (phase == null || event.getPhase() == phase) {
                    handler.handle(event);
                }
            });
        }
        return this;
    }

    static ResourceLocation sourceId(String directory, String id) {
        String path = directory + "/" + id.toLowerCase(Locale.ROOT).replace(':', '/').replaceAll("[^a-z0-9_./-]", "_");
        return ResourceLocation.fromNamespaceAndPath("kubejs", path);
    }

    private static ResourceLocation machineKey(String registryName) {
        String id = registryName == null || registryName.isBlank() ? "unknown" : registryName.trim();
        return id.indexOf(':') >= 0
                ? ResourceLocation.parse(id)
                : ResourceLocation.fromNamespaceAndPath(hellfirepvp.modularmachinery.port.ModularMachineryNeoForge.MODID, id);
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

    private static List<Double> boundingBoxValues(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof JsonArray array) {
            List<Double> values = new ArrayList<>(array.size());
            for (JsonElement element : array) {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                    values.add(element.getAsDouble());
                }
            }
            return values;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Double> values = new ArrayList<>();
            for (Object entry : iterable) {
                if (entry instanceof Number number) {
                    values.add(number.doubleValue());
                }
            }
            return values;
        }
        if (value.getClass().isArray()) {
            List<Double> values = new ArrayList<>(Array.getLength(value));
            for (int i = 0; i < Array.getLength(value); i++) {
                Object entry = Array.get(value, i);
                if (entry instanceof Number number) {
                    values.add(number.doubleValue());
                }
            }
            return values;
        }

        List<Double> values = new ArrayList<>(6);
        for (String name : List.of("minX", "minY", "minZ", "maxX", "maxY", "maxZ")) {
            Double coordinate = doubleMember(value, name)
                    .or(() -> doubleMember(value, "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1)))
                    .orElse(null);
            if (coordinate == null) {
                return List.of();
            }
            values.add(coordinate);
        }
        return values;
    }

    private static java.util.Optional<Double> doubleMember(Object target, String name) {
        try {
            Method method = target.getClass().getMethod(name);
            method.setAccessible(true);
            Object value = method.invoke(target);
            return value instanceof Number number ? java.util.Optional.of(number.doubleValue()) : java.util.Optional.empty();
        } catch (ReflectiveOperationException ignored) {
            return java.util.Optional.empty();
        }
    }

    private JsonArray coreThreads() {
        if (root.has("coreThreads") && root.get("coreThreads").isJsonArray()) {
            return root.getAsJsonArray("coreThreads");
        }
        JsonArray array = new JsonArray();
        root.add("coreThreads", array);
        return array;
    }

    private void addPart(JsonObject part) {
        parts.add(part);
        lastPart = part;
    }

    private void setLastPart(JsonObject part) {
        lastPart = part;
    }

    private void setLastPartNbt(String key, String json) {
        if (lastPart != null && json != null && !json.isBlank()) {
            lastPart.add(key, com.google.gson.JsonParser.parseString(json).getAsJsonObject());
        }
    }

    private JsonArray smartInterfaceTypes() {
        if (root.has("smartInterfaceTypes") && root.get("smartInterfaceTypes").isJsonArray()) {
            return root.getAsJsonArray("smartInterfaceTypes");
        }
        JsonArray array = new JsonArray();
        root.add("smartInterfaceTypes", array);
        return array;
    }

    private JsonArray modifiers() {
        if (root.has("modifiers") && root.get("modifiers").isJsonArray()) {
            return root.getAsJsonArray("modifiers");
        }
        JsonArray array = new JsonArray();
        root.add("modifiers", array);
        return array;
    }

    private JsonArray dynamicPatterns() {
        if (root.has("dynamic-patterns") && root.get("dynamic-patterns").isJsonArray()) {
            return root.getAsJsonArray("dynamic-patterns");
        }
        JsonArray array = new JsonArray();
        root.add("dynamic-patterns", array);
        return array;
    }

    private static void addModifiers(JsonObject entry, MmceRecipeModifier[] modifiers) {
        if (modifiers == null || modifiers.length == 0) {
            return;
        }
        if (modifiers.length == 1) {
            if (modifiers[0] != null) {
                entry.add("modifier", modifiers[0].json());
            }
            return;
        }
        JsonArray array = new JsonArray();
        for (MmceRecipeModifier modifier : modifiers) {
            if (modifier != null) {
                array.add(modifier.json());
            }
        }
        entry.add("modifiers", array);
    }

    private static void addNbt(JsonObject entry, String nbtJson, String previewNbtJson) {
        if (nbtJson != null && !nbtJson.isBlank()) {
            entry.add("nbt", com.google.gson.JsonParser.parseString(nbtJson).getAsJsonObject());
        }
        if (previewNbtJson != null && !previewNbtJson.isBlank()) {
            entry.add("preview-nbt", com.google.gson.JsonParser.parseString(previewNbtJson).getAsJsonObject());
        }
    }

    private static void replaceArray(JsonArray target, JsonArray source) {
        target.asList().clear();
        for (int index = 0; index < source.size(); index++) {
            target.add(source.get(index).deepCopy());
        }
    }
}
