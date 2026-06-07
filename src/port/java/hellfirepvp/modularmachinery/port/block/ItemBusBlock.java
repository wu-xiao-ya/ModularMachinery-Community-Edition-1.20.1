package hellfirepvp.modularmachinery.port.block;

import hellfirepvp.modularmachinery.port.block.property.ItemBusSize;
import hellfirepvp.modularmachinery.port.blockentity.ItemBusBlockEntity;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class ItemBusBlock extends MachineComponentBlock {
    public static final EnumProperty<ItemBusSize> SIZE = EnumProperty.create("size", ItemBusSize.class);

    public ItemBusBlock(BlockBehaviour.Properties properties, boolean input) {
        super(properties, input ? ItemBusBlockEntity::input : ItemBusBlockEntity::output);
        registerDefaultState(stateDefinition.any().setValue(SIZE, ItemBusSize.NORMAL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ItemBusBlockEntity bus) {
            Containers.dropContents(level, pos, bus.getItems());
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty() || !(level.getBlockEntity(pos) instanceof ItemBusBlockEntity bus) || !bus.isInput()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide()) {
            ItemStack remainder = insertIntoBus(bus, stack);
            if (remainder.getCount() != stack.getCount()) {
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, remainder);
                }
                player.displayClientMessage(Component.literal("Inserted " + (stack.getCount() - remainder.getCount()) + " item(s)"), true);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof ItemBusBlockEntity bus)) {
            return InteractionResult.PASS;
        }

        if (!player.isShiftKeyDown()) {
            return MmceMachineMenu.open(level, pos, player);
        }

        InteractionResult groupConfigResult = tryOpenGroupInputConfig(level, pos, player);
        if (groupConfigResult != InteractionResult.PASS) {
            return groupConfigResult;
        }

        if (!level.isClientSide()) {
            ItemStack extracted = extractFromBus(bus, player.isShiftKeyDown() ? 64 : 1);
            if (extracted.isEmpty()) {
                player.displayClientMessage(Component.literal("Item bus is empty"), true);
            } else if (!player.getInventory().add(extracted)) {
                player.drop(extracted, false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static ItemStack insertIntoBus(ItemBusBlockEntity bus, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < bus.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack existing = bus.getItem(slot);
            int limit = Math.min(bus.getMaxStackSize(remaining), remaining.getMaxStackSize());
            if (existing.isEmpty()) {
                int inserted = Math.min(remaining.getCount(), limit);
                bus.setItem(slot, remaining.copyWithCount(inserted));
                remaining.shrink(inserted);
            } else if (ItemStack.isSameItemSameComponents(existing, remaining)) {
                int inserted = Math.min(remaining.getCount(), limit - existing.getCount());
                if (inserted > 0) {
                    ItemStack merged = existing.copy();
                    merged.grow(inserted);
                    bus.setItem(slot, merged);
                    remaining.shrink(inserted);
                }
            }
        }
        return remaining;
    }

    private static ItemStack extractFromBus(ItemBusBlockEntity bus, int maxAmount) {
        for (int slot = bus.getContainerSize() - 1; slot >= 0; slot--) {
            ItemStack stack = bus.getItem(slot);
            if (!stack.isEmpty()) {
                return bus.removeItem(slot, Math.min(maxAmount, stack.getCount()));
            }
        }
        return ItemStack.EMPTY;
    }
}
