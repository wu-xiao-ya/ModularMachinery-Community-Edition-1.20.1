package hellfirepvp.modularmachinery.port.event;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import org.openzen.zencode.java.ZenCodeType;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.RecipeEventHandler")
public interface MmceRecipeEventHandler {
    @ZenCodeType.Method
    void handle(MmceRecipeEvent event);
}
