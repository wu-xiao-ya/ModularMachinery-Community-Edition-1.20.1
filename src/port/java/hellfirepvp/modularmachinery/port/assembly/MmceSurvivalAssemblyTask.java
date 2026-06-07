package hellfirepvp.modularmachinery.port.assembly;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.machine.MmceStructurePreview;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

final class MmceSurvivalAssemblyTask {
    private static final int TICK_INTERVAL = 5;
    private static final int BLOCKS_PER_INTERVAL = 1;

    private final ResourceKey<Level> dimension;
    private final BlockPos controllerPos;
    private final ResourceLocation machineId;
    private final Deque<Step> pending;
    private final int total;
    private int ticker;
    private int placed;
    private int skipped;
    private boolean cancelled;

    MmceSurvivalAssemblyTask(
            ResourceKey<Level> dimension,
            BlockPos controllerPos,
            ResourceLocation machineId,
            List<Step> steps
    ) {
        this.dimension = dimension;
        this.controllerPos = controllerPos.immutable();
        this.machineId = machineId;
        this.pending = new ArrayDeque<>(steps);
        this.total = steps.size();
    }

    boolean tick(ServerPlayer player) {
        if (!player.level().dimension().equals(dimension) || !(player.level() instanceof ServerLevel level)) {
            player.displayClientMessage(Component.literal("MMCE assembly stopped: dimension changed."), false);
            return true;
        }
        if (!(level.getBlockEntity(controllerPos) instanceof MachineControllerBlockEntity controller)) {
            player.displayClientMessage(Component.literal("MMCE assembly stopped: controller missing."), false);
            return true;
        }
        boolean machineStillMatches = controller.getMachineId().filter(machineId::equals).isPresent()
                || controller.getBlueprintMachineId().filter(machineId::equals).isPresent();
        if (!machineStillMatches) {
            player.displayClientMessage(Component.literal("MMCE assembly stopped: controller machine changed."), false);
            return true;
        }
        ticker++;
        if (ticker < TICK_INTERVAL) {
            return false;
        }
        ticker = 0;

        int processed = 0;
        while (processed < BLOCKS_PER_INTERVAL && !pending.isEmpty()) {
            if (processNext(player, level, controller)) {
                processed++;
            }
        }

        if (pending.isEmpty()) {
            if (cancelled) {
                return true;
            }
            boolean formed = controller.refreshStructure();
            player.displayClientMessage(Component.literal("MMCE assembly complete: placed=" + placed
                    + ", skipped=" + skipped + ", formed=" + formed), false);
            return true;
        }
        return false;
    }

    int remaining() {
        return pending.size();
    }

    int total() {
        return total;
    }

    private boolean processNext(ServerPlayer player, ServerLevel level, MachineControllerBlockEntity controller) {
        Step step = pending.removeFirst();
        if (step.entry().matches(level, controllerPos)) {
            skipped++;
            return false;
        }
        BlockState current = level.getBlockState(step.pos());
        if (!canReplace(current)) {
            player.displayClientMessage(Component.literal("MMCE assembly stopped: blocked at "
                    + step.pos().toShortString()), false);
            cancel();
            return false;
        }
        if (!MmceSurvivalInventoryHelper.consume(player, step.state())) {
            player.displayClientMessage(Component.literal("MMCE assembly stopped: missing "
                    + MmceSurvivalInventoryHelper.describe(step.state())), false);
            cancel();
            return false;
        }
        if (level.setBlock(step.pos(), step.state(), 3)) {
            placed++;
        } else {
            ItemStack refund = MmceSurvivalInventoryHelper.stackFor(step.state());
            if (!refund.isEmpty() && !player.getInventory().add(refund)) {
                player.drop(refund, false);
            }
            player.displayClientMessage(Component.literal("MMCE assembly stopped: placement failed at "
                    + step.pos().toShortString()), false);
            cancel();
        }
        return true;
    }

    private void cancel() {
        cancelled = true;
        pending.clear();
    }

    static Optional<Step> buildStep(ServerLevel level, BlockPos controllerPos, MmceStructurePreview.Entry entry) {
        if (entry.matches(level, controllerPos)) {
            return Optional.empty();
        }
        Optional<BlockState> state = entry.preferredState();
        if (state.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Step(entry.worldPos(controllerPos), state.get(), entry));
    }

    static boolean canReplace(BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }

    record Step(BlockPos pos, BlockState state, MmceStructurePreview.Entry entry) {
        Step {
            pos = pos.immutable();
        }
    }
}
