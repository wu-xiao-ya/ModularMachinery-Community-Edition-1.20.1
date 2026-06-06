package hellfirepvp.modularmachinery.common.tiles;

import github.kasuminova.mmce.common.util.concurrent.ReadWriteLockProvider;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.SelectiveUpdateTileEntity;
import hellfirepvp.modularmachinery.common.tiles.base.TileEntityRestrictedTick;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TileFluidProcessorHatch extends TileEntityRestrictedTick implements MachineComponentTile, SelectiveUpdateTileEntity, ReadWriteLockProvider {

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int DEFAULT_CAPACITY = 1000;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ProcessorInventory inventory = new ProcessorInventory(this, new int[]{INPUT_SLOT}, new int[]{OUTPUT_SLOT});

    private FluidStack fluid = null;
    private int capacity = DEFAULT_CAPACITY;

    public TileFluidProcessorHatch() {
        this.inventory.setListener(this::onInventoryChanged);
    }

    public IOInventory getInventory() {
        return inventory;
    }

    @Nullable
    @Override
    public MachineComponent<?> provideComponent() {
        return new MachineComponent.FluidHatch(IOType.INPUT) {
            @Override
            public net.minecraftforge.fluids.FluidTank getContainerProvider() {
                return null;
            }
        };
    }

    public FluidStack getFluidStack() {
        return fluid == null ? null : fluid.copy();
    }

    public int getCapacity() {
        return capacity;
    }

    private void onInventoryChanged(int slot) {
        if (world != null && !world.isRemote) {
            tryProcess();
        }
    }

    private void tryProcess() {
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (input.isEmpty()) {
            return;
        }

        IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(input);
        if (fluidHandler == null) {
            return;
        }

        ItemStack container = input.copy();
        FluidStack drained = fluidHandler.drain(Integer.MAX_VALUE, false);
        if (drained == null || drained.amount <= 0) {
            return;
        }
        if (fluid == null) {
            fluid = drained.copy();
        } else {
            if (!fluid.isFluidEqual(drained)) {
                return;
            }
            fluid.amount += drained.amount;
        }

        if (fluid.amount > capacity) {
            fluid.amount = capacity;
        }

        ItemStack result = FluidUtil.getFilledBucket(drained);
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, result);
        } else if (ItemStack.areItemStacksEqual(output, result) && ItemStack.areItemStackTagsEqual(output, result)) {
            output.grow(result.getCount());
            inventory.setStackInSlot(OUTPUT_SLOT, output);
        }

        inventory.setStackInSlot(INPUT_SLOT, ItemStack.EMPTY);
        markNoUpdateSync();
    }

    public void onPlayerInteract(EntityPlayer player, EnumHand hand) {
        tryProcess();
    }

    @Override
    public void doRestrictedTick() {
        if (!world.isRemote && ticksExisted % 20 == 0) {
            tryProcess();
        }
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);
        if (compound.hasKey("inv", Constants.NBT.TAG_COMPOUND)) {
            inventory.readNBT(compound.getCompoundTag("inv"));
            inventory.setListener(this::onInventoryChanged);
        }
        if (compound.hasKey("fluid", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound fluidTag = compound.getCompoundTag("fluid");
            fluid = FluidStack.loadFluidStackFromNBT(fluidTag);
        }
        capacity = compound.hasKey("capacity") ? compound.getInteger("capacity") : DEFAULT_CAPACITY;
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        compound.setTag("inv", inventory.writeNBT());
        if (fluid != null) {
            NBTTagCompound fluidTag = new NBTTagCompound();
            fluid.writeToNBT(fluidTag);
            compound.setTag("fluid", fluidTag);
        }
        compound.setInteger("capacity", capacity);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
            || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
            || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) inventory;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) new ProcessorFluidHandler();
        }
        return super.getCapability(capability, facing);
    }

    @Nonnull
    @Override
    public ReadWriteLock getRWLock() {
        return rwLock;
    }

    private class ProcessorInventory extends IOInventory {
        public ProcessorInventory(hellfirepvp.modularmachinery.common.tiles.base.TileEntitySynchronized owner, int[] inSlots, int[] outSlots) {
            super(owner, inSlots, outSlots);
        }
    }

    private class ProcessorFluidHandler implements net.minecraftforge.fluids.capability.IFluidHandler {
        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount <= 0) {
                return 0;
            }
            if (fluid != null && !fluid.isFluidEqual(resource)) {
                return 0;
            }
            int fillAmount = Math.min(capacity - (fluid == null ? 0 : fluid.amount), resource.amount);
            if (doFill && fillAmount > 0) {
                if (fluid == null) {
                    fluid = resource.copy();
                    fluid.amount = fillAmount;
                } else {
                    fluid.amount += fillAmount;
                }
                markNoUpdateSync();
            }
            return fillAmount;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || fluid == null || !fluid.isFluidEqual(resource)) {
                return null;
            }
            return drain(resource.amount, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (fluid == null || maxDrain <= 0) {
                return null;
            }
            int drained = Math.min(maxDrain, fluid.amount);
            FluidStack ret = fluid.copy();
            ret.amount = drained;
            if (doDrain) {
                fluid.amount -= drained;
                if (fluid.amount <= 0) {
                    fluid = null;
                }
                markNoUpdateSync();
            }
            return ret;
        }

        @Nonnull
        @Override
        public net.minecraftforge.fluids.capability.IFluidTankProperties[] getTankProperties() {
            return new net.minecraftforge.fluids.capability.IFluidTankProperties[0];
        }
    }
}
