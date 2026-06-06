package hellfirepvp.modularmachinery.port.recipe;

import hellfirepvp.modularmachinery.port.blockentity.EnergyHatchBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.FluidHatchBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.ItemBusBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.ParallelControllerBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.SmartInterfaceBlockEntity;
import hellfirepvp.modularmachinery.port.blockentity.UpgradeBusBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public record MmceMachineComponents(
        List<ItemBusBlockEntity> itemInputs,
        List<ItemBusBlockEntity> itemOutputs,
        List<FluidHatchBlockEntity> fluidInputs,
        List<FluidHatchBlockEntity> fluidOutputs,
        List<EnergyHatchBlockEntity> energyInputs,
        List<EnergyHatchBlockEntity> energyOutputs,
        List<ParallelControllerBlockEntity> parallelControllers,
        List<SmartInterfaceBlockEntity> smartInterfaces,
        List<UpgradeBusBlockEntity> upgradeBuses,
        Map<BlockPos, String> componentTags
) {
    public static MmceMachineComponents collect(Level level, List<BlockPos> componentPositions) {
        return collect(level, componentPositions, Map.of());
    }

    public static MmceMachineComponents collect(Level level, List<BlockPos> componentPositions, Map<BlockPos, String> componentTags) {
        List<ItemBusBlockEntity> itemInputs = new ArrayList<>();
        List<ItemBusBlockEntity> itemOutputs = new ArrayList<>();
        List<FluidHatchBlockEntity> fluidInputs = new ArrayList<>();
        List<FluidHatchBlockEntity> fluidOutputs = new ArrayList<>();
        List<EnergyHatchBlockEntity> energyInputs = new ArrayList<>();
        List<EnergyHatchBlockEntity> energyOutputs = new ArrayList<>();
        List<ParallelControllerBlockEntity> parallelControllers = new ArrayList<>();
        List<SmartInterfaceBlockEntity> smartInterfaces = new ArrayList<>();
        List<UpgradeBusBlockEntity> upgradeBuses = new ArrayList<>();

        for (BlockPos pos : componentPositions) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ItemBusBlockEntity bus) {
                if (bus.isInput()) {
                    itemInputs.add(bus);
                } else {
                    itemOutputs.add(bus);
                }
            } else if (blockEntity instanceof FluidHatchBlockEntity hatch) {
                if (hatch.isInput() || hatch.isProcessor()) {
                    fluidInputs.add(hatch);
                }
                if (!hatch.isInput() || hatch.isProcessor()) {
                    fluidOutputs.add(hatch);
                }
            } else if (blockEntity instanceof EnergyHatchBlockEntity hatch) {
                if (hatch.isInput()) {
                    energyInputs.add(hatch);
                } else {
                    energyOutputs.add(hatch);
                }
            } else if (blockEntity instanceof ParallelControllerBlockEntity controller) {
                parallelControllers.add(controller);
            } else if (blockEntity instanceof SmartInterfaceBlockEntity smartInterface) {
                smartInterfaces.add(smartInterface);
            } else if (blockEntity instanceof UpgradeBusBlockEntity bus) {
                upgradeBuses.add(bus);
            }
        }

        return new MmceMachineComponents(
                List.copyOf(itemInputs),
                List.copyOf(itemOutputs),
                List.copyOf(fluidInputs),
                List.copyOf(fluidOutputs),
                List.copyOf(energyInputs),
                List.copyOf(energyOutputs),
                List.copyOf(parallelControllers),
                List.copyOf(smartInterfaces),
                List.copyOf(upgradeBuses),
                Map.copyOf(componentTags == null ? Map.of() : componentTags)
        );
    }

    public List<ItemBusBlockEntity> itemInputs(Optional<String> selectorTag) {
        return byTag(itemInputs, selectorTag);
    }

    public List<ItemBusBlockEntity> itemOutputs(Optional<String> selectorTag) {
        return byTag(itemOutputs, selectorTag);
    }

    public List<FluidHatchBlockEntity> fluidInputs(Optional<String> selectorTag) {
        return byTag(fluidInputs, selectorTag);
    }

    public List<FluidHatchBlockEntity> fluidOutputs(Optional<String> selectorTag) {
        return byTag(fluidOutputs, selectorTag);
    }

    public List<EnergyHatchBlockEntity> energyInputs(Optional<String> selectorTag) {
        return byTag(energyInputs, selectorTag);
    }

    public List<EnergyHatchBlockEntity> energyOutputs(Optional<String> selectorTag) {
        return byTag(energyOutputs, selectorTag);
    }

    public List<SmartInterfaceBlockEntity> smartInterfaces(Optional<String> selectorTag) {
        return byTag(smartInterfaces, selectorTag);
    }

    public int maxParallelism() {
        int maxParallelism = 0;
        for (ParallelControllerBlockEntity controller : parallelControllers) {
            maxParallelism += controller.getParallelism();
        }
        return Math.max(1, maxParallelism);
    }

    private <T extends BlockEntity> List<T> byTag(List<T> components, Optional<String> selectorTag) {
        if (selectorTag.isEmpty()) {
            return components;
        }
        String tag = selectorTag.get();
        return components.stream()
                .filter(component -> tag.equals(componentTags.get(component.getBlockPos())))
                .toList();
    }
}
