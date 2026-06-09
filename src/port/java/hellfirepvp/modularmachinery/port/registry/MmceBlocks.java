package hellfirepvp.modularmachinery.port.registry;

import hellfirepvp.modularmachinery.port.block.CasingBlock;
import hellfirepvp.modularmachinery.port.block.ControllerBlock;
import hellfirepvp.modularmachinery.port.block.EnergyHatchBlock;
import hellfirepvp.modularmachinery.port.block.FluidHatchBlock;
import hellfirepvp.modularmachinery.port.block.ItemBusBlock;
import hellfirepvp.modularmachinery.port.block.ParallelControllerBlock;
import hellfirepvp.modularmachinery.port.block.ProviderBlock;
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

    public static final DeferredBlock<ProviderBlock> LIFE_ESSENCE_PROVIDER_INPUT = provider("blocklifeessenceproviderinput");
    public static final DeferredBlock<ProviderBlock> LIFE_ESSENCE_PROVIDER_OUTPUT = provider("blocklifeessenceprovideroutput");
    public static final DeferredBlock<ProviderBlock> WILL_PROVIDER_INPUT = provider("blockwillproviderinput");
    public static final DeferredBlock<ProviderBlock> WILL_PROVIDER_OUTPUT = provider("blockwillprovideroutput");
    public static final DeferredBlock<ProviderBlock> MANA_PROVIDER_INPUT = provider("blockmanaproviderinput");
    public static final DeferredBlock<ProviderBlock> MANA_PROVIDER_OUTPUT = provider("blockmanaprovideroutput");
    public static final DeferredBlock<ProviderBlock> STARLIGHT_PROVIDER_INPUT = provider("blockstarlightproviderinput");
    public static final DeferredBlock<ProviderBlock> STARLIGHT_PROVIDER_OUTPUT = provider("blockstarlightprovideroutput");
    public static final DeferredBlock<ProviderBlock> GRID_PROVIDER_INPUT = provider("blockgridproviderinput");
    public static final DeferredBlock<ProviderBlock> GRID_PROVIDER_OUTPUT = provider("blockgridprovideroutput");
    public static final DeferredBlock<ProviderBlock> AURA_PROVIDER_INPUT = provider("blockauraproviderinput");
    public static final DeferredBlock<ProviderBlock> AURA_PROVIDER_OUTPUT = provider("blockauraprovideroutput");
    public static final DeferredBlock<ProviderBlock> ASPECT_PROVIDER_INPUT = provider("blockaspectproviderinput");
    public static final DeferredBlock<ProviderBlock> ASPECT_PROVIDER_OUTPUT = provider("blockaspectprovideroutput");
    public static final DeferredBlock<ProviderBlock> CONSTELLATION_PROVIDER = provider("blockconstellationprovider");
    public static final DeferredBlock<ProviderBlock> RAINBOW_PROVIDER = provider("blockrainbowprovider");
    public static final DeferredBlock<ProviderBlock> ME_PATTERN_PROVIDER = provider("blockmepatternprovider");

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

    private static DeferredBlock<ProviderBlock> provider(String id) {
        return BLOCKS.register(id, () -> new ProviderBlock(componentProperties()));
    }
}
