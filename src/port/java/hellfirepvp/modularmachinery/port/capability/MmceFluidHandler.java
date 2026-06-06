package hellfirepvp.modularmachinery.port.capability;

import hellfirepvp.modularmachinery.port.blockentity.FluidHatchBlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

final class MmceFluidHandler implements IFluidHandler {
    private final FluidHatchBlockEntity hatch;

    MmceFluidHandler(FluidHatchBlockEntity hatch) {
        this.hatch = hatch;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tank == 0 ? hatch.getStoredFluid() : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? hatch.getCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank == 0 && canFill() && !stack.isEmpty();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!canFill() || resource.isEmpty()) {
            return 0;
        }

        FluidStack stored = hatch.getStoredFluid();
        if (!stored.isEmpty() && !FluidStack.isSameFluidSameComponents(stored, resource)) {
            return 0;
        }

        int filled = Math.min(resource.getAmount(), hatch.getCapacity() - stored.getAmount());
        if (filled <= 0) {
            return 0;
        }

        if (action.execute()) {
            hatch.setStoredFluid(stored.isEmpty()
                    ? resource.copyWithAmount(filled)
                    : stored.copyWithAmount(stored.getAmount() + filled));
        }
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }

        FluidStack stored = hatch.getStoredFluid();
        if (stored.isEmpty() || !FluidStack.isSameFluidSameComponents(stored, resource)) {
            return FluidStack.EMPTY;
        }
        return drain(Math.min(resource.getAmount(), stored.getAmount()), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (!canDrain() || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }

        FluidStack stored = hatch.getStoredFluid();
        if (stored.isEmpty()) {
            return FluidStack.EMPTY;
        }

        int drainedAmount = Math.min(maxDrain, stored.getAmount());
        FluidStack drained = stored.copyWithAmount(drainedAmount);
        if (action.execute()) {
            int remaining = stored.getAmount() - drainedAmount;
            hatch.setStoredFluid(remaining <= 0 ? FluidStack.EMPTY : stored.copyWithAmount(remaining));
        }
        return drained;
    }

    private boolean canFill() {
        return hatch.isInput() || hatch.isProcessor();
    }

    private boolean canDrain() {
        return !hatch.isInput() || hatch.isProcessor();
    }
}
