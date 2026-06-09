package hellfirepvp.modularmachinery.port.integration.kubejs;

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

public final class MmceKubeJSEvents {
    public static void onStructureFormed(String machineId, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(machineId), MmceMachineEventType.STRUCTURE_FORMED, handler);
    }

    public static void onStructureFormedKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(machineId), key,
                MmceMachineEventType.STRUCTURE_FORMED, handler);
    }

    public static void onStructureUpdate(String machineId, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(machineId), MmceMachineEventType.STRUCTURE_UPDATE, handler);
    }

    public static void onStructureUpdateKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(machineId), key,
                MmceMachineEventType.STRUCTURE_UPDATE, handler);
    }

    public static void onMachinePreTick(String machineId, MmceMachineEventHandler handler) {
        onMachineTick(machineId, MmceEventPhase.START, handler);
    }

    public static void onMachinePreTickKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        onMachineTickKeyed(machineId, key, MmceEventPhase.START, handler);
    }

    public static void onMachinePostTick(String machineId, MmceMachineEventHandler handler) {
        onMachineTick(machineId, MmceEventPhase.END, handler);
    }

    public static void onMachinePostTickKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        onMachineTickKeyed(machineId, key, MmceEventPhase.END, handler);
    }

    public static void onMachineTick(String machineId, MmceMachineEventHandler handler) {
        onMachinePostTick(machineId, handler);
    }

    public static void onMachineTickKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        onMachinePostTickKeyed(machineId, key, handler);
    }

    public static void onSmartInterfaceUpdate(String machineId, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(machineId), MmceMachineEventType.SMART_INTERFACE_UPDATE, handler);
    }

    public static void onSmartInterfaceUpdateKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(machineId), key,
                MmceMachineEventType.SMART_INTERFACE_UPDATE, handler);
    }

    public static void onControllerButtonClick(String machineId, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(machineId), MmceMachineEventType.CONTROLLER_BUTTON_CLICK, handler);
    }

    public static void onControllerButtonClickKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(machineId), key,
                MmceMachineEventType.CONTROLLER_BUTTON_CLICK, handler);
    }

    public static void onControllerGUIRender(String machineId, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(machineId), MmceMachineEventType.CONTROLLER_GUI_RENDER, handler);
    }

    public static void onControllerGUIRenderKeyed(String machineId, String key, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(machineId), key,
                MmceMachineEventType.CONTROLLER_GUI_RENDER, handler);
    }

    public static void onRecipePreCheck(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.CHECK, MmceEventPhase.START, handler);
    }

    public static void onRecipePreCheckKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        onRecipeKeyed(recipeId, key, MmceRecipeEventType.CHECK, MmceEventPhase.START, handler);
    }

    public static void onRecipePostCheck(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.CHECK, MmceEventPhase.END, handler);
    }

    public static void onRecipePostCheckKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        onRecipeKeyed(recipeId, key, MmceRecipeEventType.CHECK, MmceEventPhase.END, handler);
    }

    public static void onRecipeCheck(String recipeId, MmceRecipeEventHandler handler) {
        onRecipePostCheck(recipeId, handler);
    }

    public static void onRecipeCheckKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        onRecipePostCheckKeyed(recipeId, key, handler);
    }

    public static void onRecipeStart(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.START, null, handler);
    }

    public static void onRecipeStartKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        onRecipeKeyed(recipeId, key, MmceRecipeEventType.START, null, handler);
    }

    public static void onRecipePreTick(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.TICK, MmceEventPhase.START, handler);
    }

    public static void onRecipePreTickKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        onRecipeKeyed(recipeId, key, MmceRecipeEventType.TICK, MmceEventPhase.START, handler);
    }

    public static void onRecipePostTick(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.TICK, MmceEventPhase.END, handler);
    }

    public static void onRecipePostTickKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        onRecipeKeyed(recipeId, key, MmceRecipeEventType.TICK, MmceEventPhase.END, handler);
    }

    public static void onRecipeTick(String recipeId, MmceRecipeEventHandler handler) {
        onRecipePostTick(recipeId, handler);
    }

    public static void onRecipeTickKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        onRecipePostTickKeyed(recipeId, key, handler);
    }

    public static void onRecipeFailure(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.FAILURE, null, handler);
    }

    public static void onRecipeFailureKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        onRecipeKeyed(recipeId, key, MmceRecipeEventType.FAILURE, null, handler);
    }

    public static void onRecipeFinish(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.FINISH, null, handler);
    }

    public static void onRecipeFinishKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        onRecipeKeyed(recipeId, key, MmceRecipeEventType.FINISH, null, handler);
    }

    public static void onResultChance(String recipeId, MmceRecipeEventHandler handler) {
        onRecipe(recipeId, MmceRecipeEventType.RESULT_CHANCE, null, handler);
    }

    public static void onResultChanceKeyed(String recipeId, String key, MmceRecipeEventHandler handler) {
        onRecipeKeyed(recipeId, key, MmceRecipeEventType.RESULT_CHANCE, null, handler);
    }

    public static MmceRecipeCheckEvent asRecipeCheckEvent(MmceMachineEvent event) {
        return event instanceof MmceRecipeCheckEvent value ? value : null;
    }

    public static MmceRecipeStartEvent asRecipeStartEvent(MmceMachineEvent event) {
        return event instanceof MmceRecipeStartEvent value ? value : null;
    }

    public static MmceRecipeTickEvent asRecipeTickEvent(MmceMachineEvent event) {
        return event instanceof MmceRecipeTickEvent value ? value : null;
    }

    public static MmceRecipeFailureEvent asRecipeFailureEvent(MmceMachineEvent event) {
        return event instanceof MmceRecipeFailureEvent value ? value : null;
    }

    public static MmceRecipeFinishEvent asRecipeFinishEvent(MmceMachineEvent event) {
        return event instanceof MmceRecipeFinishEvent value ? value : null;
    }

    public static MmceResultChanceCreateEvent asResultChanceCreateEvent(MmceMachineEvent event) {
        return event instanceof MmceResultChanceCreateEvent value ? value : null;
    }

    public static MmceMachineStructureFormedEvent asMachineStructureFormedEvent(MmceMachineEvent event) {
        return event instanceof MmceMachineStructureFormedEvent value ? value : null;
    }

    public static MmceMachineStructureFormedEvent asStructureFormedEvent(MmceMachineEvent event) {
        return asMachineStructureFormedEvent(event);
    }

    public static MmceMachineStructureUpdateEvent asMachineStructureUpdateEvent(MmceMachineEvent event) {
        return event instanceof MmceMachineStructureUpdateEvent value ? value : null;
    }

    public static MmceMachineStructureUpdateEvent asStructureUpdateEvent(MmceMachineEvent event) {
        return asMachineStructureUpdateEvent(event);
    }

    public static MmceMachineTickEvent asMachineTickEvent(MmceMachineEvent event) {
        return event instanceof MmceMachineTickEvent value ? value : null;
    }

    public static MmceSmartInterfaceUpdateEvent asSmartInterfaceUpdateEvent(MmceMachineEvent event) {
        return event instanceof MmceSmartInterfaceUpdateEvent value ? value : null;
    }

    public static MmceControllerButtonClickEvent asControllerButtonClickEvent(MmceMachineEvent event) {
        return event instanceof MmceControllerButtonClickEvent value ? value : null;
    }

    public static MmceControllerGUIRenderEvent asControllerGUIRenderEvent(MmceMachineEvent event) {
        return event instanceof MmceControllerGUIRenderEvent value ? value : null;
    }

    public static MmceFactoryRecipeStartEvent asFactoryRecipeStartEvent(MmceMachineEvent event) {
        return event instanceof MmceFactoryRecipeStartEvent value ? value : null;
    }

    public static MmceFactoryRecipeTickEvent asFactoryRecipeTickEvent(MmceMachineEvent event) {
        return event instanceof MmceFactoryRecipeTickEvent value ? value : null;
    }

    public static MmceFactoryRecipeFailureEvent asFactoryRecipeFailureEvent(MmceMachineEvent event) {
        return event instanceof MmceFactoryRecipeFailureEvent value ? value : null;
    }

    public static MmceFactoryRecipeFinishEvent asFactoryRecipeFinishEvent(MmceMachineEvent event) {
        return event instanceof MmceFactoryRecipeFinishEvent value ? value : null;
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

    private static void onMachineTick(String machineId, MmceEventPhase phase, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(machineId), MmceMachineEventType.TICK, event -> {
            if (event.getPhase() == phase && handler != null) {
                handler.handle(event);
            }
        });
    }

    private static void onMachineTickKeyed(String machineId, String key, MmceEventPhase phase, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(machineId), key,
                MmceMachineEventType.TICK, event -> {
            if (event.getPhase() == phase && handler != null) {
                handler.handle(event);
            }
        });
    }

    private static void onRecipe(String recipeId, MmceRecipeEventType type, MmceEventPhase phase, MmceRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(recipeId), type, event -> {
            if ((phase == null || event.getPhase() == phase) && handler != null) {
                handler.handle(event);
            }
        });
    }

    private static void onRecipeKeyed(String recipeId, String key, MmceRecipeEventType type, MmceEventPhase phase, MmceRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(MmceKubeJSBindings.KUBEJS_EVENT_SOURCE, MmceEventRegistry.resolveId(recipeId), key, type, event -> {
            if ((phase == null || event.getPhase() == phase) && handler != null) {
                handler.handle(event);
            }
        });
    }

    private MmceKubeJSEvents() {
    }
}
