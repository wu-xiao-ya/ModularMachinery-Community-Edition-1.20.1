package hellfirepvp.modularmachinery.port.registry;

import hellfirepvp.modularmachinery.port.block.CasingBlock;
import hellfirepvp.modularmachinery.port.block.EnergyHatchBlock;
import hellfirepvp.modularmachinery.port.block.FluidHatchBlock;
import hellfirepvp.modularmachinery.port.block.ItemBusBlock;
import hellfirepvp.modularmachinery.port.block.ParallelControllerBlock;
import hellfirepvp.modularmachinery.port.block.SmartInterfaceBlock;
import hellfirepvp.modularmachinery.port.block.UpgradeBusBlock;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.item.MmceBlueprintData;
import hellfirepvp.modularmachinery.port.item.MmceBlueprintItem;
import hellfirepvp.modularmachinery.port.item.MmceConstructToolItem;
import hellfirepvp.modularmachinery.port.item.MmceMachineProjectorItem;
import hellfirepvp.modularmachinery.port.item.StatefulBlockItem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MmceItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModularMachineryNeoForge.MODID);

    public static final DeferredItem<BlockItem> BLOCK_CONTROLLER = blockItem(MmceBlocks.BLOCK_CONTROLLER);
    public static final DeferredItem<BlockItem> BLOCK_FACTORY_CONTROLLER = blockItem(MmceBlocks.BLOCK_FACTORY_CONTROLLER);
    public static final DeferredItem<StatefulBlockItem<?>> BLOCK_CASING = statefulBlockItem(MmceBlocks.BLOCK_CASING, CasingBlock.CASING);
    public static final DeferredItem<StatefulBlockItem<?>> ITEM_INPUT_BUS = statefulBlockItem(MmceBlocks.ITEM_INPUT_BUS, ItemBusBlock.SIZE);
    public static final DeferredItem<StatefulBlockItem<?>> ITEM_OUTPUT_BUS = statefulBlockItem(MmceBlocks.ITEM_OUTPUT_BUS, ItemBusBlock.SIZE);
    public static final DeferredItem<StatefulBlockItem<?>> FLUID_INPUT_HATCH = statefulBlockItem(MmceBlocks.FLUID_INPUT_HATCH, FluidHatchBlock.SIZE);
    public static final DeferredItem<StatefulBlockItem<?>> FLUID_OUTPUT_HATCH = statefulBlockItem(MmceBlocks.FLUID_OUTPUT_HATCH, FluidHatchBlock.SIZE);
    public static final DeferredItem<StatefulBlockItem<?>> FLUID_PROCESSOR_HATCH = statefulBlockItem(MmceBlocks.FLUID_PROCESSOR_HATCH, FluidHatchBlock.SIZE);
    public static final DeferredItem<StatefulBlockItem<?>> ENERGY_INPUT_HATCH = statefulBlockItem(MmceBlocks.ENERGY_INPUT_HATCH, EnergyHatchBlock.SIZE);
    public static final DeferredItem<StatefulBlockItem<?>> ENERGY_OUTPUT_HATCH = statefulBlockItem(MmceBlocks.ENERGY_OUTPUT_HATCH, EnergyHatchBlock.SIZE);
    public static final DeferredItem<StatefulBlockItem<?>> SMART_INTERFACE = statefulBlockItem(MmceBlocks.SMART_INTERFACE, SmartInterfaceBlock.TYPE);
    public static final DeferredItem<StatefulBlockItem<?>> PARALLEL_CONTROLLER = statefulBlockItem(MmceBlocks.PARALLEL_CONTROLLER, ParallelControllerBlock.TYPE);
    public static final DeferredItem<StatefulBlockItem<?>> UPGRADE_BUS = statefulBlockItem(MmceBlocks.UPGRADE_BUS, UpgradeBusBlock.TYPE);

    public static final DeferredItem<MmceBlueprintItem> BLUEPRINT = ITEMS.register("itemblueprint",
            () -> new MmceBlueprintItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> MODULARIUM = item("itemmodularium");
    public static final DeferredItem<MmceConstructToolItem> CONSTRUCT_TOOL = ITEMS.register("itemconstructtool",
            () -> new MmceConstructToolItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<MmceMachineProjectorItem> MACHINE_PROJECTOR = ITEMS.register("machine_projector",
            () -> new MmceMachineProjectorItem(new Item.Properties().stacksTo(1)));

    public static final List<DeferredItem<? extends Item>> CREATIVE_TAB_ITEMS = List.of(
            BLOCK_CONTROLLER,
            BLOCK_FACTORY_CONTROLLER,
            BLOCK_CASING,
            ITEM_INPUT_BUS,
            ITEM_OUTPUT_BUS,
            FLUID_INPUT_HATCH,
            FLUID_OUTPUT_HATCH,
            FLUID_PROCESSOR_HATCH,
            ENERGY_INPUT_HATCH,
            ENERGY_OUTPUT_HATCH,
            SMART_INTERFACE,
            PARALLEL_CONTROLLER,
            UPGRADE_BUS,
            BLUEPRINT,
            MODULARIUM,
            CONSTRUCT_TOOL,
            MACHINE_PROJECTOR
    );

    private MmceItems() {
    }

    private static DeferredItem<Item> item(String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties()));
    }

    private static <T extends Block> DeferredItem<BlockItem> blockItem(DeferredBlock<T> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <B extends Block, T extends Comparable<T>> DeferredItem<StatefulBlockItem<?>> statefulBlockItem(
            DeferredBlock<B> block,
            net.minecraft.world.level.block.state.properties.Property<T> property
    ) {
        return ITEMS.register(block.getId().getPath(), () -> new StatefulBlockItem<>(block.get(), new Item.Properties(), property));
    }

    public static List<ItemStack> creativeTabStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        CREATIVE_TAB_ITEMS.forEach(item -> StatefulBlockItem.acceptCreativeStacks(stacks, item.get()));
        MmceDataRegistry.snapshot().machines().values().stream()
                .sorted(Comparator.comparing(machine -> machine.id().toString()))
                .map(machine -> MmceBlueprintData.stackFor(BLUEPRINT.get(), machine.id()))
                .forEach(stacks::add);
        return stacks;
    }
}
