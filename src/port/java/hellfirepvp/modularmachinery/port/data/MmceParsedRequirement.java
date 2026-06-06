package hellfirepvp.modularmachinery.port.data;

public sealed interface MmceParsedRequirement permits MmceItemRequirement, MmceFuelRequirement, MmceFluidRequirement, MmceChemicalRequirement, MmceEnergyRequirement, MmceIngredientArrayRequirement, MmceSmartInterfaceRequirement {
    MmceIoType ioType();

    float chance();

    default int triggerTime() {
        return 0;
    }

    default boolean triggerRepeatable() {
        return false;
    }

    default boolean ignoreOutputCheck() {
        return false;
    }

    default boolean parallelizeUnaffected() {
        return false;
    }
}
