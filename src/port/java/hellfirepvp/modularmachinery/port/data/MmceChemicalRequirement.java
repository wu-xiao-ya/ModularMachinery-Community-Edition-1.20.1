package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record MmceChemicalRequirement(
        MmceIoType ioType,
        ResourceLocation chemicalId,
        int amount,
        float chance,
        JsonObject matchNbt,
        JsonObject displayNbt,
        boolean perTick,
        int triggerTime,
        boolean triggerRepeatable,
        boolean ignoreOutputCheck,
        boolean parallelizeUnaffected
) implements MmceParsedRequirement {
    public MmceChemicalRequirement {
        amount = Math.max(0, amount);
        chance = Math.max(0.0F, Math.min(1.0F, chance));
        matchNbt = matchNbt == null ? new JsonObject() : matchNbt.deepCopy();
        displayNbt = displayNbt == null ? new JsonObject() : displayNbt.deepCopy();
        triggerTime = Math.max(0, triggerTime);
    }

    public boolean matches(ResourceLocation storedChemicalId, CompoundTag storedNbt) {
        return chemicalId.equals(storedChemicalId) && MmceNbtCompat.matches(storedNbt, matchNbt);
    }

    public CompoundTag createNbt() {
        return MmceNbtCompat.toTag(matchNbt);
    }
}
