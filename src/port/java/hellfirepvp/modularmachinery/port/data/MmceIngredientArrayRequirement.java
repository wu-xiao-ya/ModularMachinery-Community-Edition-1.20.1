package hellfirepvp.modularmachinery.port.data;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.RandomSource;

public record MmceIngredientArrayRequirement(
        MmceIoType ioType,
        List<MmceItemRequirement> candidates,
        float chance,
        boolean optional,
        List<MmceMachineModifierDefinition> modifiers,
        int triggerTime,
        boolean triggerRepeatable,
        boolean ignoreOutputCheck,
        boolean parallelizeUnaffected
) implements MmceParsedRequirement {
    public MmceIngredientArrayRequirement {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
        modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
        chance = Math.max(0.0F, Math.min(1.0F, chance));
        triggerTime = Math.max(0, triggerTime);
    }

    public Optional<MmceItemRequirement> selectCandidate(RandomSource random) {
        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        float total = 0.0F;
        for (MmceItemRequirement candidate : candidates) {
            total += Math.max(0.0F, candidate.chance());
        }
        if (total <= 0.0F) {
            return Optional.of(candidates.getFirst());
        }

        float selected = random.nextFloat() * total;
        float cursor = 0.0F;
        for (MmceItemRequirement candidate : candidates) {
            cursor += Math.max(0.0F, candidate.chance());
            if (cursor >= selected) {
                return Optional.of(candidate);
            }
        }
        return Optional.of(candidates.getLast());
    }
}
