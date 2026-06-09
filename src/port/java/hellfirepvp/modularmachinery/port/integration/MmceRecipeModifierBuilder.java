package hellfirepvp.modularmachinery.port.integration;

public final class MmceRecipeModifierBuilder {
    private String type = "";
    private String ioTypeStr = "";
    private float value = 0.0F;
    private int operation = MmceRecipeModifier.OPERATION_ADD;
    private boolean affectChance = false;

    private MmceRecipeModifierBuilder() {
    }

    public static MmceRecipeModifierBuilder newBuilder() {
        return new MmceRecipeModifierBuilder();
    }

    public static MmceRecipeModifierBuilder create(String type, String ioTypeStr, float value, int operation, boolean affectChance) {
        return newBuilder()
                .setRequirementType(type)
                .setIOType(ioTypeStr)
                .setValue(value)
                .setOperation(operation)
                .isAffectChance(affectChance);
    }

    public MmceRecipeModifierBuilder setRequirementType(String type) {
        this.type = type == null ? "" : type.trim();
        return this;
    }

    public MmceRecipeModifierBuilder setTarget(String type) {
        return setRequirementType(type);
    }

    public MmceRecipeModifierBuilder set_requirement_type(String type) {
        return setRequirementType(type);
    }

    public MmceRecipeModifierBuilder setIOType(String ioTypeStr) {
        this.ioTypeStr = ioTypeStr == null ? "" : ioTypeStr.trim();
        return this;
    }

    public MmceRecipeModifierBuilder setIoType(String ioTypeStr) {
        return setIOType(ioTypeStr);
    }

    public MmceRecipeModifierBuilder set_io_type(String ioTypeStr) {
        return setIOType(ioTypeStr);
    }

    public MmceRecipeModifierBuilder setValue(float value) {
        this.value = value;
        return this;
    }

    public MmceRecipeModifierBuilder setModifier(float value) {
        return setValue(value);
    }

    public MmceRecipeModifierBuilder set_value(float value) {
        return setValue(value);
    }

    public MmceRecipeModifierBuilder setOperation(int operation) {
        this.operation = operation;
        return this;
    }

    public MmceRecipeModifierBuilder set_operation(int operation) {
        return setOperation(operation);
    }

    public MmceRecipeModifierBuilder isAffectChance(boolean affectChance) {
        this.affectChance = affectChance;
        return this;
    }

    public MmceRecipeModifierBuilder setAffectChance(boolean affectChance) {
        return isAffectChance(affectChance);
    }

    public MmceRecipeModifierBuilder setAffectsChance(boolean affectChance) {
        return isAffectChance(affectChance);
    }

    public MmceRecipeModifierBuilder set_affect_chance(boolean affectChance) {
        return isAffectChance(affectChance);
    }

    public MmceRecipeModifier build() {
        return new MmceRecipeModifier(type, ioTypeStr, value, operation, affectChance);
    }
}
