package hellfirepvp.modularmachinery.port.client;

import hellfirepvp.modularmachinery.port.blockentity.BaseMachineBlockEntity;
import hellfirepvp.modularmachinery.port.block.CasingBlock;
import hellfirepvp.modularmachinery.port.block.EnergyHatchBlock;
import hellfirepvp.modularmachinery.port.block.FluidHatchBlock;
import hellfirepvp.modularmachinery.port.block.ItemBusBlock;
import hellfirepvp.modularmachinery.port.block.ParallelControllerBlock;
import hellfirepvp.modularmachinery.port.block.SmartInterfaceBlock;
import hellfirepvp.modularmachinery.port.block.UpgradeBusBlock;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.client.gui.MmceBlueprintScreen;
import hellfirepvp.modularmachinery.port.item.MmceBlueprintScreenOpener;
import hellfirepvp.modularmachinery.port.registry.MmceBlocks;
import hellfirepvp.modularmachinery.port.registry.MmceItems;
import java.util.List;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

public final class MmceClientSetup {
    private static final int DEFAULT_MACHINE_COLOR = BaseMachineBlockEntity.DEFAULT_MACHINE_COLOR;
    private static final ResourceLocation STATE_VARIANT =
            ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, "variant");

    private MmceClientSetup() {
    }

    public static void initClient() {
        MmceBlueprintScreenOpener.register(MmceBlueprintScreen::open);
    }

    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(MmceClientSetup::registerStatefulItemModelProperties);
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

    private static void registerStatefulItemModelProperties() {
        registerStateVariant(MmceItems.BLOCK_CASING.get(), CasingBlock.CASING);
        registerStateVariant(MmceItems.ITEM_INPUT_BUS.get(), ItemBusBlock.SIZE);
        registerStateVariant(MmceItems.ITEM_OUTPUT_BUS.get(), ItemBusBlock.SIZE);
        registerStateVariant(MmceItems.FLUID_INPUT_HATCH.get(), FluidHatchBlock.SIZE);
        registerStateVariant(MmceItems.FLUID_OUTPUT_HATCH.get(), FluidHatchBlock.SIZE);
        registerStateVariant(MmceItems.FLUID_PROCESSOR_HATCH.get(), FluidHatchBlock.SIZE);
        registerStateVariant(MmceItems.ENERGY_INPUT_HATCH.get(), EnergyHatchBlock.SIZE);
        registerStateVariant(MmceItems.ENERGY_OUTPUT_HATCH.get(), EnergyHatchBlock.SIZE);
        registerStateVariant(MmceItems.SMART_INTERFACE.get(), SmartInterfaceBlock.TYPE);
        registerStateVariant(MmceItems.PARALLEL_CONTROLLER.get(), ParallelControllerBlock.TYPE);
        registerStateVariant(MmceItems.UPGRADE_BUS.get(), UpgradeBusBlock.TYPE);
    }

    private static <T extends Comparable<T>> void registerStateVariant(Item item, Property<T> property) {
        List<T> values = List.copyOf(property.getPossibleValues());
        ItemProperties.register(item, STATE_VARIANT, (stack, level, entity, seed) -> {
            T value = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(property);
            if (value == null) {
                return -1.0F;
            }
            int index = values.indexOf(value);
            return index < 0 ? -1.0F : (float) index;
        });
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
