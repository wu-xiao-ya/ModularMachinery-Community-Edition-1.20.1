package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineTickEvent")
public final class MmceMachineTickEvent extends MmceMachineEvent {
    public MmceMachineTickEvent(MachineControllerBlockEntity controller, ResourceLocation machineId, MmceEventPhase phase) {
        super(controller, machineId, MmceMachineEventType.TICK, phase);
    }
}
