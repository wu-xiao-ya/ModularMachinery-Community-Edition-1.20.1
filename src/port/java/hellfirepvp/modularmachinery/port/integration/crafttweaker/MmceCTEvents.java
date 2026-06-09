package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.event.MmceControllerButtonClickEvent;
import hellfirepvp.modularmachinery.port.event.MmceControllerGUIRenderEvent;
import hellfirepvp.modularmachinery.port.event.MmceEventPhase;
import hellfirepvp.modularmachinery.port.event.MmceEventRegistry;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeFailureEvent;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeFinishEvent;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeStartEvent;
import hellfirepvp.modularmachinery.port.event.MmceFactoryRecipeTickEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineEventType;
import hellfirepvp.modularmachinery.port.event.MmceMachineStructureFormedEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineStructureUpdateEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineTickEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeCheckEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeEventType;
import hellfirepvp.modularmachinery.port.event.MmceRecipeFailureEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeFinishEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeStartEvent;
import hellfirepvp.modularmachinery.port.event.MmceRecipeTickEvent;
import hellfirepvp.modularmachinery.port.event.MmceResultChanceCreateEvent;
import hellfirepvp.modularmachinery.port.event.MmceSmartInterfaceUpdateEvent;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MMEvents")
public final class MmceCTEvents {
    private MmceCTEvents() {
    }

