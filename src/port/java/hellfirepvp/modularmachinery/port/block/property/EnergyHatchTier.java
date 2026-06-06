package hellfirepvp.modularmachinery.port.block.property;

import net.minecraft.util.StringRepresentable;

public enum EnergyHatchTier implements StringRepresentable {
    TINY(2_048L, 128L),
    SMALL(4_096L, 512L),
    NORMAL(8_192L, 512L),
    REINFORCED(16_384L, 2_048L),
    BIG(32_768L, 8_192L),
    HUGE(131_072L, 32_768L),
    LUDICROUS(524_288L, 131_072L),
    ULTIMATE(2_097_152L, 131_072L);

    private final long capacity;
    private final long transferLimit;

    EnergyHatchTier(long capacity, long transferLimit) {
        this.capacity = capacity;
        this.transferLimit = transferLimit;
    }

    public long capacity() {
        return capacity;
    }

    public long transferLimit() {
        return transferLimit;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
