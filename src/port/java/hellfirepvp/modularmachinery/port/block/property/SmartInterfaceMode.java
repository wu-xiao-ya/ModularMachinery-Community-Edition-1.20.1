package hellfirepvp.modularmachinery.port.block.property;

import net.minecraft.util.StringRepresentable;

public enum SmartInterfaceMode implements StringRepresentable {
    NUMBER,
    STRING;

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
