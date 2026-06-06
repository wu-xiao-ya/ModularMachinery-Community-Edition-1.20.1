package hellfirepvp.modularmachinery.port.integration.kubejs;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.integration.MmceFactoryRecipeThreadBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;

public final class MmceKubeJSFactoryRecipeThreadBuilder {
    private final MmceFactoryRecipeThreadBuilder builder;

    MmceKubeJSFactoryRecipeThreadBuilder(String threadName) {
        this.builder = MmceFactoryRecipeThreadBuilder.createCoreThread(threadName);
    }

    public MmceKubeJSFactoryRecipeThreadBuilder addRecipe(String recipeName) {
        builder.addRecipe(recipeName);
        return this;
    }

    public MmceKubeJSFactoryRecipeThreadBuilder addPermanentModifier(String name, MmceRecipeModifier modifier) {
        builder.addPermanentModifier(name, modifier);
        return this;
    }

    public MmceKubeJSFactoryRecipeThreadBuilder removePermanentModifier(String name) {
        builder.removePermanentModifier(name);
        return this;
    }

    public boolean hasPermanentModifier(String name) {
        return builder.hasPermanentModifier(name);
    }

    public boolean isWorking() {
        return false;
    }

    public boolean isIdle() {
        return true;
    }

    public boolean isCoreThread() {
        return true;
    }

    public String threadName() {
        return builder.threadName();
    }

    public String getThreadName() {
        return threadName();
    }

    public JsonObject json() {
        return builder.json();
    }
}
