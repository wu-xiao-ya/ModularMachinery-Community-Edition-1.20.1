package hellfirepvp.modularmachinery.port.item;

import java.util.Collection;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public class StatefulBlockItem<T extends Comparable<T>> extends BlockItem {
    private final Property<T> property;
    private final List<T> values;

    public StatefulBlockItem(Block block, Item.Properties properties, Property<T> property) {
        super(block, properties);
        this.property = property;
        this.values = List.copyOf(property.getPossibleValues());
    }

    public List<ItemStack> getCreativeStacks() {
        return values.stream().map(this::stackFor).toList();
    }

    public ItemStack stackFor(T value) {
        ItemStack stack = new ItemStack(this);
        stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(property, value));
        return stack;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        T value = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(property);
        if (value != null) {
            return getDescriptionId() + "." + property.getName(value);
        }
        return getDescriptionId();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        T value = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(property);
        if (value != null && property.getName().equals("size")) {
            appendSizeTooltip(value, tooltipComponents);
        }
    }

    private void appendSizeTooltip(T value, List<Component> tooltipComponents) {
        if (value instanceof hellfirepvp.modularmachinery.port.block.property.ItemBusSize size) {
            tooltipComponents.add(Component.translatable(size.slots() == 1 ? "tooltip.itembus.slot" : "tooltip.itembus.slots", size.slots()));
        } else if (value instanceof hellfirepvp.modularmachinery.port.block.property.FluidHatchSize size) {
            tooltipComponents.add(Component.translatable("tooltip.fluidhatch.tank.info", size.capacity()));
        } else if (value instanceof hellfirepvp.modularmachinery.port.block.property.EnergyHatchTier tier) {
            tooltipComponents.add(Component.literal(tier.capacity() + " FE"));
        }
    }

    public static void acceptCreativeStacks(Collection<ItemStack> target, Item item) {
        if (item instanceof StatefulBlockItem<?> stateful) {
            target.addAll(stateful.getCreativeStacks());
        } else {
            target.add(new ItemStack(item));
        }
    }
}
