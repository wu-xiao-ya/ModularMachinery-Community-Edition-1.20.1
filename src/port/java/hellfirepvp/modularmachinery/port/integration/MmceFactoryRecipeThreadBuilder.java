package hellfirepvp.modularmachinery.port.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MmceFactoryRecipeThreadBuilder {
    private final String threadName;
    private final JsonArray recipes = new JsonArray();
    private final JsonObject permanentModifiers = new JsonObject();

    public MmceFactoryRecipeThreadBuilder(String threadName) {
        this.threadName = threadName == null ? "" : threadName.trim();
    }

    public static MmceFactoryRecipeThreadBuilder createCoreThread(String threadName) {
        return new MmceFactoryRecipeThreadBuilder(threadName);
    }

    public MmceFactoryRecipeThreadBuilder addRecipe(String recipeName) {
        if (recipeName != null && !recipeName.isBlank()) {
            recipes.add(recipeName.trim());
        }
        return this;
    }

    public String threadName() {
        return threadName;
    }

    public MmceFactoryRecipeThreadBuilder addPermanentModifier(String key, MmceRecipeModifier modifier) {
        if (key != null && !key.isBlank() && modifier != null) {
            permanentModifiers.add(key.trim(), modifier.json());
        }
        return this;
    }

    public MmceFactoryRecipeThreadBuilder removePermanentModifier(String key) {
        if (key != null && !key.isBlank()) {
            permanentModifiers.remove(key.trim());
        }
        return this;
    }

    public boolean hasPermanentModifier(String key) {
        return key != null && permanentModifiers.has(key.trim());
    }

    public JsonObject json() {
        JsonObject object = new JsonObject();
        object.addProperty("threadName", threadName);
        if (!recipes.isEmpty()) {
            object.add("recipes", recipes.deepCopy());
        }
        if (!permanentModifiers.isEmpty()) {
            object.add("permanentModifiers", permanentModifiers.deepCopy());
        }
        return object;
    }
}
