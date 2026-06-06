package hellfirepvp.modularmachinery.port.block.property;

import net.minecraft.util.StringRepresentable;

public enum FluidHatchSize implements StringRepresentable {
    TINY(100),
    SMALL(400),
    NORMAL(1_000),
    REINFORCED(2_000),
    BIG(4_500),
    HUGE(8_000),
    LUDICROUS(16_000),
    VACUUM(32_000);

    private final int capacity;

    FluidHatchSize(int capacity) {
        this.capacity = capacity;
    }

    public int capacity() {
        return capacity;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
