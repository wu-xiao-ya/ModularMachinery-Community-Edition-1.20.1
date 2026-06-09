package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEvent;
import hellfirepvp.modularmachinery.port.event.MmceResultChanceCreateEvent;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.RecipeEvent")
public final class MmceCTRecipeEvent extends MmceCTMachineEvent {
    private final MmceRecipeEvent event;

    private MmceCTRecipeEvent(MmceRecipeEvent event) {
        super(event);
        this.event = event;
    }

    static MmceCTRecipeEvent of(MmceRecipeEvent event) {
        return event == null ? null : new MmceCTRecipeEvent(event);
    }

    @ZenCodeType.Getter("recipeId")
    public String getRecipeId() {
        return event.getRecipeId();
    }

    @ZenCodeType.Getter("recipeType")
    public String getRecipeType() {
        return event.getRecipeEventTypeName();
    }

    @ZenCodeType.Getter("recipeEventType")
    public String getRecipeEventType() {
        return getRecipeType();
    }

    @ZenCodeType.Getter("progress")
    public int getProgress() {
        return event.getProgress();
    }

    @ZenCodeType.Getter("maxProgress")
    public int getMaxProgress() {
        return event.getMaxProgress();
    }

    @ZenCodeType.Getter("parallelism")
    public int getParallelism() {
        return event.getParallelism();
    }

    @ZenCodeType.Setter("parallelism")
    public void setParallelism(int parallelism) {
        event.setParallelism(parallelism);
    }

    @ZenCodeType.Getter("factoryRun")
    public boolean isFactoryRun() {
        return event.isFactoryRun();
    }

    @ZenCodeType.Getter("coreThread")
    public boolean isCoreThread() {
        return event.isCoreThread();
    }

    @ZenCodeType.Getter("threadName")
    public String getThreadName() {
        return event.getThreadName();
    }

    @ZenCodeType.Getter("status")
    public String getStatus() {
        return event.getStatus();
    }

    @ZenCodeType.Getter("cause")
    public String getCause() {
        return event.getCause();
    }

    @ZenCodeType.Getter("failed")
    public boolean isFailed() {
        return event.isFailed();
    }

    @ZenCodeType.Getter("destructRecipe")
    public boolean isDestructRecipe() {
        return event.isDestructRecipe();
    }

    @ZenCodeType.Setter("destructRecipe")
    public void setDestructRecipe(boolean destructRecipe) {
        event.setDestructRecipe(destructRecipe);
    }

    @ZenCodeType.Getter("preventProgressing")
    public boolean isPreventProgressing() {
        return event.isPreventProgressing();
    }

    @ZenCodeType.Getter("recipeThread")
    public MmceCTRecipeThread getRecipeThread() {
        return MmceCTRecipeThread.of(event.getController(), event.getRecipeRun());
    }

    @ZenCodeType.Getter("factoryRecipeThread")
    public MmceCTFactoryRecipeThreadBuilder getFactoryRecipeThread() {
        return MmceCTFactoryRecipeThreadBuilder.of(event.getController(), event.getRecipeRun());
    }

    @ZenCodeType.Getter("requirementType")
    public String getRequirementType() {
        return event instanceof MmceResultChanceCreateEvent chanceEvent ? chanceEvent.getRequirementType() : "";
    }

    @ZenCodeType.Getter("ioType")
    public String getIoType() {
        return event instanceof MmceResultChanceCreateEvent chanceEvent ? chanceEvent.getIoType() : "";
    }

    @ZenCodeType.Getter("chance")
    public float getChance() {
        return event instanceof MmceResultChanceCreateEvent chanceEvent ? chanceEvent.getChance() : 0.0F;
    }

    @ZenCodeType.Setter("chance")
    public void setChance(float chance) {
        if (event instanceof MmceResultChanceCreateEvent chanceEvent) {
            chanceEvent.setChance(chance);
        }
    }

    @ZenCodeType.Method
    public void preventProgressing(String reason) {
        event.preventProgressing(reason);
    }

    @ZenCodeType.Method
    public void setFailed(String reason) {
        event.setFailed(reason);
    }

    @ZenCodeType.Method
    public void setFailed(boolean destructRecipe, String reason) {
        event.setFailed(destructRecipe, reason);
    }

    @ZenCodeType.Method
    public MmceCTRecipeThread recipeThread() {
        return getRecipeThread();
    }

    @ZenCodeType.Method
    public MmceCTFactoryRecipeThreadBuilder factoryRecipeThread() {
        return getFactoryRecipeThread();
    }

    @ZenCodeType.Method
    public MmceCTRecipeEvent chance(float chance) {
        setChance(chance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTRecipeEvent setResultChance(float chance) {
        return chance(chance);
    }
}
