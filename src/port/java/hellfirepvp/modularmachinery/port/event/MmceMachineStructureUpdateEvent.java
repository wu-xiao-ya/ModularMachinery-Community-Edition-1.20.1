package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.resources.ResourceLocation;

public final class MmceMachineStructureUpdateEvent extends MmceMachineEvent {
    public MmceMachineStructureUpdateEvent(MachineControllerBlockEntity controller, ResourceLocation machineId) {
        super(controller, machineId, MmceMachineEventType.STRUCTURE_UPDATE, MmceEventPhase.END);
    }
}
