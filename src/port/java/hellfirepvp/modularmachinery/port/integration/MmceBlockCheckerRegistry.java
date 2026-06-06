package hellfirepvp.modularmachinery.port.integration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MmceBlockCheckerRegistry {
    private static final Map<String, MmceBlockChecker> CHECKERS = new ConcurrentHashMap<>();

    public static String register(String id, MmceBlockChecker checker) {
        if (checker == null) {
            return "";
        }
        String normalized = normalizeId(id);
        CHECKERS.put(normalized, checker);
        return normalized;
    }

    public static MmceBlockChecker get(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return CHECKERS.get(id.trim());
    }

    public static void clear() {
        CHECKERS.clear();
    }

    private static String normalizeId(String id) {
        String normalized = id == null ? "" : id.trim();
        if (normalized.isBlank()) {
            normalized = "checker_" + CHECKERS.size();
        }
        return normalized;
    }

    private MmceBlockCheckerRegistry() {
    }
}
