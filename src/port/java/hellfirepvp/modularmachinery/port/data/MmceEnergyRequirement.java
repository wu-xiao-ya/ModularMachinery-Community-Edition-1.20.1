package hellfirepvp.modularmachinery.port.data;

public record MmceEnergyRequirement(
        MmceIoType ioType,
        long energyPerTick,
        int triggerTime,
        boolean triggerRepeatable,
        boolean ignoreOutputCheck,
        boolean parallelizeUnaffected
) implements MmceParsedRequirement {
    public MmceEnergyRequirement {
        energyPerTick = Math.max(0L, energyPerTick);
        triggerTime = Math.max(0, triggerTime);
    }

    @Override
    public float chance() {
        return 1.0F;
    }
}
