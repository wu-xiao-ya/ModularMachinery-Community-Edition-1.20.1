package hellfirepvp.modularmachinery.port.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class MmcePayloads {
    private MmcePayloads() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                MmceSmartInterfaceUpdatePayload.TYPE,
                MmceSmartInterfaceUpdatePayload.STREAM_CODEC,
                MmceSmartInterfaceUpdatePayload::handle
        );
        registrar.playToServer(
                MmceFluidGuiInteractPayload.TYPE,
                MmceFluidGuiInteractPayload.STREAM_CODEC,
                MmceFluidGuiInteractPayload::handle
        );
        registrar.playToServer(
                MmceGroupInputConfigPayload.TYPE,
                MmceGroupInputConfigPayload.STREAM_CODEC,
                MmceGroupInputConfigPayload::handle
        );
        registrar.playToClient(
                MmceFactoryRunsPayload.TYPE,
                MmceFactoryRunsPayload.STREAM_CODEC,
                MmceFactoryRunsPayload::handle
        );
    }
}
