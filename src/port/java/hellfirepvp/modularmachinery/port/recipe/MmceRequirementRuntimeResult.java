package hellfirepvp.modularmachinery.port.recipe;

public record MmceRequirementRuntimeResult(
        boolean ok,
        MmceRecipeStatus status,
        String detail
) {
    private static final MmceRequirementRuntimeResult SUCCESS = new MmceRequirementRuntimeResult(true, MmceRecipeStatus.RUNNING, "");

    public MmceRequirementRuntimeResult {
        status = status == null ? MmceRecipeStatus.FAILED : status;
        detail = detail == null ? "" : detail;
    }

    public static MmceRequirementRuntimeResult success() {
        return SUCCESS;
    }

    public static MmceRequirementRuntimeResult failure(MmceRecipeStatus status, String detail) {
        return new MmceRequirementRuntimeResult(false, status, detail);
    }

    public static MmceRequirementRuntimeResult missingInput(String detail) {
        return failure(MmceRecipeStatus.MISSING_INPUT, detail);
    }

    public static MmceRequirementRuntimeResult outputBlocked(String detail) {
        return failure(MmceRecipeStatus.OUTPUT_BLOCKED, detail);
    }
}
