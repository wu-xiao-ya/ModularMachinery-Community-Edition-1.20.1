package hellfirepvp.modularmachinery.port.blockentity;

import hellfirepvp.modularmachinery.port.block.ParallelControllerBlock;
import hellfirepvp.modularmachinery.port.block.property.ParallelControllerTier;
import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public final class ParallelControllerBlockEntity extends BaseMachineBlockEntity {
    private int parallelism = -1;

    public ParallelControllerBlockEntity(BlockPos pos, BlockState state) {
        super(MmceBlockEntities.PARALLEL_CONTROLLER.get(), pos, state);
    }

    public ParallelControllerTier getTier() {
        return getBlockState().hasProperty(ParallelControllerBlock.TYPE)
                ? getBlockState().getValue(ParallelControllerBlock.TYPE)
                : ParallelControllerTier.NORMAL;
    }

    public int getMaxParallelism() {
        return getTier().maxParallelism();
    }

    public int getParallelism() {
        if (parallelism < 0) {
            parallelism = getMaxParallelism();
        }
        return Math.min(parallelism, getMaxParallelism());
    }

    public void setParallelism(int parallelism) {
        int clamped = clampParallelism(parallelism);
        if (this.parallelism != clamped) {
            this.parallelism = clamped;
            markForSync();
        }
    }

    public void adjustParallelism(int delta) {
        setParallelism(getParallelism() + delta);
    }

    private int clampParallelism(int value) {
        return Math.max(0, Math.min(value, getMaxParallelism()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        parallelism = tag.contains("parallelism") ? clampParallelism(tag.getInt("parallelism")) : getMaxParallelism();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("maxParallelism", getMaxParallelism());
        tag.putInt("parallelism", getParallelism());
    }
}
