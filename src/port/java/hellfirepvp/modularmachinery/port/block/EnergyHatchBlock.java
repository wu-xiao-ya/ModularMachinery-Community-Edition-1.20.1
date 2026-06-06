package hellfirepvp.modularmachinery.port.block;

import hellfirepvp.modularmachinery.port.block.property.EnergyHatchTier;
import hellfirepvp.modularmachinery.port.blockentity.EnergyHatchBlockEntity;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class EnergyHatchBlock extends MachineComponentBlock {
    public static final EnumProperty<EnergyHatchTier> SIZE = EnumProperty.create("size", EnergyHatchTier.class);

    public EnergyHatchBlock(BlockBehaviour.Properties properties, boolean input) {
        super(properties, input ? EnergyHatchBlockEntity::input : EnergyHatchBlockEntity::output);
        registerDefaultState(stateDefinition.any().setValue(SIZE, EnergyHatchTier.NORMAL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof EnergyHatchBlockEntity hatch && hatch.getCapacity() > 0) {
            return (int) Math.min(15L, (hatch.getEnergy() * 15L) / hatch.getCapacity());
        }
        return 0;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof EnergyHatchBlockEntity hatch)) {
            return InteractionResult.PASS;
        }
        if (!player.isShiftKeyDown()) {
            return MmceMachineMenu.open(level, pos, player);
        }
        if (!level.isClientSide()) {
            player.displayClientMessage(Component.literal("Energy: " + hatch.getEnergy() + "/" + hatch.getCapacity() + " FE"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
