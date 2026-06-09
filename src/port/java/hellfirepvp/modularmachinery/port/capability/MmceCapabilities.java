package hellfirepvp.modularmachinery.port.capability;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import java.lang.reflect.Method;
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

        registerMekanismChemicalCapabilities(event);
    }

    private static void registerMekanismChemicalCapabilities(RegisterCapabilitiesEvent event) {
        try {
            Class<?> capabilities = Class.forName(
                    "hellfirepvp.modularmachinery.port.integration.mekanism.MmceMekanismChemicalCapabilities",
                    false,
                    MmceCapabilities.class.getClassLoader()
            );
            Method register = capabilities.getMethod("register", RegisterCapabilitiesEvent.class);
            register.invoke(null, event);
        } catch (ReflectiveOperationException | LinkageError exception) {
            ModularMachineryNeoForge.LOGGER.warn("Could not initialize MMCE Mekanism capability bridge", exception);
        }
    }

    private MmceCapabilities() {
    }
}
