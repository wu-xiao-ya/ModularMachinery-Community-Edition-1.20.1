package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.fluids.FluidStack;

public final class MmceNbtCompat {
    public static boolean matches(ItemStack stack, JsonObject expectedJson) {
        CompoundTag expected = toTag(expectedJson);
        if (expected.isEmpty()) {
            return true;
        }
        return NbtUtils.compareNbt(expected, synthesizeTag(stack), true);
    }

    static boolean matches(FluidStack stack, JsonObject expectedJson) {
        CompoundTag expected = toTag(expectedJson);
        if (expected.isEmpty()) {
            return true;
        }
        return NbtUtils.compareNbt(expected, synthesizeTag(stack), true);
    }

    public static boolean matches(CompoundTag actualTag, JsonObject expectedJson) {
        CompoundTag expected = toTag(expectedJson);
        if (expected.isEmpty()) {
            return true;
        }
        return NbtUtils.compareNbt(expected, actualTag == null ? new CompoundTag() : actualTag, true);
    }

    static void apply(ItemStack stack, JsonObject json) {
        CompoundTag tag = toTag(json);
        if (tag.isEmpty()) {
            return;
        }

        CompoundTag customData = tag.copy();
        applyItemKnownComponents(stack, customData);
        if (!customData.isEmpty()) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
        }
    }

    static void apply(FluidStack stack, JsonObject json) {
        CompoundTag tag = toTag(json);
        if (!tag.isEmpty()) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    private static void applyItemKnownComponents(ItemStack stack, CompoundTag tag) {
        if (tag.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            stack.set(DataComponents.DAMAGE, tag.getInt("Damage"));
            tag.remove("Damage");
        }

        if (tag.contains("display", Tag.TAG_COMPOUND)) {
            CompoundTag display = tag.getCompound("display");
            if (display.contains("Name", Tag.TAG_STRING)) {
                stack.set(DataComponents.CUSTOM_NAME, Component.literal(display.getString("Name")));
                display.remove("Name");
            }
            if (display.isEmpty()) {
                tag.remove("display");
            }
        }
    }

    private static CompoundTag synthesizeTag(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (stack.has(DataComponents.DAMAGE)) {
            tag.putInt("Damage", stack.getOrDefault(DataComponents.DAMAGE, 0));
        }

        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name != null) {
            CompoundTag display = tag.contains("display", Tag.TAG_COMPOUND) ? tag.getCompound("display").copy() : new CompoundTag();
            display.putString("Name", name.getString());
            tag.put("display", display);
        }
        return tag;
    }

    private static CompoundTag synthesizeTag(FluidStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static CompoundTag toTag(JsonObject object) {
        CompoundTag tag = new CompoundTag();
        for (var entry : object.entrySet()) {
            tag.put(entry.getKey(), toTag(entry.getValue()));
        }
        return tag;
    }

    private static Tag toTag(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return StringTag.valueOf("");
        }
        if (element.isJsonObject()) {
            return toTag(element.getAsJsonObject());
        }
        if (element.isJsonArray()) {
            return toListTag(element.getAsJsonArray());
        }

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return ByteTag.valueOf(primitive.getAsBoolean());
        }
        if (primitive.isNumber()) {
            Number number = primitive.getAsNumber();
            String raw = primitive.getAsString();
            if (raw.endsWith("f") || raw.endsWith("F")) {
                return FloatTag.valueOf(number.floatValue());
            }
            if (raw.endsWith("d") || raw.endsWith("D") || raw.contains(".")) {
                return DoubleTag.valueOf(number.doubleValue());
            }
            if (raw.endsWith("l") || raw.endsWith("L")) {
                return LongTag.valueOf(number.longValue());
            }
            return IntTag.valueOf(number.intValue());
        }
        return StringTag.valueOf(primitive.getAsString());
    }

    private static ListTag toListTag(JsonArray array) {
        ListTag list = new ListTag();
        for (JsonElement element : array) {
            Tag tag = toTag(element);
            if (!list.addTag(list.size(), tag)) {
                break;
            }
        }
        return list;
    }

    private MmceNbtCompat() {
    }
}
