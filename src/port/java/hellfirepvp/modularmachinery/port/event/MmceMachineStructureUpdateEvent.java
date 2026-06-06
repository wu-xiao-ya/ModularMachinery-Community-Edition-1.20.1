package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineStructureUpdateEvent")
public final class MmceMachineStructureUpdateEvent extends MmceMachineEvent {
    public MmceMachineStructureUpdateEvent(MachineControllerBlockEntity controller, ResourceLocation machineId) {
        super(controller, machineId, MmceMachineEventType.STRUCTURE_UPDATE, MmceEventPhase.END);
    }
}
