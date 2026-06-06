package hellfirepvp.modularmachinery.port.machine;

import hellfirepvp.modularmachinery.port.block.CasingBlock;
import hellfirepvp.modularmachinery.port.block.EnergyHatchBlock;
import hellfirepvp.modularmachinery.port.block.FluidHatchBlock;
import hellfirepvp.modularmachinery.port.block.ItemBusBlock;
import hellfirepvp.modularmachinery.port.block.ParallelControllerBlock;
import hellfirepvp.modularmachinery.port.block.SmartInterfaceBlock;
import hellfirepvp.modularmachinery.port.block.UpgradeBusBlock;
import hellfirepvp.modularmachinery.port.block.property.CasingType;
import hellfirepvp.modularmachinery.port.block.property.EnergyHatchTier;
import hellfirepvp.modularmachinery.port.block.property.FluidHatchSize;
import hellfirepvp.modularmachinery.port.block.property.ItemBusSize;
import hellfirepvp.modularmachinery.port.block.property.ParallelControllerTier;
import hellfirepvp.modularmachinery.port.block.property.SmartInterfaceMode;
import hellfirepvp.modularmachinery.port.block.property.UpgradeBusTier;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

final class MmceBlockStateMatcher {
    private final ResourceLocation blockId;
    private final Map<String, String> properties;
    private final int meta;
    private final boolean hasMeta;

    private MmceBlockStateMatcher(ResourceLocation blockId, Map<String, String> properties, int meta, boolean hasMeta) {
        this.blockId = blockId;
        this.properties = properties;
        this.meta = meta;
        this.hasMeta = hasMeta;
    }

