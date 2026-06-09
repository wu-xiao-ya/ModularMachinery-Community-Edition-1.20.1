package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import org.openzen.zencode.java.ZenCodeType;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.RecipeEventHandler")
public interface MmceCTRecipeEventHandler {
    @ZenCodeType.Method
    void handle(MmceCTRecipeEvent event);
}
