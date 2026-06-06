package hellfirepvp.modularmachinery.port.integration.kubejs;

import com.google.gson.JsonArray;
import hellfirepvp.modularmachinery.port.integration.MmceIngredientArrayPrimer;
import hellfirepvp.modularmachinery.port.integration.MmceItemChecker;
import hellfirepvp.modularmachinery.port.integration.MmceItemModifier;
import hellfirepvp.modularmachinery.port.integration.MmceScriptValues;

public final class MmceKubeJSIngredientArrayPrimer extends MmceIngredientArrayPrimer {
    @Override
    public MmceKubeJSIngredientArrayPrimer addIngredient(String item) {
        super.addIngredient(item);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer addIngredient(String item, int amount) {
        super.addIngredient(item, amount);
        return this;
    }

    public MmceKubeJSIngredientArrayPrimer addIngredient(Object input) {
        for (MmceScriptValues.ItemEntry entry : MmceScriptValues.itemEntries(input, 1)) {
            addIngredient(entry);
        }
        return this;
    }

    public MmceKubeJSIngredientArrayPrimer addIngredient(Object input, int amount) {
        for (MmceScriptValues.ItemEntry entry : MmceScriptValues.itemEntries(input, Math.max(1, amount))) {
            addIngredient(entry);
        }
        return this;
    }

    public MmceKubeJSIngredientArrayPrimer addIngredients(Object input) {
        return addIngredient(input);
    }

    public MmceKubeJSIngredientArrayPrimer addIngredients(Object... inputs) {
        if (inputs != null) {
            for (Object input : inputs) {
                addIngredient(input);
            }
        }
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer addIngredients(String... inputs) {
        super.addIngredients(inputs);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer addChancedIngredient(String item, float chance) {
        super.addChancedIngredient(item, chance);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer addChancedIngredient(String item, int amount, float chance) {
        super.addChancedIngredient(item, amount, chance);
        return this;
    }

    public MmceKubeJSIngredientArrayPrimer addChancedIngredient(Object input, float chance) {
        for (MmceScriptValues.ItemEntry entry : MmceScriptValues.itemEntries(input, 1)) {
            addIngredient(entry);
            setChance(chance);
        }
        return this;
    }

    public MmceKubeJSIngredientArrayPrimer addChancedIngredient(Object input, double chance) {
        return addChancedIngredient(input, (float) chance);
    }

    public MmceKubeJSIngredientArrayPrimer addChancedIngredient(Object input, int amount, float chance) {
        for (MmceScriptValues.ItemEntry entry : MmceScriptValues.itemEntries(input, Math.max(1, amount))) {
            addIngredient(entry);
            setChance(chance);
        }
        return this;
    }

    public MmceKubeJSIngredientArrayPrimer addChancedIngredient(Object input, int amount, double chance) {
        return addChancedIngredient(input, amount, (float) chance);
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setMinMaxAmount(int min, int max) {
        super.setMinMaxAmount(min, max);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setChance(float chance) {
        super.setChance(chance);
        return this;
    }

    public MmceKubeJSIngredientArrayPrimer setChance(double chance) {
        return setChance((float) chance);
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setChecker(MmceItemChecker checker) {
        super.setChecker(checker);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setChecker(String checkerId, MmceItemChecker checker) {
        super.setChecker(checkerId, checker);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setNBTChecker(MmceItemChecker checker) {
        super.setNBTChecker(checker);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setNBTChecker(String checkerId, MmceItemChecker checker) {
        super.setNBTChecker(checkerId, checker);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setNbtChecker(MmceItemChecker checker) {
        super.setNbtChecker(checker);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setNbtChecker(String checkerId, MmceItemChecker checker) {
        super.setNbtChecker(checkerId, checker);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer addItemModifier(MmceItemModifier modifier) {
        super.addItemModifier(modifier);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer addItemModifier(String modifierId, MmceItemModifier modifier) {
        super.addItemModifier(modifierId, modifier);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer nbt(String json) {
        super.nbt(json);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer displayNbt(String json) {
        super.displayNbt(json);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setNbt(String json) {
        super.setNbt(json);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setNBT(String json) {
        super.setNBT(json);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setDisplayNbt(String json) {
        super.setDisplayNbt(json);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setDisplayNBT(String json) {
        super.setDisplayNBT(json);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer previewNbt(String json) {
        super.previewNbt(json);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setPreviewNbt(String json) {
        super.setPreviewNbt(json);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setPreviewNBT(String json) {
        super.setPreviewNBT(json);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer setPreViewNBT(String json) {
        super.setPreViewNBT(json);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer consumeDurability(int durability) {
        super.consumeDurability(durability);
        return this;
    }

    @Override
    public MmceKubeJSIngredientArrayPrimer build() {
        return this;
    }

    @Override
    public JsonArray items() {
        return super.items();
    }

    @Override
    public JsonArray getIngredientStackList() {
        return super.getIngredientStackList();
    }

    private void addIngredient(MmceScriptValues.ItemEntry entry) {
        super.addIngredient(entry.id(), entry.amount());
        entry.nbt().ifPresent(nbt -> super.nbt(nbt.toString()));
        entry.displayNbt().ifPresent(nbt -> super.displayNbt(nbt.toString()));
    }
}
