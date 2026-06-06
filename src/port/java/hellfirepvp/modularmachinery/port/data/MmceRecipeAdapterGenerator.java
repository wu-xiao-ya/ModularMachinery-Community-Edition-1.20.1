package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.event.MmceEventRegistry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;

final class MmceRecipeAdapterGenerator {
    private static final ResourceLocation MINECRAFT_FURNACE = ResourceLocation.fromNamespaceAndPath("minecraft", "furnace");
    private static final ResourceLocation MINECRAFT_SMELTING = ResourceLocation.fromNamespaceAndPath("minecraft", "smelting");
    private static final ResourceLocation MINECRAFT_BLASTING = ResourceLocation.fromNamespaceAndPath("minecraft", "blasting");
    private static final ResourceLocation MINECRAFT_SMOKING = ResourceLocation.fromNamespaceAndPath("minecraft", "smoking");
    private static final ResourceLocation MINECRAFT_CAMPFIRE = ResourceLocation.fromNamespaceAndPath("minecraft", "campfire");
    private static final ResourceLocation MINECRAFT_CAMPFIRE_COOKING = ResourceLocation.fromNamespaceAndPath("minecraft", "campfire_cooking");
    private static final ResourceLocation MINECRAFT_CRAFTING = ResourceLocation.fromNamespaceAndPath("minecraft", "crafting");
    private static final ResourceLocation MINECRAFT_CRAFTING_SHAPED = ResourceLocation.fromNamespaceAndPath("minecraft", "crafting_shaped");
    private static final ResourceLocation MINECRAFT_CRAFTING_SHAPELESS = ResourceLocation.fromNamespaceAndPath("minecraft", "crafting_shapeless");
    private static final ResourceLocation MINECRAFT_STONECUTTER = ResourceLocation.fromNamespaceAndPath("minecraft", "stonecutter");
    private static final ResourceLocation MINECRAFT_STONECUTTING = ResourceLocation.fromNamespaceAndPath("minecraft", "stonecutting");
    private static final int DEFAULT_VANILLA_ADAPTER_RECIPE_TIME = 120;
    private static final ResourceLocation ITEM = target("item");
    private static final ResourceLocation FLUID = target("fluid");
    private static final ResourceLocation GAS = target("gas");
    private static final ResourceLocation CHEMICAL = target("chemical");
    private static final ResourceLocation ENERGY = target("energy");
    private static final ResourceLocation DURATION = target("duration");

    static MmceDataRegistry.Snapshot expandAdapters(
            MmceDataRegistry.Snapshot snapshot,
            RecipeManager recipeManager,
            HolderLookup.Provider registries
    ) {
        MmceEventRegistry.clearGeneratedRecipeAdapterLinks();
        if (snapshot.adapters().isEmpty()) {
            return snapshot;
        }
        if (recipeManager == null || registries == null) {
            ModularMachineryNeoForge.LOGGER.debug("Expanding only MMCE-machine recipe adapters until vanilla recipe reload context is available");
        }

        Map<ResourceLocation, MmceRecipeDefinition> recipes = new LinkedHashMap<>(snapshot.recipes());
        List<MmceDataLoadIssue> loadIssues = new ArrayList<>(snapshot.loadIssues());
        for (MmceRecipeAdapterDefinition adapter : snapshot.adapters().values()) {
            try {
                List<MmceRecipeDefinition> generated = generate(adapter, snapshot, recipeManager, registries);
                for (MmceRecipeDefinition recipe : generated) {
                    recipes.put(recipe.id(), recipe);
                    MmceEventRegistry.linkGeneratedRecipeToAdapter(recipe.id(), adapter.id());
                }
            } catch (RuntimeException ex) {
                loadIssues.add(MmceDataLoadIssue.of("recipe adapter generation", adapter.id(), ex));
                ModularMachineryNeoForge.LOGGER.error("Failed to generate MMCE recipes from adapter {}", adapter.id(), ex);
            }
        }

        return new MmceDataRegistry.Snapshot(
                snapshot.machines(),
                Map.copyOf(recipes),
                buildRecipesByMachine(recipes),
                snapshot.adapters(),
                snapshot.variables(),
                List.copyOf(loadIssues)
        );
    }

