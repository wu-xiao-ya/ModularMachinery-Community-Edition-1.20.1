package hellfirepvp.modularmachinery.port.machine;

import hellfirepvp.modularmachinery.port.data.MmceDynamicPatternDefinition;
import hellfirepvp.modularmachinery.port.data.MmceMachineDefinition;
import hellfirepvp.modularmachinery.port.data.MmceMachineModifierDefinition;
import hellfirepvp.modularmachinery.port.data.MmceStructurePart;
import hellfirepvp.modularmachinery.port.integration.MmceBlockCheckerRegistry;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MmceStructureDiagnostics {
    public static List<Issue> validate(MmceMachineDefinition machine, Map<String, List<String>> variables) {
        List<Issue> issues = new ArrayList<>();
        if (machine.parts().isEmpty()) {
            add(issues, machine.id().toString(), "machine has no structure parts");
        }
        for (int i = 0; i < machine.parts().size(); i++) {
            validatePart(machine.id() + " parts[" + i + "]", machine.parts().get(i), variables, issues);
        }
        for (int i = 0; i < machine.modifiers().size(); i++) {
            validateModifier(machine.id() + " modifiers[" + i + "]", machine.modifiers().get(i), variables, issues);
        }
        for (int i = 0; i < machine.dynamicPatterns().size(); i++) {
            validateDynamicPattern(machine.id() + " dynamic-patterns[" + i + "]",
                    machine.dynamicPatterns().get(i), variables, issues);
        }
        return List.copyOf(issues);
    }

    private static void validateDynamicPattern(
            String owner,
            MmceDynamicPatternDefinition pattern,
            Map<String, List<String>> variables,
            List<Issue> issues
    ) {
        for (int i = 0; i < pattern.parts().size(); i++) {
            validatePart(owner + " parts[" + i + "]", pattern.parts().get(i), variables, issues);
        }
        for (int i = 0; i < pattern.partsEnd().size(); i++) {
            validatePart(owner + " parts-end[" + i + "]", pattern.partsEnd().get(i), variables, issues);
        }
    }

    private static void validatePart(
            String owner,
            MmceStructurePart part,
            Map<String, List<String>> variables,
            List<Issue> issues
    ) {
        if (part.elements().isEmpty()) {
            add(issues, owner, "has no elements");
            return;
        }
        if (part.x().isEmpty() || part.y().isEmpty() || part.z().isEmpty()) {
            add(issues, owner, "has an empty coordinate list");
        }
        part.checkerId()
                .filter(id -> MmceBlockCheckerRegistry.get(id) == null)
                .ifPresent(id -> add(issues, owner, "references unknown block checker '" + id + "'"));
        validateElements(owner, part.elements(), variables, issues);
    }

    private static void validateModifier(
            String owner,
            MmceMachineModifierDefinition modifier,
            Map<String, List<String>> variables,
            List<Issue> issues
    ) {
        if (modifier.elements().isEmpty()) {
            add(issues, owner, "has no elements");
            return;
        }
        if (modifier.x().isEmpty() || modifier.y().isEmpty() || modifier.z().isEmpty()) {
            add(issues, owner, "has an empty coordinate list");
        }
        modifier.checkerId()
                .filter(id -> MmceBlockCheckerRegistry.get(id) == null)
                .ifPresent(id -> add(issues, owner, "references unknown block checker '" + id + "'"));
        validateElements(owner, modifier.elements(), variables, issues);
    }

    private static void validateElements(
            String owner,
            List<String> elements,
            Map<String, List<String>> variables,
            List<Issue> issues
    ) {
        for (String element : elements) {
            validateElement(owner, element, variables, new LinkedHashSet<>(), issues);
        }
    }

    private static void validateElement(
            String owner,
            String element,
            Map<String, List<String>> variables,
            Set<String> visitedVariables,
            List<Issue> issues
    ) {
        if (element == null || element.isBlank()) {
            add(issues, owner, "has a blank element");
            return;
        }

        List<String> variableElements = variables.get(element);
        if (variableElements != null) {
            if (!visitedVariables.add(element)) {
                add(issues, owner, "variable cycle at '" + element + "'");
                return;
            }
            if (variableElements.isEmpty()) {
                add(issues, owner, "variable '" + element + "' is empty");
                return;
            }
            for (String variableElement : variableElements) {
                validateElement(owner + " -> " + element, variableElement, variables,
                        new LinkedHashSet<>(visitedVariables), issues);
            }
            return;
        }

        if (MmceBlockStateMatcher.parse(element).isEmpty()) {
            add(issues, owner, "invalid block element or unknown variable '" + element + "'");
        }
    }

    private static void add(List<Issue> issues, String owner, String message) {
        Issue issue = new Issue(owner, message);
        if (!issues.contains(issue)) {
            issues.add(issue);
        }
    }

    public record Issue(String owner, String message) {
    }

    private MmceStructureDiagnostics() {
    }
}
