package hellfirepvp.modularmachinery.port.event;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.resources.ResourceLocation;

public final class MmceMachineTickEvent extends MmceMachineEvent {
    public MmceMachineTickEvent(MachineControllerBlockEntity controller, ResourceLocation machineId, MmceEventPhase phase) {
        super(controller, machineId, MmceMachineEventType.TICK, phase);
    }
}
