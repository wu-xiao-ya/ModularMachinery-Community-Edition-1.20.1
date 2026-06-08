package hellfirepvp.modularmachinery.port.integration.mekanism;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.blockentity.FluidHatchBlockEntity;
import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class MmceMekanismChemicalCapabilities {
    private static final String MEKANISM_MODID = "mekanism";

    public static void register(RegisterCapabilitiesEvent event) {
        if (!ModList.get().isLoaded(MEKANISM_MODID)) {
            return;
        }

        try {
            MmceMekanismChemicalHandler.bootstrap();
            BlockCapability<?, ?> chemicalCapability = chemicalBlockCapability();
            ICapabilityProvider<FluidHatchBlockEntity, Object, Object> provider =
                    (hatch, side) -> MmceMekanismChemicalHandler.create(hatch);

            register(event, chemicalCapability, provider);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            ModularMachineryNeoForge.LOGGER.warn("Could not register Mekanism chemical capability bridge", exception);
        }
    }

    private static BlockCapability<?, ?> chemicalBlockCapability()
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Class<?> capabilities = Class.forName("mekanism.common.capabilities.Capabilities");
        Field chemicalField = capabilities.getField("CHEMICAL");
        Object chemicalCapability = chemicalField.get(null);
        Method blockMethod = chemicalCapability.getClass().getMethod("block");
        return (BlockCapability<?, ?>) blockMethod.invoke(chemicalCapability);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void register(RegisterCapabilitiesEvent event,
                                 BlockCapability<?, ?> capability,
                                 ICapabilityProvider<FluidHatchBlockEntity, Object, Object> provider) {
        BlockCapability typedCapability = capability;
        ICapabilityProvider typedProvider = provider;
        event.registerBlockEntity(typedCapability, MmceBlockEntities.FLUID_INPUT_HATCH.get(), typedProvider);
        event.registerBlockEntity(typedCapability, MmceBlockEntities.FLUID_OUTPUT_HATCH.get(), typedProvider);
        event.registerBlockEntity(typedCapability, MmceBlockEntities.FLUID_PROCESSOR_HATCH.get(), typedProvider);
    }

    private MmceMekanismChemicalCapabilities() {
    }
}
