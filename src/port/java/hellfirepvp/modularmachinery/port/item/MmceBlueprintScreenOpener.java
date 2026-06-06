package hellfirepvp.modularmachinery.port.item;

import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import java.util.Objects;
import java.util.function.Consumer;

public final class MmceBlueprintScreenOpener {
    private static final Consumer<MmceMachineDefinition> NO_OP = machine -> {
    };
    private static Consumer<MmceMachineDefinition> opener = NO_OP;

    private MmceBlueprintScreenOpener() {
    }

    public static void register(Consumer<MmceMachineDefinition> screenOpener) {
        opener = Objects.requireNonNull(screenOpener, "screenOpener");
    }

    public static boolean open(MmceMachineDefinition machine) {
        Consumer<MmceMachineDefinition> current = opener;
        if (current == NO_OP) {
            return false;
        }
        current.accept(machine);
        return true;
    }
}
