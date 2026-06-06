package hellfirepvp.modularmachinery.port.item;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

public class MmceBlueprintItem extends Item {
    public MmceBlueprintItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        Optional<ResourceLocation> machineId = MmceBlueprintData.getMachineId(stack);
        if (machineId.isEmpty()) {
            tooltipComponents.add(Component.literal("Unbound").withStyle(ChatFormatting.GRAY));
            return;
        }

        MmceDataRegistry.getMachine(machineId.get()).ifPresentOrElse(
                machine -> {
                    tooltipComponents.add(Component.literal(machine.localizedName())
                            .append(Component.literal(" (" + machine.id() + ")").withStyle(ChatFormatting.DARK_GRAY))
                            .withStyle(ChatFormatting.AQUA));
                    tooltipComponents.addAll(MmceMachineSummaryText.tooltipSummary(machine));
                },
                () -> tooltipComponents.add(Component.literal("Missing machine: " + machineId.get()).withStyle(ChatFormatting.RED))
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()
                && context.getPlayer() != null
                && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MachineControllerBlockEntity controller
                && bindController(context.getLevel(), context.getPlayer(), context.getItemInHand(), controller)) {
            return InteractionResult.SUCCESS;
        }
        showBlueprint(context.getLevel(), context.getPlayer(), context.getItemInHand());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        showBlueprint(level, player, stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void showBlueprint(Level level, Player player, ItemStack stack) {
        if (level.isClientSide() || player == null) {
            return;
        }

        Optional<ResourceLocation> machineId = MmceBlueprintData.getMachineId(stack);
        if (machineId.isEmpty()) {
            player.displayClientMessage(Component.literal("Blueprint is not bound to a machine."), false);
            return;
        }

        MmceDataRegistry.getMachine(machineId.get()).ifPresentOrElse(
                machine -> describeMachine(player, machine),
                () -> player.displayClientMessage(Component.literal("Unknown machine: " + machineId.get()), false)
        );
    }

    private static boolean bindController(Level level, Player player, ItemStack stack, MachineControllerBlockEntity controller) {
        Optional<ResourceLocation> machineId = MmceBlueprintData.getMachineId(stack);
        if (machineId.isEmpty()) {
            return false;
        }

        Optional<MmceMachineDefinition> machine = MmceDataRegistry.getMachine(machineId.get());
        if (machine.isEmpty()) {
            player.displayClientMessage(Component.literal("Unknown machine: " + machineId.get()), false);
            return true;
        }

        controller.setMachineId(machineId.get());
        boolean formed = controller.refreshStructure();
        player.displayClientMessage(Component.literal("Blueprint bound controller to "
                + machine.get().localizedName() + " (" + machineId.get() + "), formed=" + formed), false);
        return true;
    }

    private static void describeMachine(Player player, MmceMachineDefinition machine) {
        player.displayClientMessage(Component.literal("Blueprint: " + machine.localizedName() + " (" + machine.id() + ")"), false);
        MmceMachineSummaryText.chatSummary(machine, false).forEach(line -> player.displayClientMessage(line, false));
        if (machine.requiresBlueprint() || machine.hasFactory() || machine.factoryOnly()) {
            player.displayClientMessage(Component.literal("Flags: requiresBlueprint=" + machine.requiresBlueprint()
                    + ", hasFactory=" + machine.hasFactory()
                    + ", factoryOnly=" + machine.factoryOnly()), false);
        }
    }
}
