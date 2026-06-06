package hellfirepvp.modularmachinery.port.block.property;

import net.minecraft.util.StringRepresentable;

public enum ParallelControllerTier implements StringRepresentable {
    NORMAL(4),
    REINFORCED(16),
    ELITE(64),
    SUPER(256),
    ULTIMATE(512);

    private final int maxParallelism;

    ParallelControllerTier(int maxParallelism) {
        this.maxParallelism = maxParallelism;
    }

    public int maxParallelism() {
        return maxParallelism;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
