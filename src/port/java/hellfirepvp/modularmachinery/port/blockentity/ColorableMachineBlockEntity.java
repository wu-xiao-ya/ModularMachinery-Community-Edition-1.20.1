package hellfirepvp.modularmachinery.port.blockentity;

import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class ColorableMachineBlockEntity extends BaseMachineBlockEntity {
    public ColorableMachineBlockEntity(BlockPos pos, BlockState state) {
        super(MmceBlockEntities.COLORABLE_COMPONENT.get(), pos, state);
    }
}
