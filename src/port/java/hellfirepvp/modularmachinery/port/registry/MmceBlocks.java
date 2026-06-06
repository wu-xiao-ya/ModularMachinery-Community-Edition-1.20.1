package hellfirepvp.modularmachinery.port.registry;

import hellfirepvp.modularmachinery.port.block.CasingBlock;
import hellfirepvp.modularmachinery.port.block.ControllerBlock;
import hellfirepvp.modularmachinery.port.block.EnergyHatchBlock;
import hellfirepvp.modularmachinery.port.block.FluidHatchBlock;
import hellfirepvp.modularmachinery.port.block.ItemBusBlock;
import hellfirepvp.modularmachinery.port.block.ParallelControllerBlock;
import hellfirepvp.modularmachinery.port.block.SmartInterfaceBlock;
import hellfirepvp.modularmachinery.port.block.UpgradeBusBlock;
import hellfirepvp.modularmachinery.port.blockentity.FactoryControllerBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MmceBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModularMachineryNeoForge.MODID);

    public static final DeferredBlock<ControllerBlock> BLOCK_CONTROLLER = BLOCKS.register("blockcontroller",
            () -> new ControllerBlock(controllerProperties(), MachineControllerBlockEntity::new));
    public static final DeferredBlock<ControllerBlock> BLOCK_FACTORY_CONTROLLER = BLOCKS.register("blockfactorycontroller",
            () -> new ControllerBlock(controllerProperties(), FactoryControllerBlockEntity::new));
    public static final DeferredBlock<CasingBlock> BLOCK_CASING = BLOCKS.register("blockcasing",
            () -> new CasingBlock(componentProperties()));
    public static final DeferredBlock<ItemBusBlock> ITEM_INPUT_BUS = BLOCKS.register("blockinputbus",
            () -> new ItemBusBlock(componentProperties(), true));
    public static final DeferredBlock<ItemBusBlock> ITEM_OUTPUT_BUS = BLOCKS.register("blockoutputbus",
            () -> new ItemBusBlock(componentProperties(), false));
    public static final DeferredBlock<FluidHatchBlock> FLUID_INPUT_HATCH = BLOCKS.register("blockfluidinputhatch",
            () -> new FluidHatchBlock(componentProperties(), FluidHatchBlock.Kind.INPUT));
    public static final DeferredBlock<FluidHatchBlock> FLUID_OUTPUT_HATCH = BLOCKS.register("blockfluidoutputhatch",
            () -> new FluidHatchBlock(componentProperties(), FluidHatchBlock.Kind.OUTPUT));
    public static final DeferredBlock<FluidHatchBlock> FLUID_PROCESSOR_HATCH = BLOCKS.register("blockfluidprocessorhatch",
            () -> new FluidHatchBlock(componentProperties(), FluidHatchBlock.Kind.PROCESSOR));
    public static final DeferredBlock<EnergyHatchBlock> ENERGY_INPUT_HATCH = BLOCKS.register("blockenergyinputhatch",
            () -> new EnergyHatchBlock(componentProperties(), true));
    public static final DeferredBlock<EnergyHatchBlock> ENERGY_OUTPUT_HATCH = BLOCKS.register("blockenergyoutputhatch",
            () -> new EnergyHatchBlock(componentProperties(), false));
    public static final DeferredBlock<SmartInterfaceBlock> SMART_INTERFACE = BLOCKS.register("blocksmartinterface",
            () -> new SmartInterfaceBlock(controllerProperties()));
    public static final DeferredBlock<ParallelControllerBlock> PARALLEL_CONTROLLER = BLOCKS.register("blockparallelcontroller",
            () -> new ParallelControllerBlock(controllerProperties()));
    public static final DeferredBlock<UpgradeBusBlock> UPGRADE_BUS = BLOCKS.register("blockupgradebus",
            () -> new UpgradeBusBlock(controllerProperties()));

    private MmceBlocks() {
    }

    private static BlockBehaviour.Properties componentProperties() {
        return BlockBehaviour.Properties.of()
                .strength(2.0F, 6.0F)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties controllerProperties() {
        return BlockBehaviour.Properties.of()
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops();
    }
}
