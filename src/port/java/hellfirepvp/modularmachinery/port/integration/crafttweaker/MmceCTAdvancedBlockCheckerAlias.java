package hellfirepvp.modularmachinery.port.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import hellfirepvp.modularmachinery.port.integration.MmceBlockChecker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.openzen.zencode.java.ZenCodeType;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.modularmachinery.AdvancedBlockChecker")
public interface MmceCTAdvancedBlockCheckerAlias extends MmceBlockChecker {
    @Override
    @ZenCodeType.Method
    boolean isMatch(Level level, BlockPos pos, BlockState blockState, CompoundTag nbt);
}
