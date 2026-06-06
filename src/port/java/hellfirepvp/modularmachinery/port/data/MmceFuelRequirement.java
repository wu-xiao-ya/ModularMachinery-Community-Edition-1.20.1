package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;

public record MmceFuelRequirement(
        int burnTime,
        float chance,
        JsonObject matchNbt,
        int triggerTime,
        boolean triggerRepeatable,
        boolean ignoreOutputCheck,
        boolean parallelizeUnaffected
) implements MmceParsedRequirement {
    public MmceFuelRequirement {
        burnTime = Math.max(1, burnTime);
        chance = Math.max(0.0F, Math.min(1.0F, chance));
        matchNbt = matchNbt == null ? new JsonObject() : matchNbt.deepCopy();
        triggerTime = Math.max(0, triggerTime);
    }

    @Override
    public MmceIoType ioType() {
        return MmceIoType.INPUT;
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && stack.getBurnTime(null) > 0 && MmceNbtCompat.matches(stack, matchNbt);
    }
}
