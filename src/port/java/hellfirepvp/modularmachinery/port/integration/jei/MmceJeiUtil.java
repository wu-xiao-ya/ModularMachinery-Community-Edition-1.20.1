package hellfirepvp.modularmachinery.port.integration.jei;

import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.data.MmceFluidRequirement;
import hellfirepvp.modularmachinery.port.data.MmceIngredientArrayRequirement;
import hellfirepvp.modularmachinery.port.data.MmceItemRequirement;
import hellfirepvp.modularmachinery.port.data.MmceParsedRequirement;
import hellfirepvp.modularmachinery.port.data.MmceRecipeRequirement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

final class MmceJeiUtil {
    static final int SLOT_SIZE = 18;

    private MmceJeiUtil() {
    }

    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, path);
    }

    static List<ItemStack> itemStacks(MmceRecipeRequirement requirement) {
        return requirement.parsed()
                .map(MmceJeiUtil::itemStacks)
                .orElseGet(List::of);
    }

    static List<ItemStack> itemStacks(MmceParsedRequirement requirement) {
        if (requirement instanceof MmceItemRequirement itemRequirement) {
            return itemStacks(itemRequirement);
        }
        if (requirement instanceof MmceIngredientArrayRequirement arrayRequirement) {
            List<ItemStack> stacks = new ArrayList<>();
            for (MmceItemRequirement candidate : arrayRequirement.candidates()) {
                stacks.addAll(itemStacks(candidate));
            }
            return stacks;
        }
        return List.of();
    }

    static List<ItemStack> itemStacks(MmceItemRequirement requirement) {
        List<ItemStack> stacks = new ArrayList<>();
        ItemStack directStack = requirement.createStack(displayAmount(requirement));
        if (!directStack.isEmpty()) {
            stacks.add(directStack);
        }
        requirement.itemTag().ifPresent(tag -> BuiltInRegistries.ITEM.getTag(tag).ifPresent(named ->
                named.stream()
                        .map(holder -> itemStack(holder.value(), displayAmount(requirement)))
                        .filter(stack -> !stack.isEmpty())
                        .forEach(stacks::add)
        ));
        for (ResourceLocation fallbackId : requirement.fallbackItemIds()) {
            Item item = BuiltInRegistries.ITEM.get(fallbackId);
            ItemStack stack = itemStack(item, displayAmount(requirement));
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks.stream().distinct().toList();
    }

    static boolean hasItemIngredient(MmceParsedRequirement requirement) {
        return requirement instanceof MmceItemRequirement
                || requirement instanceof MmceIngredientArrayRequirement arrayRequirement && !arrayRequirement.candidates().isEmpty();
    }

    static void addItemIngredients(IIngredientAcceptor<?> acceptor, MmceRecipeRequirement requirement) {
        requirement.parsed().ifPresent(parsed -> addItemIngredients(acceptor, parsed));
    }

    private static void addItemIngredients(IIngredientAcceptor<?> acceptor, MmceParsedRequirement requirement) {
        if (requirement instanceof MmceItemRequirement itemRequirement) {
            addItemRequirement(acceptor, itemRequirement);
        } else if (requirement instanceof MmceIngredientArrayRequirement arrayRequirement) {
            for (MmceItemRequirement candidate : arrayRequirement.candidates()) {
                addItemRequirement(acceptor, candidate);
            }
        }
    }

    private static void addItemRequirement(IIngredientAcceptor<?> acceptor, MmceItemRequirement requirement) {
        requirement.itemTag().ifPresent(tag -> acceptor.addIngredients(Ingredient.of(tag)));
        List<ItemStack> stacks = itemStacks(requirement);
        if (!stacks.isEmpty()) {
            acceptor.addItemStacks(stacks);
        }
    }

    static List<FluidStack> fluidStacks(MmceRecipeRequirement requirement) {
        return requirement.parsed()
                .filter(MmceFluidRequirement.class::isInstance)
                .map(MmceFluidRequirement.class::cast)
                .map(fluid -> fluid.createStack(fluid.amount()))
                .filter(stack -> !stack.isEmpty())
                .map(List::of)
                .orElseGet(List::of);
    }

    static List<Component> slotTooltip(MmceRecipeRequirement requirement) {
        List<Component> tooltip = new ArrayList<>();
        requirement.parsed().ifPresent(parsed -> {
            if (parsed instanceof MmceIngredientArrayRequirement arrayRequirement && arrayRequirement.optional()) {
                tooltip.add(Component.translatable("tooltip.machinery.catalyst").withStyle(ChatFormatting.GREEN));
            }
            if (parsed.chance() < 1.0F) {
                tooltip.add(chanceTooltip(parsed));
            }
            if (parsed instanceof MmceItemRequirement itemRequirement) {
                addAmountTooltip(tooltip, itemRequirement);
                if (itemRequirement.durabilityCost() > 0) {
                    tooltip.add(Component.literal("Durability: " + itemRequirement.durabilityCost()).withStyle(ChatFormatting.GRAY));
                }
            } else if (parsed instanceof MmceIngredientArrayRequirement arrayRequirement) {
                if (arrayRequirement.optional()) {
                    tooltip.add(Component.literal("Optional input").withStyle(ChatFormatting.GRAY));
                } else {
                    tooltip.add(Component.translatable("tooltip.machinery.ingredient_array_input").withStyle(ChatFormatting.GRAY));
                }
            } else if (parsed instanceof MmceFluidRequirement fluidRequirement) {
                if (fluidRequirement.perTick()) {
                    tooltip.add(Component.translatable(
                            fluidRequirement.ioType().name().equals("INPUT") ? "tooltip.fluid_pertick.in" : "tooltip.fluid_pertick.out",
                            format(fluidRequirement.amount())
                    ).withStyle(ChatFormatting.GRAY));
                }
            }
        });
        requirement.parseIssue().ifPresent(issue ->
                tooltip.add(Component.literal(issue).withStyle(ChatFormatting.RED))
        );
        requirement.selectorTag().ifPresent(selector ->
                tooltip.add(Component.literal("Selector: " + selector).withStyle(ChatFormatting.DARK_GRAY))
        );
        return tooltip;
    }

    static String format(long value) {
        return NumberFormat.getIntegerInstance(Locale.ROOT).format(value);
    }

    private static ItemStack itemStack(Item item, int count) {
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, Math.max(1, Math.min(count, item.getDefaultMaxStackSize())));
    }

    private static int displayAmount(MmceItemRequirement requirement) {
        return Math.max(1, requirement.amount());
    }

    private static Component chanceTooltip(MmceParsedRequirement requirement) {
        int percent = Math.round(requirement.chance() * 100.0F);
        String key = requirement.ioType().name().equals("INPUT")
                ? "tooltip.machinery.chance.in"
                : "tooltip.machinery.chance.out";
        return Component.translatable(key, percent + "%").withStyle(ChatFormatting.GRAY);
    }

    private static void addAmountTooltip(List<Component> tooltip, MmceItemRequirement requirement) {
        if (requirement.minAmount() == requirement.maxAmount()) {
            return;
        }
        String key = requirement.ioType().name().equals("INPUT")
                ? "tooltip.machinery.min_max_amount.input"
                : "tooltip.machinery.min_max_amount.output";
        tooltip.add(Component.translatable(key, requirement.minAmount(), requirement.maxAmount()).withStyle(ChatFormatting.GRAY));
    }
}
