package hellfirepvp.modularmachinery.port.capability;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

final class MmceItemHandler implements IItemHandlerModifiable {
    private static final int DEFAULT_SLOT_LIMIT = 64;

    private final Container container;
    private final boolean canInsert;
    private final boolean canExtract;

    MmceItemHandler(Container container, boolean canInsert, boolean canExtract) {
        this.container = container;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (isSlotInRange(slot)) {
            container.setItem(slot, stack);
        }
    }

    @Override
    public int getSlots() {
        return container.getContainerSize();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return isSlotInRange(slot) ? container.getItem(slot).copy() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!canInsert || stack.isEmpty() || !isSlotInRange(slot) || !container.canPlaceItem(slot, stack)) {
            return stack;
        }

        ItemStack existing = container.getItem(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(existing, stack)) {
                return stack;
            }
            limit = Math.min(limit, existing.getMaxStackSize());
            if (existing.getCount() >= limit) {
                return stack;
            }
        }

        int inserted = Math.min(stack.getCount(), limit - existing.getCount());
        if (inserted <= 0) {
            return stack;
        }

        if (!simulate) {
            if (existing.isEmpty()) {
                container.setItem(slot, stack.copyWithCount(inserted));
            } else {
                ItemStack merged = existing.copy();
                merged.grow(inserted);
                container.setItem(slot, merged);
            }
        }

        return inserted == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!canExtract || amount <= 0 || !isSlotInRange(slot)) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = container.getItem(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int extracted = Math.min(amount, Math.min(existing.getCount(), existing.getMaxStackSize()));
        if (extracted <= 0) {
            return ItemStack.EMPTY;
        }

        return simulate ? existing.copyWithCount(extracted) : container.removeItem(slot, extracted);
    }

    @Override
    public int getSlotLimit(int slot) {
        return isSlotInRange(slot) ? Math.min(DEFAULT_SLOT_LIMIT, container.getMaxStackSize()) : 0;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return canInsert && isSlotInRange(slot) && container.canPlaceItem(slot, stack);
    }

    private boolean isSlotInRange(int slot) {
        return slot >= 0 && slot < container.getContainerSize();
    }
}
