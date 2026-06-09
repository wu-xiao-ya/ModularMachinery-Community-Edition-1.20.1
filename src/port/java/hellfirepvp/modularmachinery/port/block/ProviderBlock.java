package hellfirepvp.modularmachinery.port.block;

import hellfirepvp.modularmachinery.port.blockentity.ColorableMachineBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ProviderBlock extends MachineComponentBlock {
    public ProviderBlock(BlockBehaviour.Properties properties) {
        super(properties, ColorableMachineBlockEntity::new);
    }
}
