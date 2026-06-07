package hellfirepvp.modularmachinery.port.block;

import hellfirepvp.modularmachinery.port.blockentity.BaseMachineBlockEntity;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MachineComponentBlock extends Block implements EntityBlock {
    private final BiFunction<BlockPos, BlockState, BlockEntity> blockEntityFactory;

    protected MachineComponentBlock(BlockBehaviour.Properties properties,
                                    BiFunction<BlockPos, BlockState, BlockEntity> blockEntityFactory) {
        super(properties);
        this.blockEntityFactory = blockEntityFactory;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityFactory.apply(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    protected static boolean hasEmptyHands(Player player) {
        return player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty();
    }

    protected static InteractionResult tryOpenGroupInputConfig(Level level, BlockPos pos, Player player) {
        if (hasEmptyHands(player)
                && level.getBlockEntity(pos) instanceof BaseMachineBlockEntity blockEntity
                && blockEntity.canConfigureGroupInput()) {
            return MmceMachineMenu.open(level, pos, player);
        }
        return InteractionResult.PASS;
    }
}
