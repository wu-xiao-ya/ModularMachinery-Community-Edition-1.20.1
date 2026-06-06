package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.integration.crafttweaker.MmceCTFactoryRecipeThreadBuilder;
import hellfirepvp.modularmachinery.port.integration.crafttweaker.MmceCTRecipeThread;
import hellfirepvp.modularmachinery.port.integration.kubejs.MmceKubeJSFactoryRecipeThreadBuilder;
import hellfirepvp.modularmachinery.port.integration.kubejs.MmceKubeJSRecipeThread;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeExecutor;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.RecipeEvent")
public class MmceRecipeEvent extends MmceMachineEvent {
    private final ResourceLocation recipeId;
    private final MmceRecipeEventType recipeEventType;
    private final int progress;
    private final int maxProgress;
    private int parallelism;
    private final boolean factoryRun;
    private final boolean coreThread;
    private final String threadName;
    private String cause;
    private MmceRecipeStatus status;
    private boolean failed;
    private boolean destructRecipe;
    private boolean preventProgressing;
    private MmceRecipeExecutor.RecipeRun run;

    public MmceRecipeEvent(
            MachineControllerBlockEntity controller,
            ResourceLocation machineId,
            ResourceLocation recipeId,
            MmceRecipeEventType recipeEventType,
            MmceEventPhase phase,
            int progress,
            int maxProgress,
            int parallelism,
            boolean factoryRun,
            boolean coreThread,
            String threadName,
            MmceRecipeStatus status,
            String cause,
            boolean destructRecipe
    ) {
        super(controller, machineId, MmceMachineEventType.TICK, phase);
        this.recipeId = recipeId;
        this.recipeEventType = recipeEventType;
        this.progress = Math.max(0, progress);
        this.maxProgress = Math.max(1, maxProgress);
        this.parallelism = Math.max(1, parallelism);
        this.factoryRun = factoryRun;
        this.coreThread = coreThread;
        this.threadName = threadName == null ? "" : threadName;
        this.status = status == null ? MmceRecipeStatus.RUNNING : status;
        this.cause = cause == null ? "" : cause;
        this.destructRecipe = destructRecipe;
    }

    public MmceRecipeEvent bindRun(MmceRecipeExecutor.RecipeRun run) {
        this.run = run;
        return this;
    }

    @ZenCodeType.Getter("recipeThread")
    public MmceCTRecipeThread getRecipeThread() {
        return MmceCTRecipeThread.of(getController(), run);
    }

    @ZenCodeType.Getter("factoryRecipeThread")
    public MmceCTFactoryRecipeThreadBuilder getFactoryRecipeThread() {
        return MmceCTFactoryRecipeThreadBuilder.of(getController(), run);
    }

    public MmceKubeJSRecipeThread getKubeJSRecipeThread() {
        return MmceKubeJSRecipeThread.of(getController(), run);
    }

    public MmceKubeJSRecipeThread getKubeRecipeThread() {
        return getKubeJSRecipeThread();
    }

    public MmceKubeJSFactoryRecipeThreadBuilder getKubeJSFactoryRecipeThread() {
        return MmceKubeJSFactoryRecipeThreadBuilder.of(getController(), run);
    }

    public MmceKubeJSFactoryRecipeThreadBuilder getKubeFactoryRecipeThread() {
        return getKubeJSFactoryRecipeThread();
    }

    @ZenCodeType.Getter("recipeId")
    public String getRecipeId() {
        return recipeId == null ? "" : recipeId.toString();
    }

    public ResourceLocation getRecipeResourceLocation() {
        return recipeId;
    }

    @Override
    @ZenCodeType.Getter("type")
    public String getType() {
        return getRecipeEventTypeName();
    }

    @ZenCodeType.Getter("recipeEventType")
    public String getRecipeEventTypeName() {
        return recipeEventType.name().toLowerCase(java.util.Locale.ROOT);
    }

    public MmceRecipeEventType getRecipeEventType() {
        return recipeEventType;
    }

    @ZenCodeType.Getter("progress")
    public int getProgress() {
        return progress;
    }

    @ZenCodeType.Getter("maxProgress")
    public int getMaxProgress() {
        return maxProgress;
    }

    @ZenCodeType.Getter("parallelism")
    public int getParallelism() {
        return parallelism;
    }

    @ZenCodeType.Setter("parallelism")
    public void setParallelism(int parallelism) {
        this.parallelism = Math.max(1, Math.min(this.parallelism, parallelism));
    }

    @ZenCodeType.Getter("factoryRun")
    public boolean isFactoryRun() {
        return factoryRun;
    }

    @ZenCodeType.Getter("coreThread")
    public boolean isCoreThread() {
        return coreThread;
    }

    @ZenCodeType.Getter("threadName")
    public String getThreadName() {
        return threadName;
    }

    @ZenCodeType.Getter("status")
    public String getStatus() {
        return status.serializedName();
    }

    public MmceRecipeStatus getRecipeStatus() {
        return status;
    }

    @ZenCodeType.Getter("cause")
    public String getCause() {
        return cause;
    }

    @ZenCodeType.Getter("failed")
    public boolean isFailed() {
        return failed;
    }

    @ZenCodeType.Getter("destructRecipe")
    public boolean isDestructRecipe() {
        return destructRecipe;
    }

    @ZenCodeType.Setter("destructRecipe")
    public void setDestructRecipe(boolean destructRecipe) {
        this.destructRecipe = destructRecipe;
    }

    @ZenCodeType.Getter("preventProgressing")
    public boolean isPreventProgressing() {
        return preventProgressing;
    }

    @ZenCodeType.Method
    public void preventProgressing(String reason) {
        this.preventProgressing = true;
        this.cause = reason == null ? "" : reason;
        setCanceled(true);
    }

    @ZenCodeType.Method
    public void setFailed(String reason) {
        setFailed(false, reason);
    }

    @ZenCodeType.Method
    public void setFailed(boolean destructRecipe, String reason) {
        this.failed = true;
        this.destructRecipe = destructRecipe;
        this.cause = reason == null ? "" : reason;
        this.status = MmceRecipeStatus.FAILED;
        setCanceled(true);
    }

    @Override
    @ZenCodeType.Method
    public void addModifier(String key, MmceRecipeModifier modifier) {
        if (run != null) {
            run.addModifier(getController(), key, modifier);
        } else {
            super.addModifier(key, modifier);
        }
    }

    @Override
    @ZenCodeType.Method
    public void removeModifier(String key) {
        if (run != null) {
            run.removeModifier(getController(), key);
        } else {
            super.removeModifier(key);
        }
    }

    @Override
    @ZenCodeType.Method
    public void addPermanentModifier(String key, MmceRecipeModifier modifier) {
        if (run != null) {
            run.addPermanentModifier(getController(), key, modifier);
        } else {
            super.addPermanentModifier(key, modifier);
        }
    }

    @Override
    @ZenCodeType.Method
    public void removePermanentModifier(String key) {
        if (run != null) {
            run.removePermanentModifier(getController(), key);
        } else {
            super.removePermanentModifier(key);
        }
    }

    @Override
    @ZenCodeType.Method
    public boolean hasModifier(String key) {
        return run != null ? run.hasModifier(getController(), key) : super.hasModifier(key);
    }

    @Override
    @ZenCodeType.Method
    public boolean hasPermanentModifier(String key) {
        return run != null ? run.hasPermanentModifier(getController(), key) : super.hasPermanentModifier(key);
    }
}
