package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.RecipeFailureActions")
public enum MmceCTRecipeFailureActions {
    RESET("reset"),
    STILL("still"),
    DECREASE("decrease");

    private static final Map<String, MmceCTRecipeFailureActions> NAME_MAP = new HashMap<>();
    private static final MmceCTRecipeFailureActions DEFAULT_ACTION = STILL;

    static {
        for (MmceCTRecipeFailureActions action : values()) {
            NAME_MAP.put(action.name, action);
            NAME_MAP.put(action.name(), action);
        }
    }

    private final String name;

    MmceCTRecipeFailureActions(String name) {
        this.name = name;
    }

    @ZenCodeType.Method
    public static MmceCTRecipeFailureActions getFailureAction(String key) {
        if (key == null) {
            return DEFAULT_ACTION;
        }
        return NAME_MAP.getOrDefault(key.trim().toLowerCase(Locale.ROOT), DEFAULT_ACTION);
    }

    @ZenCodeType.Method
    public static MmceCTRecipeFailureActions getDefaultAction() {
        return DEFAULT_ACTION;
    }

    @ZenCodeType.Getter("name")
    public String getName() {
        return name;
    }
}
