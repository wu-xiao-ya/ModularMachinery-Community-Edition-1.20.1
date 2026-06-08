package hellfirepvp.modularmachinery.port.integration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hellfirepvp.modularmachinery.port.blockentity.UpgradeBusBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceNbtCompat;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class MmceMachineUpgrade {
    private final ResourceLocation id;
    private final String localizedName;
    private final float level;
    private final int maxStack;
    private final int stackSize;
    private ItemStack stack;
    private final CompoundTag data;
    private final UpgradeBusBlockEntity parentBus;
    private final int busSlot;
    private boolean changed;

    MmceMachineUpgrade(ResourceLocation id, String localizedName, float level, int maxStack, int stackSize,
                       ItemStack stack, CompoundTag data, UpgradeBusBlockEntity parentBus, int busSlot) {
        this.id = id;
        this.localizedName = localizedName == null || localizedName.isBlank() ? id.toString() : localizedName;
        this.level = level;
        this.maxStack = Math.max(1, maxStack);
        this.stackSize = Math.max(1, stackSize);
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.data = data == null ? new CompoundTag() : data.copy();
        this.parentBus = parentBus;
        this.busSlot = busSlot;
    }

    static MmceMachineUpgrade registered(ResourceLocation id, CompoundTag data) {
        return from(id, ItemStack.EMPTY, 1, data, null, -1);
    }

    static MmceMachineUpgrade from(ResourceLocation id, ItemStack stack, int stackSize, CompoundTag data) {
        return from(id, stack, stackSize, data, null, -1);
    }

    static MmceMachineUpgrade from(ResourceLocation id, ItemStack stack, int stackSize, CompoundTag data,
                                   UpgradeBusBlockEntity parentBus, int busSlot) {
        CompoundTag upgrade = data != null && data.contains("mmce_upgrade", net.minecraft.nbt.Tag.TAG_COMPOUND)
                ? data.getCompound("mmce_upgrade")
                : new CompoundTag();
        String localizedName = upgrade.getString("localizedName");
        float level = upgrade.contains("level", net.minecraft.nbt.Tag.TAG_ANY_NUMERIC) ? upgrade.getFloat("level") : 0.0F;
        int maxStack = upgrade.contains("maxStack", net.minecraft.nbt.Tag.TAG_ANY_NUMERIC) ? upgrade.getInt("maxStack") : Math.max(1, stackSize);
        return new MmceMachineUpgrade(id, localizedName, level, maxStack, stackSize, stack, data, parentBus, busSlot);
    }

    public String getName() {
        return id.toString();
    }

    public ResourceLocation id() {
        return id;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public float getLevel() {
        return level;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public int getStackSize() {
        return stackSize;
    }

    public ItemStack getStack() {
        return stack.copy();
    }

    public CompoundTag data() {
        return data.copy();
    }

    public void readRuntimeData(CompoundTag runtimeData) {
        CompoundTag runtime = runtimeData == null ? new CompoundTag() : runtimeData.copy();
        if (runtime.isEmpty() && !data.contains("customData")) {
            data.put("customData", new CompoundTag());
        } else if (!runtime.isEmpty()) {
            data.put("customData", runtime);
        }
    }

    public CompoundTag writeRuntimeData() {
        return data.getCompound("customData").copy();
    }

    void loadRuntimeData() {
        if (parentBus != null) {
            readRuntimeData(parentBus.getUpgradeCustomData(id, busSlot));
        }
    }

    public void writeBack() {
        if (parentBus == null || busSlot < 0 || busSlot >= parentBus.getContainerSize()) {
            return;
        }

        parentBus.setUpgradeCustomData(id, busSlot, writeRuntimeData());
        if (stack.isEmpty()) {
            return;
        }

        ItemStack liveStack = parentBus.getItem(busSlot);
        if (liveStack.isEmpty()) {
            return;
        }
        CompoundTag liveData = liveStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag updated = data.copy();
        CompoundTag runtime = writeRuntimeData();
        if (runtime.isEmpty()) {
            updated.remove("customData");
        } else {
            updated.put("customData", runtime);
        }
        mergeKnownUpgradeTags(liveData, updated);
        ItemStack replacement = stack.copy();
        replacement.setCount(liveStack.getCount());
        if (replacement.isDamageableItem() && replacement.getDamageValue() >= replacement.getMaxDamage()) {
            replacement.shrink(1);
        }
        if (replacement.isEmpty()) {
            parentBus.setItem(busSlot, ItemStack.EMPTY);
            changed = false;
            return;
        }
        if (liveData.isEmpty()) {
            replacement.remove(DataComponents.CUSTOM_DATA);
        } else {
            replacement.set(DataComponents.CUSTOM_DATA, CustomData.of(liveData));
        }
        parentBus.setItem(busSlot, replacement);
        changed = false;
    }

    public String getItemData() {
        return data.getCompound("itemData").toString();
    }

    public void setItemData(CompoundTag itemData) {
        setData("itemData", itemData);
    }

    public void setItemData(Object itemData) {
        setData("itemData", itemData);
    }

    public String getCustomData() {
        return data.getCompound("customData").toString();
    }

    public void setCustomData(CompoundTag customData) {
        setData("customData", customData);
    }

    public void setCustomData(Object customData) {
        setData("customData", customData);
    }

    public ItemStack getParentStack() {
        return getStack();
    }

    public String[] getDescriptions() {
        return MmceMachineUpgradeRegistry.descriptions(this, false, staticDescriptions()).toArray(String[]::new);
    }

    public String[] getBusGUIDescriptions() {
        return MmceMachineUpgradeRegistry.descriptions(this, true, staticDescriptions()).toArray(String[]::new);
    }

    public String[] getBusGuiDescriptions() {
        return getBusGUIDescriptions();
    }

    public void decrementItemDurability(int durability) {
        if (stack.isDamageableItem()) {
            stack.setDamageValue(Math.min(stack.getMaxDamage(), stack.getDamageValue() + Math.max(0, durability)));
            changed = true;
        }
    }

    private void setData(String key, Object value) {
        if (value == null) {
            data.remove(key);
            changed = true;
            return;
        }
        data.put(key, toCompound(value));
        changed = true;
    }

    private static CompoundTag toCompound(Object value) {
        if (value instanceof CompoundTag tag) {
            return tag.copy();
        }
        Optional<JsonObject> object = jsonObject(value);
        if (object.isPresent()) {
            return MmceNbtCompat.toTag(object.get());
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("value", String.valueOf(value));
        return tag;
    }

    private static Optional<JsonObject> jsonObject(Object value) {
        if (value instanceof JsonObject object) {
            return Optional.of(object.deepCopy());
        }
        if (value instanceof JsonElement element && element.isJsonObject()) {
            return Optional.of(element.getAsJsonObject().deepCopy());
        }
        if (value instanceof Map<?, ?> map) {
            return Optional.of(mapToJson(map));
        }
        Optional<Object> nested = invoke(value, "toJson", "asJson", "getJson");
        if (nested.isPresent() && nested.get() != value) {
            Optional<JsonObject> object = jsonObject(nested.get());
            if (object.isPresent()) {
                return object;
            }
        }
        if (value instanceof CharSequence sequence) {
            return parseJsonObject(sequence.toString());
        }
        try {
            return parseJsonObject(String.valueOf(value));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<JsonObject> parseJsonObject(String value) {
        String json = value == null ? "" : value.trim();
        if (json.isEmpty() || !json.startsWith("{") || !json.endsWith("}")) {
            return Optional.empty();
        }
        JsonElement parsed = JsonParser.parseString(json);
        return parsed.isJsonObject() ? Optional.of(parsed.getAsJsonObject().deepCopy()) : Optional.empty();
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
            return com.google.gson.JsonNull.INSTANCE;
        }
        if (value instanceof JsonElement element) {
            return element.deepCopy();
        }
        if (value instanceof Map<?, ?> map) {
            return mapToJson(map);
        }
        if (value instanceof Iterable<?> iterable) {
            com.google.gson.JsonArray array = new com.google.gson.JsonArray();
            iterable.forEach(entry -> array.add(jsonElement(entry)));
            return array;
        }
        if (value.getClass().isArray()) {
            com.google.gson.JsonArray array = new com.google.gson.JsonArray();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                array.add(jsonElement(Array.get(value, i)));
            }
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

    private static Optional<Object> invoke(Object target, String... names) {
        if (target == null) {
            return Optional.empty();
        }
        for (String name : names) {
            try {
                Method method = target.getClass().getMethod(name);
                method.setAccessible(true);
                return Optional.ofNullable(method.invoke(target));
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return Optional.empty();
    }

    private static void mergeKnownUpgradeTags(CompoundTag liveData, CompoundTag updated) {
        for (String key : List.of("mmce_upgrade", "mmce_modifiers", "compatibleMachines", "incompatibleMachines", "descriptions", "itemData", "customData")) {
            if (updated.contains(key)) {
                liveData.put(key, updated.get(key).copy());
            } else {
                liveData.remove(key);
            }
        }
    }

    private List<String> staticDescriptions() {
        if (!data.contains("descriptions", Tag.TAG_LIST)) {
            return List.of();
        }
        ListTag tags = data.getList("descriptions", Tag.TAG_STRING);
        List<String> out = new java.util.ArrayList<>(tags.size());
        for (int i = 0; i < tags.size(); i++) {
            String value = tags.getString(i);
            if (!value.isBlank()) {
                out.add(value);
            }
        }
        return out;
    }
}
