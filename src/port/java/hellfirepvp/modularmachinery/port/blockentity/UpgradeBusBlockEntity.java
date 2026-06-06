package hellfirepvp.modularmachinery.port.blockentity;

import hellfirepvp.modularmachinery.port.block.UpgradeBusBlock;
import hellfirepvp.modularmachinery.port.block.property.UpgradeBusTier;
import hellfirepvp.modularmachinery.port.integration.MmceMachineUpgradeHelper;
import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class UpgradeBusBlockEntity extends BaseMachineBlockEntity implements Container {
    private NonNullList<ItemStack> items;
    private CompoundTag upgradeCustomData = new CompoundTag();

    public UpgradeBusBlockEntity(BlockPos pos, BlockState state) {
        super(MmceBlockEntities.UPGRADE_BUS.get(), pos, state);
        this.items = NonNullList.withSize(getTier().slots(), ItemStack.EMPTY);
    }

    public UpgradeBusTier getTier() {
        return getBlockState().hasProperty(UpgradeBusBlock.TYPE)
                ? getBlockState().getValue(UpgradeBusBlock.TYPE)
                : UpgradeBusTier.NORMAL;
    }

    public NonNullList<ItemStack> getItems() {
        resizeIfNeeded();
        return items;
    }

    private void resizeIfNeeded() {
        int targetSize = getTier().slots();
        if (items.size() == targetSize) {
            return;
        }
        NonNullList<ItemStack> resized = NonNullList.withSize(targetSize, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(items.size(), resized.size()); i++) {
            resized.set(i, items.get(i));
        }
        items = resized;
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
            if (items.get(slot).isEmpty()) {
                clearUpgradeCustomDataSlot(slot);
            }
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        resizeIfNeeded();
        ItemStack result = ContainerHelper.takeItem(items, slot);
        if (!result.isEmpty()) {
            clearUpgradeCustomDataSlot(slot);
        }
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        resizeIfNeeded();
        ItemStack previous = items.get(slot);
        if (!ItemStack.isSameItemSameComponents(previous, stack)) {
            clearUpgradeCustomDataSlot(slot);
        }
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.isEmpty() || MmceMachineUpgradeHelper.supportsUpgrade(stack);
    }

    public CompoundTag getUpgradeCustomData(ResourceLocation upgradeId) {
        if (upgradeId == null) {
            return new CompoundTag();
        }
        return upgradeCustomData.getCompound(upgradeId.toString()).copy();
    }

    public CompoundTag getUpgradeCustomData(ResourceLocation upgradeId, int slot) {
        if (upgradeId == null) {
            return new CompoundTag();
        }
        String slotKey = upgradeSlotKey(upgradeId, slot);
        if (upgradeCustomData.contains(slotKey, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            return upgradeCustomData.getCompound(slotKey).copy();
        }
        return getUpgradeCustomData(upgradeId);
    }

    public void setUpgradeCustomData(ResourceLocation upgradeId, CompoundTag data) {
        if (upgradeId == null) {
            return;
        }
        if (data == null || data.isEmpty()) {
            upgradeCustomData.remove(upgradeId.toString());
        } else {
            upgradeCustomData.put(upgradeId.toString(), data.copy());
        }
        markForSync();
    }

    public void setUpgradeCustomData(ResourceLocation upgradeId, int slot, CompoundTag data) {
        if (upgradeId == null) {
            return;
        }
        String key = upgradeSlotKey(upgradeId, slot);
        if (data == null || data.isEmpty()) {
            upgradeCustomData.remove(key);
        } else {
            upgradeCustomData.put(key, data.copy());
        }
        markForSync();
    }

    public void clearUpgradeCustomDataSlot(int slot) {
        String suffix = "#" + Math.max(0, slot);
        java.util.List<String> keys = upgradeCustomData.getAllKeys().stream()
                .filter(key -> key.endsWith(suffix))
                .toList();
        for (String key : keys) {
            upgradeCustomData.remove(key);
        }
        if (!keys.isEmpty()) {
            markForSync();
        }
    }

    private static String upgradeSlotKey(ResourceLocation upgradeId, int slot) {
        return upgradeId + "#" + Math.max(0, slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
        upgradeCustomData = new CompoundTag();
        markForSync();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(getTier().slots(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        upgradeCustomData = tag.contains("upgradeCustomData", net.minecraft.nbt.Tag.TAG_COMPOUND)
                ? tag.getCompound("upgradeCustomData").copy()
                : new CompoundTag();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        resizeIfNeeded();
        ContainerHelper.saveAllItems(tag, items, registries);
        if (!upgradeCustomData.isEmpty()) {
            tag.put("upgradeCustomData", upgradeCustomData.copy());
        }
    }
}
