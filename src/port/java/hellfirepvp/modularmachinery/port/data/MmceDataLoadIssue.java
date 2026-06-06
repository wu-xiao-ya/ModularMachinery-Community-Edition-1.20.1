package hellfirepvp.modularmachinery.port.data;

import net.minecraft.resources.ResourceLocation;

public record MmceDataLoadIssue(
        String category,
        ResourceLocation sourceId,
        String message
) {
    public MmceDataLoadIssue {
        category = category == null ? "" : category;
        message = message == null ? "" : message;
    }

    public static MmceDataLoadIssue of(String category, ResourceLocation sourceId, RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }
        return new MmceDataLoadIssue(category, sourceId, message);
    }
}
