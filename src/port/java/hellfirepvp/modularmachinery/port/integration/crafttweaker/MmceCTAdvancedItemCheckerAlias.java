package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.integration.MmceItemChecker;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.AdvancedItemChecker")
public interface MmceCTAdvancedItemCheckerAlias extends MmceItemChecker {
    @Override
    @ZenCodeType.Method
    boolean isMatch(MachineControllerBlockEntity controller, ItemStack stack);
}
