package hellfirepvp.modularmachinery.port.data;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Optional;

public record MmceStructurePart(
        List<Integer> x,
        List<Integer> y,
        List<Integer> z,
        List<String> elements,
        Optional<String> selectorTag,
        Optional<String> checkerId,
        JsonObject matchNbt,
        JsonObject previewNbt,
        JsonObject rawJson
) {
    static MmceStructurePart parse(JsonObject part) {
        return new MmceStructurePart(
                MmceJsonUtil.coordinates(part, "x"),
                MmceJsonUtil.coordinates(part, "y"),
                MmceJsonUtil.coordinates(part, "z"),
                MmceJsonUtil.stringOrArray(part, "elements"),
                MmceJsonUtil.optionalString(part, "selector-tag", "selectorTag", "selector_tag")
                        .filter(tag -> !tag.isBlank()),
                MmceJsonUtil.optionalString(part, "checker-id", "checkerId", "block-checker", "blockChecker").filter(id -> !id.isBlank()),
                MmceJsonUtil.rawObject(part, "nbt", "match-nbt", "matchNbt", "match_nbt"),
                MmceJsonUtil.rawObject(part, "preview-nbt", "previewNbt", "preview_nbt", "nbt-display", "nbtDisplay", "nbt_display"),
                part.deepCopy()
        );
    }

    public int permutationCount() {
        return x.size() * y.size() * z.size();
    }
}
