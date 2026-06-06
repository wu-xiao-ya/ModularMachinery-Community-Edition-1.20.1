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
    }
}
