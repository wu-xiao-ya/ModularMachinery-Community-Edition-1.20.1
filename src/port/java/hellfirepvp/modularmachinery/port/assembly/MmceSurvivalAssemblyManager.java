package hellfirepvp.modularmachinery.port.assembly;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.machine.MmceStructurePreview;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public final class MmceSurvivalAssemblyManager {
    private static final Map<UUID, MmceSurvivalAssemblyTask> TASKS = new LinkedHashMap<>();

    private MmceSurvivalAssemblyManager() {
    }

    public static boolean start(
            ServerPlayer player,
            ServerLevel level,
            MachineControllerBlockEntity controller,
            MmceMachineDefinition machine
    ) {
        PrepareResult result = prepare(player, level, controller, machine);
        if (!result.canStart()) {
            player.displayClientMessage(Component.literal("MMCE assembly cannot start" + result.summary()), false);
            result.missing().entrySet().stream()
                    .limit(3)
                    .forEach(entry -> player.displayClientMessage(Component.literal("Missing "
                            + entry.getValue() + "x " + MmceSurvivalInventoryHelper.describe(entry.getKey())), false));
            return false;
        }
        if (result.steps().isEmpty()) {
            player.displayClientMessage(Component.literal("MMCE assembly: nothing to place, formed="
                    + controller.refreshStructure() + result.summary()), false);
            return false;
        }
        MmceSurvivalAssemblyTask task = new MmceSurvivalAssemblyTask(
                level.dimension(),
                controller.getBlockPos(),
                machine.id(),
                result.steps());
        TASKS.put(player.getUUID(), task);
        player.displayClientMessage(Component.literal("MMCE assembly started: " + task.total()
                + " block(s), one block every 5 ticks."), false);
        return true;
    }

    public static void tick(ServerPlayer player) {
        MmceSurvivalAssemblyTask task = TASKS.get(player.getUUID());
        if (task != null && task.tick(player)) {
            TASKS.remove(player.getUUID());
        }
    }

    public static void remove(ServerPlayer player) {
        TASKS.remove(player.getUUID());
    }

    public static Optional<Integer> remaining(ServerPlayer player) {
        return Optional.ofNullable(TASKS.get(player.getUUID())).map(MmceSurvivalAssemblyTask::remaining);
    }

    private static PrepareResult prepare(
            ServerPlayer player,
            ServerLevel level,
            MachineControllerBlockEntity controller,
            MmceMachineDefinition machine
    ) {
        List<MmceStructurePreview.Entry> entries = MmceStructurePreview.build(machine, controllerFacing(controller));
        Set<BlockPos> visited = new LinkedHashSet<>();
        List<MmceSurvivalAssemblyTask.Step> steps = new ArrayList<>();
        Map<BlockState, Integer> required = new LinkedHashMap<>();
        int existing = 0;
        int blocked = 0;
        int unresolved = 0;
        for (MmceStructurePreview.Entry entry : entries) {
            BlockPos pos = entry.worldPos(controller.getBlockPos());
            if (!visited.add(pos)) {
                continue;
            }
            if (entry.matches(level, controller.getBlockPos())) {
                existing++;
                continue;
            }
            Optional<BlockState> preferred = entry.preferredState();
            if (preferred.isEmpty()) {
                unresolved++;
                continue;
            }
            if (!MmceSurvivalAssemblyTask.canReplace(level.getBlockState(pos))) {
                blocked++;
                continue;
            }
            BlockState state = preferred.get();
            steps.add(new MmceSurvivalAssemblyTask.Step(pos, state, entry));
            required.merge(state, 1, Integer::sum);
        }
        Map<BlockState, Integer> missing = MmceSurvivalInventoryHelper.missing(player, required);
        return new PrepareResult(steps, missing, existing, blocked, unresolved);
    }

    private static net.minecraft.core.Direction controllerFacing(MachineControllerBlockEntity controller) {
        return hellfirepvp.modularmachinery.port.item.MmceConstructToolItem.controllerFacing(controller);
    }

    private record PrepareResult(
            List<MmceSurvivalAssemblyTask.Step> steps,
            Map<BlockState, Integer> missing,
            int existing,
            int blocked,
            int unresolved
    ) {
        boolean canStart() {
            return blocked == 0 && unresolved == 0 && missing.isEmpty();
        }

        String summary() {
            return ": queued=" + steps.size()
                    + ", existing=" + existing
                    + ", blocked=" + blocked
                    + ", unresolved=" + unresolved
                    + ", missing=" + missing.values().stream().mapToInt(Integer::intValue).sum();
        }
    }
}
