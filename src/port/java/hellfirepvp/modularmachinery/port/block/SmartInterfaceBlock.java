package hellfirepvp.modularmachinery.port.block;

import hellfirepvp.modularmachinery.port.block.property.SmartInterfaceMode;
import hellfirepvp.modularmachinery.port.blockentity.SmartInterfaceBlockEntity;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class SmartInterfaceBlock extends MachineComponentBlock {
    public static final EnumProperty<SmartInterfaceMode> TYPE = EnumProperty.create("type", SmartInterfaceMode.class);

    public SmartInterfaceBlock(BlockBehaviour.Properties properties) {
        super(properties, SmartInterfaceBlockEntity::new);
        registerDefaultState(stateDefinition.any().setValue(TYPE, SmartInterfaceMode.NUMBER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return MmceMachineMenu.open(level, pos, player);
    }
}
