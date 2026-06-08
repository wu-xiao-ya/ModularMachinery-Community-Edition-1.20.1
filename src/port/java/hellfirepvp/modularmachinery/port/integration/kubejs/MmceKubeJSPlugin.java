package hellfirepvp.modularmachinery.port.integration.kubejs;

import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import hellfirepvp.modularmachinery.port.blockentity.FactoryControllerBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.event.MmceControllerButtonClickEvent;
import hellfirepvp.modularmachinery.port.event.MmceControllerGUIRenderEvent;
import hellfirepvp.modularmachinery.port.event.MmceEventPhase;
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
import hellfirepvp.modularmachinery.port.integration.MmceBlockChecker;
import hellfirepvp.modularmachinery.port.integration.MmceDynamicMachineUpgradeBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceFactoryRecipeThreadBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceFunction;
import hellfirepvp.modularmachinery.port.integration.MmceItemChecker;
import hellfirepvp.modularmachinery.port.integration.MmceItemModifier;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgrade;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeAdapterBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifierBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceSmartInterfaceTypeBuilder;
import hellfirepvp.modularmachinery.port.integration.MmceUpgradeEventHandler;
import hellfirepvp.modularmachinery.port.integration.MmceUpgradeStackBuilder;

public final class MmceKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerClasses(ClassFilter filter) {
        filter.allow(MmceKubeJSBindings.class);
        filter.allow(MmceKubeJSEvents.class);
        filter.allow(MmceKubeJSMachineBuilder.class);
        filter.allow(MmceKubeJSRecipeBuilder.class);
        filter.allow(MmceKubeJSBlockArrayBuilder.class);
        filter.allow(MmceKubeJSIngredientArrayPrimer.class);
        filter.allow(MmceKubeJSFactoryRecipeThreadBuilder.class);
        filter.allow(MmceKubeJSRecipeThread.class);
        filter.allow(MmceKubeJSDynamicPatternBuilder.class);
        filter.allow(MmceKubeJSMultiBlockModifierBuilder.class);
        filter.allow(MmceKubeJSMultiBlockModifierReplacement.class);
        filter.allow(MmceRecipeAdapterBuilder.class);
        filter.allow(MmceUpgradeStackBuilder.class);
        filter.allow(MmceMachineUpgradeBuilder.class);
        filter.allow(MmceDynamicMachineUpgradeBuilder.class);
        filter.allow(MmceMachineUpgrade.class);
        filter.allow(MmceRecipeModifier.class);
        filter.allow(MmceRecipeModifierBuilder.class);
        filter.allow(MmceSmartInterfaceTypeBuilder.class);
        filter.allow(MmceBlockChecker.class);
        filter.allow(MmceItemChecker.class);
        filter.allow(MmceItemModifier.class);
        filter.allow(MmceUpgradeEventHandler.class);
        filter.allow(MmceFunction.class);
        filter.allow(MmceEventPhase.class);
        filter.allow(MmceMachineEventType.class);
        filter.allow(MmceRecipeEventType.class);
        filter.allow(MmceMachineEventHandler.class);
        filter.allow(MmceRecipeEventHandler.class);
        filter.allow(MmceMachineEvent.class);
        filter.allow(MmceRecipeEvent.class);
        filter.allow(MmceRecipeCheckEvent.class);
        filter.allow(MmceRecipeStartEvent.class);
        filter.allow(MmceRecipeTickEvent.class);
        filter.allow(MmceRecipeFailureEvent.class);
        filter.allow(MmceRecipeFinishEvent.class);
        filter.allow(MmceResultChanceCreateEvent.class);
        filter.allow(MmceMachineStructureFormedEvent.class);
        filter.allow(MmceMachineStructureUpdateEvent.class);
        filter.allow(MmceMachineTickEvent.class);
        filter.allow(MmceSmartInterfaceUpdateEvent.class);
        filter.allow(MmceControllerButtonClickEvent.class);
        filter.allow(MmceControllerGUIRenderEvent.class);
        filter.allow(MmceFactoryRecipeStartEvent.class);
        filter.allow(MmceFactoryRecipeTickEvent.class);
        filter.allow(MmceFactoryRecipeFailureEvent.class);
        filter.allow(MmceFactoryRecipeFinishEvent.class);
        filter.allow(MachineControllerBlockEntity.class);
        filter.allow(FactoryControllerBlockEntity.class);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("MMCE", MmceKubeJSBindings.class);
        bindings.add("ModularMachinery", MmceKubeJSBindings.class);
        bindings.add("MMCEEvents", MmceKubeJSEvents.class);
    }

    @Override
    public void beforeScriptsLoaded(ScriptManager manager) {
        if (manager.scriptType == ScriptType.SERVER) {
            MmceKubeJSBindings.clearKubeJSScriptDefinitions();
        }
    }
}
