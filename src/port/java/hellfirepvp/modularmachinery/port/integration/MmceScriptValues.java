package hellfirepvp.modularmachinery.port.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.fluids.FluidStack;

public final class MmceScriptValues {
    private static final Pattern ID_PATTERN = Pattern.compile("#?[a-z0-9_.-]+:[a-z0-9_./-]+");

    private MmceScriptValues() {
    }

    public static List<ItemEntry> itemEntries(Object value, int fallbackAmount) {
        List<ItemEntry> entries = new ArrayList<>();
        for (Object element : flatten(value)) {
            itemEntry(element, fallbackAmount).ifPresent(entries::add);
        }
        return entries;
    }

    public static Optional<ItemEntry> itemEntry(Object value, int fallbackAmount) {
        if (value == null) {
            return Optional.empty();
        }
        int amount = safeAmount(fallbackAmount, amountFrom(value).orElse(1));

        if (value instanceof ItemStack stack) {
            return itemStack(stack, amount);
        }
        if (value instanceof Item item) {
            return Optional.of(new ItemEntry(BuiltInRegistries.ITEM.getKey(item).toString(), amount, Optional.empty(), Optional.empty()));
        }
        if (value instanceof ResourceLocation id) {
            return Optional.of(new ItemEntry(id.toString(), amount, Optional.empty(), Optional.empty()));
        }
        if (value instanceof JsonObject object) {
            return itemEntryFromJson(object, amount);
        }
        if (value instanceof Map<?, ?> map) {
            return itemEntryFromJson(mapToJson(map), amount);
        }

        Optional<Object> nested = invoke(value, "getItemStack", "asItemStack", "getInternal", "getImmutableInternal", "getStack");
        if (nested.isPresent() && nested.get() != value) {
            return itemEntry(nested.get(), amount);
        }
        nested = invoke(value, "getItem", "item", "asItem");
        if (nested.isPresent() && nested.get() != value) {
            Optional<ItemEntry> entry = itemEntry(nested.get(), amount);
            if (entry.isPresent()) {
                return entry;
            }
        }

        Optional<String> id = idFrom(value, "getId", "id", "getItemId", "itemId", "getRegistryName", "registryName");
        if (id.isEmpty()) {
            id = resourceLike(value.toString());
        }
        return id.map(itemId -> new ItemEntry(itemId, amount, Optional.empty(), Optional.empty()));
    }

    public static List<String> blockElements(Object value) {
        List<String> entries = new ArrayList<>();
        for (Object element : flatten(value)) {
            blockElement(element).ifPresent(entries::add);
        }
        return entries;
    }

    public static Optional<String> blockElement(Object value) {
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof CharSequence sequence) {
            String descriptor = sequence.toString();
            return descriptor.isBlank() ? Optional.empty() : Optional.of(descriptor);
        }
        if (value instanceof ResourceLocation id) {
            return Optional.of(id.toString());
        }
        if (value instanceof BlockState state) {
            return Optional.of(blockStateDescriptor(state));
        }
        if (value instanceof Block block) {
            return Optional.of(BuiltInRegistries.BLOCK.getKey(block).toString());
        }
        if (value instanceof ItemStack stack) {
            return blockItemDescriptor(stack.getItem());
        }
        if (value instanceof Item item) {
            return blockItemDescriptor(item);
        }
        if (value instanceof JsonObject object) {
            return blockElementFromJson(object);
        }
        if (value instanceof Map<?, ?> map) {
            return blockElementFromJson(mapToJson(map));
        }

        Optional<Object> nested = invoke(value, "getBlockState", "asBlockState", "blockState", "getState", "state");
        if (nested.isPresent() && nested.get() != value) {
            return blockElement(nested.get());
        }
        nested = invoke(value, "getBlock", "block", "asBlock");
        if (nested.isPresent() && nested.get() != value) {
            return blockElement(nested.get());
        }
        nested = invoke(value, "getItemStack", "asItemStack", "getInternal", "getImmutableInternal", "getStack");
        if (nested.isPresent() && nested.get() != value) {
            return blockElement(nested.get());
        }
        nested = invoke(value, "getItem", "item", "asItem");
        if (nested.isPresent() && nested.get() != value) {
            return blockElement(nested.get());
        }

