package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.blockentity.MachineControllerBlockEntity;
import hellfirepvp.modularmachinery.port.integration.MmceItemCallbackRegistry;
import hellfirepvp.modularmachinery.port.integration.MmceItemChecker;
import hellfirepvp.modularmachinery.port.integration.MmceItemModifier;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record MmceItemRequirement(
        MmceIoType ioType,
        Optional<ResourceLocation> itemId,
        Optional<String> legacyOreName,
        Optional<TagKey<Item>> itemTag,
        List<ResourceLocation> fallbackItemIds,
        int amount,
        int minAmount,
        int maxAmount,
        int metadata,
        float chance,
        JsonObject matchNbt,
        JsonObject displayNbt,
        Optional<String> checkerId,
        Optional<String> itemModifierId,
        int durabilityCost,
        int triggerTime,
        boolean triggerRepeatable,
        boolean ignoreOutputCheck,
        boolean parallelizeUnaffected,
        boolean returnCraftingRemainder
) implements MmceParsedRequirement {
    public MmceItemRequirement {
        itemId = itemId == null ? Optional.empty() : itemId;
        legacyOreName = legacyOreName == null ? Optional.empty() : legacyOreName;
        itemTag = itemTag == null ? Optional.empty() : itemTag;
        fallbackItemIds = fallbackItemIds == null ? List.of() : List.copyOf(fallbackItemIds);
        amount = Math.max(1, Math.min(64, amount));
        minAmount = Math.max(1, Math.min(64, minAmount));
        maxAmount = Math.max(minAmount, Math.min(64, maxAmount));
        metadata = Math.max(0, metadata);
        chance = Math.max(0.0F, Math.min(1.0F, chance));
        matchNbt = matchNbt == null ? new JsonObject() : matchNbt.deepCopy();
        displayNbt = displayNbt == null ? new JsonObject() : displayNbt.deepCopy();
        checkerId = checkerId == null ? Optional.empty() : checkerId.filter(id -> !id.isBlank());
        itemModifierId = itemModifierId == null ? Optional.empty() : itemModifierId.filter(id -> !id.isBlank());
        durabilityCost = Math.max(0, durabilityCost);
        triggerTime = Math.max(0, triggerTime);
    }

    public boolean matches(ItemStack stack) {
        return matches(null, stack);
    }

    public boolean matches(MachineControllerBlockEntity controller, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        ResourceLocation stackId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        boolean itemMatches;
        if (itemId.isPresent()) {
            itemMatches = itemId.get().equals(stackId) && MmceNbtCompat.matches(stack, matchNbt);
        } else if (itemTag.isPresent() && stack.is(itemTag.get())) {
            itemMatches = MmceNbtCompat.matches(stack, matchNbt);
        } else {
            itemMatches = fallbackItemIds.contains(stackId) && MmceNbtCompat.matches(stack, matchNbt);
        }
        if (!itemMatches) {
            return false;
        }
        if (!matchesLegacyMetadata(stack)) {
            return false;
        }
        if (checkerId.isEmpty()) {
            return true;
        }
        MmceItemChecker checker = MmceItemCallbackRegistry.checker(checkerId.get());
        return checker != null && checker.isMatch(controller, stack);
    }

    public ItemStack createStack(int stackAmount) {
        return createStack(null, stackAmount);
    }

    public ItemStack createStack(MachineControllerBlockEntity controller, int stackAmount) {
        Optional<ResourceLocation> outputId = itemId.or(() -> fallbackItemIds.stream().findFirst());
        if (outputId.isEmpty() || stackAmount <= 0) {
            return ItemStack.EMPTY;
        }

        Item item = BuiltInRegistries.ITEM.get(outputId.get());
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, Math.min(stackAmount, item.getDefaultMaxStackSize()));
        applyLegacyMetadata(stack);
        MmceNbtCompat.apply(stack, matchNbt);
        if (itemModifierId.isEmpty()) {
            return stack;
        }
        MmceItemModifier modifier = MmceItemCallbackRegistry.modifier(itemModifierId.get());
        if (modifier == null) {
            return stack;
        }
        ItemStack modified = modifier.apply(controller, stack.copy());
        return modified == null ? ItemStack.EMPTY : modified;
    }

    private boolean matchesLegacyMetadata(ItemStack stack) {
        if (metadata <= 0 || matchNbt.has("Damage") || !stack.has(DataComponents.DAMAGE)) {
            return true;
        }
        return stack.getDamageValue() == metadata;
    }

    private void applyLegacyMetadata(ItemStack stack) {
        if (metadata > 0 && !matchNbt.has("Damage") && stack.isDamageableItem()) {
            stack.setDamageValue(metadata);
        }
    }
}
