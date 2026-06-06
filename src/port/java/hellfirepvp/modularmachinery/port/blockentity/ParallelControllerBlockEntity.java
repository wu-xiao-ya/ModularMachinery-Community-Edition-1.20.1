package hellfirepvp.modularmachinery.port.blockentity;

import hellfirepvp.modularmachinery.port.block.ParallelControllerBlock;
import hellfirepvp.modularmachinery.port.block.property.ParallelControllerTier;
import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class ParallelControllerBlockEntity extends BaseMachineBlockEntity {
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
}
