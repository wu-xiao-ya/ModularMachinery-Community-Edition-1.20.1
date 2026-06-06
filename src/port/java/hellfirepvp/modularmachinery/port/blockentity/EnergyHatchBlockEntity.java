package hellfirepvp.modularmachinery.port.blockentity;

import hellfirepvp.modularmachinery.port.block.EnergyHatchBlock;
import hellfirepvp.modularmachinery.port.block.property.EnergyHatchTier;
import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EnergyHatchBlockEntity extends BaseMachineBlockEntity {
    private final boolean input;
    private long energy;

    public static EnergyHatchBlockEntity input(BlockPos pos, BlockState state) {
        return new EnergyHatchBlockEntity(MmceBlockEntities.ENERGY_INPUT_HATCH.get(), pos, state, true);
    }

    public static EnergyHatchBlockEntity output(BlockPos pos, BlockState state) {
        return new EnergyHatchBlockEntity(MmceBlockEntities.ENERGY_OUTPUT_HATCH.get(), pos, state, false);
    }

    private EnergyHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, boolean input) {
        super(type, pos, state);
        this.input = input;
    }

    public boolean isInput() {
        return input;
    }

    public EnergyHatchTier getTier() {
        return getBlockState().hasProperty(EnergyHatchBlock.SIZE)
                ? getBlockState().getValue(EnergyHatchBlock.SIZE)
                : EnergyHatchTier.NORMAL;
    }

    public long getCapacity() {
        return getTier().capacity();
    }

    public long getTransferLimit() {
        return getTier().transferLimit();
    }

    public long getEnergy() {
        return energy;
    }

    public void setEnergy(long energy) {
        long clamped = Math.max(0L, Math.min(energy, getCapacity()));
        if (this.energy != clamped) {
            this.energy = clamped;
            markForSync();
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getLong("energy");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("energy", energy);
    }
}
