package hellfirepvp.modularmachinery.port.integration;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.event.MmceMachineEvent;
import org.openzen.zencode.java.ZenCodeType;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.UpgradeEventHandler")
public interface MmceUpgradeEventHandler {
    @ZenCodeType.Method
    void handle(MmceMachineEvent event, MmceMachineUpgrade upgrade);
}
