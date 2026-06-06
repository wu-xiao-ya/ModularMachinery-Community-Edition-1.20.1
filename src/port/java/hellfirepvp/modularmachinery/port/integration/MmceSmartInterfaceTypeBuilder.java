package hellfirepvp.modularmachinery.port.integration;

import com.google.gson.JsonObject;

public class MmceSmartInterfaceTypeBuilder {
    private final JsonObject root = new JsonObject();

    public MmceSmartInterfaceTypeBuilder(String type, float defaultValue) {
        root.addProperty("type", type == null ? "" : type.trim());
        root.addProperty("defaultValue", defaultValue);
    }

    public static MmceSmartInterfaceTypeBuilder create(String type, float defaultValue) {
        return new MmceSmartInterfaceTypeBuilder(type, defaultValue);
    }

    public MmceSmartInterfaceTypeBuilder setHeaderInfo(String value) {
        root.addProperty("headerInfo", value == null ? "" : value);
        return this;
    }

    public MmceSmartInterfaceTypeBuilder setValueInfo(String value) {
        root.addProperty("valueInfo", value == null ? "" : value);
        return this;
    }

    public MmceSmartInterfaceTypeBuilder setFooterInfo(String value) {
        root.addProperty("footerInfo", value == null ? "" : value);
        return this;
    }

    public MmceSmartInterfaceTypeBuilder setNotEqualMessage(String value) {
        root.addProperty("notEqualMessage", value == null ? "" : value);
        return this;
    }

    public MmceSmartInterfaceTypeBuilder setPriority(int priority) {
        root.addProperty("priority", priority);
        return this;
    }

    public MmceSmartInterfaceTypeBuilder setJeiTooltip(String value, int argsCount) {
        root.addProperty("jeiTooltip", value == null ? "" : value);
        root.addProperty("jeiTooltipArgsCount", Math.max(0, argsCount));
        return this;
    }

    public JsonObject json() {
        return root.deepCopy();
    }
}
