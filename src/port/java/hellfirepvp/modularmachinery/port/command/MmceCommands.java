package hellfirepvp.modularmachinery.port.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import hellfirepvp.modularmachinery.port.ModularMachineryNeoForge;
import hellfirepvp.modularmachinery.port.data.MmceDataLoadIssue;
import hellfirepvp.modularmachinery.port.data.MmceDataRegistry;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.data.MmceRecipeDefinition;
import hellfirepvp.modularmachinery.port.data.MmceRecipeRequirement;
import hellfirepvp.modularmachinery.port.data.MmceSmartInterfaceRequirement;
import hellfirepvp.modularmachinery.port.item.MmceBlueprintData;
import hellfirepvp.modularmachinery.port.machine.MmceStructureDiagnostics;
import hellfirepvp.modularmachinery.port.registry.MmceItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class MmceCommands {
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("mm-get_blueprint")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("machine", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                MmceDataRegistry.snapshot().machines().keySet().stream()
                                        .sorted(Comparator.comparing(ResourceLocation::toString))
                                        .map(MmceCommands::suggestionFor)
                                        .toList(),
                                builder))
                        .executes(context -> giveBlueprint(
                                context.getSource(),
                                StringArgumentType.getString(context, "machine")))));

        dispatcher.register(Commands.literal("mm-hand")
                .requires(source -> source.hasPermission(2))
                .executes(context -> describeHeldItem(context.getSource())));

        dispatcher.register(Commands.literal("mm-syntax")
                .requires(source -> source.hasPermission(2))
                .executes(context -> syntaxReport(context.getSource())));

        dispatcher.register(Commands.literal("mmce")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("get_blueprint")
                        .then(Commands.argument("machine", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        MmceDataRegistry.snapshot().machines().keySet().stream()
                                                .sorted(Comparator.comparing(ResourceLocation::toString))
                                                .map(MmceCommands::suggestionFor)
                                                .toList(),
                                        builder))
                                .executes(context -> giveBlueprint(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "machine")))))
                .then(Commands.literal("hand")
                        .executes(context -> describeHeldItem(context.getSource())))
                .then(Commands.literal("syntax")
                        .executes(context -> syntaxReport(context.getSource()))));
    }

    private static int giveBlueprint(CommandSourceStack source, String machineName) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        ResourceLocation machineId;
        try {
            machineId = parseMachineId(machineName);
        } catch (Exception exception) {
            source.sendFailure(Component.literal("Invalid MMCE machine id: " + machineName
                    + " (use path or namespace:path)"));
            return 0;
        }
        if (!MmceDataRegistry.snapshot().machines().containsKey(machineId)) {
            source.sendFailure(Component.literal("Unknown MMCE machine: " + machineId));
            return 0;
        }

        ItemStack blueprint = MmceBlueprintData.stackFor(MmceItems.BLUEPRINT.get(), machineId);
        if (!player.getInventory().add(blueprint)) {
            player.drop(blueprint, false);
        }
        source.sendSuccess(() -> Component.literal("Given blueprint for " + machineId), false);
        return 1;
    }

    private static int describeHeldItem(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            held = player.getOffhandItem();
        }
        if (held.isEmpty()) {
            source.sendFailure(Component.literal("No item in either hand."));
            return 0;
        }

        String description = itemDescription(held);
        source.sendSuccess(() -> Component.literal(description)
                .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, description))), false);
        int burnTime = held.getBurnTime(null);
        if (burnTime > 0) {
            source.sendSuccess(() -> Component.literal("Fuel BurnTime: " + burnTime), false);
        }
        return 1;
    }

    private static int syntaxReport(CommandSourceStack source) {
        MmceDataRegistry.Snapshot snapshot = MmceDataRegistry.snapshot();
        List<MmceRecipeDefinition> recipesWithMissingMachines = snapshot.recipes().values().stream()
                .filter(recipe -> !snapshot.machines().containsKey(recipe.machineId()))
                .toList();
        List<ResourceLocation> adaptersWithMissingMachines = snapshot.adapters().values().stream()
                .filter(adapter -> !snapshot.machines().containsKey(adapter.machineId()))
                .map(adapter -> adapter.id())
                .toList();
        List<MmceRecipeDefinition> emptyRequirementRecipes = snapshot.recipes().values().stream()
                .filter(recipe -> recipe.requirements().isEmpty())
                .toList();
        List<RequirementIssue> unsupportedRequirements = new ArrayList<>();
        snapshot.recipes().values().forEach(recipe ->
                collectRequirementIssues("recipe " + recipe.id(), recipe.requirements(), unsupportedRequirements));
        snapshot.adapters().values().forEach(adapter ->
                collectRequirementIssues("adapter " + adapter.id(), adapter.requirements(), unsupportedRequirements));
        List<String> threadIssues = collectCoreThreadIssues(snapshot);
        List<String> smartInterfaceIssues = collectSmartInterfaceIssues(snapshot);
        List<MmceStructureDiagnostics.Issue> structureIssues = snapshot.machines().values().stream()
                .flatMap(machine -> MmceStructureDiagnostics.validate(machine, snapshot.variables()).stream())
                .toList();

        source.sendSuccess(() -> Component.literal("Testing Machines:"), false);
        source.sendSuccess(() -> Component.literal("Loaded machines: " + snapshot.machines().size()), false);
        source.sendSuccess(() -> Component.literal("Loaded variables: " + snapshot.variables().size()), false);
        source.sendSuccess(() -> Component.literal("Load issues: " + snapshot.loadIssues().size()), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("Testing Recipes:"), false);
        source.sendSuccess(() -> Component.literal("Loaded recipes: " + snapshot.recipes().size()), false);
        source.sendSuccess(() -> Component.literal("Loaded recipe adapters: " + snapshot.adapters().size()), false);

        if (snapshot.loadIssues().isEmpty()
                && recipesWithMissingMachines.isEmpty() && adaptersWithMissingMachines.isEmpty()
                && emptyRequirementRecipes.isEmpty() && unsupportedRequirements.isEmpty()
                && threadIssues.isEmpty() && smartInterfaceIssues.isEmpty()
                && structureIssues.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Syntax checks: OK"), false);
            return 1;
        }

        sendLoadFailures(source, snapshot.loadIssues());
        sendStructureFailures(source, structureIssues);
        sendRecipeMachineFailures(source, recipesWithMissingMachines);
        sendAdapterMachineFailures(source, adaptersWithMissingMachines);
        sendEmptyRequirementFailures(source, emptyRequirementRecipes);
        sendUnsupportedRequirementFailures(source, unsupportedRequirements);
        sendStringFailures(source, "Core thread issues", threadIssues);
        sendStringFailures(source, "Smart interface issues", smartInterfaceIssues);
        return 0;
    }

    private static List<String> collectCoreThreadIssues(MmceDataRegistry.Snapshot snapshot) {
        List<String> issues = new ArrayList<>();
        for (MmceMachineDefinition machine : snapshot.machines().values()) {
            Set<String> threadNames = machine.coreThreads().stream()
                    .map(MmceMachineDefinition.CoreThreadDefinition::threadName)
                    .filter(name -> !name.isBlank())
                    .collect(Collectors.toSet());
            Set<String> seenThreadNames = new HashSet<>();
            for (MmceMachineDefinition.CoreThreadDefinition thread : machine.coreThreads()) {
                if (!thread.threadName().isBlank() && !seenThreadNames.add(thread.threadName())) {
                    issues.add(machine.id() + " has duplicate core thread '" + thread.threadName() + "'");
                }
                if (!machine.hasFactory() && !machine.factoryOnly()) {
                    issues.add(machine.id() + " defines core thread '" + thread.threadName()
                            + "' but is not a factory machine");
                }
                for (ResourceLocation recipeId : thread.recipes()) {
                    MmceRecipeDefinition recipe = snapshot.recipes().get(recipeId);
                    if (recipe == null) {
                        issues.add(machine.id() + " core thread '" + thread.threadName()
                                + "' references missing recipe " + recipeId);
                    } else if (!recipe.machineId().equals(machine.id())) {
                        issues.add(machine.id() + " core thread '" + thread.threadName()
                                + "' references recipe " + recipeId + " for machine " + recipe.machineId());
                    }
                }
            }
            snapshot.recipes().values().stream()
                    .filter(recipe -> recipe.machineId().equals(machine.id()))
                    .filter(recipe -> !recipe.threadName().isBlank())
                    .filter(recipe -> !threadNames.contains(recipe.threadName()))
                    .forEach(recipe -> issues.add("recipe " + recipe.id()
                            + " requires missing core thread '" + recipe.threadName()
                            + "' on " + machine.id()));
        }
        snapshot.recipes().values().stream()
                .filter(recipe -> recipe.maxThreads() == 0)
                .forEach(recipe -> issues.add("recipe " + recipe.id() + " has maxThreads=0"));
        return List.copyOf(issues);
    }

    private static List<String> collectSmartInterfaceIssues(MmceDataRegistry.Snapshot snapshot) {
        List<String> issues = new ArrayList<>();
        for (MmceRecipeDefinition recipe : snapshot.recipes().values()) {
            MmceMachineDefinition machine = snapshot.machines().get(recipe.machineId());
            if (machine == null) {
                continue;
            }
            Set<String> types = machine.smartInterfaceTypes().stream()
                    .map(MmceMachineDefinition.SmartInterfaceTypeDefinition::type)
                    .collect(Collectors.toSet());
            for (MmceRecipeRequirement requirement : recipe.requirements()) {
                if (requirement.parsed().orElse(null) instanceof MmceSmartInterfaceRequirement smart
                        && !types.contains(smart.interfaceType())) {
                    issues.add("recipe " + recipe.id() + " uses undefined smart interface type '"
                            + smart.interfaceType() + "' on " + machine.id());
                }
            }
        }
        snapshot.adapters().values().forEach(adapter -> {
            MmceMachineDefinition machine = snapshot.machines().get(adapter.machineId());
            if (machine == null) {
                return;
            }
            Set<String> types = machine.smartInterfaceTypes().stream()
                    .map(MmceMachineDefinition.SmartInterfaceTypeDefinition::type)
                    .collect(Collectors.toSet());
            for (MmceRecipeRequirement requirement : adapter.requirements()) {
                if (requirement.parsed().orElse(null) instanceof MmceSmartInterfaceRequirement smart
                        && !types.contains(smart.interfaceType())) {
                    issues.add("adapter " + adapter.id() + " uses undefined smart interface type '"
                            + smart.interfaceType() + "' on " + machine.id());
                }
            }
        });
        return List.copyOf(issues);
    }

    private static void collectRequirementIssues(
            String owner,
            List<MmceRecipeRequirement> requirements,
            List<RequirementIssue> output
    ) {
        for (int i = 0; i < requirements.size(); i++) {
            MmceRecipeRequirement requirement = requirements.get(i);
            if (requirement.parsed().isEmpty()) {
                output.add(new RequirementIssue(
                        owner,
                        i,
                        requirement.type(),
                        requirement.ioType().map(Enum::name).orElse("none"),
                        requirement.parseIssue().orElse("parser returned no executable requirement")));
            }
        }
    }

    private static void sendRecipeMachineFailures(CommandSourceStack source, List<MmceRecipeDefinition> recipes) {
        if (recipes.isEmpty()) {
            return;
        }
        source.sendFailure(Component.literal("Recipes with missing machine references: " + recipes.size()));
        recipes.stream()
                .limit(8)
                .forEach(recipe -> source.sendFailure(Component.literal(" - " + recipe.id() + " -> " + recipe.machineId())));
        sendMore(source, recipes.size());
    }

    private static void sendAdapterMachineFailures(CommandSourceStack source, List<ResourceLocation> adapters) {
        if (adapters.isEmpty()) {
            return;
        }
        source.sendFailure(Component.literal("Adapters with missing machine references: " + adapters.size()));
        adapters.stream()
                .limit(8)
                .forEach(adapter -> source.sendFailure(Component.literal(" - " + adapter)));
        sendMore(source, adapters.size());
    }

    private static void sendEmptyRequirementFailures(CommandSourceStack source, List<MmceRecipeDefinition> recipes) {
        if (recipes.isEmpty()) {
            return;
        }
        source.sendFailure(Component.literal("Recipes with empty requirements: " + recipes.size()));
        recipes.stream()
                .limit(8)
                .forEach(recipe -> source.sendFailure(Component.literal(" - " + recipe.id())));
        sendMore(source, recipes.size());
    }

    private static void sendUnsupportedRequirementFailures(CommandSourceStack source, List<RequirementIssue> issues) {
        if (issues.isEmpty()) {
            return;
        }
        source.sendFailure(Component.literal("Unsupported or unparsed requirements: " + issues.size()));
        issues.stream()
                .limit(8)
                .forEach(issue -> source.sendFailure(Component.literal(" - " + issue.owner()
                        + " requirements[" + issue.index() + "] type=" + issue.type()
                        + " io=" + issue.ioType()
                        + ": " + issue.reason())));
        sendMore(source, issues.size());
    }

    private static void sendLoadFailures(CommandSourceStack source, List<MmceDataLoadIssue> issues) {
        if (issues.isEmpty()) {
            return;
        }
        source.sendFailure(Component.literal("Data load issues: " + issues.size()));
        issues.stream()
                .limit(8)
                .forEach(issue -> source.sendFailure(Component.literal(" - " + issue.category()
                        + " " + issue.sourceId() + ": " + issue.message())));
        sendMore(source, issues.size());
    }

    private static void sendStructureFailures(CommandSourceStack source, List<MmceStructureDiagnostics.Issue> issues) {
        if (issues.isEmpty()) {
            return;
        }
        source.sendFailure(Component.literal("Machine structure issues: " + issues.size()));
        issues.stream()
                .limit(8)
                .forEach(issue -> source.sendFailure(Component.literal(" - " + issue.owner() + ": " + issue.message())));
        sendMore(source, issues.size());
    }

    private static void sendStringFailures(CommandSourceStack source, String title, List<String> issues) {
        if (issues.isEmpty()) {
            return;
        }
        source.sendFailure(Component.literal(title + ": " + issues.size()));
        issues.stream()
                .limit(8)
                .forEach(issue -> source.sendFailure(Component.literal(" - " + issue)));
        sendMore(source, issues.size());
    }

    private static void sendMore(CommandSourceStack source, int total) {
        if (total > 8) {
            source.sendFailure(Component.literal(" - ... and " + (total - 8) + " more"));
        }
    }

    private static String itemDescription(ItemStack stack) {
        StringBuilder builder = new StringBuilder(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        CompoundTag customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (stack.has(net.minecraft.core.component.DataComponents.DAMAGE)) {
            customData.putInt("Damage", stack.getOrDefault(net.minecraft.core.component.DataComponents.DAMAGE, 0));
        }
        if (!customData.isEmpty()) {
            builder.append(" (with nbt: ").append(customData).append(" )");
        }
        return builder.toString();
    }

    private static ResourceLocation parseMachineId(String value) {
        return value.indexOf(':') >= 0
                ? ResourceLocation.parse(value)
                : ResourceLocation.fromNamespaceAndPath(ModularMachineryNeoForge.MODID, value);
    }

    private static String suggestionFor(ResourceLocation id) {
        return ModularMachineryNeoForge.MODID.equals(id.getNamespace()) ? id.getPath() : id.toString();
    }

    private record RequirementIssue(String owner, int index, ResourceLocation type, String ioType, String reason) {
    }

    private MmceCommands() {
    }
}
