package hellfirepvp.modularmachinery.port.integration.kubejs;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum MmceKubeJSRecipeFailureActions {
    RESET("reset"),
    STILL("still"),
    DECREASE("decrease");

    private static final Map<String, MmceKubeJSRecipeFailureActions> NAME_MAP = new HashMap<>();
    private static final MmceKubeJSRecipeFailureActions DEFAULT_ACTION = STILL;

    static {
        for (MmceKubeJSRecipeFailureActions action : values()) {
            NAME_MAP.put(action.name, action);
            NAME_MAP.put(action.name(), action);
        }
    }

    private final String name;

    MmceKubeJSRecipeFailureActions(String name) {
        this.name = name;
    }

    public static MmceKubeJSRecipeFailureActions getFailureAction(String key) {
        if (key == null) {
            return DEFAULT_ACTION;
        }
        return NAME_MAP.getOrDefault(key.trim().toLowerCase(Locale.ROOT), DEFAULT_ACTION);
    }

    public static MmceKubeJSRecipeFailureActions getDefaultAction() {
        return DEFAULT_ACTION;
    }

    public String getName() {
        return name;
    }
}
