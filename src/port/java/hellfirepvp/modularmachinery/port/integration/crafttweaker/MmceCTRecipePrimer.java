package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.RecipePrimer")
public final class MmceCTRecipePrimer extends MmceCTRecipeBuilder {
    MmceCTRecipePrimer(String registryName, String machine, int recipeTime, int priority, boolean cancelIfPerTickFails) {
        super(registryName, machine, recipeTime, priority, cancelIfPerTickFails);
    }
}
