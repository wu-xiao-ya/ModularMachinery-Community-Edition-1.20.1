package hellfirepvp.modularmachinery.port.integration;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.RecipeModifierBuilder")
public final class MmceRecipeModifierBuilder {
    private String type = "";
    private String ioTypeStr = "";
    private float value = 0.0F;
    private int operation = MmceRecipeModifier.OPERATION_ADD;
    private boolean affectChance = false;

    private MmceRecipeModifierBuilder() {
    }

    @ZenCodeType.Method
    public static MmceRecipeModifierBuilder newBuilder() {
        return new MmceRecipeModifierBuilder();
    }

    @ZenCodeType.Method
    public static MmceRecipeModifierBuilder create(String type, String ioTypeStr, float value, int operation, boolean affectChance) {
        return newBuilder()
                .setRequirementType(type)
                .setIOType(ioTypeStr)
                .setValue(value)
                .setOperation(operation)
                .isAffectChance(affectChance);
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder setRequirementType(String type) {
        this.type = type == null ? "" : type.trim();
        return this;
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder setTarget(String type) {
        return setRequirementType(type);
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder set_requirement_type(String type) {
        return setRequirementType(type);
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder setIOType(String ioTypeStr) {
        this.ioTypeStr = ioTypeStr == null ? "" : ioTypeStr.trim();
        return this;
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder setIoType(String ioTypeStr) {
        return setIOType(ioTypeStr);
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder set_io_type(String ioTypeStr) {
        return setIOType(ioTypeStr);
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder setValue(float value) {
        this.value = value;
        return this;
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder setModifier(float value) {
        return setValue(value);
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder set_value(float value) {
        return setValue(value);
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder setOperation(int operation) {
        this.operation = operation;
        return this;
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder set_operation(int operation) {
        return setOperation(operation);
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder isAffectChance(boolean affectChance) {
        this.affectChance = affectChance;
        return this;
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder setAffectChance(boolean affectChance) {
        return isAffectChance(affectChance);
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder setAffectsChance(boolean affectChance) {
        return isAffectChance(affectChance);
    }

    @ZenCodeType.Method
    public MmceRecipeModifierBuilder set_affect_chance(boolean affectChance) {
        return isAffectChance(affectChance);
    }

    @ZenCodeType.Method
    public MmceRecipeModifier build() {
        return new MmceRecipeModifier(type, ioTypeStr, value, operation, affectChance);
    }
}
