package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.visitor.DataToJsonStringVisitor;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.event.MmceEventPhase;
import hellfirepvp.modularmachinery.port.event.MmceEventRegistry;
import hellfirepvp.modularmachinery.port.event.MmceMachineEventType;
import hellfirepvp.modularmachinery.port.integration.MmceBlockChecker;
import hellfirepvp.modularmachinery.port.integration.MmceBlockCheckerRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceMachineDefinitionPatcher;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.integration.MmceScriptValues;
import java.lang.reflect.Array;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineModifier")
public final class MmceCTMachineModifier {
    private static final String SOURCE_DIR = "crafttweaker/machine_modifiers";

    @ZenCodeType.Method
    public static void addSmartInterfaceType(String machineName, MmceCTSmartInterfaceType type) {
        if (type == null) {
            return;
        }
        MmceMachineDefinitionPatcher.patch(machineName, SOURCE_DIR,
                root -> MmceMachineDefinitionPatcher.array(root, "smartInterfaceTypes").add(type.json()));
    }

    @ZenCodeType.Method
    public static void setMaxThreads(String machineName, int maxThreads) {
        MmceMachineDefinitionPatcher.patch(machineName, SOURCE_DIR,
                root -> root.addProperty("maxThreads", Math.max(0, maxThreads)));
    }

    @ZenCodeType.Method
    public static void setMaxParallelism(String machineName, int maxParallelism) {
        MmceMachineDefinitionPatcher.patch(machineName, SOURCE_DIR,
                root -> root.addProperty("maxParallelism", Math.max(1, maxParallelism)));
    }

    @ZenCodeType.Method
    public static void setInternalParallelism(String machineName, int parallelism) {
        MmceMachineDefinitionPatcher.patch(machineName, SOURCE_DIR,
                root -> root.addProperty("internalParallelism", Math.max(0, parallelism)));
    }

    @ZenCodeType.Method
    public static void addCoreThread(String machineName, String threadName) {
        if (threadName == null || threadName.isBlank()) {
            return;
        }
        MmceMachineDefinitionPatcher.patch(machineName, SOURCE_DIR,
                root -> MmceMachineDefinitionPatcher.array(root, "coreThreads").add(threadName.trim()));
    }

    @ZenCodeType.Method
    public static void addCoreThread(String machineName, MmceCTFactoryRecipeThreadBuilder thread) {
        if (thread == null) {
            return;
        }
        MmceMachineDefinitionPatcher.patch(machineName, SOURCE_DIR,
                root -> MmceMachineDefinitionPatcher.array(root, "coreThreads").add(thread.json()));
    }

