package hellfirepvp.modularmachinery.port.integration.kubejs;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;

public final class MmceKubeJSIntegration {
    private MmceKubeJSIntegration() {
    }

    public static void bootstrap() {
        try {
            Class.forName("dev.latvian.mods.kubejs.KubeJS", false, MmceKubeJSIntegration.class.getClassLoader());
            ModularMachineryNeoForge.LOGGER.info("KubeJS detected. MMCE globals are registered by MmceKubeJSPlugin.");
        } catch (ClassNotFoundException ignored) {
            ModularMachineryNeoForge.LOGGER.debug("KubeJS was reported loaded but its API class is not visible yet.");
        }
    }
}
