package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.visitor.DataToJsonStringVisitor;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.integration.MmceBlockChecker;
import hellfirepvp.modularmachinery.port.integration.MmceBlockCheckerRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceScriptValues;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.level.block.state.BlockState;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.BlockArrayBuilder")
public final class MmceCTBlockArrayBuilder {
    private final JsonArray parts;
    private final Consumer<JsonObject> lastPartSink;
    private JsonObject lastPart;

    private MmceCTBlockArrayBuilder(JsonArray parts, Consumer<JsonObject> lastPartSink) {
        this.parts = parts;
        this.lastPartSink = lastPartSink;
    }

    @ZenCodeType.Method
    public static MmceCTBlockArrayBuilder newBuilder() {
        return new MmceCTBlockArrayBuilder(new JsonArray(), ignored -> {
        });
    }

    @ZenCodeType.Method
    public static MmceCTBlockArrayBuilder newBuilder(MmceCTBlockArrayBuilder blockArray) {
        JsonArray copy = blockArray == null ? new JsonArray() : blockArray.rawBlockArray();
        return new MmceCTBlockArrayBuilder(copy, ignored -> {
        });
    }

    static MmceCTBlockArrayBuilder wrap(JsonArray parts, Consumer<JsonObject> lastPartSink) {
        return new MmceCTBlockArrayBuilder(parts, lastPartSink);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, String... blockNames) {
        return addPart(x, y, z, stringList(blockNames));
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, BlockState... states) {
        return addPart(x, y, z, MmceScriptValues.blockElements(states));
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, IItemStack... itemStacks) {
        return addPart(x, y, z, MmceScriptValues.blockElements(itemStacks));
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, String nbtJson, String previewNbtJson, String... blockNames) {
        addBlock(x, y, z, blockNames);
        setNBT(nbtJson);
        setPreviewNBT(previewNbtJson);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, String nbtJson, String previewNbtJson, BlockState... states) {
        addBlock(x, y, z, states);
        setNBT(nbtJson);
        setPreviewNBT(previewNbtJson);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, String nbtJson, String previewNbtJson, IItemStack... itemStacks) {
        addBlock(x, y, z, itemStacks);
        setNBT(nbtJson);
        setPreviewNBT(previewNbtJson);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, IData nbt, IData previewNbt, String... blockNames) {
        addBlock(x, y, z, blockNames);
        setNBT(nbt);
        setPreviewNBT(previewNbt);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, IData nbt, IData previewNbt, BlockState... states) {
        addBlock(x, y, z, states);
        setNBT(nbt);
        setPreviewNBT(previewNbt);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, IData nbt, IData previewNbt, IItemStack... itemStacks) {
        addBlock(x, y, z, itemStacks);
        setNBT(nbt);
        setPreviewNBT(previewNbt);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, IData nbt, IData previewNbt,
                                            MmceBlockChecker checker, String... blockNames) {
        addBlock(x, y, z, nbt, previewNbt, blockNames);
        setBlockChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, IData nbt, IData previewNbt,
                                            MmceBlockChecker checker, BlockState... states) {
        addBlock(x, y, z, nbt, previewNbt, states);
        setBlockChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, IData nbt, IData previewNbt,
                                            MmceBlockChecker checker, IItemStack... itemStacks) {
        addBlock(x, y, z, nbt, previewNbt, itemStacks);
        setBlockChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, MmceBlockChecker checker, String... blockNames) {
        addBlock(x, y, z, blockNames);
        setBlockChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, MmceBlockChecker checker, BlockState... states) {
        addBlock(x, y, z, states);
        setBlockChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int x, int y, int z, MmceBlockChecker checker, IItemStack... itemStacks) {
        addBlock(x, y, z, itemStacks);
        setBlockChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, String... blockNames) {
        return addPart(x, y, z, stringList(blockNames));
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, BlockState... states) {
        return addPart(x, y, z, MmceScriptValues.blockElements(states));
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, IItemStack... itemStacks) {
        return addPart(x, y, z, MmceScriptValues.blockElements(itemStacks));
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, String nbtJson, String previewNbtJson, String... blockNames) {
        addBlock(x, y, z, blockNames);
        setNBT(nbtJson);
        setPreviewNBT(previewNbtJson);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, String nbtJson, String previewNbtJson, BlockState... states) {
        addBlock(x, y, z, states);
        setNBT(nbtJson);
        setPreviewNBT(previewNbtJson);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, String nbtJson, String previewNbtJson, IItemStack... itemStacks) {
        addBlock(x, y, z, itemStacks);
        setNBT(nbtJson);
        setPreviewNBT(previewNbtJson);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, IData nbt, IData previewNbt, String... blockNames) {
        addBlock(x, y, z, blockNames);
        setNBT(nbt);
        setPreviewNBT(previewNbt);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, IData nbt, IData previewNbt, BlockState... states) {
        addBlock(x, y, z, states);
        setNBT(nbt);
        setPreviewNBT(previewNbt);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, IData nbt, IData previewNbt, IItemStack... itemStacks) {
        addBlock(x, y, z, itemStacks);
        setNBT(nbt);
        setPreviewNBT(previewNbt);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, IData nbt, IData previewNbt,
                                            MmceBlockChecker checker, String... blockNames) {
        addBlock(x, y, z, nbt, previewNbt, blockNames);
        setBlockChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, IData nbt, IData previewNbt,
                                            MmceBlockChecker checker, BlockState... states) {
        addBlock(x, y, z, nbt, previewNbt, states);
        setBlockChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, IData nbt, IData previewNbt,
                                            MmceBlockChecker checker, IItemStack... itemStacks) {
        addBlock(x, y, z, nbt, previewNbt, itemStacks);
        setBlockChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, MmceBlockChecker checker, String... blockNames) {
        addBlock(x, y, z, blockNames);
        setBlockChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, MmceBlockChecker checker, BlockState... states) {
        addBlock(x, y, z, states);
        setBlockChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder addBlock(int[] x, int[] y, int[] z, MmceBlockChecker checker, IItemStack... itemStacks) {
        addBlock(x, y, z, itemStacks);
        setBlockChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder setNBT(String json) {
        setNbt("nbt", json);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder setNbt(String json) {
        return setNBT(json);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder nbt(String json) {
        return setNBT(json);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder setNBT(IData data) {
        setNbt("nbt", data);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder setNbt(IData data) {
        return setNBT(data);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder nbt(IData data) {
        return setNBT(data);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder setPreviewNBT(String json) {
        setNbt("preview-nbt", json);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder setPreviewNbt(String json) {
        return setPreviewNBT(json);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder previewNbt(String json) {
        return setPreviewNBT(json);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder setPreviewNBT(IData data) {
        setNbt("preview-nbt", data);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder setPreviewNbt(IData data) {
        return setPreviewNBT(data);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder previewNbt(IData data) {
        return setPreviewNBT(data);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder setTag(String tag) {
        if (lastPart != null && tag != null && !tag.isBlank()) {
            lastPart.addProperty("selector-tag", tag);
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder partTag(String tag) {
        return setTag(tag);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder selectorTag(String tag) {
        return setTag(tag);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder selector_tag(String tag) {
        return setTag(tag);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder setBlockChecker(MmceBlockChecker checker) {
        return setBlockChecker("", checker);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder partChecker(MmceBlockChecker checker) {
        return setBlockChecker(checker);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder setPartChecker(MmceBlockChecker checker) {
        return setBlockChecker(checker);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder setBlockChecker(String checkerId, MmceBlockChecker checker) {
        if (lastPart != null && checker != null) {
            lastPart.addProperty("checker-id", MmceBlockCheckerRegistry.register(checkerId, checker));
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder partChecker(String checkerId, MmceBlockChecker checker) {
        return setBlockChecker(checkerId, checker);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder setPartChecker(String checkerId, MmceBlockChecker checker) {
        return setBlockChecker(checkerId, checker);
    }

    @ZenCodeType.Method
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

    private MmceCTBlockArrayBuilder addPart(int x, int y, int z, List<String> elements) {
        JsonObject part = new JsonObject();
        part.addProperty("x", x);
        part.addProperty("y", y);
        part.addProperty("z", z);
        part.add("elements", stringArray(elements));
        addPart(part);
        return this;
    }

    private MmceCTBlockArrayBuilder addPart(int[] x, int[] y, int[] z, List<String> elements) {
        JsonObject part = new JsonObject();
        part.add("x", intArray(x));
        part.add("y", intArray(y));
        part.add("z", intArray(z));
        part.add("elements", stringArray(elements));
        addPart(part);
        return this;
    }

    private void setNbt(String key, String json) {
        if (lastPart != null && json != null && !json.isBlank()) {
            lastPart.add(key, com.google.gson.JsonParser.parseString(json).getAsJsonObject());
        }
    }

    private void setNbt(String key, IData data) {
        if (lastPart != null && data != null) {
            lastPart.add(key, com.google.gson.JsonParser.parseString(data.accept(DataToJsonStringVisitor.INSTANCE)).getAsJsonObject());
        }
    }

    private static JsonArray intArray(int[] values) {
        JsonArray array = new JsonArray();
        if (values != null) {
            for (int value : values) {
                array.add(value);
            }
        }
        return array;
    }

    private static JsonArray stringArray(String[] values) {
        return stringArray(stringList(values));
    }

    private static JsonArray stringArray(List<String> values) {
        JsonArray array = new JsonArray();
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    array.add(value);
                }
            }
        }
        return array;
    }

    private static List<String> stringList(String[] values) {
        if (values == null) {
            return List.of();
        }
        return java.util.Arrays.stream(values)
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }
}
