package hellfirepvp.modularmachinery.port.block.property;

import net.minecraft.util.StringRepresentable;

public enum ItemBusSize implements StringRepresentable {
    TINY(1),
    SMALL(4),
    NORMAL(6),
    REINFORCED(9),
    BIG(12),
    HUGE(16),
    LUDICROUS(32);

    private final int slots;

    ItemBusSize(int slots) {
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
