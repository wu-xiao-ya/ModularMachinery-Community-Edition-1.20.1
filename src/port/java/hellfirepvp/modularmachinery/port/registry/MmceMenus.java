package hellfirepvp.modularmachinery.port.registry;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.menu.MmceMachineMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MmceMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ModularMachineryNeoForge.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MmceMachineMenu>> MACHINE =
            MENUS.register("machine", () -> IMenuTypeExtension.create(MmceMachineMenu::fromNetwork));

    private MmceMenus() {
    }
}
