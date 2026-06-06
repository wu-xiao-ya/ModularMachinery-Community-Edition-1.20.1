package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.integration.MmceFactoryRecipeThreadBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeExecutor;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.FactoryRecipeThread")
public final class MmceCTFactoryRecipeThreadBuilder {
    private final MmceFactoryRecipeThreadBuilder builder;
    private final MachineControllerBlockEntity controller;
    private final MmceRecipeExecutor.RecipeRun run;

    private MmceCTFactoryRecipeThreadBuilder(String threadName) {
        this.builder = MmceFactoryRecipeThreadBuilder.createCoreThread(threadName);
        this.controller = null;
        this.run = null;
    }

    private MmceCTFactoryRecipeThreadBuilder(MachineControllerBlockEntity controller, MmceRecipeExecutor.RecipeRun run) {
        this.builder = null;
        this.controller = controller;
        this.run = run;
    }

    public static MmceCTFactoryRecipeThreadBuilder of(MachineControllerBlockEntity controller, MmceRecipeExecutor.RecipeRun run) {
        if (controller == null || run == null || !run.isFactoryRun()) {
            return null;
        }
        return new MmceCTFactoryRecipeThreadBuilder(controller, run);
    }

    @ZenCodeType.Method
    public static MmceCTFactoryRecipeThreadBuilder createCoreThread(String threadName) {
        return new MmceCTFactoryRecipeThreadBuilder(threadName);
    }

    @ZenCodeType.Method
    public MmceCTFactoryRecipeThreadBuilder addRecipe(String recipeName) {
        if (builder != null) {
            builder.addRecipe(recipeName);
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTFactoryRecipeThreadBuilder addPermanentModifier(String name, MmceRecipeModifier modifier) {
        if (run != null) {
            run.addPermanentModifier(controller, name, modifier);
        } else if (builder != null) {
            builder.addPermanentModifier(name, modifier);
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTFactoryRecipeThreadBuilder removePermanentModifier(String name) {
        if (run != null) {
            run.removePermanentModifier(controller, name);
        } else if (builder != null) {
            builder.removePermanentModifier(name);
        }
        return this;
    }

    @ZenCodeType.Method
    public boolean hasPermanentModifier(String name) {
        return run != null ? run.hasPermanentModifier(controller, name) : builder != null && builder.hasPermanentModifier(name);
    }

    @ZenCodeType.Method
    public void addModifier(String name, MmceRecipeModifier modifier) {
        if (run != null) {
            run.addModifier(controller, name, modifier);
        }
    }

    @ZenCodeType.Method
    public void removeModifier(String name) {
        if (run != null) {
            run.removeModifier(controller, name);
        }
    }

    @ZenCodeType.Method
    public boolean hasModifier(String name) {
        return run != null && run.hasModifier(controller, name);
    }

    @ZenCodeType.Getter("isWorking")
    public boolean isWorking() {
        return run != null && run.isWorking();
    }

    @ZenCodeType.Getter("isIdle")
    public boolean isIdle() {
        return run == null || run.getActiveRecipeId().isEmpty();
    }

    @ZenCodeType.Getter("isCoreThread")
    public boolean isCoreThread() {
        return run == null || run.isCoreThread();
    }

    @ZenCodeType.Getter("threadName")
    public String getThreadName() {
        return run != null ? run.threadName() : builder == null ? "" : builder.threadName();
    }

    @ZenCodeType.Getter("activeRecipe")
    public String getActiveRecipe() {
        return run == null ? "" : run.getActiveRecipeId().map(Object::toString).orElse("");
    }

    @ZenCodeType.Getter("activeRecipeId")
    public String getActiveRecipeId() {
        return getActiveRecipe();
    }

    @ZenCodeType.Getter("progress")
    public int getProgress() {
        return run == null ? 0 : run.getRecipeProgress();
    }

    @ZenCodeType.Getter("parallelism")
    public int getParallelism() {
        return run == null ? 1 : run.getActiveRecipeParallelism();
    }

    @ZenCodeType.Getter("status")
    public String getStatus() {
        return run == null ? "idle" : run.getRecipeStatus().serializedName();
    }

    @ZenCodeType.Getter("statusInfo")
    public String getStatusInfo() {
        return run == null ? "" : run.getRecipeStatusDetail();
    }

    @ZenCodeType.Method
    public void setStatusInfo(String info) {
        if (run != null) {
            run.setRecipeStatus(run.getRecipeStatus(), info == null ? "" : info);
        }
    }

    JsonObject json() {
        if (builder == null) {
            return new JsonObject();
        }
        return builder.json();
    }
}
