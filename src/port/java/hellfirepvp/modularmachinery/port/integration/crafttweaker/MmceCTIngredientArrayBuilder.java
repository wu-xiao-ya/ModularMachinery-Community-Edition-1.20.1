package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.IngredientArrayBuilder")
public final class MmceCTIngredientArrayBuilder {
    @ZenCodeType.Method
    public static MmceCTIngredientArrayPrimer newBuilder() {
        return new MmceCTIngredientArrayPrimer();
    }

    private MmceCTIngredientArrayBuilder() {
    }
}
