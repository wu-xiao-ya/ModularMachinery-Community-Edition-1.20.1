package hellfirepvp.modularmachinery.port.integration;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.AdvancedItemCheckerCT")
public interface MmceItemChecker {
    @ZenCodeType.Method
    boolean isMatch(MachineControllerBlockEntity controller, ItemStack stack);
}
