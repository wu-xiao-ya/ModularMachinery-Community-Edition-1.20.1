package hellfirepvp.modularmachinery.port.integration.jei;

import hellfirepvp.modularmachinery.port.data.MmceEnergyRequirement;
import hellfirepvp.modularmachinery.port.data.MmceFluidRequirement;
import hellfirepvp.modularmachinery.port.data.MmceIoType;
import hellfirepvp.modularmachinery.port.data.MmceParsedRequirement;
import hellfirepvp.modularmachinery.port.data.MmceRecipeRequirement;
import hellfirepvp.modularmachinery.port.registry.MmceItems;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class MmceJeiMachineRecipeCategory implements IRecipeCategory<MmceJeiMachineRecipe> {
    private static final int WIDTH = 162;
    private static final int HEIGHT = 96;
    private static final int INPUT_X = 8;
    private static final int OUTPUT_X = 112;
    private static final int SLOT_Y = 18;
    private static final int SLOT_COLUMNS = 2;

    private final RecipeType<MmceJeiMachineRecipe> recipeType;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;

    MmceJeiMachineRecipeCategory(IGuiHelper guiHelper, RecipeType<MmceJeiMachineRecipe> recipeType, Component title) {
        this.recipeType = recipeType;
        this.title = title;
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemLike(MmceItems.BLUEPRINT.get());
    }

    @Override
    public RecipeType<MmceJeiMachineRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MmceJeiMachineRecipe recipe, IFocusGroup focuses) {
        addSlots(builder, recipe, MmceIoType.INPUT, INPUT_X);
        addSlots(builder, recipe, MmceIoType.OUTPUT, OUTPUT_X);
    }

    @Override
    public void draw(MmceJeiMachineRecipe recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        int color = 0xFF404040;
        guiGraphics.drawString(minecraft.font,
                Component.translatable("tooltip.machinery.duration.seconds", recipe.recipe().recipeTime() / 20.0F),
                8, 4, color, false);
        guiGraphics.drawString(minecraft.font, Component.literal("->"), 76, 42, color, false);

        List<Component> energyLines = energyLines(recipe);
        int y = 70;
        for (Component line : energyLines) {
            guiGraphics.drawString(minecraft.font, line, 8, y, color, false);
            y += minecraft.font.lineHeight + 1;
        }

        int tooltipY = 70;
        for (String tooltip : recipe.recipe().recipeTooltips()) {
            if (tooltipY > HEIGHT - minecraft.font.lineHeight) {
                break;
            }
            guiGraphics.drawString(minecraft.font, Component.literal(tooltip), 78, tooltipY, color, false);
            tooltipY += minecraft.font.lineHeight + 1;
        }
    }

    @Override
    public ResourceLocation getRegistryName(MmceJeiMachineRecipe recipe) {
        return recipe.recipe().id();
    }

    private static void addSlots(IRecipeLayoutBuilder builder, MmceJeiMachineRecipe recipe, MmceIoType ioType, int x) {
        List<MmceRecipeRequirement> visibleRequirements = visibleRequirements(recipe, ioType);
        for (int index = 0; index < visibleRequirements.size(); index++) {
            MmceRecipeRequirement requirement = visibleRequirements.get(index);
            int slotX = x + (index % SLOT_COLUMNS) * MmceJeiUtil.SLOT_SIZE;
            int slotY = SLOT_Y + (index / SLOT_COLUMNS) * MmceJeiUtil.SLOT_SIZE;
            if (slotY > 54) {
                addInvisible(builder, requirement, ioType);
                continue;
            }

            RecipeIngredientRole role = ioType == MmceIoType.INPUT ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
            var slot = builder.addSlot(role, slotX, slotY)
                    .setStandardSlotBackground()
                    .setSlotName(ioType.name().toLowerCase() + "_" + index)
                    .addRichTooltipCallback(tooltipCallback(requirement));
            List<FluidStackBridge> fluids = fluidStacks(requirement);
            if (!fluids.isEmpty()) {
                slot.setFluidRenderer(fluids.getFirst().amount(), false, 16, 16);
                for (FluidStackBridge fluid : fluids) {
                    slot.addFluidStack(fluid.fluid(), fluid.amount(), fluid.components());
                }
            } else {
                MmceJeiUtil.addItemIngredients(slot, requirement);
            }
        }
    }

    private static void addInvisible(IRecipeLayoutBuilder builder, MmceRecipeRequirement requirement, MmceIoType ioType) {
        RecipeIngredientRole role = ioType == MmceIoType.INPUT ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
        var ingredients = builder.addInvisibleIngredients(role);
        List<FluidStackBridge> fluids = fluidStacks(requirement);
        if (!fluids.isEmpty()) {
            ingredients.addIngredients(NeoForgeTypes.FLUID_STACK, fluids.stream().map(FluidStackBridge::stack).toList());
        } else {
            MmceJeiUtil.addItemIngredients(ingredients, requirement);
        }
    }

    private static IRecipeSlotRichTooltipCallback tooltipCallback(MmceRecipeRequirement requirement) {
        return (recipeSlotView, tooltip) -> tooltip.addAll(MmceJeiUtil.slotTooltip(requirement));
    }

    private static List<MmceRecipeRequirement> visibleRequirements(MmceJeiMachineRecipe recipe, MmceIoType ioType) {
        return recipe.recipe().requirements().stream()
                .filter(requirement -> requirement.ioType().orElse(null) == ioType)
                .filter(requirement -> requirement.parsed()
                        .map(parsed -> parsed instanceof MmceEnergyRequirement ? false : supportsVisibleSlot(parsed))
                        .orElse(false))
                .toList();
    }

    private static boolean supportsVisibleSlot(MmceParsedRequirement requirement) {
        return MmceJeiUtil.hasItemIngredient(requirement) || requirement instanceof MmceFluidRequirement;
    }

    private static List<FluidStackBridge> fluidStacks(MmceRecipeRequirement requirement) {
        return MmceJeiUtil.fluidStacks(requirement).stream()
                .map(FluidStackBridge::new)
                .filter(bridge -> !bridge.stack().isEmpty())
                .toList();
    }

    private static List<Component> energyLines(MmceJeiMachineRecipe recipe) {
        long input = 0;
        long output = 0;
        for (MmceRecipeRequirement requirement : recipe.recipe().requirements()) {
            if (requirement.parsed().orElse(null) instanceof MmceEnergyRequirement energy) {
                if (energy.ioType() == MmceIoType.INPUT) {
                    input += energy.energyPerTick();
                } else {
                    output += energy.energyPerTick();
                }
            }
        }

        List<Component> lines = new ArrayList<>();
        if (input > 0) {
            lines.add(Component.translatable("tooltip.machinery.energy.in")
                    .append(" ")
                    .append(Component.translatable("tooltip.machinery.energy.tick", MmceJeiUtil.format(input), "FE"))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (output > 0) {
            lines.add(Component.translatable("tooltip.machinery.energy.out")
                    .append(" ")
                    .append(Component.translatable("tooltip.machinery.energy.tick", MmceJeiUtil.format(output), "FE"))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        return lines;
    }

    private record FluidStackBridge(net.neoforged.neoforge.fluids.FluidStack stack) {
        net.minecraft.world.level.material.Fluid fluid() {
            return stack.getFluid();
        }

        long amount() {
            return stack.getAmount();
        }

        net.minecraft.core.component.DataComponentPatch components() {
            return stack.getComponentsPatch();
        }
    }
}
