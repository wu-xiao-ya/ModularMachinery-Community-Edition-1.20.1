package hellfirepvp.modularmachinery.port.capability;

import hellfirepvp.modularmachinery.port.blockentity.EnergyHatchBlockEntity;
import net.neoforged.neoforge.energy.IEnergyStorage;

final class MmceEnergyStorage implements IEnergyStorage {
    private final EnergyHatchBlockEntity hatch;

    MmceEnergyStorage(EnergyHatchBlockEntity hatch) {
        this.hatch = hatch;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (!canReceive() || toReceive <= 0) {
            return 0;
        }

        long accepted = Math.min(toReceive, hatch.getCapacity() - hatch.getEnergy());
        accepted = Math.min(accepted, hatch.getTransferLimit());
        if (accepted <= 0) {
            return 0;
        }

        if (!simulate) {
            hatch.setEnergy(hatch.getEnergy() + accepted);
        }
        return clampToInt(accepted);
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (!canExtract() || toExtract <= 0) {
            return 0;
        }

        long extracted = Math.min(toExtract, hatch.getEnergy());
        extracted = Math.min(extracted, hatch.getTransferLimit());
        if (extracted <= 0) {
            return 0;
        }

        if (!simulate) {
            hatch.setEnergy(hatch.getEnergy() - extracted);
        }
        return clampToInt(extracted);
    }

    @Override
    public int getEnergyStored() {
        return clampToInt(hatch.getEnergy());
    }

    @Override
    public int getMaxEnergyStored() {
        return clampToInt(hatch.getCapacity());
    }

    @Override
    public boolean canExtract() {
        return !hatch.isInput();
    }

    @Override
    public boolean canReceive() {
        return hatch.isInput();
    }

    private static int clampToInt(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, value));
    }
}
