package hellfirepvp.modularmachinery.port.integration;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface MmceItemModifier {
    ItemStack apply(MachineControllerBlockEntity controller, ItemStack stack);
}
