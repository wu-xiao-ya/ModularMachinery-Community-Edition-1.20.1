package hellfirepvp.modularmachinery.port.item;

import hellfirepvp.modularmachinery.port.block.ControllerBlock;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.machine.MmceStructurePreview;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MmceConstructToolItem extends Item {
    public MmceConstructToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }
        if (!canUse(player)) {
            player.displayClientMessage(Component.literal("Construct tool requires creative mode and permission level 2."), false);
            return InteractionResult.FAIL;
        }

        if (level.getBlockEntity(context.getClickedPos()) instanceof MachineControllerBlockEntity controller) {
            return handleController(serverLevel, player, controller);
        }

        MmceStructureSelectionHelper.ToggleResult result = MmceStructureSelectionHelper.toggle(player, context.getClickedPos());
        player.displayClientMessage(Component.literal((result.selected() ? "Selected " : "Deselected ")
                + context.getClickedPos().toShortString()
                + " | total: " + result.totalSelected()), true);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult handleController(ServerLevel level, ServerPlayer player, MachineControllerBlockEntity controller) {
        int selected = MmceStructureSelectionHelper.selectedCount(player);
        if (selected > 0) {
            return exportSelection(level, player, controller);
        }

        Optional<ResourceLocation> blueprintMachine = findHeldBlueprint(player);
        if (blueprintMachine.isPresent()) {
            ResourceLocation machineId = blueprintMachine.get();
            Optional<MmceMachineDefinition> machine = MmceDataRegistry.getMachine(machineId);
            if (machine.isEmpty()) {
                player.displayClientMessage(Component.literal("Blueprint machine is not loaded: " + machineId), false);
                return InteractionResult.FAIL;
            }
            controller.setMachineId(machineId);
            AssemblyResult assembly = assembleCreative(level, controller, machine.get());
            boolean formed = controller.refreshStructure();
            player.displayClientMessage(Component.literal("Controller bound to " + machine.get().localizedName()
                    + " (" + machineId + "), formed=" + formed
                    + assembly.summary()), false);
            return InteractionResult.SUCCESS;
        }

        boolean formed = controller.refreshStructure();
        String machine = controller.getMachineId().map(ResourceLocation::toString).orElse("none");
        if (!formed) {
            Optional<MmceMachineDefinition> definition = controller.getMachineId().flatMap(MmceDataRegistry::getMachine);
            if (definition.isPresent()) {
                AssemblyResult assembly = assembleCreative(level, controller, definition.get());
                formed = controller.refreshStructure();
                player.displayClientMessage(Component.literal("Structure auto-assembly: machine=" + machine
                        + ", formed=" + formed + assembly.summary()), false);
                return InteractionResult.SUCCESS;
            }
        }
        player.displayClientMessage(Component.literal("Structure refresh: machine=" + machine + ", formed=" + formed), false);
        return InteractionResult.SUCCESS;
    }

    private static AssemblyResult assembleCreative(ServerLevel level, MachineControllerBlockEntity controller, MmceMachineDefinition machine) {
        Direction facing = controllerFacing(controller);
        List<MmceStructurePreview.Entry> entries = MmceStructurePreview.build(machine, facing);
        Set<BlockPos> visited = new LinkedHashSet<>();
        int placed = 0;
        int alreadyMatches = 0;
        int blocked = 0;
        int unresolved = 0;
        for (MmceStructurePreview.Entry entry : entries) {
            BlockPos pos = entry.worldPos(controller.getBlockPos());
            if (!visited.add(pos)) {
                continue;
            }
            if (entry.matches(level, controller.getBlockPos())) {
                alreadyMatches++;
                continue;
            }
            Optional<BlockState> preferred = entry.preferredState();
            if (preferred.isEmpty()) {
                unresolved++;
                continue;
            }
            BlockState current = level.getBlockState(pos);
            if (!canReplace(current)) {
                blocked++;
                continue;
            }
            if (level.setBlock(pos, preferred.get(), 3)) {
                placed++;
            } else {
                blocked++;
            }
        }
        return new AssemblyResult(placed, alreadyMatches, blocked, unresolved);
    }

    private static InteractionResult exportSelection(ServerLevel level, ServerPlayer player, MachineControllerBlockEntity controller) {
        BlockState state = controller.getBlockState();
        Direction facing = state.hasProperty(ControllerBlock.FACING) ? state.getValue(ControllerBlock.FACING) : Direction.NORTH;
        try {
            MmceStructureSelectionHelper.ExportResult result = MmceStructureSelectionHelper.export(
                    player,
                    level,
                    controller.getBlockPos(),
                    facing);
            player.displayClientMessage(Component.literal("Exported MMCE machine " + result.machineId()), false);
            player.displayClientMessage(Component.literal("Parts: " + result.partCount() + " | " + result.path()), false);
            player.displayClientMessage(Component.literal("Run /reload to load the exported datapack."), false);
            return InteractionResult.SUCCESS;
        } catch (IllegalStateException ex) {
            player.displayClientMessage(Component.literal(ex.getMessage()), false);
        } catch (IOException ex) {
            player.displayClientMessage(Component.literal("Failed to export machine JSON: " + ex.getMessage()), false);
        }
        return InteractionResult.FAIL;
    }

    private static Optional<ResourceLocation> findHeldBlueprint(ServerPlayer player) {
        Optional<ResourceLocation> mainHand = blueprintMachine(player.getMainHandItem());
        return mainHand.isPresent() ? mainHand : blueprintMachine(player.getOffhandItem());
    }

    private static Optional<ResourceLocation> blueprintMachine(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : MmceBlueprintData.getMachineId(stack);
    }

    private static Direction controllerFacing(MachineControllerBlockEntity controller) {
        BlockState state = controller.getBlockState();
        return state.hasProperty(ControllerBlock.FACING) ? state.getValue(ControllerBlock.FACING) : Direction.NORTH;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }

    private static boolean canUse(ServerPlayer player) {
        return player.isCreative() && player.hasPermissions(2);
    }

    private record AssemblyResult(int placed, int alreadyMatches, int blocked, int unresolved) {
        private String summary() {
            return ", placed=" + placed
                    + ", existing=" + alreadyMatches
                    + ", blocked=" + blocked
                    + ", unresolved=" + unresolved;
        }
    }
}