    @ZenCodeType.Method
    public static void onStructureFormed(String machineRegistryName, MmceCTMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.STRUCTURE_FORMED, wrap(handler));
    }

    @ZenCodeType.Method
    public static void onStructureUpdate(String machineRegistryName, MmceCTMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.STRUCTURE_UPDATE, wrap(handler));
    }

    @ZenCodeType.Method
    public static void onMachinePreTick(String machineRegistryName, MmceCTMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.TICK,
                event -> handlePhase(event, MmceEventPhase.START, handler));
    }

    @ZenCodeType.Method
    public static void onMachinePostTick(String machineRegistryName, MmceCTMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.TICK,
                event -> handlePhase(event, MmceEventPhase.END, handler));
    }

    @ZenCodeType.Method
    public static void onMachineTick(String machineRegistryName, MmceCTMachineEventHandler handler) {
        onMachinePostTick(machineRegistryName, handler);
    }

    @ZenCodeType.Method
    public static void onSmartInterfaceUpdate(String machineRegistryName, MmceCTMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.SMART_INTERFACE_UPDATE, wrap(handler));
    }

    @ZenCodeType.Method
    public static void onControllerButtonClick(String machineRegistryName, MmceCTMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.CONTROLLER_BUTTON_CLICK, wrap(handler));
    }

    @ZenCodeType.Method
    public static void onControllerGUIRender(String machineRegistryName, MmceCTMachineEventHandler handler) {
        MmceEventRegistry.registerMachine(machineRegistryName, MmceMachineEventType.CONTROLLER_GUI_RENDER, wrap(handler));
    }

    @ZenCodeType.Method
    public static void onRecipePreCheck(String recipeRegistryName, MmceCTRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.CHECK,
                event -> handlePhase(event, MmceEventPhase.START, handler));
    }

    @ZenCodeType.Method
    public static void onRecipePostCheck(String recipeRegistryName, MmceCTRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.CHECK,
                event -> handlePhase(event, MmceEventPhase.END, handler));
    }

    @ZenCodeType.Method
    public static void onRecipeCheck(String recipeRegistryName, MmceCTRecipeEventHandler handler) {
        onRecipePostCheck(recipeRegistryName, handler);
    }

    @ZenCodeType.Method
    public static void onRecipeStart(String recipeRegistryName, MmceCTRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.START, wrap(handler));
    }

    @ZenCodeType.Method
    public static void onRecipePreTick(String recipeRegistryName, MmceCTRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.TICK,
                event -> handlePhase(event, MmceEventPhase.START, handler));
    }

    @ZenCodeType.Method
    public static void onRecipePostTick(String recipeRegistryName, MmceCTRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.TICK,
                event -> handlePhase(event, MmceEventPhase.END, handler));
    }

    @ZenCodeType.Method
    public static void onRecipeTick(String recipeRegistryName, MmceCTRecipeEventHandler handler) {
        onRecipePostTick(recipeRegistryName, handler);
    }

    @ZenCodeType.Method
    public static void onRecipeFailure(String recipeRegistryName, MmceCTRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.FAILURE, wrap(handler));
    }

    @ZenCodeType.Method
    public static void onRecipeFinish(String recipeRegistryName, MmceCTRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.FINISH, wrap(handler));
    }

    @ZenCodeType.Method
    public static void onResultChance(String recipeRegistryName, MmceCTRecipeEventHandler handler) {
        MmceEventRegistry.registerRecipe(recipeRegistryName, MmceRecipeEventType.RESULT_CHANCE, wrap(handler));
    }

    @ZenCodeType.Method
    public static MmceCTRecipeThread recipeThread(MmceCTRecipeEvent event) {
        return event == null ? null : event.getRecipeThread();
    }

    @ZenCodeType.Method
    public static MmceCTFactoryRecipeThreadBuilder factoryRecipeThread(MmceCTRecipeEvent event) {
        return event == null ? null : event.getFactoryRecipeThread();
    }

    @ZenCodeType.Method
    public static MmceCTRecipeEvent castToRecipeCheckEvent(MmceCTMachineEvent event) {
        return recipeCast(event, MmceRecipeCheckEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTRecipeEvent castToRecipeStartEvent(MmceCTMachineEvent event) {
        return recipeCast(event, MmceRecipeStartEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTRecipeEvent castToRecipeTickEvent(MmceCTMachineEvent event) {
        return recipeCast(event, MmceRecipeTickEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTRecipeEvent castToRecipeFailureEvent(MmceCTMachineEvent event) {
        return recipeCast(event, MmceRecipeFailureEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTRecipeEvent castToRecipeFinishEvent(MmceCTMachineEvent event) {
        return recipeCast(event, MmceRecipeFinishEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTRecipeEvent castToResultChanceCreateEvent(MmceCTMachineEvent event) {
        return recipeCast(event, MmceResultChanceCreateEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTMachineEvent castToMachineStructureFormedEvent(MmceCTMachineEvent event) {
        return machineCast(event, MmceMachineStructureFormedEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTMachineEvent castToMachineStructureUpdateEvent(MmceCTMachineEvent event) {
        return machineCast(event, MmceMachineStructureUpdateEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTMachineEvent castToMachineTickEvent(MmceCTMachineEvent event) {
        return machineCast(event, MmceMachineTickEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTMachineEvent castToSmartInterfaceUpdateEvent(MmceCTMachineEvent event) {
        return machineCast(event, MmceSmartInterfaceUpdateEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTMachineEvent castToControllerButtonClickEvent(MmceCTMachineEvent event) {
        return machineCast(event, MmceControllerButtonClickEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTMachineEvent castToControllerGUIRenderEvent(MmceCTMachineEvent event) {
        return machineCast(event, MmceControllerGUIRenderEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTRecipeEvent castToFactoryRecipeStartEvent(MmceCTMachineEvent event) {
        return recipeCast(event, MmceFactoryRecipeStartEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTRecipeEvent castToFactoryRecipeTickEvent(MmceCTMachineEvent event) {
        return recipeCast(event, MmceFactoryRecipeTickEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTRecipeEvent castToFactoryRecipeFailureEvent(MmceCTMachineEvent event) {
        return recipeCast(event, MmceFactoryRecipeFailureEvent.class);
    }

    @ZenCodeType.Method
    public static MmceCTRecipeEvent castToFactoryRecipeFinishEvent(MmceCTMachineEvent event) {
        return recipeCast(event, MmceFactoryRecipeFinishEvent.class);
    }

    private static void handlePhase(MmceMachineEvent event, MmceEventPhase phase, MmceCTMachineEventHandler handler) {
        if (event.getPhase() == phase && handler != null) {
            handler.handle(MmceCTMachineEvent.of(event));
        }
    }

    private static void handlePhase(MmceRecipeEvent event, MmceEventPhase phase, MmceCTRecipeEventHandler handler) {
        if (event.getPhase() == phase && handler != null) {
            handler.handle(MmceCTRecipeEvent.of(event));
        }
    }

    private static hellfirepvp.modularmachinery.port.event.MmceMachineEventHandler wrap(MmceCTMachineEventHandler handler) {
        return event -> {
            if (handler != null) {
                handler.handle(MmceCTMachineEvent.of(event));
            }
        };
    }

    private static hellfirepvp.modularmachinery.port.event.MmceRecipeEventHandler wrap(MmceCTRecipeEventHandler handler) {
        return event -> {
            if (handler != null) {
                handler.handle(MmceCTRecipeEvent.of(event));
            }
        };
    }

    private static MmceCTMachineEvent machineCast(MmceCTMachineEvent event, Class<? extends MmceMachineEvent> type) {
        return event != null && type.isInstance(event.unwrap()) ? event : null;
    }

    private static MmceCTRecipeEvent recipeCast(MmceCTMachineEvent event, Class<? extends MmceRecipeEvent> type) {
        return event != null && type.isInstance(event.unwrap()) ? MmceCTRecipeEvent.of((MmceRecipeEvent) event.unwrap()) : null;
    }
}
