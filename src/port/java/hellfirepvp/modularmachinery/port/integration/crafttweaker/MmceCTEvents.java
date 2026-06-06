package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.event.MmceControllerButtonClickEvent;
import hellfirepvp.modularmachinery.port.event.MmceControllerGUIRenderEvent;
import hellfirepvp.modularmachinery.port.event.MmceEventPhase;
import hellfirepvp.modularmachinery.port.event.MmceEventRegistry;
import hellfirepvp.modularmachinery.port.event.MmceMachineEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineEventHandler;
import hellfirepvp.modularmachinery.port.event.MmceMachineEventType;
import hellfirepvp.modularmachinery.port.event.MmceMachineStructureFormedEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineStructureUpdateEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineTickEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEventHandler;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEventType;
import hellfirepvp.modularmachinery.port.event.MmceRecipeCheckEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeFailureEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeFinishEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeStartEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeTickEvent;
import hellfirepvp.modularmachinery.port.event.MmceResultChanceCreateEvent;
import hellfirepvp.modularmachinery.port.event.MmceSmartInterfaceUpdateEvent;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeFailureEvent;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeFinishEvent;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeStartEvent;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeTickEvent;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MMEvents")
public final class MmceCTEvents {
    private MmceCTEvents() {
    }

    @ZenCodeType.Method
    public static void onStructureFormed(String machineRegistryName, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.STRUCTURE_FORMED, handler);
    }

    @ZenCodeType.Method
    public static void onStructureUpdate(String machineRegistryName, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.STRUCTURE_UPDATE, handler);
    }

    @ZenCodeType.Method
    public static void onMachinePreTick(String machineRegistryName, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.TICK,
                event -> handlePhase(event, MmceEventPhase.START, handler));
    }

    @ZenCodeType.Method
    public static void onMachinePostTick(String machineRegistryName, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.TICK,
                event -> handlePhase(event, MmceEventPhase.END, handler));
    }

    @ZenCodeType.Method
    public static void onMachineTick(String machineRegistryName, MmceMachineEventHandler handler) {
        onMachinePostTick(machineRegistryName, handler);
    }

    @ZenCodeType.Method
    public static void onSmartInterfaceUpdate(String machineRegistryName, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.SMART_INTERFACE_UPDATE, handler);
    }

    @ZenCodeType.Method
    public static void onControllerButtonClick(String machineRegistryName, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.CONTROLLER_BUTTON_CLICK, handler);
    }

    @ZenCodeType.Method
    public static void onControllerGUIRender(String machineRegistryName, MmceMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.CONTROLLER_GUI_RENDER, handler);
    }

    @ZenCodeType.Method
    public static void onRecipePreCheck(String recipeRegistryName, MmceRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.CHECK,
                event -> handlePhase(event, MmceEventPhase.START, handler));
    }

    @ZenCodeType.Method
    public static void onRecipePostCheck(String recipeRegistryName, MmceRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.CHECK,
                event -> handlePhase(event, MmceEventPhase.END, handler));
    }

    @ZenCodeType.Method
    public static void onRecipeCheck(String recipeRegistryName, MmceRecipeEventHandler handler) {
        onRecipePostCheck(recipeRegistryName, handler);
    }

    @ZenCodeType.Method
    public static void onRecipeStart(String recipeRegistryName, MmceRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.START, handler);
    }

    @ZenCodeType.Method
    public static void onRecipePreTick(String recipeRegistryName, MmceRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.TICK,
                event -> handlePhase(event, MmceEventPhase.START, handler));
    }

    @ZenCodeType.Method
    public static void onRecipePostTick(String recipeRegistryName, MmceRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.TICK,
                event -> handlePhase(event, MmceEventPhase.END, handler));
    }

    @ZenCodeType.Method
    public static void onRecipeTick(String recipeRegistryName, MmceRecipeEventHandler handler) {
        onRecipePostTick(recipeRegistryName, handler);
    }

    @ZenCodeType.Method
    public static void onRecipeFailure(String recipeRegistryName, MmceRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.FAILURE, handler);
    }

    @ZenCodeType.Method
    public static void onRecipeFinish(String recipeRegistryName, MmceRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.FINISH, handler);
    }

    @ZenCodeType.Method
    public static void onResultChance(String recipeRegistryName, MmceRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.RESULT_CHANCE, handler);
    }

    @ZenCodeType.Method
    public static MmceRecipeCheckEvent castToRecipeCheckEvent(MmceMachineEvent event) {
        return event instanceof MmceRecipeCheckEvent recipeEvent ? recipeEvent : null;
    }

    @ZenCodeType.Method
    public static MmceRecipeStartEvent castToRecipeStartEvent(MmceMachineEvent event) {
        return event instanceof MmceRecipeStartEvent recipeEvent ? recipeEvent : null;
    }

    @ZenCodeType.Method
    public static MmceRecipeTickEvent castToRecipeTickEvent(MmceMachineEvent event) {
        return event instanceof MmceRecipeTickEvent recipeEvent ? recipeEvent : null;
    }

    @ZenCodeType.Method
    public static MmceRecipeFailureEvent castToRecipeFailureEvent(MmceMachineEvent event) {
        return event instanceof MmceRecipeFailureEvent recipeEvent ? recipeEvent : null;
    }

    @ZenCodeType.Method
    public static MmceRecipeFinishEvent castToRecipeFinishEvent(MmceMachineEvent event) {
        return event instanceof MmceRecipeFinishEvent recipeEvent ? recipeEvent : null;
    }

    @ZenCodeType.Method
    public static MmceResultChanceCreateEvent castToResultChanceCreateEvent(MmceMachineEvent event) {
        return event instanceof MmceResultChanceCreateEvent recipeEvent ? recipeEvent : null;
    }

    @ZenCodeType.Method
    public static MmceMachineStructureFormedEvent castToMachineStructureFormedEvent(MmceMachineEvent event) {
        return event instanceof MmceMachineStructureFormedEvent machineEvent ? machineEvent : null;
    }

    @ZenCodeType.Method
    public static MmceMachineStructureUpdateEvent castToMachineStructureUpdateEvent(MmceMachineEvent event) {
        return event instanceof MmceMachineStructureUpdateEvent machineEvent ? machineEvent : null;
    }

    @ZenCodeType.Method
    public static MmceMachineTickEvent castToMachineTickEvent(MmceMachineEvent event) {
        return event instanceof MmceMachineTickEvent machineEvent ? machineEvent : null;
    }

    @ZenCodeType.Method
    public static MmceSmartInterfaceUpdateEvent castToSmartInterfaceUpdateEvent(MmceMachineEvent event) {
        return event instanceof MmceSmartInterfaceUpdateEvent machineEvent ? machineEvent : null;
    }

    @ZenCodeType.Method
    public static MmceControllerButtonClickEvent castToControllerButtonClickEvent(MmceMachineEvent event) {
        return event instanceof MmceControllerButtonClickEvent machineEvent ? machineEvent : null;
    }

    @ZenCodeType.Method
    public static MmceControllerGUIRenderEvent castToControllerGUIRenderEvent(MmceMachineEvent event) {
        return event instanceof MmceControllerGUIRenderEvent machineEvent ? machineEvent : null;
    }

    @ZenCodeType.Method
    public static MmceFactoryRecipeStartEvent castToFactoryRecipeStartEvent(MmceMachineEvent event) {
        return event instanceof MmceFactoryRecipeStartEvent recipeEvent ? recipeEvent : null;
    }

    @ZenCodeType.Method
    public static MmceFactoryRecipeTickEvent castToFactoryRecipeTickEvent(MmceMachineEvent event) {
        return event instanceof MmceFactoryRecipeTickEvent recipeEvent ? recipeEvent : null;
    }

    @ZenCodeType.Method
    public static MmceFactoryRecipeFailureEvent castToFactoryRecipeFailureEvent(MmceMachineEvent event) {
        return event instanceof MmceFactoryRecipeFailureEvent recipeEvent ? recipeEvent : null;
    }

    @ZenCodeType.Method
    public static MmceFactoryRecipeFinishEvent castToFactoryRecipeFinishEvent(MmceMachineEvent event) {
        return event instanceof MmceFactoryRecipeFinishEvent recipeEvent ? recipeEvent : null;
    }

    private static void handlePhase(MmceMachineEvent event, MmceEventPhase phase, MmceMachineEventHandler handler) {
        if (event.getPhase() == phase && handler != null) {
            handler.handle(event);
        }
    }

    private static void handlePhase(MmceRecipeEvent event, MmceEventPhase phase, MmceRecipeEventHandler handler) {
        if (event.getPhase() == phase && handler != null) {
            handler.handle(event);
        }
    }
}
