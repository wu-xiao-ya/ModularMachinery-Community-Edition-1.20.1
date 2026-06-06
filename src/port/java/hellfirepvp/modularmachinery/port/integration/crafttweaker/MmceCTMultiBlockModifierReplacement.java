package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MultiBlockModifierReplacement")
public final class MmceCTMultiBlockModifierReplacement {
    private final String modifierName;
    private final JsonArray blockArray;
    private final JsonArray modifiers;
    private final JsonObject descriptiveStack;

    MmceCTMultiBlockModifierReplacement(String modifierName, JsonArray blockArray, JsonArray modifiers) {
        this(modifierName, blockArray, modifiers, null);
    }

    MmceCTMultiBlockModifierReplacement(String modifierName, JsonArray blockArray, JsonArray modifiers, JsonObject descriptiveStack) {
        this.modifierName = modifierName == null ? "" : modifierName;
        this.blockArray = blockArray == null ? new JsonArray() : blockArray.deepCopy();
        this.modifiers = modifiers == null ? new JsonArray() : modifiers.deepCopy();
        this.descriptiveStack = descriptiveStack == null ? null : descriptiveStack.deepCopy();
    }

    @ZenCodeType.Getter("modifierName")
    public String getModifierName() {
        return modifierName;
    }

    @ZenCodeType.Getter("blockArray")
    public JsonArray getBlockArray() {
        return blockArray.deepCopy();
    }

    @ZenCodeType.Getter("descriptiveStack")
    public JsonObject getDescriptiveStack() {
        return descriptiveStack == null ? new JsonObject() : descriptiveStack.deepCopy();
    }

    JsonArray entries() {
        JsonArray entries = new JsonArray();
        for (JsonElement partElement : blockArray) {
            if (!partElement.isJsonObject()) {
                continue;
            }
            JsonObject entry = partElement.getAsJsonObject().deepCopy();
            if (modifiers.size() == 1) {
                entry.add("modifier", modifiers.get(0).deepCopy());
            } else {
                entry.add("modifiers", modifiers.deepCopy());
            }
            if (!modifierName.isBlank()) {
                entry.addProperty("modifierName", modifierName);
            }
            if (descriptiveStack != null) {
                entry.add("descriptiveStack", descriptiveStack.deepCopy());
            }
            entries.add(entry);
        }
        return entries;
    }

    static JsonArray modifiersToJson(MmceRecipeModifier[] modifiers) {
        JsonArray array = new JsonArray();
        if (modifiers != null) {
            for (MmceRecipeModifier modifier : modifiers) {
                if (modifier != null) {
                    array.add(modifier.json());
                }
            }
        }
        return array;
    }
}
