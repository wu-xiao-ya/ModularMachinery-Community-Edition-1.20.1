package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import org.openzen.zencode.java.ZenCodeType;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineEventHandler")
public interface MmceCTMachineEventHandler {
    @ZenCodeType.Method
    void handle(MmceCTMachineEvent event);
}
