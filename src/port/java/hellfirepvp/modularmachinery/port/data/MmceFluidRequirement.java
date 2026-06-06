package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public record MmceFluidRequirement(
        MmceIoType ioType,
        ResourceLocation fluidId,
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
    public MmceFluidRequirement {
        amount = Math.max(0, amount);
        chance = Math.max(0.0F, Math.min(1.0F, chance));
        matchNbt = matchNbt == null ? new JsonObject() : matchNbt.deepCopy();
        displayNbt = displayNbt == null ? new JsonObject() : displayNbt.deepCopy();
        triggerTime = Math.max(0, triggerTime);
    }

    public boolean matches(FluidStack stack) {
        return !stack.isEmpty()
                && BuiltInRegistries.FLUID.getKey(stack.getFluid()).equals(fluidId)
                && MmceNbtCompat.matches(stack, matchNbt);
    }

    public FluidStack createStack(int stackAmount) {
        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
        if (fluid == Fluids.EMPTY || stackAmount <= 0) {
            return FluidStack.EMPTY;
        }
        FluidStack stack = new FluidStack(fluid, stackAmount);
        MmceNbtCompat.apply(stack, matchNbt);
        return stack;
    }
}
