package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.integration.MmceScriptValues;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MultiblockModifierBuilder")
public final class MmceCTMultiBlockModifierBuilder {
    private final String modifierName;
    private JsonArray blockArray = new JsonArray();
    private final JsonArray modifiers = new JsonArray();
    private JsonObject descriptiveStack;

    private MmceCTMultiBlockModifierBuilder(String modifierName) {
        this.modifierName = modifierName == null ? "" : modifierName;
    }

    @ZenCodeType.Method
    public static MmceCTMultiBlockModifierBuilder newBuilder() {
        return new MmceCTMultiBlockModifierBuilder("");
    }

    @ZenCodeType.Method
    public static MmceCTMultiBlockModifierBuilder newBuilder(String modifierName) {
        return new MmceCTMultiBlockModifierBuilder(modifierName);
    }

    @ZenCodeType.Method
    public MmceCTMultiBlockModifierBuilder setBlockArray(MmceCTBlockArrayBuilder blockArray) {
        this.blockArray = blockArray == null ? new JsonArray() : blockArray.rawBlockArray();
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMultiBlockModifierBuilder setBlockArray(JsonArray blockArray) {
        this.blockArray = blockArray == null ? new JsonArray() : blockArray.deepCopy();
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMultiBlockModifierBuilder blockArray(MmceCTBlockArrayBuilder blockArray) {
        return setBlockArray(blockArray);
    }

    @ZenCodeType.Method
    public MmceCTMultiBlockModifierBuilder blockArray(JsonArray blockArray) {
        return setBlockArray(blockArray);
    }

    @ZenCodeType.Method
    public MmceCTMultiBlockModifierBuilder addModifier(MmceRecipeModifier... modifiers) {
        JsonArray array = MmceCTMultiBlockModifierReplacement.modifiersToJson(modifiers);
        for (int index = 0; index < array.size(); index++) {
            this.modifiers.add(array.get(index).deepCopy());
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMultiBlockModifierBuilder setDescriptiveStack(Object stack) {
        MmceScriptValues.itemEntry(stack, 1).ifPresent(entry -> descriptiveStack = MmceScriptValues.itemJson(entry));
        return this;
    }

    @ZenCodeType.Method
    public MmceCTMultiBlockModifierBuilder setDescriptiveStack(IItemStack stack) {
        return setDescriptiveStack((Object) stack);
    }

    @ZenCodeType.Method
    public MmceCTMultiBlockModifierReplacement build() {
        return new MmceCTMultiBlockModifierReplacement(modifierName, blockArray, modifiers, descriptiveStack);
    }
}
