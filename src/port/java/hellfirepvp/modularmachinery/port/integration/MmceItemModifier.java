package hellfirepvp.modularmachinery.port.integration;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.AdvancedItemModifier")
public interface MmceItemModifier {
    @ZenCodeType.Method
    ItemStack apply(MachineControllerBlockEntity controller, ItemStack stack);
}