    private static List<MmceRecipeDefinition> generate(
            MmceRecipeAdapterDefinition adapter,
            MmceDataRegistry.Snapshot snapshot,
            RecipeManager recipeManager,
            HolderLookup.Provider registries
    ) {
        if (isDynamicMachineAdapter(adapter, snapshot)) {
            return generateDynamicMachine(adapter, snapshot);
        }
        if (recipeManager == null || registries == null) {
            return List.of();
        }
        if (adapter.adapterId().equals(MINECRAFT_FURNACE) || adapter.adapterId().equals(MINECRAFT_SMELTING)) {
            return generateCooking(adapter, recipeManager, registries, RecipeType.SMELTING);
        }
        if (adapter.adapterId().equals(MINECRAFT_BLASTING)) {
            return generateCooking(adapter, recipeManager, registries, RecipeType.BLASTING);
        }
        if (adapter.adapterId().equals(MINECRAFT_SMOKING)) {
            return generateCooking(adapter, recipeManager, registries, RecipeType.SMOKING);
        }
        if (adapter.adapterId().equals(MINECRAFT_CAMPFIRE_COOKING) || adapter.adapterId().equals(MINECRAFT_CAMPFIRE)) {
            return generateCooking(adapter, recipeManager, registries, RecipeType.CAMPFIRE_COOKING);
        }
        if (adapter.adapterId().equals(MINECRAFT_CRAFTING)) {
            return generateCrafting(adapter, recipeManager, registries, CraftingAdapterFilter.ALL);
        }
        if (adapter.adapterId().equals(MINECRAFT_CRAFTING_SHAPED)) {
            return generateCrafting(adapter, recipeManager, registries, CraftingAdapterFilter.SHAPED);
        }
        if (adapter.adapterId().equals(MINECRAFT_CRAFTING_SHAPELESS)) {
            return generateCrafting(adapter, recipeManager, registries, CraftingAdapterFilter.SHAPELESS);
        }
        if (adapter.adapterId().equals(MINECRAFT_STONECUTTING) || adapter.adapterId().equals(MINECRAFT_STONECUTTER)) {
            return generateStonecutting(adapter, recipeManager, registries);
        }

        ModularMachineryNeoForge.LOGGER.debug("MMCE recipe adapter {} is not implemented yet", adapter.adapterId());
        return List.of();
    }

    private static boolean isDynamicMachineAdapter(MmceRecipeAdapterDefinition adapter, MmceDataRegistry.Snapshot snapshot) {
        return snapshot.machines().containsKey(adapter.adapterId())
                || snapshot.recipesByMachine().containsKey(adapter.adapterId());
    }

    private static List<MmceRecipeDefinition> generateDynamicMachine(
            MmceRecipeAdapterDefinition adapter,
            MmceDataRegistry.Snapshot snapshot
    ) {
        List<MmceRecipeDefinition> parentRecipes = snapshot.recipesByMachine().getOrDefault(adapter.adapterId(), List.of());
        if (parentRecipes.isEmpty()) {
            return List.of();
        }

        List<MmceRecipeDefinition> generated = new ArrayList<>(parentRecipes.size());
        for (MmceRecipeDefinition parent : parentRecipes) {
            ResourceLocation recipeId = generatedRecipeId(adapter, parent.id());
            JsonObject root = parent.rawJson().deepCopy();
            root.addProperty("registryName", recipeId.toString());
            root.addProperty("machine", adapter.machineId().toString());

            int baseDuration = readAdapterOptionalInt(adapter,
                    "recipeTime", "recipe-time", "recipe_time", "duration", "time")
                    .orElse(parent.recipeTime());
            root.addProperty("recipeTime", applyInt(adapter.modifiers(), DURATION, Optional.of(MmceIoType.INPUT), baseDuration));

            readAdapterOptionalInt(adapter, "priority")
                    .ifPresent(value -> root.addProperty("priority", value));
            readAdapterOptionalBoolean(adapter,
                    "cancelIfPerTickFails",
                    "cancel-if-per-tick-fails",
                    "cancel_if_per_tick_fails",
                    "voidPerTickFailure",
                    "void-per-tick-failure",
                    "void_per_tick_failure")
                    .ifPresent(value -> root.addProperty("cancelIfPerTickFails", value));
            readAdapterOptionalBoolean(adapter, "parallelized", "parallelize", "parallelizable")
                    .ifPresent(value -> root.addProperty("parallelized", value));
            readAdapterOptionalBoolean(adapter, "loadJEI", "loadJei", "load-jei", "load_jei")
                    .ifPresent(value -> root.addProperty("loadJEI", value));
            readAdapterOptionalInt(adapter, "maxThreads", "max-threads", "max_threads")
                    .filter(value -> value != -1)
                    .ifPresent(value -> root.addProperty("maxThreads", Math.max(-1, value)));
            readAdapterOptionalString(adapter, "threadName", "thread-name", "thread_name")
                    .filter(value -> !value.isBlank())
                    .ifPresent(value -> root.addProperty("threadName", value));

            List<String> recipeTooltips = readAdapterStringList(adapter,
                    "recipeTooltips", "recipe-tooltips", "recipe_tooltips", "tooltips");
            if (!recipeTooltips.isEmpty()) {
                JsonArray tooltipArray = root.has("recipeTooltips") && root.get("recipeTooltips").isJsonArray()
                        ? root.getAsJsonArray("recipeTooltips").deepCopy()
                        : new JsonArray();
                recipeTooltips.forEach(tooltipArray::add);
                root.add("recipeTooltips", tooltipArray);
            }

            JsonArray requirements = new JsonArray();
            JsonArray parentRequirements = parent.rawJson().has("requirements") && parent.rawJson().get("requirements").isJsonArray()
                    ? parent.rawJson().getAsJsonArray("requirements")
                    : new JsonArray();
            for (JsonElement element : parentRequirements) {
                JsonObject requirement = net.minecraft.util.GsonHelper.convertToJsonObject(element, "requirements[]").deepCopy();
                applyRequirementModifiers(requirement, adapter.modifiers());
                requirements.add(requirement);
            }
            for (MmceRecipeRequirement requirement : adapter.requirements()) {
                requirements.add(requirement.rawJson().deepCopy());
            }
            root.add("requirements", requirements);

            generated.add(MmceRecipeDefinition.parse(recipeId, root));
        }
        return generated;
    }

