package hellfirepvp.modularmachinery.port.integration;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.event.MmceMachineEvent;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.UpgradeEventHandlerWrapper")
public record MmceUpgradeEventHandlerWrapper(
        @ZenCodeType.Getter("event") MmceMachineEvent event,
        @ZenCodeType.Getter("upgrade") MmceMachineUpgrade upgrade
) {
}
