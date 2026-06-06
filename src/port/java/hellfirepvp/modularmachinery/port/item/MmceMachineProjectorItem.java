package hellfirepvp.modularmachinery.port.item;

import hellfirepvp.modularmachinery.port.block.ControllerBlock;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.machine.MmceStructureMatcher;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MmceMachineProjectorItem extends Item {
    public MmceMachineProjectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            Optional<ResourceLocation> target = findTargetMachine(context.getItemInHand(), player, level, context.getClickedPos());
            if (target.isEmpty() && level.getBlockEntity(context.getClickedPos()) instanceof MachineControllerBlockEntity controller) {
                controller.refreshStructure();
                target = controller.getMachineId();
            }
            target.ifPresent(machineId -> MmceBlueprintData.setMachineId(context.getItemInHand(), machineId));
            showProjection(level, player, context.getClickedPos(), target);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide()) {
            Optional<ResourceLocation> target = findHeldBlueprint(player);
            target.ifPresent(machineId -> MmceBlueprintData.setMachineId(stack, machineId));
            showProjection(level, player, null, target);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static Optional<ResourceLocation> findTargetMachine(ItemStack projector, Player player, Level level, net.minecraft.core.BlockPos pos) {
        Optional<ResourceLocation> heldBlueprint = findHeldBlueprint(player);
        if (heldBlueprint.isPresent()) {
            return heldBlueprint;
        }
        if (level.getBlockEntity(pos) instanceof MachineControllerBlockEntity controller) {
            return controller.getMachineId();
        }
        return MmceBlueprintData.getMachineId(projector);
    }

    private static Optional<ResourceLocation> findHeldBlueprint(Player player) {
        Optional<ResourceLocation> mainHand = MmceBlueprintData.getMachineId(player.getMainHandItem());
        return mainHand.isPresent() ? mainHand : MmceBlueprintData.getMachineId(player.getOffhandItem());
    }

    private static void showProjection(Level level, Player player, net.minecraft.core.BlockPos anchor, Optional<ResourceLocation> target) {
        if (target.isEmpty()) {
            player.displayClientMessage(Component.literal("Machine projector needs a bound blueprint or a controller with a machine."), false);
            return;
        }

        MmceDataRegistry.getMachine(target.get()).ifPresentOrElse(machine -> {
            player.displayClientMessage(Component.literal("Projection: " + machine.localizedName() + " (" + machine.id() + ")"), false);
            MmceMachineSummaryText.chatSummary(machine, true).forEach(line -> player.displayClientMessage(line, false));
            if (anchor != null && level.getBlockEntity(anchor) instanceof MachineControllerBlockEntity controller) {
                boolean matches = matchesController(level, controller, machine);
                player.displayClientMessage(Component.literal("Clicked controller match: " + matches), false);
            }
        }, () -> player.displayClientMessage(Component.literal("Unknown machine: " + target.get()), false));
    }

    private static boolean matchesController(Level level, MachineControllerBlockEntity controller, MmceMachineDefinition machine) {
        BlockState state = controller.getBlockState();
        Direction facing = state.hasProperty(ControllerBlock.FACING) ? state.getValue(ControllerBlock.FACING) : Direction.NORTH;
        return MmceStructureMatcher.matches(level, controller.getBlockPos(), facing, machine);
    }
}
