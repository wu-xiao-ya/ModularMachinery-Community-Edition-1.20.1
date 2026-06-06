package hellfirepvp.modularmachinery.port.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.data.MmceScriptDataRegistry;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;

public final class MmceMachineDefinitionPatcher {
    public static boolean patch(String machineName, String sourceDirectory, Consumer<JsonObject> patcher) {
        ResourceLocation machineId = machineId(machineName);
        if (patcher == null) {
            return false;
        }
        MmceScriptDataRegistry.registerMachinePatch(machineId, sourceId(sourceDirectory, machineId), patcher);
        return true;
    }

    public static JsonArray array(JsonObject root, String key) {
        if (root.has(key) && root.get(key).isJsonArray()) {
            return root.getAsJsonArray(key);
        }
        JsonArray array = new JsonArray();
        root.add(key, array);
        return array;
    }

    public static ResourceLocation machineId(String machineName) {
        if (machineName == null || machineName.isBlank()) {
            throw new IllegalArgumentException("MMCE machine name cannot be blank.");
        }
        String normalized = machineName.trim();
        return normalized.indexOf(':') >= 0
                ? ResourceLocation.parse(normalized)
                : ResourceLocation.fromNamespaceAndPath(hellfirepvp.modularmachinery.port.ModularMachineryNeoForge.MODID, normalized);
    }

    private static ResourceLocation sourceId(String directory, ResourceLocation machineId) {
        String safeDirectory = directory == null || directory.isBlank() ? "patches" : directory.trim();
        return ResourceLocation.fromNamespaceAndPath(
                machineId.getNamespace(),
                safeDirectory + "/" + machineId.getPath().replaceAll("[^a-z0-9_./-]", "_")
        );
    }

    private MmceMachineDefinitionPatcher() {
    }
}
