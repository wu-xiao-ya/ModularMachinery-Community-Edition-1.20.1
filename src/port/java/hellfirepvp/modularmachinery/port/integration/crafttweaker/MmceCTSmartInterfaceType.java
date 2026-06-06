package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.port.integration.MmceSmartInterfaceTypeBuilder;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.SmartInterfaceType")
public final class MmceCTSmartInterfaceType {
    private final MmceSmartInterfaceTypeBuilder builder;

    private MmceCTSmartInterfaceType(String type, float defaultValue) {
        this.builder = MmceSmartInterfaceTypeBuilder.create(type, defaultValue);
    }

    @ZenCodeType.Method
    public static MmceCTSmartInterfaceType create(String type, float defaultValue) {
        return new MmceCTSmartInterfaceType(type, defaultValue);
    }

    @ZenCodeType.Method
    public MmceCTSmartInterfaceType setHeaderInfo(String value) {
        builder.setHeaderInfo(value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTSmartInterfaceType setValueInfo(String value) {
        builder.setValueInfo(value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTSmartInterfaceType setFooterInfo(String value) {
        builder.setFooterInfo(value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTSmartInterfaceType setNotEqualMessage(String value) {
        builder.setNotEqualMessage(value);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTSmartInterfaceType setPriority(int priority) {
        builder.setPriority(priority);
        return this;
    }

    @ZenCodeType.Method
    public MmceCTSmartInterfaceType setJeiTooltip(String value, int argsCount) {
        builder.setJeiTooltip(value, argsCount);
        return this;
    }

    JsonObject json() {
        return builder.json();
    }
}
