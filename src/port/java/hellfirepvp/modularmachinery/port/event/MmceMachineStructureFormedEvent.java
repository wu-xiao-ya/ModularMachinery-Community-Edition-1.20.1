package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.resources.ResourceLocation;

public final class MmceMachineStructureFormedEvent extends MmceMachineEvent {
    public MmceMachineStructureFormedEvent(MachineControllerBlockEntity controller, ResourceLocation machineId) {
        super(controller, machineId, MmceMachineEventType.STRUCTURE_FORMED, MmceEventPhase.END);
    }
}
