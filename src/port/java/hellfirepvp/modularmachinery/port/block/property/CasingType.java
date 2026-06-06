package hellfirepvp.modularmachinery.port.block.property;

import net.minecraft.util.StringRepresentable;

public enum CasingType implements StringRepresentable {
    PLAIN,
    VENT,
    FIREBOX,
    GEARBOX,
    REINFORCED,
    CIRCUITRY;

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
