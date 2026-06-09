package hellfirepvp.modularmachinery.port.event;


@FunctionalInterface
public interface MmceMachineEventHandler {
    void handle(MmceMachineEvent event);
}
