package hellfirepvp.modularmachinery.port.integration;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface MmceBlockChecker {
    boolean isMatch(Level level, BlockPos pos, BlockState blockState, CompoundTag nbt);
}
