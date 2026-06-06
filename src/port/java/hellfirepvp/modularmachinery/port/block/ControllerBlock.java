package hellfirepvp.modularmachinery.port.block;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceRecipeDefinition;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class ControllerBlock extends MachineComponentBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public ControllerBlock(BlockBehaviour.Properties properties,
                           BiFunction<BlockPos, BlockState, BlockEntity> blockEntityFactory) {
        super(properties, blockEntityFactory);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FORMED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MachineControllerBlockEntity controller) {
            if (controller.isWorking()) {
                return 15;
            }
            return controller.isStructureFormed() ? 1 : 0;
        }
        return 0;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) -> {
            if (blockEntity instanceof MachineControllerBlockEntity controller) {
                controller.serverTick();
            }
        };
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof MachineControllerBlockEntity controller) {
            controller.getOwner().ifPresent(owner -> {
                CompoundTag tag = new CompoundTag();
                tag.putUUID("owner", owner);
                stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
            });
        }
        return stack;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MachineControllerBlockEntity controller) {
            controller.refreshStructure();
        }
        return MmceMachineMenu.open(level, pos, player);
    }

    private static void showStatus(MachineControllerBlockEntity controller, Player player) {
        String machine = controller.getMachineId().map(ResourceLocation::toString).orElse("none");
        String recipe = controller.getActiveRecipeId().map(ResourceLocation::toString).orElse("none");
        String progress = formatProgress(controller);
        String detail = controller.getRecipeStatusDetail();
        String suffix = detail.isBlank() ? "" : " | " + detail;
        player.displayClientMessage(Component.literal("Machine: " + machine + " | Status: "
                + controller.getRecipeStatus().displayName() + suffix), false);
        player.displayClientMessage(Component.literal("Recipe: " + recipe + " | Progress: " + progress), false);
    }

    private static String formatProgress(MachineControllerBlockEntity controller) {
        int progress = controller.getRecipeProgress();
        return controller.getActiveRecipeId()
                .map(MmceDataRegistry.snapshot().recipes()::get)
                .map(MmceRecipeDefinition::recipeTime)
                .map(total -> progress + "/" + Math.max(1, total) + "t")
                .orElse(progress + "t");
    }
}
