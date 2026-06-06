package hellfirepvp.modularmachinery.port.registry;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MmceCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModularMachineryNeoForge.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modularmachinery"))
                    .icon(() -> new ItemStack(MmceItems.MODULARIUM.get()))
                    .displayItems((parameters, output) ->
                            MmceItems.creativeTabStacks().forEach(output::accept))
                    .build());

    private MmceCreativeTabs() {
    }
}