    @ZenCodeType.Method
    public static void addDynamicPattern(String machineName, MmceCTDynamicPatternBuilder pattern) {
        if (pattern == null) {
            return;
        }
        MmceMachineDefinitionPatcher.patch(machineName, SOURCE_DIR,
                root -> MmceMachineDefinitionPatcher.array(root, "dynamic-patterns").add(pattern.json()));
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int x, int y, int z, String element, String description,
                                              MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, element, description, "", "", modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int x, int y, int z, BlockState state, String description,
                                              MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, blockElement(state), description, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int x, int y, int z, IItemStack itemStack, String description,
                                              MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, blockElement(itemStack), description, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int x, int y, int z, String element, String description,
                                              String nbtJson, String previewNbtJson, MmceRecipeModifier... modifiers) {
        MmceMachineDefinitionPatcher.patch(machineName, SOURCE_DIR, root -> {
            JsonObject entry = new JsonObject();
            entry.addProperty("x", x);
            entry.addProperty("y", y);
            entry.addProperty("z", z);
            entry.addProperty("elements", element == null ? "" : element);
            if (description != null && !description.isBlank()) {
                entry.addProperty("description", description);
            }
            addNbt(entry, nbtJson, previewNbtJson);
            addModifiers(entry, modifiers);
            MmceMachineDefinitionPatcher.array(root, "modifiers").add(entry);
        });
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int x, int y, int z, BlockState state, String description,
                                              String nbtJson, String previewNbtJson, MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, blockElement(state), description, nbtJson, previewNbtJson, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int x, int y, int z, IItemStack itemStack, String description,
                                              String nbtJson, String previewNbtJson, MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, blockElement(itemStack), description, nbtJson, previewNbtJson, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int x, int y, int z, String element, String description,
                                              IData nbt, IData previewNbt, MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, List.of(element == null ? "" : element), description, nbtJson(nbt), nbtJson(previewNbt), null, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int x, int y, int z, BlockState state, String description,
                                              IData nbt, IData previewNbt, MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, blockElement(state), description, nbt, previewNbt, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int x, int y, int z, IItemStack itemStack, String description,
                                              IData nbt, IData previewNbt, MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, blockElement(itemStack), description, nbt, previewNbt, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int x, int y, int z, String element, String description,
                                              MmceBlockChecker checker, MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, List.of(element == null ? "" : element), description, "", "", checker, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int x, int y, int z, BlockState state, String description,
                                              MmceBlockChecker checker, MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, blockElement(state), description, checker, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int x, int y, int z, IItemStack itemStack, String description,
                                              MmceBlockChecker checker, MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, blockElement(itemStack), description, checker, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int[] x, int[] y, int[] z, String[] elements, String description,
                                              MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, stringList(elements), description, "", "", null, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int[] x, int[] y, int[] z, BlockState[] states, String description,
                                              MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, MmceScriptValues.blockElements(states), description, "", "", null, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int[] x, int[] y, int[] z, IItemStack[] itemStacks, String description,
                                              MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, MmceScriptValues.blockElements(itemStacks), description, "", "", null, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int[] x, int[] y, int[] z, String[] elements, String description,
                                              IData nbt, IData previewNbt, MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, stringList(elements), description, nbtJson(nbt), nbtJson(previewNbt), null, modifiers);
    }

    @ZenCodeType.Method
    public static void addSingleBlockModifier(String machineName, int[] x, int[] y, int[] z, String[] elements, String description,
                                              MmceBlockChecker checker, MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, x, y, z, stringList(elements), description, "", "", checker, modifiers);
    }

    @ZenCodeType.Method
    public static void addMultiBlockModifier(String machineName, MmceCTMultiBlockModifierReplacement multiBlockModifier) {
        if (multiBlockModifier == null) {
            return;
        }
        MmceMachineDefinitionPatcher.patch(machineName, SOURCE_DIR, root -> {
            JsonArray entries = multiBlockModifier.entries();
            JsonArray target = MmceMachineDefinitionPatcher.array(root, "modifiers");
            for (int index = 0; index < entries.size(); index++) {
                target.add(entries.get(index).deepCopy());
            }
        });
    }

    private static void addSingleBlockModifier(String machineName, int x, int y, int z, List<String> elements, String description,
                                               String nbtJson, String previewNbtJson, MmceBlockChecker checker, MmceRecipeModifier... modifiers) {
        addSingleBlockModifier(machineName, new int[]{x}, new int[]{y}, new int[]{z}, elements, description, nbtJson, previewNbtJson, checker, modifiers);
    }

    private static void addSingleBlockModifier(String machineName, int[] x, int[] y, int[] z, List<String> elements, String description,
                                               String nbtJson, String previewNbtJson, MmceBlockChecker checker, MmceRecipeModifier... modifiers) {
        MmceMachineDefinitionPatcher.patch(machineName, SOURCE_DIR, root -> {
            JsonObject entry = new JsonObject();
            entry.add("x", intArray(x));
            entry.add("y", intArray(y));
            entry.add("z", intArray(z));
            entry.add("elements", stringArray(elements));
            if (description != null && !description.isBlank()) {
                entry.addProperty("description", description);
            }
            addNbt(entry, nbtJson, previewNbtJson);
            if (checker != null) {
                entry.addProperty("checker-id", MmceBlockCheckerRegistry.register("", checker));
            }
            addModifiers(entry, modifiers);
            MmceMachineDefinitionPatcher.array(root, "modifiers").add(entry);
        });
    }

    @ZenCodeType.Method
    public static void addStructureFormedHandler(String machineName, MmceCTMachineEventHandler handler) {
        registerMachineHandler(machineName, MmceMachineEventType.STRUCTURE_FORMED, null, handler);
    }

    @ZenCodeType.Method
    public static void addStructureUpdateHandler(String machineName, MmceCTMachineEventHandler handler) {
        registerMachineHandler(machineName, MmceMachineEventType.STRUCTURE_UPDATE, null, handler);
    }

    @ZenCodeType.Method
    public static void addMachinePreTickHandler(String machineName, MmceCTMachineEventHandler handler) {
        registerMachineHandler(machineName, MmceMachineEventType.TICK, MmceEventPhase.START, handler);
    }

    @ZenCodeType.Method
    public static void addMachinePostTickHandler(String machineName, MmceCTMachineEventHandler handler) {
        registerMachineHandler(machineName, MmceMachineEventType.TICK, MmceEventPhase.END, handler);
    }

    @ZenCodeType.Method
    public static void addTickHandler(String machineName, MmceCTMachineEventHandler handler) {
        addMachinePostTickHandler(machineName, handler);
    }

    @ZenCodeType.Method
    public static void addSmartInterfaceUpdateHandler(String machineName, MmceCTMachineEventHandler handler) {
        registerMachineHandler(machineName, MmceMachineEventType.SMART_INTERFACE_UPDATE, null, handler);
    }

    @ZenCodeType.Method
    public static void addControllerButtonClickHandler(String machineName, MmceCTMachineEventHandler handler) {
        registerMachineHandler(machineName, MmceMachineEventType.CONTROLLER_BUTTON_CLICK, null, handler);
    }

    @ZenCodeType.Method
    public static void addGUIRenderHandler(String machineName, MmceCTMachineEventHandler handler) {
        registerMachineHandler(machineName, MmceMachineEventType.CONTROLLER_GUI_RENDER, null, handler);
    }

    @ZenCodeType.Method
    public static void addControllerGUIRenderHandler(String machineName, MmceCTMachineEventHandler handler) {
        addGUIRenderHandler(machineName, handler);
    }

    @ZenCodeType.Method
    public static void setMachineGeoModel(String machineName, String modelName) {
        MmceMachineDefinitionPatcher.patch(machineName, SOURCE_DIR,
                root -> root.addProperty("geoModel", modelName == null ? "" : modelName));
    }

    @ZenCodeType.Method
    public static void setMachinePrefix(String machineName, String prefixName) {
        MmceMachineDefinitionPatcher.patch(machineName, SOURCE_DIR,
                root -> root.addProperty("prefix", prefixName == null ? "" : prefixName));
    }

    private static void registerMachineHandler(String machineName, MmceMachineEventType type, MmceEventPhase phase,
                                               MmceCTMachineEventHandler handler) {
        if (handler == null) {
            return;
        }
        MmceEventRegistry.registerMachine(MmceMachineDefinitionPatcher.machineId(machineName), type, event -> {
            if (phase == null || event.getPhase() == phase) {
                handler.handle(MmceCTMachineEvent.of(event));
            }
        });
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

    private static String blockElement(Object value) {
        return MmceScriptValues.blockElement(value).orElse("");
    }

    private static String nbtJson(IData data) {
        return data == null ? "" : data.accept(DataToJsonStringVisitor.INSTANCE);
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

    private static JsonArray stringArray(List<String> value) {
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

    private MmceCTMachineModifier() {
    }
}
