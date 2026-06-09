package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.resources.ResourceLocation;

public final class MmceControllerButtonClickEvent extends MmceMachineEvent {
    private final int buttonId;

    public MmceControllerButtonClickEvent(MachineControllerBlockEntity controller, ResourceLocation machineId, int buttonId) {
        super(controller, machineId, MmceMachineEventType.CONTROLLER_BUTTON_CLICK, MmceEventPhase.END);
        this.buttonId = buttonId;
    }
    public int getButtonId() {
        return buttonId;
    }
}