    static Optional<MmceBlockStateMatcher> parse(String descriptor) {
        if (descriptor == null || descriptor.isBlank()) {
            return Optional.empty();
        }

        String trimmed = descriptor.trim();
        int metaSeparator = trimmed.lastIndexOf('@');
        String statePart = metaSeparator < 0 ? trimmed : trimmed.substring(0, metaSeparator);
        String metaPart = metaSeparator < 0 ? "" : trimmed.substring(metaSeparator + 1);
        try {
            ParsedState parsedState = parseState(statePart);
            return Optional.of(new MmceBlockStateMatcher(
                    parsedState.blockId(),
                    parsedState.properties(),
                    metaSeparator < 0 ? 0 : Integer.parseInt(metaPart),
                    metaSeparator >= 0));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    boolean matches(BlockState state) {
        Block block = BuiltInRegistries.BLOCK.get(blockId);
        if (block == Blocks.AIR && !blockId.equals(BuiltInRegistries.BLOCK.getKey(Blocks.AIR))) {
            return false;
        }
        if (!state.is(block)) {
            return false;
        }
        if (!matchesProperties(state)) {
            return false;
        }
        return !hasMeta || matchesMeta(state, meta);
    }

    Optional<BlockState> preferredState() {
        Block block = BuiltInRegistries.BLOCK.get(blockId);
        if (block == Blocks.AIR && !blockId.equals(BuiltInRegistries.BLOCK.getKey(Blocks.AIR))) {
            return Optional.empty();
        }
        Optional<BlockState> withProperties = applyProperties(block.defaultBlockState());
        if (withProperties.isEmpty()) {
            return Optional.empty();
        }
        BlockState state = withProperties.get();
        return Optional.of(hasMeta ? applyMeta(state, meta) : state);
    }

    private static ParsedState parseState(String statePart) {
        int propertyStart = statePart.indexOf('[');
        if (propertyStart < 0) {
            return new ParsedState(ResourceLocation.parse(statePart), Map.of());
        }
        if (!statePart.endsWith("]")) {
            throw new IllegalArgumentException("Invalid block state descriptor: " + statePart);
        }

        ResourceLocation blockId = ResourceLocation.parse(statePart.substring(0, propertyStart));
        String propertyPart = statePart.substring(propertyStart + 1, statePart.length() - 1);
        Map<String, String> properties = new LinkedHashMap<>();
        if (!propertyPart.isBlank()) {
            for (String pair : propertyPart.split(",")) {
                int separator = pair.indexOf('=');
                if (separator <= 0 || separator == pair.length() - 1) {
                    throw new IllegalArgumentException("Invalid block state property: " + pair);
                }
                properties.put(pair.substring(0, separator).trim(), pair.substring(separator + 1).trim());
            }
        }
        return new ParsedState(blockId, Map.copyOf(properties));
    }

    private boolean matchesProperties(BlockState state) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            Optional<? extends Property<?>> property = state.getProperties().stream()
                    .filter(candidate -> candidate.getName().equals(entry.getKey()))
                    .findFirst();
            if (property.isEmpty() || !matchesProperty(state, property.get(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private Optional<BlockState> applyProperties(BlockState state) {
        BlockState current = state;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            Optional<? extends Property<?>> property = current.getProperties().stream()
                    .filter(candidate -> candidate.getName().equals(entry.getKey()))
                    .findFirst();
            if (property.isEmpty()) {
                return Optional.empty();
            }
            Optional<BlockState> updated = applyProperty(current, property.get(), entry.getValue());
            if (updated.isEmpty()) {
                return Optional.empty();
            }
            current = updated.get();
        }
        return Optional.of(current);
    }

    private static <T extends Comparable<T>> boolean matchesProperty(BlockState state, Property<T> property, String value) {
        return property.getValue(value)
                .map(parsed -> state.getValue(property).equals(parsed))
                .orElse(false);
    }

    private static <T extends Comparable<T>> Optional<BlockState> applyProperty(BlockState state, Property<T> property, String value) {
        return property.getValue(value).map(parsed -> state.setValue(property, parsed));
    }

    private static BlockState applyMeta(BlockState state, int meta) {
        if (state.hasProperty(CasingBlock.CASING)) {
            return setOrdinal(state, CasingBlock.CASING, CasingType.values(), meta);
        }
        if (state.hasProperty(ItemBusBlock.SIZE)) {
            return setOrdinal(state, ItemBusBlock.SIZE, ItemBusSize.values(), meta);
        }
        if (state.hasProperty(FluidHatchBlock.SIZE)) {
            return setOrdinal(state, FluidHatchBlock.SIZE, FluidHatchSize.values(), meta);
        }
        if (state.hasProperty(EnergyHatchBlock.SIZE)) {
            return setOrdinal(state, EnergyHatchBlock.SIZE, EnergyHatchTier.values(), meta);
        }
        if (state.hasProperty(ParallelControllerBlock.TYPE)) {
            return setOrdinal(state, ParallelControllerBlock.TYPE, ParallelControllerTier.values(), meta);
        }
        if (state.hasProperty(UpgradeBusBlock.TYPE)) {
            return setOrdinal(state, UpgradeBusBlock.TYPE, UpgradeBusTier.values(), meta);
        }
        if (state.hasProperty(SmartInterfaceBlock.TYPE)) {
            return setOrdinal(state, SmartInterfaceBlock.TYPE, SmartInterfaceMode.values(), meta);
        }
        return state;
    }

    private static boolean matchesMeta(BlockState state, int meta) {
        if (state.hasProperty(CasingBlock.CASING)) {
            return matchesOrdinal(CasingType.values(), state.getValue(CasingBlock.CASING), meta);
        }
        if (state.hasProperty(ItemBusBlock.SIZE)) {
            return matchesOrdinal(ItemBusSize.values(), state.getValue(ItemBusBlock.SIZE), meta);
        }
        if (state.hasProperty(FluidHatchBlock.SIZE)) {
            return matchesOrdinal(FluidHatchSize.values(), state.getValue(FluidHatchBlock.SIZE), meta);
        }
        if (state.hasProperty(EnergyHatchBlock.SIZE)) {
            return matchesOrdinal(EnergyHatchTier.values(), state.getValue(EnergyHatchBlock.SIZE), meta);
        }
        if (state.hasProperty(ParallelControllerBlock.TYPE)) {
            return matchesOrdinal(ParallelControllerTier.values(), state.getValue(ParallelControllerBlock.TYPE), meta);
        }
        if (state.hasProperty(UpgradeBusBlock.TYPE)) {
            return matchesOrdinal(UpgradeBusTier.values(), state.getValue(UpgradeBusBlock.TYPE), meta);
        }
        if (state.hasProperty(SmartInterfaceBlock.TYPE)) {
            return matchesOrdinal(SmartInterfaceMode.values(), state.getValue(SmartInterfaceBlock.TYPE), meta);
        }
        return meta == 0;
    }

    private static <T extends Enum<T>> boolean matchesOrdinal(T[] values, T value, int meta) {
        int clamped = Math.max(0, Math.min(meta, values.length - 1));
        return values[clamped].name().toLowerCase(Locale.ROOT).equals(value.name().toLowerCase(Locale.ROOT));
    }

    private static <T extends Enum<T>> BlockState setOrdinal(BlockState state, Property<T> property, T[] values, int meta) {
        int clamped = Math.max(0, Math.min(meta, values.length - 1));
        return state.setValue(property, values[clamped]);
    }

    private record ParsedState(ResourceLocation blockId, Map<String, String> properties) {
    }
}