    private static <T extends AbstractCookingRecipe> List<MmceRecipeDefinition> generateCooking(
            MmceRecipeAdapterDefinition adapter,
            RecipeManager recipeManager,
            HolderLookup.Provider registries,
            RecipeType<T> recipeType
    ) {
        List<RecipeHolder<T>> cookingRecipes = recipeManager.getAllRecipesFor(recipeType);
        List<MmceRecipeDefinition> generated = new ArrayList<>(cookingRecipes.size());
        int priority = readAdapterOptionalInt(adapter, "priority").orElse(0);
        boolean cancelIfPerTickFails = readAdapterOptionalBoolean(adapter,
                "cancelIfPerTickFails", "cancel-if-per-tick-fails", "cancel_if_per_tick_fails",
                "voidPerTickFailure", "void-per-tick-failure", "void_per_tick_failure").orElse(false);
        boolean parallelized = readAdapterOptionalBoolean(adapter, "parallelized", "parallelize", "parallelizable")
                .orElse(false);
        int maxThreads = readAdapterMaxThreads(adapter);
        String threadName = readAdapterThreadName(adapter);
        List<String> recipeTooltips = readAdapterStringList(adapter, "recipeTooltips", "recipe-tooltips", "recipe_tooltips", "tooltips");
        boolean loadJei = readAdapterLoadJei(adapter);

        for (RecipeHolder<T> holder : cookingRecipes) {
            int baseDuration = readAdapterOptionalInt(adapter,
                    "recipeTime", "recipe-time", "recipe_time", "duration", "time")
                    .orElse(adapter.adapterId().equals(MINECRAFT_FURNACE)
                            ? DEFAULT_VANILLA_ADAPTER_RECIPE_TIME
                            : holder.value().getCookingTime());
            JsonObject root = new JsonObject();
            ResourceLocation recipeId = generatedRecipeId(adapter, holder.id());
            addAdapterRecipeProperties(root, adapter, recipeId, baseDuration, priority, cancelIfPerTickFails,
                    parallelized, maxThreads, threadName, recipeTooltips, loadJei);

            JsonArray requirements = new JsonArray();
            if (!addCookingInput(requirements, adapter, holder.id(), holder.value())) {
                continue;
            }
            if (!addCookingOutput(requirements, adapter, holder.id(), holder.value(), registries)) {
                continue;
            }
            addCookingEnergy(requirements, adapter);
            addAdditionalRequirements(requirements, adapter);
            root.add("requirements", requirements);

            generated.add(MmceRecipeDefinition.parse(recipeId, root));
        }
        return generated;
    }

    private static List<MmceRecipeDefinition> generateCrafting(
            MmceRecipeAdapterDefinition adapter,
            RecipeManager recipeManager,
            HolderLookup.Provider registries,
            CraftingAdapterFilter filter
    ) {
        List<RecipeHolder<CraftingRecipe>> craftingRecipes = recipeManager.getAllRecipesFor(RecipeType.CRAFTING);
        List<MmceRecipeDefinition> generated = new ArrayList<>(craftingRecipes.size());
        AdapterRecipeProperties properties = AdapterRecipeProperties.read(adapter);

        for (RecipeHolder<CraftingRecipe> holder : craftingRecipes) {
            CraftingRecipe recipe = holder.value();
            if (!filter.matches(recipe)) {
                continue;
            }

            JsonObject root = new JsonObject();
            ResourceLocation recipeId = generatedRecipeId(adapter, holder.id());
            addAdapterRecipeProperties(root, adapter, recipeId, properties);

            JsonArray requirements = new JsonArray();
            if (!addIngredientInputs(requirements, adapter, holder.id(), "crafting", recipe.getIngredients(), true)) {
                continue;
            }
            if (!addRecipeItemOutput(requirements, adapter, holder.id(), "crafting", recipe.getResultItem(registries), false)) {
                continue;
            }
            addCookingEnergy(requirements, adapter);
            addAdditionalRequirements(requirements, adapter);
            root.add("requirements", requirements);

            generated.add(MmceRecipeDefinition.parse(recipeId, root));
        }
        return generated;
    }

