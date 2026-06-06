package hellfirepvp.modularmachinery.port.machine;

import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.data.MmceDynamicPatternDefinition;
import hellfirepvp.modularmachinery.port.data.MmceStructurePart;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class MmceStructurePreview {
    public static List<Entry> build(MmceMachineDefinition machine, Direction facing) {
        return build(machine, facing, 0);
    }

    public static List<Entry> build(MmceMachineDefinition machine, Direction facing, int dynamicPatternSize) {
        List<Entry> entries = new ArrayList<>();
        for (MmceStructurePart part : machine.parts()) {
            addPartEntries(entries, part, facing, BlockPos.ZERO, true);
        }
        for (MmceDynamicPatternDefinition pattern : machine.dynamicPatterns()) {
            addDynamicPatternEntries(entries, pattern, facing, dynamicPatternSize);
        }
        return List.copyOf(entries);
    }

    private static void addDynamicPatternEntries(
            List<Entry> entries,
            MmceDynamicPatternDefinition pattern,
            Direction facing,
            int dynamicPatternSize
    ) {
        int size = Math.max(pattern.minSize(), Math.min(dynamicPatternSize, pattern.maxSize()));
        BlockPos offset = pattern.structureSizeOffsetStart();
        for (int index = 0; index < size; index++) {
            if (index > 0) {
                offset = offset.offset(pattern.structureSizeOffset());
            }
            for (MmceStructurePart part : pattern.parts()) {
                addPartEntries(entries, part, facing, offset, false);
            }
        }
        if (!pattern.partsEnd().isEmpty()) {
            BlockPos endOffset = offset.offset(pattern.structureSizeOffset());
            for (MmceStructurePart part : pattern.partsEnd()) {
                addPartEntries(entries, part, facing, endOffset, false);
            }
        }
    }

    private static void addPartEntries(
            List<Entry> entries,
            MmceStructurePart part,
            Direction facing,
            BlockPos baseOffset,
            boolean skipController
    ) {
        List<MmceBlockStateMatcher> candidates = new ArrayList<>();
        for (String element : part.elements()) {
            MmceStructureMatcher.addCandidates(element, candidates, Set.of());
        }
        if (candidates.isEmpty()) {
            return;
        }

        for (int x : part.x()) {
            for (int y : part.y()) {
                for (int z : part.z()) {
                    BlockPos local = baseOffset.offset(x, y, z);
                    if (skipController && local.equals(BlockPos.ZERO)) {
                        continue;
                    }
                    entries.add(new Entry(
                            MmceStructureMatcher.rotateFromNorth(local.getX(), local.getY(), local.getZ(), facing),
                            List.copyOf(candidates)));
                }
            }
        }
    }

    public static final class Entry {
        private final BlockPos offset;
        private final List<MmceBlockStateMatcher> candidates;

        private Entry(BlockPos offset, List<MmceBlockStateMatcher> candidates) {
            this.offset = offset;
            this.candidates = candidates;
        }

        public BlockPos offset() {
            return offset;
        }

        public BlockPos worldPos(BlockPos anchor) {
            return anchor.offset(offset);
        }

        public boolean matches(Level level, BlockPos anchor) {
            BlockState state = level.getBlockState(worldPos(anchor));
            return candidates.stream().anyMatch(candidate -> candidate.matches(state));
        }

        public Optional<BlockState> preferredState() {
            for (MmceBlockStateMatcher candidate : candidates) {
                Optional<BlockState> state = candidate.preferredState();
                if (state.isPresent()) {
                    return state;
                }
            }
            return Optional.empty();
        }
    }

    private MmceStructurePreview() {
    }
}
