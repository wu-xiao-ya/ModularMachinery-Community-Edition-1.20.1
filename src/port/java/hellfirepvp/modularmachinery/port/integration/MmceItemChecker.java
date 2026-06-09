package hellfirepvp.modularmachinery.port.integration;

import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface MmceItemChecker {
    boolean isMatch(MachineControllerBlockEntity controller, ItemStack stack);
}