    private static List<MmceRecipeDefinition> generateStonecutting(
            MmceRecipeAdapterDefinition adapter,
            RecipeManager recipeManager,
            HolderLookup.Provider registries
    ) {
        List<RecipeHolder<StonecutterRecipe>> stonecuttingRecipes = recipeManager.getAllRecipesFor(RecipeType.STONECUTTING);
        List<MmceRecipeDefinition> generated = new ArrayList<>(stonecuttingRecipes.size());
        AdapterRecipeProperties properties = AdapterRecipeProperties.read(adapter);

        for (RecipeHolder<StonecutterRecipe> holder : stonecuttingRecipes) {
            StonecutterRecipe recipe = holder.value();
            JsonObject root = new JsonObject();
            ResourceLocation recipeId = generatedRecipeId(adapter, holder.id());
            addAdapterRecipeProperties(root, adapter, recipeId, properties);

            JsonArray requirements = new JsonArray();
            if (!addIngredientInputs(requirements, adapter, holder.id(), "stonecutting", recipe.getIngredients(), true)) {
                continue;
            }
            if (!addRecipeItemOutput(requirements, adapter, holder.id(), "stonecutting", recipe.getResultItem(registries), false)) {
                continue;
            }
            addCookingEnergy(requirements, adapter);
            addAdditionalRequirements(requirements, adapter);
            root.add("requirements", requirements);

            generated.add(MmceRecipeDefinition.parse(recipeId, root));
        }
        return generated;
    }

    private static void addAdapterRecipeProperties(
            JsonObject root,
            MmceRecipeAdapterDefinition adapter,
            ResourceLocation recipeId,
            AdapterRecipeProperties properties
    ) {
        addAdapterRecipeProperties(root, adapter, recipeId, properties.recipeTime(), properties.priority(),
                properties.cancelIfPerTickFails(), properties.parallelized(), properties.maxThreads(),
                properties.threadName(), properties.recipeTooltips(), properties.loadJei());
    }

    private static void addAdapterRecipeProperties(
            JsonObject root,
            MmceRecipeAdapterDefinition adapter,
            ResourceLocation recipeId,
            int baseDuration,
            int priority,
            boolean cancelIfPerTickFails,
            boolean parallelized,
            int maxThreads,
            String threadName,
            List<String> recipeTooltips,
            boolean loadJei
    ) {
        root.addProperty("registryName", recipeId.toString());
        root.addProperty("machine", adapter.machineId().toString());
        root.addProperty("recipeTime", applyInt(adapter.modifiers(), DURATION, Optional.of(MmceIoType.INPUT), baseDuration));
        root.addProperty("priority", priority);
        root.addProperty("cancelIfPerTickFails", cancelIfPerTickFails);
        root.addProperty("parallelized", parallelized);
        root.addProperty("maxThreads", maxThreads);
        root.addProperty("loadJEI", loadJei);
        if (!threadName.isBlank()) {
            root.addProperty("threadName", threadName);
        }
        if (!recipeTooltips.isEmpty()) {
            JsonArray tooltipArray = new JsonArray();
            recipeTooltips.forEach(tooltipArray::add);
            root.add("recipeTooltips", tooltipArray);
        }
    }

    private static boolean addIngredientInputs(
            JsonArray requirements,
            MmceRecipeAdapterDefinition adapter,
            ResourceLocation sourceRecipeId,
            String sourceType,
            Iterable<Ingredient> ingredients,
            boolean returnCraftingRemainder
    ) {
        for (Ingredient ingredient : ingredients) {
            if (ingredient == null || ingredient.isEmpty()) {
                continue;
            }
            if (!addIngredientInput(requirements, adapter, sourceRecipeId, sourceType, ingredient, returnCraftingRemainder)) {
                return false;
            }
        }
        return true;
    }

    private static boolean addIngredientInput(
            JsonArray requirements,
            MmceRecipeAdapterDefinition adapter,
            ResourceLocation sourceRecipeId,
            String sourceType,
            Ingredient ingredient,
            boolean returnCraftingRemainder
    ) {
        List<ItemStack> candidates = ingredientCandidates(adapter, sourceRecipeId, sourceType, ingredient);
        if (candidates.isEmpty()) {
            return ingredient.getItems().length == 0;
        }

        if (candidates.size() == 1) {
            ItemStack stack = candidates.getFirst();
            int amount = applyInt(adapter.modifiers(), ITEM, Optional.of(MmceIoType.INPUT), Math.max(1, stack.getCount()));
            if (amount <= 0) {
                return true;
            }
            JsonObject requirement = itemRequirement("input", stack, amount);
            if (returnCraftingRemainder) {
                requirement.addProperty("returnCraftingRemainder", true);
            }
            requirements.add(requirement);
            return true;
        }

        int amount = applyInt(adapter.modifiers(), ITEM, Optional.of(MmceIoType.INPUT), 1);
        if (amount <= 0) {
            return true;
        }
        JsonObject requirement = baseRequirement("modularmachinery:ingredient_array_input", "input");
        JsonArray items = new JsonArray();
        for (ItemStack stack : candidates) {
            JsonObject item = itemEntry(stack, amount);
            if (returnCraftingRemainder) {
                item.addProperty("returnCraftingRemainder", true);
            }
            items.add(item);
        }
        requirement.add("items", items);
        requirements.add(requirement);
        return true;
    }

