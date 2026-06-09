package hellfirepvp.modularmachinery.port.integration;

import hellfirepvp.modularmachinery.port.event.MmceMachineEvent;

public record MmceUpgradeEventHandlerWrapper(
        MmceMachineEvent event,
        MmceMachineUpgrade upgrade
) {
}
