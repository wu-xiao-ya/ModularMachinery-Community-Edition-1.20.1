package hellfirepvp.modularmachinery.port.integration;

import hellfirepvp.modularmachinery.port.event.MmceMachineEvent;

@FunctionalInterface
public interface MmceUpgradeEventHandler {
    void handle(MmceMachineEvent event, MmceMachineUpgrade upgrade);
}
