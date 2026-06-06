package hellfirepvp.modularmachinery.port.integration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MmceItemCallbackRegistry {
    private static final Map<String, MmceItemChecker> CHECKERS = new ConcurrentHashMap<>();
    private static final Map<String, MmceItemModifier> MODIFIERS = new ConcurrentHashMap<>();

    public static String registerChecker(String id, MmceItemChecker checker) {
        if (checker == null) {
            return "";
        }
        String normalized = normalizeId(id, CHECKERS.size(), "item_checker_");
        CHECKERS.put(normalized, checker);
        return normalized;
    }

    public static String registerModifier(String id, MmceItemModifier modifier) {
        if (modifier == null) {
            return "";
        }
        String normalized = normalizeId(id, MODIFIERS.size(), "item_modifier_");
        MODIFIERS.put(normalized, modifier);
        return normalized;
    }

    public static MmceItemChecker checker(String id) {
        return id == null || id.isBlank() ? null : CHECKERS.get(id.trim());
    }

    public static MmceItemModifier modifier(String id) {
        return id == null || id.isBlank() ? null : MODIFIERS.get(id.trim());
    }

    public static void clear() {
        CHECKERS.clear();
        MODIFIERS.clear();
    }

    private static String normalizeId(String id, int size, String prefix) {
        String normalized = id == null ? "" : id.trim();
        return normalized.isBlank() ? prefix + size : normalized;
    }

    private MmceItemCallbackRegistry() {
    }
}
