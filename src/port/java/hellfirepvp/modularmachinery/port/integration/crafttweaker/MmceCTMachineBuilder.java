package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.visitor.DataToJsonStringVisitor;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.data.MmceScriptDataRegistry;
import hellfirepvp.modularmachinery.port.event.MmceEventPhase;
import hellfirepvp.modularmachinery.port.event.MmceEventRegistry;
import hellfirepvp.modularmachinery.port.event.MmceMachineEventType;
import hellfirepvp.modularmachinery.port.integration.MmceBlockChecker;
import hellfirepvp.modularmachinery.port.integration.MmceBlockCheckerRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceItemCallbackRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeHelper;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeRegistry;
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
import net.minecraft.world.level.block.state.BlockState;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineBuilder")
public final class MmceCTMachineBuilder {
    private static final Map<ResourceLocation, MmceCTMachineBuilder> PRE_LOAD_MACHINES = new LinkedHashMap<>();

    private final String registryName;
    private final JsonObject root = new JsonObject();
    private final JsonArray parts = new JsonArray();
    private JsonObject lastPart;

    private MmceCTMachineBuilder(String registryName, String localizedName) {
        this.registryName = registryName;
        root.addProperty("registryname", registryName);
        root.addProperty("localizedname", localizedName == null || localizedName.isBlank() ? registryName : localizedName);
        root.add("parts", parts);
    }

    @ZenCodeType.Method
    public static MmceCTMachineBuilder newBuilder(String registryName, String localizedName) {
        return new MmceCTMachineBuilder(registryName, localizedName);
    }

    @ZenCodeType.Method
    public static MmceCTMachineBuilder newBuilder(String registryName) {
        return new MmceCTMachineBuilder(registryName, registryName);
    }

    @ZenCodeType.Method
    public static MmceCTMachineBuilder registerMachine(String registryName, String localizedName) {
        return registerMachineInternal(new MmceCTMachineBuilder(registryName, localizedName));
    }

    @ZenCodeType.Method
    public static MmceCTMachineBuilder registerMachine(String registryName, String localizedName, boolean hasFactory, boolean factoryOnly) {
        MmceCTMachineBuilder builder = new MmceCTMachineBuilder(registryName, localizedName)
                .hasFactory(hasFactory)
                .factoryOnly(factoryOnly);
        return registerMachineInternal(builder);
    }

    @ZenCodeType.Method
    public static MmceCTMachineBuilder registerMachine(String registryName, String localizedName, boolean requiresBlueprint,
                                                       MmceCTRecipeFailureActions failureAction, int color) {
        MmceCTMachineBuilder builder = new MmceCTMachineBuilder(registryName, localizedName)
                .requiresBlueprint(requiresBlueprint)
                .setFailureAction(failureAction)
                .setColor(color);
        return registerMachineInternal(builder);
    }

    @ZenCodeType.Method
    public static MmceCTMachineBuilder registerMachine(String registryName, String localizedName, boolean requiresBlueprint,
                                                       MmceCTRecipeFailureActions failureAction, int color,
                                                       boolean hasFactory, boolean factoryOnly) {
        MmceCTMachineBuilder builder = new MmceCTMachineBuilder(registryName, localizedName)
                .requiresBlueprint(requiresBlueprint)
                .setFailureAction(failureAction)
                .setColor(color)
                .hasFactory(hasFactory)
                .factoryOnly(factoryOnly);
        return registerMachineInternal(builder);
    }

    @ZenCodeType.Method
    public static MmceCTMachineBuilder getBuilder(String registryName) {
        return PRE_LOAD_MACHINES.get(machineKey(registryName));
    }

