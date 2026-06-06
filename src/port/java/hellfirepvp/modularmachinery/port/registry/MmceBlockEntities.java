package hellfirepvp.modularmachinery.port.registry;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.blockentity.BaseMachineBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.ColorableMachineBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.EnergyHatchBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.FactoryControllerBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.FluidHatchBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.ItemBusBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.ParallelControllerBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.SmartInterfaceBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.UpgradeBusBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MmceBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModularMachineryNeoForge.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ColorableMachineBlockEntity>> COLORABLE_COMPONENT =
            BLOCK_ENTITIES.register("tilecolorablemachinecomponent",
                    () -> BlockEntityType.Builder.of(ColorableMachineBlockEntity::new, MmceBlocks.BLOCK_CASING.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineControllerBlockEntity>> MACHINE_CONTROLLER =
            BLOCK_ENTITIES.register("tilemachinecontroller",
                    () -> BlockEntityType.Builder.of(MachineControllerBlockEntity::new, MmceBlocks.BLOCK_CONTROLLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FactoryControllerBlockEntity>> FACTORY_CONTROLLER =
            BLOCK_ENTITIES.register("tilefactorycontroller",
                    () -> BlockEntityType.Builder.of(FactoryControllerBlockEntity::new, MmceBlocks.BLOCK_FACTORY_CONTROLLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemBusBlockEntity>> ITEM_INPUT_BUS =
            BLOCK_ENTITIES.register("tileiteminputbus",
                    () -> BlockEntityType.Builder.of(ItemBusBlockEntity::input, MmceBlocks.ITEM_INPUT_BUS.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemBusBlockEntity>> ITEM_OUTPUT_BUS =
            BLOCK_ENTITIES.register("tileitemoutputbus",
                    () -> BlockEntityType.Builder.of(ItemBusBlockEntity::output, MmceBlocks.ITEM_OUTPUT_BUS.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidHatchBlockEntity>> FLUID_INPUT_HATCH =
            BLOCK_ENTITIES.register("tilefluidinputhatch",
                    () -> BlockEntityType.Builder.of(FluidHatchBlockEntity::input, MmceBlocks.FLUID_INPUT_HATCH.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidHatchBlockEntity>> FLUID_OUTPUT_HATCH =
            BLOCK_ENTITIES.register("tilefluidoutputhatch",
                    () -> BlockEntityType.Builder.of(FluidHatchBlockEntity::output, MmceBlocks.FLUID_OUTPUT_HATCH.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidHatchBlockEntity>> FLUID_PROCESSOR_HATCH =
            BLOCK_ENTITIES.register("tilefluidprocessorhatch",
                    () -> BlockEntityType.Builder.of(FluidHatchBlockEntity::processor, MmceBlocks.FLUID_PROCESSOR_HATCH.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyHatchBlockEntity>> ENERGY_INPUT_HATCH =
            BLOCK_ENTITIES.register("tileenergyinputhatch",
                    () -> BlockEntityType.Builder.of(EnergyHatchBlockEntity::input, MmceBlocks.ENERGY_INPUT_HATCH.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyHatchBlockEntity>> ENERGY_OUTPUT_HATCH =
            BLOCK_ENTITIES.register("tileenergyoutputhatch",
                    () -> BlockEntityType.Builder.of(EnergyHatchBlockEntity::output, MmceBlocks.ENERGY_OUTPUT_HATCH.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SmartInterfaceBlockEntity>> SMART_INTERFACE =
            BLOCK_ENTITIES.register("tilesmartinterface",
                    () -> BlockEntityType.Builder.of(SmartInterfaceBlockEntity::new, MmceBlocks.SMART_INTERFACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ParallelControllerBlockEntity>> PARALLEL_CONTROLLER =
            BLOCK_ENTITIES.register("tileparallelcontroller",
                    () -> BlockEntityType.Builder.of(ParallelControllerBlockEntity::new, MmceBlocks.PARALLEL_CONTROLLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UpgradeBusBlockEntity>> UPGRADE_BUS =
            BLOCK_ENTITIES.register("tileupgradebus",
                    () -> BlockEntityType.Builder.of(UpgradeBusBlockEntity::new, MmceBlocks.UPGRADE_BUS.get()).build(null));

    private MmceBlockEntities() {
    }
}
