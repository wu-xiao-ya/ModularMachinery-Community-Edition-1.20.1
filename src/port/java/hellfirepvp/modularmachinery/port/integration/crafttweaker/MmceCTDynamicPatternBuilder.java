package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.world.level.block.state.BlockState;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.DynamicPatternBuilder")
public final class MmceCTDynamicPatternBuilder {
    private final JsonObject root = new JsonObject();
    private final JsonArray parts = new JsonArray();
    private JsonArray partsEnd;

    private MmceCTDynamicPatternBuilder(String name) {
        root.addProperty("name", name == null || name.isBlank() ? "dynamic" : name.trim());
        root.add("parts", parts);
    }

    @ZenCodeType.Method
    public static MmceCTDynamicPatternBuilder newBuilder(String name) {
        return new MmceCTDynamicPatternBuilder(name);
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder faces(String... faces) {
        JsonArray array = new JsonArray();
        if (faces != null) {
            for (String face : faces) {
                if (face != null && !face.isBlank()) {
                    array.add(face.trim());
                }
            }
        }
        root.add("faces", array);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder setFaces(String... faces) {
        return faces(faces);
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder minSize(int value) {
        root.addProperty("minSize", Math.max(0, value));
        return this;
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder setMinSize(int value) {
        return minSize(value);
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder maxSize(int value) {
        root.addProperty("maxSize", Math.max(0, value));
        return this;
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder setMaxSize(int value) {
        return maxSize(value);
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder structureSizeOffsetStart(int x, int y, int z) {
        root.add("structure-size-offset-start", offset(x, y, z));
        return this;
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder setStructureSizeOffsetStart(int x, int y, int z) {
        return structureSizeOffsetStart(x, y, z);
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder structure_size_offset_start(int x, int y, int z) {
        return structureSizeOffsetStart(x, y, z);
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder structureSizeOffset(int x, int y, int z) {
        root.add("structure-size-offset", offset(x, y, z));
        return this;
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder setStructureSizeOffset(int x, int y, int z) {
        return structureSizeOffset(x, y, z);
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder structure_size_offset(int x, int y, int z) {
        return structureSizeOffset(x, y, z);
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder setParts(MmceCTBlockArrayBuilder blockArray) {
        replaceArray(parts, blockArray == null ? new JsonArray() : blockArray.rawBlockArray());
        return this;
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder setPartsEnd(MmceCTBlockArrayBuilder blockArray) {
        partsEnd = blockArray == null ? new JsonArray() : blockArray.rawBlockArray();
        root.add("parts-end", partsEnd);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder partsEnd(MmceCTBlockArrayBuilder blockArray) {
        return setPartsEnd(blockArray);
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder parts_end(MmceCTBlockArrayBuilder blockArray) {
        return setPartsEnd(blockArray);
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder getPartsBuilder() {
        return MmceCTBlockArrayBuilder.wrap(parts, ignored -> {
        });
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder getPartsEndBuilder() {
        if (partsEnd == null) {
            partsEnd = new JsonArray();
            root.add("parts-end", partsEnd);
        }
        return MmceCTBlockArrayBuilder.wrap(partsEnd, ignored -> {
        });
    }

    @ZenCodeType.Method
    public MmceCTBlockArrayBuilder getEndPartsBuilder() {
        return getPartsEndBuilder();
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder addPart(int x, int y, int z, String... blockNames) {
        getPartsBuilder().addBlock(x, y, z, blockNames);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder addPart(int x, int y, int z, BlockState... states) {
        getPartsBuilder().addBlock(x, y, z, states);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder addPart(int x, int y, int z, IItemStack... itemStacks) {
        getPartsBuilder().addBlock(x, y, z, itemStacks);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder addEndPart(int x, int y, int z, String... blockNames) {
        getPartsEndBuilder().addBlock(x, y, z, blockNames);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder addEndPart(int x, int y, int z, BlockState... states) {
        getPartsEndBuilder().addBlock(x, y, z, states);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder addEndPart(int x, int y, int z, IItemStack... itemStacks) {
        getPartsEndBuilder().addBlock(x, y, z, itemStacks);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder endPart(int x, int y, int z, String... blockNames) {
        return addEndPart(x, y, z, blockNames);
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder endPart(int x, int y, int z, BlockState... states) {
        return addEndPart(x, y, z, states);
    }

    @ZenCodeType.Method
    public MmceCTDynamicPatternBuilder endPart(int x, int y, int z, IItemStack... itemStacks) {
        return addEndPart(x, y, z, itemStacks);
    }

    JsonObject json() {
        return root.deepCopy();
    }

    private static JsonObject offset(int x, int y, int z) {
        JsonObject object = new JsonObject();
        object.addProperty("x", x);
        object.addProperty("y", y);
        object.addProperty("z", z);
        return object;
    }

    private static void replaceArray(JsonArray target, JsonArray source) {
        target.asList().clear();
        for (int index = 0; index < source.size(); index++) {
            target.add(source.get(index).deepCopy());
        }
    }
}
