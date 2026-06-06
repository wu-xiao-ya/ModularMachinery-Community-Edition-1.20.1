package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;

public record MmceRecipeRequirement(
        ResourceLocation type,
        Optional<MmceIoType> ioType,
        Optional<String> selectorTag,
        Optional<MmceParsedRequirement> parsed,
        Optional<String> parseIssue,
        JsonObject rawJson
) {
    static MmceRecipeRequirement parse(JsonObject object) {
        ResourceLocation type = normalizeType(MmceJsonUtil.modId(GsonHelper.getAsString(object, "type")));
        Optional<MmceIoType> ioType = MmceIoType.byName(
                        MmceJsonUtil.optionalString(object, "io-type", "ioType", "io_type", "io").orElse(""))
                .or(() -> defaultIoType(type));
        return new MmceRecipeRequirement(
                type,
                ioType,
                MmceJsonUtil.optionalString(object, "selector-tag", "selectorTag", "selector_tag")
                        .filter(tag -> !tag.isBlank()),
                parseTyped(type, ioType, object),
                parseIssue(type, ioType, object),
                object.deepCopy()
        );
    }

    private static Optional<MmceIoType> defaultIoType(ResourceLocation type) {
        if (!"modularmachinery".equals(type.getNamespace())) {
            return Optional.empty();
        }
        return switch (type.getPath()) {
            case "fuel", "fuel_item", "fuel_item_input", "item_durability", "ingredient_array_input", "catalyst",
                    "interface_number_input", "smart_interface_number_input" -> Optional.of(MmceIoType.INPUT);
            case "ingredient_array_output", "random_item_output" -> Optional.of(MmceIoType.OUTPUT);
            default -> Optional.empty();
        };
    }

    private static Optional<String> parseIssue(ResourceLocation type, Optional<MmceIoType> ioType, JsonObject object) {
        if (!"modularmachinery".equals(type.getNamespace())) {
            return Optional.of("external requirement type is not executable by the built-in port parser");
        }
        if (ioType.isEmpty()) {
            return Optional.of("missing or invalid io-type");
        }
        return switch (type.getPath()) {
            case "fuel", "fuel_item", "fuel_item_input" -> ioType.get() != MmceIoType.INPUT
                    ? Optional.of("fuel requirements must be input")
                    : requirePositiveInt(object, "missing or non-positive burn time",
                    "time", "burnTime", "burn-time", "burn_time", "requiredTotalBurnTime");
            case "item" -> hasAnyString(object, "item", "itemId", "item-id", "item_id")
                    || hasAnyString(object, "ore", "oreDict", "ore-dict", "ore_dict", "tag")
                    ? Optional.empty()
                    : Optional.of("missing item");
            case "item_durability" -> {
                boolean hasItem = hasAnyString(object, "item", "itemId", "item-id", "item_id")
                        || hasAnyString(object, "ore", "oreDict", "ore-dict", "ore_dict", "tag");
                if (!hasItem) {
                    yield Optional.of("missing item");
                }
                yield readDurabilityCost(object) > 0 ? Optional.empty() : Optional.of("missing or non-positive durability cost");
            }
            case "ingredient_array_input", "ingredient_array_output", "ingredient_array", "random_item_output", "catalyst" ->
                    hasItems(object) ? Optional.empty() : Optional.of("missing item/items");
            case "fluid", "fluid_pertick", "fluid_per_tick" -> hasAnyString(object, "fluid")
                    || hasAnyString(object, "fluidName", "fluid-name", "fluid_name", "fluidId", "fluid-id", "fluid_id")
                    ? requirePositiveInt(object, "missing or non-positive amount", "amount", "mb", "millibuckets", "quantity")
                    : Optional.of("missing fluid");
            case "gas", "chemical", "gas_pertick", "gas_per_tick", "chemical_pertick", "chemical_per_tick" ->
                    hasAnyString(object, "chemical", "gas", "fluid")
                            || hasAnyString(object, "chemicalName", "chemical-name", "chemical_name", "chemicalId", "chemical-id", "chemical_id",
                            "gasName", "gas-name", "gas_name", "gasId", "gas-id", "gas_id", "fluidName", "fluid-name", "fluid_name", "fluidId", "fluid-id", "fluid_id")
                            ? requirePositiveInt(object, "missing or non-positive amount", "amount", "mb", "millibuckets", "quantity")
                            : Optional.of("missing chemical/gas");
            case "energy" -> requirePositiveLong(object, "missing or non-positive energy",
                    "energyPerTick", "energy-per-tick", "energy_per_tick", "energy", "amount");
            case "interface_number_input", "smart_interface_number_input" ->
                    ioType.get() != MmceIoType.INPUT ? Optional.of("smart interface requirements must be input") :
                    hasAnyString(object, "interfaceType", "interface-type", "interface_type", "smartInterfaceType",
                            "smart-interface-type", "smart_interface_type", "interface", "type", "name")
                            ? Optional.empty()
                            : Optional.of("missing interface type");
            default -> Optional.of("unsupported requirement type");
        };
    }

    private static Optional<MmceParsedRequirement> parseTyped(ResourceLocation type, Optional<MmceIoType> ioType, JsonObject object) {
        if (ioType.isEmpty() || !"modularmachinery".equals(type.getNamespace())) {
            return Optional.empty();
        }

        return switch (type.getPath()) {
            case "fuel", "fuel_item", "fuel_item_input" -> parseFuel(ioType.get(), object).map(requirement -> (MmceParsedRequirement) requirement);
            case "item", "item_durability" -> parseItemOrFuel(ioType.get(), object);
            case "ingredient_array_input", "ingredient_array_output", "ingredient_array", "random_item_output" -> parseIngredientArray(ioType.get(), object, false).map(requirement -> (MmceParsedRequirement) requirement);
            case "catalyst" -> parseIngredientArray(ioType.get(), object, true).map(requirement -> (MmceParsedRequirement) requirement);
            case "fluid" -> parseFluid(ioType.get(), object, false).map(requirement -> (MmceParsedRequirement) requirement);
            case "fluid_pertick", "fluid_per_tick" -> parseFluid(ioType.get(), object, true).map(requirement -> (MmceParsedRequirement) requirement);
            case "gas", "chemical" -> parseChemical(ioType.get(), object, false).map(requirement -> (MmceParsedRequirement) requirement);
            case "gas_pertick", "gas_per_tick", "chemical_pertick", "chemical_per_tick" -> parseChemical(ioType.get(), object, true).map(requirement -> (MmceParsedRequirement) requirement);
            case "energy" -> parseEnergy(ioType.get(), object).map(requirement -> (MmceParsedRequirement) requirement);
            case "interface_number_input", "smart_interface_number_input" -> parseSmartInterface(ioType.get(), object).map(requirement -> (MmceParsedRequirement) requirement);
            default -> Optional.empty();
        };
    }

    private static Optional<MmceItemRequirement> parseItem(MmceIoType ioType, JsonObject object) {
        Optional<String> item = MmceJsonUtil.optionalString(object, "item", "itemId", "item-id", "item_id")
                .filter(value -> !value.isBlank());
        Optional<String> legacyOre = MmceJsonUtil.optionalString(object, "ore", "oreDict", "ore-dict", "ore_dict")
                .filter(value -> !value.isBlank());
        Optional<String> tag = MmceJsonUtil.optionalString(object, "tag")
                .filter(value -> !value.isBlank());
        if (item.isEmpty()) {
            if (legacyOre.isPresent()) {
                item = Optional.of(legacyOre.get().startsWith("ore:") ? legacyOre.get() : "ore:" + legacyOre.get());
            } else if (tag.isPresent()) {
                item = Optional.of(tag.get().startsWith("#") ? tag.get() : "#" + tag.get());
            } else {
                return Optional.empty();
            }
        }
        if (isFuelItemDefinition(item.get())) {
            return Optional.empty();
        }

        String itemDefinition = item.get();
        int metadata = 0;
        int metadataIndex = itemDefinition.indexOf('@');
        if (metadataIndex >= 0 && metadataIndex < itemDefinition.length() - 1) {
            metadata = parsePositiveInt(itemDefinition.substring(metadataIndex + 1));
            itemDefinition = itemDefinition.substring(0, metadataIndex);
        }

        Optional<ResourceLocation> itemId = Optional.empty();
        Optional<String> legacyOreName = Optional.empty();
        Optional<TagKey<Item>> itemTag = Optional.empty();
        List<ResourceLocation> fallbackItemIds = List.of();

        if (itemDefinition.startsWith("ore:")) {
            String oreName = itemDefinition.substring("ore:".length());
            legacyOreName = Optional.of(oreName);
            itemTag = MmceLegacyOreTags.itemTag(oreName);
            fallbackItemIds = MmceLegacyOreTags.fallbackItems(oreName);
        } else if (itemDefinition.startsWith("#")) {
            itemTag = Optional.of(TagKey.create(Registries.ITEM, ResourceLocation.parse(itemDefinition.substring(1))));
        } else {
            itemId = Optional.of(MmceJsonUtil.id(itemDefinition));
        }

        int amount = readFirstInt(object, 1, "amount", "count", "size", "quantity");
        int minAmount = readFirstInt(object, amount, "minAmount", "min-amount", "min_amount", "min");
        int maxAmount = readFirstInt(object, amount, "maxAmount", "max-amount", "max_amount", "max");

        return Optional.of(new MmceItemRequirement(
                ioType,
                itemId,
                legacyOreName,
                itemTag,
                fallbackItemIds,
                amount,
                minAmount,
                maxAmount,
                metadata,
                readChance(object),
                MmceJsonUtil.rawObject(object, "nbt"),
                MmceJsonUtil.rawObject(object, "nbt-display", "nbtDisplay", "nbt_display"),
                readFirstString(object, "checker-id", "checkerId", "item-checker", "itemChecker").filter(value -> !value.isBlank()),
                readFirstString(object, "item-modifier-id", "itemModifierId", "item-modifier", "itemModifier").filter(value -> !value.isBlank()),
                readDurabilityCost(object),
                readTriggerTime(object),
                readTriggerRepeatable(object),
                readIgnoreOutputCheck(object),
                readParallelizeUnaffected(object),
                readReturnCraftingRemainder(object)
        ));
    }

    private static Optional<MmceParsedRequirement> parseItemOrFuel(MmceIoType ioType, JsonObject object) {
        Optional<String> item = MmceJsonUtil.optionalString(object, "item", "itemId", "item-id", "item_id")
                .filter(value -> !value.isBlank());
        if (item.isPresent() && isFuelItemDefinition(item.get())) {
            return parseFuel(ioType, object).map(requirement -> (MmceParsedRequirement) requirement);
        }
        return parseItem(ioType, object).map(requirement -> (MmceParsedRequirement) requirement);
    }

    private static Optional<MmceFuelRequirement> parseFuel(MmceIoType ioType, JsonObject object) {
        if (ioType != MmceIoType.INPUT) {
            return Optional.empty();
        }
        int burnTime = readFirstInt(object, 0, "time", "burnTime", "burn-time", "burn_time", "requiredTotalBurnTime");
        if (burnTime <= 0) {
            return Optional.empty();
        }
        return Optional.of(new MmceFuelRequirement(
                burnTime,
                readChance(object),
                MmceJsonUtil.rawObject(object, "nbt"),
                readTriggerTime(object),
                readTriggerRepeatable(object),
                readIgnoreOutputCheck(object),
                readParallelizeUnaffected(object)
        ));
    }

    private static boolean isFuelItemDefinition(String itemDefinition) {
        return "any:fuel".equalsIgnoreCase(itemDefinition == null ? "" : itemDefinition.trim());
    }

    private static Optional<MmceSmartInterfaceRequirement> parseSmartInterface(MmceIoType ioType, JsonObject object) {
        if (ioType != MmceIoType.INPUT) {
            return Optional.empty();
        }
        Optional<String> interfaceType = readFirstString(object, "interfaceType", "interface-type", "interface_type",
                        "smartInterfaceType", "smart-interface-type", "smart_interface_type", "interface", "type", "name")
                .filter(value -> !value.isBlank());
        if (interfaceType.isEmpty()) {
            return Optional.empty();
        }
        float minValue;
        float maxValue;
        if (object.has("value")) {
            minValue = GsonHelper.getAsFloat(object, "value");
            maxValue = minValue;
        } else {
            minValue = readFirstFloat(object, 0.0F, "minValue", "min-value", "min_value", "min");
            maxValue = readFirstFloat(object, minValue, "maxValue", "max-value", "max_value", "max");
        }
        return Optional.of(new MmceSmartInterfaceRequirement(ioType, interfaceType.get(), minValue, maxValue));
    }

    private static Optional<MmceIngredientArrayRequirement> parseIngredientArray(MmceIoType ioType, JsonObject object, boolean optional) {
        if ((!object.has("items") || !object.get("items").isJsonArray())
                && !hasAnyString(object, "item", "itemId", "item-id", "item_id", "ore", "oreDict", "ore-dict", "ore_dict", "tag")) {
            return Optional.empty();
        }

        List<MmceItemRequirement> candidates = new ArrayList<>();
        if (object.has("items") && object.get("items").isJsonArray()) {
            for (var element : GsonHelper.getAsJsonArray(object, "items")) {
                JsonObject candidateObject;
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    candidateObject = new JsonObject();
                    candidateObject.addProperty("item", element.getAsString());
                } else {
                    candidateObject = GsonHelper.convertToJsonObject(element, "items[]").deepCopy();
                }
                copyIfMissing(object, candidateObject, "amount");
                copyIfMissing(object, candidateObject, "nbt");
                copyIfMissing(object, candidateObject, "nbt-display");
                copyIfMissing(object, candidateObject, "consumeDurability");
                copyIfMissing(object, candidateObject, "durabilityCost");
                copyIfMissing(object, candidateObject, "durability");
                copyIfMissing(object, candidateObject, "returnCraftingRemainder");
                copyIfMissing(object, candidateObject, "return-crafting-remainder");
                copyIfMissing(object, candidateObject, "return_crafting_remainder");
                parseItem(ioType, candidateObject).ifPresent(candidates::add);
            }
        } else {
            parseItem(ioType, object).ifPresent(candidates::add);
        }
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new MmceIngredientArrayRequirement(
                ioType,
                candidates,
                readChance(object),
                optional,
                optional ? readCatalystModifiers(object) : List.of(),
                readTriggerTime(object),
                readTriggerRepeatable(object),
                readIgnoreOutputCheck(object),
                optional || readParallelizeUnaffected(object)
        ));
    }

    private static List<MmceMachineModifierDefinition> readCatalystModifiers(JsonObject object) {
        if (!object.has("modifier") && !object.has("modifiers")) {
            return List.of();
        }
        try {
            return MmceMachineModifierDefinition.parseMany(object);
        } catch (RuntimeException exception) {
            ModularMachineryNeoForge.LOGGER.warn("Ignoring invalid MMCE catalyst modifier definition {}", object, exception);
            return List.of();
        }
    }

    private static void copyIfMissing(JsonObject source, JsonObject target, String key) {
        if (!target.has(key) && source.has(key)) {
            target.add(key, source.get(key).deepCopy());
        }
    }

    private static Optional<MmceFluidRequirement> parseFluid(MmceIoType ioType, JsonObject object, boolean perTick) {
        Optional<String> fluid = MmceJsonUtil.optionalString(object, "fluid",
                        "fluidName", "fluid-name", "fluid_name", "fluidId", "fluid-id", "fluid_id")
                .filter(value -> !value.isBlank());
        if (fluid.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new MmceFluidRequirement(
                ioType,
                MmceJsonUtil.defaultId(fluid.get()),
                readAmount(object),
                readChance(object),
                MmceJsonUtil.rawObject(object, "nbt"),
                MmceJsonUtil.rawObject(object, "nbt-display", "nbtDisplay", "nbt_display"),
                perTick,
                readTriggerTime(object),
                readTriggerRepeatable(object),
                readIgnoreOutputCheck(object),
                readParallelizeUnaffected(object)
        ));
    }

    private static Optional<MmceChemicalRequirement> parseChemical(MmceIoType ioType, JsonObject object, boolean perTick) {
        Optional<String> chemical = MmceJsonUtil.optionalString(object, "chemical", "gas", "fluid",
                        "chemicalName", "chemical-name", "chemical_name", "chemicalId", "chemical-id", "chemical_id",
                        "gasName", "gas-name", "gas_name", "gasId", "gas-id", "gas_id",
                        "fluidName", "fluid-name", "fluid_name", "fluidId", "fluid-id", "fluid_id")
                .filter(value -> !value.isBlank());
        if (chemical.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new MmceChemicalRequirement(
                ioType,
                MmceJsonUtil.defaultId(chemical.get()),
                readAmount(object),
                readChance(object),
                MmceJsonUtil.rawObject(object, "nbt"),
                MmceJsonUtil.rawObject(object, "nbt-display", "nbtDisplay", "nbt_display"),
                perTick,
                readTriggerTime(object),
                readTriggerRepeatable(object),
                readIgnoreOutputCheck(object),
                readParallelizeUnaffected(object)
        ));
    }

    private static Optional<MmceEnergyRequirement> parseEnergy(MmceIoType ioType, JsonObject object) {
        long energy = readFirstLong(object, 0L, "energyPerTick", "energy-per-tick", "energy_per_tick", "energy", "amount");
        if (energy <= 0L) {
            return Optional.empty();
        }
        return Optional.of(new MmceEnergyRequirement(
                ioType,
                energy,
                readTriggerTime(object),
                readTriggerRepeatable(object),
                readIgnoreOutputCheck(object),
                readParallelizeUnaffected(object)
        ));
    }

    private static int readAmount(JsonObject object) {
        return readFirstInt(object, 0, "amount", "mb", "millibuckets", "quantity");
    }

    private static float readChance(JsonObject object) {
        return object.has("chance") ? GsonHelper.getAsFloat(object, "chance") : 1.0F;
    }

    private static int readDurabilityCost(JsonObject object) {
        if (object.has("consumeDurability")) {
            return GsonHelper.getAsInt(object, "consumeDurability", 0);
        }
        if (object.has("durabilityCost")) {
            return GsonHelper.getAsInt(object, "durabilityCost", 0);
        }
        return object.has("durability") ? GsonHelper.getAsInt(object, "durability", 0) : 0;
    }

    private static int readTriggerTime(JsonObject object) {
        if (object.has("triggerTime")) {
            return GsonHelper.getAsInt(object, "triggerTime", 0);
        }
        if (object.has("trigger-time")) {
            return GsonHelper.getAsInt(object, "trigger-time", 0);
        }
        return object.has("trigger") ? GsonHelper.getAsInt(object, "trigger", 0) : 0;
    }

    private static Optional<String> readFirstString(JsonObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key)) {
                return Optional.of(GsonHelper.getAsString(object, key, ""));
            }
        }
        return Optional.empty();
    }

    private static float readFirstFloat(JsonObject object, float fallback, String... keys) {
        for (String key : keys) {
            if (object.has(key)) {
                return GsonHelper.getAsFloat(object, key, fallback);
            }
        }
        return fallback;
    }

    private static int readFirstInt(JsonObject object, int fallback, String... keys) {
        for (String key : keys) {
            if (object.has(key)) {
                return GsonHelper.getAsInt(object, key, fallback);
            }
        }
        return fallback;
    }

    private static Optional<String> requirePositiveInt(JsonObject object, String message, String... keys) {
        return readFirstInt(object, 0, keys) > 0 ? Optional.empty() : Optional.of(message);
    }

    private static long readFirstLong(JsonObject object, long fallback, String... keys) {
        for (String key : keys) {
            if (object.has(key)) {
                return GsonHelper.getAsLong(object, key, fallback);
            }
        }
        return fallback;
    }

    private static Optional<String> requirePositiveLong(JsonObject object, String message, String... keys) {
        return readFirstLong(object, 0L, keys) > 0L ? Optional.empty() : Optional.of(message);
    }

    private static boolean hasAnyString(JsonObject object, String... keys) {
        return readFirstString(object, keys).filter(value -> !value.isBlank()).isPresent();
    }

    private static boolean hasItems(JsonObject object) {
        return object.has("item") || object.has("itemId") || object.has("item-id") || object.has("item_id")
                || object.has("ore") || object.has("oreDict") || object.has("ore-dict") || object.has("ore_dict") || object.has("tag")
                || (object.has("items") && object.get("items").isJsonArray() && !object.getAsJsonArray("items").isEmpty());
    }

    private static ResourceLocation normalizeType(ResourceLocation type) {
        if (!"modularmachinery".equals(type.getNamespace())) {
            return type;
        }
        String normalizedPath = type.getPath().replace('-', '_');
        return normalizedPath.equals(type.getPath())
                ? type
                : ResourceLocation.fromNamespaceAndPath(type.getNamespace(), normalizedPath);
    }

    private static boolean readTriggerRepeatable(JsonObject object) {
        if (object.has("triggerRepeatable")) {
            return GsonHelper.getAsBoolean(object, "triggerRepeatable", false);
        }
        return object.has("trigger-repeatable") && GsonHelper.getAsBoolean(object, "trigger-repeatable", false);
    }

    private static boolean readIgnoreOutputCheck(JsonObject object) {
        if (object.has("ignoreOutputCheck")) {
            return GsonHelper.getAsBoolean(object, "ignoreOutputCheck", false);
        }
        if (object.has("ignore-output-check")) {
            return GsonHelper.getAsBoolean(object, "ignore-output-check", false);
        }
        return object.has("ignoreOutput") && GsonHelper.getAsBoolean(object, "ignoreOutput", false);
    }

    private static boolean readParallelizeUnaffected(JsonObject object) {
        if (object.has("parallelizeUnaffected")) {
            return GsonHelper.getAsBoolean(object, "parallelizeUnaffected", false);
        }
        if (object.has("parallelize-unaffected")) {
            return GsonHelper.getAsBoolean(object, "parallelize-unaffected", false);
        }
        return object.has("parallelize_unaffected")
                && GsonHelper.getAsBoolean(object, "parallelize_unaffected", false);
    }

    private static boolean readReturnCraftingRemainder(JsonObject object) {
        if (object.has("returnCraftingRemainder")) {
            return GsonHelper.getAsBoolean(object, "returnCraftingRemainder", false);
        }
        if (object.has("return-crafting-remainder")) {
            return GsonHelper.getAsBoolean(object, "return-crafting-remainder", false);
        }
        return object.has("return_crafting_remainder")
                && GsonHelper.getAsBoolean(object, "return_crafting_remainder", false);
    }

    private static int parsePositiveInt(String value) {
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
