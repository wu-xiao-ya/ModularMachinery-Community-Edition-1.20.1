package hellfirepvp.modularmachinery.port.integration.kubejs;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.integration.MmceFactoryRecipeThreadBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeExecutor;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;

public final class MmceKubeJSFactoryRecipeThreadBuilder {
    private final MmceFactoryRecipeThreadBuilder builder;
    private final MachineControllerBlockEntity controller;
    private final MmceRecipeExecutor.RecipeRun run;

    MmceKubeJSFactoryRecipeThreadBuilder(String threadName) {
        this.builder = MmceFactoryRecipeThreadBuilder.createCoreThread(threadName);
        this.controller = null;
        this.run = null;
    }

    private MmceKubeJSFactoryRecipeThreadBuilder(MachineControllerBlockEntity controller, MmceRecipeExecutor.RecipeRun run) {
        this.builder = null;
        this.controller = controller;
        this.run = run;
    }

    public static MmceKubeJSFactoryRecipeThreadBuilder of(MachineControllerBlockEntity controller, MmceRecipeExecutor.RecipeRun run) {
        if (controller == null || run == null || !run.isFactoryRun()) {
            return null;
        }
        return new MmceKubeJSFactoryRecipeThreadBuilder(controller, run);
    }

    public MmceKubeJSFactoryRecipeThreadBuilder addRecipe(String recipeName) {
        if (builder != null) {
            builder.addRecipe(recipeName);
        }
        return this;
    }

    public MmceKubeJSFactoryRecipeThreadBuilder addPermanentModifier(String name, MmceRecipeModifier modifier) {
        if (run != null) {
            run.addPermanentModifier(controller, name, modifier);
        } else if (builder != null) {
            builder.addPermanentModifier(name, modifier);
        }
        return this;
    }

    public MmceKubeJSFactoryRecipeThreadBuilder removePermanentModifier(String name) {
        if (run != null) {
            run.removePermanentModifier(controller, name);
        } else if (builder != null) {
            builder.removePermanentModifier(name);
        }
        return this;
    }

    public boolean hasPermanentModifier(String name) {
        return run != null ? run.hasPermanentModifier(controller, name) : builder != null && builder.hasPermanentModifier(name);
    }

    public void addModifier(String name, MmceRecipeModifier modifier) {
        if (run != null) {
            run.addModifier(controller, name, modifier);
        }
    }

    public void removeModifier(String name) {
        if (run != null) {
            run.removeModifier(controller, name);
        }
    }

    public boolean hasModifier(String name) {
        return run != null && run.hasModifier(controller, name);
    }

    public boolean isWorking() {
        return run != null && run.isWorking();
    }

    public boolean isIdle() {
        return run == null || run.getActiveRecipeId().isEmpty();
    }

    public boolean isCoreThread() {
        return run == null || run.isCoreThread();
    }

    public String threadName() {
        return run != null ? run.threadName() : builder == null ? "" : builder.threadName();
    }

    public String getThreadName() {
        return threadName();
    }

    public String getActiveRecipe() {
        return run == null ? "" : run.getActiveRecipeId().map(Object::toString).orElse("");
    }

    public String getActiveRecipeId() {
        return getActiveRecipe();
    }

    public int getProgress() {
        return run == null ? 0 : run.getRecipeProgress();
    }

    public int getParallelism() {
        return run == null ? 1 : run.getActiveRecipeParallelism();
    }

    public String getStatus() {
        return run == null ? "idle" : run.getRecipeStatus().serializedName();
    }

    public String getStatusInfo() {
        return run == null ? "" : run.getRecipeStatusDetail();
    }

    public void setStatusInfo(String info) {
        if (run != null) {
            run.setRecipeStatus(run.getRecipeStatus(), info == null ? "" : info);
        }
    }

    public void setStatus(String status) {
        if (run != null) {
            run.setRecipeStatus(MmceRecipeStatus.bySerializedName(status), run.getRecipeStatusDetail());
        }
    }

    public JsonObject json() {
        if (builder == null) {
            return new JsonObject();
        }
        return builder.json();
    }
}