    private static List<ItemStack> ingredientCandidates(
            MmceRecipeAdapterDefinition adapter,
            ResourceLocation sourceRecipeId,
            String sourceType,
            Ingredient ingredient
    ) {
        List<ItemStack> candidates = new ArrayList<>();
        for (ItemStack stack : ingredient.getItems()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (!canRepresentItemComponents(stack)) {
                ModularMachineryNeoForge.LOGGER.warn("Skipping unsupported component input {} while adapting {} recipe {} through {}",
                        stack, sourceType, sourceRecipeId, adapter.id());
                continue;
            }
            if (candidates.stream().noneMatch(existing -> ItemStack.isSameItemSameComponents(existing, stack))) {
                candidates.add(stack);
            }
        }
        return candidates;
    }

    private static boolean addRecipeItemOutput(
            JsonArray requirements,
            MmceRecipeAdapterDefinition adapter,
            ResourceLocation sourceRecipeId,
            String sourceType,
            ItemStack output,
            boolean allowEmpty
    ) {
        if (output.isEmpty()) {
            return allowEmpty;
        }
        if (!canRepresentItemComponents(output)) {
            ModularMachineryNeoForge.LOGGER.warn("Skipping {} recipe {} through adapter {} because output {} has unsupported components",
                    sourceType, sourceRecipeId, adapter.id(), output);
            return false;
        }
        int amount = applyInt(adapter.modifiers(), ITEM, Optional.of(MmceIoType.OUTPUT), Math.max(1, output.getCount()));
        if (amount <= 0) {
            return true;
        }
        requirements.add(itemRequirement("output", output, amount));
        return true;
    }

    private static void addAdditionalRequirements(JsonArray requirements, MmceRecipeAdapterDefinition adapter) {
        for (MmceRecipeRequirement requirement : adapter.requirements()) {
            JsonObject copy = requirement.rawJson().deepCopy();
            applyRequirementModifiers(copy, adapter.modifiers());
            requirements.add(copy);
        }
    }

    private static boolean addCookingInput(
            JsonArray requirements,
            MmceRecipeAdapterDefinition adapter,
            ResourceLocation sourceRecipeId,
            AbstractCookingRecipe recipe
    ) {
        Ingredient ingredient = recipe.getIngredients().isEmpty() ? Ingredient.EMPTY : recipe.getIngredients().getFirst();
        List<ItemStack> candidates = new ArrayList<>();
        boolean hadCandidate = false;
        for (ItemStack stack : ingredient.getItems()) {
            if (stack.isEmpty()) {
                continue;
            }
            hadCandidate = true;
            if (!canRepresentItemComponents(stack)) {
                ModularMachineryNeoForge.LOGGER.warn("Skipping unsupported component input {} while adapting cooking recipe {} through {}",
                        stack, sourceRecipeId, adapter.id());
                continue;
            }
            if (candidates.stream().noneMatch(existing -> ItemStack.isSameItemSameComponents(existing, stack))) {
                candidates.add(stack);
            }
        }
        if (candidates.isEmpty()) {
            return !hadCandidate;
        }

        if (candidates.size() == 1) {
            ItemStack stack = candidates.getFirst();
            int amount = applyInt(adapter.modifiers(), ITEM, Optional.of(MmceIoType.INPUT), Math.max(1, stack.getCount()));
            if (amount <= 0) {
                return true;
            }
            JsonObject requirement = itemRequirement("input", stack, amount);
            requirements.add(requirement);
            return true;
        }

        int amount = applyInt(adapter.modifiers(), ITEM, Optional.of(MmceIoType.INPUT), 1);
        if (amount <= 0) {
            return true;
        }
        JsonObject requirement = baseRequirement("modularmachinery:ingredient_array_input", "input");
        JsonArray items = new JsonArray();
        for (ItemStack stack : candidates) {
            items.add(itemEntry(stack, amount));
        }
        requirement.add("items", items);
        requirements.add(requirement);
        return true;
    }

    private static boolean addCookingOutput(
            JsonArray requirements,
            MmceRecipeAdapterDefinition adapter,
            ResourceLocation sourceRecipeId,
            AbstractCookingRecipe recipe,
            HolderLookup.Provider registries
    ) {
        ItemStack output = recipe.getResultItem(registries);
        if (output.isEmpty()) {
            return true;
        }
        if (!canRepresentItemComponents(output)) {
            ModularMachineryNeoForge.LOGGER.warn("Skipping cooking recipe {} through adapter {} because output {} has unsupported components",
                    sourceRecipeId, adapter.id(), output);
            return false;
        }
        int amount = applyInt(adapter.modifiers(), ITEM, Optional.of(MmceIoType.OUTPUT), Math.max(1, output.getCount()));
        if (amount <= 0) {
            return true;
        }
        requirements.add(itemRequirement("output", output, amount));
        return true;
    }

    private static void addCookingEnergy(JsonArray requirements, MmceRecipeAdapterDefinition adapter) {
        long energy = applyLong(adapter.modifiers(), ENERGY, Optional.of(MmceIoType.INPUT), 20L);
        if (energy <= 0L) {
            return;
        }
        JsonObject requirement = baseRequirement("modularmachinery:energy", "input");
        requirement.addProperty("energyPerTick", energy);
        requirements.add(requirement);
    }

    private static JsonObject itemRequirement(String io, ItemStack stack, int amount) {
        JsonObject requirement = itemEntry(stack, amount);
        requirement.addProperty("type", "modularmachinery:item");
        requirement.addProperty("io-type", io);
        return requirement;
    }

