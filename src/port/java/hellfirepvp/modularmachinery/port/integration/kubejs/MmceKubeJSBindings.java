package hellfirepvp.modularmachinery.port.integration.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.data.MmceScriptDataRegistry;
import hellfirepvp.modularmachinery.port.event.MmceControllerButtonClickEvent;
import hellfirepvp.modularmachinery.port.event.MmceControllerGUIRenderEvent;
import hellfirepvp.modularmachinery.port.event.MmceEventPhase;
import hellfirepvp.modularmachinery.port.event.MmceEventRegistry;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeFailureEvent;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeFinishEvent;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeStartEvent;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeTickEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineEventHandler;
import hellfirepvp.modularmachinery.port.event.MmceMachineEventType;
import hellfirepvp.modularmachinery.port.event.MmceMachineStructureFormedEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineStructureUpdateEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineTickEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeCheckEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEventHandler;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEventType;
import hellfirepvp.modularmachinery.port.event.MmceRecipeFailureEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeFinishEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeStartEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeTickEvent;
import hellfirepvp.modularmachinery.port.event.MmceResultChanceCreateEvent;
import hellfirepvp.modularmachinery.port.event.MmceSmartInterfaceUpdateEvent;
import hellfirepvp.modularmachinery.port.integration.MmceBlockCheckerRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceItemCallbackRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceItemChecker;
import hellfirepvp.modularmachinery.port.integration.MmceItemModifier;
import hellfirepvp.modularmachinery.port.integration.MmceDynamicMachineUpgradeBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceMachineDefinitionPatcher;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeHelper;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeAdapterBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifierBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceSmartInterfaceTypeBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceUpgradeStackBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceUpgradeEventHandler;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public final class MmceKubeJSBindings {
    private static final List<String> BLOCK_IDS = List.of(
            "blockcontroller",
            "blockfactorycontroller",
            "blockcasing",
            "blockinputbus",
            "blockoutputbus",
            "blockfluidinputhatch",
            "blockfluidoutputhatch",
            "blockfluidprocessorhatch",
            "blockenergyinputhatch",
            "blockenergyoutputhatch",
            "blocksmartinterface",
            "blockparallelcontroller",
            "blockupgradebus"
    );

    private static final List<String> ITEM_IDS = List.of(
            "itemblueprint",
            "itemmodularium",
            "itemconstructtool",
            "machine_projector"
    );

    private MmceKubeJSBindings() {
    }

    public static String modId() {
        return ModularMachineryNeoForge.MODID;
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, path);
    }

    public static List<String> blockIds() {
        return BLOCK_IDS;
    }

    public static List<String> itemIds() {
        return ITEM_IDS;
    }

    public static void clearScriptDefinitions() {
        MmceScriptDataRegistry.clear();
        MmceMachineUpgradeRegistry.clear();
        MmceEventRegistry.clear();
        MmceBlockCheckerRegistry.clear();
        MmceItemCallbackRegistry.clear();
        MmceMachineUpgradeHelper.clear();
    }

    public static void clearEventHandlers() {
        MmceEventRegistry.clear();
    }

    public static int scriptMachineCount() {
        return MmceScriptDataRegistry.scriptMachineCount();
    }

    public static int scriptRecipeCount() {
        return MmceScriptDataRegistry.scriptRecipeCount();
    }

    public static int scriptAdapterCount() {
        return MmceScriptDataRegistry.scriptAdapterCount();
    }

    public static int scriptMachineUpgradeCount() {
        return MmceMachineUpgradeRegistry.count();
    }

    public static Class<MmceKubeJSEvents> events() {
        return MmceKubeJSEvents.class;
    }

    public static MmceKubeJSMachineBuilder machine(String registryName) {
        return machine(registryName, registryName);
    }

    public static MmceKubeJSMachineBuilder machine(String registryName, String localizedName) {
        return new MmceKubeJSMachineBuilder(registryName, localizedName);
    }

    public static MmceKubeJSMachineBuilder machineBuilder(String registryName) {
        return machine(registryName);
    }

    public static MmceKubeJSMachineBuilder machineBuilder(String registryName, String localizedName) {
        return machine(registryName, localizedName);
    }

    public static MmceKubeJSMachineBuilder newMachineBuilder(String registryName) {
        return machine(registryName);
    }

    public static MmceKubeJSMachineBuilder newMachineBuilder(String registryName, String localizedName) {
        return machine(registryName, localizedName);
    }

    public static MmceKubeJSMachineBuilder registerMachine(String registryName) {
        return machine(registryName);
    }

    public static MmceKubeJSMachineBuilder registerMachine(String registryName, String localizedName) {
        return machine(registryName, localizedName);
    }

    public static MmceKubeJSFactoryRecipeThreadBuilder coreThread(String threadName) {
        return new MmceKubeJSFactoryRecipeThreadBuilder(threadName);
    }

    public static MmceKubeJSRecipeThread recipeThread(MmceRecipeEvent event) {
        return event == null ? null : event.getKubeJSRecipeThread();
    }

    public static MmceKubeJSFactoryRecipeThreadBuilder factoryRecipeThread(MmceRecipeEvent event) {
        return event == null ? null : event.getKubeJSFactoryRecipeThread();
    }

    public static MmceKubeJSDynamicPatternBuilder dynamicPattern(String name) {
        return new MmceKubeJSDynamicPatternBuilder(name);
    }

    public static MmceKubeJSDynamicPatternBuilder dynamicPatternBuilder(String name) {
        return dynamicPattern(name);
    }

    public static MmceKubeJSDynamicPatternBuilder newDynamicPatternBuilder(String name) {
        return dynamicPattern(name);
    }

    public static MmceSmartInterfaceTypeBuilder smartInterfaceType(String type, float defaultValue) {
        return MmceSmartInterfaceTypeBuilder.create(type, defaultValue);
    }

    public static MmceKubeJSBlockArrayBuilder blockArray() {
        return MmceKubeJSBlockArrayBuilder.newBuilder();
    }

    public static MmceKubeJSBlockArrayBuilder blockArray(MmceKubeJSBlockArrayBuilder blockArray) {
        return MmceKubeJSBlockArrayBuilder.newBuilder(blockArray);
    }

    public static MmceKubeJSBlockArrayBuilder blockArrayBuilder() {
        return blockArray();
    }

    public static MmceKubeJSBlockArrayBuilder blockArrayBuilder(MmceKubeJSBlockArrayBuilder blockArray) {
        return blockArray(blockArray);
    }

    public static MmceKubeJSBlockArrayBuilder newBlockArrayBuilder() {
        return blockArray();
    }

    public static MmceKubeJSBlockArrayBuilder newBlockArrayBuilder(MmceKubeJSBlockArrayBuilder blockArray) {
        return blockArray(blockArray);
    }

    public static MmceKubeJSMultiBlockModifierBuilder multiBlockModifierBuilder() {
        return MmceKubeJSMultiBlockModifierBuilder.newBuilder();
    }

    public static MmceKubeJSMultiBlockModifierBuilder multiBlockModifierBuilder(String modifierName) {
        return MmceKubeJSMultiBlockModifierBuilder.newBuilder(modifierName);
    }

    public static MmceKubeJSMultiBlockModifierBuilder multiblockModifierBuilder() {
        return multiBlockModifierBuilder();
    }

    public static MmceKubeJSMultiBlockModifierBuilder multiblockModifierBuilder(String modifierName) {
        return multiBlockModifierBuilder(modifierName);
    }

    public static MmceKubeJSMultiBlockModifierBuilder newMultiBlockModifierBuilder() {
        return multiBlockModifierBuilder();
    }

    public static MmceKubeJSMultiBlockModifierBuilder newMultiBlockModifierBuilder(String modifierName) {
        return multiBlockModifierBuilder(modifierName);
    }

    public static MmceKubeJSMultiBlockModifierBuilder newMultiblockModifierBuilder() {
        return multiBlockModifierBuilder();
    }

    public static MmceKubeJSMultiBlockModifierBuilder newMultiblockModifierBuilder(String modifierName) {
        return multiBlockModifierBuilder(modifierName);
    }

    public static MmceKubeJSRecipeBuilder recipe(String registryName, String machine, int recipeTime) {
        return new MmceKubeJSRecipeBuilder(registryName, machine, recipeTime);
    }

    public static MmceKubeJSRecipeBuilder recipe(String registryName, String machine, int recipeTime, int priority) {
        return recipe(registryName, machine, recipeTime).priority(priority);
    }

    public static MmceKubeJSRecipeBuilder recipe(String registryName, String machine, int recipeTime,
                                                 int priority, boolean cancelIfPerTickFails) {
        return recipe(registryName, machine, recipeTime, priority)
                .cancelIfPerTickFails(cancelIfPerTickFails);
    }

    public static MmceKubeJSRecipeBuilder newRecipeBuilder(String registryName, String machine, int recipeTime) {
        return recipe(registryName, machine, recipeTime);
    }

    public static MmceKubeJSRecipeBuilder newRecipeBuilder(String registryName, String machine, int recipeTime, int priority) {
        return recipe(registryName, machine, recipeTime, priority);
    }

    public static MmceKubeJSRecipeBuilder newRecipeBuilder(String registryName, String machine, int recipeTime,
                                                           int priority, boolean cancelIfPerTickFails) {
        return recipe(registryName, machine, recipeTime, priority, cancelIfPerTickFails);
    }

    public static MmceKubeJSRecipeBuilder recipeBuilder(String registryName, String machine, int recipeTime) {
        return recipe(registryName, machine, recipeTime);
    }

    public static MmceKubeJSRecipeBuilder recipeBuilder(String registryName, String machine, int recipeTime, int priority) {
        return recipe(registryName, machine, recipeTime, priority);
    }

    public static MmceKubeJSRecipeBuilder recipeBuilder(String registryName, String machine, int recipeTime,
                                                        int priority, boolean cancelIfPerTickFails) {
        return recipe(registryName, machine, recipeTime, priority, cancelIfPerTickFails);
    }

    public static MmceKubeJSIngredientArrayPrimer ingredientArray() {
        return new MmceKubeJSIngredientArrayPrimer();
    }

    public static MmceKubeJSIngredientArrayPrimer ingredientArrayPrimer() {
        return ingredientArray();
    }

    public static String registerItemChecker(String checkerId, MmceItemChecker checker) {
        return MmceItemCallbackRegistry.registerChecker(checkerId, checker);
    }

    public static String itemChecker(String checkerId, MmceItemChecker checker) {
        return registerItemChecker(checkerId, checker);
    }

    public static String registerItemModifier(String modifierId, MmceItemModifier modifier) {
        return MmceItemCallbackRegistry.registerModifier(modifierId, modifier);
    }

    public static String itemModifier(String modifierId, MmceItemModifier modifier) {
        return registerItemModifier(modifierId, modifier);
    }

    public static MmceRecipeAdapterBuilder adapter(String sourceName, String machine, String adapter) {
        return new MmceRecipeAdapterBuilder(scriptSourceId("adapters", sourceName), machine, adapter);
    }

    public static MmceRecipeAdapterBuilder adapter(String machine, String adapter) {
        return adapter(machine + "_" + (adapter == null ? "" : adapter.replace(':', '_')), machine, adapter);
    }

    public static MmceRecipeAdapterBuilder recipeAdapter(String sourceName, String machine, String adapter) {
        return adapter(sourceName, machine, adapter);
    }

    public static MmceRecipeAdapterBuilder recipe_adapter(String sourceName, String machine, String adapter) {
        return adapter(sourceName, machine, adapter);
    }

    public static MmceRecipeAdapterBuilder createAdapter(String machine, String adapter) {
        return adapter(machine, adapter);
    }

    public static MmceRecipeAdapterBuilder adapterFromParentMachine(String machine, String parentMachine) {
        return adapter(machine, parentMachine);
    }

    public static MmceRecipeAdapterBuilder adapter_from_parent_machine(String machine, String parentMachine) {
        return adapterFromParentMachine(machine, parentMachine);
    }

    public static MmceRecipeAdapterBuilder recipeAdapterFromParentMachine(String machine, String parentMachine) {
        return adapterFromParentMachine(machine, parentMachine);
    }

    public static MmceRecipeAdapterBuilder fromParentMachineAdapter(String machine, String parentMachine) {
        return adapterFromParentMachine(machine, parentMachine);
    }

    public static MmceUpgradeStackBuilder upgradeStack(String itemId) {
        return MmceUpgradeStackBuilder.of(itemId);
    }

    public static MmceUpgradeStackBuilder upgradeStack(String itemId, int amount) {
        return MmceUpgradeStackBuilder.of(itemId, amount);
    }

    public static MmceUpgradeStackBuilder upgradeStackBuilder(String itemId) {
        return upgradeStack(itemId);
    }

    public static MmceUpgradeStackBuilder upgradeStackBuilder(String itemId, int amount) {
        return upgradeStack(itemId, amount);
    }

    public static MmceUpgradeStackBuilder newUpgradeStackBuilder(String itemId) {
        return upgradeStack(itemId);
    }

    public static MmceUpgradeStackBuilder newUpgradeStackBuilder(String itemId, int amount) {
        return upgradeStack(itemId, amount);
    }

    public static MmceMachineUpgradeBuilder machineUpgrade(String name, String localizedName, float level, int maxStack) {
        return MmceMachineUpgradeBuilder.newBuilder(name, localizedName, level, maxStack);
    }

    public static MmceMachineUpgradeBuilder machineUpgradeBuilder(String name, String localizedName, float level, int maxStack) {
        return machineUpgrade(name, localizedName, level, maxStack);
    }

    public static MmceMachineUpgradeBuilder newMachineUpgradeBuilder(String name, String localizedName, float level, int maxStack) {
        return machineUpgrade(name, localizedName, level, maxStack);
    }

    public static MmceDynamicMachineUpgradeBuilder dynamicMachineUpgrade(String name, String localizedName, float level, int maxStack) {
        return MmceDynamicMachineUpgradeBuilder.newBuilder(name, localizedName, level, maxStack);
    }

    public static MmceDynamicMachineUpgradeBuilder dynamicMachineUpgradeBuilder(String name, String localizedName, float level, int maxStack) {
        return dynamicMachineUpgrade(name, localizedName, level, maxStack);
    }

    public static MmceDynamicMachineUpgradeBuilder newDynamicMachineUpgradeBuilder(String name, String localizedName, float level, int maxStack) {
        return dynamicMachineUpgrade(name, localizedName, level, maxStack);
    }

    public static void registerSupportedUpgradeItem(net.minecraft.world.item.ItemStack stack) {
        MmceMachineUpgradeHelper.registerSupportedItem(stack);
    }

    public static void registerSupportedUpgradeItem(String itemId) {
        MmceMachineUpgradeHelper.registerSupportedItem(itemId);
    }

    public static void registerSupportedItem(net.minecraft.world.item.ItemStack stack) {
        registerSupportedUpgradeItem(stack);
    }

    public static void registerSupportedItem(String itemId) {
        registerSupportedUpgradeItem(itemId);
    }

    public static void addFixedUpgrade(net.minecraft.world.item.ItemStack stack, String upgradeName) {
        MmceMachineUpgradeHelper.addFixedUpgrade(stack, upgradeName);
    }

    public static void addFixedUpgrade(String itemId, String upgradeName) {
        MmceMachineUpgradeHelper.addFixedUpgrade(itemId, upgradeName);
    }

    public static net.minecraft.world.item.ItemStack addUpgradeToItemStack(net.minecraft.world.item.ItemStack stack, String upgradeName) {
        return MmceMachineUpgradeHelper.addUpgradeToStack(stack, upgradeName);
    }

    public static net.minecraft.world.item.ItemStack addUpgradeToItemStack(String itemId, String upgradeName) {
        return MmceMachineUpgradeHelper.addUpgradeToStack(itemId, upgradeName);
    }

    public static boolean supportsUpgrade(net.minecraft.world.item.ItemStack stack) {
        return MmceMachineUpgradeHelper.supportsUpgrade(stack);
    }

    public static boolean supportsUpgrade(String itemId) {
        return MmceMachineUpgradeHelper.supportsUpgrade(itemId);
    }

    public static void onUpgradeStructureFormed(String upgradeName, MmceUpgradeEventHandler handler) {
        MmceMachineUpgradeRegistry.registerMachineHandler(upgradeName, MmceMachineEventType.STRUCTURE_FORMED, handler);
    }

    public static void onUpgradeStructureUpdate(String upgradeName, MmceUpgradeEventHandler handler) {
        MmceMachineUpgradeRegistry.registerMachineHandler(upgradeName, MmceMachineEventType.STRUCTURE_UPDATE, handler);
    }

    public static void onUpgradeMachinePreTick(String upgradeName, MmceUpgradeEventHandler handler) {
        onUpgradeMachineTick(upgradeName, MmceEventPhase.START, handler);
    }

    public static void onUpgradeMachinePostTick(String upgradeName, MmceUpgradeEventHandler handler) {
        onUpgradeMachineTick(upgradeName, MmceEventPhase.END, handler);
    }

    public static void onUpgradeMachineTick(String upgradeName, MmceUpgradeEventHandler handler) {
        onUpgradeMachinePostTick(upgradeName, handler);
    }

    public static void onUpgradeSmartInterfaceUpdate(String upgradeName, MmceUpgradeEventHandler handler) {
        MmceMachineUpgradeRegistry.registerMachineHandler(upgradeName, MmceMachineEventType.SMART_INTERFACE_UPDATE, handler);
    }

    public static void onUpgradeControllerButtonClick(String upgradeName, MmceUpgradeEventHandler handler) {
        MmceMachineUpgradeRegistry.registerMachineHandler(upgradeName, MmceMachineEventType.CONTROLLER_BUTTON_CLICK, handler);
    }

    public static void onUpgradeControllerGUIRender(String upgradeName, MmceUpgradeEventHandler handler) {
        MmceMachineUpgradeRegistry.registerMachineHandler(upgradeName, MmceMachineEventType.CONTROLLER_GUI_RENDER, handler);
    }

    public static void onUpgradeGUIRender(String upgradeName, MmceUpgradeEventHandler handler) {
        onUpgradeControllerGUIRender(upgradeName, handler);
    }

    public static void onUpgradeRecipePreCheck(String upgradeName, MmceUpgradeEventHandler handler) {
        onUpgradeRecipe(upgradeName, MmceRecipeEventType.CHECK, MmceEventPhase.START, handler);
    }

    public static void onUpgradeRecipePostCheck(String upgradeName, MmceUpgradeEventHandler handler) {
        onUpgradeRecipe(upgradeName, MmceRecipeEventType.CHECK, MmceEventPhase.END, handler);
    }

    public static void onUpgradeRecipeCheck(String upgradeName, MmceUpgradeEventHandler handler) {
        onUpgradeRecipePostCheck(upgradeName, handler);
    }

    public static void onUpgradeRecipeStart(String upgradeName, MmceUpgradeEventHandler handler) {
        onUpgradeRecipe(upgradeName, MmceRecipeEventType.START, null, handler);
    }

    public static void onUpgradeRecipePreTick(String upgradeName, MmceUpgradeEventHandler handler) {
        onUpgradeRecipe(upgradeName, MmceRecipeEventType.TICK, MmceEventPhase.START, handler);
    }

    public static void onUpgradeRecipePostTick(String upgradeName, MmceUpgradeEventHandler handler) {
        onUpgradeRecipe(upgradeName, MmceRecipeEventType.TICK, MmceEventPhase.END, handler);
    }

    public static void onUpgradeRecipeTick(String upgradeName, MmceUpgradeEventHandler handler) {
        onUpgradeRecipePostTick(upgradeName, handler);
    }

    public static void onUpgradeRecipeFailure(String upgradeName, MmceUpgradeEventHandler handler) {
        onUpgradeRecipe(upgradeName, MmceRecipeEventType.FAILURE, null, handler);
    }

    public static void onUpgradeRecipeFinish(String upgradeName, MmceUpgradeEventHandler handler) {
        onUpgradeRecipe(upgradeName, MmceRecipeEventType.FINISH, null, handler);
    }

    public static void patchMachineMaxThreads(String machineId, int maxThreads) {
        MmceMachineDefinitionPatcher.patch(machineId, "kubejs/machine_modifiers",
                root -> root.addProperty("maxThreads", Math.max(0, maxThreads)));
    }

    public static void setMaxThreads(String machineId, int maxThreads) {
        patchMachineMaxThreads(machineId, maxThreads);
    }

    public static void patchMachineMaxParallelism(String machineId, int maxParallelism) {
        MmceMachineDefinitionPatcher.patch(machineId, "kubejs/machine_modifiers",
                root -> root.addProperty("maxParallelism", Math.max(1, maxParallelism)));
    }

    public static void setMaxParallelism(String machineId, int maxParallelism) {
        patchMachineMaxParallelism(machineId, maxParallelism);
    }

    public static void patchMachineInternalParallelism(String machineId, int parallelism) {
        MmceMachineDefinitionPatcher.patch(machineId, "kubejs/machine_modifiers",
                root -> root.addProperty("internalParallelism", Math.max(0, parallelism)));
    }

    public static void setInternalParallelism(String machineId, int parallelism) {
        patchMachineInternalParallelism(machineId, parallelism);
    }

    public static void patchMachineSmartInterface(String machineId, String type, float defaultValue) {
        MmceMachineDefinitionPatcher.patch(machineId, "kubejs/machine_modifiers", root -> {
            JsonObject object = new JsonObject();
            object.addProperty("type", type == null ? "" : type.trim());
            object.addProperty("defaultValue", defaultValue);
            MmceMachineDefinitionPatcher.array(root, "smartInterfaceTypes").add(object);
        });
    }

    public static void patchMachineSmartInterface(String machineId, MmceSmartInterfaceTypeBuilder type) {
        if (type == null) {
            return;
        }
        MmceMachineDefinitionPatcher.patch(machineId, "kubejs/machine_modifiers",
                root -> MmceMachineDefinitionPatcher.array(root, "smartInterfaceTypes").add(type.json()));
    }

    public static void addSmartInterfaceType(String machineId, String type, float defaultValue) {
        patchMachineSmartInterface(machineId, type, defaultValue);
    }

    public static void addSmartInterfaceType(String machineId, MmceSmartInterfaceTypeBuilder type) {
        patchMachineSmartInterface(machineId, type);
    }

    public static void patchMachineCoreThread(String machineId, String threadName) {
        if (threadName == null || threadName.isBlank()) {
            return;
        }
        MmceMachineDefinitionPatcher.patch(machineId, "kubejs/machine_modifiers",
                root -> MmceMachineDefinitionPatcher.array(root, "coreThreads").add(threadName.trim()));
    }

    public static void patchMachineCoreThread(String machineId, MmceKubeJSFactoryRecipeThreadBuilder thread) {
        if (thread == null) {
            return;
        }
        MmceMachineDefinitionPatcher.patch(machineId, "kubejs/machine_modifiers",
                root -> MmceMachineDefinitionPatcher.array(root, "coreThreads").add(thread.json()));
    }

    public static void addCoreThread(String machineId, String threadName) {
        patchMachineCoreThread(machineId, threadName);
    }

    public static void addCoreThread(String machineId, MmceKubeJSFactoryRecipeThreadBuilder thread) {
        patchMachineCoreThread(machineId, thread);
    }

    public static void patchMachineDynamicPattern(String machineId, MmceKubeJSDynamicPatternBuilder pattern) {
        if (pattern == null) {
            return;
        }
        MmceMachineDefinitionPatcher.patch(machineId, "kubejs/machine_modifiers",
                root -> MmceMachineDefinitionPatcher.array(root, "dynamic-patterns").add(pattern.json()));
    }

    public static void addDynamicPattern(String machineId, MmceKubeJSDynamicPatternBuilder pattern) {
        patchMachineDynamicPattern(machineId, pattern);
    }

    public static void patchMachineMultiBlockModifier(String machineId, MmceKubeJSMultiBlockModifierReplacement multiBlockModifier) {
        if (multiBlockModifier == null) {
            return;
        }
        MmceMachineDefinitionPatcher.patch(machineId, "kubejs/machine_modifiers", root -> {
            JsonArray entries = multiBlockModifier.entries();
            JsonArray target = MmceMachineDefinitionPatcher.array(root, "modifiers");
            for (int index = 0; index < entries.size(); index++) {
                target.add(entries.get(index).deepCopy());
            }
        });
    }

    public static void patchMachineMultiblockModifier(String machineId, MmceKubeJSMultiBlockModifierReplacement multiBlockModifier) {
        patchMachineMultiBlockModifier(machineId, multiBlockModifier);
    }

    public static void addMultiBlockModifier(String machineId, MmceKubeJSMultiBlockModifierReplacement multiBlockModifier) {
        patchMachineMultiBlockModifier(machineId, multiBlockModifier);
    }

    public static void addMultiblockModifier(String machineId, MmceKubeJSMultiBlockModifierReplacement multiBlockModifier) {
        patchMachineMultiBlockModifier(machineId, multiBlockModifier);
    }

    public static void patchMachinePrefix(String machineId, String prefixName) {
        MmceMachineDefinitionPatcher.patch(machineId, "kubejs/machine_modifiers",
                root -> root.addProperty("prefix", prefixName == null ? "" : prefixName));
    }

    public static void setMachinePrefix(String machineId, String prefixName) {
        patchMachinePrefix(machineId, prefixName);
    }

    public static void patchMachineGeoModel(String machineId, String modelName) {
        MmceMachineDefinitionPatcher.patch(machineId, "kubejs/machine_modifiers",
                root -> root.addProperty("geoModel", modelName == null ? "" : modelName));
    }

    public static void setMachineGeoModel(String machineId, String modelName) {
        patchMachineGeoModel(machineId, modelName);
    }

    public static MmceRecipeModifierBuilder modifierBuilder() {
        return MmceRecipeModifierBuilder.newBuilder();
    }

    public static MmceRecipeModifier modifier(String target, String io, double value, int operation) {
        return modifier(target, io, value, operation, false);
    }

    public static MmceRecipeModifier modifier(String target, String io, double value, int operation, boolean affectChance) {
        return MmceRecipeModifier.create(target, io, value, operation, affectChance);
    }

    public static void registerMachineJson(String sourceName, String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        MmceScriptDataRegistry.registerMachine(scriptSourceId("machines", sourceName), object);
    }

    public static void registerRecipeJson(String sourceName, String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        MmceScriptDataRegistry.registerRecipe(scriptSourceId("recipes", sourceName), object);
    }

    public static void registerAdapterJson(String sourceName, String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        MmceScriptDataRegistry.registerAdapter(scriptSourceId("adapters", sourceName), object);
    }

    public static void onStructureFormed(String machineId, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineId, MmceMachineEventType.STRUCTURE_FORMED, handler);
    }

    public static void onStructureFormedKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        MmceKubeJSEvents.onStructureFormedKeyed(machineId, key, handler);
    }

    public static void onStructureUpdate(String machineId, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineId, MmceMachineEventType.STRUCTURE_UPDATE, handler);
    }

    public static void onStructureUpdateKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        MmceKubeJSEvents.onStructureUpdateKeyed(machineId, key, handler);
    }

    public static void onMachinePreTick(String machineId, MmceMachineEventHandler handler) {
        onMachineTick(machineId, MmceEventPhase.START, handler);
    }

    public static void onMachinePreTickKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        MmceKubeJSEvents.onMachinePreTickKeyed(machineId, key, handler);
    }

    public static void onMachinePostTick(String machineId, MmceMachineEventHandler handler) {
        onMachineTick(machineId, MmceEventPhase.END, handler);
    }

    public static void onMachinePostTickKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        MmceKubeJSEvents.onMachinePostTickKeyed(machineId, key, handler);
    }

    public static void onMachineTick(String machineId, MmceMachineEventHandler handler) {
        onMachinePostTick(machineId, handler);
    }

    public static void onMachineTickKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        MmceKubeJSEvents.onMachineTickKeyed(machineId, key, handler);
    }

    public static void onSmartInterfaceUpdate(String machineId, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineId, MmceMachineEventType.SMART_INTERFACE_UPDATE, handler);
    }

    public static void onSmartInterfaceUpdateKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        MmceKubeJSEvents.onSmartInterfaceUpdateKeyed(machineId, key, handler);
    }

    public static void onControllerButtonClick(String machineId, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineId, MmceMachineEventType.CONTROLLER_BUTTON_CLICK, handler);
    }

    public static void onControllerButtonClickKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        MmceKubeJSEvents.onControllerButtonClickKeyed(machineId, key, handler);
    }

    public static void onControllerGUIRender(String machineId, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineId, MmceMachineEventType.CONTROLLER_GUI_RENDER, handler);
    }

    public static void onControllerGUIRenderKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        MmceKubeJSEvents.onControllerGUIRenderKeyed(machineId, key, handler);
    }

    public static void onGUIRender(String machineId, MmceMachineEventHandler handler) {
        onControllerGUIRender(machineId, handler);
    }

    public static void onRecipePreCheck(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.CHECK, MmceEventPhase.START, handler);
    }

    public static void onRecipePreCheckKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        MmceKubeJSEvents.onRecipePreCheckKeyed(recipeId, key, handler);
    }

    public static void onRecipePostCheck(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.CHECK, MmceEventPhase.END, handler);
    }

    public static void onRecipePostCheckKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        MmceKubeJSEvents.onRecipePostCheckKeyed(recipeId, key, handler);
    }

    public static void onRecipeCheck(String recipeId, MmceRecipeEventHandler handler) {
        onRecipePostCheck(recipeId, handler);
    }

    public static void onRecipeCheckKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        MmceKubeJSEvents.onRecipeCheckKeyed(recipeId, key, handler);
    }

    public static void onRecipeStart(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.START, null, handler);
    }

    public static void onRecipeStartKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        MmceKubeJSEvents.onRecipeStartKeyed(recipeId, key, handler);
    }

    public static void onRecipePreTick(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.TICK, MmceEventPhase.START, handler);
    }

    public static void onRecipePreTickKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        MmceKubeJSEvents.onRecipePreTickKeyed(recipeId, key, handler);
    }

    public static void onRecipePostTick(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.TICK, MmceEventPhase.END, handler);
    }

    public static void onRecipePostTickKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        MmceKubeJSEvents.onRecipePostTickKeyed(recipeId, key, handler);
    }

    public static void onRecipeTick(String recipeId, MmceRecipeEventHandler handler) {
        onRecipePostTick(recipeId, handler);
    }

    public static void onRecipeTickKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        MmceKubeJSEvents.onRecipeTickKeyed(recipeId, key, handler);
    }

    public static void onRecipeFailure(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.FAILURE, null, handler);
    }

    public static void onRecipeFailureKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        MmceKubeJSEvents.onRecipeFailureKeyed(recipeId, key, handler);
    }

    public static void onRecipeFinish(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.FINISH, null, handler);
    }

    public static void onRecipeFinishKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        MmceKubeJSEvents.onRecipeFinishKeyed(recipeId, key, handler);
    }

    public static void onResultChance(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.RESULT_CHANCE, null, handler);
    }

    public static void onResultChanceKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        MmceKubeJSEvents.onResultChanceKeyed(recipeId, key, handler);
    }

    public static MmceRecipeCheckEvent asRecipeCheckEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asRecipeCheckEvent(event);
    }

    public static MmceRecipeStartEvent asRecipeStartEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asRecipeStartEvent(event);
    }

    public static MmceRecipeTickEvent asRecipeTickEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asRecipeTickEvent(event);
    }

    public static MmceRecipeFailureEvent asRecipeFailureEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asRecipeFailureEvent(event);
    }

    public static MmceRecipeFinishEvent asRecipeFinishEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asRecipeFinishEvent(event);
    }

    public static MmceResultChanceCreateEvent asResultChanceCreateEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asResultChanceCreateEvent(event);
    }

    public static MmceMachineStructureFormedEvent asMachineStructureFormedEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asMachineStructureFormedEvent(event);
    }

    public static MmceMachineStructureFormedEvent asStructureFormedEvent(MmceMachineEvent event) {
        return asMachineStructureFormedEvent(event);
    }

    public static MmceMachineStructureUpdateEvent asMachineStructureUpdateEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asMachineStructureUpdateEvent(event);
    }

    public static MmceMachineStructureUpdateEvent asStructureUpdateEvent(MmceMachineEvent event) {
        return asMachineStructureUpdateEvent(event);
    }

    public static MmceMachineTickEvent asMachineTickEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asMachineTickEvent(event);
    }

    public static MmceSmartInterfaceUpdateEvent asSmartInterfaceUpdateEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asSmartInterfaceUpdateEvent(event);
    }

    public static MmceControllerButtonClickEvent asControllerButtonClickEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asControllerButtonClickEvent(event);
    }

    public static MmceControllerGUIRenderEvent asControllerGUIRenderEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asControllerGUIRenderEvent(event);
    }

    public static MmceFactoryRecipeStartEvent asFactoryRecipeStartEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asFactoryRecipeStartEvent(event);
    }

    public static MmceFactoryRecipeTickEvent asFactoryRecipeTickEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asFactoryRecipeTickEvent(event);
    }

    public static MmceFactoryRecipeFailureEvent asFactoryRecipeFailureEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asFactoryRecipeFailureEvent(event);
    }

    public static MmceFactoryRecipeFinishEvent asFactoryRecipeFinishEvent(MmceMachineEvent event) {
        return MmceKubeJSEvents.asFactoryRecipeFinishEvent(event);
    }

    public static MmceRecipeCheckEvent castToRecipeCheckEvent(MmceMachineEvent event) {
        return asRecipeCheckEvent(event);
    }

    public static MmceRecipeStartEvent castToRecipeStartEvent(MmceMachineEvent event) {
        return asRecipeStartEvent(event);
    }

    public static MmceRecipeTickEvent castToRecipeTickEvent(MmceMachineEvent event) {
        return asRecipeTickEvent(event);
    }

    public static MmceRecipeFailureEvent castToRecipeFailureEvent(MmceMachineEvent event) {
        return asRecipeFailureEvent(event);
    }

    public static MmceRecipeFinishEvent castToRecipeFinishEvent(MmceMachineEvent event) {
        return asRecipeFinishEvent(event);
    }

    public static MmceResultChanceCreateEvent castToResultChanceCreateEvent(MmceMachineEvent event) {
        return asResultChanceCreateEvent(event);
    }

    public static MmceMachineStructureFormedEvent castToMachineStructureFormedEvent(MmceMachineEvent event) {
        return asMachineStructureFormedEvent(event);
    }

    public static MmceMachineStructureFormedEvent castToStructureFormedEvent(MmceMachineEvent event) {
        return asMachineStructureFormedEvent(event);
    }

    public static MmceMachineStructureUpdateEvent castToMachineStructureUpdateEvent(MmceMachineEvent event) {
        return asMachineStructureUpdateEvent(event);
    }

    public static MmceMachineStructureUpdateEvent castToStructureUpdateEvent(MmceMachineEvent event) {
        return asMachineStructureUpdateEvent(event);
    }

    public static MmceMachineTickEvent castToMachineTickEvent(MmceMachineEvent event) {
        return asMachineTickEvent(event);
    }

    public static MmceSmartInterfaceUpdateEvent castToSmartInterfaceUpdateEvent(MmceMachineEvent event) {
        return asSmartInterfaceUpdateEvent(event);
    }

    public static MmceControllerButtonClickEvent castToControllerButtonClickEvent(MmceMachineEvent event) {
        return asControllerButtonClickEvent(event);
    }

    public static MmceControllerGUIRenderEvent castToControllerGUIRenderEvent(MmceMachineEvent event) {
        return asControllerGUIRenderEvent(event);
    }

    public static MmceFactoryRecipeStartEvent castToFactoryRecipeStartEvent(MmceMachineEvent event) {
        return asFactoryRecipeStartEvent(event);
    }

    public static MmceFactoryRecipeTickEvent castToFactoryRecipeTickEvent(MmceMachineEvent event) {
        return asFactoryRecipeTickEvent(event);
    }

    public static MmceFactoryRecipeFailureEvent castToFactoryRecipeFailureEvent(MmceMachineEvent event) {
        return asFactoryRecipeFailureEvent(event);
    }

    public static MmceFactoryRecipeFinishEvent castToFactoryRecipeFinishEvent(MmceMachineEvent event) {
        return asFactoryRecipeFinishEvent(event);
    }

    private static ResourceLocation scriptSourceId(String directory, String sourceName) {
        return MmceKubeJSMachineBuilder.sourceId(directory, sourceName);
    }

    private static void onMachineTick(String machineId, MmceEventPhase phase, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineId, MmceMachineEventType.TICK, event -> {
            if (event.getPhase() == phase && handler != null) {
                handler.handle(event);
            }
        });
    }

    private static void onRecipe(String recipeId, MmceRecipeEventType type, MmceEventPhase phase, MmceRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeId, type, event -> {
            if ((phase == null || event.getPhase() == phase) && handler != null) {
                handler.handle(event);
            }
        });
    }

    private static void onUpgradeMachineTick(String upgradeName, MmceEventPhase phase, MmceUpgradeEventHandler handler) {
        MmceMachineUpgradeRegistry.registerMachineHandler(upgradeName, MmceMachineEventType.TICK, (event, upgrade) -> {
            if (event.getPhase() == phase && handler != null) {
                handler.handle(event, upgrade);
            }
        });
    }

    private static void onUpgradeRecipe(String upgradeName, MmceRecipeEventType type, MmceEventPhase phase, MmceUpgradeEventHandler handler) {
        MmceMachineUpgradeRegistry.registerRecipeHandler(upgradeName, type, (event, upgrade) -> {
            if ((phase == null || event.getPhase() == phase) && handler != null) {
                handler.handle(event, upgrade);
            }
        });
    }
}
