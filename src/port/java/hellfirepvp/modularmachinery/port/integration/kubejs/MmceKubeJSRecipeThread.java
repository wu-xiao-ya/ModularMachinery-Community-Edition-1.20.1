package hellfirepvp.modularmachinery.port.integration.kubejs;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeExecutor;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;

public final class MmceKubeJSRecipeThread {
    private final MachineControllerBlockEntity controller;
    private final MmceRecipeExecutor.RecipeRun run;

    private MmceKubeJSRecipeThread(MachineControllerBlockEntity controller, MmceRecipeExecutor.RecipeRun run) {
        this.controller = controller;
        this.run = run;
    }

    public static MmceKubeJSRecipeThread of(MachineControllerBlockEntity controller, MmceRecipeExecutor.RecipeRun run) {
        if (controller == null || run == null) {
            return null;
        }
        return new MmceKubeJSRecipeThread(controller, run);
    }

    public String getActiveRecipe() {
        return run.getActiveRecipeId().map(Object::toString).orElse("");
    }

    public String getActiveRecipeId() {
        return getActiveRecipe();
    }

    public int getProgress() {
        return run.getRecipeProgress();
    }

    public int getParallelism() {
        return run.getActiveRecipeParallelism();
    }

    public String getStatus() {
        return run.getRecipeStatus().serializedName();
    }

    public String getStatusInfo() {
        return run.getRecipeStatusDetail();
    }

    public boolean isWorking() {
        return run.isWorking();
    }

    public boolean isIdle() {
        return run.getActiveRecipeId().isEmpty();
    }

    public boolean isCoreThread() {
        return run.isCoreThread();
    }

    public String getThreadName() {
        return run.threadName();
    }

    public boolean isFactoryRun() {
        return run.isFactoryRun();
    }

    public void addModifier(String name, MmceRecipeModifier modifier) {
        run.addModifier(controller, name, modifier);
    }

    public void removeModifier(String name) {
        run.removeModifier(controller, name);
    }

    public boolean hasModifier(String name) {
        return run.hasModifier(controller, name);
    }

    public void addPermanentModifier(String name, MmceRecipeModifier modifier) {
        run.addPermanentModifier(controller, name, modifier);
    }

    public void removePermanentModifier(String name) {
        run.removePermanentModifier(controller, name);
    }

    public boolean hasPermanentModifier(String name) {
        return run.hasPermanentModifier(controller, name);
    }

    public void setStatusInfo(String info) {
        run.setRecipeStatus(run.getRecipeStatus(), info == null ? "" : info);
    }

    public void setStatus(String status) {
        run.setRecipeStatus(MmceRecipeStatus.bySerializedName(status), run.getRecipeStatusDetail());
    }
}
