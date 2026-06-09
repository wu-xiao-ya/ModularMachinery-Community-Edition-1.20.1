package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.integration.kubejs.MmceKubeJSFactoryRecipeThreadBuilder;
import hellfirepvp.modularmachinery.port.integration.kubejs.MmceKubeJSRecipeThread;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeExecutor;
import hellfirepvp.modularmachinery.port.recipe.MmceRecipeStatus;
import net.minecraft.resources.ResourceLocation;

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

    public MmceRecipeExecutor.RecipeRun getRecipeRun() {
        return run;
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
    public String getRecipeId() {
        return recipeId == null ? "" : recipeId.toString();
    }

    public ResourceLocation getRecipeResourceLocation() {
        return recipeId;
    }

    @Override
    public String getType() {
        return getRecipeEventTypeName();
    }
    public String getRecipeEventTypeName() {
        return recipeEventType.name().toLowerCase(java.util.Locale.ROOT);
    }

    public MmceRecipeEventType getRecipeEventType() {
        return recipeEventType;
    }
    public int getProgress() {
        return progress;
    }
    public int getMaxProgress() {
        return maxProgress;
    }
    public int getParallelism() {
        return parallelism;
    }
    public void setParallelism(int parallelism) {
        this.parallelism = Math.max(1, Math.min(this.parallelism, parallelism));
    }
    public boolean isFactoryRun() {
        return factoryRun;
    }
    public boolean isCoreThread() {
        return coreThread;
    }
    public String getThreadName() {
        return threadName;
    }
    public String getStatus() {
        return status.serializedName();
    }

    public MmceRecipeStatus getRecipeStatus() {
        return status;
    }
    public String getCause() {
        return cause;
    }
    public boolean isFailed() {
        return failed;
    }
    public boolean isDestructRecipe() {
        return destructRecipe;
    }
    public void setDestructRecipe(boolean destructRecipe) {
        this.destructRecipe = destructRecipe;
    }
    public boolean isPreventProgressing() {
        return preventProgressing;
    }
    public void preventProgressing(String reason) {
        this.preventProgressing = true;
        this.cause = reason == null ? "" : reason;
        setCanceled(true);
    }
    public void setFailed(String reason) {
        setFailed(false, reason);
    }
    public void setFailed(boolean destructRecipe, String reason) {
        this.failed = true;
        this.destructRecipe = destructRecipe;
        this.cause = reason == null ? "" : reason;
        this.status = MmceRecipeStatus.FAILED;
        setCanceled(true);
    }

    @Override
    public void addModifier(String key, MmceRecipeModifier modifier) {
        if (run != null) {
            run.addModifier(getController(), key, modifier);
        } else {
            super.addModifier(key, modifier);
        }
    }

    @Override
    public void removeModifier(String key) {
        if (run != null) {
            run.removeModifier(getController(), key);
        } else {
            super.removeModifier(key);
        }
    }

    @Override
    public void addPermanentModifier(String key, MmceRecipeModifier modifier) {
        if (run != null) {
            run.addPermanentModifier(getController(), key, modifier);
        } else {
            super.addPermanentModifier(key, modifier);
        }
    }

    @Override
    public void removePermanentModifier(String key) {
        if (run != null) {
            run.removePermanentModifier(getController(), key);
        } else {
            super.removePermanentModifier(key);
        }
    }

    @Override
    public boolean hasModifier(String key) {
        return run != null ? run.hasModifier(getController(), key) : super.hasModifier(key);
    }

    @Override
    public boolean hasPermanentModifier(String key) {
        return run != null ? run.hasPermanentModifier(getController(), key) : super.hasPermanentModifier(key);
    }
}
