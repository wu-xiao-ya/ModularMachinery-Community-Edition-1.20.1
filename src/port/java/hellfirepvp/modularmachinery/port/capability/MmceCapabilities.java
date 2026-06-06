package hellfirepvp.modularmachinery.port.capability;

import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class MmceCapabilities {
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MmceBlockEntities.ITEM_INPUT_BUS.get(),
                (bus, side) -> new MmceItemHandler(bus, true, false));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MmceBlockEntities.ITEM_OUTPUT_BUS.get(),
                (bus, side) -> new MmceItemHandler(bus, false, true));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MmceBlockEntities.UPGRADE_BUS.get(),
                (bus, side) -> new MmceItemHandler(bus, true, true));

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, MmceBlockEntities.FLUID_INPUT_HATCH.get(),
                (hatch, side) -> new MmceFluidHandler(hatch));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, MmceBlockEntities.FLUID_OUTPUT_HATCH.get(),
                (hatch, side) -> new MmceFluidHandler(hatch));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, MmceBlockEntities.FLUID_PROCESSOR_HATCH.get(),
                (hatch, side) -> new MmceFluidHandler(hatch));

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, MmceBlockEntities.ENERGY_INPUT_HATCH.get(),
                (hatch, side) -> new MmceEnergyStorage(hatch));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, MmceBlockEntities.ENERGY_OUTPUT_HATCH.get(),
                (hatch, side) -> new MmceEnergyStorage(hatch));
    }

    private MmceCapabilities() {
    }
}
