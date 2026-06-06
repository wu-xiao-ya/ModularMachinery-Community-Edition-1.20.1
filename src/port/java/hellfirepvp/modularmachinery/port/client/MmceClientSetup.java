package hellfirepvp.modularmachinery.port.client;

import hellfirepvp.modularmachinery.port.blockentity.BaseMachineBlockEntity;
import hellfirepvp.modularmachinery.port.registry.MmceBlocks;
import hellfirepvp.modularmachinery.port.registry.MmceItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

public final class MmceClientSetup {
    private static final int DEFAULT_MACHINE_COLOR = BaseMachineBlockEntity.DEFAULT_MACHINE_COLOR;

    private MmceClientSetup() {
    }

    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
                    if (tintIndex < 0) {
                        return DEFAULT_MACHINE_COLOR;
                    }
                    if (level != null && pos != null) {
                        BlockEntity blockEntity = level.getBlockEntity(pos);
                        if (blockEntity instanceof BaseMachineBlockEntity machineBlockEntity) {
                            return machineBlockEntity.getMachineColor();
                        }
                    }
                    return DEFAULT_MACHINE_COLOR;
                },
                colorableBlocks());
    }

    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> DEFAULT_MACHINE_COLOR,
                MmceItems.BLOCK_CONTROLLER.get(),
                MmceItems.BLOCK_FACTORY_CONTROLLER.get(),
                MmceItems.BLOCK_CASING.get(),
                MmceItems.ITEM_INPUT_BUS.get(),
                MmceItems.ITEM_OUTPUT_BUS.get(),
                MmceItems.FLUID_INPUT_HATCH.get(),
                MmceItems.FLUID_OUTPUT_HATCH.get(),
                MmceItems.FLUID_PROCESSOR_HATCH.get(),
                MmceItems.ENERGY_INPUT_HATCH.get(),
                MmceItems.ENERGY_OUTPUT_HATCH.get(),
                MmceItems.SMART_INTERFACE.get(),
                MmceItems.PARALLEL_CONTROLLER.get(),
                MmceItems.UPGRADE_BUS.get());
    }

    private static Block[] colorableBlocks() {
        return new Block[]{
                MmceBlocks.BLOCK_CONTROLLER.get(),
                MmceBlocks.BLOCK_FACTORY_CONTROLLER.get(),
                MmceBlocks.BLOCK_CASING.get(),
                MmceBlocks.ITEM_INPUT_BUS.get(),
                MmceBlocks.ITEM_OUTPUT_BUS.get(),
                MmceBlocks.FLUID_INPUT_HATCH.get(),
                MmceBlocks.FLUID_OUTPUT_HATCH.get(),
                MmceBlocks.FLUID_PROCESSOR_HATCH.get(),
                MmceBlocks.ENERGY_INPUT_HATCH.get(),
                MmceBlocks.ENERGY_OUTPUT_HATCH.get(),
                MmceBlocks.SMART_INTERFACE.get(),
                MmceBlocks.PARALLEL_CONTROLLER.get(),
                MmceBlocks.UPGRADE_BUS.get()
        };
    }
}
