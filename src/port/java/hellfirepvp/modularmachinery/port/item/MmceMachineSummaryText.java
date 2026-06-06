package hellfirepvp.modularmachinery.port.item;

import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

final class MmceMachineSummaryText {
    private MmceMachineSummaryText() {
    }

    static List<Component> chatSummary(MmceMachineDefinition machine, boolean includeModifiers) {
        List<Component> lines = new ArrayList<>();
        String structure = "Structure parts: " + machine.parts().size()
                + " | positions: " + machine.structureBlockCount();
        if (includeModifiers) {
            structure += " | modifiers: " + machine.modifiers().size();
        } else {
            structure += " | recipes: " + MmceDataRegistry.getRecipesFor(machine.id()).size();
        }
        lines.add(Component.literal(structure));

        lines.add(Component.literal("Threads: max=" + machine.maxThreads()
                + " | core=" + machine.coreThreads().size()
                + " | smart interfaces=" + machine.smartInterfaceTypes().size()));
        lines.add(Component.literal("Parallel: " + machine.parallelizable()
                + " | max=" + formatMaxParallelism(machine.maxParallelism())
                + " | internal=" + machine.internalParallelism()));

        int dynamicPatterns = dynamicPatternCount(machine);
        if (dynamicPatterns > 0 || machine.controllerBoundingBox().isPresent()) {
            lines.add(Component.literal("Advanced shape data: dynamicPatterns=" + dynamicPatterns
                    + " | controllerBoundingBox=" + machine.controllerBoundingBox().isPresent()));
        }
        return lines;
    }

    static List<Component> tooltipSummary(MmceMachineDefinition machine) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("Parts " + machine.parts().size()
                + ", positions " + machine.structureBlockCount()
                + ", recipes " + MmceDataRegistry.getRecipesFor(machine.id()).size())
                .withStyle(ChatFormatting.GRAY));

        int dynamicPatterns = dynamicPatternCount(machine);
        if (dynamicPatterns > 0 || !machine.coreThreads().isEmpty() || !machine.smartInterfaceTypes().isEmpty()) {
            lines.add(Component.literal("Dynamic patterns " + dynamicPatterns
                    + ", core threads " + machine.coreThreads().size()
                    + ", smart interfaces " + machine.smartInterfaceTypes().size())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        if (!machine.parallelizable()
                || machine.maxParallelism() != Integer.MAX_VALUE
                || machine.internalParallelism() > 0
                || machine.controllerBoundingBox().isPresent()) {
            lines.add(Component.literal("Parallel " + machine.parallelizable()
                    + ", max " + formatMaxParallelism(machine.maxParallelism())
                    + ", internal " + machine.internalParallelism()
                    + ", controller box " + machine.controllerBoundingBox().isPresent())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        return lines;
    }

    private static int dynamicPatternCount(MmceMachineDefinition machine) {
        return machine.dynamicPatterns().size();
    }

    private static String formatMaxParallelism(int maxParallelism) {
        return maxParallelism == Integer.MAX_VALUE ? "unlimited" : Integer.toString(maxParallelism);
    }
}
