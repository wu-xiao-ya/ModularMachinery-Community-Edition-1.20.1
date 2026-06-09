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
                MmceEnergyHatchDataPayload.TYPE,
                MmceEnergyHatchDataPayload.STREAM_CODEC,
                MmceEnergyHatchDataPayload::handle
        );
        registrar.playToClient(
                MmceFluidHatchDataPayload.TYPE,
                MmceFluidHatchDataPayload.STREAM_CODEC,
                MmceFluidHatchDataPayload::handle
        );
        registrar.playToClient(
                MmceControllerDataPayload.TYPE,
                MmceControllerDataPayload.STREAM_CODEC,
                MmceControllerDataPayload::handle
        );
        registrar.playToClient(
                MmceSmartInterfaceDataPayload.TYPE,
                MmceSmartInterfaceDataPayload.STREAM_CODEC,
                MmceSmartInterfaceDataPayload::handle
        );
        registrar.playToClient(
                MmceFactoryRunsPayload.TYPE,
                MmceFactoryRunsPayload.STREAM_CODEC,
                MmceFactoryRunsPayload::handle
        );
    }
}
