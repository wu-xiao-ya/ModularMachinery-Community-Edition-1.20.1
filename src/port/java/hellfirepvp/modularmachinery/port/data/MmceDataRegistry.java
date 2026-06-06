package hellfirepvp.modularmachinery.port.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public final class MmceDataRegistry {
    private static volatile Snapshot snapshot = Snapshot.EMPTY;

    public static Snapshot snapshot() {
        return snapshot;
    }

    static void replace(Snapshot nextSnapshot) {
        snapshot = nextSnapshot;
    }

    public static Optional<MmceMachineDefinition> getMachine(ResourceLocation id) {
        return Optional.ofNullable(snapshot.machines().get(id));
    }

    public static List<MmceRecipeDefinition> getRecipesFor(ResourceLocation machineId) {
        return snapshot.recipesByMachine().getOrDefault(machineId, List.of());
    }

    public record Snapshot(
            Map<ResourceLocation, MmceMachineDefinition> machines,
            Map<ResourceLocation, MmceRecipeDefinition> recipes,
            Map<ResourceLocation, List<MmceRecipeDefinition>> recipesByMachine,
            Map<ResourceLocation, MmceRecipeAdapterDefinition> adapters,
            Map<String, List<String>> variables,
            List<MmceDataLoadIssue> loadIssues
    ) {
        public Snapshot {
            loadIssues = List.copyOf(loadIssues == null ? List.of() : loadIssues);
        }

        static final Snapshot EMPTY = new Snapshot(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), List.of());
    }

    private MmceDataRegistry() {
    }
}
