package hellfirepvp.modularmachinery.port.machine;

import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceDynamicPatternDefinition;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.data.MmceMachineModifierDefinition;
import hellfirepvp.modularmachinery.port.data.MmceNbtCompat;
import hellfirepvp.modularmachinery.port.data.MmceStructurePart;
import hellfirepvp.modularmachinery.port.integration.MmceBlockChecker;
import hellfirepvp.modularmachinery.port.integration.MmceBlockCheckerRegistry;
import com.blamejared.crafttweaker.api.data.converter.tag.TagToDataConverter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class MmceStructureMatcher {
    public static Optional<MmceMachineDefinition> findFirst(Level level, BlockPos controllerPos, Direction facing) {
        return findFirstMatch(level, controllerPos, facing).map(MatchResult::machine);
    }

    public static Optional<MatchResult> findFirstMatch(Level level, BlockPos controllerPos, Direction facing) {
        return findFirstMatch(level, controllerPos, facing, machine -> true);
    }

    public static Optional<MatchResult> findFirstMatch(
            Level level,
            BlockPos controllerPos,
            Direction facing,
            Predicate<MmceMachineDefinition> filter
    ) {
        for (MmceMachineDefinition machine : MmceDataRegistry.snapshot().machines().values()) {
            if (!filter.test(machine)) {
                continue;
            }
            Optional<MatchResult> result = match(level, controllerPos, facing, machine);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    public static Optional<MmceMachineDefinition> findById(Level level, BlockPos controllerPos, Direction facing, ResourceLocation machineId) {
        return findByIdMatch(level, controllerPos, facing, machineId).map(MatchResult::machine);
    }

    public static Optional<MatchResult> findByIdMatch(Level level, BlockPos controllerPos, Direction facing, ResourceLocation machineId) {
        return MmceDataRegistry.getMachine(machineId).flatMap(machine -> match(level, controllerPos, facing, machine));
    }

    public static boolean matches(Level level, BlockPos controllerPos, Direction facing, MmceMachineDefinition machine) {
        return match(level, controllerPos, facing, machine).isPresent();
    }

    public static Optional<MatchResult> match(Level level, BlockPos controllerPos, Direction facing, MmceMachineDefinition machine) {
        Set<BlockPos> componentPositions = new LinkedHashSet<>();
        Map<BlockPos, String> componentTags = new LinkedHashMap<>();
        Map<MmceMachineModifierDefinition, MmceMachineModifierDefinition> activeModifiers = new LinkedHashMap<>();
        for (MmceStructurePart part : machine.parts()) {
            List<MmceBlockStateMatcher> candidates = new ArrayList<>();
            for (String element : part.elements()) {
                addCandidates(element, candidates, Set.of());
            }
            if (candidates.isEmpty()) {
                return Optional.empty();
            }

            for (int x : part.x()) {
                for (int y : part.y()) {
                    for (int z : part.z()) {
                        if (x == 0 && y == 0 && z == 0) {
                            continue;
                        }
                        BlockPos rotated = rotateFromNorth(x, y, z, facing);
                        BlockPos worldPos = controllerPos.offset(rotated);
                        BlockState state = level.getBlockState(worldPos);
                        boolean partMatches = candidates.stream().anyMatch(candidate -> candidate.matches(state))
                                && matchesNbt(level, worldPos, part.matchNbt())
                                && matchesChecker(level, worldPos, state, part.checkerId());
                        if (!partMatches) {
                            Optional<MmceMachineModifierDefinition> matchedModifier = matchingModifierAt(
                                    level, controllerPos, facing, machine, x, y, z, state);
                            if (matchedModifier.isEmpty()) {
                                return Optional.empty();
                            }
                            activeModifiers.put(matchedModifier.get(), matchedModifier.get());
                        }
                        BlockPos componentPos = controllerPos.offset(rotated);
                        componentPositions.add(componentPos);
                        part.selectorTag().ifPresent(tag -> componentTags.put(componentPos, tag));
                    }
                }
            }
        }
        Optional<List<DynamicPatternMatch>> dynamicMatches = matchDynamicPatterns(
                level, controllerPos, facing, machine, componentPositions, componentTags);
        if (dynamicMatches.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new MatchResult(
                machine,
                List.copyOf(componentPositions),
                Map.copyOf(componentTags),
                List.copyOf(activeModifiers.keySet()),
                dynamicMatches.get()));
    }

    public static BlockPos rotateFromNorth(int x, int y, int z, Direction facing) {
        Direction current = Direction.NORTH;
        BlockPos pos = new BlockPos(x, y, z);
        while (current != facing) {
            current = current.getCounterClockWise();
            pos = new BlockPos(pos.getZ(), pos.getY(), -pos.getX());
        }
        return pos;
    }

    static void addCandidates(String element, List<MmceBlockStateMatcher> candidates, Set<String> visitedVariables) {
        if (element == null || element.isBlank()) {
            return;
        }

        List<String> variableElements = MmceDataRegistry.snapshot().variables().get(element);
        if (variableElements != null) {
            if (visitedVariables.contains(element)) {
                return;
            }
            Set<String> nextVisited = new LinkedHashSet<>(visitedVariables);
            nextVisited.add(element);
            for (String variableElement : variableElements) {
                addCandidates(variableElement, candidates, nextVisited);
            }
            return;
        }

        MmceBlockStateMatcher.parse(element).ifPresent(candidates::add);
    }

    private static Optional<MmceMachineModifierDefinition> matchingModifierAt(
            Level level,
            BlockPos controllerPos,
            Direction facing,
            MmceMachineDefinition machine,
            int x,
            int y,
            int z,
            BlockState state
    ) {
        for (MmceMachineModifierDefinition modifier : machine.modifiers()) {
            if (!modifier.x().contains(x) || !modifier.y().contains(y) || !modifier.z().contains(z)) {
                continue;
            }
            BlockPos rotated = rotateFromNorth(x, y, z, facing);
            BlockPos worldPos = controllerPos.offset(rotated);
            List<MmceBlockStateMatcher> candidates = new ArrayList<>();
            for (String element : modifier.elements()) {
                addCandidates(element, candidates, Set.of());
            }
            if (!candidates.isEmpty() && candidates.stream().anyMatch(candidate -> candidate.matches(state))
                    && matchesNbt(level, worldPos, modifier.matchNbt())
                    && matchesChecker(level, worldPos, state, modifier.checkerId())
                    && matchesAllModifierPositions(level, controllerPos, facing, modifier)) {
                return Optional.of(modifier);
            }
        }
        return Optional.empty();
    }

    private static boolean matchesAllModifierPositions(
            Level level,
            BlockPos controllerPos,
            Direction facing,
            MmceMachineModifierDefinition modifier
    ) {
        List<MmceBlockStateMatcher> candidates = new ArrayList<>();
        for (String element : modifier.elements()) {
            addCandidates(element, candidates, Set.of());
        }
        if (candidates.isEmpty()) {
            return false;
        }

        for (int x : modifier.x()) {
            for (int y : modifier.y()) {
                for (int z : modifier.z()) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    BlockPos rotated = rotateFromNorth(x, y, z, facing);
                    BlockPos worldPos = controllerPos.offset(rotated);
                    BlockState state = level.getBlockState(worldPos);
                    if (candidates.stream().noneMatch(candidate -> candidate.matches(state))) {
                        return false;
                    }
                    if (!matchesNbt(level, worldPos, modifier.matchNbt())) {
                        return false;
                    }
                    if (!matchesChecker(level, worldPos, state, modifier.checkerId())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static Optional<List<DynamicPatternMatch>> matchDynamicPatterns(
            Level level,
            BlockPos controllerPos,
            Direction facing,
            MmceMachineDefinition machine,
            Set<BlockPos> componentPositions,
            Map<BlockPos, String> componentTags
    ) {
        if (machine.dynamicPatterns().isEmpty()) {
            return Optional.of(List.of());
        }

        Set<BlockPos> dynamicPositions = new LinkedHashSet<>();
        Map<BlockPos, String> dynamicTags = new LinkedHashMap<>();
        List<DynamicPatternMatch> matched = new ArrayList<>(machine.dynamicPatterns().size());
        for (MmceDynamicPatternDefinition pattern : machine.dynamicPatterns()) {
            Optional<DynamicPatternMatch> result = matchDynamicPattern(level, controllerPos, facing, pattern, dynamicPositions, dynamicTags);
            if (result.isEmpty()) {
                return Optional.empty();
            }
            matched.add(result.get());
        }
        componentPositions.addAll(dynamicPositions);
        componentTags.putAll(dynamicTags);
        return Optional.of(List.copyOf(matched));
    }

    private static Optional<DynamicPatternMatch> matchDynamicPattern(
            Level level,
            BlockPos controllerPos,
            Direction facing,
            MmceDynamicPatternDefinition pattern,
            Set<BlockPos> dynamicPositions,
            Map<BlockPos, String> dynamicTags
    ) {
        Optional<DynamicPatternMatch> best = Optional.empty();
        for (Direction patternFace : truePatternFaces(pattern, facing)) {
            Optional<DynamicPatternMatch> result = matchDynamicPatternFace(
                    level, controllerPos, facing, pattern, patternFace);
            if (result.isPresent() && (best.isEmpty() || result.get().size() > best.get().size())) {
                best = result;
            }
        }
        best.ifPresent(match -> {
            dynamicPositions.addAll(match.componentPositions());
            dynamicTags.putAll(match.componentTags());
        });
        return best;
    }

    private static Optional<DynamicPatternMatch> matchDynamicPatternFace(
            Level level,
            BlockPos controllerPos,
            Direction facing,
            MmceDynamicPatternDefinition pattern,
            Direction patternFace
    ) {
        BlockPos offset = pattern.structureSizeOffsetStart();
        BlockPos step = pattern.structureSizeOffset();
        Set<BlockPos> matchedPositions = new LinkedHashSet<>();
        Map<BlockPos, String> matchedTags = new LinkedHashMap<>();
        List<DynamicPartMatch> matchedSegments = new ArrayList<>();
        int size = 0;
        boolean first = true;

        while (true) {
            if (first) {
                first = false;
            } else {
                offset = offset.offset(step);
            }

            int segmentIndex = size;
            Optional<DynamicPartMatch> segment = matchPartsAt(
                    level, controllerPos, facing, offset, pattern.parts(), tag -> tag + "_" + pattern.name() + "_" + segmentIndex);
            if (segment.isPresent()) {
                size++;
                if (size > pattern.maxSize()) {
                    return Optional.empty();
                }
                matchedPositions.addAll(segment.get().positions());
                matchedTags.putAll(segment.get().tags());
                matchedSegments.add(segment.get());
                continue;
            }

            if (!pattern.partsEnd().isEmpty()) {
                Optional<DynamicPartMatch> end = matchPartsAt(
                        level, controllerPos, facing, offset, pattern.partsEnd(), tag -> tag + "_" + pattern.name() + "_end");
                if (end.isEmpty()) {
                    if (size <= 0) {
                        return Optional.empty();
                    }
                    BlockPos previous = offset.subtract(step);
                    end = matchPartsAt(
                            level, controllerPos, facing, previous, pattern.partsEnd(), tag -> tag + "_" + pattern.name() + "_end");
                    if (end.isEmpty()) {
                        return Optional.empty();
                    }
                    DynamicPartMatch overlappedSegment = matchedSegments.removeLast();
                    matchedPositions.removeAll(overlappedSegment.positions());
                    for (BlockPos pos : overlappedSegment.tags().keySet()) {
                        matchedTags.remove(pos);
                    }
                    size--;
                }
                matchedPositions.addAll(end.get().positions());
                matchedTags.putAll(end.get().tags());
            }
            break;
        }

        if (size < pattern.minSize()) {
            return Optional.empty();
        }
        return Optional.of(new DynamicPatternMatch(
                pattern.name(),
                patternFace,
                size,
                List.copyOf(matchedPositions),
                Map.copyOf(matchedTags)));
    }

    private static Set<Direction> truePatternFaces(MmceDynamicPatternDefinition pattern, Direction facing) {
        if (facing == Direction.NORTH) {
            return pattern.faces();
        }
        Set<Direction> faces = new LinkedHashSet<>();
        for (Direction face : pattern.faces()) {
            if (face.getAxis() == Direction.Axis.Y) {
                faces.add(face);
                continue;
            }
            Direction cursor = facing;
            Direction rotated = face;
            while (cursor != Direction.NORTH) {
                cursor = cursor.getCounterClockWise();
                rotated = rotated.getCounterClockWise();
            }
            faces.add(rotated);
        }
        return faces;
    }

    private static Optional<Set<BlockPos>> matchPartsAt(
            Level level,
            BlockPos controllerPos,
            Direction facing,
            BlockPos baseOffset,
            List<MmceStructurePart> parts
    ) {
        Set<BlockPos> matchedPositions = new LinkedHashSet<>();
        for (MmceStructurePart part : parts) {
            List<MmceBlockStateMatcher> candidates = new ArrayList<>();
            for (String element : part.elements()) {
                addCandidates(element, candidates, Set.of());
            }
            if (candidates.isEmpty()) {
                return Optional.empty();
            }

            for (int x : part.x()) {
                for (int y : part.y()) {
                    for (int z : part.z()) {
                        BlockPos local = baseOffset.offset(x, y, z);
                        BlockPos rotated = rotateFromNorth(local.getX(), local.getY(), local.getZ(), facing);
                        BlockPos worldPos = controllerPos.offset(rotated);
                        BlockState state = level.getBlockState(worldPos);
                        if (candidates.stream().noneMatch(candidate -> candidate.matches(state))) {
                            return Optional.empty();
                        }
                        if (!matchesNbt(level, worldPos, part.matchNbt())) {
                            return Optional.empty();
                        }
                        if (!matchesChecker(level, worldPos, state, part.checkerId())) {
                            return Optional.empty();
                        }
                        matchedPositions.add(worldPos);
                    }
                }
            }
        }
        return Optional.of(matchedPositions);
    }

    private static Optional<DynamicPartMatch> matchPartsAt(
            Level level,
            BlockPos controllerPos,
            Direction facing,
            BlockPos baseOffset,
            List<MmceStructurePart> parts,
            java.util.function.Function<String, String> tagName
    ) {
        Set<BlockPos> matchedPositions = new LinkedHashSet<>();
        Map<BlockPos, String> matchedTags = new LinkedHashMap<>();
        for (MmceStructurePart part : parts) {
            List<MmceBlockStateMatcher> candidates = new ArrayList<>();
            for (String element : part.elements()) {
                addCandidates(element, candidates, Set.of());
            }
            if (candidates.isEmpty()) {
                return Optional.empty();
            }

            for (int x : part.x()) {
                for (int y : part.y()) {
                    for (int z : part.z()) {
                        BlockPos local = baseOffset.offset(x, y, z);
                        BlockPos rotated = rotateFromNorth(local.getX(), local.getY(), local.getZ(), facing);
                        BlockPos worldPos = controllerPos.offset(rotated);
                        BlockState state = level.getBlockState(worldPos);
                        if (candidates.stream().noneMatch(candidate -> candidate.matches(state))) {
                            return Optional.empty();
                        }
                        if (!matchesNbt(level, worldPos, part.matchNbt())) {
                            return Optional.empty();
                        }
                        if (!matchesChecker(level, worldPos, state, part.checkerId())) {
                            return Optional.empty();
                        }
                        matchedPositions.add(worldPos);
                        part.selectorTag().ifPresent(tag -> matchedTags.put(worldPos, tagName.apply(tag)));
                    }
                }
            }
        }
        return Optional.of(new DynamicPartMatch(matchedPositions, matchedTags));
    }

    private static boolean matchesChecker(Level level, BlockPos worldPos, BlockState state, Optional<String> checkerId) {
        if (checkerId.isEmpty()) {
            return true;
        }
        MmceBlockChecker checker = MmceBlockCheckerRegistry.get(checkerId.get());
        if (checker == null) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(worldPos);
        CompoundTag tag = blockEntity == null ? new CompoundTag() : blockEntity.saveWithoutMetadata(level.registryAccess());
        return checker.isMatch(level, worldPos, state, TagToDataConverter.convert(tag));
    }

    private static boolean matchesNbt(Level level, BlockPos worldPos, com.google.gson.JsonObject expected) {
        if (expected == null || expected.isEmpty()) {
            return true;
        }
        BlockEntity blockEntity = level.getBlockEntity(worldPos);
        if (blockEntity == null) {
            return false;
        }
        CompoundTag tag = blockEntity.saveWithoutMetadata(level.registryAccess());
        return MmceNbtCompat.matches(tag, expected);
    }

    private MmceStructureMatcher() {
    }

    public record MatchResult(
            MmceMachineDefinition machine,
            List<BlockPos> componentPositions,
            Map<BlockPos, String> componentTags,
            List<MmceMachineModifierDefinition> activeModifiers,
            List<DynamicPatternMatch> dynamicPatterns
    ) {
    }

    public record DynamicPatternMatch(
            String name,
            Direction facing,
            int size,
            List<BlockPos> componentPositions,
            Map<BlockPos, String> componentTags
    ) {
    }

    private record DynamicPartMatch(
            Set<BlockPos> positions,
            Map<BlockPos, String> tags
    ) {
    }
}
