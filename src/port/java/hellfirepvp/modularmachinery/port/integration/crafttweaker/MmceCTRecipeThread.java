package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeExecutor;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.RecipeThread")
public final class MmceCTRecipeThread {
    private final MachineControllerBlockEntity controller;
    private final MmceRecipeExecutor.RecipeRun run;

    private MmceCTRecipeThread(MachineControllerBlockEntity controller, MmceRecipeExecutor.RecipeRun run) {
        this.controller = controller;
        this.run = run;
    }

    public static MmceCTRecipeThread of(MachineControllerBlockEntity controller, MmceRecipeExecutor.RecipeRun run) {
        if (controller == null || run == null) {
            return null;
        }
        return new MmceCTRecipeThread(controller, run);
    }

    @ZenCodeType.Getter("activeRecipe")
    public String getActiveRecipe() {
        return run.getActiveRecipeId().map(Object::toString).orElse("");
    }

    @ZenCodeType.Getter("activeRecipeId")
    public String getActiveRecipeId() {
        return getActiveRecipe();
    }

    @ZenCodeType.Getter("progress")
    public int getProgress() {
        return run.getRecipeProgress();
    }

    @ZenCodeType.Getter("parallelism")
    public int getParallelism() {
        return run.getActiveRecipeParallelism();
    }

    @ZenCodeType.Getter("status")
    public String getStatus() {
        return run.getRecipeStatus().serializedName();
    }

    @ZenCodeType.Getter("statusInfo")
    public String getStatusInfo() {
        return run.getRecipeStatusDetail();
    }

    @ZenCodeType.Getter("isWorking")
    public boolean isWorking() {
        return run.isWorking();
    }

    @ZenCodeType.Getter("isIdle")
    public boolean isIdle() {
        return run.getActiveRecipeId().isEmpty();
    }

    @ZenCodeType.Getter("isCoreThread")
    public boolean isCoreThread() {
        return run.isCoreThread();
    }

    @ZenCodeType.Getter("threadName")
    public String getThreadName() {
        return run.threadName();
    }

    @ZenCodeType.Getter("factoryRun")
    public boolean isFactoryRun() {
        return run.isFactoryRun();
    }

    @ZenCodeType.Method
    public void addModifier(String name, MmceRecipeModifier modifier) {
        run.addModifier(controller, name, modifier);
    }

    @ZenCodeType.Method
    public void removeModifier(String name) {
        run.removeModifier(controller, name);
    }

    @ZenCodeType.Method
    public boolean hasModifier(String name) {
        return run.hasModifier(controller, name);
    }

    @ZenCodeType.Method
    public void addPermanentModifier(String name, MmceRecipeModifier modifier) {
        run.addPermanentModifier(controller, name, modifier);
    }

    @ZenCodeType.Method
    public void removePermanentModifier(String name) {
        run.removePermanentModifier(controller, name);
    }

    @ZenCodeType.Method
    public boolean hasPermanentModifier(String name) {
        return run.hasPermanentModifier(controller, name);
    }

    @ZenCodeType.Method
    public void setStatusInfo(String info) {
        run.setRecipeStatus(run.getRecipeStatus(), info == null ? "" : info);
    }

    @ZenCodeType.Method
    public void setStatus(String status) {
        run.setRecipeStatus(MmceRecipeStatus.bySerializedName(status), run.getRecipeStatusDetail());
    }
}
