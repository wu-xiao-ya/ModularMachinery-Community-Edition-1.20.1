package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.event.MmceControllerButtonClickEvent;
import hellfirepvp.modularmachinery.port.event.MmceControllerGUIRenderEvent;
import hellfirepvp.modularmachinery.port.event.MmceMachineEvent;
import hellfirepvp.modularmachinery.port.event.MmceSmartInterfaceUpdateEvent;
import hellfirepvp.modularmachinery.port.integration.MmceRecipeModifier;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineEvent")
public class MmceCTMachineEvent {
    private final MmceMachineEvent event;
    private final MmceCTMachineController controller;

    MmceCTMachineEvent(MmceMachineEvent event) {
        this.event = event;
        this.controller = event == null ? null : MmceCTMachineController.of(event.getController());
    }

    static MmceCTMachineEvent of(MmceMachineEvent event) {
        return event == null ? null : new MmceCTMachineEvent(event);
    }

    @ZenCodeType.Getter("controller")
    public MmceCTMachineController getController() {
        return controller;
    }

    @ZenCodeType.Getter("machineId")
    public String getMachineId() {
        return event.getMachineId();
    }

    @ZenCodeType.Getter("type")
    public String getType() {
        return event.getType();
    }

    @ZenCodeType.Getter("phase")
    public String getPhase() {
        return event.getPhase() == null ? "" : event.getPhase().name().toLowerCase(java.util.Locale.ROOT);
    }

    @ZenCodeType.Getter("canceled")
    public boolean isCanceled() {
        return event.isCanceled();
    }

    @ZenCodeType.Setter("canceled")
    public void setCanceled(boolean canceled) {
        event.setCanceled(canceled);
    }

    @ZenCodeType.Getter("controllerX")
    public int getControllerX() {
        return event.getControllerX();
    }

    @ZenCodeType.Getter("controllerY")
    public int getControllerY() {
        return event.getControllerY();
    }

    @ZenCodeType.Getter("controllerZ")
    public int getControllerZ() {
        return event.getControllerZ();
    }

    @ZenCodeType.Getter("buttonId")
    public int getButtonId() {
        return event instanceof MmceControllerButtonClickEvent buttonEvent ? buttonEvent.getButtonId() : -1;
    }

    @ZenCodeType.Getter("extraInfo")
    public String[] getExtraInfo() {
        return event instanceof MmceControllerGUIRenderEvent guiEvent ? guiEvent.getExtraInfo() : new String[0];
    }

    @ZenCodeType.Setter("extraInfo")
    public void setExtraInfo(String... info) {
        if (event instanceof MmceControllerGUIRenderEvent guiEvent) {
            guiEvent.setExtraInfo(info);
        }
    }

    @ZenCodeType.Getter("interfaceType")
    public String getInterfaceType() {
        return event instanceof MmceSmartInterfaceUpdateEvent smartEvent ? smartEvent.getInterfaceType() : "";
    }

    @ZenCodeType.Getter("oldValue")
    public float getOldValue() {
        return event instanceof MmceSmartInterfaceUpdateEvent smartEvent ? smartEvent.getOldValue() : 0.0F;
    }

    @ZenCodeType.Getter("newValue")
    public float getNewValue() {
        return event instanceof MmceSmartInterfaceUpdateEvent smartEvent ? smartEvent.getNewValue() : 0.0F;
    }

    @ZenCodeType.Getter("interfaceX")
    public int getInterfaceX() {
        return event instanceof MmceSmartInterfaceUpdateEvent smartEvent ? smartEvent.getInterfaceX() : 0;
    }

    @ZenCodeType.Getter("interfaceY")
    public int getInterfaceY() {
        return event instanceof MmceSmartInterfaceUpdateEvent smartEvent ? smartEvent.getInterfaceY() : 0;
    }

    @ZenCodeType.Getter("interfaceZ")
    public int getInterfaceZ() {
        return event instanceof MmceSmartInterfaceUpdateEvent smartEvent ? smartEvent.getInterfaceZ() : 0;
    }

    @ZenCodeType.Method
    public void cancel() {
        event.cancel();
    }

    @ZenCodeType.Method
    public void addModifier(String key, MmceRecipeModifier modifier) {
        event.addModifier(key, modifier);
    }

    @ZenCodeType.Method
    public void removeModifier(String key) {
        event.removeModifier(key);
    }

    @ZenCodeType.Method
    public boolean hasModifier(String key) {
        return event.hasModifier(key);
    }

    @ZenCodeType.Method
    public void addPermanentModifier(String key, MmceRecipeModifier modifier) {
        event.addPermanentModifier(key, modifier);
    }

    @ZenCodeType.Method
    public void removePermanentModifier(String key) {
        event.removePermanentModifier(key);
    }

    @ZenCodeType.Method
    public boolean hasPermanentModifier(String key) {
        return event.hasPermanentModifier(key);
    }

    @ZenCodeType.Method
    public void addExtraInfo(String line) {
        if (event instanceof MmceControllerGUIRenderEvent guiEvent) {
            guiEvent.addExtraInfo(line);
        }
    }

    @ZenCodeType.Method
    public void addExtraInfo(String... info) {
        if (event instanceof MmceControllerGUIRenderEvent guiEvent) {
            guiEvent.addExtraInfo(info);
        }
    }

    @ZenCodeType.Method
    public void clearExtraInfo() {
        if (event instanceof MmceControllerGUIRenderEvent guiEvent) {
            guiEvent.clearExtraInfo();
        }
    }

    MmceMachineEvent unwrap() {
        return event;
    }
}
