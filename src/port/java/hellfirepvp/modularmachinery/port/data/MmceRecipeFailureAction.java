package hellfirepvp.modularmachinery.port.data;

import java.util.Locale;

public enum MmceRecipeFailureAction {
    RESET("reset"),
    STILL("still"),
    DECREASE("decrease");

    private final String serializedName;

    MmceRecipeFailureAction(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public static MmceRecipeFailureAction byName(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        for (MmceRecipeFailureAction action : values()) {
            if (action.serializedName.equals(normalized) || action.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return action;
            }
        }
        return STILL;
    }
}