    @ZenCodeType.Method
    public static void clearScriptDefinitions() {
        PRE_LOAD_MACHINES.clear();
        MmceScriptDataRegistry.clear();
        MmceEventRegistry.clear();
        MmceBlockCheckerRegistry.clear();
        MmceItemCallbackRegistry.clear();
        MmceMachineUpgradeRegistry.clear();
        MmceMachineUpgradeHelper.clear();
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder part(int x, int y, int z, String element) {
        return part(x, y, z, new String[]{element});
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder part(int x, int y, int z, String... elements) {
        return addPart(x, y, z, stringList(elements));
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder part(int x, int y, int z, BlockState... states) {
        return addPart(x, y, z, MmceScriptValues.blockElements(states));
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder part(int x, int y, int z, IItemStack... itemStacks) {
        return addPart(x, y, z, MmceScriptValues.blockElements(itemStacks));
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder partArray(int[] x, int[] y, int[] z, String[] elements) {
        return addPart(x, y, z, stringList(elements));
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder partArray(int[] x, int[] y, int[] z, BlockState... states) {
        return addPart(x, y, z, MmceScriptValues.blockElements(states));
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder partArray(int[] x, int[] y, int[] z, IItemStack... itemStacks) {
        return addPart(x, y, z, MmceScriptValues.blockElements(itemStacks));
    }

    private MmceCTMachineBuilder addPart(int x, int y, int z, List<String> elements) {
        JsonObject part = new JsonObject();
        part.addProperty("x", x);
        part.addProperty("y", y);
        part.addProperty("z", z);
        part.add("elements", stringArray(elements));
        addPart(part);
        return this;
    }

    private MmceCTMachineBuilder addPart(int[] x, int[] y, int[] z, List<String> elements) {
        JsonObject part = new JsonObject();
        part.add("x", intArray(x));
        part.add("y", intArray(y));
        part.add("z", intArray(z));
        part.add("elements", stringArray(elements));
        addPart(part);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder getBlockArrayBuilder() {
        return MmceCTBlockArrayBuilder.wrap(parts, this::setLastPart);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder getBlockArray() {
        return getBlockArrayBuilder();
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setNBT(String json) {
        setLastPartNbt("nbt", json);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setNbt(String json) {
        return setNBT(json);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder nbt(String json) {
        return setNBT(json);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder partNbt(String json) {
        return setNBT(json);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setPartNbt(String json) {
        return setNBT(json);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setNBT(IData data) {
        setLastPartNbt("nbt", data);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setNbt(IData data) {
        return setNBT(data);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder nbt(IData data) {
        return setNBT(data);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder partNbt(IData data) {
        return setNBT(data);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setPartNbt(IData data) {
        return setNBT(data);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setPreviewNBT(String json) {
        setLastPartNbt("preview-nbt", json);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setPreviewNbt(String json) {
        return setPreviewNBT(json);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder previewNbt(String json) {
        return setPreviewNBT(json);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder partPreviewNbt(String json) {
        return setPreviewNBT(json);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setPartPreviewNbt(String json) {
        return setPreviewNBT(json);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setPreviewNBT(IData data) {
        setLastPartNbt("preview-nbt", data);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setPreviewNbt(IData data) {
        return setPreviewNBT(data);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder previewNbt(IData data) {
        return setPreviewNBT(data);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder partPreviewNbt(IData data) {
        return setPreviewNBT(data);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setPartPreviewNbt(IData data) {
        return setPreviewNBT(data);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setTag(String tag) {
        if (lastPart != null && tag != null && !tag.isBlank()) {
            lastPart.addProperty("selector-tag", tag);
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder partTag(String tag) {
        return setTag(tag);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder selectorTag(String tag) {
        return setTag(tag);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder selector_tag(String tag) {
        return setTag(tag);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setBlockChecker(MmceBlockChecker checker) {
        return setBlockChecker("", checker);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder partChecker(MmceBlockChecker checker) {
        return setBlockChecker(checker);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setPartChecker(MmceBlockChecker checker) {
        return setBlockChecker(checker);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setBlockChecker(String checkerId, MmceBlockChecker checker) {
        if (lastPart != null && checker != null) {
            lastPart.addProperty("checker-id", MmceBlockCheckerRegistry.register(checkerId, checker));
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder partChecker(String checkerId, MmceBlockChecker checker) {
        return setBlockChecker(checkerId, checker);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setPartChecker(String checkerId, MmceBlockChecker checker) {
        return setBlockChecker(checkerId, checker);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder requiresBlueprint(boolean value) {
        root.addProperty("requires-blueprint", value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setRequiresBlueprint(boolean value) {
        return requiresBlueprint(value);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder hasFactory(boolean value) {
        root.addProperty("has-factory", value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setHasFactory(boolean value) {
        return hasFactory(value);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder factoryOnly(boolean value) {
        root.addProperty("factory-only", value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setFactoryOnly(boolean value) {
        return factoryOnly(value);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setParallelizable(boolean value) {
        root.addProperty("parallelizable", value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setMaxParallelism(int value) {
        root.addProperty("maxParallelism", Math.max(1, value));
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setInternalParallelism(int value) {
        root.addProperty("internalParallelism", Math.max(0, value));
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setFailureAction(MmceCTRecipeFailureActions failureAction) {
        if (failureAction != null) {
            root.addProperty("failure-action", failureAction.getName());
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setFailureAction(String failureAction) {
        if (failureAction != null && !failureAction.isBlank()) {
            root.addProperty("failure-action", failureAction.trim());
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setColor(int color) {
        root.addProperty("color", color);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setPrefix(String prefixName) {
        root.addProperty("prefix", prefixName == null ? "" : prefixName);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setMachinePrefix(String prefixName) {
        return setPrefix(prefixName);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setGeoModel(String modelName) {
        root.addProperty("geoModel", modelName == null ? "" : modelName);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setMachineGeoModel(String modelName) {
        return setGeoModel(modelName);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setControllerBoundingBox(double minX, double minY, double minZ,
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

    @ZenCodeType.Method
    public MmceCTMachineBuilder setControllerBoundingBox(Object aabb) {
        List<Double> values = boundingBoxValues(aabb);
        if (values.size() == 6) {
            return setControllerBoundingBox(values.get(0), values.get(1), values.get(2),
                    values.get(3), values.get(4), values.get(5));
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder maxThreads(int value) {
        root.addProperty("maxThreads", Math.max(0, value));
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder setMaxThreads(int value) {
        return maxThreads(value);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder coreThread(String threadName) {
        if (threadName != null && !threadName.isBlank()) {
            coreThreads().add(threadName.trim());
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addCoreThread(String threadName) {
        return coreThread(threadName);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addCoreThread(MmceCTFactoryRecipeThreadBuilder thread) {
        if (thread != null) {
            coreThreads().add(thread.json());
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addSmartInterfaceType(MmceCTSmartInterfaceType type) {
        if (type != null) {
            smartInterfaceTypes().add(type.json());
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder smartInterfaceType(String type, float defaultValue) {
        JsonObject object = new JsonObject();
        object.addProperty("type", type == null ? "" : type.trim());
        object.addProperty("defaultValue", defaultValue);
        smartInterfaceTypes().add(object);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder hideComponentsWhenFormed(boolean value) {
        root.addProperty("hide-components-when-formed", value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addDynamicPattern(MmceCTDynamicPatternBuilder pattern) {
        if (pattern != null) {
            dynamicPatterns().add(pattern.json());
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addDynamicPatternJson(String json) {
        if (json != null && !json.isBlank()) {
            dynamicPatterns().add(com.google.gson.JsonParser.parseString(json).getAsJsonObject());
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addSingleBlockModifier(int x, int y, int z, String element, String description, MmceRecipeModifier... modifiers) {
        return singleBlockModifier(x, y, z, element, description, modifiers);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addSingleBlockModifier(int x, int y, int z, BlockState state, String description, MmceRecipeModifier... modifiers) {
        return singleBlockModifier(x, y, z, state, description, modifiers);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addSingleBlockModifier(int x, int y, int z, IItemStack itemStack, String description, MmceRecipeModifier... modifiers) {
        return singleBlockModifier(x, y, z, itemStack, description, modifiers);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder singleBlockModifier(int x, int y, int z, String element, String description, MmceRecipeModifier... modifiers) {
        return singleBlockModifier(x, y, z, element, description, "", "", modifiers);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder singleBlockModifier(int x, int y, int z, BlockState state, String description, MmceRecipeModifier... modifiers) {
        return singleBlockModifier(x, y, z, firstBlockElement(state), description, modifiers);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder singleBlockModifier(int x, int y, int z, IItemStack itemStack, String description, MmceRecipeModifier... modifiers) {
        return singleBlockModifier(x, y, z, firstBlockElement(itemStack), description, modifiers);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder singleBlockModifier(int x, int y, int z, String element, String description,
                                                    String nbtJson, String previewNbtJson, MmceRecipeModifier... modifiers) {
        JsonObject entry = new JsonObject();
        entry.addProperty("x", x);
        entry.addProperty("y", y);
        entry.addProperty("z", z);
        entry.addProperty("elements", element);
        if (description != null && !description.isBlank()) {
            entry.addProperty("description", description);
        }
        addNbt(entry, nbtJson, previewNbtJson);
        addModifiers(entry, modifiers);
        modifiers().add(entry);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addSingleBlockModifierArray(int[] x, int[] y, int[] z, String[] elements, String description, MmceRecipeModifier... modifiers) {
        return addSingleBlockModifierArray(x, y, z, stringList(elements), description, modifiers);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addSingleBlockModifierArray(int[] x, int[] y, int[] z, BlockState[] states, String description, MmceRecipeModifier... modifiers) {
        return addSingleBlockModifierArray(x, y, z, MmceScriptValues.blockElements(states), description, modifiers);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addSingleBlockModifierArray(int[] x, int[] y, int[] z, IItemStack[] itemStacks, String description, MmceRecipeModifier... modifiers) {
        return addSingleBlockModifierArray(x, y, z, MmceScriptValues.blockElements(itemStacks), description, modifiers);
    }

    private MmceCTMachineBuilder addSingleBlockModifierArray(int[] x, int[] y, int[] z, List<String> elements, String description, MmceRecipeModifier... modifiers) {
        JsonObject entry = new JsonObject();
        entry.add("x", intArray(x));
        entry.add("y", intArray(y));
        entry.add("z", intArray(z));
        entry.add("elements", stringArray(elements));
        if (description != null && !description.isBlank()) {
            entry.addProperty("description", description);
        }
        addModifiers(entry, modifiers);
        modifiers().add(entry);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addMultiBlockModifier(MmceCTMultiBlockModifierReplacement multiBlockModifier) {
        if (multiBlockModifier != null) {
            JsonArray entries = multiBlockModifier.entries();
            JsonArray target = modifiers();
            for (int index = 0; index < entries.size(); index++) {
                target.add(entries.get(index).deepCopy());
            }
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addStructureFormedHandler(MmceCTMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.STRUCTURE_FORMED, null, handler);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addStructureUpdateHandler(MmceCTMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.STRUCTURE_UPDATE, null, handler);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addMachinePreTickHandler(MmceCTMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.TICK, MmceEventPhase.START, handler);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addMachinePostTickHandler(MmceCTMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.TICK, MmceEventPhase.END, handler);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addTickHandler(MmceCTMachineEventHandler handler) {
        return addMachinePostTickHandler(handler);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addSmartInterfaceUpdateHandler(MmceCTMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.SMART_INTERFACE_UPDATE, null, handler);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addControllerButtonClickHandler(MmceCTMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.CONTROLLER_BUTTON_CLICK, null, handler);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addGUIRenderHandler(MmceCTMachineEventHandler handler) {
        return machineHandler(MmceMachineEventType.CONTROLLER_GUI_RENDER, null, handler);
    }

    @ZenCodeType.Method
    public MmceCTMachineBuilder addControllerGUIRenderHandler(MmceCTMachineEventHandler handler) {
        return addGUIRenderHandler(handler);
    }

    @ZenCodeType.Method
    public void build() {
        PRE_LOAD_MACHINES.remove(machineKey(registryName));
        MmceScriptDataRegistry.registerMachine(sourceId("crafttweaker/machines", registryName), root);
    }

    private static MmceCTMachineBuilder registerMachineInternal(MmceCTMachineBuilder builder) {
        PRE_LOAD_MACHINES.putIfAbsent(machineKey(builder.registryName), builder);
        return PRE_LOAD_MACHINES.get(machineKey(builder.registryName));
    }

    private static ResourceLocation machineKey(String registryName) {
        String id = registryName == null || registryName.isBlank() ? "unknown" : registryName.trim();
        return id.indexOf(':') >= 0
                ? ResourceLocation.parse(id)
                : ResourceLocation.fromNamespaceAndPath(hellfirepvp.modularmachinery.port.ModularMachineryNeoForge.MODID, id);
    }

    private MmceCTMachineBuilder machineHandler(MmceMachineEventType type, MmceEventPhase phase, MmceCTMachineEventHandler handler) {
        if (handler != null) {
            MmceEventRegistry.registerMachine(registryName, type, event -> {
                if (phase == null || event.getPhase() == phase) {
                    handler.handle(MmceCTMachineEvent.of(event));
                }
            });
        }
        return this;
    }

    static ResourceLocation sourceId(String directory, String id) {
        String path = directory + "/" + id.toLowerCase(Locale.ROOT).replace(':', '/').replaceAll("[^a-z0-9_./-]", "_");
        return ResourceLocation.fromNamespaceAndPath("crafttweaker", path);
    }

    private static JsonArray intArray(Object value) {
        JsonArray array = new JsonArray();
        if (value != null && value.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(value); i++) {
                Object entry = Array.get(value, i);
                if (entry instanceof Number number) {
                    array.add(number.intValue());
                }
            }
        }
        return array;
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

    private void addPart(JsonObject part) {
        parts.add(part);
        setLastPart(part);
    }

    private void setLastPart(JsonObject part) {
        lastPart = part;
    }

    private void setLastPartNbt(String key, String json) {
        if (lastPart != null && json != null && !json.isBlank()) {
            lastPart.add(key, com.google.gson.JsonParser.parseString(json).getAsJsonObject());
        }
    }

    private void setLastPartNbt(String key, IData data) {
        if (lastPart != null && data != null) {
            lastPart.add(key, com.google.gson.JsonParser.parseString(data.accept(DataToJsonStringVisitor.INSTANCE)).getAsJsonObject());
        }
    }

    private static JsonElement stringArray(String[] value) {
        return stringArray(stringList(value));
    }

    private static JsonElement stringArray(List<String> value) {
        JsonArray array = new JsonArray();
        if (value != null) {
            for (String entry : value) {
                if (entry != null && !entry.isBlank()) {
                    array.add(entry);
                }
            }
        }
        return array;
    }

    private static List<String> stringList(String[] value) {
        if (value == null) {
            return List.of();
        }
        return java.util.Arrays.stream(value)
                .filter(entry -> entry != null && !entry.isBlank())
                .toList();
    }

    private static String firstBlockElement(Object value) {
        return MmceScriptValues.blockElement(value).orElse("");
    }

    private JsonArray coreThreads() {
        if (root.has("coreThreads") && root.get("coreThreads").isJsonArray()) {
            return root.getAsJsonArray("coreThreads");
        }
        JsonArray array = new JsonArray();
        root.add("coreThreads", array);
        return array;
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
}
