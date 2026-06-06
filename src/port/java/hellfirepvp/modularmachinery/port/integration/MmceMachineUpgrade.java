package hellfirepvp.modularmachinery.port.integration;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.visitor.DataToJsonStringVisitor;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hellfirepvp.modularmachinery.port.blockentity.UpgradeBusBlockEntity;
import hellfirepvp.modularmachinery.port.data.MmceNbtCompat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.openzen.zencode.java.ZenCodeType;

import java.util.List;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.MachineUpgrade")
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

    @ZenCodeType.Getter("name")
    public String getName() {
        return id.toString();
    }

    public ResourceLocation id() {
        return id;
    }

    @ZenCodeType.Getter("localizedName")
    public String getLocalizedName() {
        return localizedName;
    }

    @ZenCodeType.Getter("level")
    public float getLevel() {
        return level;
    }

    @ZenCodeType.Getter("maxStack")
    public int getMaxStack() {
        return maxStack;
    }

    @ZenCodeType.Getter("stackSize")
    public int getStackSize() {
        return stackSize;
    }

    @ZenCodeType.Getter("stack")
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

    @ZenCodeType.Getter("itemData")
    public String getItemData() {
        return data.getCompound("itemData").toString();
    }

    @ZenCodeType.Setter("itemData")
    public void setItemData(IData itemData) {
        setData("itemData", itemData);
    }

    @ZenCodeType.Getter("customData")
    public String getCustomData() {
        return data.getCompound("customData").toString();
    }

    @ZenCodeType.Setter("customData")
    public void setCustomData(IData customData) {
        setData("customData", customData);
    }

    @ZenCodeType.Getter("parentStack")
    public ItemStack getParentStack() {
        return getStack();
    }

    @ZenCodeType.Getter("descriptions")
    public String[] getDescriptions() {
        return MmceMachineUpgradeRegistry.descriptions(this, false, staticDescriptions()).toArray(String[]::new);
    }

    @ZenCodeType.Getter("busGUIDescriptions")
    public String[] getBusGUIDescriptions() {
        return MmceMachineUpgradeRegistry.descriptions(this, true, staticDescriptions()).toArray(String[]::new);
    }

    @ZenCodeType.Getter("busGuiDescriptions")
    public String[] getBusGuiDescriptions() {
        return getBusGUIDescriptions();
    }

    @ZenCodeType.Method
    public void decrementItemDurability(int durability) {
        if (stack.isDamageableItem()) {
            stack.setDamageValue(Math.min(stack.getMaxDamage(), stack.getDamageValue() + Math.max(0, durability)));
            changed = true;
        }
    }

    private void setData(String key, IData value) {
        if (value == null) {
            data.remove(key);
            changed = true;
            return;
        }
        data.put(key, toCompound(value));
        changed = true;
    }

    private static CompoundTag toCompound(IData value) {
        try {
            JsonObject object = JsonParser.parseString(value.accept(DataToJsonStringVisitor.INSTANCE)).getAsJsonObject();
            return MmceNbtCompat.toTag(object);
        } catch (RuntimeException ignored) {
            CompoundTag tag = new CompoundTag();
            tag.putString("value", value.accept(DataToJsonStringVisitor.INSTANCE));
            return tag;
        }
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
