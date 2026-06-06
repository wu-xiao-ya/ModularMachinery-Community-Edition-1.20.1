package hellfirepvp.modularmachinery.port.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MmceIngredientArrayPrimer {
    private final JsonArray items = new JsonArray();
    private JsonObject lastItem;

    public MmceIngredientArrayPrimer addIngredient(String item) {
        return addIngredient(item, 1);
    }

    public MmceIngredientArrayPrimer addIngredient(String item, int amount) {
        if (item == null || item.isBlank()) {
            return this;
        }
        JsonObject entry = new JsonObject();
        entry.addProperty("item", item);
        entry.addProperty("amount", Math.max(1, amount));
        items.add(entry);
        lastItem = entry;
        return this;
    }

    public MmceIngredientArrayPrimer addIngredients(String... inputs) {
        if (inputs != null) {
            for (String input : inputs) {
                addIngredient(input);
            }
        }
        return this;
    }

    public MmceIngredientArrayPrimer addChancedIngredient(String item, float chance) {
        addIngredient(item);
        return setChance(chance);
    }

    public MmceIngredientArrayPrimer addChancedIngredient(String item, int amount, float chance) {
        addIngredient(item, amount);
        return setChance(chance);
    }

    public MmceIngredientArrayPrimer setMinMaxAmount(int min, int max) {
        if (lastItem != null) {
            int safeMin = Math.max(1, min);
            int safeMax = Math.max(safeMin, max);
            lastItem.addProperty("minAmount", safeMin);
            lastItem.addProperty("maxAmount", safeMax);
        }
        return this;
    }

    public MmceIngredientArrayPrimer setChance(float chance) {
        if (lastItem != null) {
            lastItem.addProperty("chance", Math.max(0.0F, Math.min(1.0F, chance)));
        }
        return this;
    }

    public MmceIngredientArrayPrimer setChecker(MmceItemChecker checker) {
        return setChecker("", checker);
    }

    public MmceIngredientArrayPrimer setChecker(String checkerId, MmceItemChecker checker) {
        if (lastItem != null && checker != null) {
            lastItem.addProperty("checker-id", MmceItemCallbackRegistry.registerChecker(checkerId, checker));
        }
        return this;
    }

    public MmceIngredientArrayPrimer setNBTChecker(MmceItemChecker checker) {
        return setChecker(checker);
    }

    public MmceIngredientArrayPrimer setNBTChecker(String checkerId, MmceItemChecker checker) {
        return setChecker(checkerId, checker);
    }

    public MmceIngredientArrayPrimer setNbtChecker(MmceItemChecker checker) {
        return setChecker(checker);
    }

    public MmceIngredientArrayPrimer setNbtChecker(String checkerId, MmceItemChecker checker) {
        return setChecker(checkerId, checker);
    }

    public MmceIngredientArrayPrimer addItemModifier(MmceItemModifier modifier) {
        return addItemModifier("", modifier);
    }

    public MmceIngredientArrayPrimer addItemModifier(String modifierId, MmceItemModifier modifier) {
        if (lastItem != null && modifier != null) {
            lastItem.addProperty("item-modifier-id", MmceItemCallbackRegistry.registerModifier(modifierId, modifier));
        }
        return this;
    }

    public MmceIngredientArrayPrimer nbt(String json) {
        if (lastItem != null && json != null && !json.isBlank()) {
            lastItem.add("nbt", com.google.gson.JsonParser.parseString(json).getAsJsonObject());
        }
        return this;
    }

    public MmceIngredientArrayPrimer displayNbt(String json) {
        if (lastItem != null && json != null && !json.isBlank()) {
            lastItem.add("nbt-display", com.google.gson.JsonParser.parseString(json).getAsJsonObject());
        }
        return this;
    }

    public MmceIngredientArrayPrimer setNbt(String json) {
        return nbt(json);
    }

    public MmceIngredientArrayPrimer setNBT(String json) {
        return nbt(json);
    }

    public MmceIngredientArrayPrimer setDisplayNbt(String json) {
        return displayNbt(json);
    }

    public MmceIngredientArrayPrimer setDisplayNBT(String json) {
        return displayNbt(json);
    }

    public MmceIngredientArrayPrimer previewNbt(String json) {
        return displayNbt(json);
    }

    public MmceIngredientArrayPrimer setPreviewNbt(String json) {
        return previewNbt(json);
    }

    public MmceIngredientArrayPrimer setPreviewNBT(String json) {
        return previewNbt(json);
    }

    public MmceIngredientArrayPrimer setPreViewNBT(String json) {
        return previewNbt(json);
    }

    public MmceIngredientArrayPrimer consumeDurability(int durability) {
        if (lastItem != null) {
            lastItem.addProperty("consumeDurability", Math.max(0, durability));
        }
        return this;
    }

    public MmceIngredientArrayPrimer build() {
        return this;
    }

    public JsonArray items() {
        return items.deepCopy();
    }

    public JsonArray getIngredientStackList() {
        return items();
    }
}
