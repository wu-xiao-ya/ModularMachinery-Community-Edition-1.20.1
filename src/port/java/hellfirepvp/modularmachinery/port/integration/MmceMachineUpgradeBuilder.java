package hellfirepvp.modularmachinery.port.integration;

import hellfirepvp.modularmachinery.port.event.MmceEventPhase;
import hellfirepvp.modularmachinery.port.event.MmceMachineEventType;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEventType;
import net.minecraft.world.item.ItemStack;

public final class MmceMachineUpgradeBuilder {
    private final String name;
    private final String localizedName;
    private final float level;
    private final int maxStack;
    private final MmceUpgradeStackBuilder builder;

    private MmceMachineUpgradeBuilder(String name, String localizedName, float level, int maxStack) {
        this.name = name == null || name.isBlank() ? "upgrade" : name.trim();
        this.localizedName = localizedName == null || localizedName.isBlank() ? this.name : localizedName;
        this.level = level;
        this.maxStack = Math.max(1, maxStack);
        this.builder = MmceUpgradeStackBuilder.of("minecraft:paper", this.maxStack)
                .upgrade(this.name, this.localizedName, this.level, this.maxStack);
    }

    public static MmceMachineUpgradeBuilder newBuilder(String name, String localizedName, float level, int maxStack) {
        return new MmceMachineUpgradeBuilder(name, localizedName, level, maxStack);
    }

    public MmceMachineUpgradeBuilder addModifier(boolean stackable, String modifierKey, MmceRecipeModifier modifier) {
        builder.addModifier(stackable, modifierKey, modifier);
        return this;
    }

    public MmceMachineUpgradeBuilder addModifier(MmceRecipeModifier modifier) {
        builder.addModifier(modifier);
        return this;
    }

    public MmceMachineUpgradeBuilder addModifier(MmceRecipeModifier modifier, boolean stackable) {
        builder.addModifier(modifier, stackable);
        return this;
    }

    public MmceMachineUpgradeBuilder itemInputModifier(int operation, double multiplier) {
        return modifier("item", "input", operation, multiplier);
    }

