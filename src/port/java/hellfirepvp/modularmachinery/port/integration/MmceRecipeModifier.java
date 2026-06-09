package hellfirepvp.modularmachinery.port.integration;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class MmceRecipeModifier {
    public static final String IO_INPUT = "input";
    public static final String IO_OUTPUT = "output";
    public static final int OPERATION_ADD = 0;
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

    public static MmceRecipeModifier create(String target, String ioTarget, double modifier, int operation) {
        return create(target, ioTarget, modifier, operation, false);
    }

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

    public String getTarget() {
        return target.toString();
    }

    public String getIOTarget() {
        return ioTarget;
    }

    public float getModifier() {
        return (float) modifier;
    }

    public double modifier() {
        return modifier;
    }

    public boolean affectsChance() {
        return affectsChance;
    }

    public int getOperation() {
        return operation;
    }

    public MmceRecipeModifier multiply(float value) {
        return new MmceRecipeModifier(target.toString(), ioTarget, modifier * value, operation, affectsChance);
    }

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
