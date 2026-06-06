package hellfirepvp.modularmachinery.port.data;

import java.util.Locale;
import java.util.Optional;

public enum MmceIoType {
    INPUT,
    OUTPUT;

    public static Optional<MmceIoType> byName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(name.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
