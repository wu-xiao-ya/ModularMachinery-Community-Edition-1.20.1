package hellfirepvp.modularmachinery.port.block.property;

import net.minecraft.util.StringRepresentable;

public enum UpgradeBusTier implements StringRepresentable {
    NORMAL(3),
    REINFORCED(6),
    ELITE(9),
    SUPER(12),
    ULTIMATE(18);

    private final int slots;

    UpgradeBusTier(int slots) {
        this.slots = slots;
    }

    public int slots() {
        return slots;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
