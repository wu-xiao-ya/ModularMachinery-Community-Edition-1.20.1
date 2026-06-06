package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import org.openzen.zencode.java.ZenCodeType;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineEventHandler")
public interface MmceMachineEventHandler {
    @ZenCodeType.Method
    void handle(MmceMachineEvent event);
}
