package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record MmceRecipeAdapterDefinition(
        ResourceLocation id,
        ResourceLocation machineId,
        ResourceLocation adapterId,
        List<MmceRecipeRequirement> requirements,
        List<MmceMachineModifierDefinition> modifiers,
        JsonObject rawJson
) {
    static MmceRecipeAdapterDefinition parse(ResourceLocation sourceId, JsonElement json) {
        JsonObject root = GsonHelper.convertToJsonObject(json, "recipe adapter");
        List<MmceRecipeRequirement> parsedRequirements = new ArrayList<>();
        if (root.has("requirements")) {
            JsonArray requirementArray = GsonHelper.getAsJsonArray(root, "requirements");
            parsedRequirements = new ArrayList<>(requirementArray.size());
            for (JsonElement element : requirementArray) {
                parsedRequirements.add(MmceRecipeRequirement.parse(GsonHelper.convertToJsonObject(element, "requirements[]")));
            }
        }

        List<MmceMachineModifierDefinition> parsedModifiers = new ArrayList<>();
        if (root.has("modifiers")) {
            JsonArray modifierArray = GsonHelper.getAsJsonArray(root, "modifiers");
            parsedModifiers = new ArrayList<>(modifierArray.size());
            for (JsonElement element : modifierArray) {
                JsonObject modifierWrapper = new JsonObject();
                modifierWrapper.add("modifier", element.deepCopy());
                parsedModifiers.add(MmceMachineModifierDefinition.parse(modifierWrapper));
            }
        }

        return new MmceRecipeAdapterDefinition(
                sourceId,
                MmceJsonUtil.modId(GsonHelper.getAsString(root, "machine")),
                MmceJsonUtil.modId(GsonHelper.getAsString(root, "adapter")),
                List.copyOf(parsedRequirements),
                List.copyOf(parsedModifiers),
                root.deepCopy()
        );
    }
}