    private static JsonObject itemEntry(ItemStack stack, int amount) {
        JsonObject object = new JsonObject();
        object.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        object.addProperty("amount", amount);
        JsonObject nbt = knownComponentNbt(stack);
        if (!nbt.isEmpty()) {
            object.add("nbt", nbt);
        }
        return object;
    }

    private static JsonObject baseRequirement(String type, String io) {
        JsonObject requirement = new JsonObject();
        requirement.addProperty("type", type);
        requirement.addProperty("io-type", io);
        return requirement;
    }

    private static JsonObject knownComponentNbt(ItemStack stack) {
        return MmceNbtCompat.itemStackNbt(stack);
    }

    private static boolean canRepresentItemComponents(ItemStack stack) {
        for (var entry : stack.getComponentsPatch().entrySet()) {
            Optional<?> value = entry.getValue();
            if (value.isEmpty() || !isRepresentableComponent(entry.getKey())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isRepresentableComponent(DataComponentType<?> type) {
        return type == DataComponents.CUSTOM_DATA
                || type == DataComponents.DAMAGE
                || type == DataComponents.CUSTOM_NAME;
    }

    private static int applyInt(List<MmceMachineModifierDefinition> modifiers, ResourceLocation target, Optional<MmceIoType> io, int value) {
        return Math.max(0, clampToInt(Math.round(apply(modifiers, List.of(target), io, value, false))));
    }

    private static long applyLong(List<MmceMachineModifierDefinition> modifiers, ResourceLocation target, Optional<MmceIoType> io, long value) {
        double applied = apply(modifiers, List.of(target), io, value, false);
        if (applied <= 0.0D) {
            return 0L;
        }
        if (applied >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.round(applied);
    }

    private static double apply(List<MmceMachineModifierDefinition> modifiers, List<ResourceLocation> targets, Optional<MmceIoType> io, double value, boolean affectChance) {
        double add = 0.0D;
        double multiply = 1.0D;
        for (MmceMachineModifierDefinition modifier : modifiers) {
            if (!targets.contains(modifier.target()) || modifier.affectChance() != affectChance) {
                continue;
            }
            if (modifier.ioType().isPresent() && io.isPresent() && modifier.ioType().get() != io.get()) {
                continue;
            }
            if (modifier.operation() == 0) {
                add += modifier.multiplier();
            } else if (modifier.operation() == 1) {
                multiply *= modifier.multiplier();
            }
        }
        return (value + add) * multiply;
    }

    private static void applyRequirementModifiers(JsonObject requirement, List<MmceMachineModifierDefinition> modifiers) {
        if (modifiers.isEmpty()) {
            return;
        }
        Optional<ResourceLocation> type = readRequirementType(requirement);
        if (type.isEmpty()) {
            return;
        }

        List<ResourceLocation> targets = modifierTargets(type.get());
        Optional<MmceIoType> io = readRequirementIo(requirement, type.get());
        boolean fuel = isFuelType(type.get());
        boolean energy = targets.contains(ENERGY);
        applyRequirementFields(requirement, modifiers, targets, io, fuel, energy);

        if (requirement.has("items") && requirement.get("items").isJsonArray()) {
            JsonArray items = requirement.getAsJsonArray("items");
            for (JsonElement element : items) {
                if (element != null && element.isJsonObject()) {
                    applyRequirementFields(element.getAsJsonObject(), modifiers, targets, io, fuel, energy);
                }
            }
        }
    }

    private static void applyRequirementFields(
            JsonObject object,
            List<MmceMachineModifierDefinition> modifiers,
            List<ResourceLocation> targets,
            Optional<MmceIoType> io,
            boolean fuel,
            boolean energy
    ) {
        if (hasApplicableModifier(modifiers, targets, io, true) || object.has("chance")) {
            double chance = object.has("chance")
                    ? net.minecraft.util.GsonHelper.getAsDouble(object, "chance", 1.0D)
                    : 1.0D;
            double applied = apply(modifiers, targets, io, chance, true);
            object.addProperty("chance", Math.max(0.0D, Math.min(1.0D, applied)));
        }

        if (energy) {
            applyLongFields(object, modifiers, targets, io,
                    "energyPerTick", "energy-per-tick", "energy_per_tick", "energy", "amount");
            return;
        }
        if (fuel) {
            applyIntFields(object, modifiers, targets, io,
                    "time", "burnTime", "burn-time", "burn_time", "requiredTotalBurnTime");
        }
        applyIntFields(object, modifiers, targets, io, "amount", "count", "size", "quantity");
        applyIntFields(object, modifiers, targets, io, "minAmount", "min-amount", "min_amount", "min");
        applyIntFields(object, modifiers, targets, io, "maxAmount", "max-amount", "max_amount", "max");
    }

    private static void applyIntFields(
            JsonObject object,
            List<MmceMachineModifierDefinition> modifiers,
            List<ResourceLocation> targets,
            Optional<MmceIoType> io,
            String... keys
    ) {
        for (String key : keys) {
            if (object.has(key) && object.get(key).isJsonPrimitive()) {
                int value = Math.max(0, clampToInt(Math.round(apply(modifiers, targets, io, object.get(key).getAsDouble(), false))));
                object.addProperty(key, value);
            }
        }
    }

    private static void applyLongFields(
            JsonObject object,
            List<MmceMachineModifierDefinition> modifiers,
            List<ResourceLocation> targets,
            Optional<MmceIoType> io,
            String... keys
    ) {
        for (String key : keys) {
            if (object.has(key) && object.get(key).isJsonPrimitive()) {
                double applied = apply(modifiers, targets, io, object.get(key).getAsDouble(), false);
                long value = applied <= 0.0D ? 0L : applied >= Long.MAX_VALUE ? Long.MAX_VALUE : Math.round(applied);
                object.addProperty(key, value);
            }
        }
    }

    private static boolean hasApplicableModifier(
            List<MmceMachineModifierDefinition> modifiers,
            List<ResourceLocation> targets,
            Optional<MmceIoType> io,
            boolean affectChance
    ) {
        for (MmceMachineModifierDefinition modifier : modifiers) {
            if (!targets.contains(modifier.target()) || modifier.affectChance() != affectChance) {
                continue;
            }
            if (modifier.ioType().isPresent() && io.isPresent() && modifier.ioType().get() != io.get()) {
                continue;
            }
            return true;
        }
        return false;
    }

    private static Optional<ResourceLocation> readRequirementType(JsonObject requirement) {
        if (!requirement.has("type")) {
            return Optional.empty();
        }
        try {
            ResourceLocation type = MmceJsonUtil.modId(net.minecraft.util.GsonHelper.getAsString(requirement, "type"));
            return Optional.of(ResourceLocation.fromNamespaceAndPath(type.getNamespace(), type.getPath().replace('-', '_')));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<MmceIoType> readRequirementIo(JsonObject requirement, ResourceLocation type) {
        Optional<MmceIoType> io = readFirstString(requirement, "io", "io-type", "ioType", "io_type")
                .flatMap(MmceIoType::byName);
        return io.or(() -> defaultIoType(type));
    }

    private static Optional<MmceIoType> defaultIoType(ResourceLocation type) {
        if (!ModularMachineryNeoForge.MODID.equals(type.getNamespace())) {
            return Optional.empty();
        }
        return switch (type.getPath()) {
            case "fuel", "fuel_item", "fuel_item_input", "ingredient_array_input", "catalyst",
                    "interface_number_input", "smart_interface_number_input" -> Optional.of(MmceIoType.INPUT);
            case "ingredient_array_output", "random_item_output" -> Optional.of(MmceIoType.OUTPUT);
            default -> Optional.empty();
        };
    }

    private static List<ResourceLocation> modifierTargets(ResourceLocation type) {
        if (!ModularMachineryNeoForge.MODID.equals(type.getNamespace())) {
            return List.of(type);
        }
        List<ResourceLocation> targets = new ArrayList<>();
        switch (type.getPath()) {
            case "fluid", "fluid_pertick", "fluid_per_tick" -> targets.add(FLUID);
            case "gas", "gas_pertick", "gas_per_tick" -> {
                targets.add(GAS);
                targets.add(CHEMICAL);
            }
            case "chemical", "chemical_pertick", "chemical_per_tick" -> {
                targets.add(CHEMICAL);
                targets.add(GAS);
            }
            case "energy" -> targets.add(ENERGY);
            default -> targets.add(ITEM);
        }
        if (!targets.contains(type)) {
            targets.add(type);
        }
        if (type.getPath().startsWith("ingredient_array") && !targets.contains(target("ingredient_array"))) {
            targets.add(target("ingredient_array"));
        }
        return List.copyOf(targets);
    }

    private static boolean isFuelType(ResourceLocation type) {
        return ModularMachineryNeoForge.MODID.equals(type.getNamespace())
                && switch (type.getPath()) {
                    case "fuel", "fuel_item", "fuel_item_input" -> true;
                    default -> false;
                };
    }

    private static Optional<Integer> readAdapterOptionalInt(MmceRecipeAdapterDefinition adapter, String... keys) {
        for (String key : keys) {
            if (adapter.rawJson().has(key)) {
                return Optional.of(net.minecraft.util.GsonHelper.getAsInt(adapter.rawJson(), key));
            }
        }
        return Optional.empty();
    }

    private static Optional<Boolean> readAdapterOptionalBoolean(MmceRecipeAdapterDefinition adapter, String... keys) {
        for (String key : keys) {
            if (adapter.rawJson().has(key)) {
                return Optional.of(net.minecraft.util.GsonHelper.getAsBoolean(adapter.rawJson(), key));
            }
        }
        return Optional.empty();
    }

    private static Optional<String> readAdapterOptionalString(MmceRecipeAdapterDefinition adapter, String... keys) {
        return readFirstString(adapter.rawJson(), keys).map(String::trim);
    }

    private static Optional<String> readFirstString(JsonObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key)) {
                return Optional.of(net.minecraft.util.GsonHelper.getAsString(object, key, ""));
            }
        }
        return Optional.empty();
    }

    private static int readAdapterMaxThreads(MmceRecipeAdapterDefinition adapter) {
        return readAdapterOptionalInt(adapter, "maxThreads", "max-threads", "max_threads")
                .map(value -> Math.max(-1, value))
                .orElse(-1);
    }

    private static String readAdapterThreadName(MmceRecipeAdapterDefinition adapter) {
        return readAdapterOptionalString(adapter, "threadName", "thread-name", "thread_name").orElse("");
    }

    private static boolean readAdapterLoadJei(MmceRecipeAdapterDefinition adapter) {
        return readAdapterOptionalBoolean(adapter, "loadJEI", "loadJei", "load-jei", "load_jei").orElse(true);
    }

    private static List<String> readAdapterStringList(MmceRecipeAdapterDefinition adapter, String... keys) {
        for (String key : keys) {
            if (!adapter.rawJson().has(key)) {
                continue;
            }
            JsonElement value = adapter.rawJson().get(key);
            List<String> values = new ArrayList<>();
            if (value.isJsonArray()) {
                JsonArray array = value.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    JsonElement element = array.get(i);
                    if (!element.isJsonNull()) {
                        values.add(net.minecraft.util.GsonHelper.convertToString(element, key + "[" + i + "]"));
                    }
                }
            } else if (!value.isJsonNull()) {
                values.add(net.minecraft.util.GsonHelper.convertToString(value, key));
            }
            return List.copyOf(values);
        }
        return List.of();
    }

    private static ResourceLocation generatedRecipeId(MmceRecipeAdapterDefinition adapter, ResourceLocation sourceRecipeId) {
        String sourcePath = adapter.id().getPath().replace(".adapter", "");
        String path = "adapter_generated/" + sanitize(sourcePath) + "/" + sanitize(sourceRecipeId.toString());
        return ResourceLocation.fromNamespaceAndPath(adapter.id().getNamespace(), path);
    }

    private static String sanitize(String value) {
        return value.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
    }

    private static Map<ResourceLocation, List<MmceRecipeDefinition>> buildRecipesByMachine(Map<ResourceLocation, MmceRecipeDefinition> recipes) {
        Map<ResourceLocation, List<MmceRecipeDefinition>> recipesByMachine = new LinkedHashMap<>();
        for (MmceRecipeDefinition recipe : recipes.values()) {
            recipesByMachine.computeIfAbsent(recipe.machineId(), ignored -> new ArrayList<>()).add(recipe);
        }
        recipesByMachine.replaceAll((machineId, values) -> {
            values.sort(Comparator.comparingInt(MmceRecipeDefinition::priority));
            return List.copyOf(values);
        });
        return Map.copyOf(recipesByMachine);
    }

    private static int clampToInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    private static ResourceLocation target(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, path);
    }

    private record AdapterRecipeProperties(
            int recipeTime,
            int priority,
            boolean cancelIfPerTickFails,
            boolean parallelized,
            int maxThreads,
            String threadName,
            List<String> recipeTooltips,
            boolean loadJei
    ) {
        static AdapterRecipeProperties read(MmceRecipeAdapterDefinition adapter) {
            return new AdapterRecipeProperties(
                    readAdapterOptionalInt(adapter, "recipeTime", "recipe-time", "recipe_time", "duration", "time")
                            .orElse(DEFAULT_VANILLA_ADAPTER_RECIPE_TIME),
                    readAdapterOptionalInt(adapter, "priority").orElse(0),
                    readAdapterOptionalBoolean(adapter,
                            "cancelIfPerTickFails", "cancel-if-per-tick-fails", "cancel_if_per_tick_fails",
                            "voidPerTickFailure", "void-per-tick-failure", "void_per_tick_failure").orElse(false),
                    readAdapterOptionalBoolean(adapter, "parallelized", "parallelize", "parallelizable").orElse(false),
                    readAdapterMaxThreads(adapter),
                    readAdapterThreadName(adapter),
                    readAdapterStringList(adapter, "recipeTooltips", "recipe-tooltips", "recipe_tooltips", "tooltips"),
                    readAdapterLoadJei(adapter)
            );
        }
    }

    private enum CraftingAdapterFilter {
        ALL {
            @Override
            boolean matches(CraftingRecipe recipe) {
                return isStaticCrafting(recipe);
            }
        },
        SHAPED {
            @Override
            boolean matches(CraftingRecipe recipe) {
                return recipe.getSerializer() == RecipeSerializer.SHAPED_RECIPE;
            }
        },
        SHAPELESS {
            @Override
            boolean matches(CraftingRecipe recipe) {
                return recipe.getSerializer() == RecipeSerializer.SHAPELESS_RECIPE;
            }
        };

        abstract boolean matches(CraftingRecipe recipe);

        private static boolean isStaticCrafting(CraftingRecipe recipe) {
            return recipe.getSerializer() == RecipeSerializer.SHAPED_RECIPE
                    || recipe.getSerializer() == RecipeSerializer.SHAPELESS_RECIPE;
        }
    }

    private MmceRecipeAdapterGenerator() {
    }
}