    public MmceMachineUpgradeBuilder itemInputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("item", "input", operation, multiplier, affectChance);
    }

    public MmceMachineUpgradeBuilder itemOutputModifier(int operation, double multiplier) {
        return modifier("item", "output", operation, multiplier);
    }

    public MmceMachineUpgradeBuilder itemOutputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("item", "output", operation, multiplier, affectChance);
    }

    public MmceMachineUpgradeBuilder fluidInputModifier(int operation, double multiplier) {
        return modifier("fluid", "input", operation, multiplier);
    }

    public MmceMachineUpgradeBuilder fluidInputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("fluid", "input", operation, multiplier, affectChance);
    }

    public MmceMachineUpgradeBuilder fluidOutputModifier(int operation, double multiplier) {
        return modifier("fluid", "output", operation, multiplier);
    }

    public MmceMachineUpgradeBuilder fluidOutputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("fluid", "output", operation, multiplier, affectChance);
    }

    public MmceMachineUpgradeBuilder gasInputModifier(int operation, double multiplier) {
        return modifier("gas", "input", operation, multiplier);
    }

    public MmceMachineUpgradeBuilder gasInputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("gas", "input", operation, multiplier, affectChance);
    }

    public MmceMachineUpgradeBuilder gasOutputModifier(int operation, double multiplier) {
        return modifier("gas", "output", operation, multiplier);
    }

    public MmceMachineUpgradeBuilder gasOutputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("gas", "output", operation, multiplier, affectChance);
    }

    public MmceMachineUpgradeBuilder chemicalInputModifier(int operation, double multiplier) {
        return modifier("chemical", "input", operation, multiplier);
    }

    public MmceMachineUpgradeBuilder chemicalInputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("chemical", "input", operation, multiplier, affectChance);
    }

    public MmceMachineUpgradeBuilder chemicalOutputModifier(int operation, double multiplier) {
        return modifier("chemical", "output", operation, multiplier);
    }

    public MmceMachineUpgradeBuilder chemicalOutputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("chemical", "output", operation, multiplier, affectChance);
    }

    public MmceMachineUpgradeBuilder energyInputModifier(int operation, double multiplier) {
        return modifier("energy", "input", operation, multiplier);
    }

    public MmceMachineUpgradeBuilder energyInputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("energy", "input", operation, multiplier, affectChance);
    }

    public MmceMachineUpgradeBuilder energyOutputModifier(int operation, double multiplier) {
        return modifier("energy", "output", operation, multiplier);
    }

    public MmceMachineUpgradeBuilder energyOutputModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("energy", "output", operation, multiplier, affectChance);
    }

    public MmceMachineUpgradeBuilder durationModifier(int operation, double multiplier) {
        return modifier("duration", "", operation, multiplier);
    }

    public MmceMachineUpgradeBuilder durationModifier(int operation, double multiplier, boolean affectChance) {
        return modifier("duration", "", operation, multiplier, affectChance);
    }

    public MmceMachineUpgradeBuilder addDescriptions(String... descriptions) {
        builder.descriptions(descriptions);
        return this;
    }

    public MmceMachineUpgradeBuilder setBusGUIDescriptionHandler(MmceFunction<MmceMachineUpgrade, String[]> handler) {
        MmceMachineUpgradeRegistry.registerBusGuiDescriptionHandler(name, handler);
        return this;
    }

    public MmceMachineUpgradeBuilder addCompatibleMachines(String... machineNames) {
        builder.compatibleMachines(machineNames);
        return this;
    }

    public MmceMachineUpgradeBuilder addIncompatibleMachines(String... machineNames) {
        builder.incompatibleMachines(machineNames);
        return this;
    }

    public MmceMachineUpgradeBuilder addRecipeCheckHandler(MmceUpgradeEventHandler handler) {
        return addPostRecipeCheckHandler(handler);
    }

    public MmceMachineUpgradeBuilder addPreRecipeCheckHandler(MmceUpgradeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.CHECK, MmceEventPhase.START, handler);
    }

    public MmceMachineUpgradeBuilder addPostRecipeCheckHandler(MmceUpgradeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.CHECK, MmceEventPhase.END, handler);
    }

    public MmceMachineUpgradeBuilder addRecipeStartHandler(MmceUpgradeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.START, null, handler);
    }

    public MmceMachineUpgradeBuilder addRecipePreTickHandler(MmceUpgradeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.TICK, MmceEventPhase.START, handler);
    }

    public MmceMachineUpgradeBuilder addRecipePostTickHandler(MmceUpgradeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.TICK, MmceEventPhase.END, handler);
    }

    public MmceMachineUpgradeBuilder addRecipeTickHandler(MmceUpgradeEventHandler handler) {
        return addRecipePostTickHandler(handler);
    }

    public MmceMachineUpgradeBuilder addRecipeFailureHandler(MmceUpgradeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.FAILURE, null, handler);
    }

    public MmceMachineUpgradeBuilder addRecipeFinishHandler(MmceUpgradeEventHandler handler) {
        return recipeHandler(MmceRecipeEventType.FINISH, null, handler);
    }

    public MmceMachineUpgradeBuilder addFactoryRecipeStartHandler(MmceUpgradeEventHandler handler) {
        return addRecipeStartHandler(handler);
    }

    public MmceMachineUpgradeBuilder addFactoryRecipePreTickHandler(MmceUpgradeEventHandler handler) {
        return addRecipePreTickHandler(handler);
    }

    public MmceMachineUpgradeBuilder addFactoryRecipePostTickHandler(MmceUpgradeEventHandler handler) {
        return addRecipePostTickHandler(handler);
    }

    public MmceMachineUpgradeBuilder addFactoryRecipeFailureHandler(MmceUpgradeEventHandler handler) {
        return addRecipeFailureHandler(handler);
    }

    public MmceMachineUpgradeBuilder addFactoryRecipeFinishHandler(MmceUpgradeEventHandler handler) {
        return addRecipeFinishHandler(handler);
    }

    public MmceMachineUpgradeBuilder addMachinePreTickHandler(MmceUpgradeEventHandler handler) {
        return machineHandler(MmceMachineEventType.TICK, MmceEventPhase.START, handler);
    }

    public MmceMachineUpgradeBuilder addMachinePostTickHandler(MmceUpgradeEventHandler handler) {
        return machineHandler(MmceMachineEventType.TICK, MmceEventPhase.END, handler);
    }

    public MmceMachineUpgradeBuilder addMachineTickHandler(MmceUpgradeEventHandler handler) {
        return addMachinePostTickHandler(handler);
    }

    public MmceMachineUpgradeBuilder addStructureFormedHandler(MmceUpgradeEventHandler handler) {
        return machineHandler(MmceMachineEventType.STRUCTURE_FORMED, null, handler);
    }

    public MmceMachineUpgradeBuilder addStructureUpdateHandler(MmceUpgradeEventHandler handler) {
        return machineHandler(MmceMachineEventType.STRUCTURE_UPDATE, null, handler);
    }

    public MmceMachineUpgradeBuilder addControllerGUIRenderHandler(MmceUpgradeEventHandler handler) {
        return machineHandler(MmceMachineEventType.CONTROLLER_GUI_RENDER, null, handler);
    }

    public MmceMachineUpgradeBuilder addGUIRenderHandler(MmceUpgradeEventHandler handler) {
        return addControllerGUIRenderHandler(handler);
    }

    public MmceMachineUpgradeBuilder addControllerButtonClickHandler(MmceUpgradeEventHandler handler) {
        return machineHandler(MmceMachineEventType.CONTROLLER_BUTTON_CLICK, null, handler);
    }

    public MmceMachineUpgradeBuilder addSmartInterfaceUpdateHandler(MmceUpgradeEventHandler handler) {
        return machineHandler(MmceMachineEventType.SMART_INTERFACE_UPDATE, null, handler);
    }

    public ItemStack build() {
        return builder.build();
    }

    public ItemStack applyTo(ItemStack stack) {
        return builder.applyTo(stack);
    }

    public String customDataSnbt() {
        return builder.customDataSnbt();
    }

    public void buildAndRegister() {
        MmceMachineUpgradeRegistry.register(name, builder.registeredData());
    }

    public String getName() {
        return name;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public float getLevel() {
        return level;
    }

    public int getMaxStack() {
        return maxStack;
    }

    private MmceMachineUpgradeBuilder modifier(String target, String io, int operation, double multiplier) {
        return modifier(target, io, operation, multiplier, false);
    }

    private MmceMachineUpgradeBuilder modifier(String target, String io, int operation, double multiplier, boolean affectChance) {
        return addModifier(new MmceRecipeModifier(target, io, multiplier, operation, affectChance));
    }

    private MmceMachineUpgradeBuilder machineHandler(MmceMachineEventType type, MmceEventPhase phase, MmceUpgradeEventHandler handler) {
        if (handler != null) {
            MmceMachineUpgradeRegistry.registerMachineHandler(name, type, (event, upgrade) -> {
                if (phase == null || event.getPhase() == phase) {
                    handler.handle(event, upgrade);
                }
            });
        }
        return this;
    }

    private MmceMachineUpgradeBuilder recipeHandler(MmceRecipeEventType type, MmceEventPhase phase, MmceUpgradeEventHandler handler) {
        if (handler != null) {
            MmceMachineUpgradeRegistry.registerRecipeHandler(name, type, (event, upgrade) -> {
                if (phase == null || event.getPhase() == phase) {
                    handler.handle(event, upgrade);
                }
            });
        }
        return this;
    }
}
