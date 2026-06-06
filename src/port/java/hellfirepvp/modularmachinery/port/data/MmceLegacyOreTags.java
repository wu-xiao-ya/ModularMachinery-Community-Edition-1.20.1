package hellfirepvp.modularmachinery.port.data;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

final class MmceLegacyOreTags {
    static Optional<TagKey<Item>> itemTag(String legacyOreName) {
        return commonTagPath(legacyOreName)
                .map(path -> TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path)));
    }

    static List<ResourceLocation> fallbackItems(String legacyOreName) {
        return switch (legacyOreName) {
            case "ingotIron" -> List.of(ResourceLocation.withDefaultNamespace("iron_ingot"));
            case "ingotGold" -> List.of(ResourceLocation.withDefaultNamespace("gold_ingot"));
            case "dustRedstone" -> List.of(ResourceLocation.withDefaultNamespace("redstone"));
            case "dustGlowstone" -> List.of(ResourceLocation.withDefaultNamespace("glowstone_dust"));
            case "gemDiamond" -> List.of(ResourceLocation.withDefaultNamespace("diamond"));
            case "gemEmerald" -> List.of(ResourceLocation.withDefaultNamespace("emerald"));
            case "coal" -> List.of(ResourceLocation.withDefaultNamespace("coal"));
            case "charcoal" -> List.of(ResourceLocation.withDefaultNamespace("charcoal"));
            default -> List.of();
        };
    }

    private static Optional<String> commonTagPath(String legacyOreName) {
        for (Prefix prefix : PREFIXES) {
            if (legacyOreName.startsWith(prefix.oldPrefix()) && legacyOreName.length() > prefix.oldPrefix().length()) {
                return Optional.of(prefix.commonDirectory() + "/" + lowerFirst(legacyOreName.substring(prefix.oldPrefix().length())));
            }
        }
        return Optional.empty();
    }

    private static String lowerFirst(String value) {
        if (value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toLowerCase(Locale.ROOT) + value.substring(1);
    }

    private record Prefix(String oldPrefix, String commonDirectory) {
    }

    private static final Set<Prefix> PREFIXES = Set.of(
            new Prefix("ingot", "ingots"),
            new Prefix("dust", "dusts"),
            new Prefix("ore", "ores"),
            new Prefix("gem", "gems"),
            new Prefix("plate", "plates"),
            new Prefix("nugget", "nuggets"),
            new Prefix("block", "storage_blocks")
    );

    private MmceLegacyOreTags() {
    }
}
