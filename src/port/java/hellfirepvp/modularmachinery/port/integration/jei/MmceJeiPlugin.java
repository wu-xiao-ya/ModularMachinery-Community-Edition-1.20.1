package hellfirepvp.modularmachinery.port.integration.jei;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.data.MmceRecipeDefinition;
import hellfirepvp.modularmachinery.port.item.MmceBlueprintData;
import hellfirepvp.modularmachinery.port.registry.MmceBlocks;
import hellfirepvp.modularmachinery.port.registry.MmceItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public final class MmceJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = MmceJeiUtil.id("jei_plugin");
    private static final Map<ResourceLocation, RecipeType<MmceJeiMachineRecipe>> RECIPE_TYPES = new LinkedHashMap<>();

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        RECIPE_TYPES.clear();
        List<MmceMachineDefinition> machines = sortedMachines();
        for (MmceMachineDefinition machine : machines) {
            if (recipesFor(machine).isEmpty()) {
                continue;
            }
            RecipeType<MmceJeiMachineRecipe> type = recipeType(machine);
            RECIPE_TYPES.put(machine.id(), type);
            registration.addRecipeCategories(new MmceJeiMachineRecipeCategory(guiHelper, type, title(machine)));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        for (MmceMachineDefinition machine : sortedMachines()) {
            RecipeType<MmceJeiMachineRecipe> type = RECIPE_TYPES.get(machine.id());
            if (type == null) {
                continue;
            }
            List<MmceJeiMachineRecipe> recipes = recipesFor(machine).stream()
                    .filter(MmceRecipeDefinition::loadJei)
                    .map(recipe -> new MmceJeiMachineRecipe(machine, recipe))
                    .toList();
            if (!recipes.isEmpty()) {
                registration.addRecipes(type, recipes);
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (MmceMachineDefinition machine : sortedMachines()) {
            RecipeType<MmceJeiMachineRecipe> type = RECIPE_TYPES.get(machine.id());
            if (type == null) {
                continue;
            }
            registration.addRecipeCatalyst(MmceBlocks.BLOCK_CONTROLLER.get(), type);
            if (machine.hasFactory() || machine.factoryOnly()) {
                registration.addRecipeCatalyst(MmceBlocks.BLOCK_FACTORY_CONTROLLER.get(), type);
            }
            ItemStack blueprint = MmceBlueprintData.stackFor(MmceItems.BLUEPRINT.get(), machine.id());
            registration.addRecipeCatalyst(blueprint, type);
        }
    }

    private static List<MmceMachineDefinition> sortedMachines() {
        return MmceDataRegistry.snapshot().machines().values().stream()
                .sorted(Comparator.comparing(machine -> machine.id().toString()))
                .toList();
    }

    private static List<MmceRecipeDefinition> recipesFor(MmceMachineDefinition machine) {
        List<MmceRecipeDefinition> recipes = new ArrayList<>(MmceDataRegistry.getRecipesFor(machine.id()));
        recipes.removeIf(recipe -> !recipe.loadJei());
        recipes.sort(Comparator.comparingInt(MmceRecipeDefinition::priority)
                .thenComparing(recipe -> recipe.id().toString()));
        return recipes;
    }

    private static RecipeType<MmceJeiMachineRecipe> recipeType(MmceMachineDefinition machine) {
        ResourceLocation uid = ResourceLocation.fromNamespaceAndPath(
                ModularMachineryNeoForge.MODID,
                "machine/" + machine.id().getNamespace() + "/" + machine.id().getPath()
        );
        return new RecipeType<>(uid, MmceJeiMachineRecipe.class);
    }

    private static Component title(MmceMachineDefinition machine) {
        if (machine.localizedName().isBlank()) {
            return Component.literal(machine.id().toString());
        }
        return Component.literal(machine.localizedName());
    }
}
