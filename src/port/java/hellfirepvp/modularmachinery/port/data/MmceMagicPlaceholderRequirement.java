package hellfirepvp.modularmachinery.port.data;

import java.util.Optional;

public record MmceMagicPlaceholderRequirement(
        MmceIoType ioType,
        String kind,
        double amount,
        Optional<String> variant,
        double min,
        double max,
        boolean perTick,
        String requiredIntegration
) implements MmceParsedRequirement {
    public MmceMagicPlaceholderRequirement {
        kind = kind == null ? "" : kind;
        variant = variant == null ? Optional.empty() : variant;
        requiredIntegration = requiredIntegration == null ? "" : requiredIntegration;
    }

    @Override
    public float chance() {
        return 1.0F;
    }
}
