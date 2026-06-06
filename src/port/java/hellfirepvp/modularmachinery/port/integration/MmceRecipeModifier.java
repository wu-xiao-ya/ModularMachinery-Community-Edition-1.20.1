package hellfirepvp.modularmachinery.port.integration;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.RecipeModifier")
public final class MmceRecipeModifier {
    @ZenCodeType.Field("IO_INPUT")
    public static final String IO_INPUT = "input";
    @ZenCodeType.Field("IO_OUTPUT")
    public static final String IO_OUTPUT = "output";
    @ZenCodeType.Field("OPERATION_ADD")
    public static final int OPERATION_ADD = 0;
    @ZenCodeType.Field("OPERATION_MULTIPLY")
    public static final int OPERATION_MULTIPLY = 1;

    private final ResourceLocation target;
    private final String ioTarget;
    private final double modifier;
    private final int operation;
    private final boolean affectsChance;

    public MmceRecipeModifier(String target, String ioTarget, double modifier, int operation, boolean affectsChance) {
        this.target = targetId(target);
        this.ioTarget = normalizeIo(ioTarget);
        this.modifier = modifier;
        this.operation = validateOperation(operation);
        this.affectsChance = affectsChance;
    }

    @ZenCodeType.Method
    public static MmceRecipeModifier create(String target, String ioTarget, double modifier, int operation) {
        return create(target, ioTarget, modifier, operation, false);
    }

    @ZenCodeType.Method
    public static MmceRecipeModifier create(String target, String ioTarget, double modifier, int operation, boolean affectsChance) {
        return new MmceRecipeModifier(target, ioTarget, modifier, operation, affectsChance);
    }

    public JsonObject json() {
        JsonObject object = new JsonObject();
        object.addProperty("target", target.toString());
        if (!ioTarget.isBlank()) {
            object.addProperty("io", ioTarget);
        }
        object.addProperty("operation", operation);
        object.addProperty("multiplier", modifier);
        object.addProperty("affectChance", affectsChance);
        return object;
    }

    public CompoundTag tag(boolean stackable) {
        CompoundTag tag = new CompoundTag();
        tag.putString("target", target.toString());
        if (!ioTarget.isBlank()) {
            tag.putString("io", ioTarget);
        }
        tag.putInt("operation", operation);
        tag.putDouble("multiplier", modifier);
        tag.putBoolean("affectChance", affectsChance);
        tag.putBoolean("stackable", stackable);
        return tag;
    }

    @ZenCodeType.Method
    public String getTarget() {
        return target.toString();
    }

    @ZenCodeType.Method
    public String getIOTarget() {
        return ioTarget;
    }

    @ZenCodeType.Method
    public float getModifier() {
        return (float) modifier;
    }

    public double modifier() {
        return modifier;
    }

    @ZenCodeType.Method
    public boolean affectsChance() {
        return affectsChance;
    }

    @ZenCodeType.Method
    public int getOperation() {
        return operation;
    }

    @ZenCodeType.Method
    public MmceRecipeModifier multiply(float value) {
        return new MmceRecipeModifier(target.toString(), ioTarget, modifier * value, operation, affectsChance);
    }

    @ZenCodeType.Method
    public MmceRecipeModifier add(float value) {
        return new MmceRecipeModifier(target.toString(), ioTarget, modifier + value, operation, affectsChance);
    }

    ResourceLocation targetId() {
        return target;
    }

    String ioTarget() {
        return ioTarget;
    }

    private static int validateOperation(int operation) {
        if (operation != OPERATION_ADD && operation != OPERATION_MULTIPLY) {
            throw new IllegalArgumentException("MMCE recipe modifier operation must be 0/add or 1/multiply.");
        }
        return operation;
    }

    private static ResourceLocation targetId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("MMCE recipe modifier target cannot be blank.");
        }
        return id.indexOf(':') >= 0
                ? ResourceLocation.parse(id)
                : ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, id);
    }

    private static String normalizeIo(String io) {
        if (io == null || io.isBlank()) {
            return "";
        }
        String normalized = io.trim().toLowerCase(java.util.Locale.ROOT);
        if (!normalized.equals(IO_INPUT) && !normalized.equals(IO_OUTPUT)) {
            throw new IllegalArgumentException("MMCE recipe modifier io must be input, output, or blank.");
        }
        return normalized;
    }
}
