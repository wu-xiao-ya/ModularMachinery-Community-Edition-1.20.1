package hellfirepvp.modularmachinery.port.integration.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.integration.MmceScriptValues;

public final class MmceKubeJSMultiBlockModifierBuilder {
    private final String modifierName;
    private JsonArray blockArray = new JsonArray();
    private final JsonArray modifiers = new JsonArray();
    private JsonObject descriptiveStack;

    private MmceKubeJSMultiBlockModifierBuilder(String modifierName) {
        this.modifierName = modifierName == null ? "" : modifierName;
    }

    public static MmceKubeJSMultiBlockModifierBuilder newBuilder() {
        return new MmceKubeJSMultiBlockModifierBuilder("");
    }

    public static MmceKubeJSMultiBlockModifierBuilder newBuilder(String modifierName) {
        return new MmceKubeJSMultiBlockModifierBuilder(modifierName);
    }

    public MmceKubeJSMultiBlockModifierBuilder setBlockArray(MmceKubeJSBlockArrayBuilder blockArray) {
        this.blockArray = blockArray == null ? new JsonArray() : blockArray.rawBlockArray();
        return this;
    }

    public MmceKubeJSMultiBlockModifierBuilder setBlockArray(JsonArray blockArray) {
        this.blockArray = blockArray == null ? new JsonArray() : blockArray.deepCopy();
        return this;
    }

    public MmceKubeJSMultiBlockModifierBuilder blockArray(MmceKubeJSBlockArrayBuilder blockArray) {
        return setBlockArray(blockArray);
    }

    public MmceKubeJSMultiBlockModifierBuilder blockArray(JsonArray blockArray) {
        return setBlockArray(blockArray);
    }

    public MmceKubeJSMultiBlockModifierBuilder addModifier(MmceRecipeModifier... modifiers) {
        JsonArray array = MmceKubeJSMultiBlockModifierReplacement.modifiersToJson(modifiers);
        for (int index = 0; index < array.size(); index++) {
            this.modifiers.add(array.get(index).deepCopy());
        }
        return this;
    }

    public MmceKubeJSMultiBlockModifierBuilder modifier(MmceRecipeModifier... modifiers) {
        return addModifier(modifiers);
    }

    public MmceKubeJSMultiBlockModifierBuilder modifiers(MmceRecipeModifier... modifiers) {
        return addModifier(modifiers);
    }

    public MmceKubeJSMultiBlockModifierBuilder setDescriptiveStack(Object stack) {
        MmceScriptValues.itemEntry(stack, 1).ifPresent(entry -> descriptiveStack = MmceScriptValues.itemJson(entry));
        return this;
    }

    public MmceKubeJSMultiBlockModifierBuilder descriptiveStack(Object stack) {
        return setDescriptiveStack(stack);
    }

    public MmceKubeJSMultiBlockModifierReplacement build() {
        return new MmceKubeJSMultiBlockModifierReplacement(modifierName, blockArray, modifiers, descriptiveStack);
    }
}
