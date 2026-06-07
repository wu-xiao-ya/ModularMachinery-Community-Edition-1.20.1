package hellfirepvp.modularmachinery.port.blockentity;

import hellfirepvp.modularmachinery.port.block.FluidHatchBlock;
import hellfirepvp.modularmachinery.port.block.property.FluidHatchSize;
import hellfirepvp.modularmachinery.port.registry.MmceBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidHatchBlockEntity extends BaseMachineBlockEntity {
    private final boolean input;
    private final boolean processor;
    private FluidStack storedFluid = FluidStack.EMPTY;
    private ResourceLocation storedChemicalId;
    private int storedChemicalAmount;
    private CompoundTag storedChemicalNbt = new CompoundTag();

    public static FluidHatchBlockEntity input(BlockPos pos, BlockState state) {
        return new FluidHatchBlockEntity(MmceBlockEntities.FLUID_INPUT_HATCH.get(), pos, state, true, false);
    }

    public static FluidHatchBlockEntity output(BlockPos pos, BlockState state) {
        return new FluidHatchBlockEntity(MmceBlockEntities.FLUID_OUTPUT_HATCH.get(), pos, state, false, false);
    }

    public static FluidHatchBlockEntity processor(BlockPos pos, BlockState state) {
        return new FluidHatchBlockEntity(MmceBlockEntities.FLUID_PROCESSOR_HATCH.get(), pos, state, true, true);
    }

    private FluidHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, boolean input, boolean processor) {
        super(type, pos, state);
        this.input = input;
        this.processor = processor;
    }

    public boolean isInput() {
        return input;
    }

    public boolean isProcessor() {
        return processor;
    }

    @Override
    public boolean canConfigureGroupInput() {
        return input && !processor;
    }

    public FluidHatchSize getSize() {
        return getBlockState().hasProperty(FluidHatchBlock.SIZE)
                ? getBlockState().getValue(FluidHatchBlock.SIZE)
                : FluidHatchSize.NORMAL;
    }

    public int getCapacity() {
        return getSize().capacity();
    }

    public long getStoredAmount() {
        return storedFluid.getAmount();
    }

    public void setStoredAmount(long storedAmount) {
        if (storedFluid.isEmpty()) {
            return;
        }
        int clamped = (int) Math.max(0L, Math.min(storedAmount, getCapacity()));
        setStoredFluid(clamped <= 0 ? FluidStack.EMPTY : storedFluid.copyWithAmount(clamped));
    }

    public FluidStack getStoredFluid() {
        return storedFluid.copy();
    }

    public void setStoredFluid(FluidStack storedFluid) {
        FluidStack clamped = clampFluid(storedFluid);
        if (!FluidStack.matches(this.storedFluid, clamped)) {
            this.storedFluid = clamped;
            markForSync();
        }
    }

    public ResourceLocation getStoredChemicalId() {
        return storedChemicalId;
    }

    public int getStoredChemicalAmount() {
        return storedChemicalAmount;
    }

    public CompoundTag getStoredChemicalNbt() {
        return storedChemicalNbt.copy();
    }

    public boolean hasStoredChemical() {
        return storedChemicalId != null && storedChemicalAmount > 0;
    }

    public void clearStoredChemical() {
        setStoredChemical(null, 0, new CompoundTag());
    }

    public void setStoredChemical(ResourceLocation chemicalId, int amount, CompoundTag nbt) {
        int clamped = Math.max(0, Math.min(amount, getCapacity()));
        ResourceLocation newId = clamped <= 0 ? null : chemicalId;
        CompoundTag newNbt = clamped <= 0 || nbt == null ? new CompoundTag() : nbt.copy();
        if (sameChemical(this.storedChemicalId, newId)
                && this.storedChemicalAmount == clamped
                && this.storedChemicalNbt.equals(newNbt)) {
            return;
        }

        this.storedChemicalId = newId;
        this.storedChemicalAmount = newId == null ? 0 : clamped;
        this.storedChemicalNbt = newId == null ? new CompoundTag() : newNbt;
        markForSync();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        storedFluid = tag.contains("storedFluid", Tag.TAG_COMPOUND)
                ? clampFluid(FluidStack.parseOptional(registries, tag.getCompound("storedFluid")))
                : FluidStack.EMPTY;
        if (tag.contains("storedChemical", Tag.TAG_STRING)) {
            storedChemicalId = ResourceLocation.parse(tag.getString("storedChemical"));
            storedChemicalAmount = Math.max(0, Math.min(tag.getInt("storedChemicalAmount"), getCapacity()));
            storedChemicalNbt = tag.contains("storedChemicalNbt", Tag.TAG_COMPOUND)
                    ? tag.getCompound("storedChemicalNbt").copy()
                    : new CompoundTag();
            if (storedChemicalAmount <= 0) {
                clearStoredChemical();
            }
        } else {
            storedChemicalId = null;
            storedChemicalAmount = 0;
            storedChemicalNbt = new CompoundTag();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("storedFluid", storedFluid.saveOptional(registries));
        tag.putLong("storedAmount", getStoredAmount());
        if (hasStoredChemical()) {
            tag.putString("storedChemical", storedChemicalId.toString());
            tag.putInt("storedChemicalAmount", storedChemicalAmount);
            if (!storedChemicalNbt.isEmpty()) {
                tag.put("storedChemicalNbt", storedChemicalNbt.copy());
            }
        }
    }

    private FluidStack clampFluid(FluidStack fluid) {
        if (fluid.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return fluid.copyWithAmount(Math.min(fluid.getAmount(), getCapacity()));
    }

    private static boolean sameChemical(ResourceLocation left, ResourceLocation right) {
        return left == null ? right == null : left.equals(right);
    }
}
