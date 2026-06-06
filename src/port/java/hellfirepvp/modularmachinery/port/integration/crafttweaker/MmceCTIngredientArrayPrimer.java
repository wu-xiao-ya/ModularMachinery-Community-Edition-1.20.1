package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.integration.MmceIngredientArrayPrimer;
import hellfirepvp.modularmachinery.port.integration.MmceItemChecker;
import hellfirepvp.modularmachinery.port.integration.MmceItemModifier;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.IngredientArrayPrimer")
public final class MmceCTIngredientArrayPrimer extends MmceIngredientArrayPrimer {
    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer addIngredient(String item) {
        super.addIngredient(item);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer addIngredient(String item, int amount) {
        super.addIngredient(item, amount);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTIngredientArrayPrimer addIngredient(IItemStack stack) {
        return addStack(stack, 0);
    }

    @ZenCodeType.Method
    public MmceCTIngredientArrayPrimer addIngredient(IIngredient ingredient) {
        return addIngredient(ingredient, 0, null);
    }

    @ZenCodeType.Method
    public MmceCTIngredientArrayPrimer addIngredient(IIngredient ingredient, int amount) {
        return addIngredient(ingredient, Math.max(1, amount), null);
    }

    @ZenCodeType.Method
    public MmceCTIngredientArrayPrimer addIngredient(IIngredientWithAmount ingredient) {
        if (ingredient != null && ingredient.ingredient() != null) {
            addIngredient(ingredient.ingredient(), ingredient.amount(), null);
        }
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer addIngredients(String... inputs) {
        super.addIngredients(inputs);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTIngredientArrayPrimer addIngredients(IItemStack... inputs) {
        if (inputs != null) {
            for (IItemStack input : inputs) {
                addIngredient(input);
            }
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTIngredientArrayPrimer addIngredients(IIngredient... inputs) {
        if (inputs != null) {
            for (IIngredient input : inputs) {
                addIngredient(input);
            }
        }
        return this;
    }

    @ZenCodeType.Method
    public MmceCTIngredientArrayPrimer addIngredients(IIngredientWithAmount... inputs) {
        if (inputs != null) {
            for (IIngredientWithAmount input : inputs) {
                addIngredient(input);
            }
        }
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer addChancedIngredient(String item, float chance) {
        super.addChancedIngredient(item, chance);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer addChancedIngredient(String item, int amount, float chance) {
        super.addChancedIngredient(item, amount, chance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTIngredientArrayPrimer addChancedIngredient(IItemStack stack, float chance) {
        addIngredient(stack);
        return setChance(chance);
    }

    @ZenCodeType.Method
    public MmceCTIngredientArrayPrimer addChancedIngredient(IIngredient ingredient, float chance) {
        return addIngredient(ingredient, 0, chance);
    }

    @ZenCodeType.Method
    public MmceCTIngredientArrayPrimer addChancedIngredient(IIngredient ingredient, int amount, float chance) {
        return addIngredient(ingredient, Math.max(1, amount), chance);
    }

    @ZenCodeType.Method
    public MmceCTIngredientArrayPrimer addChancedIngredient(IIngredientWithAmount ingredient, float chance) {
        if (ingredient != null && ingredient.ingredient() != null) {
            addIngredient(ingredient.ingredient(), ingredient.amount(), chance);
        }
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setMinMaxAmount(int min, int max) {
        super.setMinMaxAmount(min, max);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setChance(float chance) {
        super.setChance(chance);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTIngredientArrayPrimer setChance(double chance) {
        return setChance((float) chance);
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setChecker(MmceItemChecker checker) {
        super.setChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setChecker(String checkerId, MmceItemChecker checker) {
        super.setChecker(checkerId, checker);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setNBTChecker(MmceItemChecker checker) {
        super.setNBTChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setNBTChecker(String checkerId, MmceItemChecker checker) {
        super.setNBTChecker(checkerId, checker);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setNbtChecker(MmceItemChecker checker) {
        super.setNbtChecker(checker);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setNbtChecker(String checkerId, MmceItemChecker checker) {
        super.setNbtChecker(checkerId, checker);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer addItemModifier(MmceItemModifier modifier) {
        super.addItemModifier(modifier);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer addItemModifier(String modifierId, MmceItemModifier modifier) {
        super.addItemModifier(modifierId, modifier);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer nbt(String json) {
        super.nbt(json);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setNbt(String json) {
        super.setNbt(json);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setNBT(String json) {
        super.setNBT(json);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer displayNbt(String json) {
        super.displayNbt(json);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setDisplayNbt(String json) {
        super.setDisplayNbt(json);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setDisplayNBT(String json) {
        super.setDisplayNBT(json);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer previewNbt(String json) {
        super.previewNbt(json);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setPreViewNBT(String json) {
        super.setPreViewNBT(json);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setPreviewNbt(String json) {
        super.setPreviewNbt(json);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer setPreviewNBT(String json) {
        super.setPreviewNBT(json);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer consumeDurability(int durability) {
        super.consumeDurability(durability);
        return this;
    }

    @ZenCodeType.Method
    @Override
    public MmceCTIngredientArrayPrimer build() {
        return this;
    }

    @ZenCodeType.Method
    @Override
    public JsonArray getIngredientStackList() {
        return super.getIngredientStackList();
    }

    private MmceCTIngredientArrayPrimer addIngredient(IIngredient ingredient, int amountOverride, Float chance) {
        if (ingredient == null || ingredient.isEmpty()) {
            return this;
        }
        for (IItemStack stack : ingredient.getItems()) {
            addStack(stack, amountOverride);
            if (chance != null) {
                setChance(chance);
            }
        }
        return this;
    }

    private MmceCTIngredientArrayPrimer addStack(IItemStack stack, int amountOverride) {
        if (stack == null || stack.isEmpty()) {
            return this;
        }
        int amount = amountOverride > 0 ? amountOverride : MmceCTRecipeBuilder.itemAmount(stack);
        super.addIngredient(MmceCTRecipeBuilder.itemId(stack), amount);
        JsonObject nbt = MmceCTRecipeBuilder.itemStackNbt(stack.getInternal());
        if (!nbt.isEmpty()) {
            super.nbt(nbt.toString());
        }
        return this;
    }
}
