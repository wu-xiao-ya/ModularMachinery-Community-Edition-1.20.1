package hellfirepvp.modularmachinery.port.block;

import hellfirepvp.modularmachinery.port.block.property.FluidHatchSize;
import hellfirepvp.modularmachinery.port.blockentity.FluidHatchBlockEntity;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public final class FluidHatchBlock extends MachineComponentBlock {
    public static final EnumProperty<FluidHatchSize> SIZE = EnumProperty.create("size", FluidHatchSize.class);

    public FluidHatchBlock(BlockBehaviour.Properties properties, Kind kind) {
        super(properties, switch (kind) {
            case INPUT -> FluidHatchBlockEntity::input;
            case OUTPUT -> FluidHatchBlockEntity::output;
            case PROCESSOR -> FluidHatchBlockEntity::processor;
        });
        registerDefaultState(stateDefinition.any().setValue(SIZE, FluidHatchSize.NORMAL));
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
        if (level.getBlockEntity(pos) instanceof FluidHatchBlockEntity hatch && hatch.getCapacity() > 0) {
            long stored = Math.max(hatch.getStoredAmount(), hatch.getStoredChemicalAmount());
            return (int) Math.min(15L, (stored * 15L) / hatch.getCapacity());
        }
        return 0;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty() || FluidUtil.getFluidHandler(stack).isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide()) {
            if (!FluidUtil.interactWithFluidHandler(player, hand, level, pos, hitResult.getDirection())) {
                showStatus(level, pos, player);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof FluidHatchBlockEntity)) {
            return InteractionResult.PASS;
        }
        if (!player.isShiftKeyDown()) {
            return MmceMachineMenu.open(level, pos, player);
        }
        if (!level.isClientSide()) {
            showStatus(level, pos, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void showStatus(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof FluidHatchBlockEntity hatch) {
            FluidStack fluid = hatch.getStoredFluid();
            String fluidName = fluid.isEmpty() ? "empty" : fluid.getHoverName().getString();
            String chemicalName = hatch.hasStoredChemical() ? hatch.getStoredChemicalId().toString() : "empty";
            player.displayClientMessage(Component.literal("Fluid: " + fluidName + " " + fluid.getAmount() + "/" + hatch.getCapacity()
                    + " mB | Chemical: " + chemicalName + " " + hatch.getStoredChemicalAmount() + "/" + hatch.getCapacity()), true);
        }
    }

    public enum Kind {
        INPUT,
        OUTPUT,
        PROCESSOR
    }
}
