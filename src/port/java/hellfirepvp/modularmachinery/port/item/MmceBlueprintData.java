package hellfirepvp.modularmachinery.port.item;

import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class MmceBlueprintData {
    public static final String LEGACY_MACHINE_KEY = "dynamicmachine";
    private static final String MACHINE_KEY = "machineId";

    public static Optional<ResourceLocation> getMachineId(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String value = tag.contains(LEGACY_MACHINE_KEY) ? tag.getString(LEGACY_MACHINE_KEY) : tag.getString(MACHINE_KEY);
        if (value.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ResourceLocation.tryParse(value));
    }

    public static ItemStack stackFor(MmceBlueprintItem item, ResourceLocation machineId) {
        ItemStack stack = new ItemStack(item);
        setMachineId(stack, machineId);
        return stack;
    }

    public static void setMachineId(ItemStack stack, ResourceLocation machineId) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (machineId == null) {
            tag.remove(LEGACY_MACHINE_KEY);
            tag.remove(MACHINE_KEY);
        } else {
            String value = machineId.toString();
            tag.putString(LEGACY_MACHINE_KEY, value);
            tag.putString(MACHINE_KEY, value);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private MmceBlueprintData() {
    }
}
