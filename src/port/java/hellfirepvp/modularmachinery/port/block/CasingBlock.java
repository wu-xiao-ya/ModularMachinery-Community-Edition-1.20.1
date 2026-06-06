package hellfirepvp.modularmachinery.port.block;

import hellfirepvp.modularmachinery.port.block.property.CasingType;
import hellfirepvp.modularmachinery.port.blockentity.ColorableMachineBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public final class CasingBlock extends MachineComponentBlock {
    public static final EnumProperty<CasingType> CASING = EnumProperty.create("casing", CasingType.class);

    public CasingBlock(BlockBehaviour.Properties properties) {
        super(properties, ColorableMachineBlockEntity::new);
        registerDefaultState(stateDefinition.any().setValue(CASING, CasingType.PLAIN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(CASING);
    }
}
