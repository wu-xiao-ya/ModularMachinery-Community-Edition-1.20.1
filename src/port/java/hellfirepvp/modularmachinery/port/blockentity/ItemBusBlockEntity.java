package hellfirepvp.modularmachinery.port.blockentity;

import hellfirepvp.modularmachinery.port.block.ItemBusBlock;
import hellfirepvp.modularmachinery.port.block.property.ItemBusSize;
import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ItemBusBlockEntity extends BaseMachineBlockEntity implements Container {
    private final boolean input;
    private NonNullList<ItemStack> items;

    public static ItemBusBlockEntity input(BlockPos pos, BlockState state) {
        return new ItemBusBlockEntity(MmceBlockEntities.ITEM_INPUT_BUS.get(), pos, state, true);
    }

    public static ItemBusBlockEntity output(BlockPos pos, BlockState state) {
        return new ItemBusBlockEntity(MmceBlockEntities.ITEM_OUTPUT_BUS.get(), pos, state, false);
    }

    private ItemBusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, boolean input) {
        super(type, pos, state);
        this.input = input;
        this.items = NonNullList.withSize(sizeFromState(state).slots(), ItemStack.EMPTY);
    }

    public boolean isInput() {
        return input;
    }

    public ItemBusSize getSize() {
        return sizeFromState(getBlockState());
    }

    public NonNullList<ItemStack> getItems() {
        resizeIfNeeded();
        return items;
    }

    private void resizeIfNeeded() {
        int targetSize = getSize().slots();
        if (items.size() == targetSize) {
            return;
        }
        NonNullList<ItemStack> resized = NonNullList.withSize(targetSize, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(items.size(), resized.size()); i++) {
            resized.set(i, items.get(i));
        }
        items = resized;
    }

    private static ItemBusSize sizeFromState(BlockState state) {
        return state.hasProperty(ItemBusBlock.SIZE) ? state.getValue(ItemBusBlock.SIZE) : ItemBusSize.NORMAL;
    }

    @Override
    public int getContainerSize() {
        resizeIfNeeded();
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        resizeIfNeeded();
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        resizeIfNeeded();
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        resizeIfNeeded();
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        resizeIfNeeded();
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        resizeIfNeeded();
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(getSize().slots(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        resizeIfNeeded();
        ContainerHelper.saveAllItems(tag, items, registries);
    }
}