        Optional<String> id = idFrom(value, "getId", "id", "getBlockId", "blockId", "getRegistryName", "registryName");
        if (id.isEmpty()) {
            id = resourceLike(value.toString());
        }
        return id;
    }

    public static Optional<FluidEntry> fluidEntry(Object value, int fallbackAmount) {
        if (value == null) {
            return Optional.empty();
        }
        int amount = safeAmount(fallbackAmount, amountFrom(value).orElse(0));

        if (value instanceof FluidStack stack) {
            ResourceLocation id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
            return Optional.of(new FluidEntry(id.toString(), safeAmount(fallbackAmount, stack.getAmount()), fluidStackNbt(stack)));
        }
        if (value instanceof ResourceLocation id) {
            return Optional.of(new FluidEntry(id.toString(), amount, Optional.empty()));
        }
        if (value instanceof JsonObject object) {
            return fluidEntryFromJson(object, amount);
        }
        if (value instanceof Map<?, ?> map) {
            return fluidEntryFromJson(mapToJson(map), amount);
        }

        Optional<Object> nested = invoke(value, "getFluidStack", "asFluidStack", "getInternal", "getImmutableInternal", "getStack");
        if (nested.isPresent() && nested.get() != value) {
            return fluidEntry(nested.get(), amount);
        }
        nested = invoke(value, "getFluid", "fluid", "asFluid");
        if (nested.isPresent() && nested.get() != value) {
            Optional<FluidEntry> entry = fluidEntry(nested.get(), amount);
            if (entry.isPresent()) {
                return entry;
            }
        }

        Optional<String> id = idFrom(value, "getId", "id", "getFluidId", "fluidId", "getRegistryName", "registryName");
        if (id.isEmpty()) {
            id = resourceLike(value.toString());
        }
        return id.map(fluidId -> new FluidEntry(fluidId, amount, Optional.empty()));
    }

    public static Optional<ChemicalEntry> chemicalEntry(Object value, int fallbackAmount) {
        if (value == null) {
            return Optional.empty();
        }
        int amount = safeAmount(fallbackAmount, amountFrom(value).orElse(0));

        if (value instanceof ResourceLocation id) {
            return Optional.of(new ChemicalEntry(id.toString(), amount, Optional.empty()));
        }
        if (value instanceof JsonObject object) {
            return chemicalEntryFromJson(object, amount);
        }
        if (value instanceof Map<?, ?> map) {
            return chemicalEntryFromJson(mapToJson(map), amount);
        }

        Optional<Object> nested = invoke(value,
                "getChemicalStack", "asChemicalStack", "getGasStack", "asGasStack",
                "getInternal", "getImmutableInternal", "getStack");
        if (nested.isPresent() && nested.get() != value) {
            return chemicalEntry(nested.get(), amount);
        }
        nested = invoke(value, "getChemical", "chemical", "asChemical", "getGas", "gas", "asGas", "getType", "type");
        if (nested.isPresent() && nested.get() != value) {
            Optional<ChemicalEntry> entry = chemicalEntry(nested.get(), amount);
            if (entry.isPresent()) {
                return entry;
            }
        }

        Optional<String> id = idFrom(value, "getId", "id", "getChemicalId", "chemicalId",
                "getGasId", "gasId", "getRegistryName", "registryName", "getName", "name");
        if (id.isEmpty() && value instanceof CharSequence sequence && !sequence.toString().isBlank()) {
            id = Optional.of(sequence.toString());
        }
        if (id.isEmpty()) {
            id = resourceLike(value.toString());
        }
        return id.map(chemicalId -> new ChemicalEntry(chemicalId, amount, Optional.empty()));
    }

    public static JsonObject itemJson(ItemEntry entry) {
        JsonObject object = new JsonObject();
        object.addProperty("item", entry.id());
        object.addProperty("amount", entry.amount());
        entry.nbt().ifPresent(nbt -> object.add("nbt", nbt.deepCopy()));
        entry.displayNbt().ifPresent(nbt -> object.add("nbt-display", nbt.deepCopy()));
        return object;
    }

    private static Optional<ItemEntry> itemEntryFromJson(JsonObject object, int fallbackAmount) {
        Optional<String> id = stringMember(object, "item", "itemId", "item-id", "item_id", "id", "input", "ingredient");
        if (id.isEmpty()) {
            Optional<String> tag = stringMember(object, "tag");
            if (tag.isPresent()) {
                id = Optional.of(tag.get().startsWith("#") ? tag.get() : "#" + tag.get());
            }
        }
        if (id.isEmpty()) {
            Optional<String> ore = stringMember(object, "ore", "oreDict", "ore-dict", "ore_dict");
            if (ore.isPresent()) {
                id = Optional.of(ore.get().startsWith("ore:") ? ore.get() : "ore:" + ore.get());
            }
        }
        if (id.isEmpty()) {
            return Optional.empty();
        }
        int amount = safeAmount(firstInt(object, fallbackAmount, "amount", "count", "size", "quantity"), fallbackAmount);
        return Optional.of(new ItemEntry(
                id.get(),
                amount,
                jsonObjectMember(object, "nbt", "NBT", "data"),
                jsonObjectMember(object, "nbt-display", "nbtDisplay", "displayNbt", "previewNbt")
        ));
    }

    private static Optional<FluidEntry> fluidEntryFromJson(JsonObject object, int fallbackAmount) {
        Optional<String> id = stringMember(object, "fluid", "fluidId", "fluid-id", "fluid_id", "id", "input", "ingredient");
        if (id.isEmpty()) {
            return Optional.empty();
        }
        int amount = safeAmount(firstInt(object, fallbackAmount, "amount", "mb", "millibuckets", "quantity"), fallbackAmount);
        return Optional.of(new FluidEntry(id.get(), amount, jsonObjectMember(object, "nbt", "NBT", "data")));
    }

    private static Optional<ChemicalEntry> chemicalEntryFromJson(JsonObject object, int fallbackAmount) {
        Optional<String> id = stringMember(object, "chemical", "chemicalId", "chemical-id", "chemical_id",
                "gas", "gasId", "gas-id", "gas_id", "id", "input", "ingredient");
        if (id.isEmpty()) {
            return Optional.empty();
        }
        int amount = safeAmount(firstInt(object, fallbackAmount, "amount", "mb", "millibuckets", "quantity"), fallbackAmount);
        return Optional.of(new ChemicalEntry(id.get(), amount, jsonObjectMember(object, "nbt", "NBT", "data")));
    }

    private static Optional<String> blockElementFromJson(JsonObject object) {
        Optional<String> descriptor = stringMember(object,
                "element", "block", "blockId", "block-id", "block_id", "state", "id", "item", "input", "ingredient");
        if (descriptor.isEmpty()) {
            return Optional.empty();
        }
        String value = descriptor.get();
        Optional<JsonObject> properties = jsonObjectMember(object, "properties", "stateProperties", "state-properties", "state_properties");
        if (properties.isPresent() && value.indexOf('[') < 0) {
            StringJoiner joiner = new StringJoiner(",", value + "[", "]");
            for (Map.Entry<String, JsonElement> entry : properties.get().entrySet()) {
                if (!entry.getValue().isJsonNull()) {
                    joiner.add(entry.getKey() + "=" + entry.getValue().getAsString());
                }
            }
            value = joiner.toString();
        }
        Optional<Integer> meta = intFromJson(object, "meta", "metadata", "damage");
        if (meta.isPresent() && value.indexOf('@') < 0) {
            value = value + "@" + meta.get();
        }
        return value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    private static Optional<ItemEntry> itemStack(ItemStack stack, int fallbackAmount) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        int amount = fallbackAmount > 0 ? fallbackAmount : stack.getCount();
        return Optional.of(new ItemEntry(id.toString(), Math.max(1, amount), itemStackNbt(stack), Optional.empty()));
    }

    private static Optional<String> blockItemDescriptor(Item item) {
        if (item instanceof BlockItem blockItem) {
            return Optional.of(BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()).toString());
        }
        return Optional.empty();
    }

    private static String blockStateDescriptor(BlockState state) {
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        if (state.getProperties().isEmpty()) {
            return id;
        }
        StringJoiner joiner = new StringJoiner(",", id + "[", "]");
        for (Property<?> property : state.getProperties()) {
            joiner.add(property.getName() + "=" + propertyValue(state, property));
        }
        return joiner.toString();
    }

    private static <T extends Comparable<T>> String propertyValue(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private static List<Object> flatten(Object value) {
        if (value == null || value instanceof CharSequence || value instanceof JsonObject || value instanceof Map<?, ?>) {
            return value == null ? List.of() : List.of(value);
        }
        if (value instanceof JsonArray array) {
            List<Object> values = new ArrayList<>();
            array.forEach(values::add);
            return values;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> values = new ArrayList<>();
            iterable.forEach(values::add);
            return values;
        }
        if (value instanceof Collection<?> collection) {
            return new ArrayList<>(collection);
        }
        Class<?> type = value.getClass();
        if (type.isArray()) {
            int length = Array.getLength(value);
            List<Object> values = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                values.add(Array.get(value, i));
            }
            return values;
        }
        Optional<Integer> length = intFrom(invoke(value, "length", "size", "getLength"));
        Optional<Method> getter = method(value, "get");
        if (length.isPresent() && getter.isPresent()) {
            List<Object> values = new ArrayList<>(length.get());
            Method method = getter.get();
            for (int i = 0; i < length.get(); i++) {
                try {
                    values.add(method.invoke(value, i));
                } catch (ReflectiveOperationException ignored) {
                    break;
                }
            }
            if (!values.isEmpty()) {
                return values;
            }
        }
        return List.of(value);
    }

    private static Optional<Integer> amountFrom(Object value) {
        return intFrom(invoke(value, "amount", "getAmount", "count", "getCount", "size", "getSize"));
    }

    private static int safeAmount(int preferred, int fallback) {
        int value = preferred > 0 ? preferred : fallback;
        return Math.max(1, value);
    }

    private static Optional<String> idFrom(Object value, String... methods) {
        for (String method : methods) {
            Optional<Object> result = invoke(value, method);
            if (result.isPresent()) {
                Optional<String> id = resourceLike(String.valueOf(result.get()));
                if (id.isPresent()) {
                    return id;
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<String> resourceLike(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String lower = value.toLowerCase(Locale.ROOT);
        Matcher matcher = ID_PATTERN.matcher(lower);
        return matcher.find() ? Optional.of(matcher.group()) : Optional.empty();
    }

    private static Optional<Object> invoke(Object target, String... names) {
        for (String name : names) {
            Optional<Method> method = method(target, name);
            if (method.isEmpty()) {
                continue;
            }
            try {
                return Optional.ofNullable(method.get().invoke(target));
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return Optional.empty();
    }

    private static Optional<Method> method(Object target, String name) {
        if (target == null) {
            return Optional.empty();
        }
        try {
            Method method = target.getClass().getMethod(name);
            method.setAccessible(true);
            return Optional.of(method);
        } catch (ReflectiveOperationException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<Integer> intFrom(Optional<Object> value) {
        if (value.isEmpty()) {
            return Optional.empty();
        }
        Object raw = value.get();
        if (raw instanceof Number number) {
            return Optional.of(number.intValue());
        }
        try {
            return Optional.of(Integer.parseInt(String.valueOf(raw)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<String> stringMember(JsonObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key) && !object.get(key).isJsonNull()) {
                String value = object.get(key).getAsString();
                if (!value.isBlank()) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }

    private static int firstInt(JsonObject object, int fallback, String... keys) {
        for (String key : keys) {
            if (object.has(key) && !object.get(key).isJsonNull()) {
                try {
                    return object.get(key).getAsInt();
                } catch (RuntimeException ignored) {
                }
            }
        }
        return fallback;
    }

    private static Optional<Integer> intFromJson(JsonObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key) && !object.get(key).isJsonNull()) {
                try {
                    return Optional.of(object.get(key).getAsInt());
                } catch (RuntimeException ignored) {
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<JsonObject> jsonObjectMember(JsonObject object, String... keys) {
        for (String key : keys) {
            if (!object.has(key) || object.get(key).isJsonNull()) {
                continue;
            }
            JsonElement value = object.get(key);
            if (value.isJsonObject()) {
                return Optional.of(value.getAsJsonObject().deepCopy());
            }
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                return Optional.of(JsonParser.parseString(value.getAsString()).getAsJsonObject());
            }
        }
        return Optional.empty();
    }

    private static JsonObject mapToJson(Map<?, ?> map) {
        JsonObject object = new JsonObject();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                object.add(String.valueOf(entry.getKey()), jsonElement(entry.getValue()));
            }
        }
        return object;
    }

    private static JsonElement jsonElement(Object value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        if (value instanceof JsonElement element) {
            return element.deepCopy();
        }
        if (value instanceof Map<?, ?> map) {
            return mapToJson(map);
        }
        if (value instanceof Iterable<?> iterable) {
            JsonArray array = new JsonArray();
            iterable.forEach(element -> array.add(jsonElement(element)));
            return array;
        }
        if (value instanceof Number number) {
            return new com.google.gson.JsonPrimitive(number);
        }
        if (value instanceof Boolean bool) {
            return new com.google.gson.JsonPrimitive(bool);
        }
        return new com.google.gson.JsonPrimitive(String.valueOf(value));
    }

    private static Optional<JsonObject> itemStackNbt(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (stack.has(DataComponents.DAMAGE)) {
            tag.putInt("Damage", stack.getOrDefault(DataComponents.DAMAGE, 0));
        }
        return nbtToJson(tag);
    }

    private static Optional<JsonObject> fluidStackNbt(FluidStack stack) {
        return nbtToJson(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
    }

    private static Optional<JsonObject> nbtToJson(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag).getAsJsonObject());
    }

    public record ItemEntry(String id, int amount, Optional<JsonObject> nbt, Optional<JsonObject> displayNbt) {
        public ItemEntry {
            amount = Math.max(1, amount);
            nbt = nbt == null ? Optional.empty() : nbt;
            displayNbt = displayNbt == null ? Optional.empty() : displayNbt;
        }
    }

    public record FluidEntry(String id, int amount, Optional<JsonObject> nbt) {
        public FluidEntry {
            amount = Math.max(1, amount);
            nbt = nbt == null ? Optional.empty() : nbt;
        }
    }

    public record ChemicalEntry(String id, int amount, Optional<JsonObject> nbt) {
        public ChemicalEntry {
            amount = Math.max(1, amount);
            nbt = nbt == null ? Optional.empty() : nbt;
        }
    }
}
