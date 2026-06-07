package hellfirepvp.modularmachinery.port.assembly;

import hellfirepvp.modularmachinery.port.item.StatefulBlockItem;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

final class MmceSurvivalInventoryHelper {
    private MmceSurvivalInventoryHelper() {
    }

    static boolean consume(ServerPlayer player, BlockState target) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (matches(stack, target)) {
                stack.shrink(1);
                player.getInventory().setChanged();
                return true;
            }
        }
        return false;
    }

    static Map<BlockState, Integer> missing(ServerPlayer player, Map<BlockState, Integer> required) {
        if (player.getAbilities().instabuild) {
            return Map.of();
        }
        Map<BlockState, Integer> remaining = new LinkedHashMap<>(required);
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) {
                continue;
            }
            int available = stack.getCount();
            for (Map.Entry<BlockState, Integer> entry : remaining.entrySet()) {
                if (available <= 0) {
                    break;
                }
                int needed = entry.getValue();
                if (needed <= 0 || !matches(stack, entry.getKey())) {
                    continue;
                }
                int used = Math.min(available, needed);
                available -= used;
                entry.setValue(needed - used);
            }
        }
        remaining.entrySet().removeIf(entry -> entry.getValue() <= 0);
        return Map.copyOf(remaining);
    }

    static String describe(BlockState state) {
        Item item = state.getBlock().asItem();
        if (item == ItemStack.EMPTY.getItem()) {
            return state.getBlock().getName().getString();
        }
        return item.getDescription().getString();
    }

    static ItemStack stackFor(BlockState state) {
        Item item = state.getBlock().asItem();
        if (item == ItemStack.EMPTY.getItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        BlockItemStateProperties properties = BlockItemStateProperties.EMPTY;
        for (Property<?> property : state.getProperties()) {
            if (!state.getBlock().defaultBlockState().hasProperty(property)
                    || !state.getBlock().defaultBlockState().getValue(property).equals(state.getValue(property))) {
                properties = with(properties, property, state);
            }
        }
        if (!properties.isEmpty()) {
            stack.set(DataComponents.BLOCK_STATE, properties);
        }
        return stack;
    }

    private static boolean matches(ItemStack stack, BlockState target) {
        if (stack.isEmpty()) {
            return false;
        }
        Block block = target.getBlock();
        Item item = block.asItem();
        if (item == ItemStack.EMPTY.getItem() || !stack.is(item)) {
            return false;
        }
        if (!(stack.getItem() instanceof BlockItem blockItem) || !blockItem.getBlock().equals(block)) {
            return false;
        }
        if (stack.getItem() instanceof StatefulBlockItem<?>) {
            return stateProperties(stack)
                    .map(properties -> properties.apply(block.defaultBlockState()).equals(target))
                    .orElseGet(() -> block.defaultBlockState().equals(target));
        }
        Optional<BlockItemStateProperties> properties = stateProperties(stack);
        return properties.map(value -> value.apply(block.defaultBlockState()).equals(target))
                .orElse(true);
    }

    private static Optional<BlockItemStateProperties> stateProperties(ItemStack stack) {
        BlockItemStateProperties properties = stack.get(DataComponents.BLOCK_STATE);
        return properties == null || properties.isEmpty() ? Optional.empty() : Optional.of(properties);
    }

    private static <T extends Comparable<T>> BlockItemStateProperties with(
            BlockItemStateProperties properties,
            Property<T> property,
            BlockState state
    ) {
        return properties.with(property, state);
    }
}
