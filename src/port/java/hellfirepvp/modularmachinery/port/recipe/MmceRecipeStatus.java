package hellfirepvp.modularmachinery.port.recipe;

import java.util.Locale;

public enum MmceRecipeStatus {
    IDLE("idle", "Idle"),
    STRUCTURE_MISSING("structure_missing", "Structure missing"),
    NO_MACHINE("no_machine", "No machine"),
    NO_RECIPE("no_recipe", "No recipe"),
    UNSUPPORTED_REQUIREMENT("unsupported_requirement", "Unsupported requirement"),
    MISSING_INPUT("missing_input", "Missing input"),
    OUTPUT_BLOCKED("output_blocked", "Output blocked"),
    MISSING_ENERGY("missing_energy", "Missing energy"),
    FAILED("failed", "Failed"),
    RUNNING("running", "Running"),
    FINISHED("finished", "Finished");

    private final String serializedName;
    private final String displayName;

    MmceRecipeStatus(String serializedName, String displayName) {
        this.serializedName = serializedName;
        this.displayName = displayName;
    }

    public String serializedName() {
        return serializedName;
    }

    public String displayName() {
        return displayName;
    }

    public static MmceRecipeStatus bySerializedName(String name) {
        String normalized = name == null ? "" : name.toLowerCase(Locale.ROOT);
        for (MmceRecipeStatus status : values()) {
            if (status.serializedName.equals(normalized)) {
                return status;
            }
        }
        return IDLE;
    }
}
