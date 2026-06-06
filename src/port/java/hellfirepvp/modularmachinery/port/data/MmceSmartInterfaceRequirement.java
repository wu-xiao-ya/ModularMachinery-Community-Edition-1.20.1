package hellfirepvp.modularmachinery.port.data;

public record MmceSmartInterfaceRequirement(
        MmceIoType ioType,
        String interfaceType,
        float minValue,
        float maxValue
) implements MmceParsedRequirement {
    @Override
    public float chance() {
        return 1.0F;
    }
}
