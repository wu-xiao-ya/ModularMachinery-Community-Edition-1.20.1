package hellfirepvp.modularmachinery.port;

import com.mojang.logging.LogUtils;
import hellfirepvp.modularmachinery.port.capability.MmceCapabilities;
import hellfirepvp.modularmachinery.port.command.MmceCommands;
import hellfirepvp.modularmachinery.port.data.MmceDataReloadListener;
import hellfirepvp.modularmachinery.port.integration.kubejs.MmceKubeJSIntegration;
import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import hellfirepvp.modularmachinery.port.registry.MmceBlocks;
import hellfirepvp.modularmachinery.port.registry.MmceCreativeTabs;
import hellfirepvp.modularmachinery.port.registry.MmceItems;
import hellfirepvp.modularmachinery.port.registry.MmceMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(ModularMachineryNeoForge.MODID)
public final class ModularMachineryNeoForge {
    public static final String MODID = "modularmachinery";
    public static final String NAME = "Modular Machinery: Community Edition";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ModularMachineryNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        MmceBlocks.BLOCKS.register(modEventBus);
        MmceBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        MmceItems.ITEMS.register(modEventBus);
        MmceCreativeTabs.TABS.register(modEventBus);
        MmceMenus.MENUS.register(modEventBus);
        modEventBus.addListener(MmceCapabilities::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(MmceDataReloadListener::addReloadListener);
        NeoForge.EVENT_BUS.addListener(MmceCommands::register);

        if (ModList.get().isLoaded("kubejs")) {
            MmceKubeJSIntegration.bootstrap();
        }

        LOGGER.info("Bootstrapping {} on NeoForge 1.21.1", NAME);
    }
}
